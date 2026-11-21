package com.music.mkr.playback

import com.music.mkr.constants.CrossfadeCurve
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTransitionPolicyTest {
    @Test
    fun deniedFocusAndMuteSilenceEvenWhenDucking() {
        assertEquals(0f, playbackOutputVolume(0.7f, muted = false, focusGranted = false, ducking = false), 0f)
        assertEquals(0f, playbackOutputVolume(0.7f, muted = true, focusGranted = true, ducking = true), 0f)
        assertEquals(0.14f, playbackOutputVolume(0.7f, muted = false, focusGranted = true, ducking = true), 0.0001f)
    }

    @Test
    fun incomingBufferingKeepsOnlyTheOutgoingPathWhenPlaybackIsAllowed() {
        assertEquals(OutgoingPlayback.Carry, outgoingPlayback(true, true, false, false, true))
        assertEquals(OutgoingPlayback.Fade, outgoingPlayback(true, true, false, true, false))
        assertEquals(OutgoingPlayback.Pause, outgoingPlayback(true, false, false, false, true))
        assertEquals(OutgoingPlayback.Pause, outgoingPlayback(false, true, false, false, true))
        assertEquals(OutgoingPlayback.Pause, outgoingPlayback(true, true, true, false, true))
    }

    @Test
    fun crossfadeShortensForShortTracksAndKeepsGainHeadroom() {
        assertEquals(750L, crossfadeLengthMs(5_000, 3_000, 2f))
        assertEquals(0L, crossfadeLengthMs(0, 3_000, 1f))
        for (curve in CrossfadeCurve.entries) {
            assertEquals(1f to 0f, crossfadeGains(curve, 0f))
            assertEquals(0f to 1f, crossfadeGains(curve, 1f))
            val (out, incoming) = crossfadeGains(curve, 0.5f)
            assertTrue(out > 0f && incoming > 0f && out + incoming <= 1.00001f)
        }
    }

    @Test
    fun gaplessHistoryWaitsForAudioAndDiscardsAnAbandonedTrack() {
        assertEquals(GaplessHistoryStart.Wait, gaplessHistoryStart(true, false, false))
        assertEquals(GaplessHistoryStart.Record, gaplessHistoryStart(true, false, true))
        assertEquals(GaplessHistoryStart.Discard, gaplessHistoryStart(false, false, true))
        assertEquals(GaplessHistoryStart.Discard, gaplessHistoryStart(true, true, false))
    }

    @Test
    fun restoredPositionAndShuffleSurviveMembershipChanges() {
        val retained = listOf(1, 3, 4)
        assertEquals(1, restoredQueueIndex(2, retained))
        assertEquals(2, restoredQueueIndex(8, retained))
        assertArrayEquals(intArrayOf(2, 0, 1), remapSavedShuffleOrder(intArrayOf(4, 1, 2, 3), retained))
        assertNull(remapSavedShuffleOrder(intArrayOf(4, 4, 1), retained))
    }

    @Test
    fun offlineReadinessFallsWhenCompletionOrReadableCoverageDisappears() {
        val completed = mapOf("song" to "cache-key")
        assertTrue(isOfflineDownloadReady("song", completed, mapOf("cache-key" to 10L)))
        assertFalse(isOfflineDownloadReady("song", completed, emptyMap()))
        assertFalse(isOfflineDownloadReady("song", emptyMap(), mapOf("cache-key" to 10L)))
        assertFalse(isOfflineDownloadReady("song", completed, mapOf("song" to 10L)))
    }
}
