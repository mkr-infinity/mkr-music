/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import android.content.Context
import androidx.annotation.DrawableRes
import com.music.mkr.BuildConfig
import com.music.mkr.R

/**
 * Metadata representing an indexed setting item for full-text multi-word token search.
 * Includes pre-tokenized fields to ensure search computation is instant and non-laggy.
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
    val keywords: List<String> = emptyList(),
) {
    val normalizedTitle: String = title.lowercase()
    val normalizedDescription: String = description.lowercase()
    val normalizedCategory: String = category.lowercase()
    val normalizedKeywords: List<String> = keywords.map { it.lowercase() }
    
    // Pre-computed word tokens for $O(1)$ set lookups
    val allSearchTokens: Set<String> = buildSet {
        addAll(normalizedTitle.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 })
        addAll(normalizedDescription.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 })
        addAll(normalizedCategory.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 })
        normalizedKeywords.forEach { kw ->
            addAll(kw.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 })
        }
    }
}

/**
 * Builds the comprehensive search catalog across all MKR Music configuration categories.
 */
fun buildSettingsSearchCatalog(context: Context): List<SearchableSettingItem> {
    return listOf(
        // ====================================================================
        // 1. APPEARANCE & THEMES
        // ====================================================================
        SearchableSettingItem(
            id = "appearance_theme_palette",
            title = "Themes & Accent Colors",
            description = "Dynamic Material You colors, amber accents, custom palette hues, and dark/light modes",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_theme_palette",
            keywords = listOf("theme", "color", "accent", "amber", "dynamic theme", "monet", "palette", "dark mode", "light mode", "contrast", "custom color")
        ),
        SearchableSettingItem(
            id = "appearance_pure_black",
            title = "Pure Black OLED Mode",
            description = "Deep AMOLED true black background across player, library, navigation, and all surfaces to save battery",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance/theme",
            targetKey = "appearance_pure_black",
            keywords = listOf("pure black", "oled", "amoled", "true black", "pitch black", "battery saver", "contrast", "dark")
        ),
        SearchableSettingItem(
            id = "appearance_dynamic_theme",
            title = "Dynamic Artwork Theming",
            description = "Extract primary and secondary accent colors dynamically from current song's album art",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance/theme",
            targetKey = "appearance_dynamic_theme",
            keywords = listOf("dynamic", "monet", "artwork theme", "album art color", "palette extraction", "adaptive theme")
        ),
        SearchableSettingItem(
            id = "appearance_icon_gallery",
            title = "App Icon Gallery",
            description = "Choose dynamic launcher icons, retro badges, and custom branding to customize your home screen",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_icon_gallery",
            keywords = listOf("launcher", "icon", "logo", "adaptive", "aesthetic", "homescreen", "gallery", "app icon", "badge", "mkr logo")
        ),
        SearchableSettingItem(
            id = "appearance_navigation_style",
            title = "Navigation Bar Style",
            description = "Telegram glass capsule, expressive pill, or material dock bottom navigation bar",
            category = "Appearance & Themes",
            iconRes = R.drawable.nav_bar,
            route = "settings/appearance",
            targetKey = "appearance_navigation_style",
            keywords = listOf("navbar", "navigation", "style", "glass", "capsule", "dock", "bottom bar", "floating", "telegram")
        ),
        SearchableSettingItem(
            id = "appearance_floating_nav",
            title = "Floating Navigation Bar",
            description = "Floating pill-shaped navigation dock with smooth glass blur effect and translucent background",
            category = "Appearance & Themes",
            iconRes = R.drawable.nav_bar,
            route = "settings/appearance",
            targetKey = "appearance_floating_nav",
            keywords = listOf("navbar", "navigation", "floating", "bottom bar", "pill", "dock", "blur", "glassmorphism")
        ),
        SearchableSettingItem(
            id = "appearance_slider_style",
            title = "Player Seekbar & Sliders",
            description = "Animated wavy, squiggly, slim, or expressive player seekbar progress sliders",
            category = "Appearance & Themes",
            iconRes = R.drawable.linear_scale,
            route = "settings/appearance",
            targetKey = "appearance_slider_style",
            keywords = listOf("wavy", "squiggly", "slider", "seekbar", "progress", "wave", "track", "slider style", "seek")
        ),
        SearchableSettingItem(
            id = "appearance_custom_font",
            title = "Typography & Font Selection",
            description = "Google Sans, Sans Flex, Outfit, Plus Jakarta Sans, or custom TTF/OTF font file",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance/font",
            targetKey = "appearance_font",
            keywords = listOf("font", "typography", "text", "google sans", "outfit", "typeface", "custom font", "ttf", "otf")
        ),
        SearchableSettingItem(
            id = "appearance_high_refresh_rate",
            title = "High Refresh Rate Display",
            description = "Force 90Hz / 120Hz smooth scrolling for ultra-fluid animations and responsive gestures",
            category = "Appearance & Themes",
            iconRes = R.drawable.speed,
            route = "settings/appearance",
            targetKey = "appearance_high_refresh_rate",
            keywords = listOf("refresh rate", "120hz", "90hz", "fps", "smooth", "fluid", "performance", "display")
        ),
        SearchableSettingItem(
            id = "appearance_audio_quality_badge",
            title = "Audio Quality Badge",
            description = "Display Hi-Res, Lossless, HD, or Bitrate badge on player screen and song rows",
            category = "Appearance & Themes",
            iconRes = R.drawable.earbud_case,
            route = "settings/appearance",
            targetKey = "appearance_audio_quality_badge",
            keywords = listOf("badge", "hi-res", "lossless", "hd", "audio quality", "bitrate", "codec", "format")
        ),
        SearchableSettingItem(
            id = "appearance_display_density",
            title = "Display Density Scale",
            description = "Adjust UI element scale, compact spacing, padding, and zoom across all app screens",
            category = "Appearance & Themes",
            iconRes = R.drawable.grid_view,
            route = "settings/appearance",
            targetKey = "appearance_density",
            keywords = listOf("density", "scale", "compact", "size", "zoom", "ui scale", "spacing", "layout", "grid")
        ),
        SearchableSettingItem(
            id = "appearance_interface_animations",
            title = "Interface Animations",
            description = "Enable or disable smooth screen transitions, tactile spring physics, and list fade effects",
            category = "Appearance & Themes",
            iconRes = R.drawable.palette,
            route = "settings/appearance",
            targetKey = "appearance_animations",
            keywords = listOf("animation", "motion", "spring", "transitions", "effects", "smooth", "disable animations", "reduced motion")
        ),
        SearchableSettingItem(
            id = "appearance_rotating_thumbnail",
            title = "Rotating Vinyl Album Art",
            description = "Continuous spinning vinyl record animation for artwork while music is playing",
            category = "Appearance & Themes",
            iconRes = R.drawable.album,
            route = "settings/appearance",
            targetKey = "appearance_rotating_thumbnail",
            keywords = listOf("rotating", "spin", "vinyl", "record", "disc", "animation", "artwork", "album cover")
        ),
        SearchableSettingItem(
            id = "appearance_swipe_thumbnail",
            title = "Swipe Artwork to Skip",
            description = "Swipe album cover left or right to skip to previous or next song in queue",
            category = "Appearance & Themes",
            iconRes = R.drawable.album,
            route = "settings/appearance",
            targetKey = "appearance_swipe_thumbnail",
            keywords = listOf("swipe", "gesture", "skip", "next song", "previous song", "thumbnail gesture", "artwork swipe")
        ),
        SearchableSettingItem(
            id = "appearance_crop_album_art",
            title = "Crop Album Artwork",
            description = "Crop square album covers to fill edge-to-edge or fit completely with letterboxing",
            category = "Appearance & Themes",
            iconRes = R.drawable.album,
            route = "settings/appearance",
            targetKey = "appearance_crop_art",
            keywords = listOf("crop", "fit", "fill", "album art", "thumbnail", "aspect ratio", "letterbox")
        ),
        SearchableSettingItem(
            id = "appearance_thumbnail_shadow",
            title = "Player Thumbnail Shadow & Elevation",
            description = "Customize depth, blur, and elevation shadow cast by album artwork",
            category = "Appearance & Themes",
            iconRes = R.drawable.album,
            route = "settings/appearance",
            targetKey = "appearance_thumbnail_shadow",
            keywords = listOf("shadow", "elevation", "depth", "glow", "artwork shadow", "thumbnail elevation")
        ),
        SearchableSettingItem(
            id = "appearance_lyrics_size",
            title = "Lyrics Text Size & Line Spacing",
            description = "Adjust font size, line height, and text alignment for synchronized lyrics screen",
            category = "Appearance & Themes",
            iconRes = R.drawable.lyrics,
            route = "settings/appearance",
            targetKey = "appearance_lyrics_size",
            keywords = listOf("lyrics", "size", "font size", "text size", "line height", "spacing", "alignment", "lyrics style")
        ),
        SearchableSettingItem(
            id = "appearance_apple_lyrics_blur",
            title = "Apple Music Lyrics Blur",
            description = "Apple Music style progressive gaussian blur on past and upcoming lyrics lines",
            category = "Appearance & Themes",
            iconRes = R.drawable.lyrics,
            route = "settings/appearance",
            targetKey = "appearance_lyrics_blur",
            keywords = listOf("lyrics", "blur", "apple music", "lyrics blur", "frosted glass", "progressive blur")
        ),
        SearchableSettingItem(
            id = "appearance_lyrics_glow",
            title = "Lyrics Active Line Glow",
            description = "Vibrant illuminated glow halo around the currently sung synchronized lyric",
            category = "Appearance & Themes",
            iconRes = R.drawable.lyrics,
            route = "settings/appearance",
            targetKey = "appearance_lyrics_glow",
            keywords = listOf("glow", "halo", "illuminated", "highlight", "lyrics glow", "active lyric")
        ),
        SearchableSettingItem(
            id = "appearance_default_tab",
            title = "Default Startup Tab",
            description = "Choose which screen opens when launching MKR Music: Home, Library, or Explore",
            category = "Appearance & Themes",
            iconRes = R.drawable.home_outlined,
            route = "settings/appearance",
            targetKey = "appearance_default_tab",
            keywords = listOf("default tab", "startup", "launch", "open tab", "start screen", "home", "library")
        ),

        // ====================================================================
        // 2. PLAYER & AUDIO
        // ====================================================================
        SearchableSettingItem(
            id = "player_audio_quality",
            title = "Streaming Audio Quality",
            description = "Select bitrates up to 256kbps AAC / Opus high-fidelity audio streams",
            category = "Player and Audio",
            iconRes = R.drawable.volume_up,
            route = "settings/player",
            targetKey = "player_audio_quality",
            keywords = listOf("quality", "bitrate", "stream", "audio", "high", "lossless", "aac", "opus", "kbps", "sound")
        ),
        SearchableSettingItem(
            id = "player_normalization",
            title = "Audio Normalization & Loudness",
            description = "Automatically level volume across different songs to prevent sudden loud or quiet tracks (ReplayGain)",
            category = "Player and Audio",
            iconRes = R.drawable.volume_up,
            route = "settings/player",
            targetKey = "player_audio_normalization",
            keywords = listOf("normalize", "loudness", "volume", "replaygain", "level", "quiet", "loud", "gain", "normalization")
        ),
        SearchableSettingItem(
            id = "player_skip_silence",
            title = "Skip Silence",
            description = "Automatically detect and skip silence at the beginning or end of songs",
            category = "Player and Audio",
            iconRes = R.drawable.volume_off,
            route = "settings/player",
            targetKey = "player_skip_silence",
            keywords = listOf("silence", "skip silence", "quiet", "trim", "gapless", "skip empty", "silence trimming")
        ),
        SearchableSettingItem(
            id = "player_audio_offload",
            title = "Audio Offload to Hardware DSP",
            description = "Offload audio decoding directly to device DSP processor to dramatically save battery during playback",
            category = "Player and Audio",
            iconRes = R.drawable.earbud_case,
            route = "settings/player",
            targetKey = "player_audio_offload",
            keywords = listOf("offload", "dsp", "hardware", "battery", "decoder", "audio offload", "power saving")
        ),
        SearchableSettingItem(
            id = "player_crossfade",
            title = "Crossfade & Gapless Playback",
            description = "Smoothly blend track transitions without silence using configurable crossfade durations and curves",
            category = "Player and Audio",
            iconRes = R.drawable.graphic_eq,
            route = "settings/player",
            targetKey = "player_crossfade",
            keywords = listOf("crossfade", "fade", "transition", "overlap", "gapless", "blend", "curve", "crossfade duration")
        ),
        SearchableSettingItem(
            id = "player_stop_on_task_clear",
            title = "Stop Music on Clear Task",
            description = "Stop music playback and terminate background audio service when swiping app away from recent apps (prevents device lag)",
            category = "Player and Audio",
            iconRes = R.drawable.clear_all,
            route = "settings/player",
            targetKey = "player_stop_on_task_clear",
            keywords = listOf("task clear", "clear task", "kill", "lag", "stop music", "swiped away", "recent apps", "terminate", "background service", "process kill", "freeze")
        ),
        SearchableSettingItem(
            id = "player_resume_bluetooth",
            title = "Resume on Bluetooth Connect",
            description = "Automatically resume music playback when connecting to Bluetooth headphones, earbuds, or car audio",
            category = "Player and Audio",
            iconRes = R.drawable.bluetooth,
            route = "settings/player",
            targetKey = "player_resume_bluetooth",
            keywords = listOf("bluetooth", "headphones", "earbuds", "car audio", "auto play", "resume", "connect", "handsfree")
        ),
        SearchableSettingItem(
            id = "player_pause_on_mute",
            title = "Pause Playback on Mute",
            description = "Automatically pause music when volume is reduced to zero or system volume is muted",
            category = "Player and Audio",
            iconRes = R.drawable.volume_mute,
            route = "settings/player",
            targetKey = "player_pause_on_mute",
            keywords = listOf("pause on mute", "mute", "zero volume", "pause", "silent", "sound off")
        ),
        SearchableSettingItem(
            id = "player_keep_screen_on",
            title = "Keep Screen On",
            description = "Prevent display from sleeping or turning off while player or synchronized lyrics screen is open",
            category = "Player and Audio",
            iconRes = R.drawable.settings,
            route = "settings/player",
            targetKey = "player_keep_screen_on",
            keywords = listOf("screen on", "stay awake", "awake", "sleep timer", "display timeout", "wakelock")
        ),
        SearchableSettingItem(
            id = "player_persistent_queue",
            title = "Persistent Playback Queue",
            description = "Save and restore your current queue, song index, and playback position across app restarts",
            category = "Player and Audio",
            iconRes = R.drawable.queue_music,
            route = "settings/player",
            targetKey = "player_persistent_queue",
            keywords = listOf("persistent queue", "queue", "remember queue", "restore queue", "restart", "session", "current track")
        ),
        SearchableSettingItem(
            id = "player_seek_seconds",
            title = "Seek Jump Duration",
            description = "Set double-tap and fast forward / rewind jump interval (5s, 10s, 15s, 30s)",
            category = "Player and Audio",
            iconRes = R.drawable.skip_next,
            route = "settings/player",
            targetKey = "player_seek_seconds",
            keywords = listOf("seek", "jump", "forward", "rewind", "seconds", "double tap", "fast forward", "scrub")
        ),
        SearchableSettingItem(
            id = "player_prevent_duplicates",
            title = "Prevent Duplicate Songs in Queue",
            description = "Avoid adding songs that already exist in the active playback queue",
            category = "Player and Audio",
            iconRes = R.drawable.queue_music,
            route = "settings/player",
            targetKey = "player_prevent_duplicates",
            keywords = listOf("duplicate", "prevent duplicates", "queue duplicates", "unique songs", "filter queue")
        ),
        SearchableSettingItem(
            id = "player_auto_download_on_like",
            title = "Auto Download on Like",
            description = "Automatically download songs to offline storage whenever you tap the favorite heart button",
            category = "Player and Audio",
            iconRes = R.drawable.favorite,
            route = "settings/player",
            targetKey = "player_auto_download_on_like",
            keywords = listOf("auto download", "like", "favorite", "offline download", "save song", "heart")
        ),
        SearchableSettingItem(
            id = "player_auto_skip_error",
            title = "Auto Skip Next on Error",
            description = "Automatically skip to next track in queue if current stream fails to load or decode",
            category = "Player and Audio",
            iconRes = R.drawable.skip_next,
            route = "settings/player",
            targetKey = "player_auto_skip_error",
            keywords = listOf("skip on error", "playback error", "failed stream", "broken track", "skip unplayable")
        ),
        SearchableSettingItem(
            id = "player_google_cast",
            title = "Google Cast & Chromecast",
            description = "Stream and cast music to Google Nest speakers, Smart TVs, and Chromecast devices",
            category = "Player and Audio",
            iconRes = R.drawable.earbud_case,
            route = "settings/player",
            targetKey = "player_google_cast",
            keywords = listOf("cast", "chromecast", "google cast", "speakers", "nest", "tv", "stream to speaker", "wifi stream")
        ),
        SearchableSettingItem(
            id = "player_equalizer",
            title = "Equalizer",
            description = "Open device system audio equalizer",
            category = "Player and Audio",
            iconRes = R.drawable.equalizer,
            route = "settings/player",
            targetKey = "player_equalizer",
            keywords = listOf("equalizer", "eq", "bass", "system equalizer", "sound effects", "frequencies", "audio fx")
        ),
        SearchableSettingItem(
            id = "player_pitch_speed",
            title = "Pitch & Playback Tempo Speed",
            description = "Adjust song playback speed (0.5x to 2.0x) and pitch shifting in real time",
            category = "Player and Audio",
            iconRes = R.drawable.graphic_eq,
            route = "settings/player",
            targetKey = "player_pitch_speed",
            keywords = listOf("pitch", "speed", "tempo", "rate", "slow down", "speed up", "nightcore", "slowed", "playback speed")
        ),
        SearchableSettingItem(
            id = "player_style_architecture",
            title = "Player Architectural Style",
            description = "Classic, Modern Pill, Immersive Extended, Material Extended, Apple Music, Minimal, or Cinematic",
            category = "Player and Audio",
            iconRes = R.drawable.palette,
            route = "settings/player",
            targetKey = "player_style",
            keywords = listOf("player style", "layout", "classic", "modern", "apple", "cinematic", "immersive", "material", "controls layout")
        ),
        SearchableSettingItem(
            id = "player_apple_mini_player",
            title = "Apple Mini Player",
            description = "Floating translucent iOS-inspired mini player bar with high-fidelity blur",
            category = "Player and Audio",
            iconRes = R.drawable.nav_bar,
            route = "settings/player",
            targetKey = "player_apple_mini_player",
            keywords = listOf("mini player", "apple mini player", "ios", "floating mini", "capsule mini")
        ),
        SearchableSettingItem(
            id = "player_liquid_mini_player",
            title = "Liquid Wave Mini Player",
            description = "Fluid animated liquid wave progress canvas along the bottom edge of mini player",
            category = "Player and Audio",
            iconRes = R.drawable.linear_scale,
            route = "settings/player",
            targetKey = "player_liquid_mini_player",
            keywords = listOf("liquid", "wave", "liquid mini player", "fluid progress", "animated mini player")
        ),
        SearchableSettingItem(
            id = "player_pure_black_mini_player",
            title = "Pure Black Mini Player",
            description = "Pitch-black background styling for the mini-player dock bar",
            category = "Player and Audio",
            iconRes = R.drawable.palette,
            route = "settings/player",
            targetKey = "player_pure_black_mini_player",
            keywords = listOf("pure black mini", "oled mini player", "dark mini player")
        ),

        // ====================================================================
        // 3. SPONSORBLOCK
        // ====================================================================
        SearchableSettingItem(
            id = "sponsorblock_master",
            title = "SponsorBlock Integration",
            description = "Automatically skip non-music intros, sponsors, merch shoutouts, and silence segments",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_master",
            keywords = listOf("sponsor", "sponsorblock", "ads", "skip", "promo", "intro", "outro", "intermission", "silence")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_non_music",
            title = "Skip Non-Music Audio Segments",
            description = "Skip background story scenes, comedy skits, and dialogue in official music videos",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_non_music",
            keywords = listOf("non-music", "skit", "dialogue", "scene", "music video", "story", "acting")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_sponsor",
            title = "Skip Sponsor Segments",
            description = "Skip paid promotions and sponsored endorsements embedded in audio",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_sponsor",
            keywords = listOf("sponsor", "paid promotion", "advertisement", "endorsement", "ad")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_selfpromo",
            title = "Skip Self-Promo & Merch",
            description = "Skip artist merch plugs, tour announcements, Patreon, and donation requests",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_selfpromo",
            keywords = listOf("selfpromo", "merch", "tour", "patreon", "merchandise", "donation", "promo")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_interaction",
            title = "Skip Interaction Reminders",
            description = "Skip 'subscribe to channel', 'leave a like', and notification bell reminder prompts",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_interaction",
            keywords = listOf("interaction", "subscribe", "like reminder", "bell", "comment", "channel")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_intro_outro",
            title = "Skip Music Video Intros & Outros",
            description = "Skip opening title cards, credits roll, and silence at end of video",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_intro_outro",
            keywords = listOf("intro", "outro", "credits", "title card", "end screen", "opening")
        ),
        SearchableSettingItem(
            id = "sponsorblock_skip_preview",
            title = "Skip Preview & Filler Segments",
            description = "Skip recap clips, preview snippets, and dead filler time",
            category = "SponsorBlock",
            iconRes = R.drawable.fast_forward,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_skip_preview",
            keywords = listOf("preview", "filler", "recap", "teaser", "snippet")
        ),
        SearchableSettingItem(
            id = "sponsorblock_show_toast",
            title = "SponsorBlock Skip Notification Toasts",
            description = "Display brief HUD toast banner when an unwanted segment is skipped",
            category = "SponsorBlock",
            iconRes = R.drawable.info,
            route = "settings/player/sponsorblock",
            targetKey = "sponsorblock_show_toast",
            keywords = listOf("toast", "notification", "hud", "skip alert", "sponsorblock toast")
        ),

        // ====================================================================
        // 4. CONTENT, LANGUAGE & SEARCH
        // ====================================================================
        SearchableSettingItem(
            id = "content_app_language",
            title = "App Interface Language",
            description = "Change the language of MKR Music app interface and menus",
            category = "Content & Language",
            iconRes = R.drawable.language,
            route = "settings/content",
            targetKey = "content_app_language",
            keywords = listOf("language", "locale", "translation", "english", "spanish", "hindi", "app language")
        ),
        SearchableSettingItem(
            id = "content_country_region",
            title = "Content Country & Music Region",
            description = "Customize YouTube Music trending charts, discover feeds, and regional music releases",
            category = "Content & Language",
            iconRes = R.drawable.language,
            route = "settings/content",
            targetKey = "content_country_region",
            keywords = listOf("country", "region", "location", "trending", "charts", "content language", "youtube music country")
        ),
        SearchableSettingItem(
            id = "content_explicit_filter",
            title = "Explicit Content Filter",
            description = "Hide explicit songs and songs containing adult language from search and recommendations",
            category = "Content & Language",
            iconRes = R.drawable.explicit,
            route = "settings/content",
            targetKey = "content_explicit",
            keywords = listOf("explicit", "parental control", "clean", "hide explicit", "bad words", "adult")
        ),
        SearchableSettingItem(
            id = "content_hide_videos",
            title = "Hide Video Songs",
            description = "Filter out video-only tracks to guarantee pure studio audio quality",
            category = "Content & Language",
            iconRes = R.drawable.slow_motion_video,
            route = "settings/content",
            targetKey = "content_hide_videos",
            keywords = listOf("video", "hide videos", "music video", "studio audio", "audio only")
        ),
        SearchableSettingItem(
            id = "content_hide_shorts",
            title = "Hide YouTube Shorts",
            description = "Hide short-form video clips and YouTube Shorts from search results and feeds",
            category = "Content & Language",
            iconRes = R.drawable.slow_motion_video,
            route = "settings/content",
            targetKey = "content_hide_shorts",
            keywords = listOf("shorts", "youtube shorts", "reels", "hide shorts", "clips")
        ),
        SearchableSettingItem(
            id = "content_artist_info",
            title = "Show Artist Description & Stats",
            description = "Display artist biography, subscriber count, and background info on artist profile pages",
            category = "Content & Language",
            iconRes = R.drawable.artist,
            route = "settings/content",
            targetKey = "content_artist_info",
            keywords = listOf("artist", "bio", "subscribers", "artist info", "biography", "stats")
        ),
        SearchableSettingItem(
            id = "content_monthly_listeners",
            title = "Show Monthly Listeners",
            description = "Display monthly listener counts and popularity metrics on artist profiles",
            category = "Content & Language",
            iconRes = R.drawable.artist,
            route = "settings/content",
            targetKey = "content_monthly_listeners",
            keywords = listOf("monthly listeners", "listeners", "artist stats", "popularity", "views")
        ),
        SearchableSettingItem(
            id = "content_proxy_vpn",
            title = "HTTP Proxy & Network Configuration",
            description = "Configure custom HTTP / SOCKS proxy server, host port, and authentication credentials",
            category = "Content & Language",
            iconRes = R.drawable.network_node,
            route = "settings/content",
            targetKey = "content_proxy_vpn",
            keywords = listOf("proxy", "vpn", "network", "port", "host", "socks", "http proxy", "bypass", "geo-block")
        ),
        SearchableSettingItem(
            id = "content_lyrics_providers",
            title = "Lyrics Providers Engine",
            description = "Kugou, LRCLIB, BetterLyrics, YouLyPlus, Paxsenix, Unison, and BiniLyrics integrations",
            category = "Content & Language",
            iconRes = R.drawable.lyrics,
            route = "settings/content",
            targetKey = "content_lyrics_providers",
            keywords = listOf("lyrics", "lrclib", "kugou", "betterlyrics", "providers", "synced lyrics", "lrc")
        ),
        SearchableSettingItem(
            id = "content_lyrics_reorder",
            title = "Reorder Lyrics Providers & Prioritize Sync",
            description = "Drag to reorder lyrics provider priority and prioritize synchronized time-synced accuracy",
            category = "Content & Language",
            iconRes = R.drawable.lyrics,
            route = "settings/content",
            targetKey = "content_lyrics_reorder",
            keywords = listOf("reorder lyrics", "lyrics priority", "sync accuracy", "lyrics order", "synced")
        ),
        SearchableSettingItem(
            id = "content_top_length",
            title = "Top Songs Playlist Size",
            description = "Set number of songs in auto-generated My Top playlist (20, 50, or 100 songs)",
            category = "Content & Language",
            iconRes = R.drawable.playlist_add,
            route = "settings/content",
            targetKey = "content_top_length",
            keywords = listOf("top songs", "top size", "50", "100", "my top", "playlist length")
        ),
        SearchableSettingItem(
            id = "content_quick_picks",
            title = "Quick Picks & Speed Dial Engine",
            description = "Customize Home screen recommendation source: Quick Picks or Speed Dial",
            category = "Content & Language",
            iconRes = R.drawable.home_outlined,
            route = "settings/content",
            targetKey = "content_quick_picks",
            keywords = listOf("quick picks", "speed dial", "home recommendations", "discover", "suggestions")
        ),
        SearchableSettingItem(
            id = "content_wrapped_card",
            title = "MKR Wrapped Recap Card",
            description = "Show interactive musical recap summary card on Home screen",
            category = "Content & Language",
            iconRes = R.drawable.star,
            route = "settings/content",
            targetKey = "content_wrapped_card",
            keywords = listOf("wrapped", "recap", "summary", "year in review", "stats", "card")
        ),
        SearchableSettingItem(
            id = "content_randomize_home",
            title = "Randomize Home Sections Order",
            description = "Shuffle and randomize the vertical ordering of Home screen music shelves",
            category = "Content & Language",
            iconRes = R.drawable.shuffle,
            route = "settings/content",
            targetKey = "content_randomize_home",
            keywords = listOf("randomize home", "home order", "shelves", "sections order", "shuffle home")
        ),

        // ====================================================================
        // 5. LYRICS ROMANIZATION
        // ====================================================================
        SearchableSettingItem(
            id = "romanization_main_lyrics",
            title = "Display Romanized as Main Lyrics",
            description = "Render Latin romanization as primary text with original characters as subtitle",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_main_lyrics",
            keywords = listOf("romanization", "romanize", "latin", "main lyrics", "transliteration", "lyrics text")
        ),
        SearchableSettingItem(
            id = "romanization_japanese",
            title = "Japanese Romaji Romanization",
            description = "Transliterate Japanese Kanji, Hiragana, and Katakana into Romaji Latin script",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_japanese",
            keywords = listOf("japanese", "romaji", "kanji", "hiragana", "katakana", "anime", "jpop")
        ),
        SearchableSettingItem(
            id = "romanization_korean",
            title = "Korean Hangul Romanization",
            description = "Transliterate Korean Hangul syllables into standard Latin script for K-Pop lyrics",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_korean",
            keywords = listOf("korean", "hangul", "kpop", "romanized kpop", "latin korean")
        ),
        SearchableSettingItem(
            id = "romanization_chinese",
            title = "Chinese Pinyin Romanization",
            description = "Transliterate Simplified and Traditional Chinese Hanzi into Pinyin with tone markers",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_chinese",
            keywords = listOf("chinese", "pinyin", "hanzi", "mandarin", "cpop")
        ),
        SearchableSettingItem(
            id = "romanization_indic",
            title = "Hindi & Punjabi Romanization",
            description = "Transliterate Devanagari Hindi and Gurmukhi Punjabi lyrics into English Latin script",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_indic",
            keywords = listOf("hindi", "punjabi", "bollywood", "devanagari", "gurmukhi", "indic", "hinglish")
        ),
        SearchableSettingItem(
            id = "romanization_cyrillic",
            title = "Russian & Cyrillic Romanization",
            description = "Transliterate Russian, Ukrainian, Serbian, and Cyrillic lyrics into Latin characters",
            category = "Lyrics Romanization",
            iconRes = R.drawable.language,
            route = "settings/content/romanization",
            targetKey = "romanization_cyrillic",
            keywords = listOf("russian", "cyrillic", "ukrainian", "serbian", "latin", "transliterate")
        ),

        // ====================================================================
        // 6. AI & SMART FEATURES
        // ====================================================================
        SearchableSettingItem(
            id = "ai_provider_setup",
            title = "AI Provider & OpenRouter Setup",
            description = "Configure AI intelligence backend using OpenRouter, Google Gemini, or custom LLM endpoints",
            category = "AI & Smart Features",
            iconRes = R.drawable.sparks,
            route = "settings/ai",
            targetKey = "ai_provider",
            keywords = listOf("ai", "openrouter", "gemini", "intelligence", "llm", "smart", "ai provider")
        ),
        SearchableSettingItem(
            id = "ai_gemini_model",
            title = "Gemini AI Model Selection",
            description = "Choose Google Gemini 2.5 Flash, Flash-Lite, or custom model identifier",
            category = "AI & Smart Features",
            iconRes = R.drawable.sparks,
            route = "settings/ai",
            targetKey = "ai_model",
            keywords = listOf("gemini", "gemini 2.5", "flash", "flash-lite", "google ai", "model")
        ),
        SearchableSettingItem(
            id = "ai_api_key",
            title = "OpenRouter / AI API Key",
            description = "Securely enter your API key to unlock AI lyrics translation and song explanations",
            category = "AI & Smart Features",
            iconRes = R.drawable.key,
            route = "settings/ai",
            targetKey = "ai_api_key",
            keywords = listOf("api key", "key", "token", "openrouter key", "gemini key", "secret")
        ),
        SearchableSettingItem(
            id = "ai_lyrics_translation",
            title = "AI Lyrics Translation & Meaning",
            description = "Instant contextual song meaning explanations and multi-language lyrics translation",
            category = "AI & Smart Features",
            iconRes = R.drawable.sparks,
            route = "settings/ai",
            targetKey = "ai_lyrics_translation",
            keywords = listOf("ai lyrics", "meaning", "song meaning", "translate lyrics", "lyrics explanation", "story")
        ),
        SearchableSettingItem(
            id = "ai_deepl_setup",
            title = "DeepL Translation API",
            description = "Configure DeepL Pro / Free API key for high-accuracy translation formality",
            category = "AI & Smart Features",
            iconRes = R.drawable.language,
            route = "settings/ai",
            targetKey = "ai_deepl",
            keywords = listOf("deepl", "deepl api", "formality", "high accuracy translation")
        ),

        // ====================================================================
        // 7. STORAGE, CACHE & OFFLINE
        // ====================================================================
        SearchableSettingItem(
            id = "storage_max_song_cache",
            title = "Song Audio Cache Size",
            description = "Configure disk cache space allocated for streamed music tracks (100MB to Unlimited)",
            category = "Storage & Cache",
            iconRes = R.drawable.storage,
            route = "settings/storage",
            targetKey = "storage_song_cache",
            keywords = listOf("cache", "song cache", "audio cache", "disk space", "storage", "buffer", "clear cache")
        ),
        SearchableSettingItem(
            id = "storage_max_image_cache",
            title = "Album Artwork Cache Size",
            description = "Manage thumbnail, album artwork, and artist picture storage cache size",
            category = "Storage & Cache",
            iconRes = R.drawable.image,
            route = "settings/storage",
            targetKey = "storage_image_cache",
            keywords = listOf("image cache", "artwork cache", "thumbnails", "pictures", "storage", "clear image cache")
        ),
        SearchableSettingItem(
            id = "storage_clear_song_cache",
            title = "Clear Song Cache",
            description = "Free up storage space by clearing cached audio tracks without affecting downloaded offline songs",
            category = "Storage & Cache",
            iconRes = R.drawable.delete,
            route = "settings/storage",
            targetKey = "storage_clear_song_cache",
            keywords = listOf("clear cache", "delete cache", "free space", "clean storage", "wipe cache")
        ),
        SearchableSettingItem(
            id = "storage_clear_image_cache",
            title = "Clear Image & Artwork Cache",
            description = "Clear downloaded image thumbnails and artist photos to reclaim device memory",
            category = "Storage & Cache",
            iconRes = R.drawable.delete,
            route = "settings/storage",
            targetKey = "storage_clear_image_cache",
            keywords = listOf("clear images", "wipe thumbnails", "free space", "clean memory")
        ),
        SearchableSettingItem(
            id = "storage_download_directory",
            title = "Download Location & SD Card",
            description = "Choose whether offline downloaded songs are stored in internal memory or external SD card",
            category = "Storage & Cache",
            iconRes = R.drawable.download,
            route = "settings/storage",
            targetKey = "storage_download_location",
            keywords = listOf("download location", "sd card", "internal storage", "folder", "save offline", "path")
        ),
        SearchableSettingItem(
            id = "storage_cache_during_playback",
            title = "Pre-Cache Next Songs",
            description = "Automatically buffer and pre-cache upcoming songs in queue to prevent playback stutter",
            category = "Storage & Cache",
            iconRes = R.drawable.storage,
            route = "settings/storage",
            targetKey = "storage_pre_cache",
            keywords = listOf("pre-cache", "buffer", "playback cache", "stutter", "gapless buffer", "smooth playback")
        ),

        // ====================================================================
        // 8. PRIVACY, HISTORY & SECURITY
        // ====================================================================
        SearchableSettingItem(
            id = "privacy_pause_listen_history",
            title = "Pause Listening History",
            description = "Prevent newly played songs from being recorded in your listening history and stats",
            category = "Privacy & History",
            iconRes = R.drawable.history,
            route = "settings/privacy",
            targetKey = "privacy_pause_listen_history",
            keywords = listOf("pause history", "history", "listening history", "private listening", "incognito history")
        ),
        SearchableSettingItem(
            id = "privacy_clear_listen_history",
            title = "Clear Listening History",
            description = "Permanently delete all previously recorded song playback history and timestamps",
            category = "Privacy & History",
            iconRes = R.drawable.delete_history,
            route = "settings/privacy",
            targetKey = "privacy_clear_listen_history",
            keywords = listOf("clear history", "delete history", "wipe history", "reset history")
        ),
        SearchableSettingItem(
            id = "privacy_pause_search_history",
            title = "Pause Search History",
            description = "Stop saving recent search queries and keyword suggestions",
            category = "Privacy & History",
            iconRes = R.drawable.search_off,
            route = "settings/privacy",
            targetKey = "privacy_pause_search_history",
            keywords = listOf("pause search", "search history", "recent searches", "incognito search")
        ),
        SearchableSettingItem(
            id = "privacy_clear_search_history",
            title = "Clear Search History",
            description = "Delete all saved search keywords and query suggestions",
            category = "Privacy & History",
            iconRes = R.drawable.clear_all,
            route = "settings/privacy",
            targetKey = "privacy_clear_search_history",
            keywords = listOf("clear search", "wipe search", "delete queries", "reset search")
        ),
        SearchableSettingItem(
            id = "privacy_disable_screenshot",
            title = "Disable Screenshots (Secure Window)",
            description = "Block screenshots and screen recording across the app for maximum privacy",
            category = "Privacy & History",
            iconRes = R.drawable.screenshot,
            route = "settings/privacy",
            targetKey = "privacy_disable_screenshot",
            keywords = listOf("screenshot", "screen record", "secure window", "flag secure", "incognito", "privacy")
        ),

        // ====================================================================
        // 9. INTEGRATIONS & CLOUD
        // ====================================================================
        SearchableSettingItem(
            id = "integrations_discord_rpc",
            title = "Discord Rich Presence (RPC)",
            description = "Show Now Playing track title, artist name, and live playback progress in your Discord profile status",
            category = "Integrations",
            iconRes = R.drawable.discord,
            route = "settings/integrations/discord",
            targetKey = "integrations_discord",
            keywords = listOf("discord", "rpc", "rich presence", "status", "now playing discord", "token", "gaming")
        ),
        SearchableSettingItem(
            id = "integrations_lastfm",
            title = "Last.fm Scrobbler",
            description = "Automatically scrobble played songs, sync loved tracks, and track personal music charts",
            category = "Integrations",
            iconRes = R.drawable.music_note,
            route = "settings/integrations/lastfm",
            targetKey = "integrations_lastfm",
            keywords = listOf("lastfm", "scrobbler", "scrobble", "loved tracks", "music charts", "stats", "charts")
        ),
        SearchableSettingItem(
            id = "integrations_listen_together",
            title = "Listen Together (Group Session)",
            description = "Host or join synchronized real-time shared listening rooms with friends over the internet",
            category = "Integrations",
            iconRes = R.drawable.earbud_case,
            route = "settings/integrations/listen_together",
            targetKey = "integrations_listen_together",
            keywords = listOf("listen together", "group session", "room", "share", "party", "sync audio", "friends")
        ),
        SearchableSettingItem(
            id = "integrations_spotify_import",
            title = "Spotify Playlist Importer",
            description = "Import and transfer your favorite playlists and liked songs directly from Spotify into MKR Music",
            category = "Integrations",
            iconRes = R.drawable.spotify,
            route = "settings/spotify",
            targetKey = "integrations_spotify",
            keywords = listOf("spotify", "import", "transfer", "migrate", "playlists", "spotify sync")
        ),

        // ====================================================================
        // 10. SYSTEM, BACKUP & UPDATES
        // ====================================================================
        SearchableSettingItem(
            id = "account_login",
            title = "Google & YouTube Music Sync",
            description = "Log in with your Google account to sync your playlists, liked songs, and subscriptions",
            category = "Account & System",
            iconRes = R.drawable.account,
            route = "settings/account",
            targetKey = "account_login",
            keywords = listOf("google", "account", "login", "youtube music", "sync", "playlists", "sign in")
        ),
        SearchableSettingItem(
            id = "account_listening_summary",
            title = "Listening Summary & Stats",
            description = "View your top played artists, most repeated songs, total listening hours, and genres",
            category = "Account & System",
            iconRes = R.drawable.history,
            route = "settings/listening_summary",
            targetKey = "account_summary",
            keywords = listOf("summary", "stats", "statistics", "top songs", "top artists", "hours listened", "analytics")
        ),
        SearchableSettingItem(
            id = "backup_export_data",
            title = "Export Backup",
            description = "Export your playlists, song favorites, listening history, and preferences to a backup file",
            category = "Backup & Restore",
            iconRes = R.drawable.restore,
            route = "settings/backup_restore",
            targetKey = "backup_export",
            keywords = listOf("backup", "export", "save data", "backup file", "playlists backup")
        ),
        SearchableSettingItem(
            id = "backup_restore_data",
            title = "Restore Backup File",
            description = "Import and restore previously backed up playlists, settings, and database",
            category = "Backup & Restore",
            iconRes = R.drawable.restore,
            route = "settings/backup_restore",
            targetKey = "backup_restore",
            keywords = listOf("restore", "import", "load backup", "recover", "restore playlists")
        ),
        SearchableSettingItem(
            id = "backup_automatic_schedule",
            title = "Automatic Scheduled Backups",
            description = "Enable periodic automated backups weekly or automatically before applying app updates",
            category = "Backup & Restore",
            iconRes = R.drawable.restore,
            route = "settings/backup_restore/autobackup",
            targetKey = "backup_autobackup",
            keywords = listOf("autobackup", "auto backup", "scheduled backup", "weekly backup", "safe backup")
        ),
        SearchableSettingItem(
            id = "update_check",
            title = "Check for System Updates",
            description = "Check for MKR Music new releases and download latest universal APK directly",
            category = "Updates & About",
            iconRes = R.drawable.system_update_uptodate,
            route = "settings/update",
            targetKey = "update_check",
            keywords = listOf("update", "upgrade", "check update", "new version", "apk", "universal apk", "download update")
        ),
        SearchableSettingItem(
            id = "update_notification_permission",
            title = "Update Notification Permissions",
            description = "Allow notifications for newly available MKR Music version releases and security patches",
            category = "Updates & About",
            iconRes = R.drawable.notification,
            route = "settings/update/notification_permission",
            targetKey = "update_notifications",
            keywords = listOf("notification", "permission", "update alert", "patch", "new release notification")
        ),
        SearchableSettingItem(
            id = "update_commits",
            title = "Git Commits & Development History",
            description = "Inspect real-time commit history and active development progress from GitHub repository",
            category = "Updates & About",
            iconRes = R.drawable.info,
            route = "settings/commits",
            targetKey = "update_commits",
            keywords = listOf("commits", "git", "git log", "developer", "repository", "open source")
        ),
        SearchableSettingItem(
            id = "about_app",
            title = "About MKR Music",
            description = "Version v${BuildConfig.VERSION_NAME}, developer credits & GitHub repository",
            category = "Updates & About",
            iconRes = R.drawable.info,
            route = "settings/about",
            targetKey = "about_app",
            keywords = listOf("about", "version", "developer", "mkr", "github", "source code", "credits", "copyright")
        )
    )
}

/**
 * Searches the catalog with multi-word token matching and ranked relevance scoring.
 * Guarantees that ANY word matching in title, description, category, or keywords will return the item.
 */
fun searchSettingsCatalog(query: String, catalog: List<SearchableSettingItem>): List<SearchableSettingItem> {
    if (query.isBlank()) return emptyList()

    val normalizedQuery = query.lowercase().trim()
    val queryWords = normalizedQuery.split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
    if (queryWords.isEmpty()) return emptyList()

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
    var score = 0
    val title = item.normalizedTitle
    val desc = item.normalizedDescription
    val cat = item.normalizedCategory
    val keywords = item.normalizedKeywords

    // 1. Exact full query matches
    if (title == query) return 160
    if (title.startsWith(query)) score += 110
    else if (title.contains(query)) score += 90

    if (keywords.any { it == query }) score += 100
    else if (keywords.any { it.startsWith(query) }) score += 85
    else if (keywords.any { it.contains(query) }) score += 75

    if (desc.contains(query)) score += 80
    if (cat.contains(query)) score += 40

    // 2. Token & word level checks (Ensures ANY word matches)
    var wordsMatchedInTitle = 0
    var wordsMatchedInDesc = 0
    var wordsMatchedInKeywords = 0
    var tokenPrefixMatches = 0

    for (word in queryWords) {
        var wordMatched = false
        if (title.contains(word)) {
            wordsMatchedInTitle++
            wordMatched = true
        }
        if (desc.contains(word)) {
            wordsMatchedInDesc++
            wordMatched = true
        }
        if (keywords.any { it.contains(word) }) {
            wordsMatchedInKeywords++
            wordMatched = true
        }
        if (!wordMatched && item.allSearchTokens.any { it.startsWith(word) }) {
            tokenPrefixMatches++
            wordMatched = true
        }
    }

    val totalWords = queryWords.size
    val totalMatched = wordsMatchedInTitle + wordsMatchedInDesc + wordsMatchedInKeywords + tokenPrefixMatches

    // If not a single query word was found in title, description, keywords, or category, not a match
    if (score == 0 && totalMatched == 0) return 0

    // Multi-token scoring boosts
    score += wordsMatchedInTitle * 30
    score += wordsMatchedInDesc * 25
    score += wordsMatchedInKeywords * 20
    score += tokenPrefixMatches * 15

    // All-words bonus when searching multi-word phrases
    val allWordsPresent = queryWords.all { word ->
        title.contains(word) || desc.contains(word) || keywords.any { it.contains(word) } || item.allSearchTokens.any { it.startsWith(word) }
    }
    if (allWordsPresent && totalWords > 1) {
        score += 60
    }

    return score
}
