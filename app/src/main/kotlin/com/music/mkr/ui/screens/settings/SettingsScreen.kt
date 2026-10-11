/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import com.music.mkr.R
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.utils.backToMain
import androidx.compose.ui.platform.LocalUriHandler
import com.music.mkr.ui.utils.safeOpenUri
import com.music.mkr.BuildConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState

private data class SearchableSetting(
    val title: String,
    val description: String,
    val category: String,
    val iconRes: Int,
    val route: String,
    val targetKey: String,
    val categoryKey: String? = null,
    val keywords: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val autoUpdateSetting = remember { getAutoUpdateCheckSetting(context) }
    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
    var searchQuery by remember { mutableStateOf("") }

    val searchableSettings = remember {
        listOf(
            // --- Appearance & Theming ---
            SearchableSetting(
                title = "App Icon Gallery",
                description = "Choose dynamic launcher icons to customize your home screen",
                category = "Appearance",
                iconRes = R.drawable.palette,
                route = "settings/appearance",
                targetKey = "appearance_icon_gallery",
                keywords = listOf("launcher", "icon", "logo", "adaptive", "aesthetic", "homescreen", "gallery")
            ),
            SearchableSetting(
                title = "Themes & Accent Colors",
                description = "Dynamic Material You colors, palettes, and pure dark mode",
                category = "Appearance",
                iconRes = R.drawable.palette,
                route = "settings/appearance",
                targetKey = "appearance_theme_palette",
                keywords = listOf("monet", "color", "accent", "dynamic theme", "palette", "custom color", "dark mode")
            ),
            SearchableSetting(
                title = "Pure Black Mini Player",
                description = "OLED pitch black background for the player and miniplayer",
                category = "Appearance",
                iconRes = R.drawable.contrast,
                route = "settings/appearance",
                targetKey = "appearance_pure_black",
                keywords = listOf("oled", "amoled", "black", "pure black", "miniplayer", "dark")
            ),
            SearchableSetting(
                title = "Floating Navigation Bar",
                description = "Floating pill-shaped navigation bar with smooth blur effect",
                category = "Appearance",
                iconRes = R.drawable.nav_bar,
                route = "settings/appearance",
                targetKey = "appearance_floating_nav",
                keywords = listOf("navbar", "navigation", "floating", "bottom bar", "pill", "dock")
            ),
            SearchableSetting(
                title = "Player Seekbar & Sliders",
                description = "Animated wavy and squiggly player seekbar sliders",
                category = "Appearance",
                iconRes = R.drawable.linear_scale,
                route = "settings/appearance",
                targetKey = "appearance_slider_style",
                keywords = listOf("wavy", "squiggly", "slider", "seekbar", "progress", "wave", "track")
            ),
            SearchableSetting(
                title = "Apple-Style Player Design",
                description = "Fluid gestures, apple miniplayer layout and modern typography",
                category = "Appearance",
                iconRes = R.drawable.lyrics_apple,
                route = "settings/appearance",
                targetKey = "appearance_apple_player",
                keywords = listOf("apple", "player design", "miniplayer", "fluid", "gestures", "ios")
            ),
            SearchableSetting(
                title = "Animated & Liquid Lyrics",
                description = "Real-time glowing liquid lyrics animation with synchronized rhythm",
                category = "Appearance",
                iconRes = R.drawable.lyrics,
                route = "settings/appearance",
                targetKey = "appearance_lyrics_animation",
                keywords = listOf("liquid", "lyrics animation", "glow", "animated lyrics", "karaoke", "blur")
            ),

            // --- Player & Audio Engine ---
            SearchableSetting(
                title = "Streaming Audio Quality",
                description = "High-fidelity JioSaavn streaming (320kbps) & YouTube Music fallback",
                category = "Player & Audio",
                iconRes = R.drawable.graphic_eq,
                route = "settings/player",
                targetKey = "player_audio_quality",
                keywords = listOf("bitrate", "high quality", "lossless", "audio quality", "320kbps", "sound quality", "saavn", "streaming")
            ),
            SearchableSetting(
                title = "Crossfade Between Songs",
                description = "Seamless DJ-style crossfade transitions and custom duration curves",
                category = "Player & Audio",
                iconRes = R.drawable.linear_scale,
                route = "settings/player",
                targetKey = "player_crossfade",
                keywords = listOf("crossfade", "fade", "transition", "gapless", "mix", "dj", "duration")
            ),
            SearchableSetting(
                title = "Skip Silence In Audio",
                description = "Automatically skip silent gaps between songs instantly",
                category = "Player & Audio",
                iconRes = R.drawable.fast_forward,
                route = "settings/player",
                targetKey = "player_skip_silence",
                keywords = listOf("skip silence", "silence", "quiet", "instant skip", "gaps", "fast forward")
            ),
            SearchableSetting(
                title = "Axion Equalizer & Bass Boost",
                description = "Parametric equalizer, bass boost & hardware loudness enhancement",
                category = "Player & Audio",
                iconRes = R.drawable.mkrequlizer,
                route = "settings/player",
                targetKey = "player_equalizer",
                keywords = listOf("equalizer", "eq", "bass", "loudness", "bass boost", "dsp", "sound", "volume")
            ),
            SearchableSetting(
                title = "SponsorBlock Integration",
                description = "Skip sponsored segments, intros, music video sketches automatically",
                category = "Player & Audio",
                iconRes = R.drawable.fast_forward,
                route = "settings/player",
                targetKey = "player_sponsorblock",
                keywords = listOf("sponsorblock", "sponsor", "skip ads", "intro", "outro", "video skip")
            ),
            SearchableSetting(
                title = "Persistent Playback Queue",
                description = "Preserve current listening queue across app restarts and app clears",
                category = "Player & Audio",
                iconRes = R.drawable.queue_music,
                route = "settings/player",
                targetKey = "player_persistent_queue",
                keywords = listOf("queue", "persistent queue", "resume queue", "save queue", "tracks")
            ),
            SearchableSetting(
                title = "Resume on Bluetooth Connect",
                description = "Automatically resume audio playback when headphones or car connect",
                category = "Player & Audio",
                iconRes = R.drawable.earbud_case,
                route = "settings/player",
                targetKey = "player_bluetooth_resume",
                keywords = listOf("bluetooth", "headphones", "auto play", "connect", "car audio")
            ),

            // --- Content, Lyrics & AI ---
            SearchableSetting(
                title = "Content Language & Location",
                description = "Customize recommendations, trending charts, and discovery language",
                category = "Content",
                iconRes = R.drawable.language,
                route = "settings/content",
                targetKey = "content_language",
                keywords = listOf("language", "country", "region", "charts", "recommendations", "content")
            ),
            SearchableSetting(
                title = "Lyrics Providers & LRCLIB",
                description = "Real-time synchronized lyrics engine, romanization, and translations",
                category = "Content",
                iconRes = R.drawable.lyrics,
                route = "settings/content",
                targetKey = "content_lyrics",
                keywords = listOf("lrclib", "lyrics", "synced lyrics", "subtitles", "lyrics provider")
            ),
            SearchableSetting(
                title = "AI Lyrics Translation",
                description = "Translate song lyrics on-the-fly using intelligent AI models",
                category = "Content",
                iconRes = R.drawable.translate,
                route = "settings/ai",
                targetKey = "content_ai_lyrics",
                keywords = listOf("ai", "translation", "translator", "ai lyrics", "translate")
            ),
            SearchableSetting(
                title = "Explicit Content Filter",
                description = "Toggle filtering and playback of explicit or mature audio tracks",
                category = "Content",
                iconRes = R.drawable.security,
                route = "settings/content",
                targetKey = "content_explicit",
                keywords = listOf("explicit", "clean", "mature", "curse", "parental filter")
            ),

            // --- Storage & Cache ---
            SearchableSetting(
                title = "Disable Song Caching",
                description = "Play songs directly without saving to offline cache storage",
                category = "Storage & Cache",
                iconRes = R.drawable.cached,
                route = "settings/storage",
                targetKey = "storage_disable_song_cache",
                keywords = listOf("disable cache", "cache", "offline cache", "storage", "no cache", "clear cache")
            ),
            SearchableSetting(
                title = "Audio Cache Size Limit",
                description = "Configurable offline memory buffer up to 10GB for instant playback",
                category = "Storage & Cache",
                iconRes = R.drawable.storage,
                route = "settings/storage",
                targetKey = "storage_cache_limit",
                keywords = listOf("cache", "song cache", "buffer", "10gb", "cache size", "memory")
            ),
            SearchableSetting(
                title = "Offline Downloads Library",
                description = "Manage downloaded songs, storage location, and unmetered play",
                category = "Storage & Cache",
                iconRes = R.drawable.download,
                route = "settings/storage",
                targetKey = "storage_downloads",
                keywords = listOf("download", "offline", "saved tracks", "offline music", "downloads")
            ),
            SearchableSetting(
                title = "Clear Cache & Temporary Files",
                description = "Purge cached album artwork and audio chunks to reclaim free space",
                category = "Storage & Cache",
                iconRes = R.drawable.clear_all,
                route = "settings/storage",
                targetKey = "storage_clear_cache",
                keywords = listOf("clear cache", "purge", "free space", "delete cache", "cleanup")
            ),

            // --- Privacy & Community ---
            SearchableSetting(
                title = "Playback History Tracking",
                description = "Pause or enable tracking of listened songs and music history",
                category = "Privacy",
                iconRes = R.drawable.history,
                route = "settings/privacy",
                targetKey = "privacy_history",
                keywords = listOf("history", "listening history", "recently played", "pause history")
            ),
            SearchableSetting(
                title = "Search History Logs",
                description = "Manage search query suggestions and clear previous searches",
                category = "Privacy",
                iconRes = R.drawable.search,
                route = "settings/privacy",
                targetKey = "privacy_search_history",
                keywords = listOf("search history", "queries", "clear search", "recent searches")
            ),
            SearchableSetting(
                title = "Listening Summary & Recap",
                description = "Weekly recap stats, top artists, top genres, and musical moods",
                category = "Features & Data",
                iconRes = R.drawable.history,
                route = "settings/listening_summary",
                targetKey = "privacy_listening_summary",
                keywords = listOf("recap", "summary", "stats", "weekly stats", "genres", "artists")
            ),
            SearchableSetting(
                title = "Discord Rich Presence",
                description = "Display live playing song, artist, and album art on Discord status",
                category = "Integrations",
                iconRes = R.drawable.discord,
                route = "settings/integrations",
                targetKey = "privacy_discord_rpc",
                keywords = listOf("discord", "rpc", "rich presence", "status", "listening on discord")
            ),
            SearchableSetting(
                title = "Listen Together",
                description = "Synchronized live group listening sessions with friends",
                category = "Media & Player Experience",
                iconRes = R.drawable.group,
                route = Screens.ListenTogether.route,
                targetKey = "screen_listen_together",
                keywords = listOf("listen together", "party", "group session", "sync listen", "friends")
            ),

            // --- Account & Updates ---
            SearchableSetting(
                title = "Google & YouTube Music Sync",
                description = "Sync your liked songs, playlists, and subscriptions seamlessly",
                category = "Account",
                iconRes = R.drawable.google,
                route = "settings/account",
                targetKey = "account_login",
                keywords = listOf("google", "account", "login", "youtube music", "sync", "playlists")
            ),
            SearchableSetting(
                title = "Check for System Updates",
                description = "Check for MKR Music new releases and install latest universal APK",
                category = "Updates",
                iconRes = R.drawable.system_update_uptodate,
                route = "settings/update",
                targetKey = "update_check",
                keywords = listOf("update", "upgrade", "check update", "new version", "apk", "system update")
            ),
            SearchableSetting(
                title = "Automatic Update Check",
                description = "Automatically check for updates and notify when a new release is available",
                category = "Updates",
                iconRes = R.drawable.system_update_uptodate,
                route = "settings/update",
                targetKey = "update_auto_check",
                keywords = listOf("auto update", "notify", "update check", "background check")
            ),
            SearchableSetting(
                title = "Changelog & Release Notes",
                description = "Read full version history and what's new in latest MKR Music releases",
                category = "Updates",
                iconRes = R.drawable.changelog_paper,
                route = "settings/changelog",
                targetKey = "update_changelog",
                keywords = listOf("changelog", "release notes", "what's new", "version history")
            ),

            // --- System & Support ---
            SearchableSetting(
                title = "Backup & Restore",
                description = "Export and import your playlists, preferences, and database backups",
                category = "System & Support",
                iconRes = R.drawable.restore,
                route = "settings/backup_restore",
                targetKey = "backup_export_import",
                keywords = listOf("backup", "restore", "export", "import", "data", "playlists")
            ),
            SearchableSetting(
                title = "About",
                description = "Version v${BuildConfig.VERSION_NAME}, developer credits, licenses & GitHub repo",
                category = "System & Support",
                iconRes = R.drawable.info,
                route = "settings/about",
                targetKey = "about_app",
                keywords = listOf("about", "version", "developer", "mkr", "github", "source code")
            ),
            SearchableSetting(
                title = "Support Developer / Buy Me a Coffee",
                description = "Support ongoing development of MKR Music directly",
                category = "System & Support",
                iconRes = R.drawable.buymeacoffee,
                route = "settings/about",
                targetKey = "about_support",
                keywords = listOf("support", "coffee", "buymeacoffee", "donate", "contribute")
            ),
            SearchableSetting(
                title = "Request a Feature",
                description = "Propose new ideas & enhancements on GitHub",
                category = "Feedback & Community",
                iconRes = R.drawable.support_mkr,
                route = "settings",
                targetKey = "support_request_feature",
                keywords = listOf("feature", "request", "idea", "suggestion", "github", "proposal")
            ),
            SearchableSetting(
                title = "Report a Problem",
                description = "Submit issue report with device & version details on GitHub",
                category = "Feedback & Community",
                iconRes = R.drawable.bug_report,
                route = "settings",
                targetKey = "support_report_problem",
                keywords = listOf("bug", "issue", "problem", "error", "crash", "report", "github")
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Search Bar (Capsule Style)
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
            modifier = Modifier.fillMaxWidth()
        )

        if (searchQuery.isNotBlank()) {
            val filtered = remember(searchQuery) {
                val query = searchQuery.trim().lowercase()
                searchableSettings.filter {
                    it.title.lowercase().contains(query) ||
                    it.description.lowercase().contains(query) ||
                    it.category.lowercase().contains(query) ||
                    it.keywords.any { kw -> kw.lowercase().contains(query) }
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
                        horizontalAlignment = Alignment.CenterHorizontally
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
                            description = { Text("${item.category} • ${item.description}") },
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
            // Section 2 — Four settings grid cards (adaptive 2-by-2 grid with fixed height alignment)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).height(126.dp),
                        key = "appearance_theme_palette",
                        icon = R.drawable.palette,
                        title = "Appearance",
                        subtitle = "Themes, accents & launcher icons",
                        onClick = { navController.navigate("settings/appearance") }
                    )
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).height(126.dp),
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
                        modifier = Modifier.weight(1f).height(126.dp),
                        key = "storage_downloads",
                        icon = R.drawable.storage,
                        title = "Storage & Cache",
                        subtitle = "Offline tracks & cache budgets",
                        onClick = { navController.navigate("settings/storage") }
                    )
                    SettingsGridCard(
                        modifier = Modifier.weight(1f).height(126.dp),
                        key = "account_login",
                        icon = R.drawable.google,
                        title = "Account & Sync",
                        subtitle = "Sync playlists & library",
                        onClick = { navController.navigate("settings/account") }
                    )
                }
            }

            // Section 3 — Features & Updates
            ExpressiveSettingGroup(
                title = "Features & Updates",
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
                        key = "privacy_listening_summary",
                        icon = painterResource(R.drawable.history),
                        title = { Text("Listening Recap") },
                        description = { Text("Weekly listening stats, top artists, moods & highlights") },
                        onClick = { navController.navigate("settings/listening_summary") }
                    ),
                    Material3SettingsItem(
                        key = "screen_listen_together",
                        icon = painterResource(R.drawable.group),
                        title = { Text("Listen Together") },
                        description = { Text("Synchronized live group listening sessions with friends") },
                        onClick = { navController.navigate(Screens.ListenTogether.route) }
                    )
                )
            )

            // Section 4 — Content & Lyrics
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

            // Section 5 — Security & Data
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

            // Section 6 — Feedback & Community
            ExpressiveSettingGroup(
                title = "Feedback & Community",
                itemMinHeight = 64.dp,
                items = listOf(
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

                                ### Steps to Reproduce
                                1.
                                2.
                                3.

                                ### Expected Behavior
                                What should have happened.

                                ### Actual Behavior
                                What actually happened.
                            """.trimIndent()
                            val url = "https://github.com/mkr-infinity/mkr-music/issues/new?title=" +
                                URLEncoder.encode(bugTitle, StandardCharsets.UTF_8.name()) +
                                "&body=" + URLEncoder.encode(bugBody, StandardCharsets.UTF_8.name())
                            uriHandler.safeOpenUri(context, url)
                        },
                        isExternalLink = true
                    )
                )
            )

            // Section 7 — About
            ExpressiveSettingGroup(
                title = "About",
                itemMinHeight = 64.dp,
                items = listOf(
                    Material3SettingsItem(
                        key = "about_app",
                        icon = painterResource(R.drawable.info),
                        title = { Text("About") },
                        description = { Text("About and information") },
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
        },
        actions = {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { navController.navigate("settings/about") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
            .settingsHighlight(key, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 13.dp),
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
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
