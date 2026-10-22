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
 * Renders an organic liquid water progress fill that gently flows from left to right
 * based on current playback progress.
 *
 * Characteristics:
 * - Natural undulating wave surface with sinusoidal flow
 * - Dual layered waves for depth
 * - Highlight crest stroke along the leading edge
 * - Disables continuous wave oscillation when paused or when interfaceAnimations is disabled
 * - Translucent amber/primary tint maintaining complete legibility of text and controls
 * - Reads progress in the draw phase via lambda () -> Float to avoid recomposition churn
 * - Reuses Path objects to eliminate allocations inside the 60fps draw loop
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
                    animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
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
        if (rawProgress.isNaN() || rawProgress <= 0.001f) return@Canvas

        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val clampedProgress = rawProgress.coerceIn(0f, 1f)
        val fillWidth = width * clampedProgress
        val animatedPhase = phaseAnimatable.value

        // Amplitude scales down smoothly at extremes (0% or 100%) to avoid harsh clipping
        val edgeFactor = (clampedProgress * 15f).coerceAtMost(1f) * ((1f - clampedProgress) * 15f).coerceAtMost(1f)
        val amplitude = (5.dp.toPx() * edgeFactor).coerceAtLeast(0.5f)
        val frequency = 0.08f

        // 1. Back wave (secondary depth wave with slight phase offset)
        backWavePath.reset()
        backWavePath.moveTo(0f, 0f)
        val step = 6f
        var y = 0f
        while (y <= height) {
            val waveX = (fillWidth + amplitude * 0.7f * sin(y * frequency + animatedPhase + 1.2f))
                .coerceIn(0f, width)
            backWavePath.lineTo(waveX, y)
            y += step
        }
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
                endX = (fillWidth + amplitude).coerceAtLeast(1f)
            )
        )

        // 2. Front wave (main liquid fill)
        frontWavePath.reset()
        frontWavePath.moveTo(0f, 0f)
        y = 0f
        while (y <= height) {
            val waveX = (fillWidth + amplitude * sin(y * frequency + animatedPhase))
                .coerceIn(0f, width)
            frontWavePath.lineTo(waveX, y)
            y += step
        }
        frontWavePath.lineTo(0f, height)
        frontWavePath.close()

        drawPath(
            path = frontWavePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.12f),
                    primaryColor.copy(alpha = 0.22f)
                ),
                startX = 0f,
                endX = (fillWidth + amplitude).coerceAtLeast(1f)
            )
        )

        // 3. Highlight crest stroke along the leading edge
        crestPath.reset()
        y = 0f
        val startX = (fillWidth + amplitude * sin(0f + animatedPhase)).coerceIn(0f, width)
        crestPath.moveTo(startX, 0f)
        while (y <= height) {
            val waveX = (fillWidth + amplitude * sin(y * frequency + animatedPhase))
                .coerceIn(0f, width)
            crestPath.lineTo(waveX, y)
            y += step
        }

        drawPath(
            path = crestPath,
            color = primaryColor.copy(alpha = 0.55f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
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
