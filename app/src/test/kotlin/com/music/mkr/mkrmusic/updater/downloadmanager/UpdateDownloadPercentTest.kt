package com.music.mkr.mkrmusic.updater.downloadmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateDownloadPercentTest {
    @Test
    fun calculatesPercentFromReportedBytes() {
        assertEquals(0, updateDownloadPercent(0, 800))
        assertEquals(25, updateDownloadPercent(200, 800))
        assertEquals(50, updateDownloadPercent(4_000_000_000L, 8_000_000_000L))
    }

    @Test
    fun missingSizeIsIndeterminate() {
        assertNull(updateDownloadPercent(200, -1))
        assertNull(updateDownloadPercent(0, 0))
        assertNull(updateDownloadPercent(-1, 800))
    }

    @Test
    fun keepsCompletionForTheSuccessfulDownloadState() {
        assertEquals(99, updateDownloadPercent(800, 800))
        assertEquals(99, updateDownloadPercent(850, 800))
    }
}
