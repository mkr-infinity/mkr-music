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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
 * Immersive Player style:
 * Artwork-led layout with spacious transport controls.
 */
@Composable
fun ImmersivePlayer(
    mediaMetadata: MediaMetadata,
    itemArtwork: String?,
    animateArtwork: Boolean,
    foreground: Color,
    primaryButtonColor: Color,
    primaryIconColor: Color,
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
    currentFormat: FormatEntity?,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val titleActions = rememberPlayerTitleActions(mediaMetadata, navController, state)

    val secondaryForeground = foreground.copy(alpha = 0.82f)
    val subtitle = mediaMetadata.album?.title ?: stringResource(R.string.queue)
    val closeLabel = stringResource(R.string.close)
    val sheetScrollConnection = remember(state) { state.preUpPostDownNestedScrollConnection }

    AdaptivePlayerLayout(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(sheetScrollConnection)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = QueuePeekHeight + 8.dp),
        artworkFraction = 1f,
        immersive = true,
        artwork = { size ->
            ImmersiveArtwork(
                trackId = mediaMetadata.id,
                thumbnail = mediaMetadata.thumbnailUrl,
                itemArtwork = itemArtwork,
                animate = animateArtwork,
                backgroundColor = foreground.copy(alpha = 0.05f),
                modifier = Modifier.size(size).clip(MaterialTheme.shapes.large),
                contentDescription = mediaMetadata.title,
            )
        },
        header = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { state.collapseSoft() },
                    modifier = Modifier
                        .align(if (maxWidth < 144.dp) Alignment.CenterStart else Alignment.Center)
                        .size(48.dp)
                        .semantics { contentDescription = closeLabel },
                ) {
                    Box(
                        Modifier
                            .size(width = 32.dp, height = 4.dp)
                            .background(secondaryForeground, CircleShape)
                    )
                }

                IconButton(
                    onClick = onShowMenu,
                    modifier = Modifier.align(Alignment.CenterEnd).size(48.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = stringResource(R.string.more_label),
                        tint = foreground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaMetadata.title,
                        style = MaterialTheme.typography.headlineSmall,
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
                    Text(
                        text = "${stringResource(R.string.playing_from)} · $subtitle",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
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
                        modifier = Modifier.size(26.dp)
                    )
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
                    text = "-" + makeTimeString((duration - (sliderPosition ?: position)).coerceAtLeast(0L)),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryForeground
                )
            }

            Spacer(Modifier.height(16.dp))

            // Immersive Playback Controls (Tactile spring zero-bounce feedback)
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
                primaryContainerColor = primaryButtonColor,
                primaryContentColor = primaryIconColor,
                secondaryContainerColor = foreground.copy(alpha = 0.06f),
                secondaryContentColor = foreground,
                playButtonWidth = 100.dp,
                playButtonHeight = 68.dp,
                sideButtonWidth = 56.dp,
                sideButtonHeight = 68.dp,
                playIconSize = 34.dp,
                sideIconSize = 30.dp,
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

            Spacer(Modifier.height(16.dp))
        }
    }
}
