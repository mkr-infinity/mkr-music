/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.mkr.R
import com.music.mkr.constants.QueuePeekHeight
import com.music.mkr.db.entities.FormatEntity
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.component.PlayerTransportRow
import com.music.mkr.ui.component.TransportVariant
import com.music.mkr.ui.player.AudioQualityBadge
import com.music.mkr.ui.player.ClickableArtists
import com.music.mkr.ui.player.rememberPlayerTitleActions
import com.music.mkr.utils.makeTimeString
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Immersive Player style:
 * Artwork-led layout with Apple Music aesthetic backdrop and spacious controls.
 */
@Composable
fun ImmersivePlayer(
    mediaMetadata: MediaMetadata,
    itemArtwork: String?,
    animateArtwork: Boolean,
    foreground: Color,
    artworkIsBackdrop: Boolean,
    primaryButtonColor: Color,
    playbackState: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    sliderPosition: Long?,
    position: Long,
    duration: Long,
    isLiked: Boolean,
    currentFormat: FormatEntity?,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val titleActions = rememberPlayerTitleActions(mediaMetadata, navController, state)

    val secondaryForeground = foreground.copy(alpha = 0.82f)
    val closeLabel = stringResource(R.string.close)
    val sheetScrollConnection = remember(state) { state.preUpPostDownNestedScrollConnection }

    // System volume tracking & interactive control
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxSystemVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat().coerceAtLeast(1f) }
    val systemVolume by produceState(initialValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        awaitDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }

    val menuButton: @Composable (Modifier) -> Unit = { buttonModifier ->
        IconButton(
            onClick = onShowMenu,
            modifier = buttonModifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = foreground.copy(alpha = 0.12f),
                contentColor = foreground,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = stringResource(R.string.more_label),
                modifier = Modifier.size(22.dp),
            )
        }
    }

    AdaptivePlayerLayout(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(sheetScrollConnection)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = QueuePeekHeight + 8.dp),
        artworkFraction = 1f,
        immersive = true,
        artwork = { size ->
            if (artworkIsBackdrop) {
                Spacer(Modifier.size(size))
            } else {
                FullPlayerArtwork(
                    metadata = mediaMetadata,
                    itemArtwork = itemArtwork,
                    animate = animateArtwork,
                    backgroundColor = foreground.copy(alpha = 0.05f),
                    modifier = Modifier.size(size),
                )
            }
        },
        header = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                // Visible Apple Music style grabber handle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(if (maxWidth < 144.dp) Alignment.CenterStart else Alignment.Center)
                        .size(width = 64.dp, height = 24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { state.collapseSoft() }
                        )
                        .semantics { contentDescription = closeLabel },
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(foreground.copy(alpha = 0.45f))
                    )
                }

                if (maxWidth < 232.dp) menuButton(Modifier.align(Alignment.CenterEnd))
            }
        },
    ) {
        Column(Modifier.fillMaxWidth()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val showMenuNextToLike = maxWidth >= 232.dp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaMetadata.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = foreground,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { titleActions.onTitleClick() }
                                .then(if (animateArtwork) Modifier.basicMarquee() else Modifier),
                        )
                        Spacer(Modifier.height(4.dp))
                        ClickableArtists(
                            artists = mediaMetadata.artists,
                            onArtistClick = titleActions.onArtistClick,
                            style = MaterialTheme.typography.bodyLarge,
                            color = secondaryForeground,
                            modifier = if (animateArtwork) Modifier.basicMarquee() else Modifier
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Original Heart Like Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.toggleLike()
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = foreground.copy(alpha = 0.12f)),
                    ) {
                        Icon(
                            painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                            contentDescription = stringResource(if (isLiked) R.string.action_remove_like else R.string.action_like),
                            tint = if (isLiked) primaryButtonColor else foreground,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    if (showMenuNextToLike) {
                        Spacer(Modifier.width(4.dp))
                        menuButton(Modifier)
                    }
                }
            }

            AudioQualityBadge(
                videoId = mediaMetadata.id,
                explicitFormat = currentFormat,
                contentColor = foreground,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Progress Slider
            PlayerSliderTrack(
                sliderColors = SliderDefaults.colors(
                    thumbColor = foreground,
                    activeTrackColor = foreground,
                    inactiveTrackColor = foreground.copy(alpha = 0.36f),
                ),
                isPlaying = isPlaying && animateArtwork,
                value = (sliderPosition ?: position).toFloat(),
                onValueChange = { onSliderValueChange(it.toLong()) },
                onValueChangeFinished = onSliderValueChangeFinished,
                valueRange = 0f..duration.coerceAtLeast(0L).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryForeground
                )
                Text(
                    text = makeTimeString(duration.coerceAtLeast(0L)),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryForeground
                )
            }

            Spacer(Modifier.height(14.dp))

            // Immersive Playback Controls (Solid filled play/pause button + double triangle skips)
            PlayerTransportRow(
                isPlaying = isPlaying,
                isLoading = isLoading,
                playbackState = playbackState,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                onPlayPause = { playerConnection.togglePlayPause(restartIfEnded = true) },
                onSkipPrevious = { playerConnection.seekToPrevious() },
                onSkipNext = { playerConnection.seekToNext() },
                variant = TransportVariant.IMMERSIVE,
                primaryContainerColor = Color.Transparent,
                primaryContentColor = foreground,
                secondaryContainerColor = Color.Transparent,
                secondaryContentColor = foreground,
                playButtonWidth = 72.dp,
                playButtonHeight = 64.dp,
                sideButtonWidth = 64.dp,
                sideButtonHeight = 60.dp,
                playIconSize = 44.dp,
                sideIconSize = 40.dp,
                haptic = haptic,
            )

            Spacer(Modifier.height(14.dp))

            // Interactive Volume Slider Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                var isDraggingVolume by remember { mutableStateOf(false) }
                var lastAppliedStep by remember { mutableStateOf((systemVolume * maxSystemVolume).roundToInt()) }
                val animatedVolume = remember { Animatable(systemVolume) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(systemVolume) {
                    if (!isDraggingVolume) {
                        lastAppliedStep = (systemVolume * maxSystemVolume).roundToInt()
                        animatedVolume.animateTo(
                            targetValue = systemVolume,
                            animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)
                        )
                    }
                }

                fun applyVolumeFraction(fraction: Float) {
                    val clamped = fraction.coerceIn(0f, 1f)
                    coroutineScope.launch { animatedVolume.snapTo(clamped) }
                    val targetStep = (clamped * maxSystemVolume).roundToInt().coerceIn(0, maxSystemVolume.roundToInt())
                    if (targetStep != lastAppliedStep) {
                        lastAppliedStep = targetStep
                        runCatching {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetStep, 0)
                        }
                    }
                }

                val currentBarHeight by animateDpAsState(
                    targetValue = if (isDraggingVolume) 10.dp else 6.dp,
                    animationSpec = tween(durationMillis = 150),
                    label = "volumeBarHeight"
                )

                val iconColor = foreground.copy(alpha = 0.65f)

                Image(
                    painter = painterResource(R.drawable.apple_volume_down),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .pointerInput(maxSystemVolume) {
                            detectTapGestures { offset ->
                                if (size.width <= 0) return@detectTapGestures
                                applyVolumeFraction(offset.x / size.width.toFloat())
                            }
                        }
                        .pointerInput(maxSystemVolume) {
                            var dragFraction = animatedVolume.value
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    if (size.width <= 0) return@detectHorizontalDragGestures
                                    isDraggingVolume = true
                                    dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    applyVolumeFraction(dragFraction)
                                },
                                onHorizontalDrag = { _, delta ->
                                    if (size.width <= 0) return@detectHorizontalDragGestures
                                    dragFraction = (dragFraction + delta / size.width.toFloat()).coerceIn(0f, 1f)
                                    applyVolumeFraction(dragFraction)
                                },
                                onDragEnd = { isDraggingVolume = false },
                                onDragCancel = { isDraggingVolume = false }
                            )
                        }
                ) {
                    val trackHeightPx = currentBarHeight.toPx()
                    val topY = (size.height - trackHeightPx) / 2f
                    val cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
                    val fraction = animatedVolume.value.coerceIn(0f, 1f)

                    drawRoundRect(
                        color = foreground.copy(alpha = 0.22f),
                        topLeft = Offset(0f, topY),
                        size = Size(size.width, trackHeightPx),
                        cornerRadius = cornerRadius
                    )

                    if (fraction > 0f) {
                        drawRoundRect(
                            color = foreground,
                            topLeft = Offset(0f, topY),
                            size = Size((size.width * fraction).coerceIn(0f, size.width), trackHeightPx),
                            cornerRadius = cornerRadius
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(R.drawable.apple_volume_up),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
