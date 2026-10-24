/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.music.mkr.R
import com.music.mkr.playback.PlayerConnection

/**
 * Shape styling variant for the player transport controls.
 */
enum class TransportVariant {
    /** Classic and Minimal circular buttons */
    CIRCLE,
    /** Proportional pill buttons used by Modern player */
    PILL,
    /** Smooth rounded squircle used by MaterialExtended and Apple player */
    EXPRESSIVE
}

/**
 * Consolidated high-performance playback transport row shared across player styles.
 *
 * Implements:
 * - Fluid, low-latency press-scale physics via [Spring.DampingRatioNoBouncy]
 * - Unified play / pause / replay / loading lifecycle handling
 * - Accessible skip previous / next state management with haptic feedback
 * - Parameterized design tokens for container and content palettes
 * - Optional leading and trailing action slots (shuffle, repeat, like, lyrics)
 */
@Composable
fun PlayerTransportRow(
    isPlaying: Boolean,
    isLoading: Boolean,
    playbackState: Int,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TransportVariant = TransportVariant.CIRCLE,
    primaryContainerColor: Color = MaterialTheme.colorScheme.primary,
    primaryContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    secondaryContainerColor: Color = primaryContainerColor.copy(alpha = 0.16f),
    secondaryContentColor: Color = MaterialTheme.colorScheme.onSurface,
    playButtonSize: Dp = 72.dp,
    sideButtonSize: Dp = 52.dp,
    playButtonWidth: Dp? = null,
    playButtonHeight: Dp? = null,
    sideButtonWidth: Dp? = null,
    sideButtonHeight: Dp? = null,
    playIconSize: Dp = 36.dp,
    sideIconSize: Dp = 28.dp,
    haptic: HapticFeedback = LocalHapticFeedback.current,
    leadingControl: (@Composable RowScope.() -> Unit)? = null,
    trailingControl: (@Composable RowScope.() -> Unit)? = null,
) {
    val effectivePlayWidth = playButtonWidth ?: if (variant == TransportVariant.PILL) playButtonSize * 1.55f else playButtonSize
    val effectivePlayHeight = playButtonHeight ?: playButtonSize
    val effectiveSideWidth = sideButtonWidth ?: if (variant == TransportVariant.PILL) sideButtonSize * 1.30f else sideButtonSize
    val effectiveSideHeight = sideButtonHeight ?: sideButtonSize
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    val playScale by animateFloatAsState(
        targetValue = if (isPlayPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "playPressScale"
    )

    val prevInteractionSource = remember { MutableInteractionSource() }
    val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
    val prevScale by animateFloatAsState(
        targetValue = if (isPrevPressed && canSkipPrevious) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "prevPressScale"
    )

    val nextInteractionSource = remember { MutableInteractionSource() }
    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
    val nextScale by animateFloatAsState(
        targetValue = if (isNextPressed && canSkipNext) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nextPressScale"
    )

    val playShape: Shape = when (variant) {
        TransportVariant.CIRCLE -> CircleShape
        TransportVariant.PILL -> RoundedCornerShape(32.dp)
        TransportVariant.EXPRESSIVE -> RoundedCornerShape(26.dp)
    }

    val sideShape: Shape = when (variant) {
        TransportVariant.CIRCLE -> CircleShape
        TransportVariant.PILL -> RoundedCornerShape(24.dp)
        TransportVariant.EXPRESSIVE -> CircleShape
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (leadingControl != null || trailingControl != null) Arrangement.SpaceEvenly else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingControl != null) {
            leadingControl()
        }

        // Skip Previous
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPrevious()
            },
            enabled = canSkipPrevious,
            shape = sideShape,
            color = secondaryContainerColor.copy(alpha = if (canSkipPrevious) 1f else 0.40f),
            modifier = Modifier
                .scale(prevScale)
                .size(width = effectiveSideWidth, height = effectiveSideHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = stringResource(R.string.widget_previous),
                    tint = secondaryContentColor.copy(alpha = if (canSkipPrevious) 1f else 0.38f),
                    modifier = Modifier.size(sideIconSize)
                )
            }
        }

        Spacer(Modifier.width(if (leadingControl != null) 12.dp else 20.dp))

        // Play / Pause / Replay Central Button
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayPause()
            },
            shape = playShape,
            color = primaryContainerColor,
            shadowElevation = if (variant == TransportVariant.PILL) 4.dp else 6.dp,
            modifier = Modifier
                .scale(playScale)
                .size(width = effectivePlayWidth, height = effectivePlayHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = primaryContentColor,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(playIconSize * 0.85f)
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            when {
                                playbackState == Player.STATE_ENDED -> R.drawable.replay
                                isPlaying -> R.drawable.pause
                                else -> R.drawable.play
                            }
                        ),
                        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                        tint = primaryContentColor,
                        modifier = Modifier.size(playIconSize)
                    )
                }
            }
        }

        Spacer(Modifier.width(if (trailingControl != null) 12.dp else 20.dp))

        // Skip Next
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            enabled = canSkipNext,
            shape = sideShape,
            color = secondaryContainerColor.copy(alpha = if (canSkipNext) 1f else 0.40f),
            modifier = Modifier
                .scale(nextScale)
                .size(width = effectiveSideWidth, height = effectiveSideHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = stringResource(R.string.widget_next),
                    tint = secondaryContentColor.copy(alpha = if (canSkipNext) 1f else 0.38f),
                    modifier = Modifier.size(sideIconSize)
                )
            }
        }

        if (trailingControl != null) {
            trailingControl()
        }
    }
}
