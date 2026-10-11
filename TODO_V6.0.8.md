# MKR Music v6.0.8 Feature Implementation & Refactoring Plan

## 1. Version Update Everywhere to `v6.0.8`
- [x] `app/build.gradle.kts`: update `versionName` to `"6.0.8"` (and ensure `versionCode` is consistent).
- [x] `app/src/main/kotlin/com/music/mkr/github/GitHubConfig.kt`: set `VERSION_LABEL = "v6.0.8 Stable"`.
- [x] `app/src/main/kotlin/com/music/mkr/ui/screens/settings/SettingsScreen.kt`: update version badge and text to `"v6.0.8 Stable"`.
- [x] `README.md`: update version badge to `v6.0.8 Stable`.
- [x] `.github/workflows/release.yml`: ensure version extraction and release tags dynamically support `v6.0.8`.

## 2. Notification Icon & App Small Logo Harmonization
- [x] Ensure `R.drawable.mkrmusicnotification` and all notification handlers (`MusicService`, `UpdateNotificationHelper`, `NewReleaseNotificationHelper`, `ViviNotificationProvider`) strictly use the authentic MKR Line Note logo monochrome asset.
- [x] Replace any leftover `R.drawable.icon` or generic notification icon usages with the authentic `R.drawable.mkrmusicnotification` for notifications and authentic high-res largeIcon.

## 3. Settings UI: Capsule Header & Top Search Bar
- [x] Design and add an expressive **Capsule-style Search Bar** at the top of `SettingsScreen` with smooth real-time query filtering across all settings items.
- [x] Tapping a filtered search result navigates directly to that setting destination route.
- [x] Elevate the top branding banner into a modern **Capsule Card**: "MKR Music v6.0.8 Stable • Crafted by @mkr-infinity" with sleek glassy gradient accents, smooth pill tags, and clean Material 3 typography.
- [x] Modernize settings categories into clean expressive rounded card groups with clear iconography (no emojis).

## 4. About Screen Modernization & Upstream Section Removal
- [x] Completely remove the "Upstream Project" row and link from `AboutScreen.kt`.
- [x] Redesign Developer Hero Card into a beautiful, modern single-line layout:
  - High-res avatar on the left in a sleek bordered circle.
  - Right column: Developer name in a single crisp line (responsive typography), subtitle pill tag `A Tech Enthusiast`, and version tag `v6.0.8 Stable`.

## 5. Update Settings: Remove Beta Channel
- [x] Remove beta update switches and references in `UpdateSettings.kt` (only stable releases are published).

## 6. Top-Left Logo Easter Egg: 5-Click Activation & 3-Second Inactivity Reset
- [x] In `MainActivity.kt`:
  - Reset click counter if user pauses tapping for 3 seconds.
  - Do NOT trigger any toast until at least 5 clicks are reached within the 3-second window.
  - After 5 clicks, display fun developer easter egg messages.
  - Dismiss/stop toast when inactive for 3 seconds.

## 7. Intelligent Contextual Music Suggestions (Same Mood & Era/Genre Matching)
- [x] In `HomeViewModel.kt` (`loadSimilarRecommendations` and `enrichQuickPicksWithSimilar`):
  - Analyze the user's currently playing track and listening history metadata (e.g. 90s Indian, Bollywood, Lo-Fi, Retro, Synthwave).
  - Bias related endpoints and radio recommendation queries towards matched era, mood, and language/region keywords so recommendations mirror the exact vibe.

## 8. Automatic History Caching for Offline Playback
- [x] In `MusicService.kt` (during track finish / playback stats recording):
  - When a song is registered in listening history, automatically mark and cache the track into offline storage so users can play their history offline without manual downloads.

## 9. In-App Crash Diagnostic Dialog with Automatic Device & Log Report to GitHub
- [x] In `CrashActivity.kt` / `CrashHandler.kt`:
  - Enhance crash screen with a one-tap button: "Submit Report on GitHub".
  - Automatically format device model, Android OS version, app version (v6.0.8), architecture, and stack trace into a pre-filled GitHub Issue URL (`https://github.com/mkr-infinity/mkr-music/issues/new?title=...&body=...`).
  - Provide direct copy & share actions.

## 10. Clean App Termination on Task Clear & Battery Conservation
- [x] In `MusicService.kt` (`onTaskRemoved`):
  - When "Stop music on task clear" is enabled, completely terminate playback, cancel all foreground notifications, unregister media session, abandon audio focus, and stop the service cleanly to avoid background battery drain on older devices.

## 11. Fix Mid-Song Playback Skipping
- [x] Audit ExoPlayer error handling in `MusicService.kt`:
  - Ensure temporary network fluctuations or silence false-positives do not abruptly skip to the next track.
  - Guard `performInstantSilenceSkip` from executing during buffering and prevent jumping to end of song.

## 12. Default Floating Navigation Bar
- [x] Change `FloatingNavBarKey` default value to `true` in `MainActivity.kt`, `AppearanceSettings.kt`, and `PreferenceKeys.kt`. Users can toggle it off anytime in Appearance settings.
