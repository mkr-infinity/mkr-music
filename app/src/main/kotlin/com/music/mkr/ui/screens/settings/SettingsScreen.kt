/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import com.music.mkr.R
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState

private data class SearchableSetting(
    val title: String,
    val description: String,
    val category: String,
    val iconRes: Int,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val autoUpdateSetting = remember { getAutoUpdateCheckSetting(context) }
    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
    var searchQuery by remember { mutableStateOf("") }

    val searchableSettings = remember {
        listOf(
            SearchableSetting(
                title = "System Update",
                description = "Check for MKR Music updates and downloads",
                category = "Account & Updates",
                iconRes = R.drawable.system_update_uptodate,
                route = "settings/update"
            ),
            SearchableSetting(
                title = "Account",
                description = "Google account integration and sync",
                category = "Account & Updates",
                iconRes = R.drawable.google,
                route = "settings/account"
            ),
            SearchableSetting(
                title = "Appearance",
                description = "Dynamic themes, accent colors, custom app icons, floating navbar",
                category = "Media & Player Experience",
                iconRes = R.drawable.palette,
                route = "settings/appearance"
            ),
            SearchableSetting(
                title = "Player & Audio",
                description = "Equalizer, crossfade, silence skipping, audio cache",
                category = "Media & Player Experience",
                iconRes = R.drawable.earbud_case,
                route = "settings/player"
            ),
            SearchableSetting(
                title = "Listen Together",
                description = "Synchronized live group listening sessions with friends",
                category = "Media & Player Experience",
                iconRes = R.drawable.group,
                route = Screens.ListenTogether.route
            ),
            SearchableSetting(
                title = "Content",
                description = "Content language, lyrics providers, LRCLIB",
                category = "Media & Player Experience",
                iconRes = R.drawable.language,
                route = "settings/content"
            ),
            SearchableSetting(
                title = "AI Lyrics Translation",
                description = "Translate song lyrics using AI engines",
                category = "Features & Data",
                iconRes = R.drawable.translate,
                route = "settings/ai"
            ),
            SearchableSetting(
                title = "Privacy",
                description = "Playback history, search logs, telemetry options",
                category = "Features & Data",
                iconRes = R.drawable.security,
                route = "settings/privacy"
            ),
            SearchableSetting(
                title = "Listening Summary",
                description = "Weekly listening stats, musical moods recap",
                category = "Features & Data",
                iconRes = R.drawable.history,
                route = "settings/listening_summary"
            ),
            SearchableSetting(
                title = "Storage & Cache",
                description = "Cached music, offline files, cache limit, purge",
                category = "Features & Data",
                iconRes = R.drawable.storage,
                route = "settings/storage"
            ),
            SearchableSetting(
                title = "Backup & Restore",
                description = "Export playlists, app settings, import backups",
                category = "System & Support",
                iconRes = R.drawable.restore,
                route = "settings/backup_restore"
            ),
            SearchableSetting(
                title = "About MKR Music",
                description = "Version v6.0.8 Stable, developer info, GitHub repository",
                category = "System & Support",
                iconRes = R.drawable.info,
                route = "settings/about"
            )
        )
    }

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
        )

        // Capsule Search Bar
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
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )

        if (searchQuery.isNotBlank()) {
            val filtered = remember(searchQuery) {
                val query = searchQuery.trim().lowercase()
                searchableSettings.filter {
                    it.title.lowercase().contains(query) ||
                    it.description.lowercase().contains(query) ||
                    it.category.lowercase().contains(query)
                }
            }

            if (filtered.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching settings found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Search Results (${filtered.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                ExpressiveSettingGroup(
                    items = filtered.map { item ->
                        Material3SettingsItem(
                            icon = painterResource(item.iconRes),
                            title = { Text(item.title) },
                            description = { Text(item.description) },
                            onClick = { navController.navigate(item.route) }
                        )
                    }
                )
            }
        } else {
            // MKR Music Hero Header Card (Capsule Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("settings/about") },
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 3.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MKR Music",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "v6.0.8 Stable",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Crafted by @mkr-infinity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.navigate_next),
                        contentDescription = "About",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Discovery Grid - Quick Capsule Hubs (no emojis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiscoveryCapsuleCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.palette,
                    title = "Appearance",
                    subtitle = "Theme & Icons",
                    onClick = { navController.navigate("settings/appearance") }
                )
                DiscoveryCapsuleCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.earbud_case,
                    title = "Player & Audio",
                    subtitle = "Equalizer & Sound",
                    onClick = { navController.navigate("settings/player") }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiscoveryCapsuleCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.storage,
                    title = "Storage",
                    subtitle = "Cache & Offline",
                    onClick = { navController.navigate("settings/storage") }
                )
                DiscoveryCapsuleCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.history,
                    title = "Summary",
                    subtitle = "Stats & Moods",
                    onClick = { navController.navigate("settings/listening_summary") }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Group 1: Important / Account
            ExpressiveSettingGroup(
                title = "Account & Updates",
                itemMinHeight = 64.dp,
                items = buildList {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(if (isUpdateAvailable) R.drawable.update_alert else R.drawable.system_update_uptodate),
                            title = { Text(stringResource(R.string.system_update)) },
                            description = {
                                if (isUpdateAvailable) {
                                    Text(
                                        text = stringResource(R.string.update_available),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(stringResource(R.string.app_update_uptodate))
                                }
                            },
                            onClick = { navController.navigate("settings/update") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.google),
                            title = { Text(stringResource(R.string.account)) },
                            description = { Text(stringResource(R.string.setting_account_desc)) },
                            onClick = { navController.navigate("settings/account") }
                        )
                    )
                }
            )

            // Group 2: Media & Player Experience
            ExpressiveSettingGroup(
                itemMinHeight = 64.dp,
                items = buildList {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.palette),
                            title = { Text(stringResource(R.string.appearance)) },
                            description = { Text(stringResource(R.string.setting_appearance_desc)) },
                            onClick = { navController.navigate("settings/appearance") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.earbud_case),
                            title = { Text(stringResource(R.string.player_and_audio)) },
                            description = { Text(stringResource(R.string.setting_player_desc)) },
                            onClick = { navController.navigate("settings/player") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.group),
                            title = { Text(stringResource(R.string.listen_together)) },
                            description = { Text(stringResource(R.string.setting_listen_together_desc)) },
                            onClick = { navController.navigate(Screens.ListenTogether.route) }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.language),
                            title = { Text(stringResource(R.string.content)) },
                            description = { Text(stringResource(R.string.setting_content_desc)) },
                            onClick = { navController.navigate("settings/content") }
                        )
                    )
                }
            )

            // Group 3: Features & Data
            ExpressiveSettingGroup(
                itemMinHeight = 64.dp,
                items = buildList {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.translate),
                            title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                            description = { Text(stringResource(R.string.setting_ai_lyrics_translation_desc)) },
                            onClick = { navController.navigate("settings/ai") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.security),
                            title = { Text(stringResource(R.string.privacy)) },
                            description = { Text(stringResource(R.string.setting_privacy_desc)) },
                            onClick = { navController.navigate("settings/privacy") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.history),
                            title = { Text(stringResource(R.string.listening_summary)) },
                            description = { Text(stringResource(R.string.setting_listening_summary_desc)) },
                            onClick = { navController.navigate("settings/listening_summary") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.storage),
                            title = { Text(stringResource(R.string.storage)) },
                            description = { Text(stringResource(R.string.setting_storage_desc)) },
                            onClick = { navController.navigate("settings/storage") }
                        )
                    )
                }
            )

            // Group 4: System & Support
            ExpressiveSettingGroup(
                itemMinHeight = 64.dp,
                items = buildList {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.restore),
                            title = { Text(stringResource(R.string.backup_restore)) },
                            description = { Text(stringResource(R.string.setting_backup_restore_desc)) },
                            onClick = { navController.navigate("settings/backup_restore") }
                        )
                    )
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.info),
                            title = { Text(stringResource(R.string.about)) },
                            description = { Text(stringResource(R.string.setting_about_desc)) },
                            onClick = { navController.navigate("settings/about") }
                        )
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun DiscoveryCapsuleCard(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
