/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    if (!enabled || progress <= 0.001f) return

    val animatedPhase: Float = if (isPlaying && interfaceAnimations) {
        val infiniteTransition = rememberInfiniteTransition(label = "liquidWaveTransition")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "liquidPhase"
        )
        phase
    } else {
        0f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val clampedProgress = progress.coerceIn(0f, 1f)
        val fillWidth = width * clampedProgress

        // Amplitude scales down smoothly at extremes (0% or 100%) to avoid harsh clipping
        val edgeFactor = (clampedProgress * 15f).coerceAtMost(1f) * ((1f - clampedProgress) * 15f).coerceAtMost(1f)
        val amplitude = (5.dp.toPx() * edgeFactor).coerceAtLeast(0.5f)
        val frequency = 0.08f

        // 1. Back wave (secondary depth wave with slight phase offset)
        val backWavePath = Path().apply {
            moveTo(0f, 0f)
            val step = 6f
            var y = 0f
            while (y <= height) {
                val waveX = (fillWidth + amplitude * 0.7f * sin(y * frequency + animatedPhase + 1.2f))
                    .coerceIn(0f, width)
                lineTo(waveX, y)
                y += step
            }
            lineTo(0f, height)
            close()
        }

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
        val frontWavePath = Path().apply {
            moveTo(0f, 0f)
            val step = 6f
            var y = 0f
            while (y <= height) {
                val waveX = (fillWidth + amplitude * sin(y * frequency + animatedPhase))
                    .coerceIn(0f, width)
                lineTo(waveX, y)
                y += step
            }
            lineTo(0f, height)
            close()
        }

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
        val crestPath = Path().apply {
            var y = 0f
            val startX = (fillWidth + amplitude * sin(0f + animatedPhase)).coerceIn(0f, width)
            moveTo(startX, 0f)
            val step = 6f
            while (y <= height) {
                val waveX = (fillWidth + amplitude * sin(y * frequency + animatedPhase))
                    .coerceIn(0f, width)
                lineTo(waveX, y)
                y += step
            }
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
