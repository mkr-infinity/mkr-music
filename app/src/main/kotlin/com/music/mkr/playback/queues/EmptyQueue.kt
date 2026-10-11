/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.queues

import androidx.media3.common.MediaItem
import com.music.mkr.models.MediaMetadata

object EmptyQueue : Queue {
    override val preloadItem: MediaMetadata? = null

    override suspend fun getInitialStatus() = Queue.Status(null, emptyList(), -1)

    override fun hasNextPage() = false

    override suspend fun nextPage() = emptyList<MediaItem>()
}
