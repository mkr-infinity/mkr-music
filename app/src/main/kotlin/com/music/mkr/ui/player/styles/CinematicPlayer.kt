/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Cinematic Player style (ArchiveTune V4 layout):
 * Asymmetric widescreen controls with scaled gaps and large animated play button.
 */
@Composable
fun CinematicPlayer(
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

    val cinematicPlayPauseCorner by animateDpAsState(
        targetValue = if (isPlaying) 28.dp else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cinematicPlayPauseCorner"
    )

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
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
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

        // Title and actions
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

        // Cinematic Controls Row (ArchiveTune V4 layout)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val baseLarge = 56.dp
            val baseSmall = 46.dp
            val baseGap = 12.dp
            val baseLargeIcon = 28.dp
            val baseSmallIcon = 22.dp
            val baseLargeRadius = 18.dp
            val baseSmallRadius = 16.dp
            val centerSize = 88.dp
            val centerPadding = 36.dp
            val sideTotal = (maxWidth - centerSize - centerPadding) / 2f
            val scale = ((sideTotal - baseGap) / (baseLarge + baseSmall)).coerceIn(0.65f, 1f)

            val large = baseLarge * scale
            val small = baseSmall * scale
            val gap = baseGap * scale
            val largeIcon = baseLargeIcon * scale
            val smallIcon = baseSmallIcon * scale
            val largeRadius = baseLargeRadius * scale
            val smallRadius = baseSmallRadius * scale

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left wing: Shuffle + Skip Previous
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                        },
                        shape = RoundedCornerShape(smallRadius),
                        color = textBackgroundColor.copy(alpha = if (shuffleModeEnabled) 0.22f else 0.08f),
                        modifier = Modifier.size(small)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = stringResource(R.string.shuffle),
                                tint = textBackgroundColor.copy(alpha = if (shuffleModeEnabled) 1f else 0.6f),
                                modifier = Modifier.size(smallIcon)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(gap))

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToPrevious()
                        },
                        enabled = canSkipPrevious,
                        shape = RoundedCornerShape(largeRadius),
                        color = textBackgroundColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(large)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.skip_previous),
                                contentDescription = stringResource(R.string.widget_previous),
                                tint = textBackgroundColor.copy(alpha = if (canSkipPrevious) 1f else 0.4f),
                                modifier = Modifier.size(largeIcon)
                            )
                        }
                    }
                }

                // Center: Big Play / Pause Button
                Box(
                    modifier = Modifier.padding(horizontal = (gap * 1.5f)),
                    contentAlignment = Alignment.Center
                ) {
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
                        shape = RoundedCornerShape(cinematicPlayPauseCorner),
                        color = textButtonColor,
                        modifier = Modifier.size(centerSize)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(38.dp),
                                    color = iconButtonColor,
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
                                    tint = iconButtonColor,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }
                }

                // Right wing: Skip Next + Repeat
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToNext()
                        },
                        enabled = canSkipNext,
                        shape = RoundedCornerShape(largeRadius),
                        color = textBackgroundColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(large)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.skip_next),
                                contentDescription = stringResource(R.string.next),
                                tint = textBackgroundColor.copy(alpha = if (canSkipNext) 1f else 0.4f),
                                modifier = Modifier.size(largeIcon)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(gap))

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.player.toggleRepeatMode()
                        },
                        shape = RoundedCornerShape(smallRadius),
                        color = textBackgroundColor.copy(alpha = if (repeatMode != Player.REPEAT_MODE_OFF) 0.22f else 0.08f),
                        modifier = Modifier.size(small)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(
                                    when (repeatMode) {
                                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                        else -> R.drawable.repeat
                                    }
                                ),
                                contentDescription = stringResource(R.string.repeat),
                                tint = textBackgroundColor.copy(alpha = if (repeatMode != Player.REPEAT_MODE_OFF) 1f else 0.6f),
                                modifier = Modifier.size(smallIcon)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Bottom Bar (Lyrics & Queue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShowLyrics) {
                Icon(
                    painter = painterResource(R.drawable.lyrics),
                    contentDescription = stringResource(R.string.lyrics),
                    tint = textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onShowQueue) {
                Icon(
                    painter = painterResource(R.drawable.queue_music),
                    contentDescription = stringResource(R.string.queue),
                    tint = textBackgroundColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
