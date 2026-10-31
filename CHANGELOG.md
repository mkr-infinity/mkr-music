# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/releases/mkr-music-v1.1.0-poster.png" alt="MKR Music v1.1.0 Release Poster" width="100%" />
</p>

---

## [v1.1.0] - 2026-10-31

Milestone release delivering instant audio streaming, intelligent background caching, complete UI redesigns for Album, Artist, and Playlist views, universal synchronized lyrics animation with dynamic provider selection, hardware equalizer integration with fine-tuned presets, default floating glass capsule navigation, an overhauled App Icon gallery with deferred switching, and architectural consolidation into a single Universal Release APK.

---

### 🚀 Added
- **Default Floating Glass Navigation:** Default floating frosted glass navigation bar featuring fluid pill-shaped buttons, active gradient glows, and tactile spring press animations.
- **Audio Output Equalizer Quick-Access:** Dedicated Equalizer button integrated into the Audio Output sheet for immediate sound customization without leaving playback.
- **Hardware Equalizer Presets:** High-precision built-in acoustic presets including *Flat*, *Bass Boost*, *Pure Clarity*, *MKR Signature*, *Electronic*, and *Rock*.
- **Dynamic Lyrics Provider Switching:** Real-time provider switcher chip in the lyrics view allowing listeners to seamlessly swap between available lyric sources on the fly with custom source logos.
- **Lyrics Phonetics & Romanization Toggle:** Quick-access toggle on the lyrics screen providing instantaneous romanization without navigating to settings.
- **Dynamic Onboarding Ambient Effects:** Fluid animated pulse wave behind the welcome logo and real-time color-morphing ambient backdrops on the features onboarding carousel that adapt to each feature theme.
- **Comprehensive Listening Breakdown:** Rich stats summary displaying total listening duration, active listening days, and daily listening averages.
- **11 Launcher Icon Styles:** Complete gallery of high-resolution launcher icons including *Default*, *Monochrome*, *Retro*, *Neon*, *Gold*, *Creative Cracked Record Cover*, *Creative Vinyl Forest*, *Creative Moonlit Cherry Blossom*, *Creative Neon Wisteria*, *Creative Autumn Glow*, and *Creative Cyberpunk Neon*.
- **Deferred Launcher Icon Switching:** Safe queued launcher icon application that applies the chosen icon when the app is sent to the background (`MainActivity.onStop()`) or on cold startup, eliminating sudden process terminations and launcher restart stuttering.
- **Creator Attribution in Icon Gallery:** Description-style attribution badges displaying creator usernames (`@kaif-coder`, `@mkr-infinity`) and GitHub platform badges for all 11 launcher icons.
- **Configurable Background Cache Delay:** Dedicated slider under Storage & Cache settings (5s–90s, default 5s) ensuring only actively listened tracks are cached to device storage.
- **Watch-Inspired History Navigation:** Dedicated timepiece action capsule on the Home screen providing immediate one-tap navigation to listening history and playback events.
- **Multi-Density Adaptive Launcher Assets:** Full set of `anydpi-v26` adaptive XML, round XML, and 5 legacy density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) for all launcher icon styles.

---

### 🌟 Improved
- **Instantaneous Like & Favorite Response:** Optimistic UI updating on like button interactions across all player layouts and the mini-player for zero-latency feedback.
- **Equalizer Session Card Styling:** Displays the active track title directly on the equalizer session card with clean typography, zero raw identifiers, and zero bullet dots.
- **Instant Audio Playback Pipeline:** Starts audio streaming immediately upon selection by bypassing initial cache write buffer lag, delivering near-instant online playback.
- **Redesigned Album Screen:** Elevated album artwork hero presentation with dynamic color extraction, release year, track count, total runtime, record label details, and prominent primary action capsules (Play All, Shuffle, Download, Favorite, Share).
- **Redesigned Artist Screen:** Full-width header banner with artist avatar, subscriber count stats, top songs spotlight, and categorized discography carousels separating studio albums, singles, EPs, and appearances.
- **Redesigned Online Playlist Screen:** Expressive header card with creator attribution, track count, and duration summary, accompanied by tactile floating action controls.
- **Universal Letter-by-Letter Lyrics Animation:** Character-level progressive reveal active across all songs and providers with smooth offscreen Canvas clipping (`BlendMode.DstIn`) and zero layout shifts.
- **Word-by-Word Lyrics Precision:** Refined word sync algorithm to ensure smooth progression without unexpected line-end jumping.
- **Notification Media Controls:** Restored media seekbar slider seeking directly in the system media notification.
- **Settings Discovery & Deep Search:** Pre-tokenized keyword search indexing all configuration items, toggles, and descriptions with instant relevance ranking and direct target highlighting.
- **Account-Aware Header Navigation:** Dynamic avatar button on the Home screen that displays current account authentication status with smooth transitions.

---

### 🛠️ Fixed
- **Second-Launch Startup Crash:** Resolved window and splash screen collision on second launch after onboarding completion, ensuring smooth immediate entry into the main application.
- **Local Liked Songs Library Updates:** Fixed Room database liked song persistence without login, guaranteeing that every liked song is immediately saved and instantly reflected in the Liked Songs playlist.
- **Reliable Song Liking Pipeline:** Fixed desync between UI state, Room database entities, and synchronization tasks, ensuring likes persist reliably across all menus, players, and views.
- **Stats Top-Right Navigation & Wrapped Stability:** Resolved crashes and infinite loops in Wrapped recap, adding safe bounds checking for smaller libraries and eliminating pre-composition auto-advances.
- **Wrapped Display & Layout Freezes:** Clamped text measurement while loops in recap screens to prevent freezes across varying screen aspect ratios and tablets.
- **Appearance Settings Clean-up:** Removed redundant duplicate top bar quick-access setting from Auto Playlists while preserving the home screen shortcut.
- **Strict Clean Typography Policy:** Enforced complete elimination of unwanted bullet dots and separator symbols across all song info and status strings.
- **Unused Translation Cleanup:** Removed 35 obsolete unused translations to ensure clean multi-locale builds.
- **Settings Reference Wiring:** Resolved broken, missing, or unmapped configuration references across Appearance, Lyrics, and Storage settings screens.
- **Lyrics Seeking Synchronization:** Fixed character desync during seek operations, pausing animations cleanly on pause and preventing stale character carryover when switching tracks.
- **System Window Insets & Overlaps:** Resolved navigation bar, status bar, and mini-player clipping across all primary screens using `LocalPlayerAwareWindowInsets` and `WindowInsets.ime`.
- **App Icon Grid Sizing:** Hardened dialog layout constraints with `heightIn(max = 420.dp)` and adaptive column sizing (`minSize = 135.dp`) preventing title truncation on compact screens.
- **Queue Duplicate Track Suppression:** Fixed duplicate track insertion across all queue entry paths, keeping playback queues clean.
- **Offline Cache Indexing:** Resolved caching edge cases ensuring downloaded songs and cached tracks are properly recognized across History, Library, and offline playback modes.

---

### ⚡ Performance
- **Low-Priority Cache IO Threads:** Background song caching runs on dedicated IO coroutine dispatchers with automatic cancellation if a track is skipped or paused early, preventing playback stutter.
- **Offscreen Lyrics Rendering:** High-performance Canvas shader rendering that minimizes recompositions during word-by-word karaoke animations.
- **Optimized Room Database Queries:** Preserved Room schema at version 34 (`19aa47f2...`) with indexed foreign keys and atomic auto-migrations for fast local library loading.

---

### 🎨 UI/UX
- **Material 3 Expressive Refinements:** Edge-to-edge layouts, tactile spring physics on interactive buttons, subtle borders on action cards, and coherent dark mode themes.
- **Lyrics Customization Panel:** Dedicated toggles under Lyrics Settings allowing users to easily choose between character-level animation, line-by-line sync, or static text.
- **Refined Navigation Bar:** Streamlined bottom navigation and player bar docking with adaptive gesture padding.

---

### 🛡️ Reliability & Architecture
- **Single Unified Application Architecture:** Consolidated build variants into a single production `:app` target producing a Universal Release APK with full multi-ABI support (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).
- **Complete Elimination of Dead Code:** Safely eradicated legacy Shazam/recognition remnants, C++ CMake FFTW actions, unused microphone permissions (`RECORD_AUDIO`), and orphaned string resources.
- **Fail-Safe Release Workflow:** Automated GitHub Actions workflow targeting `:app:assembleUniversalRelease` with SHA-256 generation, APK integrity validation, and release notes automation.

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
