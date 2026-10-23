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
import androidx.compose.ui.semantics.Role
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
import com.music.mkr.ui.player.AudioQualityBadge
import com.music.mkr.ui.player.ClickableArtists
import com.music.mkr.ui.player.rememberPlayerTitleActions
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.utils.makeTimeString

/**
 * Minimal Player style:
 * Clean square geometry, understated surfaces, and balanced controls.
 */
@Composable
fun MinimalPlayer(
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
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 68.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Clean Artwork with sharp minimal corners
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
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

        // Audio Quality Badge (respects ShowAudioQualityBadgeKey, genuine format only)
        AudioQualityBadge(
            videoId = mediaMetadata.id,
            contentColor = textBackgroundColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaMetadata.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee()
                        .clickable { titleActions.onTitleClick() }
                )
                Spacer(Modifier.height(2.dp))
                ClickableArtists(
                    artists = mediaMetadata.artists,
                    onArtistClick = titleActions.onArtistClick,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textBackgroundColor.copy(alpha = 0.65f),
                    modifier = Modifier.basicMarquee()
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
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onShowMenu) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = stringResource(R.string.more_label),
                    tint = textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Minimal Slider
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
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = makeTimeString(sliderPosition ?: position),
                style = MaterialTheme.typography.labelSmall,
                color = textBackgroundColor.copy(alpha = 0.6f)
            )
            Text(
                text = makeTimeString(duration),
                style = MaterialTheme.typography.labelSmall,
                color = textBackgroundColor.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(18.dp))

        // Minimal Controls Row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shuffle (40dp size, 10dp rounded corner)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = stringResource(R.string.shuffle),
                    tint = textBackgroundColor.copy(alpha = if (shuffleModeEnabled) 1f else 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Skip Previous (52dp size, 14dp rounded box, 0.08 alpha bg)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(textBackgroundColor.copy(alpha = 0.08f))
                    .clickable(
                        role = Role.Button,
                        enabled = canSkipPrevious
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToPrevious()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = stringResource(R.string.widget_previous),
                    tint = textBackgroundColor.copy(alpha = if (canSkipPrevious) 0.9f else 0.4f),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Play / Pause (64dp size, 18dp rounded box, solid container)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(textButtonColor)
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = iconButtonColor,
                        strokeWidth = 2.5.dp
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
                        tint = iconButtonColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Skip Next (52dp size, 14dp rounded box, 0.08 alpha bg)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(textBackgroundColor.copy(alpha = 0.08f))
                    .clickable(
                        role = Role.Button,
                        enabled = canSkipNext
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToNext()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = stringResource(R.string.next),
                    tint = textBackgroundColor.copy(alpha = if (canSkipNext) 0.9f else 0.4f),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Repeat (40dp size, 10dp rounded box)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.player.toggleRepeatMode()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        when {
                            repeatMode == Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                            else -> R.drawable.repeat
                        }
                    ),
                    contentDescription = stringResource(R.string.repeat),
                    tint = textBackgroundColor.copy(alpha = if (repeatMode == Player.REPEAT_MODE_OFF) 0.4f else 1f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
