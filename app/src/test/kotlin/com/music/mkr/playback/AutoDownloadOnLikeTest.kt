package com.music.mkr.playback

import androidx.media3.exoplayer.offline.Download
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoDownloadOnLikeTest {
    @Test
    fun allowsNewRequestsAndFailedRetries() {
        assertTrue(canAutoDownloadLikedSong(null))
        assertTrue(canAutoDownloadLikedSong(Download.STATE_FAILED))
    }

    @Test
    fun keepsExistingDownloadsAndUserPauses() {
        val existingStates = listOf(
            Download.STATE_QUEUED,
            Download.STATE_DOWNLOADING,
            Download.STATE_COMPLETED,
            Download.STATE_STOPPED,
            Download.STATE_REMOVING,
            Download.STATE_RESTARTING,
        )
        existingStates.forEach { assertFalse(canAutoDownloadLikedSong(it)) }
    }
}
