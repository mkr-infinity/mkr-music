/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search.suggestions

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit

object AppleMusicScraper {
    private const val TAG = "AppleMusicScraper"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun executeGet(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing GET request for $url", e)
            null
        }
    }

    private fun fetchWithCache(
        context: Context,
        url: String,
        cacheName: String,
        cacheMaxAgeMs: Long = 6 * 60 * 60 * 1000
    ): String? {
        val cacheFile = File(context.cacheDir, cacheName)
        if (cacheFile.exists()) {
            val lastModified = cacheFile.lastModified()
            if (System.currentTimeMillis() - lastModified < cacheMaxAgeMs) {
                return cacheFile.readText()
            }
        }
        
        val response = executeGet(url)
        return if (response != null) {
            try { cacheFile.writeText(response) } catch (e: Exception) {}
            response
        } else {
            if (cacheFile.exists()) {
                try { cacheFile.readText() } catch (e: Exception) { null }
            } else {
                null
            }
        }
    }

    fun fetchTopAlbums(context: Context, countryCode: String = "us"): List<SuggestionAlbum> {
        val albums = mutableListOf<SuggestionAlbum>()
        try {
            val url = "https://rss.applemarketingtools.com/api/v2/$countryCode/music/most-played/20/albums.json"
            val response = fetchWithCache(context, url, "apple_albums_$countryCode.json") ?: return albums
            
            val json = JSONObject(response)
            val results = json.getJSONObject("feed").getJSONArray("results")
            
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val rank = i + 1
                val title = item.getString("name")
                val artist = item.getString("artistName")
                val artwork = item.getString("artworkUrl100")
                    .replace(Regex("(\\d+)x(\\d+)"), "1400x1400")
                val appleUrl = item.getString("url")
                
                albums.add(SuggestionAlbum(rank, title, artist, artwork, appleUrl))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Apple Music Top Albums for $countryCode", e)
        }
        return albums
    }
}
