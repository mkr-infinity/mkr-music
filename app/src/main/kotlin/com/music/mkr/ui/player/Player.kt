/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.produceState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import coil3.size.Size as CoilSize
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalDownloadUtil
import com.music.mkr.LocalListenTogetherManager
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.constants.CropAlbumArtKey
import com.music.mkr.constants.DarkModeKey
import com.music.mkr.constants.HidePlayerThumbnailKey
import com.music.mkr.constants.EnableLyricsThumbnailPlayPauseKey
import com.music.mkr.constants.KeepScreenOn
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.PlayerButtonsStyle
import com.music.mkr.constants.PlayerButtonsStyleKey
import com.music.mkr.constants.PlayerHorizontalPadding
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.constants.PlayerStyleKey
import com.music.mkr.ui.player.styles.ClassicPlayer
import com.music.mkr.ui.player.styles.ModernPlayer
import com.music.mkr.ui.player.styles.MinimalPlayer
import com.music.mkr.ui.player.styles.CinematicPlayer
import com.music.mkr.ui.player.styles.ApplePlayer
import com.music.mkr.ui.player.styles.ImmersivePlayer
import com.music.mkr.ui.player.styles.ImmersiveExtendedPlayer
import com.music.mkr.ui.player.styles.MaterialExtendedPlayer
import com.music.mkr.ui.player.styles.EditorialPlayer
import com.music.mkr.constants.QueuePeekHeight
import com.music.mkr.constants.SliderStyle
import com.music.mkr.constants.SliderStyleKey
import com.music.mkr.constants.SquigglySliderKey
import com.music.mkr.constants.SwipeLyricsKey
import com.music.mkr.constants.ThumbnailCornerRadius
import com.music.mkr.constants.ShowAudioQualityBadgeKey
import com.music.mkr.db.entities.LyricsEntity
import com.music.mkr.extensions.SwipeGesture
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.listentogether.RoomRole
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.ExoDownloadService
import com.music.mkr.mkrmusic.getConnectedBluetoothDeviceName
import com.music.mkr.mkrmusic.isBuds
import com.music.mkr.mkrmusic.isSpeaker
import com.music.mkr.mkrmusic.AudioDeviceBottomSheet
import com.music.mkr.ui.component.BottomSheet
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.Lyrics
import com.music.mkr.ui.component.LyricsV2
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.component.ResizableIconButton
import com.music.mkr.ui.component.SquigglySlider
import com.music.mkr.ui.component.WavySlider
import com.music.mkr.ui.component.rememberBottomSheetState
import com.music.mkr.ui.menu.OldPlayerMenu
import com.music.mkr.ui.menu.PlayerMenu
import com.music.mkr.ui.component.VolumeSlider
import com.music.mkr.ui.screens.settings.DarkMode
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.ui.utils.ShowMediaInfo
import com.music.mkr.ui.utils.ShowOffsetDialog
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.ermohdamaan.justforpixel.justforpixelexpressivelab.core.components.sliders.ExpressiveWavySlider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt
import com.music.mkr.ui.component.Icon as MIcon
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.extensions.metadata
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playerHazeState = remember { HazeState() }

    val (showAudioQualityBadge) = rememberPreference(
        ShowAudioQualityBadgeKey,
        defaultValue = false
    )
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(HidePlayerThumbnailKey, false)
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)
    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.GRADIENT
    )
    val playerButtonsStyle by rememberEnumPreference(
        key = PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val shouldUseDarkButtonColors = remember(playerBackground, useDarkTheme) {
        when (playerBackground) {
            PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH -> true
            PlayerBackgroundStyle.DEFAULT -> useDarkTheme
        }
    }

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val swipeLyrics by rememberPreference(SwipeLyricsKey, false)
    val enableLyricsThumbnailPlayPause by rememberPreference(EnableLyricsThumbnailPlayPauseKey, false)
    val isKeepScreenOn by rememberPreference(KeepScreenOn, false)
    val keepScreenOn = isPlaying && isKeepScreenOn

    DisposableEffect(playerBackground, state.isExpanded, useDarkTheme, keepScreenOn) {
        val window = (context as? android.app.Activity)?.window
        if (window != null && state.isExpanded) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            when (playerBackground) {
                PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH -> {
                    insetsController.isAppearanceLightStatusBars = false
                }
                PlayerBackgroundStyle.DEFAULT -> {
                    insetsController.isAppearanceLightStatusBars = !useDarkTheme
                }
            }

            if (keepScreenOn && state.isExpanded)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    val onBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val useBlackBackground =
        remember(isSystemInDarkTheme, darkTheme, pureBlack) {
            val useDarkTheme =
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            useDarkTheme && pureBlack
        }

    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)
    val automix by playerConnection.service.automixItems.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val isMuted by playerConnection.isMuted.collectAsState()
    val playerVolume by playerConnection.service.playerVolume.collectAsState()

    val (audioQuality) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.SLIM)
    val squigglySlider by rememberPreference(SquigglySliderKey, defaultValue = false)
    
    // Listen Together state (reactive)
    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsState(initial = RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST
    
    // Cast state - safely access castConnectionHandler to prevent crashes during service lifecycle changes
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }
    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
    val castPosition by castHandler?.castPosition?.collectAsState() ?: remember { mutableLongStateOf(0L) }
    val castDuration by castHandler?.castDuration?.collectAsState() ?: remember { mutableLongStateOf(0L) }
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val castVolume by castHandler?.castVolume?.collectAsState() ?: remember { mutableFloatStateOf(1f) }
    
    // Use Cast state when casting, otherwise local player
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying

    // Use State objects for position/duration to pass to MiniPlayer without causing recomposition
    // These states persist across playback state changes to ensure continuous progress updates
    val positionState = remember { mutableLongStateOf(0L) }
    val durationState = remember { mutableLongStateOf(0L) }
    
    // Convenience accessors for local use
    var position by positionState
    var duration by durationState
    
    val effectivePosition by remember {
        derivedStateOf {
            if (isCasting) {
                castPosition
            } else {
                position
            }
        }
    }
    
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }
    // Track when we last manually set position to avoid Cast overwriting it
    var lastManualSeekTime by remember { mutableLongStateOf(0L) }
    
    val artworkPalette = com.music.mkr.ui.theme.rememberArtworkPalette(mediaMetadata)
    
    var gradientColors by remember {
        mutableStateOf<List<Color>>(artworkPalette.backgroundGradient)
    }
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }

    if (!canSkipNext && automix.isNotEmpty()) {
        playerConnection.service.addToQueueAutomix(automix[0], 0)
    }

    val bluetoothDeviceName by produceState<String?>(initialValue = getConnectedBluetoothDeviceName(context)) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                value = getConnectedBluetoothDeviceName(context)
            }
        }

        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : android.media.AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out android.media.AudioDeviceInfo>?) {
                    value = getConnectedBluetoothDeviceName(context)
                }
                override fun onAudioDevicesRemoved(removedDevices: Array<out android.media.AudioDeviceInfo>?) {
                    value = getConnectedBluetoothDeviceName(context)
                }
            }
        } else null

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction("android.bluetooth.adapter.action.STATE_CHANGED")
            addAction("android.bluetooth.device.action.ACL_CONNECTED")
            addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
            addAction("android.media.AUDIO_BECOMING_NOISY")
        }
        
        context.registerReceiver(receiver, filter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
            audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        }
        
        awaitDispose {
            context.unregisterReceiver(receiver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
                audioManager.unregisterAudioDeviceCallback(callback)
            }
        }
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxSystemVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    val systemVolume by produceState(initialValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(receiver, filter)
        awaitDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val defaultGradientColors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata?.id, playerBackground, artworkPalette) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT) {
            gradientColors = artworkPalette.backgroundGradient
        } else if (playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                val cachedColors = gradientColorsCache[currentMetadata.id]
                if (cachedColors != null) {
                    gradientColors = cachedColors
                    return@LaunchedEffect
                }
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(100, 100)
                        .allowHardware(false)
                        .memoryCacheKey("gradient_${currentMetadata.id}")
                        .build()

                    val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
                    if (result != null) {
                        val bitmap = result.image?.toBitmap()
                        if (bitmap != null) {
                            val palette = withContext(Dispatchers.Default) {
                                Palette.from(bitmap)
                                    .maximumColorCount(8)
                                    .resizeBitmapArea(100 * 100)
                                    .generate()
                            }
                            val extractedColors = listOfNotNull(
                                palette.getVibrantColor(fallbackColor).let { Color(it) },
                                palette.getLightVibrantColor(fallbackColor).let { Color(it) },
                                palette.getDarkVibrantColor(fallbackColor).let { Color(it) },
                                palette.getMutedColor(fallbackColor).let { Color(it) },
                                palette.getLightMutedColor(fallbackColor).let { Color(it) },
                                palette.getDarkMutedColor(fallbackColor).let { Color(it) }
                            ).distinct()
                            gradientColorsCache[currentMetadata.id] = extractedColors
                            withContext(Dispatchers.Main) { gradientColors = extractedColors }
                        }
                    }
                }
            } else {
                gradientColors = artworkPalette.backgroundGradient
            }
        } else {
            gradientColors = emptyList()
        }
    }

    val TextBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            else -> artworkPalette.textPrimary
        },
        label = "TextBackgroundColor"
    )

    val icBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surface
            PlayerBackgroundStyle.BLUR -> Color.Black
            PlayerBackgroundStyle.GRADIENT -> Color.Black
            PlayerBackgroundStyle.GLOW_ANIMATED -> Color.Black
            PlayerBackgroundStyle.APPLE_MUSIC -> Color.Black
            PlayerBackgroundStyle.LIVE_MESH -> Color.Black
        },
        label = "icBackgroundColor"
    )

    val (textButtonColor, iconButtonColor) = when {
        playerBackground == PlayerBackgroundStyle.BLUR || 
        playerBackground == PlayerBackgroundStyle.GRADIENT ||
        playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED ||
        playerBackground == PlayerBackgroundStyle.APPLE_MUSIC ||
        playerBackground == PlayerBackgroundStyle.LIVE_MESH -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(artworkPalette.primaryAccent, com.music.mkr.ui.theme.PlayerColorExtractor.CharcoalDark)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary
                )
            }
        }
        else -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT ->
                    if (useDarkTheme) Pair(artworkPalette.primaryAccent, com.music.mkr.ui.theme.PlayerColorExtractor.CharcoalDark)
                    else Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }

    // Separate colors for Previous/Next buttons in PRIMARY/TERTIARY modes
    val (sideButtonContainerColor, sideButtonContentColor) = when {
        playerBackground == PlayerBackgroundStyle.BLUR || 
        playerBackground == PlayerBackgroundStyle.GRADIENT -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    Color.White.copy(alpha = 0.2f), 
                    Color.White
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    Color.White.copy(alpha = 0.2f), 
                    Color.White
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        else -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurface
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }

    val download by LocalDownloadUtil.current.getDownload(mediaMetadata?.id ?: "")
        .collectAsState(initial = null)

    val sleepTimerEnabled =
        remember(
            playerConnection.service.sleepTimer.triggerTime,
            playerConnection.service.sleepTimer.pauseWhenSongEnd
        ) {
            playerConnection.service.sleepTimer.isActive
        }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft =
                    if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                        playerConnection.player.duration - playerConnection.player.currentPosition
                    } else {
                        playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                    }
                delay(1000L)
            }
        }
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showInlineLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    var isFullScreen by rememberSaveable {
        mutableStateOf(false)
    }

    // Position update - only for local playback
    // When casting, we use castPosition directly to avoid sync issues
    // Use isPlaying instead of playbackState to ensure continuous updates during playback
    LaunchedEffect(isPlaying, isCasting) {
        if (!isCasting && isPlaying) {
            while (isActive) {
                delay(100) // Update more frequently for smoother progress bar
                if (sliderPosition == null) { // Only update if user isn't dragging
                    position = playerConnection.player.currentPosition
                    duration = playerConnection.player.duration
                }
            }
        }
    }
    
    // Also update position when playback state changes (e.g., song change, seek)
    LaunchedEffect(playbackState, mediaMetadata?.id) {
        if (!isCasting) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
        }
    }
    
    // When casting, use Cast position/duration directly
    // But wait a bit after manual seeks to let Cast catch up
    LaunchedEffect(isCasting, castPosition, castDuration) {
        if (isCasting && sliderPosition == null) {
            val timeSinceManualSeek = System.currentTimeMillis() - lastManualSeekTime
            if (timeSinceManualSeek > 1500) {
                // Only update from Cast if we haven't manually seeked recently
                position = castPosition
                if (castDuration > 0) duration = castDuration
            }
        }
    }

    val dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = state.expandedBound,
        collapsedBound = dismissedBound + 1.dp,
        initialAnchor = 1
    )

    val bottomSheetBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC ->
            MaterialTheme.colorScheme.surfaceContainer
        PlayerBackgroundStyle.LIVE_MESH ->
            Color.Black
        else ->
            if (useBlackBackground) Color.Black
            else MaterialTheme.colorScheme.surfaceContainer
    }

    val backgroundAlpha = state.progress.coerceIn(0f, 1f)

    BottomSheet(
        state = state,
        modifier = modifier,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomSheetBackgroundColor)
            ) {
                when (playerBackground) {
                    PlayerBackgroundStyle.BLUR -> {
                        AnimatedContent(
                            targetState = mediaMetadata?.thumbnailUrl,
                            transitionSpec = {
                                fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
                            },
                            label = "blurBackground"
                        ) { thumbnailUrl ->
                            if (thumbnailUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(100, 100) // Lower resolution for better performance
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(120.dp) // hardware-efficient native Compose blur on small image
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.30f))
                                    )
                                }
                            }
                        }
                    }
                    PlayerBackgroundStyle.GRADIENT -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            transitionSpec = {
                                fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
                            },
                            label = "gradientBackground"
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val gradientColorStops = if (colors.size >= 3) {
                                    arrayOf(
                                        0.0f to colors[0],
                                        0.5f to colors[1],
                                        1.0f to colors[2]
                                    )
                                } else {
                                    arrayOf(
                                        0.0f to colors[0],
                                        0.6f to colors[0].copy(alpha = 0.7f),
                                        1.0f to Color.Black
                                    )
                                }
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .background(Brush.verticalGradient(colorStops = gradientColorStops))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                    PlayerBackgroundStyle.GLOW_ANIMATED -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            transitionSpec = {
                                fadeIn(tween(1200)) togetherWith fadeOut(tween(1200))
                            },
                            label = "GlowAnimatedContent"
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val infiniteTransition =
                                    rememberInfiniteTransition(label = "GlowAnimation")

                                val progress by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(20000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "glowProgress"
                                )

                                fun rotatedColorAt(index: Int): Color {
                                    val size = colors.size
                                    val idx = index.toFloat() + progress * size
                                    val a = kotlin.math.floor(idx).toInt() % size
                                    val b = (a + 1) % size
                                    val frac = idx - kotlin.math.floor(idx)
                                    return androidx.compose.ui.graphics.lerp(
                                        colors.getOrElse(a) { Color.DarkGray },
                                        colors.getOrElse(b) { Color.DarkGray },
                                        frac
                                    )
                                }

                                fun oscillate(
                                    min: Float,
                                    max: Float,
                                    phase: Float,
                                    speed: Float = 1f
                                ): Float {
                                    val v = kotlin.math.sin(
                                        2f * kotlin.math.PI.toFloat() * (progress * speed + phase)
                                    )
                                    return min + (max - min) * ((v + 1f) * 0.5f)
                                }

                                val color1 = rotatedColorAt(0)
                                val color2 = rotatedColorAt(1)
                                val color3 = rotatedColorAt(2)
                                val color4 = rotatedColorAt(3)
                                val color5 = rotatedColorAt(4)
                                val color6 = rotatedColorAt(5)

                                val o1x = oscillate(0.0f, 1.0f, 0.00f, 1.0f)
                                val o1y = oscillate(0.0f, 0.5f, 0.07f, 1.0f)
                                val r1 = oscillate(0.8f, 1.6f, 0.12f, 1.0f)

                                val o2x = oscillate(1.0f, 0.0f, 0.2f, 1.0f)
                                val o2y = oscillate(0.5f, 1.0f, 0.25f, 1.0f)
                                val r2 = oscillate(0.7f, 1.5f, 0.18f, 1.0f)

                                val o3x = oscillate(0.2f, 0.8f, 0.33f, 1.0f)
                                val o3y = oscillate(0.8f, 0.2f, 0.36f, 1.0f)
                                val r3 = oscillate(0.6f, 1.4f, 0.29f, 1.0f)

                                val o4x = oscillate(0.3f, 0.7f, 0.44f, 1.0f)
                                val o4y = oscillate(0.2f, 0.8f, 0.41f, 1.0f)
                                val r4 = oscillate(0.9f, 1.7f, 0.47f, 1.0f)

                                val o5x = oscillate(0.4f, 0.6f, 0.55f, 1.0f)
                                val o5y = oscillate(0.0f, 1.0f, 0.51f, 1.0f)
                                val r5 = oscillate(0.7f, 1.5f, 0.58f, 1.0f)

                                val o6x = oscillate(0.0f, 1.0f, 0.66f, 1.0f)
                                val o6y = oscillate(0.5f, 0.7f, 0.62f, 1.0f)
                                val r6 = oscillate(0.8f, 1.8f, 0.69f, 1.0f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .drawWithCache {
                                            val width = size.width
                                            val height = size.height
                                            val baseColor = Color(0xFF050505)

                                            val brush1 = Brush.radialGradient(
                                                colors = listOf(
                                                    color1.copy(alpha = 0.85f),
                                                    color1.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o1x, height * o1y),
                                                radius = width * r1
                                            )
                                            val brush2 = Brush.radialGradient(
                                                colors = listOf(
                                                    color2.copy(alpha = 0.8f),
                                                    color2.copy(alpha = 0.45f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o2x, height * o2y),
                                                radius = width * r2
                                            )
                                            val brush3 = Brush.radialGradient(
                                                colors = listOf(
                                                    color3.copy(alpha = 0.75f),
                                                    color3.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o3x, height * o3y),
                                                radius = width * r3
                                            )
                                            val brush4 = Brush.radialGradient(
                                                colors = listOf(
                                                    color4.copy(alpha = 0.7f),
                                                    color4.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o4x, height * o4y),
                                                radius = width * r4
                                            )
                                            val brush5 = Brush.radialGradient(
                                                colors = listOf(
                                                    color5.copy(alpha = 0.65f),
                                                    color5.copy(alpha = 0.3f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o5x, height * o5y),
                                                radius = width * r5
                                            )
                                            val brush6 = Brush.radialGradient(
                                                colors = listOf(
                                                    color6.copy(alpha = 0.6f),
                                                    color6.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o6x, height * o6y),
                                                radius = width * r6
                                            )

                                            onDrawBehind {
                                                drawRect(color = baseColor)
                                                drawRect(brush = brush1)
                                                drawRect(brush = brush2)
                                                drawRect(brush = brush3)
                                                drawRect(brush = brush4)
                                                drawRect(brush = brush5)
                                                drawRect(brush = brush6)
                                            }
                                        }
                                )
                            }
                        }
                    }
                    PlayerBackgroundStyle.APPLE_MUSIC -> {
                        AnimatedContent(
                            targetState = mediaMetadata?.thumbnailUrl,
                            transitionSpec = {
                                fadeIn(tween(1200)).togetherWith(fadeOut(tween(1200)))
                            },
                            label = "appleMusicBackground"
                        ) { thumbnailUrl ->
                            if (thumbnailUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                ) {
                                    // Full-Screen Ambient Blurred Background (Apple Music fluid mesh)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(128, 128) // Downsample significantly for performance
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(140.dp)
                                    )

                                    // Ambient gradient overlay for deep contrast and richness
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Black.copy(alpha = 0.15f),
                                                        Color.Black.copy(alpha = 0.55f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                    PlayerBackgroundStyle.LIVE_MESH -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "liveMeshRotation")
                        
                        val anchorRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = -360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(80000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "anchorRotation"
                        )
                        
                        val fastRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(40000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "fastRotation"
                        )
                        
                        val slowRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(60000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "slowRotation"
                        )

                        AnimatedContent(
                            targetState = mediaMetadata?.thumbnailUrl,
                            transitionSpec = {
                                fadeIn(tween(1500)).togetherWith(fadeOut(tween(1500)))
                            },
                            label = "liveMeshBackground"
                        ) { thumbnailUrl ->
                            if (thumbnailUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .graphicsLayer {
                                            // Scale up to avoid showing edges during rotation
                                            scaleX = 1.7f
                                            scaleY = 1.7f
                                        }
                                ) {
                                    val matrix = remember { 
                                        val m = ColorMatrix()
                                        m.setToSaturation(1.8f) // Reduced to avoid neon look
                                        m
                                    }
                                    val colorFilter = ColorFilter.colorMatrix(matrix)

                                    // Layer 1: The Anchor (Full Image, Counter-Clockwise)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(128, 128) // Downsample significantly for performance
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        colorFilter = colorFilter,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(100.dp)
                                            .graphicsLayer { rotationZ = anchorRotation }
                                    )

                                    // Layer 2: Fast Rotating Crop (Top-Left)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(128, 128) // Downsample significantly for performance
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        colorFilter = colorFilter,
                                        alignment = Alignment.TopStart,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(120.dp)
                                            .graphicsLayer { 
                                                rotationZ = fastRotation
                                                alpha = 0.6f
                                            }
                                    )

                                    // Layer 3: Slow Rotating Crop (Bottom-Right)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(thumbnailUrl)
                                            .size(128, 128) // Downsample significantly for performance
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        colorFilter = colorFilter,
                                        alignment = Alignment.BottomEnd,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(120.dp)
                                            .graphicsLayer { 
                                                rotationZ = slowRotation
                                                alpha = 0.5f
                                            }
                                    )
                                    
                                    // Global dark tint to prevent neon look + vertical gradient for depth
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.25f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                    PlayerBackgroundStyle.DEFAULT -> {
                        // Nothing
                    }
                }
            }
        },
        onDismiss = {
            playerConnection.service.clearAutomix()
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer(
                positionState = positionState,
                durationState = durationState
            )
        },
    ) {
        val playerStyle by rememberEnumPreference(PlayerStyleKey, defaultValue = PlayerStyle.CINEMATIC)
        val isLoading = playbackState == androidx.media3.common.Player.STATE_BUFFERING

        val onOpenMenu: (MediaMetadata) -> Unit = { metadata ->
            menuState.show {
                PlayerMenu(
                    mediaMetadata = metadata,
                    navController = navController,
                    playerBottomSheetState = state,
                    onShowDetailsDialog = {
                        metadata.id.let {
                            bottomSheetPageState.show {
                                ShowMediaInfo(it)
                            }
                        }
                    },
                    onDismiss = menuState::dismiss
                )
            }
        }

        BackHandler(enabled = showInlineLyrics && state.isExpanded) {
            showInlineLyrics = false
        }

        AnimatedContent(
            targetState = showInlineLyrics,
            label = "PlayerLyricsSwitch",
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
        ) { lyricsVisible ->
            if (lyricsVisible && mediaMetadata != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showInlineLyrics = false }) {
                                Icon(
                                    painter = painterResource(R.drawable.expand_more),
                                    contentDescription = stringResource(R.string.close),
                                    tint = textButtonColor
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mediaMetadata?.title.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = textButtonColor
                                )
                                Text(
                                    text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = textButtonColor.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { queueSheetState.expandSoft() }) {
                                Icon(
                                    painter = painterResource(R.drawable.queue_music),
                                    contentDescription = stringResource(R.string.queue),
                                    tint = textButtonColor
                                )
                            }
                        }

                        InlineLyricsView(
                            mediaMetadata = mediaMetadata,
                            showLyrics = true,
                            positionProvider = { effectivePosition }
                        )
                    }
                }
            } else {
                mediaMetadata?.let { metadata ->
                    when (playerStyle) {
                        PlayerStyle.APPLE -> ApplePlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize(),
                            currentFormat = currentFormat
                        )
                        PlayerStyle.CLASSIC -> ClassicPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.MODERN -> ModernPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.MINIMAL -> MinimalPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.CINEMATIC -> CinematicPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.IMMERSIVE -> ImmersivePlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            currentFormat = currentFormat,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.IMMERSIVE_EXTENDED -> ImmersiveExtendedPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerVolume = playerVolume,
                            currentFormat = currentFormat,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onVolumeChange = { playerConnection.service.playerVolume.value = it },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.MATERIAL_EXTENDED -> MaterialExtendedPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                        PlayerStyle.EDITORIAL -> EditorialPlayer(
                            mediaMetadata = metadata,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            canSkipPrevious = canSkipPrevious,
                            canSkipNext = canSkipNext,
                            sliderPosition = sliderPosition,
                            position = effectivePosition,
                            duration = duration,
                            repeatMode = repeatMode,
                            shuffleModeEnabled = playerConnection.player.shuffleModeEnabled,
                            isLiked = currentSong?.song?.liked == true,
                            playerConnection = playerConnection,
                            navController = navController,
                            state = state,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            textBackgroundColor = TextBackgroundColor,
                            onSliderValueChange = { sliderPosition = it },
                            onSliderValueChangeFinished = {
                                sliderPosition?.let { playerConnection.player.seekTo(it) }
                                sliderPosition = null
                            },
                            onShowLyrics = { showInlineLyrics = !showInlineLyrics },
                            onShowQueue = { queueSheetState.expandSoft() },
                            onShowMenu = { onOpenMenu(metadata) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !isFullScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Queue(
                state = queueSheetState,
                playerBottomSheetState = state,
            navController = navController,
            background =
            if (useBlackBackground) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.surface //fixed the issue causing the queue ui not good surfacecontainer
            },
            onBackgroundColor = onBackgroundColor,
            TextBackgroundColor = TextBackgroundColor,
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            pureBlack = pureBlack,
            showInlineLyrics = showInlineLyrics,
            playerBackground = playerBackground,
            onToggleLyrics = {
                showInlineLyrics = !showInlineLyrics
            },
            )
        }
    }
}

@Composable
fun InlineLyricsView(
    mediaMetadata: MediaMetadata?,
    showLyrics: Boolean,
    positionProvider: () -> Long
) {
    LyricsV2(
        mediaMetadata = mediaMetadata,
        showLyrics = showLyrics,
        positionProvider = positionProvider,
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun MoreActionsButton(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    state: BottomSheetState,
    textButtonColor: Color,
    iconButtonColor: Color
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(textButtonColor)
            .clickable {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        playerBottomSheetState = state,
                        onShowDetailsDialog = {
                            mediaMetadata.id.let {
                                bottomSheetPageState.show {
                                    ShowMediaInfo(it)
                                }
                            }
                        },
                        onDismiss = menuState::dismiss
                    )
                }
            }
    ) {
        Image(
            painter = painterResource(R.drawable.more_vert),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconButtonColor)
        )
    }
}

@Composable
private fun PlayerMoreMenuButton(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    state: BottomSheetState,
    textButtonColor: Color,
    iconButtonColor: Color,
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    Box(
        contentAlignment = Alignment.Center,
        modifier =
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(textButtonColor)
            .clickable {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        playerBottomSheetState = state,
                        onShowDetailsDialog = {
                            mediaMetadata.id.let {
                                bottomSheetPageState.show {
                                    ShowMediaInfo(it)
                                }
                            }
                        },
                        onDismiss = menuState::dismiss,
                    )
                }
            },
    ) {
        Image(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconButtonColor),
        )
    }
}
