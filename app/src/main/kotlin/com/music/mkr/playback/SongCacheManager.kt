/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import androidx.media3.datasource.cache.SimpleCache
import com.music.mkr.db.MusicDatabase
import com.music.mkr.extensions.tryOrNull
import timber.log.Timber

object SongCacheManager {
    /**
     * Enforces the maximum streaming song limit on [playerCache].
     * ONLY evicts streaming songs from [playerCache].
     * Offline downloads in downloadCache and local device files are NEVER touched.
     */
    suspend fun trimStreamingCache(
        playerCache: SimpleCache,
        database: MusicDatabase,
        maxSongCount: Int
    ): Int {
        if (maxSongCount <= 0) return 0 // -1 or 0 represents unlimited / disabled

        val allCandidateIds = playerCache.keys.toList()
        if (allCandidateIds.isEmpty()) return 0

        val songs = database.getSongsByIds(allCandidateIds)
        // Strictly filter to streaming cached songs only
        val streamingCached = songs
            .filter { !it.song.isDownloaded && !it.song.isLocal && it.song.dateDownload != null }
            .sortedBy { it.song.dateDownload }

        val excess = streamingCached.size - maxSongCount
        if (excess <= 0) return 0

        val toEvict = streamingCached.take(excess)
        var evictedCount = 0
        for (song in toEvict) {
            tryOrNull {
                playerCache.removeResource(song.song.id)
                database.query {
                    update(song.song.copy(dateDownload = null))
                }
                evictedCount++
            }
        }
        Timber.d("Trimmed $evictedCount excess streaming songs from playerCache (limit: $maxSongCount)")
        return evictedCount
    }
}
