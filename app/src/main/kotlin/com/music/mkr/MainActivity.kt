/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.music.mkr.mkrmusic.updater.ReleasePreviewState
import com.music.mkr.constants.LastSeenStarPromptVersionKey
import com.music.mkr.constants.HasStarredRepoKey
import com.music.mkr.constants.HasShownSupportPromptKey
import com.music.mkr.ui.component.ActionPromptDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.ui.component.snackbar.LocalSnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.mkr.constants.AppBarHeight
import com.music.mkr.constants.AppLanguageKey
import com.music.mkr.constants.DarkModeKey
import com.music.mkr.constants.DefaultOpenTabKey
import com.music.mkr.constants.DisableScreenshotKey
import com.music.mkr.constants.DynamicThemeKey
import com.music.mkr.constants.EnableHighRefreshRateKey
import com.music.mkr.constants.ListenTogetherInTopBarKey
import com.music.mkr.constants.ListenTogetherUsernameKey
import com.music.mkr.constants.MiniPlayerBottomSpacing
import com.music.mkr.constants.MiniPlayerHeight
import com.music.mkr.constants.NavigationBarAnimationSpec
import com.music.mkr.constants.NavigationBarHeight
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.isNewerVersion
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState
import com.music.mkr.mkrmusic.updater.getUpdateNotificationsSetting
import com.music.mkr.mkrmusic.updater.getBetaUpdatesSetting
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.shouldRunNightlyCheck
import com.music.mkr.mkrmusic.updater.markNightlyCheckDone
import com.music.mkr.mkrmusic.UpdateNotificationHelper
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.music.mkr.constants.PauseListenHistoryKey
import com.music.mkr.constants.PauseSearchHistoryKey
import com.music.mkr.constants.PureBlackKey
import com.music.mkr.constants.SYSTEM_DEFAULT
import com.music.mkr.constants.SelectedThemeColorKey
import com.music.mkr.constants.SlimNavBarHeight
import com.music.mkr.constants.SlimNavBarKey
import com.music.mkr.constants.FloatingNavBarKey
import com.music.mkr.constants.NavigationStyle
import com.music.mkr.constants.NavigationStyleKey
import com.music.mkr.constants.StopMusicOnTaskClearKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.extensions.toEnum
import com.music.mkr.models.MediaMetadata
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.DownloadUtil
import com.music.mkr.playback.MusicService
import com.music.mkr.playback.MusicService.MusicBinder
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.AppNavigationBar
import com.music.mkr.ui.component.AppNavigationRail
import com.music.mkr.ui.component.BottomSheetMenu
import com.music.mkr.ui.component.BottomSheetPage
import com.music.mkr.ui.component.ExpressiveIconButton
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.rememberBottomSheetState
import com.music.mkr.ui.component.shimmer.ShimmerTheme
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.ui.player.BottomSheetPlayer
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.screens.navigationBuilder
import com.music.mkr.ui.screens.settings.DarkMode
import com.music.mkr.ui.screens.settings.NavigationTab
import com.music.mkr.ui.theme.ColorSaver
import com.music.mkr.ui.theme.DefaultThemeColor
import com.music.mkr.ui.theme.mkrMusicTheme
import com.music.mkr.ui.theme.extractThemeColor
import com.music.mkr.ui.utils.appBarScrollBehavior
import com.music.mkr.ui.utils.resetHeightOffset
import com.music.mkr.utils.SyncUtils
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import com.music.mkr.utils.reportException
import com.music.mkr.utils.setAppLocale
import com.music.mkr.viewmodels.HistoryViewModel
import com.music.mkr.viewmodels.HomeViewModel
import com.music.mkr.ui.screens.search.suggestions.SuggestionsViewModel
import com.valentinilk.shimmer.LocalShimmerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import com.music.mkr.github.GitHubViewModel
import javax.inject.Inject
import androidx.activity.viewModels
import androidx.compose.material3.LocalContentColor

@Suppress("DEPRECATION", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val ACTION_SEARCH = "com.music.mkr.action.SEARCH"
        private const val ACTION_LIBRARY = "com.music.mkr.action.LIBRARY"
    }

    @Inject
    lateinit var database: MusicDatabase

    @Inject
    lateinit var downloadUtil: DownloadUtil

    @Inject
    lateinit var syncUtils: SyncUtils

    @Inject
    lateinit var listenTogetherManager: com.music.mkr.listentogether.ListenTogetherManager

    private val gitHubViewModel: GitHubViewModel by viewModels()

    private lateinit var navController: NavHostController
    private var pendingIntent: Intent? = null

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                try {
                    playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
                    Timber.tag("MainActivity").d("PlayerConnection created successfully")
                    // Connect Listen Together manager to player
                    listenTogetherManager.setPlayerConnection(playerConnection)
                } catch (e: Exception) {
                    Timber.tag("MainActivity").e(e, "Failed to create PlayerConnection")
                    // Retry after a delay of 500ms
                    lifecycleScope.launch {
                        delay(500)
                        try {
                            playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
                            listenTogetherManager.setPlayerConnection(playerConnection)
                        } catch (e2: Exception) {
                            Timber.tag("MainActivity").e(e2, "Failed to create PlayerConnection on retry")
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Disconnect Listen Together manager
            listenTogetherManager.setPlayerConnection(null)
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onStart() {
        super.onStart()
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1000)
            }
        }

        // On Android 12+, we can't start foreground services from background
        // Use BIND_AUTO_CREATE which will create the service if needed
        // The service will call startForeground() in onCreate() when bound
        isServiceBound = bindService(
            Intent(this, MusicService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                // Ignore
            }
            isServiceBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dataStore.get(StopMusicOnTaskClearKey, false) && isFinishing) {
            try {
                playerConnection?.service?.let { s ->
                    s.player.pause()
                    s.player.stop()
                    s.stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE)
                }
                stopService(Intent(this, MusicService::class.java))
                if (isServiceBound) {
                    try {
                        unbindService(serviceConnection)
                    } catch (e: Exception) {
                        // Ignore
                    }
                    isServiceBound = false
                }
                playerConnection = null
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::navController.isInitialized) {
            handleDeepLinkIntent(intent, navController)
        } else {
            pendingIntent = intent
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize Listen Together manager
        listenTogetherManager.initialize()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val locale = dataStore[AppLanguageKey]
                ?.takeUnless { it == SYSTEM_DEFAULT }
                ?.let { Locale.forLanguageTag(it) }
                ?: Locale.getDefault()
            setAppLocale(this, locale)
        }

        lifecycleScope.launch {
            dataStore.data
                .map { it[DisableScreenshotKey] ?: false }
                .distinctUntilChanged()
                .collectLatest {
                    if (it) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE,
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        setContent {
            mkrMusicApp(
                playerConnection = playerConnection,
                database = database,
                downloadUtil = downloadUtil,
                syncUtils = syncUtils,
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun mkrMusicApp(
        playerConnection: PlayerConnection?,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
        syncUtils: SyncUtils,
    ) {
        val enableDynamicTheme by rememberPreference(DynamicThemeKey, defaultValue = true)
        val enableHighRefreshRate by rememberPreference(EnableHighRefreshRateKey, defaultValue = true)
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            SnackbarManager.events.collectLatest { event ->
                snackbarHostState.currentSnackbarData?.dismiss()
                val messageStr = context.getString(event.messageResource)
                val actionLabelStr = event.actionLabel?.let { context.getString(it) }
                val result = snackbarHostState.showSnackbar(
                    message = messageStr,
                    actionLabel = actionLabelStr,
                    duration = event.duration
                )
                if (result == SnackbarResult.ActionPerformed) {
                    event.onAction?.invoke()
                }
            }
        }

        LaunchedEffect(Unit) {
            if (getAutoUpdateCheckSetting(context)) {
                val betaEnabled = getBetaUpdatesSetting(context)

                // Beta/nightly path: only run once per day at 9 PM or later.
                // The run-number comparison ("is there a new commit?") is already
                // handled inside checkForUpdate — we just gate WHEN it is allowed
                // to execute so it doesn't fire on every single app launch.
                if (betaEnabled && !shouldRunNightlyCheck(context)) {
                    Log.d("UpdateCheck", "Beta check skipped: outside 9 PM window or already ran today")
                    return@LaunchedEffect
                }

                // Delay to not block app startup
                delay(2000L)
                checkForUpdate(
                    context = context,
                    onSuccess = { latestVersion, isAvailable, _, _, _, _, _, _ ->
                        if (!getAutoUpdateCheckSetting(context)) {
                            Log.d("UpdateCheck", "Auto update check disabled while in-flight; skipping startup update notification")
                            return@checkForUpdate
                        }
                        val currentVersion = BuildConfig.VERSION_NAME
                        Log.d("UpdateCheck", "Startup check success. Latest: $latestVersion, Current: $currentVersion, isAvailable: $isAvailable")
                        saveUpdateAvailableState(context, isAvailable)

                        if (isAvailable && getUpdateNotificationsSetting(context)) {
                            Log.d("UpdateCheck", "Posting update notification for $latestVersion")
                            UpdateNotificationHelper.showUpdateNotification(context, latestVersion)
                        }

                        // Stamp today so no more nightly checks until tomorrow 9 PM
                        if (betaEnabled) markNightlyCheckDone(context)
                    },
                    onError = {
                        Log.e("UpdateCheck", "Startup check failed")
                        // Do not clear the state on error, in case of offline launch
                        // Note: we do NOT stamp markNightlyCheckDone on error so it
                        // can retry later the same night if the user reopens the app.
                    }
                )
            }
        }

        LaunchedEffect(enableHighRefreshRate) {
            val window = this@MainActivity.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val layoutParams = window.attributes
                if (enableHighRefreshRate) {
                    layoutParams.preferredDisplayModeId = 0
                } else {
                    val modes = window.windowManager.defaultDisplay.supportedModes
                    val mode60 = modes.firstOrNull { kotlin.math.abs(it.refreshRate - 60f) < 1f }
                        ?: modes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }

                    if (mode60 != null) {
                        layoutParams.preferredDisplayModeId = mode60.modeId
                    }
                }
                window.attributes = layoutParams
            } else {
                val params = window.attributes
                if (enableHighRefreshRate) {
                    params.preferredRefreshRate = 0f
                } else {
                    params.preferredRefreshRate = 60f
                }
                window.attributes = params
            }
        }

        val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)

        val (hasShownSupportPrompt, setHasShownSupportPrompt) = rememberPreference(HasShownSupportPromptKey, defaultValue = false)
        val (hasStarredRepo) = rememberPreference(HasStarredRepoKey, false)
        val uriHandler = LocalUriHandler.current
        val playbackEventCount by database.eventCount().collectAsState(initial = 0)
        val totalListenTime by database.totalListenTime().collectAsState(initial = 0L)
        val reachedSupportMilestone = (playbackEventCount >= 10 || totalListenTime >= 36_000_000L)


        val isSystemInDarkTheme = isSystemInDarkTheme()
        val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
            if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        }

        LaunchedEffect(useDarkTheme) {
            setSystemBarAppearance(useDarkTheme)
        }

        val pureBlackEnabled by rememberPreference(PureBlackKey, defaultValue = false)
        val pureBlack = remember(pureBlackEnabled, useDarkTheme) {
            pureBlackEnabled && useDarkTheme
        }

        val (selectedThemeColorInt) = rememberPreference(SelectedThemeColorKey, defaultValue = DefaultThemeColor.toArgb())
        val selectedThemeColor = Color(selectedThemeColorInt)

        var themeColor by rememberSaveable(stateSaver = ColorSaver) {
            mutableStateOf(selectedThemeColor)
        }

        LaunchedEffect(selectedThemeColor) {
            if (!enableDynamicTheme) {
                themeColor = selectedThemeColor
            }
        }

        LaunchedEffect(playerConnection, enableDynamicTheme, selectedThemeColor) {
            val playerConnection = playerConnection
            if (!enableDynamicTheme || playerConnection == null) {
                themeColor = selectedThemeColor
                return@LaunchedEffect
            }

            playerConnection.service.currentMediaMetadata.collectLatest { song ->
                if (song?.thumbnailUrl != null) {
                    withContext(Dispatchers.IO) {
                        try {
                            val result = imageLoader.execute(
                                ImageRequest.Builder(this@MainActivity)
                                    .data(song.thumbnailUrl)
                                    .allowHardware(false)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .networkCachePolicy(CachePolicy.ENABLED)
                                    .crossfade(false)
                                    .build()
                            )
                            themeColor = result.image?.toBitmap()?.extractThemeColor() ?: selectedThemeColor
                        } catch (e: Exception) {
                            // Fallback to default on error
                            themeColor = selectedThemeColor
                        }
                    }
                } else {
                    themeColor = selectedThemeColor
                }
            }
        }

        val gitHubViewModel: GitHubViewModel = hiltViewModel()
        val isStarred by gitHubViewModel.isStarred.collectAsState()
        val showThankYouDialog by gitHubViewModel.showThankYouDialog.collectAsState()

        LaunchedEffect(Unit) {
            gitHubViewModel.checkStarStatus(this@MainActivity)
        }

        mkrMusicTheme(
            darkTheme = useDarkTheme,
            pureBlack = pureBlack,
            themeColor = themeColor,
        ) {
            if (!hasShownSupportPrompt && !hasStarredRepo && !isStarred && reachedSupportMilestone) {
                ActionPromptDialog(
                    titleBar = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.star),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Support MKR Music",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    onDismiss = { setHasShownSupportPrompt(true) },
                    onConfirm = {
                        setHasShownSupportPrompt(true)
                        uriHandler.openUri(com.music.mkr.github.GitHubConfig.REPO_URL)
                    },
                    onCancel = { setHasShownSupportPrompt(true) },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                text = "Enjoying your music experience?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "MKR Music is 100% free, ad-free, and open-source. Starring on GitHub or supporting development helps keep this project alive and growing!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        setHasShownSupportPrompt(true)
                                        uriHandler.openUri(com.music.mkr.github.GitHubConfig.REPO_URL)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.star),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Star Repo")
                                }
                                OutlinedButton(
                                    onClick = {
                                        setHasShownSupportPrompt(true)
                                        uriHandler.openUri(com.music.mkr.github.GitHubConfig.DEV_BUYMEACOFFEE_URL)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.buymeacoffee),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Coffee")
                                }
                            }
                        }
                    }
                )
            }

            if (showThankYouDialog) {
                ActionPromptDialog(
                    title = "Thank You!",
                    onDismiss = { gitHubViewModel.dismissThankYouDialog() },
                    onConfirm = { gitHubViewModel.dismissThankYouDialog() },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Thank you so much for your support! \u2764\uFE0F",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Starring the repository helps us grow and keep the project alive. Enjoy the music!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val btnModifier = Modifier.size(44.dp)
                                val btnColors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                                
                                IconButton(
                                    onClick = { uriHandler.openUri(com.music.mkr.github.GitHubConfig.DEV_BUYMEACOFFEE_URL) },
                                    modifier = btnModifier,
                                    colors = btnColors
                                ) {
                                    Icon(painter = painterResource(R.drawable.buymeacoffee), contentDescription = "Buy Me A Coffee", tint = iconTint)
                                }
                            }
                        }
                    }
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface)
            ) {
                val focusManager = LocalFocusManager.current
                val density = LocalDensity.current
                val configuration = LocalWindowInfo.current
                val cutoutInsets = WindowInsets.displayCutout
                val windowsInsets = WindowInsets.systemBars
                val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }
                val bottomInsetDp = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = hiltViewModel()
                // Pre-warm HistoryViewModel at Activity scope so history data loads
                // in background immediately — zero lag when user taps the history icon
                hiltViewModel<HistoryViewModel>()
                // Pre-warm SuggestionsViewModel at Activity scope so suggestions data loads
                // in background immediately — zero lag when user opens the Search screen
                hiltViewModel<SuggestionsViewModel>()
                val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()
                val isLoggedIn by homeViewModel.isLoggedIn.collectAsState()
                val updateState by homeViewModel.updateState.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val (previousTab, setPreviousTab) = rememberSaveable { mutableStateOf("home") }

                val (listenTogetherInTopBar) = rememberPreference(ListenTogetherInTopBarKey, defaultValue = true)
                val navigationItems = remember { Screens.MainScreens }
                val (slimNav) = rememberPreference(SlimNavBarKey, defaultValue = false)
                val (floatingNav) = rememberPreference(FloatingNavBarKey, defaultValue = true)
                val (navigationStyle) = rememberEnumPreference(NavigationStyleKey, defaultValue = NavigationStyle.DEFAULT)
                val (useNewMiniPlayerDesign) = rememberPreference(UseNewMiniPlayerDesignKey, defaultValue = true)
                val (useAppleMiniPlayer) = rememberPreference(UseAppleMiniPlayerKey, defaultValue = false)
                val defaultOpenTab = remember {
                    dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME)
                }
                val tabOpenedFromShortcut = remember {
                    when (intent?.action) {
                        ACTION_SEARCH -> NavigationTab.SEARCH
                        ACTION_LIBRARY -> NavigationTab.LIBRARY
                        else -> null
                    }
                }

                val topLevelScreens = remember {
                    listOf(
                        Screens.Home.route,
                        Screens.Search.route,
                        "search",
                        Screens.Library.route,
                        Screens.Account.route,
                        Screens.ListenTogether.route,
                        "settings",
                    )
                }

                val (query, onQueryChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) {
                    mutableStateOf(TextFieldValue())
                }

                val onSearch: (String) -> Unit = remember {
                    { searchQuery ->
                        if (searchQuery.isNotEmpty()) {
                            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")

                            if (dataStore[PauseSearchHistoryKey] != true) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    database.query {
                                        insert(SearchHistory(query = searchQuery))
                                    }
                                }
                            }
                        }
                    }
                }

                // Use derivedStateOf to avoid unnecessary recompositions
                val currentRoute by remember {
                    derivedStateOf { navBackStackEntry?.destination?.route }
                }

                val inSearchResultScreen by remember {
                    derivedStateOf { currentRoute?.startsWith("search/") == true }
                }

                val inSearchScreen by remember {
                    derivedStateOf { currentRoute?.startsWith("search/") == true || currentRoute == Screens.Search.route || currentRoute == "search" }
                }
                val navigationItemRoutes = remember(navigationItems) {
                    navigationItems.map { it.route }.toSet()
                }

                val shouldShowNavigationBar = remember(currentRoute, navigationItemRoutes) {
                    currentRoute == null ||
                        navigationItemRoutes.contains(currentRoute) ||
                        currentRoute == "search" ||
                        currentRoute?.startsWith("search/") == true
                }

                val isLandscape = configuration.containerDpSize.width > configuration.containerDpSize.height

                val showRail = isLandscape && !inSearchResultScreen

                val navPadding = if (shouldShowNavigationBar && !showRail) {
                    if (slimNav) SlimNavBarHeight else NavigationBarHeight
                } else {
                    0.dp
                }

                val navigationBarHeight by animateDpAsState(
                    targetValue = if (shouldShowNavigationBar && !showRail) NavigationBarHeight else 0.dp,
                    animationSpec = NavigationBarAnimationSpec,
                    label = "navBarHeight",
                )

                val isSnackbarShowing = snackbarHostState.currentSnackbarData != null
                val extraSnackbarHeight by animateDpAsState(
                    targetValue = if (isSnackbarShowing) 64.dp else 0.dp,
                    label = "snackbarHeight"
                )

                val playerBottomSheetState = rememberBottomSheetState(
                    dismissedBound = 0.dp,
                    collapsedBound = bottomInset +
                        (if (!showRail && shouldShowNavigationBar) navPadding else 0.dp) +
                        extraSnackbarHeight +
                        (if (useNewMiniPlayerDesign || useAppleMiniPlayer) {
                            if (navigationStyle == NavigationStyle.CLEAN_MATERIAL) 0.dp else MiniPlayerBottomSpacing
                        } else 0.dp) +
                        MiniPlayerHeight,
                    expandedBound = maxHeight,
                )

                val playerAwareWindowInsets = remember(
                    bottomInset,
                    shouldShowNavigationBar,
                    playerBottomSheetState.isDismissed,
                    showRail,
                    extraSnackbarHeight
                ) {
                    var bottom = bottomInset
                    if (shouldShowNavigationBar && !showRail) {
                        bottom += NavigationBarHeight
                    }
                    bottom += extraSnackbarHeight
                    if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                    windowsInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .add(WindowInsets(top = AppBarHeight, bottom = bottom))
                }
                appBarScrollBehavior(
                    canScroll = {
                        !inSearchScreen &&
                            (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                    }
                )

                val topAppBarScrollBehavior = appBarScrollBehavior(
                    canScroll = {
                        !inSearchScreen &&
                            (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                    },
                )

                // Navigation tracking
                LaunchedEffect(navBackStackEntry) {
                    if (inSearchResultScreen) {
                        val searchQuery = withContext(Dispatchers.IO) {
                            val rawQuery = navBackStackEntry?.arguments?.getString("query").orEmpty()
                            try {
                                URLDecoder.decode(rawQuery, "UTF-8")
                            } catch (e: Exception) {
                                rawQuery
                            }
                        }
                        onQueryChange(
                            TextFieldValue(
                                searchQuery,
                                TextRange(searchQuery.length)
                            )
                        )
                    } else if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } || navBackStackEntry?.destination?.route == "search") {
                        onQueryChange(TextFieldValue())
                    }

                    // Reset scroll behavior for main navigation items
                    if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                        if (navigationItems.fastAny { it.route == previousTab }) {
                            topAppBarScrollBehavior.state.resetHeightOffset()
                        }
                    }

                    topAppBarScrollBehavior.state.resetHeightOffset()

                    // Track previous tab for animations
                    navController.currentBackStackEntry?.destination?.route?.let {
                        setPreviousTab(it)
                    }
                }

                LaunchedEffect(playerConnection) {
                    val player = playerConnection?.player ?: return@LaunchedEffect
                    if (player.currentMediaItem == null) {
                        if (!playerBottomSheetState.isDismissed) {
                            playerBottomSheetState.dismiss()
                        }
                    } else {
                        if (playerBottomSheetState.isDismissed) {
                            playerBottomSheetState.collapseSoft()
                        }
                    }
                }

                DisposableEffect(playerConnection, playerBottomSheetState) {
                    val player = playerConnection?.player ?: return@DisposableEffect onDispose { }
                    val listener = object : Player.Listener {
                        override fun onMediaItemTransition(
                            mediaItem: MediaItem?,
                            reason: Int,
                        ) {
                            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED &&
                                mediaItem != null &&
                                playerBottomSheetState.isDismissed
                            ) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }
                    player.addListener(listener)
                    onDispose {
                        player.removeListener(listener)
                    }
                }

                var shouldShowTopBar by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(navBackStackEntry, listenTogetherInTopBar) {
                    val currentRoute = navBackStackEntry?.destination?.route
                    val isListenTogetherScreen = currentRoute == Screens.ListenTogether.route || 
                        currentRoute == "listen_together_from_topbar"
                    shouldShowTopBar = currentRoute in topLevelScreens &&
                        currentRoute != "settings" &&
                        currentRoute != Screens.Search.route &&
                        currentRoute != "search" &&
                        !(isListenTogetherScreen && listenTogetherInTopBar)
                }

                val sharedPrefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                val isUpdateAvailable = remember {
                    mutableStateOf(if (getAutoUpdateCheckSetting(context)) getUpdateAvailableState(context) else false)
                }
                
                val updateListener = remember {
                    SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                        if (key == "update_available" || key == "auto_update_check") {
                            isUpdateAvailable.value = if (getAutoUpdateCheckSetting(context)) getUpdateAvailableState(context) else false
                        }
                    }
                }
                
                DisposableEffect(sharedPrefs, updateListener) {
                    sharedPrefs.registerOnSharedPreferenceChangeListener(updateListener)
                    onDispose {
                        sharedPrefs.unregisterOnSharedPreferenceChangeListener(updateListener)
                    }
                }
                // Snackbar is now triggered in checkForUpdate directly
                val coroutineScope = rememberCoroutineScope()
                var sharedSong: SongItem? by remember {
                    mutableStateOf(null)
                }

                LaunchedEffect(Unit) {
                    if (pendingIntent != null) {
                        handleDeepLinkIntent(pendingIntent!!, navController)
                        pendingIntent = null
                    } else {
                        handleDeepLinkIntent(intent, navController)
                    }
                }

                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> { intent ->
                        handleDeepLinkIntent(intent, navController)
                    }

                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                val currentTitleRes = remember(navBackStackEntry) {
                    when (navBackStackEntry?.destination?.route) {
                        Screens.Home.route -> R.string.app_name
                        Screens.Search.route, "search" -> R.string.search
                        Screens.Library.route -> R.string.filter_library
                        Screens.ListenTogether.route -> R.string.together
                        else -> null
                    }
                }



                val pauseListenHistory by rememberPreference(PauseListenHistoryKey, defaultValue = false)
                val eventCount by database.eventCount().collectAsState(initial = 0)
                val showHistoryButton = true

                val baseBg = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer

                CompositionLocalProvider(
                    LocalDatabase provides database,
                    LocalContentColor provides if (pureBlack) Color.White else contentColorFor(MaterialTheme.colorScheme.surface),
                    LocalPlayerConnection provides playerConnection,
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                    LocalDownloadUtil provides downloadUtil,
                    LocalShimmerTheme provides ShimmerTheme,
                    LocalSyncUtils provides syncUtils,
                    LocalListenTogetherManager provides listenTogetherManager,
                    LocalSnackbarHostState provides snackbarHostState,
                ) {
                    Scaffold(
                        containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface,
                        topBar = {
                            AnimatedVisibility(
                                visible = shouldShowTopBar,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 200))
                            ) {
                                var logoClickCount by remember { mutableIntStateOf(0) }
                                var lastClickTime by remember { mutableLongStateOf(0L) }
                                val logoScope = rememberCoroutineScope()
                                var resetJob by remember { mutableStateOf<Job?>(null) }
                                var currentToast by remember { mutableStateOf<Toast?>(null) }

                                val isHome = navBackStackEntry?.destination?.route == Screens.Home.route
                                val isLibrary = navBackStackEntry?.destination?.route == Screens.Library.route
                                val isTopBarImmersive = isHome || isLibrary

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (isTopBarImmersive) {
                                        val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(AppBarHeight + statusBarHeight + 12.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            (if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface).copy(alpha = 0.88f),
                                                            (if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface).copy(alpha = 0.65f),
                                                            (if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface).copy(alpha = 0.30f),
                                                            Color.Transparent,
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    TopAppBar(
                                        title = {
                                            if (isHome) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    ExpressiveIconButton(
                                                        onClick = {
                                                            val currentTime = System.currentTimeMillis()
                                                            // Debounce rapid bursts (< 180ms)
                                                            if (currentTime - lastClickTime < 180L) {
                                                                return@ExpressiveIconButton
                                                            }
                                                            if (currentTime - lastClickTime > 2500L) {
                                                                logoClickCount = 1
                                                            } else {
                                                                logoClickCount++
                                                            }
                                                            lastClickTime = currentTime

                                                            // Cancel previous inactivity timer
                                                            resetJob?.cancel()

                                                            // Easter egg triggers at 5 or more deliberate clicks
                                                            if (logoClickCount >= 5) {
                                                                currentToast?.cancel()
                                                                val toastMessage = when (logoClickCount) {
                                                                    5 -> "Hey, what are you doing?"
                                                                    6 -> "Stop poking the MKR logo bro!"
                                                                    7 -> "One more click and something wild might happen..."
                                                                    8 -> "Bro please don't do it bro! What are you looking for?! There's no secret menu here!"
                                                                    9 -> "Bro really has too much free time today!"
                                                                    10 -> "MKR Music — MKR stands for Mohammad Kaif Raja"
                                                                    else -> {
                                                                        val funnyQuotes = listOf(
                                                                            "Go listen to some bangers instead bro!",
                                                                            "MKR Music loves you, but please spare the logo!",
                                                                            "Still clicking?! You must really love this note!",
                                                                            "Achievement unlocked: Professional Logo Tapper"
                                                                        )
                                                                        funnyQuotes[(logoClickCount - 11) % funnyQuotes.size]
                                                                    }
                                                                }

                                                                currentToast = Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT)
                                                                currentToast?.show()
                                                            }

                                                            // Stop toast and reset clicks if user is not clicking for 2.5 seconds
                                                            resetJob = logoScope.launch {
                                                                delay(2500L)
                                                                currentToast?.cancel()
                                                                currentToast = null
                                                                logoClickCount = 0
                                                            }
                                                        },
                                                        size = 38.dp,
                                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                                    ) {
                                                        Image(
                                                            painter = painterResource(R.drawable.icon),
                                                            contentDescription = stringResource(R.string.app_name),
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(5.dp)
                                                                .clip(RoundedCornerShape(8.dp)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }

                                                    Text(
                                                        text = stringResource(R.string.app_name),
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 20.sp,
                                                            letterSpacing = (-0.5).sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            } else if (isLibrary) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    ExpressiveIconButton(
                                                        onClick = { },
                                                        size = 38.dp,
                                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.library_music),
                                                            contentDescription = stringResource(R.string.filter_library),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    Text(
                                                        text = stringResource(R.string.filter_library),
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 20.sp,
                                                            letterSpacing = (-0.5).sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            } else {
                                                val titleText = currentTitleRes?.let { stringResource(it) } ?: ""
                                                if (titleText.isNotEmpty()) {
                                                    Text(
                                                        text = titleText,
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 20.sp,
                                                            letterSpacing = (-0.5).sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        },
                                        actions = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(end = 12.dp)
                                            ) {
                                                // Listening History navigation action (expressive squircle capsule)
                                                if (showHistoryButton) {
                                                    val historyDesc = stringResource(R.string.listening_history)
                                                    ExpressiveIconButton(
                                                        onClick = { navController.navigate("history") }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.history),
                                                            contentDescription = historyDesc,
                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                // Account-aware button reactive to auth state (hidden when not authenticated)
                                                if (isLoggedIn) {
                                                    val accountDesc = stringResource(R.string.account)
                                                    ExpressiveIconButton(
                                                        onClick = { navController.navigate("settings/account") }
                                                    ) {
                                                        if (accountImageUrl != null) {
                                                            AsyncImage(
                                                                model = accountImageUrl,
                                                                contentDescription = accountDesc,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .size(26.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                            )
                                                        } else {
                                                            Icon(
                                                                painter = painterResource(R.drawable.account),
                                                                contentDescription = accountDesc,
                                                                tint = MaterialTheme.colorScheme.onSurface,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                // Settings navigation action (expressive squircle capsule)
                                                val settingsDesc = stringResource(R.string.settings)
                                                ExpressiveIconButton(
                                                    onClick = { navController.navigate("settings") }
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.settings),
                                                        contentDescription = settingsDesc,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        },
                                        scrollBehavior = topAppBarScrollBehavior,
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = if (isTopBarImmersive) Color.Transparent else if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface,
                                            scrolledContainerColor = if (isTopBarImmersive) Color.Transparent else if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.windowInsetsPadding(
                                            if (showRail) {
                                                WindowInsets(left = NavigationBarHeight)
                                                    .add(cutoutInsets.only(WindowInsetsSides.Start))
                                            } else {
                                                cutoutInsets.only(WindowInsetsSides.Start + WindowInsetsSides.End)
                                            }
                                        )
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            val onNavItemClick: (Screens, Boolean) -> Unit = remember(navController, coroutineScope, topAppBarScrollBehavior, playerBottomSheetState) {
                                { screen: Screens, isSelected: Boolean ->
                                    if (playerBottomSheetState.isExpanded) {
                                        playerBottomSheetState.collapseSoft()
                                    }

                                    if (isSelected) {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("scrollToTop", true)
                                        coroutineScope.launch {
                                            topAppBarScrollBehavior.state.resetHeightOffset()
                                        }
                                    } else {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }

                            val onSearchLongClick: (() -> Unit)? = null

                            // Pre-calculate values for graphicsLayer to avoid reading state during composition
                            val navBarTotalHeight = bottomInset + NavigationBarHeight

                            if (!showRail && currentRoute != "wrapped" && currentRoute != "update" && currentRoute != "listen_together/chat") {
                                Box {
                                    BottomSheetPlayer(
                                        state = playerBottomSheetState,
                                        navController = navController,
                                        pureBlack = pureBlack
                                    )

                                    val snackbarBottomPadding = remember(bottomInset, shouldShowNavigationBar, slimNav) {
                                        var bottom = bottomInset
                                        if (shouldShowNavigationBar) {
                                            bottom += if (slimNav) SlimNavBarHeight else NavigationBarHeight
                                        }
                                        bottom
                                    }

                                    SnackbarHost(
                                        hostState = snackbarHostState,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = snackbarBottomPadding)
                                            .graphicsLayer {
                                                alpha = (1f - playerBottomSheetState.progress).coerceIn(0f, 1f)
                                            }
                                    ) { data ->
                                        Snackbar(
                                            snackbarData = data,
                                            shape = RoundedCornerShape(28.dp),
                                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                                            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                            actionColor = MaterialTheme.colorScheme.inversePrimary,
                                            actionContentColor = MaterialTheme.colorScheme.inversePrimary,
                                            dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface
                                        )
                                    }

                                    AppNavigationBar(
                                        navigationItems = navigationItems,
                                        currentRoute = currentRoute,
                                        onItemClick = onNavItemClick,
                                        pureBlack = pureBlack,
                                        slimNav = slimNav,
                                        onSearchLongClick = onSearchLongClick,
                                        navigationStyle = navigationStyle,
                                        floatingNav = floatingNav,
                                        bottomInset = bottomInset,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .height(bottomInset + navPadding)
                                            // Use graphicsLayer instead of offset to avoid recomposition
                                            // graphicsLayer runs during draw phase, not composition phase
                                            .graphicsLayer {
                                                val navBarHeightPx = navigationBarHeight.toPx()
                                                val totalHeightPx = navBarTotalHeight.toPx()

                                                translationY = if (navBarHeightPx == 0f) {
                                                    totalHeightPx
                                                } else {
                                                    // Read progress only during draw phase
                                                    val progress = playerBottomSheetState.progress.coerceIn(0f, 1f)
                                                    val slideOffset = totalHeightPx * progress
                                                    val hideOffset = totalHeightPx * (1 - navBarHeightPx / NavigationBarHeight.toPx())
                                                    slideOffset + hideOffset
                                                }
                                            }
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .height(bottomInsetDp)
                                            // Use graphicsLayer for background color changes
                                            .graphicsLayer {
                                                val progress = playerBottomSheetState.progress
                                                alpha = if (progress > 0f || (useNewMiniPlayerDesign && !shouldShowNavigationBar) || navigationStyle.isFloating) 0f else 1f
                                            }
                                            .background(baseBg)
                                    )
                                }
                            } else {
                                if (currentRoute != "wrapped" && currentRoute != "update" && currentRoute != "listen_together/chat") {
                                    BottomSheetPlayer(
                                        state = playerBottomSheetState,
                                        navController = navController,
                                        pureBlack = pureBlack
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .height(bottomInsetDp)
                                        // Use graphicsLayer for background color changes
                                        .graphicsLayer {
                                            val progress = playerBottomSheetState.progress
                                            alpha = if (progress > 0f || (useNewMiniPlayerDesign && !shouldShowNavigationBar)) 0f else 1f
                                        }
                                        .background(baseBg)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            val onRailItemClick: (Screens, Boolean) -> Unit = remember(navController, coroutineScope, topAppBarScrollBehavior, playerBottomSheetState) {
                                { screen: Screens, isSelected: Boolean ->
                                    if (playerBottomSheetState.isExpanded) {
                                        playerBottomSheetState.collapseSoft()
                                    }

                                    if (isSelected) {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("scrollToTop", true)
                                        coroutineScope.launch {
                                            topAppBarScrollBehavior.state.resetHeightOffset()
                                        }
                                    } else {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }

                            val onRailSearchLongClick: (() -> Unit)? = null

                            if (showRail && currentRoute != "wrapped" && currentRoute != "update") {
                                AppNavigationRail(
                                    navigationItems = navigationItems,
                                    currentRoute = currentRoute,
                                    onItemClick = onRailItemClick,
                                    pureBlack = pureBlack,
                                    onSearchLongClick = onRailSearchLongClick
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                // NavHost with animations (Material 3 Expressive style)
                                NavHost(
                                    navController = navController,
                                    startDestination = when (tabOpenedFromShortcut ?: defaultOpenTab) {
                                        NavigationTab.HOME -> Screens.Home
                                        NavigationTab.LIBRARY -> Screens.Library
                                        NavigationTab.SEARCH -> Screens.Search
                                        else -> Screens.Home
                                    }.route,
                                    // Enter Transition - Material 3 Expressive spring motion with subtle parallax
                                    enterTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst {
                                            it.route == targetState.destination.route
                                        }
                                        val previousRouteIndex = navigationItems.indexOfFirst {
                                            it.route == initialState.destination.route
                                        }

                                        val navSpring = spring<IntOffset>(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )

                                        if (currentRouteIndex == -1 || currentRouteIndex > previousRouteIndex)
                                            slideInHorizontally(animationSpec = navSpring) { it / 8 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                                        else
                                            slideInHorizontally(animationSpec = navSpring) { -it / 8 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                                    },
                                    // Exit Transition - Material 3 Expressive spring motion with subtle parallax
                                    exitTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst {
                                            it.route == initialState.destination.route
                                        }
                                        val targetRouteIndex = navigationItems.indexOfFirst {
                                            it.route == targetState.destination.route
                                        }

                                        val navSpring = spring<IntOffset>(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )

                                        if (targetRouteIndex == -1 || targetRouteIndex > currentRouteIndex)
                                            slideOutHorizontally(animationSpec = navSpring) { -it / 8 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                        else
                                            slideOutHorizontally(animationSpec = navSpring) { it / 8 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                    },
                                    // Pop Enter Transition
                                    popEnterTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst {
                                            it.route == targetState.destination.route
                                        }
                                        val previousRouteIndex = navigationItems.indexOfFirst {
                                            it.route == initialState.destination.route
                                        }

                                        val navSpring = spring<IntOffset>(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )

                                        if (previousRouteIndex != -1 && previousRouteIndex < currentRouteIndex)
                                            slideInHorizontally(animationSpec = navSpring) { it / 8 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                                        else
                                            slideInHorizontally(animationSpec = navSpring) { -it / 8 } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                                    },
                                    // Pop Exit Transition
                                    popExitTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst {
                                            it.route == initialState.destination.route
                                        }
                                        val targetRouteIndex = navigationItems.indexOfFirst {
                                            it.route == targetState.destination.route
                                        }

                                        val navSpring = spring<IntOffset>(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )

                                        if (currentRouteIndex != -1 && currentRouteIndex < targetRouteIndex)
                                            slideOutHorizontally(animationSpec = navSpring) { -it / 8 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                        else
                                            slideOutHorizontally(animationSpec = navSpring) { it / 8 } + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                    },
                                    modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                                ) {
                                    navigationBuilder(
                                        navController = navController,
                                        scrollBehavior = topAppBarScrollBehavior,
                                        activity = this@MainActivity,
                                        snackbarHostState = snackbarHostState,
                                        homeViewModel = homeViewModel
                                    )
                                }
                            }
                        }
                    }

                    BottomSheetMenu(
                        state = LocalMenuState.current,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    BottomSheetPage(
                        state = LocalBottomSheetPageState.current,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )



                    sharedSong?.let { song ->
                        playerConnection?.let {
                            Dialog(
                                onDismissRequest = { sharedSong = null },
                                properties = DialogProperties(usePlatformDefaultWidth = false),
                            ) {
                                Surface(
                                    modifier = Modifier.padding(24.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = AlertDialogDefaults.containerColor,
                                    tonalElevation = AlertDialogDefaults.TonalElevation,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        YouTubeSongMenu(
                                            song = song,
                                            navController = navController,
                                            onDismiss = { sharedSong = null },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent, navController: NavHostController) {
        val rawText = intent.extras?.getString(Intent.EXTRA_TEXT)
        val uri: Uri = intent.data ?: run {
            if (rawText != null) {
                val urlMatch = Regex("""(https?://[^\s]+)""").find(rawText)?.value
                urlMatch?.toUri() ?: runCatching { rawText.trim().toUri() }.getOrNull()
            } else {
                null
            }
        } ?: return

        intent.data = null
        intent.removeExtra(Intent.EXTRA_TEXT)
        val coroutineScope = lifecycle.coroutineScope

        val listenCode = uri.getQueryParameter("code")
            ?: uri.getQueryParameter("room")
            ?: uri.pathSegments.getOrNull(1)
        val isListenLink = uri.pathSegments.firstOrNull() == "listen" || uri.host?.equals("listen", ignoreCase = true) == true
        if (!listenCode.isNullOrBlank() && isListenLink) {
            val username = dataStore.get(ListenTogetherUsernameKey, "").ifBlank { "Guest" }
            listenTogetherManager.joinRoom(listenCode, username)
            return
        }

        when (val path = uri.pathSegments.firstOrNull()) {
            "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                if (playlistId.startsWith("OLAK5uy_")) {
                    coroutineScope.launch(Dispatchers.IO) {
                        YouTube.albumSongs(playlistId).onSuccess { songs ->
                            songs.firstOrNull()?.album?.id?.let { browseId ->
                                withContext(Dispatchers.Main) {
                                    navController.navigate("album/$browseId")
                                }
                            }
                        }.onFailure { reportException(it) }
                    }
                } else {
                    navController.navigate("online_playlist/$playlistId")
                }
            }

            "browse" -> uri.lastPathSegment?.let { browseId ->
                navController.navigate("album/$browseId")
            }

            "channel", "c" -> uri.lastPathSegment?.let { artistId ->
                navController.navigate("artist/$artistId")
            }

            "search" -> {
                val q = uri.getQueryParameter("q")
                if (!q.isNullOrEmpty()) {
                    navController.navigate("search/${URLEncoder.encode(q, "UTF-8")}")
                } else {
                    navController.navigate(Screens.Search.route)
                }
            }

            else -> {
                val videoId = when {
                    path == "watch" -> uri.getQueryParameter("v")
                    uri.host?.contains("youtu.be") == true -> uri.pathSegments.firstOrNull()
                    uri.pathSegments.contains("shorts") -> {
                        val idx = uri.pathSegments.indexOf("shorts")
                        uri.pathSegments.getOrNull(idx + 1)
                    }
                    uri.pathSegments.contains("live") -> {
                        val idx = uri.pathSegments.indexOf("live")
                        uri.pathSegments.getOrNull(idx + 1)
                    }
                    uri.pathSegments.contains("embed") -> {
                        val idx = uri.pathSegments.indexOf("embed")
                        uri.pathSegments.getOrNull(idx + 1)
                    }
                    uri.pathSegments.contains("v") -> {
                        val idx = uri.pathSegments.indexOf("v")
                        uri.pathSegments.getOrNull(idx + 1)
                    }
                    else -> {
                        Regex("""(?:v=|/(?:v|embed|shorts|live)/|youtu\.be/)([a-zA-Z0-9_-]{11})""")
                            .find(uri.toString())?.groupValues?.get(1)
                    }
                }

                val playlistId = uri.getQueryParameter("list")

                if (videoId != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        var attempts = 0
                        while (playerConnection == null && attempts < 15) {
                            delay(200)
                            attempts++
                        }

                        YouTube.queue(listOf(videoId), playlistId).onSuccess { queue ->
                            val firstItem = queue.firstOrNull()
                            if (firstItem != null) {
                                withContext(Dispatchers.Main) {
                                    playerConnection?.playQueue(
                                        YouTubeQueue(
                                            WatchEndpoint(videoId = firstItem.id, playlistId = playlistId),
                                            firstItem.toMediaMetadata()
                                        )
                                    )
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, R.string.error_playback_failed, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.onFailure { ex ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, R.string.error_no_stream, Toast.LENGTH_SHORT).show()
                            }
                            reportException(ex)
                        }
                    }
                } else if (playlistId != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        var attempts = 0
                        while (playerConnection == null && attempts < 15) {
                            delay(200)
                            attempts++
                        }

                        YouTube.queue(null, playlistId).onSuccess { queue ->
                            val firstItem = queue.firstOrNull()
                            if (firstItem != null) {
                                withContext(Dispatchers.Main) {
                                    playerConnection?.playQueue(
                                        YouTubeQueue(
                                            WatchEndpoint(videoId = firstItem.id, playlistId = playlistId),
                                            firstItem.toMediaMetadata()
                                        )
                                    )
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, R.string.error_playback_failed, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.onFailure { ex ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, R.string.error_no_stream, Toast.LENGTH_SHORT).show()
                            }
                            reportException(ex)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setSystemBarAppearance(isDark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }
}

val LocalDatabase = staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalDownloadUtil = staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }
val LocalSyncUtils = staticCompositionLocalOf<SyncUtils> { error("No SyncUtils provided") }
val LocalListenTogetherManager = staticCompositionLocalOf<com.music.mkr.listentogether.ListenTogetherManager?> { null }
val LocalIsPlayerExpanded = compositionLocalOf { false }
