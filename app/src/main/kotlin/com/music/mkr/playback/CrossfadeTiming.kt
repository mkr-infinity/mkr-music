/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import com.music.mkr.constants.CrossfadeCurve

internal const val CROSSFADE_PREPARE_LEAD_MS = 3_500L
internal const val CROSSFADE_MIN_OVERLAP_MS = 160L

internal fun crossfadeLengthMs(configuredMs: Long, durationMs: Long, speed: Float): Long {
    if (configuredMs <= 0 || durationMs <= 0 || !speed.isFinite() || speed <= 0f) return 0
    return minOf(configuredMs, (durationMs / speed.toDouble() / 2).toLong())
}

internal fun remainingPlaybackMs(durationMs: Long, positionMs: Long, speed: Float): Long =
    ((durationMs - positionMs).coerceAtLeast(0) / speed.toDouble()).toLong()

internal fun crossfadeGains(curve: CrossfadeCurve, progress: Float): Pair<Float, Float> {
    if (progress <= 0f) return 1f to 0f
    if (progress >= 1f) return 0f to 1f
    val out = curve.fadeOut(progress)
    val incoming = curve.fadeIn(progress)
    val peak = maxOf(1f, out + incoming)
    return (out / peak) to (incoming / peak)
}
