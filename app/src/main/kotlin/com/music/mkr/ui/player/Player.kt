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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalDownloadUtil
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.constants.DarkModeKey
import com.music.mkr.constants.EnableLyricsThumbnailPlayPauseKey
import com.music.mkr.constants.KeepScreenOn
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.resolveFullPlayerBackground
import com.music.mkr.constants.PlayerButtonsStyle
import com.music.mkr.constants.PlayerButtonsStyleKey
import com.music.mkr.constants.PlayerHorizontalPadding
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.constants.PlayerStyleKey
import com.music.mkr.ui.player.styles.MinimalPlayer
import com.music.mkr.ui.player.styles.CinematicPlayer
import com.music.mkr.ui.player.styles.ImmersivePlayer
import com.music.mkr.constants.QueuePeekHeight
import com.music.mkr.constants.SliderStyle
import com.music.mkr.constants.SliderStyleKey
import com.music.mkr.constants.SquigglySliderKey
import com.music.mkr.constants.SwipeLyricsKey
import com.music.mkr.constants.ThumbnailCornerRadius
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.db.entities.LyricsEntity
import com.music.mkr.extensions.SwipeGesture
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.models.MediaMetadata
import com.music.mkr.playback.ExoDownloadService
import com.music.mkr.ui.component.LocalPlaybackCookieAngle
import com.music.mkr.ui.component.rememberPlaybackCookieAngle
import com.music.mkr.ui.component.BottomSheet
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.LyricsV2
import com.music.mkr.ui.component.PlayerSliderTrack
import com.music.mkr.ui.component.ResizableIconButton
import com.music.mkr.ui.component.SquigglySlider
import com.music.mkr.ui.component.WavySlider
import com.music.mkr.ui.component.rememberBottomSheetState
import com.music.mkr.ui.menu.LyricsMenu
import com.music.mkr.ui.menu.PlayerMenu
import com.music.mkr.ui.screens.settings.DarkMode
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.ui.theme.PlayerSliderColors
import com.music.mkr.ui.utils.ShowMediaInfo
import com.music.mkr.ui.utils.ShowOffsetDialog
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import timber.log.Timber
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

    val (interfaceAnimations) = rememberPreference(
        InterfaceAnimationsKey,
        defaultValue = true
    )
    val playerStyleName by rememberPreference(PlayerStyleKey, PlayerStyle.DEFAULT.name)
    val playerStyle = remember(playerStyleName) { PlayerStyle.fromString(playerStyleName) }
    val savedPlayerBackground by rememberPreference(PlayerBackgroundStyleKey, "")
    val playerBackground = resolveFullPlayerBackground(savedPlayerBackground, playerStyle)
    val playerButtonsStyle by rememberEnumPreference(
        key = PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackError by playerConnection.error.collectAsState()
    val swipeLyrics by rememberPreference(SwipeLyricsKey, false)
    val enableLyricsThumbnailPlayPause by rememberPreference(EnableLyricsThumbnailPlayPauseKey, false)
    val isKeepScreenOn by rememberPreference(KeepScreenOn, false)
    val keepScreenOn = isPlaying && isKeepScreenOn

    DisposableEffect(playerBackground, state.isExpanded, useDarkTheme, keepScreenOn) {
        val window = (context as? android.app.Activity)?.window
        if (window != null && state.isExpanded) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            when (playerBackground) {
                PlayerBackgroundStyle.DEFAULT -> {
                    insetsController.isAppearanceLightStatusBars = !useDarkTheme
                }
                else -> insetsController.isAppearanceLightStatusBars = false
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
    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)
    val automix by playerConnection.service.automixItems.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val isMuted by playerConnection.isMuted.collectAsState()

    val (audioQuality) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.WAVY)
    val squigglySlider by rememberPreference(SquigglySliderKey, defaultValue = true)
    
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
    val castIsBuffering by castHandler?.castIsBuffering?.collectAsState() ?: remember { mutableStateOf(false) }
    
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
    
    val artworkPalette = com.music.mkr.ui.theme.rememberArtworkPalette(mediaMetadata)
    val currentItem = playerConnection.player.currentMediaItem
    val currentItemArtwork = currentItem?.takeIf { it.mediaId == mediaMetadata?.id }
        ?.mediaMetadata?.artworkUri?.toString()
    val backgroundArtwork = playerBackgroundArtworkUrl(mediaMetadata?.thumbnailUrl, currentItemArtwork)
    val backgroundVisible = !state.isCollapsed && !state.isDismissed
    val fallbackGradient = PlayerColorExtractor.DefaultPalette.backgroundGradient
    val gradientColorsCache = remember { android.util.LruCache<String, List<Color>>(32) }
    var gradientColors by remember(backgroundArtwork, playerBackground) {
        mutableStateOf(fallbackGradient)
    }

    LaunchedEffect(canSkipNext, automix.firstOrNull()?.mediaId) {
        if (!canSkipNext && automix.isNotEmpty()) {
            playerConnection.service.addToQueueAutomix(automix[0], 0)
        }
    }

    LaunchedEffect(backgroundArtwork, playerBackground, backgroundVisible) {
        val url = backgroundArtwork ?: return@LaunchedEffect
        if (!backgroundVisible) return@LaunchedEffect

        when (playerBackground) {
            PlayerBackgroundStyle.GRADIENT -> {
                val palette = PlayerColorExtractor.extractPalette(context, url)
                if (palette.isFallback) Timber.w("Full-player gradient artwork unavailable")
                gradientColors = palette.backgroundGradient
            }
            PlayerBackgroundStyle.GLOW_ANIMATED -> {
                val cachedColors = gradientColorsCache.get(url)
                if (cachedColors != null) {
                    gradientColors = cachedColors
                    return@LaunchedEffect
                }

                val colors = try {
                    val bitmap = withContext(Dispatchers.IO) {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .size(100, 100)
                            .allowHardware(false)
                            .build()
                        context.imageLoader.execute(request).image?.toBitmap()
                    }
                    bitmap?.let {
                        withContext(Dispatchers.Default) {
                            val palette = Palette.from(it).maximumColorCount(8).generate()
                            listOfNotNull(
                                palette.vibrantSwatch,
                                palette.lightVibrantSwatch,
                                palette.darkVibrantSwatch,
                                palette.mutedSwatch,
                                palette.lightMutedSwatch,
                                palette.darkMutedSwatch,
                            ).map { swatch -> Color(swatch.rgb) }.distinct()
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }

                if (colors.isNullOrEmpty()) {
                    Timber.w("Full-player glow artwork unavailable")
                    gradientColors = fallbackGradient
                } else {
                    gradientColorsCache.put(url, colors)
                    gradientColors = colors
                }
            }
            else -> Unit
        }
    }

    val TextBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            else -> PlayerColorExtractor.DefaultPalette.textPrimary
        },
        label = "TextBackgroundColor"
    )

    val icBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surface
            else -> Color.Black
        },
        label = "icBackgroundColor"
    )

    val (textButtonColor, iconButtonColor) = when {
        playerBackground != PlayerBackgroundStyle.DEFAULT -> {
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

    val download by LocalDownloadUtil.current.getDownload(mediaMetadata?.id ?: "")
        .collectAsState(initial = null)

    var showInlineLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    // Position update - only for local playback
    // When casting, we use castPosition directly to avoid sync issues
    // Use isPlaying instead of playbackState to ensure continuous updates during playback
    LaunchedEffect(isPlaying, isCasting, state.isExpanded) {
        if (!isCasting && isPlaying) {
            val updateInterval = if (state.isExpanded) 100L else 500L
            while (isActive) {
                delay(updateInterval) // 100ms for smooth expanded slider, 500ms when collapsed to save battery
                if (sliderPosition == null) { // Only update if user isn't dragging
                    position = playerConnection.player.currentPosition
                    duration = playerConnection.player.duration
                }
            }
        }
    }
    
    // Also update position when playback state changes (e.g., song change, seek)
    LaunchedEffect(playbackState, mediaMetadata?.id, isCasting) {
        if (!isCasting) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
        }
    }

    LaunchedEffect(isCasting, castPosition, castDuration) {
        if (isCasting && sliderPosition == null) {
            position = castPosition
            if (castDuration > 0) duration = castDuration
        }
    }

    val dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = state.expandedBound,
        collapsedBound = dismissedBound + 1.dp,
        initialAnchor = 1
    )

    LaunchedEffect(state.isCollapsed) {
        if (state.isCollapsed && queueSheetState.isExpanded) {
            queueSheetState.collapseSoft()
        }
    }

    val bottomSheetBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.IMMERSIVE_ARTWORK, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED ->
            PlayerColorExtractor.CharcoalDark
        PlayerBackgroundStyle.LIVE_MESH ->
            Color.Black
        else ->
            if (useBlackBackground) Color.Black
            else MaterialTheme.colorScheme.surfaceContainer
    }

    val backgroundLifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val animateBackground = interfaceAnimations &&
        backgroundLifecycle.isAtLeast(Lifecycle.State.RESUMED) &&
        android.animation.ValueAnimator.areAnimatorsEnabled()
    val backgroundAlpha = state.progress.coerceIn(0f, 1f)
    val immersiveArtworkBackground = playerBackground != PlayerBackgroundStyle.DEFAULT
    val immersiveForeground = if (immersiveArtworkBackground) PlayerColorExtractor.TextPrimary
        else MaterialTheme.colorScheme.onSurface

    BottomSheet(
        state = state,
        modifier = modifier,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomSheetBackgroundColor)
            ) {
                if (backgroundVisible && backgroundAlpha > 0.01f) {
                    when (playerBackground) {
                    PlayerBackgroundStyle.IMMERSIVE_ARTWORK -> {
                        ImmersiveArtworkBackground(
                            trackId = mediaMetadata?.id.orEmpty(),
                            thumbnail = mediaMetadata?.thumbnailUrl,
                            itemArtwork = currentItemArtwork,
                            animate = animateBackground,
                            dimForLyrics = showInlineLyrics,
                            modifier = Modifier.fillMaxSize().alpha(backgroundAlpha),
                        )
                    }
                    PlayerBackgroundStyle.GRADIENT -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                val duration = if (animateBackground) 800 else 0
                                fadeIn(tween(duration)).togetherWith(fadeOut(tween(duration)))
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
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                val duration = if (animateBackground) 1200 else 0
                                fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                            },
                            label = "GlowAnimatedContent"
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val glowProgress = if (animateBackground && colors != fallbackGradient) {
                                    rememberInfiniteTransition(label = "GlowAnimation").animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(20000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "glowProgress"
                                    )
                                } else null

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .drawBehind {
                                            val progress = glowProgress?.value ?: 0f
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

                                            drawRect(color = baseColor)
                                            drawRect(brush = brush1)
                                            drawRect(brush = brush2)
                                            drawRect(brush = brush3)
                                            drawRect(brush = brush4)
                                            drawRect(brush = brush5)
                                            drawRect(brush = brush6)
                                        }
                                )
                            }
                        }
                    }
                    PlayerBackgroundStyle.LIVE_MESH -> {
                        // A shared clock wraps after whole turns for all three layers.
                        val rotation = if (animateBackground && backgroundArtwork != null) {
                            rememberInfiniteTransition(label = "liveMeshRotation").animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(tween(240000, easing = LinearEasing)),
                                label = "meshCycle",
                            )
                        } else null
                        AnimatedContent(
                            targetState = backgroundArtwork,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                val duration = if (animateBackground) 1500 else 0
                                fadeIn(tween(duration)).togetherWith(fadeOut(tween(duration)))
                            },
                            label = "liveMeshBackground"
                        ) { artworkUrl ->
                            if (artworkUrl == null) return@AnimatedContent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(backgroundAlpha)
                            ) {
                                val matrix = remember {
                                    val m = ColorMatrix()
                                    m.setToSaturation(1.8f) // Reduced to avoid neon look
                                    m
                                }
                                val colorFilter = remember(matrix) { ColorFilter.colorMatrix(matrix) }
                                val painter = rememberPlayerBackgroundPainter(artworkUrl, 15, animateBackground)

                                // Keep the base still so the moving layers cannot expose an empty edge.
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Layer 2: Fast Rotating Crop (Top-Left)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter,
                                    alignment = Alignment.TopStart,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = 1.7f
                                            scaleY = 1.7f
                                            rotationZ = 6f * (rotation?.value ?: 0f)
                                            alpha = 0.6f
                                        }
                                )

                                // Layer 3: Slow Rotating Crop (Bottom-Right)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter,
                                    alignment = Alignment.BottomEnd,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = 1.7f
                                            scaleY = 1.7f
                                            rotationZ = 4f * (rotation?.value ?: 0f)
                                            alpha = 0.5f
                                        }
                                )

                                // Global dark tint to prevent neon look + vertical gradient for depth
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.55f))
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
                    PlayerBackgroundStyle.DEFAULT -> {
                        // Nothing
                    }
                }
            }
        }
        },
        onDismiss = playerConnection.service::dismissPlayback,
        collapsedContent = {
            MiniPlayer(
                positionState = positionState,
                durationState = durationState,
                visible = state.isCollapsed,
            )
        },
    ) {
        val transportState = if (isCasting) Player.STATE_READY else playbackState
        val isLoading = if (isCasting) castIsBuffering else playbackState == Player.STATE_BUFFERING
        val effectiveDuration = if (isCasting) castDuration else duration
        val finishSeek: () -> Unit = {
            sliderPosition?.let {
                if (isCasting) {
                    castHandler?.seekTo(it)
                } else {
                    playerConnection.seekTo(it)
                }
                position = it
            }
            sliderPosition = null
        }

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
                        .background(Color.Black.copy(alpha = 0.40f))
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
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
                                    tint = Color.White
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
                                    color = Color.White
                                )
                                Text(
                                    text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val metadata = mediaMetadata ?: return@IconButton
                                    menuState.show {
                                        LyricsMenu(
                                            lyricsProvider = { currentLyrics },
                                            songProvider = { currentSong?.song },
                                            mediaMetadataProvider = { metadata },
                                            onDismiss = menuState::dismiss,
                                            onShowOffsetDialog = {
                                                bottomSheetPageState.show {
                                                    ShowOffsetDialog(
                                                        songProvider = { currentSong?.song }
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = stringResource(R.string.more_label),
                                    tint = Color.White
                                )
                            }
                        }

                        InlineLyricsView(
                            mediaMetadata = mediaMetadata,
                            showLyrics = true,
                            positionProvider = { effectivePosition },
                            contentPadding = PaddingValues(bottom = dismissedBound)
                        )
                    }
                }
            } else {
                mediaMetadata?.let { metadata ->
                    val isCurrentTrackLiked = if (currentSong?.id == metadata.id) {
                        currentSong?.song?.liked == true
                    } else {
                        metadata.liked
                    }
                    val cookieAngle = rememberPlaybackCookieAngle(
                        playerConnection,
                        visible = state.isExpanded && !queueSheetState.isExpanded && !showInlineLyrics,
                    )
                    CompositionLocalProvider(LocalPlaybackCookieAngle provides cookieAngle) {
                        Crossfade(
                            targetState = playerStyle,
                            animationSpec = if (interfaceAnimations) tween(durationMillis = 240) else snap(),
                            label = "PlayerStyleCrossfade"
                        ) { currentStyle ->
                            when (currentStyle) {
                            PlayerStyle.MINIMAL -> MinimalPlayer(
                                mediaMetadata = metadata,
                                itemArtwork = currentItemArtwork,
                                animateArtwork = animateBackground,
                                playbackState = transportState,
                                isPlaying = effectiveIsPlaying,
                                isLoading = isLoading,
                                canSkipPrevious = canSkipPrevious,
                                canSkipNext = canSkipNext,
                                sliderPosition = sliderPosition,
                                position = effectivePosition,
                                duration = effectiveDuration,
                                isLiked = isCurrentTrackLiked,
                                playerConnection = playerConnection,
                                navController = navController,
                                state = state,
                                textButtonColor = textButtonColor,
                                iconButtonColor = iconButtonColor,
                                textBackgroundColor = TextBackgroundColor,
                                playerBackground = playerBackground,
                                useDarkTheme = useDarkTheme,
                                onSliderValueChange = { sliderPosition = it },
                                onSliderValueChangeFinished = finishSeek,
                                onShowMenu = { onOpenMenu(metadata) },
                                modifier = Modifier.fillMaxSize()
                            )
                            PlayerStyle.CINEMATIC -> CinematicPlayer(
                                mediaMetadata = metadata,
                                itemArtwork = currentItemArtwork,
                                animateArtwork = animateBackground,
                                playbackState = transportState,
                                isPlaying = effectiveIsPlaying,
                                isLoading = isLoading,
                                canSkipPrevious = canSkipPrevious,
                                canSkipNext = canSkipNext,
                                sliderPosition = sliderPosition,
                                position = effectivePosition,
                                duration = effectiveDuration,
                                isLiked = isCurrentTrackLiked,
                                playerConnection = playerConnection,
                                navController = navController,
                                state = state,
                                textButtonColor = textButtonColor,
                                iconButtonColor = iconButtonColor,
                                textBackgroundColor = TextBackgroundColor,
                                playerBackground = playerBackground,
                                useDarkTheme = useDarkTheme,
                                onSliderValueChange = { sliderPosition = it },
                                onSliderValueChangeFinished = finishSeek,
                                onShowMenu = { onOpenMenu(metadata) },
                                modifier = Modifier.fillMaxSize()
                            )
                            PlayerStyle.IMMERSIVE -> ImmersivePlayer(
                                mediaMetadata = metadata,
                                itemArtwork = currentItemArtwork,
                                animateArtwork = animateBackground,
                                foreground = immersiveForeground,
                                artworkIsBackdrop = playerBackground == PlayerBackgroundStyle.IMMERSIVE_ARTWORK,
                                primaryButtonColor = textButtonColor,
                                playbackState = transportState,
                                isPlaying = effectiveIsPlaying,
                                isLoading = isLoading,
                                canSkipPrevious = canSkipPrevious,
                                canSkipNext = canSkipNext,
                                sliderPosition = sliderPosition,
                                position = effectivePosition,
                                duration = effectiveDuration,
                                isLiked = isCurrentTrackLiked,
                                currentFormat = currentFormat,
                                playerConnection = playerConnection,
                                navController = navController,
                                state = state,
                                onSliderValueChange = { sliderPosition = it },
                                onSliderValueChangeFinished = finishSeek,
                                onShowMenu = { onOpenMenu(metadata) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    }
                    AnimatedVisibility(
                        visible = playbackError != null && !lyricsVisible && state.isExpanded,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    ) {
                        playbackError?.let { err ->
                            PlaybackError(
                                error = err,
                                retry = playerConnection.player::prepare
                            )
                        }
                    }
            }
        }
    }
        Queue(
            state = queueSheetState,
            playerBottomSheetState = state,
            navController = navController,
            background =
            if (useBlackBackground) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.surface
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

@Composable
fun InlineLyricsView(
    mediaMetadata: MediaMetadata?,
    showLyrics: Boolean,
    positionProvider: () -> Long,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LyricsV2(
        mediaMetadata = mediaMetadata,
        showLyrics = showLyrics,
        positionProvider = positionProvider,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    )
}
