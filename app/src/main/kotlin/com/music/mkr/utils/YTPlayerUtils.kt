/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.net.ConnectivityManager
import android.util.Log
import androidx.media3.common.PlaybackException
import com.music.innertube.NewPipeExtractor
import com.music.innertube.YouTube
import com.music.innertube.models.YouTubeClient
import com.music.innertube.models.YouTubeClient.Companion.ANDROID_CREATOR
import com.music.mkr.utils.BotDetectionMitigator
import com.music.innertube.models.YouTubeClient.Companion.ANDROID_VR_1_43_32
import com.music.innertube.models.YouTubeClient.Companion.ANDROID_VR_1_61_48
import com.music.innertube.models.YouTubeClient.Companion.ANDROID_VR_NO_AUTH
import com.music.innertube.models.YouTubeClient.Companion.IOS
import com.music.innertube.models.YouTubeClient.Companion.IPADOS
import com.music.innertube.models.YouTubeClient.Companion.MOBILE
import com.music.innertube.models.YouTubeClient.Companion.TVHTML5
import com.music.innertube.models.YouTubeClient.Companion.TVHTML5_SIMPLY_EMBEDDED_PLAYER
import com.music.innertube.models.YouTubeClient.Companion.WEB
import com.music.innertube.models.YouTubeClient.Companion.WEB_CREATOR
import com.music.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.music.innertube.models.response.PlayerResponse
import com.music.mkr.constants.AudioQuality
import com.music.mkr.utils.cipher.CipherDeobfuscator
import com.music.mkr.utils.sabr.EjsNTransformSolver
import com.music.mkr.utils.PlaybackLogLevel
import com.music.mkr.utils.PlaybackLogManager
import com.music.innertube.models.IpVersion
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YouTubeClient.Companion.VISIONOS
import com.music.innertube.strategy.ContentAwareFallbackStrategy
import com.music.innertube.strategy.ContentHints
import okhttp3.Dns
import okhttp3.OkHttpClient
import timber.log.Timber
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object YTPlayerUtils {
    private const val logTag = "YTPlayerUtils"
    private const val TAG = "YTPlayerUtils"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val addresses = Dns.SYSTEM.lookup(hostname)
                return when (YouTube.ipVersion) {
                    IpVersion.IPV4 -> addresses.filter { it is Inet4Address }.ifEmpty { addresses }
                    IpVersion.IPV6 -> addresses.filter { it is Inet6Address }.ifEmpty { addresses }
                    IpVersion.AUTO -> addresses
                }
            }
        })
        .proxySelector(object : ProxySelector() {
            override fun select(uri: URI?): List<Proxy> = listOfNotNull(YouTube.proxy ?: Proxy.NO_PROXY)
            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
                Timber.tag(TAG).e(ioe, "Proxy connection failed for URI: $uri")
            }
        })
        .proxyAuthenticator { _, response ->
            YouTube.proxyAuth?.let { auth ->
                response.request.newBuilder()
                    .header("Proxy-Authorization", auth)
                    .build()
            } ?: response.request
        }
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()



    /**
     * Client used for fast, low-latency stream resolution.
     * ANDROID_VR clients don't require PoToken and start instantly.
     * Note: ANDROID_VR has loginSupported=false, so metadata like audioConfig and
     * playbackTracking must be supplemented from an authenticated client (WEB_REMIX)
     * when the user is logged in.
     */
    private val MAIN_CLIENT: YouTubeClient = ANDROID_VR_1_43_32

    /**
     * Client used to fetch metadata (audioConfig, playbackTracking) when the user is
     * logged in. This ensures remote YouTube history is correctly updated.
     */
    private val METADATA_CLIENT: YouTubeClient = WEB_REMIX
    private val fallbackStrategy = ContentAwareFallbackStrategy()

    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(
        IOS,
        VISIONOS,
        ANDROID_VR_1_43_32,
        WEB_REMIX,
        TVHTML5,
        TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        ANDROID_CREATOR,
        WEB_CREATOR
    )

    /**
     * Custom player response intended to use for playback.
     * Stream URLs come from [MAIN_CLIENT] or [STREAM_FALLBACK_CLIENTS] for fast loading.
     * Metadata (audioConfig, playbackTracking) come from [METADATA_CLIENT] (WEB_REMIX)
     * when the user is logged in, to ensure remote history recording works correctly.
     */
    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
        context: android.content.Context? = null,
        contentHints: ContentHints = ContentHints(),
    ): Result<InnerTubeXPlayer.PlaybackData> {
        val firstAttempt = resolvePlaybackData(videoId, playlistId, audioQuality, connectivityManager, contentHints)
        
        if (firstAttempt.isFailure) {
            Timber.tag(TAG).w("Playback failed for initial attempt. Rotating session and retrying...")
            PlaybackLogManager.log(PlaybackLogLevel.BOT, "Playback failed", "Triggering bot detection mitigation (rotating guest session)")
            BotDetectionMitigator.rotateGuestSession()
            val retryResult = resolvePlaybackData(videoId, playlistId, audioQuality, connectivityManager, contentHints)
            if (retryResult.isSuccess) {
                retryResult.onSuccess { BotDetectionMitigator.notifyPlaybackSuccess() }
                return retryResult
            }

            // If user is logged in, auth cookies or account restrictions might be blocking the stream. Fall back to guest session.
            if (YouTube.cookie != null) {
                Timber.tag(TAG).w("Stream extraction failed with auth cookies. Attempting guest session fallback...")
                val savedCookie = YouTube.cookie
                try {
                    YouTube.cookie = null
                    val guestResult = resolvePlaybackData(videoId, playlistId, audioQuality, connectivityManager, contentHints)
                    if (guestResult.isSuccess) {
                        guestResult.onSuccess { BotDetectionMitigator.notifyPlaybackSuccess() }
                        return guestResult
                    }
                } finally {
                    YouTube.cookie = savedCookie
                }
            }

            val err = retryResult.exceptionOrNull() ?: firstAttempt.exceptionOrNull()
            Timber.tag(TAG).e(err, "Stream resolution exhausted for $videoId after session rotation")
            PlaybackLogManager.log(
                PlaybackLogLevel.ERROR,
                "No playable stream",
                "All clients failed for $videoId: ${err?.message ?: "unknown"}"
            )
            return retryResult
        }
        
        firstAttempt.onSuccess { BotDetectionMitigator.notifyPlaybackSuccess() }
        return firstAttempt
    }

    private suspend fun resolvePlaybackData(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
        contentHints: ContentHints = ContentHints(),
    ): Result<InnerTubeXPlayer.PlaybackData> {
        Timber.tag(logTag).d("Fetching player response for videoId: ${videoId} via InnerTubeX")
        PlaybackLogManager.log(PlaybackLogLevel.INFO, "Resolving playback data", "Video: ${videoId}")
        println("[PLAYBACK_DEBUG] resolvePlaybackData (InnerTubeX) called: videoId=${videoId}")
        
        val mappedHints = com.metrolist.innertubex.extraction.ContentHints(
            isKidsContent = contentHints.isKidsContent,
            isExplicit = contentHints.isExplicit,
            isLive = contentHints.isLive,
            isUploaded = contentHints.isUploaded
        )
        return InnerTubeXPlayer.playerResponseForPlayback(
            videoId = videoId,
            playlistId = playlistId,
            audioQuality = audioQuality,
            connectivityManager = connectivityManager,
            contentHints = mappedHints,
            allowBoundedRange = true
        )
    }

    /**
     * Simple player response intended to use for metadata only.
     * Stream URLs of this response might not work so don't use them.
     */
    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> {
        Timber.tag(logTag).d("Fetching metadata-only player response for videoId: $videoId")
        return YouTube.player(videoId, playlistId, client = WEB_REMIX) // ANDROID_VR does not work with history
            .onSuccess { Timber.tag(logTag).d("Successfully fetched metadata") }
            .onFailure { Timber.tag(logTag).e(it, "Failed to fetch metadata") }
    }
}
