/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.mkrmusiccanvas

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import com.music.mkr.canvas.CanvasArtwork

@Serializable
data class MkrMusicCanvasManifest(
    val items: List<MkrMusicCanvasItem> = emptyList()
)

@Serializable
data class MkrMusicCanvasItem(
    val song: String,
    val artist: String,
    val url: String,
    val album: String = "",
)

object MkrMusicCanvasProvider {
    private const val BASE_URL = "https://vivimusicanvas.mkmdevilmi.workers.dev/canvas.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                connectTimeoutMillis = 12_000
                requestTimeoutMillis = 18_000
                socketTimeoutMillis = 18_000
            }
            install(ContentEncoding) {
                gzip()
                deflate()
            }
            install(HttpCache)
            expectSuccess = false
        }
    }

    private data class CacheEntry(
        val value: MkrMusicCanvasManifest?,
        val expiresAtMs: Long,
    )

    private var cache: CacheEntry? = null
    private val cacheLock = Any()
    private const val CACHE_TTL_MS = 60 * 60 * 1000L

    private suspend fun fetchManifest(): MkrMusicCanvasManifest? {
        val now = System.currentTimeMillis()
        synchronized(cacheLock) {
            val current = cache
            if (current != null && current.expiresAtMs > now) {
                return current.value
            }
        }

        return try {
            val manifest: MkrMusicCanvasManifest = client.get(BASE_URL).body()
            synchronized(cacheLock) {
                cache = CacheEntry(manifest, now + CACHE_TTL_MS)
            }
            manifest
        } catch (_: Throwable) {
            synchronized(cacheLock) {
                val current = cache
                if (current?.value != null) {
                    return current.value
                }
                cache = CacheEntry(null, now + 60_000L)
            }
            null
        }
    }

    suspend fun getBySongArtist(song: String, artist: String, album: String? = null): CanvasArtwork? {
        val manifest = fetchManifest() ?: return null
        val s = song.trim().lowercase()
        val a = artist.trim().lowercase()
        val al = album?.trim()?.lowercase()

        val exactMatch = manifest.items.firstOrNull {
            it.song.trim().lowercase() == s &&
                    it.artist.trim().lowercase() == a &&
                    (al == null || it.album.isBlank() || it.album.trim().lowercase() == al)
        }
        val match = exactMatch ?: manifest.items.firstOrNull {
            val itemSong = it.song.trim().lowercase()
            val itemArtist = it.artist.trim().lowercase()
            (itemSong == s || s.contains(itemSong) || itemSong.contains(s)) &&
                    (itemArtist == a || a.contains(itemArtist) || itemArtist.contains(a))
        }

        return match?.let {
            CanvasArtwork(
                name = it.song,
                artist = it.artist,
                albumName = it.album.takeIf { alb -> alb.isNotBlank() },
                animated = it.url,
                videoUrl = it.url
            )
        }
    }
}
