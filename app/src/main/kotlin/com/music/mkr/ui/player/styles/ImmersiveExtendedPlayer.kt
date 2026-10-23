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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.mkr.ui.player.AudioQualityBadge
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.mkr.R
import com.music.mkr.db.entities.FormatEntity
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
 * Immersive Extended Player style:
 * Ambient blurred backdrop, queue subtitle context, integrated volume bar, and rich metadata.
 */
@Composable
fun ImmersiveExtendedPlayer(
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

    val foreground = Color.White
    val secondaryForeground = foreground.copy(alpha = 0.72f)
    val subtitle = mediaMetadata.album?.title ?: stringResource(R.string.queue)

    Box(modifier = modifier.fillMaxSize()) {
        // High-blur ambient backdrop
        AsyncImage(
            model = mediaMetadata.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(54.dp)
        )

        // Multi-stop gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 68.dp)
        ) {
            // Header Top Bar with Context Subtitle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { state.collapseSoft() }) {
                    Icon(
                        painter = painterResource(R.drawable.expand_more),
                        contentDescription = "Collapse",
                        tint = foreground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.playing_from),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryForeground.copy(alpha = 0.6f)
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

                IconButton(onClick = onShowMenu) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = stringResource(R.string.more_label),
                        tint = foreground
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.weight(1f))

            Spacer(Modifier.height(16.dp))

            // Audio Quality Badge (respects ShowAudioQualityBadgeKey, genuine format only)
            AudioQualityBadge(
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = foreground,
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
                        color = secondaryForeground,
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
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else foreground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress Slider
            PlayerSliderTrack(
                sliderColors = PlayerSliderColors.getSliderColors(PlayerBackgroundStyle.GRADIENT, true),
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

            // Transport Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                }) {
                    Icon(
                        painter = painterResource(R.drawable.shuffle),
                        contentDescription = stringResource(R.string.shuffle),
                        tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else foreground.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToPrevious()
                    },
                    enabled = canSkipPrevious
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = stringResource(R.string.widget_previous),
                        tint = foreground.copy(alpha = if (canSkipPrevious) 1f else 0.4f),
                        modifier = Modifier.size(34.dp)
                    )
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
                    shape = CircleShape,
                    color = foreground,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = Color.Black,
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
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playerConnection.seekToNext()
                    },
                    enabled = canSkipNext
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = stringResource(R.string.next),
                        tint = foreground.copy(alpha = if (canSkipNext) 1f else 0.4f),
                        modifier = Modifier.size(34.dp)
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
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else foreground.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

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
                )

                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = null,
                    tint = secondaryForeground.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
