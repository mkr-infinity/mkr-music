/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

internal enum class SongDownloadStage {
    Queued, Downloading, Paused, Saving, Completed, Failed, SaveFailed,
}

internal data class SongDownloadStatus(
    val attempt: Long,
    val stage: SongDownloadStage,
    val progress: Float? = null,
)
