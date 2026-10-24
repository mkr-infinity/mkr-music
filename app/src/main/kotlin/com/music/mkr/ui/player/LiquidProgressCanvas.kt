/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Renders an organic, continuously flowing liquid water fill inside the mini-player container.
 *
 * Characteristics:
 * - Natural horizontal water level that rises and falls with song progress
 * - Continuous sinusoidal undulating wave surface while playing
 * - Dual layered waves with phase offset for visual depth
 * - Highlight crest stroke along the leading water surface
 * - Calm, stable flat level when paused or when interface animations are disabled
 * - Translucent amber/primary tint maintaining complete legibility of text and controls
 * - Zero allocations in the draw loop via remembered Path objects
 * - Safe handling of NaN, zero duration, or out-of-bound playback values
 */
@Composable
fun LiquidProgressCanvas(
    progress: () -> Float,
    isPlaying: Boolean,
    enabled: Boolean,
    interfaceAnimations: Boolean,
    primaryColor: Color = Color(0xFFF59E0B),
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val phaseAnimatable = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, interfaceAnimations, enabled) {
        if (isPlaying && interfaceAnimations && enabled) {
            while (true) {
                phaseAnimatable.animateTo(
                    targetValue = (2f * Math.PI).toFloat(),
                    animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
                )
                phaseAnimatable.snapTo(0f)
            }
        } else {
            phaseAnimatable.snapTo(0f)
        }
    }

    val backWavePath = remember { Path() }
    val frontWavePath = remember { Path() }
    val crestPath = remember { Path() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val rawProgress = progress()
        if (rawProgress.isNaN() || rawProgress.isInfinite() || rawProgress <= 0.001f) return@Canvas

        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val clampedProgress = rawProgress.coerceIn(0f, 1f)
        val baseWaterX = width * clampedProgress

        val animatedPhase = if (interfaceAnimations && isPlaying) phaseAnimatable.value else 0f

        // Amplitude dampens near extremes (0% or 100%) to smoothly meet container edges
        val edgeFactor = (clampedProgress * 15f).coerceAtMost(1f) * ((1f - clampedProgress) * 15f).coerceAtMost(1f)
        val amplitude = if (interfaceAnimations && isPlaying) (2.5.dp.toPx() * edgeFactor) else 0f

        // Natural frequency scaled to canvas height for vertical wave crest along horizontal progress
        val waveLength1 = (height * 1.5f).coerceIn(40f, 200f)
        val freq1 = (2f * Math.PI.toFloat()) / waveLength1

        val waveLength2 = (height * 2.2f).coerceIn(60f, 300f)
        val freq2 = (2f * Math.PI.toFloat()) / waveLength2

        val step = 4f

        // 1. Secondary Back Wave (depth layer with subtle phase offset)
        backWavePath.reset()
        backWavePath.moveTo(0f, 0f)
        var y = 0f
        val startX2 = (baseWaterX + amplitude * 0.65f * sin(0f + animatedPhase + 1.4f)).coerceIn(0f, width)
        backWavePath.lineTo(startX2, 0f)
        while (y <= height) {
            val waveX = (baseWaterX + amplitude * 0.65f * sin(y * freq2 + animatedPhase + 1.4f)).coerceIn(0f, width)
            backWavePath.lineTo(waveX, y)
            y += step
        }
        val endX2 = (baseWaterX + amplitude * 0.65f * sin(height * freq2 + animatedPhase + 1.4f)).coerceIn(0f, width)
        backWavePath.lineTo(endX2, height)
        backWavePath.lineTo(0f, height)
        backWavePath.close()

        drawPath(
            path = backWavePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.07f),
                    primaryColor.copy(alpha = 0.15f)
                ),
                startX = 0f,
                endX = (baseWaterX + amplitude).coerceAtMost(width)
            )
        )

        // 2. Primary Front Wave (main liquid body flowing left-to-right)
        frontWavePath.reset()
        frontWavePath.moveTo(0f, 0f)
        y = 0f
        val startX1 = (baseWaterX + amplitude * sin(0f + animatedPhase)).coerceIn(0f, width)
        frontWavePath.lineTo(startX1, 0f)
        while (y <= height) {
            val waveX = (baseWaterX + amplitude * sin(y * freq1 + animatedPhase)).coerceIn(0f, width)
            frontWavePath.lineTo(waveX, y)
            y += step
        }
        val endX1 = (baseWaterX + amplitude * sin(height * freq1 + animatedPhase)).coerceIn(0f, width)
        frontWavePath.lineTo(endX1, height)
        frontWavePath.lineTo(0f, height)
        frontWavePath.close()

        drawPath(
            path = frontWavePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.13f),
                    primaryColor.copy(alpha = 0.24f)
                ),
                startX = 0f,
                endX = (baseWaterX + amplitude).coerceAtMost(width)
            )
        )

        // 3. Highlight Crest Line along leading wave front surface
        if (amplitude > 0.5f) {
            crestPath.reset()
            y = 0f
            crestPath.moveTo(startX1, 0f)
            while (y <= height) {
                val waveX = (baseWaterX + amplitude * sin(y * freq1 + animatedPhase)).coerceIn(0f, width)
                crestPath.lineTo(waveX, y)
                y += step
            }
            crestPath.lineTo(endX1, height)

            drawPath(
                path = crestPath,
                color = primaryColor.copy(alpha = 0.45f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Backward-compatible overload accepting a direct Float progress value.
 */
@Composable
fun LiquidProgressCanvas(
    progress: Float,
    isPlaying: Boolean,
    enabled: Boolean,
    interfaceAnimations: Boolean,
    primaryColor: Color = Color(0xFFF59E0B),
    modifier: Modifier = Modifier
) {
    LiquidProgressCanvas(
        progress = { progress },
        isPlaying = isPlaying,
        enabled = enabled,
        interfaceAnimations = interfaceAnimations,
        primaryColor = primaryColor,
        modifier = modifier
    )
}
