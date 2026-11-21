/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import android.database.SQLException
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.Event
import com.music.mkr.extensions.metadata
import com.music.mkr.models.MediaMetadata
import java.time.LocalDateTime
import timber.log.Timber

@androidx.annotation.OptIn(UnstableApi::class)
internal class PlaybackHistoryRecorder(
    private val database: MusicDatabase,
    private val isEnabled: () -> Boolean,
    private val activePlayTime: () -> Long,
) : AnalyticsListener {
    private class Play(val windowUid: Any, val songId: String, val baselineMs: Long) {
        // These fields are only accessed on Room's transaction executor.
        var eventId = -1L
        var appliedTimeMs = 0L
    }

    private val plays = linkedMapOf<Long, Play>()
    private var ended: Pair<Long, Long>? = null
    private var lastAudible: Pair<Long, Any>? = null
    private var pendingGaplessStart: Pair<Long, Timeline.Window>? = null

    override fun onAudioPositionAdvancing(eventTime: EventTime, playoutStartSystemTimeMs: Long) {
        val period = eventTime.currentMediaPeriodId
            ?: eventTime.mediaPeriodId?.takeIf { eventTime.windowIndex == eventTime.currentWindowIndex }
            ?: return
        if (period.isAd || eventTime.currentTimeline.isEmpty) return
        val window = eventTime.currentTimeline.getWindow(eventTime.currentWindowIndex, Timeline.Window())
        start(period.windowSequenceNumber, window, LocalDateTime.now())
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        if (reason != Player.DISCONTINUITY_REASON_AUTO_TRANSITION || eventTime.currentTimeline.isEmpty) return
        val next = eventTime.currentMediaPeriodId ?: return
        if (next.isAd) return
        val window = eventTime.currentTimeline.getWindow(eventTime.currentWindowIndex, Timeline.Window())
        if (window.uid != newPosition.windowUid) return
        // The audio sink may keep running across a gapless boundary.
        val previous = lastAudible ?: return
        if (previous.second == oldPosition.windowUid && previous.first != next.windowSequenceNumber) {
            pendingGaplessStart = next.windowSequenceNumber to window
        }
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        val gapless = pendingGaplessStart
        pendingGaplessStart = null
        if (gapless != null && player.isPlaying && player.currentTracks.isTypeSelected(C.TRACK_TYPE_AUDIO) &&
            !player.currentTimeline.isEmpty &&
            player.currentTimeline.getWindow(player.currentMediaItemIndex, Timeline.Window()).uid == gapless.second.uid
        ) {
            start(gapless.first, gapless.second, LocalDateTime.now())
        }

        if (events.contains(AnalyticsListener.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
            val sequence = lastAudible?.first ?: return
            val play = plays[sequence] ?: return
            val total = activePlayTime()
            ended = sequence to total
            updatePlayTime(play, total)
        }
    }

    private fun start(sequence: Long, window: Timeline.Window, timestamp: LocalDateTime) {
        lastAudible = sequence to window.uid
        val replay = ended?.takeIf { it.first == sequence }
        if (sequence in plays && replay == null) return
        if (!isEnabled()) return

        val mediaItem = window.mediaItem
        val metadata = mediaItem.metadata ?: fallbackMetadata(mediaItem)
        val play = Play(window.uid, mediaItem.mediaId, replay?.second ?: 0L)
        plays[sequence] = play
        if (replay != null) ended = null
        database.transaction {
            try {
                if (getSongByIdBlocking(play.songId) == null) insert(metadata)
                play.eventId = insert(Event(songId = play.songId, timestamp = timestamp, playTime = 0L))
            } catch (error: SQLException) {
                Timber.w(error, "Could not record playback history")
            }
        }
    }

    fun finish(eventTime: EventTime, playTimeMs: Long) {
        if (eventTime.mediaPeriodId?.isAd == true || eventTime.timeline.isEmpty) return
        val sequence = eventTime.mediaPeriodId?.windowSequenceNumber ?: run {
            val uid = eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).uid
            plays.entries.firstOrNull { it.value.windowUid == uid }?.key ?: return
        }
        val play = plays.remove(sequence) ?: return
        if (ended?.first == sequence) ended = null
        updatePlayTime(play, playTimeMs)
    }

    private fun updatePlayTime(play: Play, totalMs: Long) {
        database.transaction {
            val elapsed = (totalMs - play.baselineMs).coerceAtLeast(0)
            val additional = elapsed - play.appliedTimeMs
            if (play.eventId > 0 && additional > 0) {
                updateEventPlayTime(play.eventId, elapsed)
                incrementTotalPlayTime(play.songId, additional)
                play.appliedTimeMs = elapsed
            }
        }
    }

    private fun fallbackMetadata(item: MediaItem) = MediaMetadata(
        id = item.mediaId,
        title = item.mediaMetadata.title?.toString() ?: item.mediaId,
        artists = item.mediaMetadata.artist?.toString()?.let { listOf(MediaMetadata.Artist(null, it)) }.orEmpty(),
        duration = -1,
        thumbnailUrl = item.mediaMetadata.artworkUri?.toString(),
    )
}
