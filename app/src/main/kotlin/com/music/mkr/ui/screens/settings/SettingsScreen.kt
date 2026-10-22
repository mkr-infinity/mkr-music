/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
                    saveUpdateAvailableState(context, isAvailable)
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

            if (searchResults.isEmpty()) {
                EmptyPlaceholder(
                    icon = R.drawable.search,
                    text = "No matching settings found",
                    description = "Try searching with different keywords like 'liquid', 'gesture', 'eq', 'theme', or 'audio'",
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
                        text = "Results (${searchResults.size})",
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
                    items = searchResults.map { item ->
                        Material3SettingsItem(
                            icon = painterResource(item.iconRes),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.SemiBold
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
                            description = { Text(item.description) },
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
            }
        } else {
            // Section 1 — Four Quick Hub Cards (2-by-2 Grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).heightIn(min = 126.dp),
                        key = "appearance_theme_palette",
                        icon = R.drawable.palette,
                        title = "Appearance",
                        subtitle = "Themes, accents & launcher icons",
                        onClick = { navController.navigate("settings/appearance") }
                    )
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).heightIn(min = 126.dp),
                        key = "player_audio_quality",
                        icon = R.drawable.earbud_case,
                        title = "Player & Audio",
                        subtitle = "Dual engine, equalizer & sound",
                        onClick = { navController.navigate("settings/player") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).heightIn(min = 126.dp),
                        key = "storage_downloads",
                        icon = R.drawable.storage,
                        title = "Storage & Cache",
                        subtitle = "Offline tracks & cache budgets",
                        onClick = { navController.navigate("settings/storage") }
                    )
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).heightIn(min = 126.dp),
                        key = "account_login",
                        icon = R.drawable.google,
                        title = "Account & Sync",
                        subtitle = "Sync playlists & library",
                        onClick = { navController.navigate("settings/account") }
                    )
                }
            }

            // Section 2 — Player Styles & Mini-Player
            ExpressiveSettingGroup(
                title = "Player Styles & Mini-Player",
                itemMinHeight = 64.dp,
                items = listOf(
                    Material3SettingsItem(
                        key = "appearance_player_style",
                        icon = painterResource(R.drawable.lyrics),
                        title = { Text("Player Architecture") },
                        description = { Text("Apple Player signature design alongside 8 ArchiveTune styles") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_player_style")
                            navController.navigate("settings/appearance")
                        }
                    ),
                    Material3SettingsItem(
                        key = "appearance_apple_player",
                        icon = painterResource(R.drawable.nav_bar),
                        title = { Text("Mini-Player Style") },
                        description = { Text("Select Classic bar, New expressive pill, or Apple capsule") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_apple_player")
                            navController.navigate("settings/appearance")
                        }
                    ),
                    Material3SettingsItem(
                        key = "appearance_liquid_mini_player",
                        icon = painterResource(R.drawable.sparks),
                        title = { Text("Liquid Mini-Player") },
                        description = { Text("Fluid organic water progress animation on mini-player") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_liquid_mini_player")
                            navController.navigate("settings/appearance")
                        }
                    ),
                    Material3SettingsItem(
                        key = "appearance_mini_player_outline",
                        icon = painterResource(R.drawable.contrast),
                        title = { Text("Mini-Player Outline & Pure Black") },
                        description = { Text("Ambient border outline and OLED pitch black background") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_pure_black")
                            navController.navigate("settings/appearance")
                        }
                    )
                )
            )

            // Section 3 — Gestures & Performance
            ExpressiveSettingGroup(
                title = "Gestures & Performance",
                itemMinHeight = 64.dp,
                items = listOf(
                    Material3SettingsItem(
                        key = "appearance_swipe_thumbnail",
                        icon = painterResource(R.drawable.swipe),
                        title = { Text("Mini-Player Swipe Gestures") },
                        description = { Text("Interactive directional skip feedback with real-time haptics") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_swipe_thumbnail")
                            navController.navigate("settings/appearance")
                        }
                    ),
                    Material3SettingsItem(
                        key = "appearance_interface_animations",
                        icon = painterResource(R.drawable.speed),
                        title = { Text("Interface Animations") },
                        description = { Text("Enable smooth transitions, liquid wave motion, and effects") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_interface_animations")
                            navController.navigate("settings/appearance")
                        }
                    ),
                    Material3SettingsItem(
                        key = "appearance_high_refresh_rate",
                        icon = painterResource(R.drawable.speed),
                        title = { Text("High Refresh Rate") },
                        description = { Text("Smooth 90Hz / 120Hz display refresh rate optimization") },
                        onClick = {
                            SettingsHighlightManager.requestHighlight("appearance_high_refresh_rate")
                            navController.navigate("settings/appearance")
                        }
                    )
                )
            )

            // Section 4 — Playback & Audio
            ExpressiveSettingGroup(
                title = "Playback & Audio",
                itemMinHeight = 64.dp,
                items = listOf(
                    Material3SettingsItem(
                        key = "player_audio_quality",
                        icon = painterResource(R.drawable.graphic_eq),
                        title = { Text(stringResource(R.string.audio_quality)) },
                        description = { Text("Opus streaming bitrate, audio offload, and sound settings") },
                        onClick = { navController.navigate("settings/player") }
                    ),
                    Material3SettingsItem(
                        key = "player_equalizer",
                        icon = painterResource(R.drawable.mkrequlizer),
                        title = { Text("Axion Equalizer & Bass Boost") },
                        description = { Text("Parametric EQ, Dolby Atmos and Dirac sound enhancement") },
                        onClick = { navController.navigate("settings/equalizer") }
                    ),
                    Material3SettingsItem(
                        key = "player_sponsorblock",
                        icon = painterResource(R.drawable.fast_forward),
                        title = { Text("SponsorBlock") },
                        description = { Text("Skip non-music video scenes and sponsorships automatically") },
                        onClick = { navController.navigate("settings/player/sponsorblock") }
                    )
                )
            )

            // Section 5 — Content & Lyrics
            ExpressiveSettingGroup(
                title = "Content & Lyrics",
                itemMinHeight = 64.dp,
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
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
private fun SettingsGridCard(
    modifier: Modifier = Modifier,
    key: String? = null,
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .settingsHighlight(key, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.navigate_next),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    minLines = 2,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
