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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.utils.makeTimeString

/**
 * Editorial Player style:
 * High-contrast two-tone layout, bold serif typography, die-cut artwork, and editorial action buttons.
 */
@Composable
fun EditorialPlayer(
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

    val accent = textBackgroundColor
    val field = textButtonColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 68.dp)
    ) {
        // Editorial Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f),
                onClick = { state.collapseSoft() },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.expand_more),
                        contentDescription = "Collapse",
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.player_style_editorial),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                ),
                color = accent.copy(alpha = 0.85f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.14f),
                    onClick = onShowLyrics,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.lyrics),
                            contentDescription = stringResource(R.string.lyrics),
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.14f),
                    onClick = onShowQueue,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.queue_music),
                            contentDescription = stringResource(R.string.queue),
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.14f),
                    onClick = onShowMenu,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = stringResource(R.string.more_label),
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Center Die-Cut Artwork
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 14.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
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

        Spacer(Modifier.height(18.dp))

        // Editorial Typography Title & Artist + Like Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = mediaMetadata.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee()
                        .clickable { titleActions.onTitleClick() }
                )
                Spacer(Modifier.height(4.dp))
                ClickableArtists(
                    artists = mediaMetadata.artists,
                    onArtistClick = titleActions.onArtistClick,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth().basicMarquee()
                )
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playerConnection.toggleLike()
            }) {
                Icon(
                    painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                    contentDescription = stringResource(if (isLiked) R.string.action_remove_like else R.string.action_like),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else accent.copy(alpha = 0.8f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Progress Slider
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
                color = accent.copy(alpha = 0.7f)
            )
            Text(
                text = makeTimeString(duration),
                style = MaterialTheme.typography.labelMedium,
                color = accent.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Editorial Playback Controls (Two-tone high-contrast styling)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
            }) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = stringResource(R.string.shuffle),
                    tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else accent.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.12f),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.seekToPrevious()
                },
                enabled = canSkipPrevious,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = stringResource(R.string.widget_previous),
                        tint = accent.copy(alpha = if (canSkipPrevious) 1f else 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Center Two-Tone Play Button
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
                shape = CircleShape,
                color = accent,
                modifier = Modifier.size(76.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = field,
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
                            tint = if (accent == Color.White) Color.Black else Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.12f),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.seekToNext()
                },
                enabled = canSkipNext,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = stringResource(R.string.next),
                        tint = accent.copy(alpha = if (canSkipNext) 1f else 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
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
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else accent.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
