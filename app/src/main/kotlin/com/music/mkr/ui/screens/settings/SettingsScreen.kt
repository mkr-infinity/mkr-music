/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.ExpressiveIconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.safeOpenUri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val autoUpdateSetting = remember { getAutoUpdateCheckSetting(context) }
    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
    var searchQuery by remember { mutableStateOf("") }

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

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Search Bar (16.dp Rounded Surface)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search settings, features...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
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
            // Section 1 — Four Primary Categories in Card-Style UI
            val primaryCards = remember {
                listOf(
                    SettingsHubCategory(
                        key = "appearance_theme_palette",
                        icon = R.drawable.palette,
                        title = "Appearance",
                        subtitle = "Themes, accents & fonts",
                        route = "settings/appearance"
                    ),
                    SettingsHubCategory(
                        key = "player_audio_quality",
                        icon = R.drawable.earbud_case,
                        title = "Player and Audio",
                        subtitle = "Quality, styling & EQ",
                        route = "settings/player"
                    ),
                    SettingsHubCategory(
                        key = "account_login",
                        icon = R.drawable.account,
                        title = "Account",
                        subtitle = "Profiles, login & sync",
                        route = "settings/account"
                    ),
                    SettingsHubCategory(
                        key = "storage_downloads",
                        icon = R.drawable.storage,
                        title = "Storage",
                        subtitle = "Cache & download budget",
                        route = "settings/storage"
                    )
                )
            }

            SettingsCategoryCarousel(
                categories = primaryCards,
                onCategoryClick = { category ->
                    navController.navigate(category.route)
                }
            )

            // Section 2 — Content & AI
            ExpressiveSettingGroup(
                title = "Content & AI",
                itemMinHeight = 64.dp,
                useCapsules = true,
                items = listOf(
                    Material3SettingsItem(
                        key = "content_language",
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.content)) },
                        description = { Text("Language, country, lyrics providers & explicit filter") },
                        onClick = { navController.navigate("settings/content") }
                    ),
                    Material3SettingsItem(
                        key = "content_ai_lyrics",
                        icon = painterResource(R.drawable.translate),
                        title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                        description = { Text("Real-time song lyrics translation using intelligent AI") },
                        onClick = { navController.navigate("settings/ai") }
                    )
                )
            )

            // Section 6 — Features & Listen Together
            ExpressiveSettingGroup(
                title = "Features & Community",
                itemMinHeight = 64.dp,
                useCapsules = true,
                items = listOf(
                    Material3SettingsItem(
                        key = "screen_listen_together",
                        icon = painterResource(R.drawable.group),
                        title = { Text("Listen Together") },
                        description = { Text("Synchronized live group listening sessions with friends") },
                        onClick = { navController.navigate(Screens.ListenTogether.route) }
                    ),
                    Material3SettingsItem(
                        key = "stats_screen",
                        icon = painterResource(R.drawable.stats),
                        title = { Text(stringResource(R.string.stats)) },
                        description = { Text("Detailed personal listening statistics, top tracks & artists") },
                        onClick = { navController.navigate("stats") }
                    ),
                    Material3SettingsItem(
                        key = "privacy_listening_summary",
                        icon = painterResource(R.drawable.history),
                        title = { Text("Listening Recap") },
                        description = { Text("Weekly listening stats, top artists, moods & highlights") },
                        onClick = { navController.navigate("settings/listening_summary") }
                    ),
                    Material3SettingsItem(
                        key = "privacy_discord_rpc",
                        icon = painterResource(R.drawable.discord),
                        title = { Text("Discord Rich Presence") },
                        description = { Text("Display live playing track and album art on Discord") },
                        onClick = { navController.navigate("settings/integrations") }
                    )
                )
            )

            // Section 7 — Security & Data
            ExpressiveSettingGroup(
                title = "Security & Data",
                itemMinHeight = 64.dp,
                useCapsules = true,
                items = listOf(
                    Material3SettingsItem(
                        key = "privacy_history",
                        icon = painterResource(R.drawable.security),
                        title = { Text(stringResource(R.string.privacy)) },
                        description = { Text("History tracking, search logs & permission controls") },
                        onClick = { navController.navigate("settings/privacy") }
                    ),
                    Material3SettingsItem(
                        key = "backup_export_import",
                        icon = painterResource(R.drawable.restore),
                        title = { Text(stringResource(R.string.backup_restore)) },
                        description = { Text("Export & import playlists, data & settings backups") },
                        onClick = { navController.navigate("settings/backup_restore") }
                    )
                )
            )

            // Section 8 — Updates & Feedback
            ExpressiveSettingGroup(
                title = "Updates & Feedback",
                itemMinHeight = 64.dp,
                useCapsules = true,
                items = listOf(
                    Material3SettingsItem(
                        key = "update_check",
                        icon = painterResource(if (isUpdateAvailable) R.drawable.update_alert else R.drawable.system_update_uptodate),
                        title = { Text(if (isUpdateAvailable) "Update Available" else "System Updates") },
                        description = { Text(if (isUpdateAvailable) "New MKR Music release is ready to install" else "MKR Music is up to date • Check for updates") },
                        onClick = { navController.navigate("settings/update") }
                    ),
                    Material3SettingsItem(
                        key = "support_request_feature",
                        icon = painterResource(R.drawable.support_mkr),
                        title = { Text("Request a Feature") },
                        description = { Text("Propose new ideas & enhancements on GitHub") },
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
                        icon = painterResource(R.drawable.bug_report),
                        title = { Text("Report a Problem") },
                        description = { Text("Submit issue report with device & version details") },
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
                        icon = painterResource(R.drawable.info),
                        title = { Text("About MKR Music") },
                        description = { Text("Version v${BuildConfig.VERSION_NAME} • Licenses & Credits") },
                        onClick = { navController.navigate("settings/about") }
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    TopAppBar(
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
        }
    )
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
    val iconTint: Color
)

@Composable
private fun SettingsCategoryCarousel(
    categories: List<SettingsHubCategory>,
    onCategoryClick: (SettingsHubCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastInteractionTime by remember { mutableStateOf(0L) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            lastInteractionTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(categories, lifecycleOwner) {
        if (categories.size <= 1) return@LaunchedEffect
        var forward = true
        while (isActive) {
            delay(3500L)
            if (!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                continue
            }
            if (listState.isScrollInProgress) {
                lastInteractionTime = System.currentTimeMillis()
                continue
            }

            val elapsedSinceInteraction = System.currentTimeMillis() - lastInteractionTime
            if (elapsedSinceInteraction < 6000L) {
                delay(6000L - elapsedSinceInteraction)
                continue
            }
            if (listState.isScrollInProgress) {
                lastInteractionTime = System.currentTimeMillis()
                continue
            }

            val currentIdx = listState.firstVisibleItemIndex
            val targetIdx = if (forward) {
                if (currentIdx >= categories.size - 1) {
                    forward = false
                    (categories.size - 2).coerceAtLeast(0)
                } else {
                    currentIdx + 1
                }
            } else {
                if (currentIdx <= 0) {
                    forward = true
                    1.coerceAtMost(categories.size - 1)
                } else {
                    currentIdx - 1
                }
            }.coerceIn(0, categories.size - 1)

            if (targetIdx != currentIdx) {
                try {
                    listState.animateScrollToItem(targetIdx)
                } catch (e: Exception) {
                    lastInteractionTime = System.currentTimeMillis()
                }
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(categories, key = { _, cat -> cat.key }) { index, category ->
            val colorScheme = when (index % 4) {
                0 -> CardColorScheme(
                    container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    iconTint = MaterialTheme.colorScheme.primary
                )
                1 -> CardColorScheme(
                    container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                    iconTint = MaterialTheme.colorScheme.secondary
                )
                2 -> CardColorScheme(
                    container = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                    content = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                else -> CardColorScheme(
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                    content = MaterialTheme.colorScheme.onSurface,
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }

            SettingsCarouselCard(
                category = category,
                colors = colorScheme,
                modifier = Modifier
                    .width(180.dp)
                    .heightIn(min = 140.dp),
                onClick = {
                    lastInteractionTime = System.currentTimeMillis()
                    onCategoryClick(category)
                }
            )
        }
    }
}

@Composable
private fun SettingsCarouselCard(
    category: SettingsHubCategory,
    colors: CardColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .settingsHighlight(category.key, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = category.title,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = colors.container,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(category.icon),
                    contentDescription = null,
                    tint = colors.iconTint,
                    modifier = Modifier.size(22.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = colors.content.copy(alpha = 0.80f),
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
