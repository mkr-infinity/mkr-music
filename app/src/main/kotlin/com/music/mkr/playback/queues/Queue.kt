/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.queues

import androidx.media3.common.MediaItem
import com.music.mkr.extensions.metadata
import com.music.mkr.models.MediaMetadata
import com.music.innertube.pages.RadioChip
import kotlinx.coroutines.flow.StateFlow

interface Queue {
    val preloadItem: MediaMetadata?
    val radioChips: StateFlow<List<RadioChip>>? get() = null


    suspend fun getInitialStatus(): Status

    fun hasNextPage(): Boolean

    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val title: String?,
        val items: List<MediaItem>,
        val mediaItemIndex: Int,
        val position: Long = 0L,
    ) {
        fun filterExplicit(enabled: Boolean = true) =
            if (enabled) withItems(items.filterExplicit()) else this

        fun filterVideoSongs(disableVideos: Boolean = false) =
            if (disableVideos) withItems(items.filterVideoSongs(true)) else this

        fun withItems(newItems: List<MediaItem>): Status {
            val selected = items.getOrNull(mediaItemIndex)
            val selectedIndex = newItems.indexOfFirst { it === selected }.takeIf { it >= 0 }
                ?: newItems.indexOfFirst { it.mediaId == selected?.mediaId }
            val nextIndex = when {
                selected == null -> 0
                selectedIndex >= 0 -> selectedIndex
                else -> {
                    val retainedIds = newItems.mapTo(hashSetOf()) { it.mediaId }
                    val next = items.asSequence().drop(mediaItemIndex + 1)
                        .firstOrNull { it.mediaId in retainedIds }
                    newItems.indexOfFirst { it === next }.takeIf { it >= 0 }
                        ?: newItems.lastIndex.coerceAtLeast(0)
                }
            }
            return copy(
                items = newItems,
                mediaItemIndex = nextIndex,
                position = if (selectedIndex != -1 || selected == null) position else 0L,
            )
        }
    }
}

fun List<MediaItem>.filterExplicit(enabled: Boolean = true) =
    if (enabled) {
        filterNot {
            it.metadata?.explicit == true
        }
    } else {
        this
    }

fun List<MediaItem>.filterVideoSongs(disableVideos: Boolean = false) =
    if (disableVideos) {
        filterNot { it.metadata?.isVideoSong == true }
    } else {
        this
    }
