/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.SimpleCache
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.Song
import com.music.mkr.di.DownloadCache
import com.music.mkr.di.PlayerCache
import com.music.mkr.extensions.filterExplicit
import com.music.mkr.extensions.filterVideoSongs
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CachePlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    @PlayerCache private val playerCache: SimpleCache,
    @DownloadCache private val downloadCache: SimpleCache
) : ViewModel() {

    private val _cachedSongs = MutableStateFlow<List<Song>>(emptyList())
    val cachedSongs: StateFlow<List<Song>> = _cachedSongs

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)

                val cachedKeys = playerCache.keys.toSet() + downloadCache.keys.toSet()
                if (cachedKeys.isEmpty()) {
                    _cachedSongs.value = emptyList()
                    delay(1000)
                    continue
                }

                // Query database for all candidate IDs in safe chunks
                val songs = cachedKeys.chunked(500).flatMap { chunk ->
                    database.getSongsByIds(chunk)
                }

                val songsById = songs.associateBy { it.id }
                val validCachedSongs = mutableListOf<Song>()
                val newlyDiscoveredSongs = mutableListOf<Song>()

                for (key in cachedKeys) {
                    val song = songsById[key] ?: continue

                    val formatLength = song.format?.contentLength?.takeIf { it > 0 }
                    val plLen = ContentMetadata.getContentLength(playerCache.getContentMetadata(key)).takeIf { it > 0 }
                    val dlLen = ContentMetadata.getContentLength(downloadCache.getContentMetadata(key)).takeIf { it > 0 }

                    val isCachedInPlayer = (plLen != null && playerCache.isCached(key, 0L, plLen)) ||
                        (formatLength != null && playerCache.isCached(key, 0L, formatLength))
                    val isCachedInDownload = (dlLen != null && downloadCache.isCached(key, 0L, dlLen)) ||
                        (formatLength != null && downloadCache.isCached(key, 0L, formatLength))

                    val isPlayable = song.song.isDownloaded || isCachedInPlayer || isCachedInDownload

                    if (isPlayable) {
                        validCachedSongs += song
                        if (song.song.dateDownload == null) {
                            newlyDiscoveredSongs += song
                        }
                    } else if (song.song.dateDownload != null && !song.song.isDownloaded) {
                        // Reconcile stale cache record: audio file is missing or corrupt
                        database.query { update(song.song.copy(dateDownload = null)) }
                    }
                }

                if (newlyDiscoveredSongs.isNotEmpty()) {
                    val now = LocalDateTime.now()
                    database.query {
                        newlyDiscoveredSongs.forEach { item ->
                            update(item.song.copy(dateDownload = now))
                        }
                    }
                }

                _cachedSongs.value = validCachedSongs
                    .sortedByDescending { it.song.dateDownload ?: LocalDateTime.MIN }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)

                delay(1000)
            }
        }
    }

    fun removeSongFromCache(songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                playerCache.removeResource(songId)
            } catch (_: Exception) {}
            val song = database.song(songId).first()
            if (song != null && !song.song.isDownloaded) {
                database.query { update(song.song.copy(dateDownload = null)) }
            }
        }
    }
}
