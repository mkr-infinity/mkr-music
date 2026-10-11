@file:OptIn(ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.music.mkr

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.music.mkr.ui.theme.mkrMusicTheme
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.music.mkr.constants.IsFirstRunKey
import com.music.mkr.ui.utils.safeOpenUri
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import android.app.Activity
import androidx.compose.animation.core.animateDpAsState
import com.music.mkr.constants.AppLanguageKey
import com.music.mkr.constants.SYSTEM_DEFAULT
import com.music.mkr.constants.LanguageCodeToName
import com.music.mkr.ui.component.EnumDialog
import com.music.mkr.utils.rememberPreference
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay


data class OnboardingPageInfo(
    val content: @Composable (onUpdateScrollState: (Boolean) -> Unit) -> Unit
)

private data class OnboardingFeatureItem(
    val title: String,
    val description: String,
    val tag: String,
    val iconRes: Int,
    val iconColor: Color,
    val iconTint: Color
)

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val isFirstRun = dataStore.get(IsFirstRunKey, true)
        val forceShow = intent.getBooleanExtra("FORCE_SHOW", false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val appLang = dataStore.get(AppLanguageKey, SYSTEM_DEFAULT)
            val locale = appLang
                .takeUnless { it == SYSTEM_DEFAULT }
                ?.let { java.util.Locale.forLanguageTag(it) }
                ?: java.util.Locale.getDefault()
            com.music.mkr.utils.setAppLocale(this, locale)
        }

        if (!isFirstRun && !forceShow) {
            finishOnboarding()
            return
        }

        enableEdgeToEdge()
        setContent {
            mkrMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomePagerScreen(onFinished = {
                        lifecycleScope.launch {
                            dataStore.edit { it[IsFirstRunKey] = false }
                            if (forceShow) {
                                finish()
                            } else {
                                finishOnboarding()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        resId = com.music.mkr.R.font.plus_jakarta_sans,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

@Composable
fun WelcomePagerScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val commonAnimSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
    val pageTransitionSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val primaryColor = MaterialTheme.colorScheme.primary

    val (appLanguage, onAppLanguageChange) = rememberPreference(key = AppLanguageKey, defaultValue = SYSTEM_DEFAULT)
    var showAppLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishingTransition by remember { mutableStateOf(false) }

    LaunchedEffect(showFinishingTransition) {
        if (showFinishingTransition) {
            delay(1200)
            onFinished()
        }
    }


    val topCardShape =
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleCardShape = RoundedCornerShape(4.dp)
    val bottomCardShape =
        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = com.music.mkr.R.font.plus_jakarta_sans,
            variationSettings = FontVariation.Settings(
                FontVariation.slant(-9f),
                FontVariation.width(111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    val thinHeaderStyle = TextStyle(
        fontFamily = customWelcomeFontFamily,
        fontSize = 48.sp
    )

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isLastPageScrolledToEnd by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    val pages = listOf(
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    RotatingShapeContainer(
                        modifier = Modifier
                            .size(280.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val welcomeString = stringResource(id = com.music.mkr.R.string.welcome_to_vivi)
                    val annotatedWelcome = remember(welcomeString, primaryColor) {
                        buildAnnotatedString {
                            val target = "MKR"
                            val index = welcomeString.indexOf(target)
                            if (index != -1) {
                                val prefix = welcomeString.substring(0, index).trim()
                                append(prefix)
                                append("\n")
                                withStyle(
                                    style = SpanStyle(
                                        color = primaryColor,
                                        fontFamily = GoogleSansFlex,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(target)
                                }
                                append(welcomeString.substring(index + target.length))
                            } else {
                                append(welcomeString)
                            }
                        }
                    }

                    Text(
                        text = annotatedWelcome,
                        style = thinHeaderStyle.copy(fontSize = 56.sp, lineHeight = 64.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Stable v${BuildConfig.VERSION_NAME}",
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(AssistChipDefaults.IconSize)
                                    .clip(CircleShape)
                            )
                        },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "By ${com.music.mkr.github.GitHubConfig.DEV_NAME}",
                                fontFamily = GoogleSansFlex
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.person),
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        ),
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    Text(
                        text = stringResource(com.music.mkr.R.string.perm_required),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(com.music.mkr.R.string.perm_permissions),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 56.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(com.music.mkr.R.string.perm_intro_text),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlex
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        PermissionCard(
                            icon = painterResource(id = R.drawable.notification),
                            iconColor = Color(0xFFffaee4),
                            iconTint = Color(0xFF8d0053),
                            title = stringResource(com.music.mkr.R.string.perm_notif_title),
                            description = stringResource(com.music.mkr.R.string.perm_notif_desc),
                            shape = RoundedCornerShape(24.dp),
                            control = {
                                Switch(
                                    checked = hasNotificationPermission,
                                    onCheckedChange = {
                                        if (hasNotificationPermission) {
                                            val intent =
                                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                    putExtra(
                                                        Settings.EXTRA_APP_PACKAGE,
                                                        context.packageName
                                                    )
                                                }
                                            context.startActivity(intent)
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                    },
                                    thumbContent = {
                                        Icon(
                                            painter = painterResource(if (hasNotificationPermission) R.drawable.check else R.drawable.close),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            },
                            onClick = {
                                if (hasNotificationPermission) {
                                    val intent =
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(
                                                Settings.EXTRA_APP_PACKAGE,
                                                context.packageName
                                            )
                                        }
                                    context.startActivity(intent)
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        ),
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    Text(
                        text = "Join our",
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Community",
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 56.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MKR Music is open-source and crafted by ${com.music.mkr.github.GitHubConfig.DEV_NAME}. Connect with us, join our community, and support development!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlex
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        PermissionCard(
                            icon = painterResource(id = R.drawable.star),
                            iconColor = Color(0xFFfff1a8),
                            iconTint = Color(0xFF8d6e00),
                            title = "Star MKR Music",
                            description = "Star our repository on GitHub to support our work.",
                            shape = topCardShape,
                            control = {
                                Icon(
                                    painter = painterResource(id = R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                uriHandler.safeOpenUri(context, com.music.mkr.github.GitHubConfig.REPO_URL)
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        PermissionCard(
                            icon = painterResource(com.music.mkr.R.drawable.instagram),
                            iconColor = Color(0xFFffb0cd),
                            iconTint = Color(0xFF7b003a),
                            title = "Instagram",
                            description = "${com.music.mkr.github.GitHubConfig.DEV_HANDLE} - Follow for updates and news.",
                            shape = middleCardShape,
                            control = {
                                Icon(
                                    painter = painterResource(id = R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                uriHandler.safeOpenUri(context, com.music.mkr.github.GitHubConfig.DEV_INSTAGRAM_URL)
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        PermissionCard(
                            icon = painterResource(com.music.mkr.R.drawable.globe),
                            iconColor = Color(0xFFb5e48c),
                            iconTint = Color(0xFF1b4332),
                            title = "Developer Website",
                            description = "Visit mkr-infinity.github.io to explore more projects.",
                            shape = middleCardShape,
                            control = {
                                Icon(
                                    painter = painterResource(id = R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                uriHandler.safeOpenUri(context, com.music.mkr.github.GitHubConfig.DEV_WEBSITE_URL)
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        PermissionCard(
                            icon = painterResource(com.music.mkr.R.drawable.buymeacoffee),
                            iconColor = Color(0xFFffb4ab),
                            iconTint = Color(0xFF690005),
                            title = "Buy Me a Coffee",
                            description = "Support ongoing development of MKR Music directly.",
                            shape = bottomCardShape,
                            control = {
                                Icon(
                                    painter = painterResource(id = R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                uriHandler.safeOpenUri(context, com.music.mkr.github.GitHubConfig.DEV_BUYMEACOFFEE_URL)
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        ),
        OnboardingPageInfo(
            content = { onUpdateScroll ->
                LaunchedEffect(Unit) {
                    onUpdateScroll(true)
                }

                val features = remember {
                    listOf(
                        OnboardingFeatureItem(
                            title = "Apple-Style Player",
                            description = "Fluid gestures, animated liquid lyrics and floating miniplayer crafted with pure elegance.",
                            tag = "EXPERIENCE",
                            iconRes = R.drawable.lyrics_apple,
                            iconColor = Color(0xFFffaee4),
                            iconTint = Color(0xFF8d0053)
                        ),
                        OnboardingFeatureItem(
                            title = "Dual Engine Streaming",
                            description = "High-fidelity JioSaavn audio streaming paired with seamless YouTube Music fallback.",
                            tag = "AUDIO CORE",
                            iconRes = R.drawable.high_quality,
                            iconColor = Color(0xFFffb683),
                            iconTint = Color(0xFF753403)
                        ),
                        OnboardingFeatureItem(
                            title = "Synced & Liquid Lyrics",
                            description = "Real-time synchronized lyric scrolls with translation, romanization and word highlights.",
                            tag = "LYRICS",
                            iconRes = R.drawable.lyrics,
                            iconColor = Color(0xFF80da88),
                            iconTint = Color(0xFF00522c)
                        ),
                        OnboardingFeatureItem(
                            title = "Axion DSP Equalizer",
                            description = "Full parametric equalizer, bass boost and hardware loudness enhancement engine.",
                            tag = "DSP SOUND",
                            iconRes = R.drawable.equalizer,
                            iconColor = Color(0xFFcabeff),
                            iconTint = Color(0xFF1c0062)
                        ),
                        OnboardingFeatureItem(
                            title = "Offline Downloads",
                            description = "Download tracks, albums and entire playlists for unmetered, lossless offline listening anywhere.",
                            tag = "OFFLINE",
                            iconRes = R.drawable.download,
                            iconColor = Color(0xFF67d4ff),
                            iconTint = Color(0xFF004e5d)
                        ),
                        OnboardingFeatureItem(
                            title = "Dynamic Launcher Icons",
                            description = "Choose from exclusive adaptive app icon styles to match your home screen aesthetic.",
                            tag = "CUSTOMIZATION",
                            iconRes = R.drawable.palette,
                            iconColor = Color(0xFFffb0cd),
                            iconTint = Color(0xFF7b003a)
                        ),
                        OnboardingFeatureItem(
                            title = "Discord Rich Presence",
                            description = "Broadcast your real-time song playback status, album art and artist details on Discord.",
                            tag = "COMMUNITY",
                            iconRes = R.drawable.discord,
                            iconColor = Color(0xFFb6c6ed),
                            iconTint = Color(0xFF001b3f)
                        ),
                        OnboardingFeatureItem(
                            title = "Smart Audio Caching",
                            description = "Intelligent prefetching and customizable cache storage up to 10GB for instant zero-lag playback.",
                            tag = "PERFORMANCE",
                            iconRes = R.drawable.storage,
                            iconColor = Color(0xFFb5e48c),
                            iconTint = Color(0xFF1b4332)
                        )
                    )
                }

                val featureCount = features.size
                val virtualStart = remember { 1000 * featureCount }
                val featurePagerState = rememberPagerState(
                    initialPage = virtualStart,
                    pageCount = { Int.MAX_VALUE }
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(
                            text = stringResource(com.music.mkr.R.string.feat_discover),
                            style = thinHeaderStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(com.music.mkr.R.string.feat_features),
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 50.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Slide smoothly across features crafted for the ultimate music journey.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = GoogleSansFlex
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(
                            state = featurePagerState,
                            contentPadding = PaddingValues(horizontal = 36.dp),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val actualIndex = (page % featureCount + featureCount) % featureCount
                            val item = features[actualIndex]

                            val pageOffset = ((featurePagerState.currentPage - page) + featurePagerState.currentPageOffsetFraction).absoluteValue
                            val scale = lerp(0.88f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            val alpha = lerp(0.50f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(310.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    },
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = item.iconColor,
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = item.iconRes),
                                                    contentDescription = null,
                                                    tint = item.iconTint,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = item.tag,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = GoogleSansFlex,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.1.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = GoogleSansFlex,
                                            fontSize = 21.sp,
                                            lineHeight = 27.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = item.description,
                                            fontFamily = GoogleSansFlex,
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${actualIndex + 1} / $featureCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = GoogleSansFlex,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentRealIndex = (featurePagerState.currentPage % featureCount + featureCount) % featureCount
                        repeat(featureCount) { idx ->
                            val isSelected = idx == currentRealIndex
                            val dotWidth by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = tween(280),
                                label = "featureDotWidth"
                            )
                            val dotColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(8.dp)
                                    .width(dotWidth)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isFirstPage = pagerState.currentPage == 0
    val isTargetFirstPage = pagerState.targetPage == 0
    val isLastPage = pagerState.currentPage == pages.size - 1

    if (showAppLanguageDialog) {
        EnumDialog(
            onDismiss = { showAppLanguageDialog = false },
            onSelect = { selectedLang ->
                scope.launch {
                    context.dataStore.edit { it[AppLanguageKey] = selectedLang }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        val locale = if (selectedLang == SYSTEM_DEFAULT) java.util.Locale.getDefault() else java.util.Locale.forLanguageTag(selectedLang)
                        com.music.mkr.utils.setAppLocale(context, locale)
                        (context as? Activity)?.recreate()
                    }
                }
                showAppLanguageDialog = false
            },
            title = stringResource(com.music.mkr.R.string.app_language),
            current = appLanguage,
            values = (listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList()),
            valueText = {
                LanguageCodeToName.getOrElse(it) { stringResource(com.music.mkr.R.string.system_default) }
            }
        )
    }


    BackHandler(enabled = !isTargetFirstPage && !showFinishingTransition) {
        scope.launch {
            pagerState.animateScrollToPage(
                pagerState.currentPage - 1,
                animationSpec = pageTransitionSpatialSpec
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = !showFinishingTransition,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { index ->
                    pages[index].content { scrolledToEnd ->
                        if (index == pages.size - 1) {
                            isLastPageScrolledToEnd = true
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val languageButtonWidth by animateDpAsState(
                    targetValue = if (isTargetFirstPage) 64.dp else 0.dp,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "languageButtonWidth"
                )
                val languageButtonAlpha by animateFloatAsState(
                    targetValue = if (isTargetFirstPage) 1f else 0f,
                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "languageButtonAlpha"
                )
                val spacingValue by animateDpAsState(
                    targetValue = if (isTargetFirstPage) 12.dp else 0.dp,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "buttonSpacing"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val languageButtonShapes = IconButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(12.dp)
                    )
                    FilledTonalIconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APP_LOCALE_SETTINGS,
                                            "package:${context.packageName}".toUri()
                                        )
                                    )
                                }
                            } else {
                                showAppLanguageDialog = true
                            }
                        },
                        modifier = Modifier
                            .size(languageButtonWidth)
                            .alpha(languageButtonAlpha),
                        shapes = languageButtonShapes,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = com.music.mkr.R.drawable.language),
                            contentDescription = "Language",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(spacingValue))

                    val isNextEnabled = !isLastPage || isLastPageScrolledToEnd
                    val alphaNext by animateFloatAsState(
                        targetValue = if (isNextEnabled) 1f else 0.5f,
                        label = "nextAlpha"
                    )

                    WelcomeExpressiveButton(
                        text = if (isLastPage) stringResource(com.music.mkr.R.string.get_started) else stringResource(com.music.mkr.R.string.next),
                        onClick = {
                            if (isLastPage) {
                                if (isLastPageScrolledToEnd) showFinishingTransition = true
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1,
                                        animationSpec = pageTransitionSpatialSpec
                                    )
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = alphaNext),
                        contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = alphaNext),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        showArrowOnly = isTargetFirstPage
                    )
                }
            }
        }

        // Finishing transition screen (blob cluster loading)
        AnimatedVisibility(
            visible = showFinishingTransition,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + scaleIn(initialScale = 0.92f),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Setting up",
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "MKR Music…",
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 56.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                BlobClusterLoading(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.weight(1.2f))
            }
        }

        // Top-left back arrow overlay using TopAppBar for alignment with settings screens
        AnimatedVisibility(
            visible = !isTargetFirstPage && !showFinishingTransition,
            enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1,
                                animationSpec = pageTransitionSpatialSpec
                            )
                        }
                    }) {
                        Icon(
                            painter = painterResource(com.music.mkr.R.drawable.arrow_back),
                            contentDescription = stringResource(com.music.mkr.R.string.back_button_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun BlobClusterLoading(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blobClusterRotation")
    
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )
    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation3"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Blob 1 (large, primary)
        Icon(
            painter = painterResource(id = com.music.mkr.R.drawable.ic_ten_sided_cookie),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(140.dp)
                .rotate(rotation1)
        )
        // Blob 2 (medium, secondary, offset)
        Icon(
            painter = painterResource(id = com.music.mkr.R.drawable.ic_ten_sided_cookie),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .size(100.dp)
                .rotate(rotation2)
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
        )
        // Blob 3 (small, tertiary, offset)
        Icon(
            painter = painterResource(id = com.music.mkr.R.drawable.ic_ten_sided_cookie),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .size(80.dp)
                .rotate(rotation3)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        )
    }
}

@Composable
fun RotatingShapeContainer(modifier: Modifier = Modifier, rotate: Boolean = true) {
    val rotation = if (rotate) {
        val infiniteTransition = rememberInfiniteTransition(label = "shapeRotation")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        ).value
    } else {
        0f
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = com.music.mkr.R.drawable.ic_ten_sided_cookie),
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        )

        Image(
            painter = painterResource(com.music.mkr.R.drawable.icon),
            contentDescription = "MKR Music",
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun PermissionCard(
    icon: Painter,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    shape: Shape,
    control: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "anim_shape"
    )

    val animatedShape = remember(shape, pressProgress) {
        if (shape is RoundedCornerShape) {
            object : Shape {
                override fun createOutline(
                    size: Size,
                    layoutDirection: LayoutDirection,
                    density: Density
                ): Outline {
                    val targetPx = with(density) { 20.dp.toPx() }
                    fun lerp(start: Float, stop: Float, fraction: Float) =
                        (1 - fraction) * start + fraction * stop

                    val ts = lerp(shape.topStart.toPx(size, density), targetPx, pressProgress)
                    val te = lerp(shape.topEnd.toPx(size, density), targetPx, pressProgress)
                    val bs = lerp(shape.bottomStart.toPx(size, density), targetPx, pressProgress)
                    val be = lerp(shape.bottomEnd.toPx(size, density), targetPx, pressProgress)

                    return Outline.Rounded(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(
                                0f,
                                0f,
                                size.width,
                                size.height
                            ),
                            topLeft = androidx.compose.ui.geometry.CornerRadius(ts),
                            topRight = androidx.compose.ui.geometry.CornerRadius(te),
                            bottomRight = androidx.compose.ui.geometry.CornerRadius(be),
                            bottomLeft = androidx.compose.ui.geometry.CornerRadius(bs)
                        )
                    )
                }
            }
        } else shape
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(animatedShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = onClick
            ),
        shape = animatedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex
                )
            },
            supportingContent = {
                Text(
                    text = description,
                    fontFamily = GoogleSansFlex,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            trailingContent = control,
            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun FeatureCard(
    icon: Painter,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    shape: Shape
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex
                )
            },
            supportingContent = {
                Text(
                    text = description,
                    fontFamily = GoogleSansFlex,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun FeatureGridCard(
    icon: Painter,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 120.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontFamily = GoogleSansFlex,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WelcomeExpressiveButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
    showArrowOnly: Boolean = false
) {
    val shapes = ButtonDefaults.shapes(
        shape = CircleShape,
        pressedShape = RoundedCornerShape(20.dp)
    )
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shapes = shapes
    ) {
        AnimatedContent(
            targetState = showArrowOnly,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) + 
                 scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            label = "buttonContentTransition"
        ) { arrowOnly ->
            if (arrowOnly) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = text,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex,
                    fontSize = 18.sp
                )
            }
        }
    }
}

