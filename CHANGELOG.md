# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

---

## [v1.1.0] - 2026-10-30

Milestone release featuring complete UI redesigns for Album, Artist, and Playlist views, universal letter-by-letter lyrics animation, an overhauled App Icon Selector with deferred switching, and architectural consolidation into a single unified application.

---

### 1. Album, Artist & Online Playlist UI Redesigns
- **Distinctive Visual Hierarchy**: Each screen features its own tailored visual layout and button aesthetics while sharing a unified, modern, and polished design language.
- **Album Screen**:
  - Elevated album artwork hero presentation with dynamic color extraction.
  - Redesigned header layout displaying release year, track count, total runtime, and record label details.
  - Prominent primary action capsule (Play All, Shuffle) with secondary quick-action buttons (Download, Favorite, Share).
  - Clean tracklist layout with clear track numbers, duration timestamps, explicit badges, and contextual track menus.
- **Artist Screen**:
  - Full-width artist banner and avatar header with follower stats and artist metadata.
  - Top songs spotlight section with quick play controls.
  - Categorized discography carousels separating studio albums, singles, EPs, and appearances.
  - Dedicated "Similar Artists" recommendations strip.
- **Online Playlist Screen**:
  - Expressive playlist header card with creator attribution, track count, and duration summary.
  - Floating action capsule with tactile spring physics for Play All, Shuffle, and Download.
  - Responsive layout optimized for seamless interaction across both compact and large screen form factors.

---

### 2. Universal Letter-by-Letter Lyrics Animation
- **Progressive Character Reveal**: Smooth letter-by-letter text animation reveals lyrics in real time according to song tempo and vocal timing.
- **Enabled by Default**: Character-level animation is active by default across all songs and all supported lyrics providers.
- **Resilient Playback Synchronization**:
  - Recalculates character progress instantaneously during seeking without flickering or desync.
  - Automatically pauses and resumes animation cleanly when playback pauses or resumes.
  - Prevents stale character carryover when skipping tracks or switching queues.
- **Configurable Lyrics Settings**: Dedicated toggle under Lyrics Settings to switch between letter-by-letter reveal, line-by-line sync, or standard text display.

---

### 3. App Icon Selector & Deferred Switching
- **Overhauled Icon Selector**:
  - Redesigned gallery grid showcasing high-resolution launcher icon artwork.
  - Displays icon titles, creator handles (`@username`), and platform badges with clean description-style layouts.
  - Adaptive grid columns that dynamically adjust between portrait, landscape, and large displays.
- **Deferred Icon Switching**:
  - Queues launcher icon changes and applies them seamlessly when the application exits or is sent to the background.
  - Eliminates abrupt process terminations and launcher restart stuttering while preserving user selection across restarts.
- **Expanded Icon Collection**:
  - Added *Wisteria Glow*, *Sakura Dusk*, and *Forest Glow* icons alongside existing collection for 10 unique launcher styles.

---

### 4. Settings Wireup & Search Enhancements
- **Complete Reference Wiring**: Fixed unresolved configuration references and aligned Appearance and Lyrics options.
- **Deep Search Indexing**: Granular keyword matching across all settings categories with direct highlighting of target options upon search selection.

---

### 5. Architectural Consolidation & Source Cleanup
- **Single Main Application**:
  - Consolidated build flavors into a single unified main application target (`:app`).
  - Google Cast functionality and dependencies integrated directly into the main application.
  - Standardized release build workflow to generate a single Universal Release APK (`:app:assembleUniversalRelease`).
- **Comprehensive Source Cleanup**:
  - Purged obsolete native C++ code, build actions, and unused microphone hardware permissions (`RECORD_AUDIO`).
  - Cleaned up orphaned database queries and unused string resources across all 19 supported languages.
  - Preserved Room database schema compatibility at version 34 to ensure 100% data integrity for existing installations.
- **Universal Multi-ABI Support**:
  - Retained full multi-architecture native library inclusion across `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.

---

## [v1.0.0] - 2026-10-24

Initial production release of MKR Music under the `com.kaif.music` application identity.

---

### 1. Application Identity & Core Architecture
- **Package Identifier**: `com.kaif.music`
- **Release Version**: `1.0.0` (Version Code: `10000`)
- **Technology Stack**: 100% Kotlin with modern Jetpack Compose Material 3 UI toolkit and reactive Android Media3 / ExoPlayer audio pipelines.
- **Data Persistence**: Offline-first Room Database architecture with automated entity relationships, indexed foreign keys, and atomic database queries.
- **Service Management**: Foreground `MusicService` implementation with resilient lifecycle handlers, media session integration, notification synchronization, and proactive audio focus conflict management.

---

### 2. Playback Pipeline & Audio Engine
- **Instant Stream Resolution**: Multi-client streaming pipeline that resolves audio endpoints with near-zero latency and handles rate limiting via automated session rotation.
- **Resilient Guest Authentication Fallback**: Authenticated account sessions automatically fall back to guest endpoint resolution in the event of expired session tokens or account-specific stream restrictions, ensuring continuous playback without disruption.
- **Predictive Stream Pre-caching**: Look-ahead chunk prefetching that analyzes upcoming tracks in the active queue to buffer audio streams ahead of track transitions, eliminating stutter and loading delays.
- **Gapless Audio Transitions**: Native gapless playback support eliminating silence and audio boundary artifacts between consecutive album tracks.
- **Configurable Crossfade**: Smooth audio fading between songs with user-defined crossfade durations (1s to 12s) and customizable volume slopes.
- **Audio Offload Engine**: Direct hardware audio offloading to device digital signal processors (DSP) to minimize CPU wakeups, preserve battery, and maintain high-fidelity audio output.
- **Audio Normalization**: ReplayGain metadata extraction and loudness normalization to ensure consistent perceived loudness across disparate tracks and albums.
- **Hardware Silence Skipping**: Automated silence detector capable of analyzing audio thresholds in real time and instantly skipping silent track intros, outros, and pauses.
- **Pitch & Playback Speed Controls**: Real-time adjustable playback speed (0.25x to 2.0x) and independent musical pitch adjustments.
- **Queue Duplicate Prevention**: Intelligent queue filtering that detects and blocks duplicate track additions to keep playback queues organized.
- **SponsorBlock Integration**: Integrated segment skipping engine supporting automatic and manual skipping of sponsors, self-promos, non-music intros, outros, and interactions with customizable skip notifications.

---

### 3. Equalizer & DSP Audio Processing
- **10-Band Parametric Equalizer**: High-precision multi-band equalizer supporting custom frequency band adjustments from 31 Hz to 16 kHz.
- **Preset Library**: Pre-tuned genre presets including Flat, Bass Boost, Rock, Pop, Jazz, Electronic, Vocal Boost, Classical, Hip Hop, and Acoustic.
- **Bass Boost & Virtualizer**: Dedicated low-frequency boost slider and surround-sound spatial audio virtualization.
- **Equalizer Profiles**: System-wide audio processor integration supporting persistent audio preferences across Bluetooth, wired headphones, and internal speakers.

---

### 4. Extended Player Styles & Visual Presentation
- **Seven Dedicated Player Layouts**:
  - **Classic Player**: Balanced design featuring central album artwork, prominent transport controls, and contextual queue actions.
  - **Immersive Player**: Edge-to-edge album artwork presentation with floating translucent controls.
  - **Editorial Player**: Typographic magazine-style layout highlighting artist information, track descriptions, and metadata.
  - **Apple Player**: Fluid presentation pairing artwork with full-screen dynamic ambient gradient backdrops.
  - **Cinematic Player**: High-contrast full-bleed visual layout emphasizing album artwork and dynamic controls.
  - **Material Extended Player**: Comprehensive Material You design featuring expanded track information chips and technical audio badges.
  - **Minimal Player**: Clean, distraction-free interface displaying essential playback controls.
- **Artwork Sizing & Framing**: Configurable album art scaling with options for rounded corners, square cards, circular vinyl presentation, and edge cropping.
- **Technical Audio Badges**: Real-time format badges displaying codec (Opus, AAC, FLAC), bitrate, sample rate (44.1 kHz, 48 kHz), and channel configuration.

---

### 5. Dynamic Ambient Theming & Shader System
- **Five Player Background Styles**:
  - **Blur**: Native hardware-accelerated Compose image blur with high-performance downsampled texture processing.
  - **Gradient**: Dynamic multi-stop vertical and radial gradients extracted from primary, secondary, and tertiary album art color palettes.
  - **Animated Glow**: Orbiting ambient light glow pulses that dynamically respond to track dynamics.
  - **Live Mesh**: Multi-layered real-time rotating mesh gradient canvas creating a fluid ambient background.
  - **Pure Black OLED**: Deep `#000000` AMOLED canvas across player, navigation, and library views to maximize battery savings.
- **Background Shader Gating**: Advanced lifecycle gating that automatically pauses animated mesh, glow, and canvas shaders when the expanded player is collapsed or when the screen is off, eliminating GPU overhead.

---

### 6. Interactive Seekbars, Gestures & Queue Dynamics
- **Seekbar Customization**:
  - **Material Standard**: Clean, responsive linear seekbar.
  - **Expressive Wavy**: Animated wave seekbar that moves dynamically during active playback.
  - **Squiggly Slider**: Stylized squiggle waveform with customizable stroke thickness and phase speed.
- **Interactive Queue Physics**: Drag-and-drop queue reordering with elevation lift, spring physics, and haptic feedback.
- **Interactive Scrub Previews**: Real-time timestamp scrub previews with time-remaining and elapsed time formatting.
- **Volume & Balance Sliders**: In-player volume slider with fine-grained attenuation controls and channel balance adjustments.
- **Sleep Timer**: Flexible timer supporting duration countdowns (5 to 120 minutes) or "Pause when track ends" mode with gradual audio fade-out.

---

### 7. Synchronized Lyrics, Translation & Romanization Engine
- **Offline Cached Lyrics**: Room database lookup prioritizes locally cached lyrics before initiating remote network requests, guaranteeing that previously played and downloaded tracks display synchronized lyrics in airplane mode.
- **Automatic Download Pre-caching**: Background worker automatically queries and stores synchronized lyrics into the local database upon track download completion.
- **Synchronized Scrolling**: Word-by-word and line-by-line synchronized scrolling with interactive tap-to-seek playback alignment.
- **Multi-Source Provider Hierarchy**: Extensible lyrics engine querying multiple providers with user-configurable priority ordering and automated fallback.
- **Phonetic Romanization**: One-tap in-player phonetic conversion for Asian languages:
  - Japanese Romaji
  - Korean Hangul
  - Chinese Pinyin
- **Real-Time Translation**: Dual-language viewing supporting AI and DeepL engines for side-by-side translated and original lyrics.
- **Manual Lyrics Editor**: Built-in dialog to edit, correct, or paste custom LRC formatted lyrics directly into the local database.

---

### 8. Complete Offline Capabilities & Storage Architecture
- **High-Speed Downloader**: Robust multi-threaded download manager supporting pause, resume, and automated network retry handling.
- **External Storage & SD Card Support**: Full support for directing downloads to secondary SD cards or custom user-selected storage directories.
- **Disk Cache Management**: Independent cache quotas and clearing mechanisms for audio track chunks and image disk caches.
- **Local Media Library Scanning**: Automated indexing of device audio files (MP3, FLAC, M4A, OGG, WAV) with metadata tag parsing and folder browsing.
- **Encrypted Backup & Restore**: Full JSON export and restore for playlists, track favorites, listening history, and preferences with optional automated weekly backup scheduling.

---

### 9. Home & Dynamic Discovery Engine
- **Personalized Recommendations**: Dynamic radio and recommendation queues accurately matched to artist style, language, era, and listening mood.
- **Quick Picks & Speed Dial**: Instant local-first dashboard showcasing frequently played tracks with zero network loading delay.
- **Curated Content Sections**:
  - *Similar Recommendations*: Contextual suggestions derived from user listening history.
  - *Keep Listening*: Resume recently active playlists and albums.
  - *Forgotten Favorites*: Rediscover beloved tracks that haven't been played recently.
  - *Mood & Genre Chips*: Quick-filter pills for Chill, Energy, Focus, Workout, Romance, and Commute.
  - *Community Playlists*: Curated playlists covering trending genres and eras.
- **Listening Review Recap**: Dedicated recap dashboard summarizing top tracks, artists, genres, and listening hours.

---

### 10. Search Architecture & Deep Full-Text Settings Index
- **Deep Settings Search Index**: Pre-tokenized full-text multi-word search engine indexing 100 configuration items, descriptions, subsettings, keywords, and nested categories with instant relevance ranking and direct target highlighting.
- **Global Media Search**: Universal search bar supporting fast multi-category queries across songs, artists, albums, and playlists.
- **Instant Search Suggestions**: Search autocomplete with recent query history chips and instant one-tap removal.
- **Voice Search Integration**: Direct speech-to-text voice search capabilities.

---

### 11. Navigation, Themes & Customization Hub
- **Cohesive Header Identity**: Top-left branding with icon and text pairings across both Home (App Logo + App Name with interactive easter egg) and Library (Library Icon + Library Name).
- **Four Navigation Bar Architectures**:
  - *Floating Glass Capsule*: Translucent floating navigation dock with dynamic blur.
  - *Expressive Pill*: Compact pill container with active tab highlight indicators.
  - *Clean Material*: Standard Material You bottom navigation dock.
  - *Amber Dock*: Dynamic tinted floating dock.
- **Color Palettes & Dynamic Wallpaper Theming**:
  - Over 20 handcrafted color palettes (Amber Warmth, Obsidian, Slate, Emerald, Ocean, Rose, Sunset, etc.).
  - Material You dynamic wallpaper color extraction (Android 12+).
  - Dynamic artwork theming matching primary accents to current album art.
  - Pure Black OLED theme mode for dark mode battery conservation.
- **App Icon Gallery**: Selection of unique launcher icons including Cyber Dots, Sunset Pastel, Retro Synthwave, Aurora Glow, and Matte Obsidian.
- **Typography & Font Engine**: Customizable system and bundled typography fonts.

---

### 12. Social Synchronization: Listen Together
- **Low-Latency Room Synchronization**: Dedicated WebSocket connection enabling multi-user synchronized playback.
- **Host & Guest Controls**: Room creation, custom invite codes, guest join approval requests, and synchronized play/pause/seek events.
- **Chat & Room Management**: Real-time room chat with participant rosters and connection status indicators.
- **Heartbeat & Reconnection**: Automated heartbeat monitoring and resilient auto-reconnect handling for seamless listening across network handoffs.

---

### 13. Third-Party Integrations & Metadata Scrobbling
- **Discord Rich Presence (RPC)**: Real-time status broadcasting displaying current track title, artist, album, album art, and elapsed time on Discord.
- **Last.fm Scrobbler**: Automatic scrobbling and now-playing status updates with customizable scrobble thresholds.

---

### 14. Home Screen Interactive Widgets
- **Music Player Widget**: Resizable 4x2 home screen widget featuring playback transport controls, track title, artist name, and album artwork.
- **Turntable Vinyl Widget**: Circular animated vinyl turntable widget displaying album art and playback controls.

---

### 15. Performance, Battery & Lifecycle Optimizations
- **Shader Deactivation on Collapse**: Halts animated mesh, glow, and canvas shaders when the player is collapsed or backgrounded to eliminate unnecessary GPU work.
- **Throttled Progress Polling**: Switches slider polling from 100ms to 500ms during mini-player and collapsed states to minimize CPU wakeups.
- **Widget IPC Throttling**: Regulated home-screen widget IPC binder updates to 1Hz, saving 80% of binder transactions and CPU wakeups.
- **Consolidated Storage Writes**: Optimized persistent queue disk serialization to prevent flash storage wear.
- **Graceful Process Termination**: Clean service lifecycle handling and background resource release to eliminate UI freeze and lag upon clearing tasks.

---

### 16. Technical Specifications & Compatibility
- **Build Variant**: Universal APK (`assembleUniversalRelease`).
- **Target SDK**: Android 15 (API 35).
- **Minimum SDK**: Android 7.0 (API 24).
- **License**: GNU General Public License v3.0 (GPL-3.0).
