# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

---

## [v1.0.0] - 2026-10-22

First release of MKR Music under the new `com.kaif.music` application identity.

### Application Identity
- **Package**: `com.kaif.music`
- **Version Name**: `1.0.0`
- **Version Code**: `10000`
- Canonical version managed from `version.properties` with deterministic version-code derivation.

### Playback Engine
- Instant audio playback with non-blocking stream initialization.
- Configurable delayed background caching (5s–90s threshold).
- Crossfade and gapless playback transitions.
- Native 10-band parametric equalizer, pitch and speed controls, volume normalization.
- SponsorBlock non-music segment skipping.

### Navigation
- Four selectable navigation architectures: Floating Glass, Expressive Pill, Clean Material, and Amber Dock.
- Persistent navigation style preference.
- Mini-player integration with all navigation styles.

### Theme and Appearance
- Artwork-adaptive dynamic theming with Material 3 color extraction.
- Light, Dark, and Pure Black AMOLED modes.
- 20+ selectable color palettes.
- Five custom launcher icons (Cyber Dots, Sunset Pastel, Retro Synthwave, Aurora Glow, Matte Obsidian).
- Capsule-style UI selectors for appearance settings.

### Listen Together
- Self-hosted sync server at `wss://listen-together-mkr-music.onrender.com`.
- Room creation, joining, approval, and synchronized playback.
- Heartbeat, reconnection, and session management.
- Accessible via Settings only (no Home header shortcut).

### Lyrics
- Synchronized word-by-word lyrics with tap-to-seek.
- Multi-source lyrics provider engine.

### Widgets
- Music Player home-screen widget.
- Turntable home-screen widget.

### Build and CI
- GitHub Actions release workflow with `workflow_dispatch` trigger.
- Configuration-cache-compatible `printAppVersion` Gradle task.
- Universal APK build variant (`assembleUniversalGmsRelease`).
- FFTW native library build and cache action.

### Credits
- See README.md for full attribution and third-party license notices.
