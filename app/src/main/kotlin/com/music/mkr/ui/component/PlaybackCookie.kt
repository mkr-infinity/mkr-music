package com.music.mkr.ui.component

import android.animation.ValueAnimator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive

internal val LocalPlaybackCookieAngle = compositionLocalOf<State<Float>?> { null }

@Composable
internal fun rememberPlaybackCookieAngle(connection: PlayerConnection?, visible: Boolean): State<Float> {
    val localPlaying = connection?.isPlaying?.collectAsStateWithLifecycle()?.value == true
    val animations by rememberPreference(InterfaceAnimationsKey, true)
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val playing = localPlaying
    val angle = remember { mutableFloatStateOf(0f) }
    val active = visible && playing && animations && lifecycle.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        val context = currentCoroutineContext()
        val durationScale = context[MotionDurationScale]
        if (durationScale == null && !ValueAnimator.areAnimatorsEnabled()) return@LaunchedEffect
        while (context.isActive) {
            snapshotFlow { durationScale?.scaleFactor ?: 1f }.first { it > 0f }
            var previous = withFrameNanos { it }
            while ((durationScale?.scaleFactor ?: 1f) > 0f && context.isActive) {
                val frame = withFrameNanos { it }
                val scale = durationScale?.scaleFactor ?: 1f
                if (scale > 0f) {
                    val elapsed = (frame - previous).coerceAtMost(50_000_000L)
                    angle.floatValue = (angle.floatValue + elapsed * (360f / 60_000_000_000f) / scale) % 360f
                }
                previous = frame
            }
        }
    }
    return angle
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PlaybackCookieFace(
    color: Color,
    angle: State<Float>? = LocalPlaybackCookieAngle.current,
    faceFraction: Float = 0.88f,
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(minOf(maxWidth, maxHeight) * faceFraction)
                .graphicsLayer { rotationZ = angle?.value ?: 0f }
                .background(color, shape),
        )
    }
}

@Composable
internal fun PlaybackCookieButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    angle: State<Float>? = LocalPlaybackCookieAngle.current,
    faceFraction: Float = 0.88f,
    content: @Composable () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PlaybackCookieFace(if (enabled) color else color.copy(alpha = 0.45f), angle, faceFraction)
            content()
        }
    }
}
