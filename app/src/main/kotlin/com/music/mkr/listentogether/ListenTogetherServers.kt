/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.listentogether

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ListenTogetherServer(
    val name: String,
    val url: String,
    val location: String,
    val operator: String
)

object ListenTogetherServers {
    const val PRIMARY_SERVER_WS_URL = "wss://listen-together-mkr-music.onrender.com"
    const val PRIMARY_SERVER_HTTP_URL = "https://listen-together-mkr-music.onrender.com"

    private const val ServersJson = """
        [
          {
            "name": "MKR Music Hosted Server",
            "url": "wss://listen-together-mkr-music.onrender.com",
            "location": "Render (Cloud)",
            "operator": "mkr-infinity"
          }
        ]
    """

    private val json = Json { ignoreUnknownKeys = true }

    val servers: List<ListenTogetherServer> by lazy {
        json.decodeFromString(ServersJson)
    }

    val defaultServerUrl: String
        get() = PRIMARY_SERVER_WS_URL

    fun findByUrl(url: String): ListenTogetherServer? {
        val normalized = normalizeWebSocketUrl(url)
        return servers.firstOrNull { normalizeWebSocketUrl(it.url) == normalized }
    }

    /**
     * Converts any scheme (https://, http://, or schemeless) to secure wss:// or ws:// for OkHttp WebSocket
     */
    fun normalizeWebSocketUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("https://", ignoreCase = true) -> "wss://" + trimmed.substring(8)
            trimmed.startsWith("http://", ignoreCase = true) -> "ws://" + trimmed.substring(7)
            trimmed.startsWith("wss://", ignoreCase = true) || trimmed.startsWith("ws://", ignoreCase = true) -> trimmed
            else -> "wss://$trimmed"
        }
    }

    /**
     * Converts WebSocket scheme to corresponding HTTPS or HTTP scheme for REST/health requests
     */
    fun normalizeHttpUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("wss://", ignoreCase = true) -> "https://" + trimmed.substring(6)
            trimmed.startsWith("ws://", ignoreCase = true) -> "http://" + trimmed.substring(5)
            trimmed.startsWith("https://", ignoreCase = true) || trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
    }

    /**
     * Generates an invitation link for a room
     */
    fun getInviteLink(roomCode: String, serverUrl: String = defaultServerUrl): String {
        val httpBase = normalizeHttpUrl(serverUrl).trimEnd('/')
        return "$httpBase/listen?code=$roomCode"
    }
}
