/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.utils.rememberPreference

/**
 * Shape styling variant for the player transport controls.
 */
enum class TransportVariant {
    /** Classic and Minimal circular buttons */
    CIRCLE,
    /** Proportional pill buttons used by Modern player */
    PILL,
    /** Smooth rounded squircle used by MaterialExtended and Apple player */
    EXPRESSIVE,
    IMMERSIVE,
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
@OptIn(ExperimentalLayoutApi::class)
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
    secondaryContainerColor: Color = Color.Transparent,
    secondaryContentColor: Color = if (primaryContainerColor != Color.White && primaryContainerColor != Color.Transparent) primaryContainerColor else MaterialTheme.colorScheme.primary,
    playButtonSize: Dp = 72.dp,
    sideButtonSize: Dp = 52.dp,
    playButtonWidth: Dp? = null,
    playButtonHeight: Dp? = null,
    sideButtonWidth: Dp? = null,
    sideButtonHeight: Dp? = null,
    playIconSize: Dp = 36.dp,
    sideIconSize: Dp = 32.dp,
    haptic: HapticFeedback = LocalHapticFeedback.current,
    fitCompactWidth: Boolean = false,
    leadingControl: (@Composable RowScope.() -> Unit)? = null,
    trailingControl: (@Composable RowScope.() -> Unit)? = null,
) {
    val (interfaceAnimations) = rememberPreference(InterfaceAnimationsKey, defaultValue = true)
    val immersive = variant == TransportVariant.IMMERSIVE
    val effectivePlayWidth = playButtonWidth ?: if (variant == TransportVariant.PILL) playButtonSize * 1.55f else playButtonSize
    val effectivePlayHeight = playButtonHeight ?: playButtonSize
    val effectiveSideWidth = sideButtonWidth ?: if (variant == TransportVariant.PILL) sideButtonSize * 1.30f else sideButtonSize
    val effectiveSideHeight = sideButtonHeight ?: sideButtonSize
    val playShape = if (immersive) {
        val corner by animateDpAsState(
            targetValue = if (isPlaying) 20.dp else effectivePlayHeight / 2,
            animationSpec = if (interfaceAnimations) spring(dampingRatio = 0.9f, stiffness = 400f) else snap(),
            label = "immersiveTransportCorner",
        )
        RoundedCornerShape(corner)
    } else CircleShape
    val skipShape = if (immersive) RoundedCornerShape(effectiveSideHeight / 2) else CircleShape
    val pressSpec = if (interfaceAnimations) {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (immersive) 400f else Spring.StiffnessMediumLow,
        )
    } else snap<Float>()

    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    val playScale by animateFloatAsState(
        targetValue = if (isPlayPressed && interfaceAnimations) (if (immersive) 0.96f else 0.92f) else 1f,
        animationSpec = pressSpec,
        label = "playPressScale"
    )

    val prevInteractionSource = remember { MutableInteractionSource() }
    val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
    val prevScale by animateFloatAsState(
        targetValue = if (isPrevPressed && canSkipPrevious && interfaceAnimations) (if (immersive) 0.94f else 0.90f) else 1f,
        animationSpec = pressSpec,
        label = "prevPressScale"
    )

    val nextInteractionSource = remember { MutableInteractionSource() }
    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
    val nextScale by animateFloatAsState(
        targetValue = if (isNextPressed && canSkipNext && interfaceAnimations) (if (immersive) 0.94f else 0.90f) else 1f,
        animationSpec = pressSpec,
        label = "nextPressScale"
    )

    val sideFace = if (secondaryContainerColor == Color.Transparent) {
        secondaryContentColor.copy(alpha = 0.12f)
    } else {
        secondaryContainerColor
    }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val auxiliaryCount = listOfNotNull(leadingControl, trailingControl).size
        val inlineAuxiliary = !immersive &&
            maxWidth >= effectivePlayWidth + effectiveSideWidth * 2 + 48.dp * auxiliaryCount + 24.dp
        val gap = when {
            immersive -> 8.dp
            inlineAuxiliary -> 12.dp
            else -> 4.dp
        }
        val fitTransport = immersive || fitCompactWidth
        val wrapTransport = fitTransport && maxWidth < 48.dp * 3 + gap * 2
        val widthBudget = (maxWidth - gap * 2 - if (inlineAuxiliary) 48.dp * auxiliaryCount else 0.dp)
            .coerceAtLeast(144.dp)
        val defaultSideWidth = minOf(effectiveSideWidth, widthBudget * 0.30f).coerceAtLeast(48.dp)
        val playWidthLimit = if (fitTransport && !wrapTransport) {
            minOf(widthBudget * 0.40f, widthBudget - defaultSideWidth * 2)
        } else {
            widthBudget * 0.40f
        }
        val useImmersiveRow = immersive && !wrapTransport
        val immersiveWidth = widthBudget.coerceAtMost(328.dp)
        val playWidth = if (useImmersiveRow) {
            val animateWidth = interfaceAnimations && android.animation.ValueAnimator.areAnimatorsEnabled()
            val playShare = if (isPlayPressed && animateWidth) 0.66f else 0.58f
            val targetWidth = (immersiveWidth * playShare)
                .coerceIn(48.dp, immersiveWidth - 96.dp)
            animateDpAsState(
                targetValue = targetWidth,
                animationSpec = if (animateWidth) spring(dampingRatio = 0.8f, stiffness = 400f) else snap(),
                label = "immersiveTransportWidth",
            ).value.coerceIn(48.dp, immersiveWidth - 96.dp)
        } else {
            minOf(effectivePlayWidth, playWidthLimit).coerceAtLeast(48.dp)
        }
        val sideWidth = if (useImmersiveRow) (immersiveWidth - playWidth) / 2 else defaultSideWidth
        val actualPlayIconSize = if (immersive) minOf(playIconSize, playWidth - 12.dp) else playIconSize
        val actualSideIconSize = if (immersive) minOf(sideIconSize, sideWidth - 12.dp) else sideIconSize
        val playLabel = stringResource(if (isPlaying) R.string.pause else R.string.play)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val transport: @Composable RowScope.() -> Unit = {
                if (inlineAuxiliary) leadingControl?.invoke(this)

                // Skip Previous
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipPrevious()
                    },
                    enabled = canSkipPrevious,
                    shape = skipShape,
                    color = Color.Transparent,
                    interactionSource = prevInteractionSource,
                    modifier = Modifier
                        .scale(if (immersive) 1f else prevScale)
                        .size(width = sideWidth, height = effectiveSideHeight.coerceAtLeast(48.dp))
                ) {
                    val face = sideFace.copy(alpha = if (canSkipPrevious) sideFace.alpha else sideFace.alpha * 0.4f)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().then(
                            if (immersive) Modifier.scale(prevScale).background(face, skipShape) else Modifier
                        )
                    ) {
                        if (!immersive) PlaybackCookieFace(face)
                        Icon(
                            painter = painterResource(R.drawable.skip_previous),
                            contentDescription = stringResource(R.string.previous_track),
                            tint = secondaryContentColor.copy(alpha = if (canSkipPrevious) 1f else 0.38f),
                            modifier = Modifier.size(actualSideIconSize)
                        )
                    }
                }

                if (!wrapTransport) Spacer(Modifier.width(gap))

                // Play / Pause / Replay Central Button
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayPause()
                    },
                    shape = playShape,
                    color = Color.Transparent,
                    interactionSource = playInteractionSource,
                    modifier = Modifier
                        .scale(if (immersive) 1f else playScale)
                        .size(width = playWidth, height = effectivePlayHeight.coerceAtLeast(48.dp))
                        .then(if (immersive && isLoading) Modifier.semantics { contentDescription = playLabel } else Modifier)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().then(
                            if (immersive) Modifier.scale(playScale).background(primaryContainerColor, playShape) else Modifier
                        )
                    ) {
                        if (!immersive) PlaybackCookieFace(primaryContainerColor)
                        AnimatedContent(
                            targetState = Triple(isLoading, playbackState == Player.STATE_ENDED, isPlaying),
                            transitionSpec = {
                                if (interfaceAnimations) {
                                    val enterMs = if (immersive) 150 else 180
                                    val exitMs = if (immersive) 110 else 140
                                    val iconScale = if (immersive) 0.94f else 0.85f
                                    (fadeIn(animationSpec = tween(enterMs, easing = LinearEasing)) + scaleIn(initialScale = iconScale, animationSpec = tween(enterMs)))
                                        .togetherWith(fadeOut(animationSpec = tween(exitMs, easing = LinearEasing)) + scaleOut(targetScale = iconScale, animationSpec = tween(exitMs)))
                                } else {
                                    fadeIn(animationSpec = snap()).togetherWith(fadeOut(animationSpec = snap()))
                                }
                            },
                            label = "playPauseCrossfade"
                        ) { (loading, ended, playing) ->
                            if (loading) {
                                CircularProgressIndicator(
                                    color = primaryContentColor,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(actualPlayIconSize * 0.85f)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(
                                        when {
                                            ended -> R.drawable.replay
                                            playing -> R.drawable.pause
                                            else -> R.drawable.play
                                        }
                                    ),
                                    contentDescription = stringResource(if (playing) R.string.pause else R.string.play),
                                    tint = primaryContentColor,
                                    modifier = Modifier.size(actualPlayIconSize)
                                )
                            }
                        }
                    }
                }

                if (!wrapTransport) Spacer(Modifier.width(gap))

                // Skip Next
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipNext()
                    },
                    enabled = canSkipNext,
                    shape = skipShape,
                    color = Color.Transparent,
                    interactionSource = nextInteractionSource,
                    modifier = Modifier
                        .scale(if (immersive) 1f else nextScale)
                        .size(width = sideWidth, height = effectiveSideHeight.coerceAtLeast(48.dp))
                ) {
                    val face = sideFace.copy(alpha = if (canSkipNext) sideFace.alpha else sideFace.alpha * 0.4f)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().then(
                            if (immersive) Modifier.scale(nextScale).background(face, skipShape) else Modifier
                        )
                    ) {
                        if (!immersive) PlaybackCookieFace(face)
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = stringResource(R.string.next_track),
                            tint = secondaryContentColor.copy(alpha = if (canSkipNext) 1f else 0.38f),
                            modifier = Modifier.size(actualSideIconSize)
                        )
                    }
                }

                if (inlineAuxiliary) trailingControl?.invoke(this)
            }
            if (wrapTransport) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) { transport() }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = transport,
                )
            }
            if (!inlineAuxiliary && auxiliaryCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingControl?.invoke(this)
                    trailingControl?.invoke(this)
                }
            }
        }
    }
}
