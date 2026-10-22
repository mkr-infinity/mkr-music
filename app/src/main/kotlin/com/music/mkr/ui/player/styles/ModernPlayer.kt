/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.mkr.R
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.player.ClickableArtists
import com.music.mkr.ui.player.rememberPlayerTitleActions
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.utils.makeTimeString

/**
 * Modern Player style (ArchiveTune V2 layout):
 * Pill-shaped tonal playback controls with proportional geometry.
 */
@Composable
fun ModernPlayer(
    mediaMetadata: MediaMetadata,
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
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    textButtonColor: Color,
    iconButtonColor: Color,
    textBackgroundColor: Color,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    onShowLyrics: () -> Unit,
    onShowQueue: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val titleActions = rememberPlayerTitleActions(mediaMetadata, navController, state)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Center Artwork
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = mediaMetadata.thumbnailUrl,
                    contentDescription = mediaMetadata.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Title and actions row
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
                    color = textBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee()
                        .clickable { titleActions.onTitleClick() }
                )
                Spacer(Modifier.height(4.dp))
                ClickableArtists(
                    artists = mediaMetadata.artists,
                    onArtistClick = titleActions.onArtistClick,
                    style = MaterialTheme.typography.titleMedium,
                    color = textBackgroundColor.copy(alpha = 0.72f),
                    modifier = Modifier.basicMarquee()
                )
            }

            Spacer(Modifier.width(12.dp))

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playerConnection.toggleLike()
            }) {
                Icon(
                    painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                    contentDescription = stringResource(if (isLiked) R.string.action_remove_like else R.string.action_like),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else textBackgroundColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(onClick = onShowMenu) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = stringResource(R.string.more_label),
                    tint = textBackgroundColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Slider
        PlayerSliderTrack(
            sliderColors = PlayerSliderColors.getSliderColors(PlayerBackgroundStyle.DEFAULT, false),
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
                color = textBackgroundColor.copy(alpha = 0.7f)
            )
            Text(
                text = makeTimeString(duration),
                style = MaterialTheme.typography.labelMedium,
                color = textBackgroundColor.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Modern Pill Playback Controls (ArchiveTune V2 specification)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxW = maxWidth
            val playButtonHeight = (maxW / 5.5f).coerceIn(60.dp, 76.dp)
            val playButtonWidth = playButtonHeight * 1.55f
            val sideButtonHeight = playButtonHeight * 0.82f
            val sideButtonWidth = sideButtonHeight * 1.3f

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToPrevious()
                    },
                    enabled = canSkipPrevious,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = textButtonColor.copy(alpha = 0.18f),
                        contentColor = textBackgroundColor
                    ),
                    modifier = Modifier
                        .size(width = sideButtonWidth, height = sideButtonHeight)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = stringResource(R.string.widget_previous),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = textButtonColor,
                        contentColor = iconButtonColor
                    ),
                    modifier = Modifier
                        .size(width = playButtonWidth, height = playButtonHeight)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = iconButtonColor,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
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
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToNext()
                    },
                    enabled = canSkipNext,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = textButtonColor.copy(alpha = 0.18f),
                        contentColor = textBackgroundColor
                    ),
                    modifier = Modifier
                        .size(width = sideButtonWidth, height = sideButtonHeight)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = stringResource(R.string.next),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bottom action bar: Shuffle, Lyrics, Queue, Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
            }) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = stringResource(R.string.shuffle),
                    tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else textBackgroundColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onShowLyrics) {
                Icon(
                    painter = painterResource(R.drawable.lyrics),
                    contentDescription = stringResource(R.string.lyrics),
                    tint = textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onShowQueue) {
                Icon(
                    painter = painterResource(R.drawable.queue_music),
                    contentDescription = stringResource(R.string.queue),
                    tint = textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playerConnection.player.toggleRepeatMode()
            }) {
                Icon(
                    painter = painterResource(
                        when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                            else -> R.drawable.repeat
                        }
                    ),
                    contentDescription = stringResource(R.string.repeat),
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else textBackgroundColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
