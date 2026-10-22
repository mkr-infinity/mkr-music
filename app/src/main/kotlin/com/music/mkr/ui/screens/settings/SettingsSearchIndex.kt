/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import android.content.Context
import androidx.annotation.DrawableRes
import com.music.mkr.BuildConfig
import com.music.mkr.R
import com.music.mkr.ui.screens.Screens

/**
 * Metadata representing an indexed setting item for full-text multi-word token search.
 */
data class SearchableSettingItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    @DrawableRes val iconRes: Int,
    val route: String,
    val targetKey: String,
    val categoryKey: String? = null,
    val keywords: List<String> = emptyList()
)

/**
 * Builds the complete search catalog across all MKR Music configuration categories.
 */
fun buildSettingsSearchCatalog(context: Context): List<SearchableSettingItem> {
    return listOf(
        // ====================================================================
        // 1. APPEARANCE & THEMES
        // ====================================================================
        SearchableSettingItem(
            id = "appearance_theme_palette",
            title = "Themes & Accent Colors",
            description = "Dynamic Material You colors, amber accents, and pure black OLED mode",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_theme_palette",
            keywords = listOf("theme", "color", "accent", "amber", "dynamic theme", "monet", "palette", "dark mode", "light mode", "pure black", "oled", "contrast")
        ),
        SearchableSettingItem(
            id = "appearance_icon_gallery",
            title = "App Icon Gallery",
            description = "Choose dynamic launcher icons to customize your home screen",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_icon_gallery",
            keywords = listOf("launcher", "icon", "logo", "adaptive", "aesthetic", "homescreen", "gallery", "app icon", "badge")
        ),
        SearchableSettingItem(
            id = "appearance_navigation_style",
            title = "Navigation Bar Style",
            description = "Telegram glass capsule, expressive pill, or material dock navigation",
            category = "Appearance & Themes",
            iconRes = R.drawable.nav_bar,
            route = "settings/appearance",
            targetKey = "appearance_navigation_style",
            keywords = listOf("navbar", "navigation", "style", "glass", "capsule", "dock", "bottom bar", "floating", "telegram")
        ),
        SearchableSettingItem(
            id = "appearance_floating_nav",
            title = "Floating Navigation Bar",
            description = "Floating pill-shaped navigation bar with smooth glass blur effect",
            category = "Appearance & Themes",
            iconRes = R.drawable.nav_bar,
            route = "settings/appearance",
            targetKey = "appearance_floating_nav",
            keywords = listOf("navbar", "navigation", "floating", "bottom bar", "pill", "dock", "blur")
        ),
        SearchableSettingItem(
            id = "appearance_slider_style",
            title = "Player Seekbar & Sliders",
            description = "Animated wavy, squiggly, slim, or expressive player seekbar sliders",
            category = "Appearance & Themes",
            iconRes = R.drawable.linear_scale,
            route = "settings/appearance",
            targetKey = "appearance_slider_style",
            keywords = listOf("wavy", "squiggly", "slider", "seekbar", "progress", "wave", "track", "slider style")
        ),
        SearchableSettingItem(
            id = "appearance_custom_font",
            title = "Typography & Font Selection",
            description = "Google Sans, Sans Flex, Outfit, Plus Jakarta Sans, or custom TTF/OTF font",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance/font",
            targetKey = "appearance_font",
            keywords = listOf("font", "typography", "text", "google sans", "outfit", "typeface", "custom font")
        ),
        SearchableSettingItem(
            id = "appearance_display_density",
            title = "Display Density Scale",
            description = "Adjust UI element scale and compact spacing across all screens",
            category = "Appearance & Themes",
            iconRes = R.drawable.grid_view,
            route = "settings/appearance",
            targetKey = "appearance_density",
            keywords = listOf("density", "scale", "compact", "size", "zoom", "ui scale", "dpi")
        ),

        // ====================================================================
        // 2. PLAYER STYLES & MINI-PLAYER
        // ====================================================================
        SearchableSettingItem(
            id = "player_style_architecture",
            title = "Player Style Architecture",
            description = "Select from 9 player styles: Apple Player or 8 ArchiveTune designs",
            category = "Player & Mini-Player",
            iconRes = R.drawable.lyrics,
            route = "settings/appearance",
            targetKey = "appearance_player_style",
            keywords = listOf("player style", "apple", "classic", "modern", "minimal", "cinematic", "immersive", "material extended", "editorial", "architectures", "now playing", "player design")
        ),
        SearchableSettingItem(
            id = "mini_player_design",
            title = "Mini-Player Style",
            description = "Select mini-player design: Classic bar, New expressive pill, or Apple capsule",
            category = "Player & Mini-Player",
            iconRes = R.drawable.nav_bar,
            route = "settings/appearance",
            targetKey = "appearance_apple_player",
            keywords = listOf("mini player", "miniplayer", "compact player", "bottom player", "apple mini player", "new mini player", "classic mini player", "capsule", "pill")
        ),
        SearchableSettingItem(
            id = "appearance_liquid_mini_player",
            title = "Liquid Mini-Player",
            description = "Fill the mini-player with an organic fluid water animation as music plays",
            category = "Player & Mini-Player",
            iconRes = R.drawable.sparks,
            route = "settings/appearance",
            targetKey = "appearance_liquid_mini_player",
            keywords = listOf("liquid", "water", "fluid", "wave", "waves", "fill", "animation", "miniplayer", "progress", "ripple", "undulating", "liquid mini player")
        ),
        SearchableSettingItem(
            id = "appearance_mini_player_outline",
            title = "Mini-Player Outline",
            description = "Display subtle ambient outline border around the mini-player container",
            category = "Player & Mini-Player",
            iconRes = R.drawable.contrast,
            route = "settings/appearance",
            targetKey = "appearance_mini_player_outline",
            keywords = listOf("outline", "border", "stroke", "miniplayer", "ambient", "edge")
        ),
        SearchableSettingItem(
            id = "appearance_pure_black",
            title = "Pure Black Mini-Player",
            description = "Pitch black OLED surface for mini-player and navigation bar",
            category = "Player & Mini-Player",
            iconRes = R.drawable.contrast,
            route = "settings/appearance",
            targetKey = "appearance_pure_black",
            keywords = listOf("oled", "amoled", "black", "pure black", "miniplayer", "pitch black")
        ),
        SearchableSettingItem(
            id = "appearance_player_background",
            title = "Player Background Effects",
            description = "Dynamic gradient, Gaussian blur, live animated mesh, or solid surface",
            category = "Player & Mini-Player",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_player_background",
            keywords = listOf("background", "gradient", "blur", "mesh", "live mesh", "glow", "artwork background")
        ),
        SearchableSettingItem(
            id = "appearance_hide_thumbnail",
            title = "Hide Player Artwork Thumbnail",
            description = "Hide the square album cover artwork in full player view",
            category = "Player & Mini-Player",
            iconRes = R.drawable.hide_image,
            route = "settings/appearance",
            targetKey = "appearance_hide_thumbnail",
            keywords = listOf("thumbnail", "album art", "cover", "hide image", "artwork")
        ),
        SearchableSettingItem(
            id = "appearance_thumbnail_radius",
            title = "Artwork Corner Radius & Shadow",
            description = "Adjust player album art rounded corners and shadow elevation",
            category = "Player & Mini-Player",
            iconRes = R.drawable.crop,
            route = "settings/appearance",
            targetKey = "appearance_thumbnail_radius",
            keywords = listOf("corner radius", "shadow", "elevation", "round", "artwork", "thumbnail")
        ),

        // ====================================================================
        // 3. GESTURES & INTERACTION
        // ====================================================================
        SearchableSettingItem(
            id = "gestures_swipe_track",
            title = "Mini-Player Directional Swipe",
            description = "Real-time swipe left for next track and swipe right for previous track",
            category = "Gestures & Interaction",
            iconRes = R.drawable.swipe,
            route = "settings/appearance",
            targetKey = "appearance_swipe_thumbnail",
            keywords = listOf("swipe", "gesture", "next track", "previous track", "directional", "drag", "threshold", "skip", "track change", "miniplayer swipe")
        ),
        SearchableSettingItem(
            id = "gestures_swipe_sensitivity",
            title = "Swipe Sensitivity Threshold",
            description = "Calibrate drag distance and velocity threshold for track skip gestures",
            category = "Gestures & Interaction",
            iconRes = R.drawable.swipe,
            route = "settings/appearance",
            targetKey = "appearance_swipe_sensitivity",
            keywords = listOf("sensitivity", "swipe", "threshold", "velocity", "drag distance", "calibrate")
        ),
        SearchableSettingItem(
            id = "gestures_swipe_to_song",
            title = "Swipe Song to Enqueue",
            description = "Quick swipe right on list items to add track to play next",
            category = "Gestures & Interaction",
            iconRes = R.drawable.swipe,
            route = "settings/appearance",
            targetKey = "appearance_swipe_to_song",
            keywords = listOf("swipe to song", "enqueue", "add to queue", "play next", "list swipe")
        ),
        SearchableSettingItem(
            id = "gestures_swipe_to_remove",
            title = "Swipe Song to Remove",
            description = "Swipe left to quickly dismiss or delete track from queue",
            category = "Gestures & Interaction",
            iconRes = R.drawable.swipe,
            route = "settings/appearance",
            targetKey = "appearance_swipe_to_remove",
            keywords = listOf("swipe to remove", "delete", "dismiss", "remove from queue")
        ),

        // ====================================================================
        // 4. PERFORMANCE & ANIMATIONS
        // ====================================================================
        SearchableSettingItem(
            id = "appearance_interface_animations",
            title = "Interface Animations",
            description = "Enable smooth transitions, liquid wave motion, and interactive effects",
            category = "Performance & Animations",
            iconRes = R.drawable.speed,
            route = "settings/appearance",
            targetKey = "appearance_interface_animations",
            keywords = listOf("interface animations", "animation", "lag", "performance", "slow", "stutter", "freeze", "battery", "reduced motion", "effects", "smooth", "fps", "low power")
        ),
        SearchableSettingItem(
            id = "appearance_high_refresh_rate",
            title = "High Refresh Rate (90Hz / 120Hz)",
            description = "Force maximum screen refresh rate for ultra-smooth fluid scrolling",
            category = "Performance & Animations",
            iconRes = R.drawable.speed,
            route = "settings/appearance",
            targetKey = "appearance_high_refresh_rate",
            keywords = listOf("refresh rate", "120hz", "90hz", "fps", "smooth", "high refresh", "display")
        ),

        // ====================================================================
        // 5. PLAYBACK & AUDIO ENGINE
        // ====================================================================
        SearchableSettingItem(
            id = "player_audio_quality",
            title = "Streaming Audio Quality",
            description = "High-fidelity audio streaming, opus bitrate, and quality selection",
            category = "Playback & Audio",
            iconRes = R.drawable.graphic_eq,
            route = "settings/player",
            targetKey = "player_audio_quality",
            keywords = listOf("bitrate", "high quality", "lossless", "audio quality", "sound quality", "streaming", "opus", "hires")
        ),
        SearchableSettingItem(
            id = "player_crossfade",
            title = "Crossfade Between Songs",
            description = "Seamless DJ-style crossfade transitions and custom duration curves",
            category = "Playback & Audio",
            iconRes = R.drawable.linear_scale,
            route = "settings/player",
            targetKey = "player_crossfade",
            keywords = listOf("crossfade", "fade", "transition", "gapless", "mix", "dj", "duration")
        ),
        SearchableSettingItem(
            id = "player_skip_silence",
            title = "Skip Silence in Audio",
            description = "Automatically detect and skip silent gaps between songs instantly",
            category = "Playback & Audio",
            iconRes = R.drawable.fast_forward,
            route = "settings/player",
            targetKey = "player_skip_silence",
            keywords = listOf("skip silence", "silence", "quiet", "instant skip", "gaps", "fast forward")
        ),
        SearchableSettingItem(
            id = "player_equalizer",
            title = "Axion Equalizer & Bass Boost",
            description = "Parametric equalizer, bass boost, and Dolby Atmos audio processing",
            category = "Playback & Audio",
            iconRes = R.drawable.mkrequlizer,
            route = "settings/equalizer",
            targetKey = "player_equalizer",
            keywords = listOf("equalizer", "eq", "bass", "loudness", "bass boost", "dsp", "sound", "volume", "dolby", "dirac")
        ),
        SearchableSettingItem(
            id = "player_sponsorblock",
            title = "SponsorBlock Integration",
            description = "Skip sponsored segments, intros, and non-music sections automatically",
            category = "Playback & Audio",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "player_sponsorblock",
            keywords = listOf("sponsorblock", "sponsor", "skip ads", "intro", "outro", "video skip")
        ),
        SearchableSettingItem(
            id = "player_persistent_queue",
            title = "Persistent Playback Queue",
            description = "Preserve current listening queue across app restarts and task clears",
            category = "Playback & Audio",
            iconRes = R.drawable.queue_music,
            route = "settings/player",
            targetKey = "player_persistent_queue",
            keywords = listOf("queue", "persistent queue", "resume queue", "save queue", "tracks")
        ),
        SearchableSettingItem(
            id = "player_bluetooth_resume",
            title = "Resume on Bluetooth Connect",
            description = "Automatically resume audio playback when headphones or car connect",
            category = "Playback & Audio",
            iconRes = R.drawable.earbud_case,
            route = "settings/player",
            targetKey = "player_bluetooth_resume",
            keywords = listOf("bluetooth", "headphones", "auto play", "connect", "car audio", "resume")
        ),

        // ====================================================================
        // 6. CONTENT, LYRICS & AI
        // ====================================================================
        SearchableSettingItem(
            id = "content_language",
            title = "Content Language & Location",
            description = "Customize recommendations, trending charts, and discovery language",
            category = "Content & Lyrics",
            iconRes = R.drawable.language,
            route = "settings/content",
            targetKey = "content_language",
            keywords = listOf("language", "country", "region", "charts", "recommendations", "content", "locale")
        ),
        SearchableSettingItem(
            id = "content_lyrics",
            title = "Lyrics Providers & LRCLIB",
            description = "Real-time synchronized lyrics engine, romanization, and text sizing",
            category = "Content & Lyrics",
            iconRes = R.drawable.lyrics,
            route = "settings/content",
            targetKey = "content_lyrics",
            keywords = listOf("lrclib", "lyrics", "synced lyrics", "subtitles", "lyrics provider", "karaoke")
        ),
        SearchableSettingItem(
            id = "content_ai_lyrics",
            title = "AI Lyrics Translation",
            description = "Translate song lyrics on-the-fly using intelligent AI models",
            category = "Content & Lyrics",
            iconRes = R.drawable.translate,
            route = "settings/ai",
            targetKey = "content_ai_lyrics",
            keywords = listOf("ai", "translation", "translator", "ai lyrics", "translate", "gemini", "model")
        ),
        SearchableSettingItem(
            id = "content_explicit",
            title = "Explicit Content Filter",
            description = "Toggle filtering and playback of explicit or mature audio tracks",
            category = "Content & Lyrics",
            iconRes = R.drawable.security,
            route = "settings/content",
            targetKey = "content_explicit",
            keywords = listOf("explicit", "clean", "mature", "curse", "parental filter", "parental")
        ),

        // ====================================================================
        // 7. STORAGE & CACHE
        // ====================================================================
        SearchableSettingItem(
            id = "storage_cache_limit",
            title = "Audio Cache Size Limit",
            description = "Configurable offline memory buffer up to 10GB for instant playback",
            category = "Storage & Cache",
            iconRes = R.drawable.storage,
            route = "settings/storage",
            targetKey = "storage_cache_limit",
            keywords = listOf("cache", "song cache", "buffer", "10gb", "cache size", "memory", "storage")
        ),
        SearchableSettingItem(
            id = "storage_downloads",
            title = "Offline Downloads Library",
            description = "Manage downloaded songs, storage location, and unmetered play",
            category = "Storage & Cache",
            iconRes = R.drawable.download,
            route = "settings/storage",
            targetKey = "storage_downloads",
            keywords = listOf("download", "offline", "saved tracks", "offline music", "downloads")
        ),
        SearchableSettingItem(
            id = "storage_clear_cache",
            title = "Clear Cache & Temporary Files",
            description = "Purge cached album artwork and audio chunks to reclaim free space",
            category = "Storage & Cache",
            iconRes = R.drawable.clear_all,
            route = "settings/storage",
            targetKey = "storage_clear_cache",
            keywords = listOf("clear cache", "purge", "free space", "delete cache", "cleanup", "reclaim space")
        ),
        SearchableSettingItem(
            id = "storage_cache_delay",
            title = "Cache After Listening Delay",
            description = "Delay song caching (5s–90s) for instant lag-free playback start",
            category = "Storage & Cache",
            iconRes = R.drawable.timer,
            route = "settings/storage",
            targetKey = "storage_cache_delay",
            keywords = listOf("cache", "delay", "storage", "instant", "seconds", "fast", "buffer")
        ),

        // ====================================================================
        // 8. LISTEN TOGETHER & INTEGRATIONS
        // ====================================================================
        SearchableSettingItem(
            id = "screen_listen_together",
            title = "Listen Together",
            description = "Synchronized live group listening sessions with friends",
            category = "Listen Together",
            iconRes = R.drawable.group,
            route = Screens.ListenTogether.route,
            targetKey = "screen_listen_together",
            keywords = listOf("listen together", "party", "group session", "sync listen", "friends", "room", "host")
        ),
        SearchableSettingItem(
            id = "privacy_discord_rpc",
            title = "Discord Rich Presence",
            description = "Display live playing song, artist, and album art on Discord status",
            category = "Integrations",
            iconRes = R.drawable.discord,
            route = "settings/integrations",
            targetKey = "privacy_discord_rpc",
            keywords = listOf("discord", "rpc", "rich presence", "status", "listening on discord", "integration")
        ),
        SearchableSettingItem(
            id = "spotify_import",
            title = "Spotify Playlist Import",
            description = "Migrate your public and private Spotify playlists directly into MKR Music",
            category = "Integrations",
            iconRes = R.drawable.spotify,
            route = "settings/spotify",
            targetKey = "spotify_import",
            keywords = listOf("spotify", "import", "playlists", "sync", "migration")
        ),

        // ====================================================================
        // 9. SYSTEM, BACKUP & ABOUT
        // ====================================================================
        SearchableSettingItem(
            id = "account_login",
            title = "Google & YouTube Music Sync",
            description = "Sync your liked songs, playlists, and subscriptions seamlessly",
            category = "Account & System",
            iconRes = R.drawable.google,
            route = "settings/account",
            targetKey = "account_login",
            keywords = listOf("google", "account", "login", "youtube music", "sync", "playlists")
        ),
        SearchableSettingItem(
            id = "backup_export_import",
            title = "Backup & Restore",
            description = "Export and import your playlists, preferences, and database backups",
            category = "Account & System",
            iconRes = R.drawable.restore,
            route = "settings/backup_restore",
            targetKey = "backup_export_import",
            keywords = listOf("backup", "restore", "export", "import", "data", "playlists")
        ),
        SearchableSettingItem(
            id = "update_check",
            title = "Check for System Updates",
            description = "Check for MKR Music new releases and install latest universal APK",
            category = "Account & System",
            iconRes = R.drawable.system_update_uptodate,
            route = "settings/update",
            targetKey = "update_check",
            keywords = listOf("update", "upgrade", "check update", "new version", "apk", "system update")
        ),
        SearchableSettingItem(
            id = "about_app",
            title = "About MKR Music",
            description = "Version v${BuildConfig.VERSION_NAME}, developer credits, licenses & GitHub repo",
            category = "Account & System",
            iconRes = R.drawable.info,
            route = "settings/about",
            targetKey = "about_app",
            keywords = listOf("about", "version", "developer", "mkr", "github", "source code", "credits", "license")
        )
    )
}

/**
 * Searches the catalog with multi-word token matching and ranked relevance scoring.
 */
fun searchSettingsCatalog(query: String, catalog: List<SearchableSettingItem>): List<SearchableSettingItem> {
    if (query.isBlank()) return emptyList()

    val normalizedQuery = query.lowercase().trim()
    val queryWords = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

    return catalog
        .mapNotNull { item ->
            val score = computeMatchScore(item, normalizedQuery, queryWords)
            if (score > 0) Pair(item, score) else null
        }
        .sortedByDescending { it.second }
        .map { it.first }
}

private fun computeMatchScore(
    item: SearchableSettingItem,
    query: String,
    queryWords: List<String>
): Int {
    val titleLower = item.title.lowercase()
    val descLower = item.description.lowercase()
    val categoryLower = item.category.lowercase()

    // 1. Exact match on title
    if (titleLower == query) return 100

    // 2. Title starts with query
    if (titleLower.startsWith(query)) return 90

    // 3. Title contains full query
    if (titleLower.contains(query)) return 85

    // 4. Exact keyword match
    if (item.keywords.any { it.lowercase() == query }) return 80

    // 5. Keyword starts with query
    if (item.keywords.any { it.lowercase().startsWith(query) }) return 75

    // 6. All query words present in title
    if (queryWords.size > 1 && queryWords.all { titleLower.contains(it) }) return 70

    // 7. Any keyword contains query
    if (item.keywords.any { it.lowercase().contains(query) }) return 65

    // 8. All query words found in keywords
    if (queryWords.all { word -> item.keywords.any { it.lowercase().contains(word) } }) return 60

    // 9. Description contains full query
    if (descLower.contains(query)) return 50

    // 10. All query words in description
    if (queryWords.size > 1 && queryWords.all { descLower.contains(it) }) return 45

    // 11. Any query word in title
    if (queryWords.any { titleLower.contains(it) }) return 40

    // 12. Any query word in keywords
    if (queryWords.any { word -> item.keywords.any { it.lowercase().contains(word) } }) return 35

    // 13. Category contains query
    if (categoryLower.contains(query)) return 30

    // 14. Any query word in description
    if (queryWords.any { descLower.contains(it) }) return 25

    return 0
}
