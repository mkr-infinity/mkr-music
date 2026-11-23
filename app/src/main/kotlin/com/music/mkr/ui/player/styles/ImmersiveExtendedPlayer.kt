/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.music.mkr.R
import com.music.mkr.constants.QueuePeekHeight
import com.music.mkr.db.entities.FormatEntity
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
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

/**
 * Immersive Extended Player style:
 * Framed artwork, album context, and an integrated volume row.
 */
@Composable
fun ImmersiveExtendedPlayer(
    mediaMetadata: MediaMetadata,
    itemArtwork: String?,
    animateArtwork: Boolean,
    foreground: Color,
    controlsBackground: Color,
    primaryButtonColor: Color,
    primaryIconColor: Color,
    sideButtonColor: Color,
    sideIconColor: Color,
    playbackState: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    sliderPosition: Long?,
    position: Long,
    duration: Long,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    isLiked: Boolean,
    playerVolume: Float,
    currentFormat: FormatEntity?,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onShowLyrics: () -> Unit,
    onShowQueue: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val titleActions = rememberPlayerTitleActions(mediaMetadata, navController, state)

    val secondaryForeground = foreground.copy(alpha = 0.82f)
    val subtitle = mediaMetadata.album?.title ?: stringResource(R.string.queue)
    val panelShape = MaterialTheme.shapes.large
    val controlPadding = if (controlsBackground == Color.Transparent) 0.dp else 12.dp

    AdaptivePlayerLayout(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = QueuePeekHeight + 8.dp),
        artworkFraction = 0.92f,
        immersive = true,
        artwork = { size ->
            ImmersiveArtwork(
                trackId = mediaMetadata.id,
                thumbnail = mediaMetadata.thumbnailUrl,
                itemArtwork = itemArtwork,
                animate = animateArtwork,
                backgroundColor = foreground.copy(alpha = 0.10f),
                modifier = Modifier.size(size).clip(MaterialTheme.shapes.small),
            )
        },
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(controlsBackground, panelShape)
                    .padding(horizontal = controlPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = foreground.copy(alpha = 0.10f),
                    onClick = { state.collapseSoft() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.expand_more),
                            contentDescription = stringResource(R.string.close),
                            tint = foreground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.playing_from),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryForeground
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = foreground.copy(alpha = 0.10f),
                    onClick = onShowMenu,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = stringResource(R.string.more_label),
                            tint = foreground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(controlsBackground, panelShape)
                .padding(horizontal = controlPadding, vertical = 8.dp),
        ) {
            // Audio Quality Badge (respects ShowAudioQualityBadgeKey, genuine format only)
            AudioQualityBadge(
                videoId = mediaMetadata.id,
                explicitFormat = currentFormat,
                contentColor = foreground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Title and Like Action
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { titleActions.onTitleClick() }
                    )
                    Spacer(Modifier.height(4.dp))
                    ClickableArtists(
                        artists = mediaMetadata.artists,
                        onArtistClick = titleActions.onArtistClick,
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryForeground,
                        modifier = if (animateArtwork) Modifier.basicMarquee() else Modifier
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.toggleLike()
                }) {
                    Icon(
                        painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = stringResource(if (isLiked) R.string.action_remove_like else R.string.action_like),
                        tint = if (isLiked) primaryButtonColor else foreground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

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
                    text = makeTimeString(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryForeground
                )
            }

            Spacer(Modifier.height(14.dp))

            // Transport Controls Row (Tactile zero-bounce spring feedback)
            PlayerTransportRow(
                isPlaying = isPlaying,
                isLoading = isLoading,
                playbackState = playbackState,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                onPlayPause = {
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
                onSkipPrevious = { playerConnection.seekToPrevious() },
                onSkipNext = { playerConnection.seekToNext() },
                variant = TransportVariant.IMMERSIVE_EXTENDED,
                primaryContainerColor = primaryButtonColor,
                primaryContentColor = primaryIconColor,
                secondaryContainerColor = sideButtonColor,
                secondaryContentColor = sideIconColor,
                playButtonSize = 72.dp,
                sideButtonSize = 56.dp,
                playIconSize = 48.dp,
                sideIconSize = 36.dp,
                haptic = haptic,
                leadingControl = {
                    IconButton(
                        modifier = Modifier.semantics { selected = shuffleModeEnabled },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.shuffle),
                            contentDescription = stringResource(R.string.shuffle),
                            tint = if (shuffleModeEnabled) primaryButtonColor else secondaryForeground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                trailingControl = {
                    IconButton(
                        modifier = Modifier.semantics { selected = repeatMode != Player.REPEAT_MODE_OFF },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.player.toggleRepeatMode()
                        },
                    ) {
                        Icon(
                            painter = painterResource(
                                when (repeatMode) {
                                    Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                    else -> R.drawable.repeat
                                }
                            ),
                            contentDescription = stringResource(R.string.repeat),
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) primaryButtonColor else secondaryForeground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            // Integrated Volume Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.volume_off),
                    contentDescription = null,
                    tint = secondaryForeground.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )

                val volumeLabel = stringResource(R.string.volume)
                Slider(
                    value = playerVolume.coerceIn(0f, 1f),
                    onValueChange = onVolumeChange,
                    colors = SliderDefaults.colors(
                        thumbColor = foreground,
                        activeTrackColor = foreground,
                        inactiveTrackColor = foreground.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .semantics { contentDescription = volumeLabel },
                )

                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = null,
                    tint = secondaryForeground.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
