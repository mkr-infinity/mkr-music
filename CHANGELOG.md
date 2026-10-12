# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

> [!NOTE]
> The changelog has been reset to establish a fresh new stage from the base model. All current platform features, architecture implementations, audio pipelines, and UI refinements are comprehensively documented below.

---

## [v6.0.8 Stable] - 2026-10-12

### ⚡ Playback & Streaming Engine
- **Instant Playback Start**: Re-architected streaming data source pipeline with non-blocking initial cache sinks, bypassing buffering lag for instantaneous track playback.
- **Configurable Delayed Caching**: New Storage & Cache setting allowing background caching only after continuous listening (adjustable slider from 5s to 90s, default 10s).
- **Decoupled Background Caching**: Background caching executes asynchronously via `CacheWriter` on low-priority IO dispatchers with zero interference to the active playback thread.
- **Auto-Cancellation on Skip/Pause**: Background caching cleanly cancels whenever a track is skipped or paused before reaching the configured threshold.
- **Crossfade & Gapless Playback**: Seamless audio crossfade transitions with configurable durations for smooth, uninterrupted listening sessions.
- **Audio Effects Engine**: Native 10-band equalizer, pitch and playback speed controls, and volume normalization.

### 💖 Support & Contributions
- **Chainsaw Spikes Action Button**: Custom-designed animated moving chainsaw teeth border on the Donate & Support button with a smooth continuous perimeter loop and zero text clipping.
- **Direct UPI Copy Card**: One-tap manual copy card for verified UPI payment details with tactile spring press feedback ("Copied ✓").
- **Official Payment App Logos**: Authentic vector logos for PhonePe, Google Pay, Paytm, BHIM UPI, and mobile banking apps.
- **One-Touch Note Remark**: Dedicated copy action for the payment remark (`"Support MKR Music"`).
- **Rotating Gratitude Toasts**: 6 randomized appreciation toast messages displaying on copy with automatic 4-second dismiss and instant reset on subsequent clicks.
- **External Contribution Link**: Direct integration with Buy Me a Coffee platform.

### ⚙️ Settings & System Updates
- **Granular Keyword Search**: Settings search engine indexes granular sub-options and keywords (e.g., searching "hide" displays all hide toggles across categories).
- **Search Deduplication**: Filtered results automatically deduplicate to prevent repetitive entries.
- **Simplified Update Experience**: Completely removed in-app background downloaders, APK installers, flavour switchers, and cache cleaners in favor of direct GitHub Releases links.
- **Data Protection Guidance**: Prominent backup warning card instructing users to export an app backup prior to updating.
- **Clean Settings Toolbar**: Removed redundant top-right About capsule from the settings header for a clean, distraction-free view.

### 🎨 Onboarding & User Interface
- **Balanced Onboarding Hero**: Centered MKR Music logo enclosed in an elevated surface card with "Welcome to the world of music" tagline and developer badge.
- **Responsive Layout**: Replaced legacy animations with a vertically scrollable container ensuring full compatibility across all screen sizes and aspect ratios.
- **Subtle Aesthetic Borders**: Subtle borders added to developer profile cards and the home screen settings dropdown button for enhanced visual definition.
- **Material 3 Expressive UI**: Dynamic color harmonized themes, pure black AMOLED support, and expressive container styling.
- **Selectable App Icons**: 9 launcher icons organized into Anime Boys, Anime Girls, and Music Logos with dynamic launcher alias switching.

### 💾 Cache & Storage Architecture
- **Dual Cache Pipeline**: Independent storage allocation for playback cache, downloaded tracks, and image disk caches.
- **LRU Cache Protection**: Smart LRU cache eviction algorithms that protect explicitly downloaded tracks and local library files.
- **Configurable Cache Limits**: Granular song count (50, 100, 200, 500, 1000, Custom) and byte-size storage thresholds.
