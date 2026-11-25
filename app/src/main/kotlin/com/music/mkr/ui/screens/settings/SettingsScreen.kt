/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.music.innertube.utils.parseCookieString
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.ui.component.rememberPlaybackCookieAngle
import com.music.mkr.viewmodels.HomeViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.mkr.BuildConfig
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.R
import com.music.mkr.constants.PureBlackKey
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.ExpressiveIconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.component.DefaultDialog
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.screens.settings.DarkMode
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.safeOpenUri
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.rememberPreference
import com.music.mkr.constants.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    val autoUpdateSetting = remember { getAutoUpdateCheckSetting(context) }
    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
    var searchQuery by remember { mutableStateOf("") }
    val darkThemeMode by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isDarkTheme = when (darkThemeMode) {
        DarkMode.ON -> true
        DarkMode.OFF -> false
        DarkMode.AUTO -> isSystemInDarkTheme()
    }
    val pureBlackPref by rememberPreference(PureBlackKey, defaultValue = false)
    val pureBlack = pureBlackPref && isDarkTheme
    val accountName by rememberPreference(AccountNameKey, defaultValue = "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, defaultValue = "")
    val homeAccountName by homeViewModel.accountName.collectAsState()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val resolvedAccountName = remember(homeAccountName, accountName) {
        listOf(homeAccountName.takeIf { it != "Guest" }, accountName)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
    }
    val cookieAngle by rememberPlaybackCookieAngle(LocalPlayerConnection.current, visible = true)

    val searchCatalog = remember(context) { buildSettingsSearchCatalog(context) }

    DisposableEffect(context) {
        val sharedPrefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "update_available") {
                isUpdateAvailable = getUpdateAvailableState(context)
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        isUpdateAvailable = getUpdateAvailableState(context)
        onDispose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (autoUpdateSetting) {
            checkForUpdate(
                context = context,
                onSuccess = { _, isAvailable, _, _, _, _, _, _ ->
                    if (getAutoUpdateCheckSetting(context)) {
                        saveUpdateAvailableState(context, isAvailable)
                    }
                },
                onError = {}
            )
        }
    }

    val surfaceColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface
    val cardContainerBase = if (pureBlack) Color(0xFF141518) else MaterialTheme.colorScheme.surfaceContainerLow

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        val isWide = maxWidth >= 600.dp
        val horizontalPadding = if (isWide) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                if (searchQuery.isBlank()) {
                    SettingsHeroBanner(
                        accountName = resolvedAccountName,
                        accountImageUrl = accountImageUrl,
                        isLoggedIn = isLoggedIn,
                        isUpdateAvailable = isUpdateAvailable,
                        pureBlack = pureBlack,
                        cookieAngle = cookieAngle,
                        onBannerClick = {
                            navController.navigate(if (isUpdateAvailable) "settings/update" else "settings/account")
                        },
                        onVersionClick = { navController.navigate("settings/about") }
                    )
                }

                // Borderless Pill Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search settings...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = stringResource(R.string.clear),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (pureBlack) Color(0xFF141518) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = if (pureBlack) Color(0xFF141518) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchQuery.isNotBlank()) {
                    val searchResults = remember(searchQuery, searchCatalog) {
                        searchSettingsCatalog(searchQuery, searchCatalog)
                    }
                    val displayedResults = remember(searchResults) {
                        searchResults.take(35)
                    }

                    if (searchResults.isEmpty()) {
                        EmptyPlaceholder(
                            icon = R.drawable.search,
                            text = "No matching settings found",
                            description = "Try searching with different keywords like 'silence', 'bluetooth', 'sponsor', 'eq', 'theme', 'cache', or 'clear task'",
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchResults.size > displayedResults.size) "Results (top ${displayedResults.size} of ${searchResults.size})" else "Results (${searchResults.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ranked by relevance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        ExpressiveSettingGroup(
                            items = displayedResults.map { item ->
                                Material3SettingsItem(
                                    icon = painterResource(item.iconRes),
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    },
                                    description = {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        SettingsHighlightManager.requestHighlight(item.targetKey, item.categoryKey)
                                        if (item.route == "settings") {
                                            searchQuery = ""
                                        } else {
                                            navController.navigate(item.route)
                                        }
                                    }
                                )
                            }
                        )

                        if (searchResults.size > displayedResults.size) {
                            Text(
                                text = "Type more letters to narrow down among ${searchResults.size} matches",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }
                    }
                } else {
                    // Section 1 — Four Primary Categories in Adaptive Expressive Bento Grid
                    val primaryCards = remember {
                        listOf(
                            SettingsHubCategory(
                                key = "appearance_theme_palette",
                                icon = R.drawable.palette,
                                title = "Appearance",
                                subtitle = "Theme & layout",
                                route = "settings/appearance"
                            ),
                            SettingsHubCategory(
                                key = "player_audio_quality",
                                icon = R.drawable.earbud_case,
                                title = "Player and Audio",
                                subtitle = "Playback & EQ",
                                route = "settings/player"
                            ),
                            SettingsHubCategory(
                                key = "account_login",
                                icon = R.drawable.account,
                                title = "Account",
                                subtitle = "Sync & profile",
                                route = "settings/account"
                            ),
                            SettingsHubCategory(
                                key = "storage_downloads",
                                icon = R.drawable.storage,
                                title = "Storage",
                                subtitle = "Cache & downloads",
                                route = "settings/storage"
                            )
                        )
                    }

                    SettingsBentoGrid(
                        categories = primaryCards,
                        isWide = isWide,
                        pureBlack = pureBlack,
                        baseSurface = cardContainerBase,
                        cookieAngle = cookieAngle,
                        onCategoryClick = { category ->
                            navController.navigate(category.route)
                        }
                    )

                    val contentAiGroup: @Composable () -> Unit = {
                        val contentCookieShape = MaterialShapes.Clover8Leaf.toShape()
                        ExpressiveSettingGroup(
                            title = "Content & AI",
                            itemMinHeight = 56.dp,
                            useCapsules = false,
                            items = listOf(
                                Material3SettingsItem(
                                    key = "content_language",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.language,
                                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.secondary,
                                            shape = contentCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.content)) },
                                    description = { Text("Region & lyrics") },
                                    onClick = { navController.navigate("settings/content") }
                                ),
                                Material3SettingsItem(
                                    key = "content_ai_lyrics",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.translate,
                                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.secondary,
                                            shape = contentCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                                    description = { Text("Auto-translate") },
                                    onClick = { navController.navigate("settings/ai") }
                                )
                            )
                        )
                    }

                    val communityGroup: @Composable () -> Unit = {
                        val communityCookieShape = MaterialShapes.Cookie4Sided.toShape()
                        ExpressiveSettingGroup(
                            title = "Features & Community",
                            itemMinHeight = 56.dp,
                            useCapsules = false,
                            items = listOf(
                                Material3SettingsItem(
                                    key = "screen_listen_together",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.group,
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.tertiary,
                                            shape = communityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text("Listen Together") },
                                    description = { Text("Live sessions") },
                                    onClick = { navController.navigate(Screens.ListenTogether.route) }
                                ),
                                Material3SettingsItem(
                                    key = "stats_screen",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.stats,
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.tertiary,
                                            shape = communityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.stats)) },
                                    description = { Text("Listening insights") },
                                    onClick = { navController.navigate("stats") }
                                ),
                                Material3SettingsItem(
                                    key = "privacy_listening_summary",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.history,
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.tertiary,
                                            shape = communityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text("Listening Recap") },
                                    description = { Text("Top tracks") },
                                    onClick = { navController.navigate("settings/listening_summary") }
                                ),
                                Material3SettingsItem(
                                    key = "privacy_discord_rpc",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.integration,
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.tertiary,
                                            shape = communityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.integrations)) },
                                    description = { Text("Last.fm & Discord") },
                                    onClick = { navController.navigate("settings/integrations") }
                                )
                            )
                        )
                    }

                    val securityGroup: @Composable () -> Unit = {
                        val securityCookieShape = MaterialShapes.Pill.toShape()
                        ExpressiveSettingGroup(
                            title = "Security & Data",
                            itemMinHeight = 56.dp,
                            useCapsules = false,
                            items = listOf(
                                Material3SettingsItem(
                                    key = "privacy_history",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.security,
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            shape = securityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.privacy)) },
                                    description = { Text("History & permissions") },
                                    onClick = { navController.navigate("settings/privacy") }
                                ),
                                Material3SettingsItem(
                                    key = "backup_export_import",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.restore,
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            shape = securityCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(stringResource(R.string.backup_restore)) },
                                    description = { Text("Export & import") },
                                    onClick = { navController.navigate("settings/backup_restore") }
                                )
                            )
                        )
                    }

                    val updatesAboutGroup: @Composable () -> Unit = {
                        val updatesCookieShape = MaterialShapes.Cookie12Sided.toShape()
                        ExpressiveSettingGroup(
                            title = "Updates & Feedback",
                            itemMinHeight = 56.dp,
                            useCapsules = false,
                            items = listOf(
                                Material3SettingsItem(
                                    key = "update_check",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = if (isUpdateAvailable) R.drawable.update_alert else R.drawable.system_update_uptodate,
                                            containerColor = if (isUpdateAvailable) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerHighest
                                            },
                                            contentColor = if (isUpdateAvailable) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            shape = updatesCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text(if (isUpdateAvailable) "Update Available" else "System Updates") },
                                    description = {
                                        Text(if (isUpdateAvailable) "Update ready" else "v${BuildConfig.VERSION_NAME}")
                                    },
                                    onClick = { navController.navigate("settings/update") }
                                ),
                                Material3SettingsItem(
                                    key = "support_request_feature",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.support_mkr,
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            shape = updatesCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text("Request a Feature") },
                                    description = { Text("Share new ideas") },
                                    onClick = {
                                        val featureTitle = "Feature Request: "
                                        val featureBody = """
                                            ### Feature Description
                                            Please describe the feature you would like to see in MKR Music.

                                            ### Benefit & Use Case
                                            Explain how this enhancement benefits listeners.
                                        """.trimIndent()
                                        val url = "https://github.com/mkr-infinity/mkr-music/issues/new?title=" +
                                            URLEncoder.encode(featureTitle, StandardCharsets.UTF_8.name()) +
                                            "&body=" + URLEncoder.encode(featureBody, StandardCharsets.UTF_8.name())
                                        uriHandler.safeOpenUri(context, url)
                                    },
                                    isExternalLink = true
                                ),
                                Material3SettingsItem(
                                    key = "support_report_problem",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.bug_report,
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            shape = updatesCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text("Report a Problem") },
                                    description = { Text("Submit bug report") },
                                    onClick = {
                                        val bugTitle = "Bug Report: "
                                        val bugBody = """
                                            ### Device & App Information
                                            - App Version: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                                            - Android Version: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                                            - Device: ${Build.MANUFACTURER} ${Build.MODEL}

                                            ### Description of the Issue
                                            Please describe what occurred.
                                        """.trimIndent()
                                        val url = "https://github.com/mkr-infinity/mkr-music/issues/new?title=" +
                                            URLEncoder.encode(bugTitle, StandardCharsets.UTF_8.name()) +
                                            "&body=" + URLEncoder.encode(bugBody, StandardCharsets.UTF_8.name())
                                        uriHandler.safeOpenUri(context, url)
                                    },
                                    isExternalLink = true
                                ),
                                Material3SettingsItem(
                                    key = "about_app",
                                    leadingContent = {
                                        ExpressiveRowBadge(
                                            icon = R.drawable.info,
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            shape = updatesCookieShape,
                                            rotationAngle = cookieAngle
                                        )
                                    },
                                    title = { Text("About MKR Music") },
                                    description = { Text("Credits & info") },
                                    onClick = { navController.navigate("settings/about") }
                                )
                            )
                        )
                    }

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                contentAiGroup()
                                securityGroup()
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                communityGroup()
                                updatesAboutGroup()
                            }
                        }
                    } else {
                        contentAiGroup()
                        communityGroup()
                        securityGroup()
                        updatesAboutGroup()
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = surfaceColor,
            scrolledContainerColor = surfaceColor
        ),
        title = {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 12.dp)) {
                ExpressiveIconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                ExpressiveIconButton(
                    onClick = { showResetSettingsDialog = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.replay),
                        contentDescription = stringResource(R.string.reset_settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )

    if (showResetSettingsDialog) {
        DefaultDialog(
            onDismiss = { showResetSettingsDialog = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.replay),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.reset_settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            buttons = {
                TextButton(
                    onClick = { showResetSettingsDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        showResetSettingsDialog = false
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                context.dataStore.edit { prefs ->
                                    val preservedAuth = mutableMapOf<Preferences.Key<*>, Any>()
                                    listOf(
                                        InnerTubeCookieKey,
                                        VisitorDataKey,
                                        DataSyncIdKey,
                                        AccountNameKey,
                                        AccountEmailKey,
                                        AccountChannelHandleKey,
                                        GitHubAccessTokenKey,
                                    ).forEach { key ->
                                        prefs[key]?.let { preservedAuth[key] = it }
                                    }

                                    prefs.clear()

                                    preservedAuth.forEach { (key, value) ->
                                        @Suppress("UNCHECKED_CAST")
                                        prefs[key as Preferences.Key<Any>] = value
                                    }
                                }

                                val sharedPrefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                                sharedPrefs.edit().clear().apply()

                                withContext(Dispatchers.Main) {
                                    SnackbarManager.show(R.string.reset_settings_done)
                                }
                            } catch (e: Exception) {
                                Timber.tag("SettingsScreen").e(e, "Error resetting settings")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.reset_settings_action))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.reset_settings_confirm_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class SettingsHubCategory(
    val key: String,
    val icon: Int,
    val title: String,
    val subtitle: String,
    val route: String
)

private data class CardColorScheme(
    val container: Color,
    val content: Color,
    val iconBg: Color,
    val iconTint: Color,
    val border: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsHeroBanner(
    accountName: String,
    accountImageUrl: String?,
    isLoggedIn: Boolean,
    isUpdateAvailable: Boolean,
    pureBlack: Boolean,
    cookieAngle: Float,
    onBannerClick: () -> Unit,
    onVersionClick: () -> Unit,
) {
    val hasSession = isLoggedIn || accountName.isNotBlank()
    val containerColor = if (isUpdateAvailable) {
        val transition = rememberInfiniteTransition(label = "UpdateBannerColor")
        val animatedColor by transition.animateColor(
            initialValue = MaterialTheme.colorScheme.primaryContainer,
            targetValue = MaterialTheme.colorScheme.tertiaryContainer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "UpdateBannerContainerColor"
        )
        animatedColor
    } else if (pureBlack) {
        Color(0xFF141518)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = if (isUpdateAvailable) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(role = Role.Button, onClick = onBannerClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val badgeShape = MaterialShapes.Cookie12Sided.toShape()
            if (!isUpdateAvailable && hasSession && !accountImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = accountImageUrl,
                    contentDescription = accountName.ifBlank { "YouTube account" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(badgeShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = cookieAngle }
                            .clip(badgeShape)
                            .background(
                                if (isUpdateAvailable) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            )
                    )
                    Icon(
                        painter = painterResource(
                            if (isUpdateAvailable) R.drawable.update_alert else R.drawable.account
                        ),
                        contentDescription = null,
                        tint = if (isUpdateAvailable) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUpdateAvailable) {
                        "Update Ready"
                    } else if (accountName.isNotBlank()) {
                        accountName
                    } else if (isLoggedIn) {
                        "YouTube Music"
                    } else {
                        "MKR Music"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isUpdateAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isUpdateAvailable) {
                        "Tap to install"
                    } else if (isLoggedIn) {
                        "YouTube Music"
                    } else if (accountName.isNotBlank()) {
                        "Signed in"
                    } else {
                        "Local session"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUpdateAvailable) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isUpdateAvailable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onVersionClick)
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isUpdateAvailable) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpressiveRowBadge(
    icon: Int,
    containerColor: Color,
    contentColor: Color,
    shape: Shape,
    rotationAngle: Float = 0f,
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rotationAngle != 0f) {
                        rotationZ = rotationAngle
                    }
                }
                .clip(shape)
                .background(containerColor)
        )
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsBentoGrid(
    categories: List<SettingsHubCategory>,
    isWide: Boolean,
    pureBlack: Boolean,
    baseSurface: Color,
    cookieAngle: Float,
    onCategoryClick: (SettingsHubCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    @Composable
    fun bentoColorsFor(key: String): CardColorScheme = when (key) {
        "appearance_theme_palette" -> CardColorScheme(
            container = MaterialTheme.colorScheme.primary.copy(alpha = if (pureBlack) 0.14f else 0.10f).compositeOver(baseSurface),
            content = MaterialTheme.colorScheme.onSurface,
            iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            iconTint = MaterialTheme.colorScheme.primary,
            border = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        )
        "player_audio_quality" -> CardColorScheme(
            container = MaterialTheme.colorScheme.secondary.copy(alpha = if (pureBlack) 0.14f else 0.10f).compositeOver(baseSurface),
            content = MaterialTheme.colorScheme.onSurface,
            iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            iconTint = MaterialTheme.colorScheme.secondary,
            border = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
        )
        "account_login" -> CardColorScheme(
            container = MaterialTheme.colorScheme.tertiary.copy(alpha = if (pureBlack) 0.14f else 0.10f).compositeOver(baseSurface),
            content = MaterialTheme.colorScheme.onSurface,
            iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
            iconTint = MaterialTheme.colorScheme.tertiary,
            border = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
        )
        else -> CardColorScheme(
            container = MaterialTheme.colorScheme.surfaceTint.copy(alpha = if (pureBlack) 0.12f else 0.08f).compositeOver(baseSurface),
            content = MaterialTheme.colorScheme.onSurface,
            iconBg = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.15f),
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }

    @Composable
    fun bentoShape(): Shape = MaterialShapes.Cookie6Sided.toShape()

    if (isWide) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                SettingsBentoCard(
                    category = category,
                    colors = bentoColorsFor(category.key),
                    iconShape = bentoShape(),
                    rotationAngle = cookieAngle,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .heightIn(min = 128.dp),
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { category ->
                        SettingsBentoCard(
                            category = category,
                            colors = bentoColorsFor(category.key),
                            iconShape = bentoShape(),
                            rotationAngle = cookieAngle,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .heightIn(min = 124.dp),
                            onClick = { onCategoryClick(category) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsBentoCard(
    category: SettingsHubCategory,
    colors: CardColorScheme,
    iconShape: Shape,
    rotationAngle: Float = 0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .settingsHighlight(category.key, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = category.title,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        color = colors.container,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (rotationAngle != 0f) {
                                rotationZ = rotationAngle
                            }
                        }
                        .clip(iconShape)
                        .background(colors.iconBg)
                )
                Icon(
                    painter = painterResource(category.icon),
                    contentDescription = null,
                    tint = colors.iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
