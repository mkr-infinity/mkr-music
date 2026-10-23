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
        val waterHeight = height * clampedProgress
        val baseWaterY = height - waterHeight

        val animatedPhase = if (interfaceAnimations && isPlaying) phaseAnimatable.value else 0f

        // Amplitude dampens near extremes (0% or 100%) to smoothly meet container edges
        val edgeFactor = (clampedProgress * 15f).coerceAtMost(1f) * ((1f - clampedProgress) * 15f).coerceAtMost(1f)
        val amplitude = if (interfaceAnimations && isPlaying) (3.2.dp.toPx() * edgeFactor) else 0f

        // Natural frequency scaled to canvas width
        val waveLength1 = (width * 0.75f).coerceIn(180f, 550f)
        val freq1 = (2f * Math.PI.toFloat()) / waveLength1

        val waveLength2 = (width * 1.05f).coerceIn(240f, 700f)
        val freq2 = (2f * Math.PI.toFloat()) / waveLength2

        val step = 8f

        // 1. Secondary Back Wave (depth layer with subtle phase offset)
        backWavePath.reset()
        backWavePath.moveTo(0f, height)
        var x = 0f
        val startY2 = (baseWaterY + amplitude * 0.65f * sin(0f + animatedPhase + 1.4f)).coerceIn(0f, height)
        backWavePath.lineTo(0f, startY2)
        while (x <= width) {
            val waveY = (baseWaterY + amplitude * 0.65f * sin(x * freq2 + animatedPhase + 1.4f)).coerceIn(0f, height)
            backWavePath.lineTo(x, waveY)
            x += step
        }
        val endY2 = (baseWaterY + amplitude * 0.65f * sin(width * freq2 + animatedPhase + 1.4f)).coerceIn(0f, height)
        backWavePath.lineTo(width, endY2)
        backWavePath.lineTo(width, height)
        backWavePath.close()

        drawPath(
            path = backWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.07f),
                    primaryColor.copy(alpha = 0.15f)
                ),
                startY = (baseWaterY - amplitude).coerceAtLeast(0f),
                endY = height
            )
        )

        // 2. Primary Front Wave (main liquid body)
        frontWavePath.reset()
        frontWavePath.moveTo(0f, height)
        x = 0f
        val startY1 = (baseWaterY + amplitude * sin(0f + animatedPhase)).coerceIn(0f, height)
        frontWavePath.lineTo(0f, startY1)
        while (x <= width) {
            val waveY = (baseWaterY + amplitude * sin(x * freq1 + animatedPhase)).coerceIn(0f, height)
            frontWavePath.lineTo(x, waveY)
            x += step
        }
        val endY1 = (baseWaterY + amplitude * sin(width * freq1 + animatedPhase)).coerceIn(0f, height)
        frontWavePath.lineTo(width, endY1)
        frontWavePath.lineTo(width, height)
        frontWavePath.close()

        drawPath(
            path = frontWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.13f),
                    primaryColor.copy(alpha = 0.24f)
                ),
                startY = (baseWaterY - amplitude).coerceAtLeast(0f),
                endY = height
            )
        )

        // 3. Highlight Crest Line along top surface
        if (amplitude > 0.5f) {
            crestPath.reset()
            x = 0f
            crestPath.moveTo(0f, startY1)
            while (x <= width) {
                val waveY = (baseWaterY + amplitude * sin(x * freq1 + animatedPhase)).coerceIn(0f, height)
                crestPath.lineTo(x, waveY)
                x += step
            }
            crestPath.lineTo(width, endY1)

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
