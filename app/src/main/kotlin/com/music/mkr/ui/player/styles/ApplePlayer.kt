/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.ui.player.AudioQualityBadge
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.mkr.R
import com.music.mkr.constants.CropAlbumArtKey
import com.music.mkr.constants.HidePlayerThumbnailKey
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.PlayerHorizontalPadding
import com.music.mkr.constants.PlayerThumbnailShadowElevationKey
import com.music.mkr.constants.ShowAudioQualityBadgeKey
import com.music.mkr.constants.ShowPlayerThumbnailShadowKey
import com.music.mkr.constants.ThumbnailCornerRadius
import com.music.mkr.extensions.SwipeGesture
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.mkrmusic.getConnectedBluetoothDeviceName
import com.music.mkr.mkrmusic.isBuds
import com.music.mkr.mkrmusic.isSpeaker
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.component.ResizableIconButton
import com.music.mkr.ui.player.ClickableArtists
import com.music.mkr.ui.player.rememberPlayerTitleActions
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Apple Player (MKR Music Signature Design):
 * The original, fluid Apple-inspired player architecture featuring rounded artwork,
 * smooth dynamic play/pause morphing, system volume slider, output device indicator,
 * and seamless background treatment.
 */
@Composable
fun ApplePlayer(
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
    currentFormat: com.music.mkr.db.entities.FormatEntity? = null,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val titleActions = rememberPlayerTitleActions(mediaMetadata, navController, state)

    val hidePlayerThumbnail by rememberPreference(HidePlayerThumbnailKey, false)
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)
    val showPlayerThumbnailShadow by rememberPreference(ShowPlayerThumbnailShadowKey, defaultValue = false)
    val playerThumbnailShadowElevation by rememberPreference(PlayerThumbnailShadowElevationKey, defaultValue = 8f)
    val showAudioQualityBadge by rememberPreference(ShowAudioQualityBadgeKey, defaultValue = false)
    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.GRADIENT)

    // Volume management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxSystemVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var systemVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                    systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    val animatedSystemVolume by animateFloatAsState(
        targetValue = systemVolume,
        animationSpec = tween(150, easing = LinearOutSlowInEasing),
        label = "appleVolumeAnimation"
    )

    val playPauseRoundness by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 36.dp,
        animationSpec = tween(durationMillis = 90, easing = LinearEasing),
        label = "applePlayPauseRoundness"
    )

    val bluetoothDeviceName = remember(context) { getConnectedBluetoothDeviceName(context) }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Album Art
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(ThumbnailCornerRadius),
                    shadowElevation = if (showPlayerThumbnailShadow) playerThumbnailShadowElevation.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                        .SwipeGesture(
                            enabled = true,
                            onSwipeLeft = { if (canSkipNext) playerConnection.player.seekToNext() },
                            onSwipeRight = { if (canSkipPrevious) playerConnection.player.seekToPrevious() }
                        )
                ) {
                    if (hidePlayerThumbnail || mediaMetadata.thumbnailUrl.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mkr_music_small_icon),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = textButtonColor.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = mediaMetadata.thumbnailUrl,
                            contentDescription = mediaMetadata.title,
                            contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // Right: Controls Column
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                        Spacer(Modifier.height(2.dp))
                        ClickableArtists(
                            artists = mediaMetadata.artists,
                            onArtistClick = titleActions.onArtistClick,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textBackgroundColor.copy(alpha = 0.7f),
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(textButtonColor.copy(alpha = 0.15f))
                                .clickable(onClick = onShowMenu),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more),
                                tint = textButtonColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(textButtonColor.copy(alpha = 0.15f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    playerConnection.toggleLike()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isLiked) R.drawable.favorite else R.drawable.favorite_border
                                ),
                                contentDescription = null,
                                tint = if (isLiked) MaterialTheme.colorScheme.error else textButtonColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Progress slider
                PlayerSliderTrack(
                    value = (sliderPosition ?: position).toFloat(),
                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                    onValueChange = { onSliderValueChange(it.toLong()) },
                    onValueChangeFinished = onSliderValueChangeFinished,
                    sliderColors = PlayerSliderColors.getSliderColors(
                        activeColor = textButtonColor.copy(alpha = 0.7f),
                        playerBackground = playerBackground,
                        useDarkTheme = true
                    ),
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxWidth()
                )

                // Controls: Previous, Play/Pause, Next
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResizableIconButton(
                        icon = R.drawable.skip_previous,
                        contentDescription = stringResource(R.string.widget_previous),
                        enabled = canSkipPrevious,
                        color = textBackgroundColor,
                        modifier = Modifier.size(44.dp).alpha(if (canSkipPrevious) 1f else 0.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToPrevious()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(playPauseRoundness))
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

                    ResizableIconButton(
                        icon = R.drawable.skip_next,
                        contentDescription = stringResource(R.string.next),
                        enabled = canSkipNext,
                        color = textBackgroundColor,
                        modifier = Modifier.size(44.dp).alpha(if (canSkipNext) 1f else 0.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToNext()
                        }
                    )
                }
            }
        }
    } else {
        // Portrait Mode
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 68.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Large Apple Album Artwork
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(ThumbnailCornerRadius),
                    shadowElevation = if (showPlayerThumbnailShadow) playerThumbnailShadowElevation.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                        .SwipeGesture(
                            enabled = true,
                            onSwipeLeft = { if (canSkipNext) playerConnection.player.seekToNext() },
                            onSwipeRight = { if (canSkipPrevious) playerConnection.player.seekToPrevious() }
                        )
                ) {
                    if (hidePlayerThumbnail || mediaMetadata.thumbnailUrl.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mkr_music_small_icon),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = textButtonColor.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = mediaMetadata.thumbnailUrl,
                            contentDescription = mediaMetadata.title,
                            contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Title and Actions Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
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
                        style = MaterialTheme.typography.titleMedium,
                        color = textBackgroundColor.copy(alpha = 0.7f),
                        modifier = Modifier.basicMarquee()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // More action button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(textButtonColor.copy(alpha = 0.15f))
                            .clickable(onClick = onShowMenu),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = stringResource(R.string.more),
                            tint = textButtonColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Like button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(textButtonColor.copy(alpha = 0.15f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playerConnection.toggleLike()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isLiked) R.drawable.favorite else R.drawable.favorite_border
                            ),
                            contentDescription = null,
                            tint = if (isLiked) MaterialTheme.colorScheme.error else textButtonColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress Slider
            PlayerSliderTrack(
                value = (sliderPosition ?: position).toFloat(),
                valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                onValueChange = { onSliderValueChange(it.toLong()) },
                onValueChangeFinished = onSliderValueChangeFinished,
                sliderColors = PlayerSliderColors.getSliderColors(
                    activeColor = textButtonColor.copy(alpha = 0.7f),
                    playerBackground = playerBackground,
                    useDarkTheme = true
                ),
                isPlaying = isPlaying,
                modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
            )

            // Time stamps and quality badge
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Text(
                    text = makeTimeString(position),
                    style = MaterialTheme.typography.labelMedium,
                    color = textBackgroundColor.copy(alpha = 0.7f)
                )

                AudioQualityBadge(
                    videoId = mediaMetadata.id,
                    explicitFormat = currentFormat,
                    contentColor = textBackgroundColor
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = textBackgroundColor.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Playback controls row: 48dp Prev, 100dp Play/Pause, 48dp Next
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = R.drawable.skip_previous,
                        contentDescription = stringResource(R.string.widget_previous),
                        enabled = canSkipPrevious,
                        color = textBackgroundColor,
                        modifier = Modifier
                            .size(48.dp)
                            .alpha(if (canSkipPrevious) 1f else 0.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToPrevious()
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(playPauseRoundness))
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
                            color = textButtonColor,
                            modifier = Modifier.size(48.dp),
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
                            colorFilter = ColorFilter.tint(textBackgroundColor),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = R.drawable.skip_next,
                        contentDescription = stringResource(R.string.next),
                        enabled = canSkipNext,
                        color = textBackgroundColor,
                        modifier = Modifier
                            .size(48.dp)
                            .alpha(if (canSkipNext) 1f else 0.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playerConnection.seekToNext()
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // System Volume Slider Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Icon(
                    painter = painterResource(R.drawable.volume_mute),
                    contentDescription = null,
                    tint = textButtonColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(12.dp))

                Slider(
                    value = animatedSystemVolume,
                    onValueChange = { newVolume ->
                        scope.launch(Dispatchers.Default) {
                            val newStep = (newVolume * maxSystemVolume).roundToInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newStep, 0)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = textButtonColor,
                        activeTrackColor = textButtonColor.copy(alpha = 0.7f),
                        inactiveTrackColor = textButtonColor.copy(alpha = 0.15f)
                    )
                )

                Spacer(Modifier.width(12.dp))

                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = null,
                    tint = textButtonColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Output Device / Bluetooth Indicator
            if (bluetoothDeviceName != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(
                            when {
                                isSpeaker(bluetoothDeviceName) -> R.drawable.home_speaker_devices
                                isBuds(bluetoothDeviceName) -> R.drawable.bluetooth
                                else -> R.drawable.headphones
                            }
                        ),
                        contentDescription = null,
                        tint = textButtonColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = bluetoothDeviceName,
                        style = MaterialTheme.typography.labelSmall,
                        color = textButtonColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
