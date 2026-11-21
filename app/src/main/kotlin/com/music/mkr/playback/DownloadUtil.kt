/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.music.innertube.YouTube
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.constants.IpVersionKey
import com.music.innertube.models.IpVersion
import okhttp3.Dns
import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.FormatEntity
import com.music.mkr.db.entities.LyricsEntity
import com.music.mkr.constants.SongSortType
import com.music.mkr.db.entities.SongEntity
import com.music.mkr.di.DownloadCache
import com.music.mkr.di.LyricsHelperEntryPoint
import com.music.mkr.di.PlayerCache
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.YTPlayerUtils
import com.music.mkr.utils.enumPreference
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import timber.log.Timber
import java.time.LocalDateTime
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadUtil
@Inject
constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
    val databaseProvider: DatabaseProvider,
    @DownloadCache val downloadCache: SimpleCache,
    @PlayerCache val playerCache: SimpleCache,
    private val cacheChanges: CacheSpanChanges,
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    private val ipVersion by enumPreference(context, IpVersionKey, IpVersion.AUTO)
    private val songUrlCache = StreamUrlCache()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())
    private val downloadsPaused = MutableStateFlow(false)
    private data class CompletionCheck(val download: Download, val stage: SongDownloadStage)
    private val completionChecks = MutableStateFlow<Map<String, CompletionCheck>>(emptyMap())

    // Media3 publishes byte changes into volatile fields, without publishing every change to its listeners.
    private val progressTicks = flow {
        while (true) {
            delay(250)
            emit(Unit)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed())

    private val stateLock = Mutex()
    private val exportPreferences = context.getSharedPreferences("download_exports", Context.MODE_PRIVATE)
    private val exportFailures = MutableStateFlow(exportPreferences.all.keys.toSet())
    private val activeExports = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()


    private val dataSourceFactory =
        ResolvingDataSource.Factory(
            CacheDataSource
                .Factory()
                .setCache(playerCache)
                .setCacheWriteDataSinkFactory(null) // PREVENTS DEADLOCKS! Don't write to playerCache here; just read from it!
                .setUpstreamDataSourceFactory(
                    OkHttpDataSource.Factory(
                        OkHttpClient.Builder()
                            .dns(object : Dns {
                                override fun lookup(hostname: String): List<InetAddress> {
                                    val addresses = Dns.SYSTEM.lookup(hostname)
                                    return when (this@DownloadUtil.ipVersion) {
                                        IpVersion.IPV4 -> addresses.filter { it is Inet4Address }.ifEmpty { addresses }
                                        IpVersion.IPV6 -> addresses.filter { it is Inet6Address }.ifEmpty { addresses }
                                        IpVersion.AUTO -> addresses
                                    }
                                }
                            })
                            .proxy(YouTube.proxy)
                            .proxyAuthenticator { _, response ->
                                YouTube.proxyAuth?.let { auth ->
                                    response.request.newBuilder()
                                        .header("Proxy-Authorization", auth)
                                        .build()
                                } ?: response.request
                            }
                            .build(),
                    ),
                ),
        ) { dataSpec ->
            val mediaId = dataSpec.key ?: error("No media id")
            Timber.tag("DownloadDiagnostics").d("dataSourceFactory triggered for mediaId=$mediaId, position=${dataSpec.position}, length=${dataSpec.length}")

            if (playerCache.completeAudioCachedAt(mediaId) != null) {
                Timber.tag("DownloadDiagnostics").d("Complete stream available in playerCache: $mediaId")
                return@Factory dataSpec
            }

            songUrlCache[mediaId]?.let { cachedStream ->
                Timber.tag("DownloadDiagnostics").d("Using cached stream URL from valid songUrlCache for $mediaId")
                return@Factory dataSpec
                    .withUri(cachedStream.url.toUri())
                    .withRequestHeaders(dataSpec.httpRequestHeaders + cachedStream.requestHeaders)
            }
            Timber.tag("DownloadDiagnostics").w("No valid cached URL found for $mediaId. Triggering heavy network playback resolution!")
            val cacheGeneration = songUrlCache.generation(mediaId)

            val playbackData = runBlocking(Dispatchers.IO) {
                YTPlayerUtils.playerResponseForPlayback(
                    mediaId,
                    audioQuality = audioQuality,
                    connectivityManager = connectivityManager,
                    context = context,
                )
            }.getOrThrow()
            val format = playbackData.format

            val actualContentLength = format.contentLength ?: run {
                var length: Long? = null
                val client = OkHttpClient.Builder()
                    .proxy(YouTube.proxy)
                    .proxyAuthenticator { _, response ->
                        YouTube.proxyAuth?.let { auth ->
                            response.request.newBuilder()
                                .header("Proxy-Authorization", auth)
                                .build()
                        } ?: response.request
                    }
                    .build()
                val request = okhttp3.Request.Builder()
                    .head()
                    .url(playbackData.streamUrl)
                    .apply {
                        playbackData.streamHeaders.forEach { (name, value) ->
                            header(name, value)
                        }
                    }
                    .build()
                client.newCall(request).execute().use { response ->
                    length = response.header("Content-Length")?.toLongOrNull()
                }
                length ?: error("Failed to retrieve content length")
            }

            database.query {
                upsert(
                    FormatEntity(
                        id = mediaId,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        codecs = format.mimeType.split("codecs=").getOrNull(1)?.removeSurrounding("\"") ?: "mp4a.40.2",
                        bitrate = format.bitrate,
                        sampleRate = format.audioSampleRate,
                        contentLength = actualContentLength,
                        loudnessDb = playbackData.audioConfig?.loudnessDb,
                        perceptualLoudnessDb = playbackData.audioConfig?.perceptualLoudnessDb,
                        playbackUrl = playbackData.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                    ),
                )

                val now = LocalDateTime.now()
                // Metadata registration only — dateDownload is intentionally NOT set here.
                // It belongs solely to onDownloadChanged()'s STATE_COMPLETED branch below,
                // which only fires once the download has actually finished.
                val existing = getSongByIdBlocking(mediaId)?.song

                val updatedSong = existing ?: SongEntity(
                    id = mediaId,
                    title = playbackData.videoDetails?.title ?: "Unknown",
                    duration = playbackData.videoDetails?.lengthSeconds?.toIntOrNull() ?: 0,
                    thumbnailUrl = playbackData.videoDetails?.thumbnail?.thumbnails?.lastOrNull()?.url?.resize(1200, 1200),
                    dateDownload = null,
                    isDownloaded = false
                )

                upsert(updatedSong)

                // Pre-cache the high-res thumbnail immediately when download starts
                updatedSong.thumbnailUrl?.let { url ->
                    val request = ImageRequest.Builder(context)
                        .data(url.resize(1200, 1200))
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    SingletonImageLoader.get(context).enqueue(request)
                }
            }

            val streamUrl = "${playbackData.streamUrl}&range=0-${actualContentLength}"

            songUrlCache.put(
                mediaId = mediaId,
                url = streamUrl,
                requestHeaders = playbackData.streamHeaders,
                clientName = playbackData.streamClient,
                expiresInSeconds = playbackData.streamExpiresInSeconds,
                expectedGeneration = cacheGeneration,
            )
            dataSpec
                .withUri(streamUrl.toUri())
                .withRequestHeaders(dataSpec.httpRequestHeaders + playbackData.streamHeaders)
        }

    val downloadNotificationHelper =
        DownloadNotificationHelper(context, ExoDownloadService.CHANNEL_ID)

    @OptIn(DelicateCoroutinesApi::class)
    val downloadManager: DownloadManager =
        DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            dataSourceFactory,
            Executor(Runnable::run)
        ).apply {
            maxParallelDownloads = 3
            addListener(
                object : DownloadManager.Listener {
                    override fun onDownloadsPausedChanged(downloadManager: DownloadManager, downloadsPaused: Boolean) {
                        this@DownloadUtil.downloadsPaused.value = downloadsPaused
                    }

                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?,
                    ) {
                        Timber.tag("DownloadDiagnostics").d(
                            "onDownloadChanged [%s]: state = %s, progress = %.2f%%, exception = %s",
                            download.request.id,
                            downloadStateToString(download.state),
                            download.percentDownloaded,
                            finalException?.message ?: "None"
                        )
                        
                        if (download.state == Download.STATE_FAILED && finalException.isExpiredStreamError()) {
                            songUrlCache.invalidate(download.request.id)
                        }

                        val downloadId = download.request.id
                        if (download.state == Download.STATE_COMPLETED) {
                            completionChecks.update { it + (downloadId to CompletionCheck(download, SongDownloadStage.Saving)) }
                        }
                        downloads.update { it + (downloadId to download) }
                        if (download.state != Download.STATE_COMPLETED) {
                            completionChecks.update { it - downloadId }
                        }

                        scope.launch {
                            when (download.state) {
                                Download.STATE_COMPLETED -> {
                                    verifyCompletion(download) {
                                        // Keep an interrupted export recoverable across process death.
                                        stateLock.withLock {
                                            if (downloads.value[downloadId] === download) {
                                                if (!exportPreferences.edit().putBoolean(downloadId, true).commit()) {
                                                    Timber.w("Could not persist pending export for %s", downloadId)
                                                }
                                            }
                                        }
                                        syncOfflineState(downloadId)
                                    }
                                    if (completionChecks.value[downloadId]?.stage == SongDownloadStage.Completed) {
                                        exportDownload(download)
                                    }

                                    // Pre-cache lyrics for completed download so offline playback has instant lyrics
                                    try {
                                        val song = database.song(download.request.id).firstOrNull()
                                        if (song != null) {
                                            val existingLyrics = database.lyrics(download.request.id).firstOrNull()
                                            if (existingLyrics == null || existingLyrics.lyrics.isBlank() || existingLyrics.lyrics == LyricsEntity.LYRICS_NOT_FOUND) {
                                                val lyricsHelper = EntryPointAccessors.fromApplication(
                                                    context.applicationContext,
                                                    LyricsHelperEntryPoint::class.java
                                                ).lyricsHelper()
                                                lyricsHelper.getLyrics(song.toMediaMetadata())
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Timber.tag("DownloadDiagnostics").w(e, "Could not pre-cache lyrics for download %s", download.request.id)
                                    }
                                }
                                Download.STATE_FAILED,
                                Download.STATE_STOPPED,
                                Download.STATE_REMOVING -> {
                                    syncOfflineState(download.request.id)
                                }
                                else -> {
                                }
                            }
                        }
                    }

                    override fun onDownloadRemoved(
                        downloadManager: DownloadManager,
                        download: Download,
                    ) {
                        val downloadId = download.request.id
                        Timber.tag("DownloadDiagnostics").d("onDownloadRemoved [%s]: App requested download removal", downloadId)
                        songUrlCache.invalidate(downloadId)

                        downloads.update { it - downloadId }
                        completionChecks.update { it - downloadId }
                        scope.launch {
                            syncOfflineState(downloadId)
                            stateLock.withLock {
                                if (downloads.value[downloadId] == null) setExportFailed(downloadId, false)
                            }
                        }
                    }
                }
            )
        }

    init {
        val result = mutableMapOf<String, Download>()
        downloadManager.downloadIndex.getDownloads().use { cursor ->
            while (cursor.moveToNext()) {
                result[cursor.download.request.id] = cursor.download
            }
        }
        downloadsPaused.value = downloadManager.downloadsPaused
        val completed = result.filterValues { it.state == Download.STATE_COMPLETED }
        completionChecks.value = completed.mapValues { CompletionCheck(it.value, SongDownloadStage.Saving) }
        downloads.value = result
        scope.launch {
            val knownDownloads = database.downloadedSongIds().toSet()
            for (songId in knownDownloads + completed.keys) {
                val download = completed[songId]
                if (download == null) {
                    syncOfflineState(songId)
                } else {
                    verifyCompletion(download) {
                        syncOfflineState(songId, recoverExportFailure = songId !in knownDownloads)
                    }
                }
            }
        }
    }

    private val offlineReadyIds by lazy {
        combine(
            database.observeDownloadedSongIds(),
            downloads.map { items ->
                items.values.filter { it.state == Download.STATE_COMPLETED }.associate { it.request.id to it.cacheKey() }
            }.distinctUntilChanged(),
            cacheChanges.completeEntries(downloadCache),
        ) { members, completed, files ->
            members.filterTo(mutableSetOf()) { isOfflineDownloadReady(it, completed, files) }
        }.distinctUntilChanged().flowOn(Dispatchers.IO)
            .catch {
                Timber.w(it, "Could not observe offline downloads")
                emit(mutableSetOf<String>())
            }
            .shareIn(scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)
    }

    fun completedSongs(sortType: SongSortType, descending: Boolean) =
        combine(database.downloadedSongs(sortType, descending), offlineReadyIds) { songs, ids ->
            songs.filter { it.id in ids }
        }.flowOn(Dispatchers.IO)

    private fun Download.cacheKey() = request.customCacheKey ?: request.uri.toString()

    fun getDownload(songId: String): Flow<Download?> = downloads.map { it[songId] }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun observeSongDownload(songId: String): Flow<SongDownloadStatus?> =
        combine(
            getDownload(songId).distinctUntilChanged(),
            downloadsPaused,
            completionChecks.map { it[songId] }.distinctUntilChanged(),
            offlineReadyIds.map { songId in it }.distinctUntilChanged(),
        ) { download, paused, completion, offlineReady ->
            if (download == null) return@combine flowOf<SongDownloadStatus?>(null)
            when (download.state) {
                Download.STATE_QUEUED -> {
                    val stage = if (paused) SongDownloadStage.Paused else SongDownloadStage.Queued
                    flowOf(SongDownloadStatus(download.startTimeMs, stage, download.progressFraction().takeIf { paused }))
                }
                Download.STATE_STOPPED -> flowOf(
                    SongDownloadStatus(download.startTimeMs, SongDownloadStage.Paused, download.progressFraction())
                )
                Download.STATE_RESTARTING -> flowOf(SongDownloadStatus(download.startTimeMs, SongDownloadStage.Queued))
                Download.STATE_DOWNLOADING -> {
                    if (paused) {
                        flowOf(SongDownloadStatus(download.startTimeMs, SongDownloadStage.Paused, download.progressFraction()))
                    } else {
                        progressTicks.onStart { emit(Unit) }.map {
                            val progress = download.progressFraction()
                            val stage = if (progress == 1f) SongDownloadStage.Saving else SongDownloadStage.Downloading
                            SongDownloadStatus(download.startTimeMs, stage, progress)
                        }
                    }
                }
                Download.STATE_COMPLETED -> {
                    val check = completion?.takeIf { it.download === download }?.stage
                    val stage = when {
                        offlineReady -> SongDownloadStage.Completed
                        check == null || check == SongDownloadStage.Saving -> SongDownloadStage.Saving
                        else -> SongDownloadStage.SaveFailed
                    }
                    flowOf(
                        SongDownloadStatus(
                            download.startTimeMs,
                            stage,
                            if (stage == SongDownloadStage.Saving) download.progressFraction() else null,
                        )
                    )
                }
                Download.STATE_FAILED -> flowOf(SongDownloadStatus(download.startTimeMs, SongDownloadStage.Failed))
                else -> flowOf(null)
            }
        }.flatMapLatest { it }.distinctUntilChanged()

    private fun Download.progressFraction(): Float? {
        val total = contentLength
        val bytes = bytesDownloaded
        if (total <= 0 || bytes < 0 || bytes > total) return null
        return if (bytes == total) 1f else (bytes.toDouble() / total).toFloat().coerceAtMost(0.99999994f)
    }

    private suspend fun verifyCompletion(download: Download, save: suspend () -> Unit) {
        val id = download.request.id
        val pending = completionChecks.value[id] ?: return
        if (pending.download !== download) return
        val stage = try {
            save()
            val persisted = downloadManager.downloadIndex.getDownload(id)
            val key = download.cacheKey()
            if (persisted != null && persisted.state == Download.STATE_COMPLETED &&
                persisted.updateTimeMs == download.updateTimeMs &&
                downloadCache.completeAudioCachedAt(key) != null &&
                database.getSongByIdBlocking(id)?.song?.isDownloaded == true
            ) SongDownloadStage.Completed else SongDownloadStage.SaveFailed
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Could not verify download completion for %s", id)
            SongDownloadStage.SaveFailed
        }
        completionChecks.update { checks ->
            if (checks[id] === pending) checks + (id to CompletionCheck(download, stage)) else checks
        }
    }

    fun hasExportFailure(songId: String): Flow<Boolean> =
        combine(exportFailures, offlineReadyIds) { failures, ready -> songId in failures && songId in ready }
            .distinctUntilChanged()

    fun retryExport(songId: String) {
        scope.launch {
            val download = downloads.value[songId]?.takeIf { it.state == Download.STATE_COMPLETED } ?: return@launch
            if (songId in exportFailures.value && songId in offlineReadyIds.first()) exportDownload(download)
        }
    }

    private suspend fun syncOfflineState(songId: String, recoverExportFailure: Boolean = false) = stateLock.withLock {
        val song = database.getSongByIdBlocking(songId)?.song ?: return@withLock
        val completed = downloads.value[songId]?.state == Download.STATE_COMPLETED
        if (song.isDownloaded != completed) {
            if (completed && recoverExportFailure && songId !in activeExports) setExportFailed(songId, true)
            database.updateDownloadedInfo(songId, completed, if (completed) LocalDateTime.now() else null)
        }
    }

    private suspend fun exportDownload(download: Download) {
        val songId = download.request.id
        if (downloads.value[songId] !== download) return
        if (!activeExports.add(songId)) return
        try {
            val written = DownloadDestinationWriter.writeCompletedDownload(context, downloadCache, database, songId)
            stateLock.withLock {
                if (downloads.value[songId] === download) setExportFailed(songId, !written)
            }
        } finally {
            activeExports.remove(songId)
        }
    }

    private fun setExportFailed(songId: String, failed: Boolean) {
        exportPreferences.edit().apply {
            if (failed) putBoolean(songId, true) else remove(songId)
        }.apply()
        exportFailures.update { if (failed) it + songId else it - songId }
    }

    fun release() {
        scope.cancel()
    }

    private fun Throwable?.isExpiredStreamError(): Boolean {
        var current = this
        while (current != null) {
            if (current is HttpDataSource.InvalidResponseCodeException &&
                (current.responseCode == 403 || current.responseCode == 410)
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }
}

private fun downloadStateToString(state: Int): String {
    return when (state) {
        Download.STATE_QUEUED -> "QUEUED"
        Download.STATE_STOPPED -> "STOPPED"
        Download.STATE_DOWNLOADING -> "DOWNLOADING"
        Download.STATE_COMPLETED -> "COMPLETED"
        Download.STATE_FAILED -> "FAILED"
        Download.STATE_REMOVING -> "REMOVING"
        Download.STATE_RESTARTING -> "RESTARTING"
        else -> "UNKNOWN ($state)"
    }
}
