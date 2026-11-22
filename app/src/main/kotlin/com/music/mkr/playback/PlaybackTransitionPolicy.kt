package com.music.mkr.playback

internal fun playbackOutputVolume(master: Float, muted: Boolean, focusGranted: Boolean, ducking: Boolean): Float {
    if (muted || !focusGranted) return 0f
    return master.coerceIn(0f, 1f) * if (ducking) 0.2f else 1f
}

internal enum class OutgoingPlayback { Pause, Carry, Fade }

internal fun outgoingPlayback(
    focusGranted: Boolean,
    playRequested: Boolean,
    suppressed: Boolean,
    incomingPlaying: Boolean,
    incomingBuffering: Boolean,
): OutgoingPlayback = when {
    !focusGranted || !playRequested || suppressed -> OutgoingPlayback.Pause
    incomingPlaying -> OutgoingPlayback.Fade
    incomingBuffering -> OutgoingPlayback.Carry
    else -> OutgoingPlayback.Pause
}

internal enum class GaplessHistoryStart { Wait, Discard, Record }

internal fun gaplessHistoryStart(matchesTrack: Boolean, terminalState: Boolean, playingAudio: Boolean): GaplessHistoryStart =
    when {
        !matchesTrack || terminalState -> GaplessHistoryStart.Discard
        playingAudio -> GaplessHistoryStart.Record
        else -> GaplessHistoryStart.Wait
    }

internal fun shouldStopPlaybackOnTaskRemoval(
    stopOnTaskClear: Boolean,
    hasQueue: Boolean,
    isLoadingQueue: Boolean,
    isCasting: Boolean,
): Boolean = stopOnTaskClear || !(hasQueue || isLoadingQueue || isCasting)
