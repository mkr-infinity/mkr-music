/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.mkr.R
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.ui.player.AudioQualityBadge
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.component.ResizableIconButton
import com.music.mkr.ui.player.ClickableArtists
import com.music.mkr.ui.player.rememberPlayerTitleActions
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.ThumbnailCornerRadius
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.utils.makeTimeString

/**
 * Classic Player style:
 * Timeless layout with circular controls, classic sizing, repeat, like, and queue shortcuts.
 */
@Composable
fun ClassicPlayer(
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

    val playPauseRoundness by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 36.dp,
        animationSpec = tween(durationMillis = 90, easing = LinearEasing),
        label = "classicPlayPauseRoundness"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 68.dp)
    ) {
        // Top collapsed bar / drag handle area breathing room
        Spacer(Modifier.height(12.dp))

        // Large Album Artwork
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(ThumbnailCornerRadius),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaMetadata.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = mediaMetadata.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Audio Quality Badge (respects ShowAudioQualityBadgeKey, genuine format only)
        AudioQualityBadge(
            videoId = mediaMetadata.id,
            contentColor = textBackgroundColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Title and actions row
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = textBackgroundColor.copy(alpha = 0.72f),
                    modifier = Modifier.basicMarquee()
                )
            }

            Spacer(Modifier.width(12.dp))

            IconButton(onClick = onShowMenu) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = stringResource(R.string.more_label),
                    tint = textBackgroundColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Progress Seekbar
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

        Spacer(Modifier.height(20.dp))

        // Classic Playback Controls: Repeat, Skip Previous, Play/Pause, Skip Next, Favorite
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Repeat button
            Box(contentAlignment = Alignment.Center) {
                ResizableIconButton(
                    icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                        Player.REPEAT_MODE_ALL -> R.drawable.repeat
                        else -> R.drawable.repeat
                    },
                    contentDescription = stringResource(R.string.repeat),
                    color = textBackgroundColor,
                    modifier = Modifier
                        .size(36.dp)
                        .alpha(if (repeatMode == Player.REPEAT_MODE_OFF) 0.45f else 1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.player.toggleRepeatMode()
                    }
                )
            }

            // Skip Previous
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.seekToPrevious()
                },
                enabled = canSkipPrevious,
                shape = CircleShape,
                color = textBackgroundColor.copy(alpha = if (canSkipPrevious) 0.12f else 0.05f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = stringResource(R.string.widget_previous),
                        tint = textBackgroundColor.copy(alpha = if (canSkipPrevious) 1f else 0.38f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Play / Pause Circle
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
                shape = RoundedCornerShape(playPauseRoundness),
                color = textButtonColor,
                shadowElevation = 8.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = iconButtonColor,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                when {
                                    playbackState == Player.STATE_ENDED -> R.drawable.replay
                                    isPlaying -> R.drawable.pause
                                    else -> R.drawable.play
                                }
                            ),
                            contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                            colorFilter = ColorFilter.tint(iconButtonColor),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Skip Next
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.seekToNext()
                },
                enabled = canSkipNext,
                shape = CircleShape,
                color = textBackgroundColor.copy(alpha = if (canSkipNext) 0.12f else 0.05f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = stringResource(R.string.next),
                        tint = textBackgroundColor.copy(alpha = if (canSkipNext) 1f else 0.38f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Like / Favorite
            Box(contentAlignment = Alignment.Center) {
                ResizableIconButton(
                    icon = if (isLiked) R.drawable.favorite else R.drawable.favorite_border,
                    contentDescription = stringResource(if (isLiked) R.string.action_remove_like else R.string.action_like),
                    color = if (isLiked) MaterialTheme.colorScheme.error else textBackgroundColor,
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.toggleLike()
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
