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
     * Cache capacity and eviction are governed strictly by storage size (MB/bytes)
     * via [LeastRecentlyUsedCacheEvictor] in [com.music.mkr.di.AppModule].
     * The legacy count-based restriction has been removed to preserve all cached songs
     * until storage size thresholds are reached.
     */
    suspend fun trimStreamingCache(
        playerCache: SimpleCache,
        database: MusicDatabase,
        maxSongCount: Int = -1
    ): Int {
        // Count-based eviction removed: size-based eviction via LeastRecentlyUsedCacheEvictor handles quota.
        return 0
    }
}
