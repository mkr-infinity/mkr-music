# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

---

## [v1.0.0] - 2026-10-24

Initial release of MKR Music under the `com.kaif.music` application identity.

### Application Identity & Architecture
- **Package Name**: `com.kaif.music`
- **Version Name**: `1.0.0`
- **Version Code**: `10000`
- **Architecture**: 100% Kotlin with Jetpack Compose Material 3 and Media3 / ExoPlayer audio pipeline.
- **Database**: Offline-first Room Database architecture with automatic schema migration and relational mapping.
- **Service Architecture**: Foreground media service with robust lifecycle management, notification sync, and audio focus handling.

---

### Playback & Audio Engine
- **Instant Stream Resolution**: Multi-client streaming pipeline with automatic session rotation for low latency and zero buffering delays.
- **Resilient Authentication Fallback**: Authenticated account playback automatically falls back to guest resolution if credentials or tokens expire, ensuring uninterrupted playback and recommendation loading.
- **Predictive Stream Pre-caching**: Intelligent look-ahead queue prefetching that prepares audio chunks for upcoming tracks in the queue.
- **Gapless Playback & Crossfade**: Smooth transitions between songs with configurable crossfade durations and volume slopes.
- **Hardware DSP Offload**: Direct audio offloading to dedicated hardware digital signal processors to significantly reduce CPU activity and extend battery life.
- **Loudness Normalization**: ReplayGain metadata processing and automatic volume normalization to maintain consistent listening volume across tracks.
- **Silence Skipping**: Built-in hardware-level silence detector with instant skip capabilities to eliminate silent intros, outros, and gaps.
- **10-Band Equalizer**: Native 10-band parametric equalizer, bass boost, and audio virtualizer presets.
- **Queue Duplicate Prevention**: Option to automatically filter and prevent duplicate tracks from being added to the active playback queue.
- **SponsorBlock Integration**: Automatic non-music segment skipping (sponsors, self-promos, intros, outros, and intermissions) with category controls and customizable toast notices.

---

### Expanded Player & Visual Experience
- **Seven Distinct Player Styles**:
  - *Classic Player*: Clean, balanced transport controls with prominent artwork and controls.
  - *Immersive Player*: Edge-to-edge artwork presentation with contextual controls.
  - *Editorial Player*: Magazine-inspired typography and layout styling.
  - *Apple Player*: Fluid presentation with dynamic ambient backdrops.
  - *Cinematic Player*: High-contrast full-bleed visual experience.
  - *Material Extended Player*: Complete Material You layout with detailed track info chips.
  - *Minimal Player*: Distraction-free, essential transport interface.
- **Dynamic Artwork Background Styles**:
  - *Blur*: Hardware-accelerated native Compose blur rendering.
  - *Gradient*: Multi-stop adaptive gradients extracted dynamically from active album art.
  - *Animated Glow*: Smooth orbiting ambient light pulses.
  - *Live Mesh*: Multi-layered dynamic mesh gradient simulation.
  - *Pure Black OLED*: True `#000000` black canvas across all surfaces to maximize AMOLED power savings.
- **Interactive Seekbars**: Support for standard Material, expressive wavy, and squiggly slider styles with real-time scrub previews.
- **Interactive Queue Physics**: Drag-and-drop queue reordering with elevation lift, spring physics, and haptic feedback.
- **Sleep Timer**: Configurable sleep timer with gradual volume fade-out and "End of track" trigger mode.

---

### Offline Lyrics & Translation
- **Offline Cached Lyrics**: Room database lookup prioritizes locally cached lyrics before initiating remote requests, allowing previously played and downloaded tracks to display full lyrics without internet access.
- **Automatic Download Pre-caching**: Automatically fetches and saves lyrics to the local database upon track download completion.
- **Synchronized Lyrics**: Word-by-word and line-by-line synchronized scrolling with interactive tap-to-seek functionality.
- **Multi-Source Provider Engine**: Pluggable provider hierarchy with user-defined priority ordering and automatic fallback.
- **Phonetic Romanization**: Instant one-tap romanization for Japanese Romaji, Korean Hangul, and Chinese Pinyin.
- **AI & DeepL Translation**: Built-in translation pipeline for dual-language side-by-side lyrics viewing.

---

### Home & Discovery
- **Personalized Recommendations**: Radio and recommendation queues accurately matched to artist style, language, era, and listening mood.
- **Quick Picks & Speed Dial**: Rapid access to frequent tracks with local-first database retrieval.
- **Curated Sections**: Similar recommendations, forgotten favorites, keep listening, and mood/genre chips.
- **Listening History**: Complete chronological history tracking with per-song play statistics.
- **Recap**: Listening review recap highlighting top played tracks, artists, and listening milestones.

---

### Search Engine
- **Deep Settings Search**: Full-text multi-word token search across 100 configuration items, descriptions, subsettings, and keywords with instant relevance ranking and direct setting highlight navigation.
- **Unified Media Search**: Fast global search across songs, artists, albums, and playlists with search suggestions, recent chips, and fuzzy matching.

---

### Navigation, Themes & Customization
- **Top-Left Header Layout**: Unified branding with icon and text pairings across both Home (App Logo + App Name) and Library (Library Icon + Library Name).
- **Four Navigation Bar Architectures**:
  - *Floating Glass Capsule*: Translucent floating navigation dock.
  - *Expressive Pill*: Compact pill container with active tab highlights.
  - *Clean Material*: Standard Material You bottom navigation dock.
  - *Amber Dock*: Dynamic tinted floating dock.
- **Theming & Color Palettes**:
  - Over 20 curated color palettes plus dynamic Material You wallpaper color extraction.
  - Dynamic artwork theming matching active album artwork.
  - Pure Black OLED theme mode for dark mode battery conservation.
- **App Icon Gallery**: Customizable home screen launcher icons.
- **Typography**: System and custom font family selection.

---

### Storage, Downloads & Backup
- **High-Speed Downloader**: Multi-threaded download manager with pause, resume, and retry handling.
- **Storage Destination**: Configurable download storage location with SD card / external storage support.
- **Cache Management**: Independent control and clear actions for player audio cache and image disk cache.
- **Encrypted Backup & Restore**: Full JSON export and restore for playlists, favorites, history, and preferences with optional automated weekly backup scheduling.

---

### Listen Together & Integrations
- **Synchronized Group Listening**: Low-latency WebSocket room synchronization with host controls, invite codes, and synchronized playback.
- **Discord Rich Presence (RPC)**: Live status broadcasting showing track title, artist, album, and elapsed time on Discord.
- **Last.fm Scrobbler**: Automatic scrobbling and now-playing broadcast support.

---

### Battery & Resource Optimizations
- **Background Shader Gating**: Automatically halts dynamic animated canvas and mesh shaders when the expanded player is collapsed or backgrounded.
- **Throttled Progress Polling**: Switches slider polling from 100ms to 500ms during mini-player and collapsed states to minimize CPU wakeups.
- **Widget IPC Throttling**: Regulated home-screen widget IPC binder updates to 1Hz.
- **Consolidated Storage Writes**: Optimized persistent queue disk serialization to prevent flash storage wear.
- **Graceful Lifecycle Management**: Clean service lifecycle handling and background resource release to prevent UI lag.

---

### Build & Release Information
- **Build Variant**: Universal APK (`assembleUniversalGmsRelease`).
- **Target SDK**: Android 15 (API 35).
- **Minimum SDK**: Android 7.0 (API 24).
- **License**: GNU General Public License v3.0 (GPL-3.0).
