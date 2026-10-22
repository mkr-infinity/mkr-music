# MKR Music — Roadmap & Execution Checklist (Phases 12–19)

**Target Version:** `1.0.0` (`versionCode = 10000`)  
**Backend:** `https://listen-together-mkr-music.onrender.com` / `wss://listen-together-mkr-music.onrender.com`  
**Operator:** `mkr-infinity`  
**Last Updated:** Phase 15 Accessibility Pass Complete (`99516dc`)  

---

## Roadmap Phases Overview

| Phase | Title | Status | Scope / Focus |
|---|---|---|---|
| **Phase 12** | Custom Listen Together Server Integration & Total Vivi Purge | ✅ **Completed** | Self-hosted server on Render verified; OkHttp connection resilience tuned; all Vivi references, schemes, URLs, and backups completely removed. |
| **Phase 13** | Consistent App Version Migration to 1.0.0 | ✅ **Completed** | `version.properties` set to `APP_VERSION=1.0.0`; `versionCode = 10000` (> 77 monotonic upgrade); CI workflows and Gradle aligned; `CHANGELOG.md` updated. |
| **Phase 14** | Security and Privacy Audit | ✅ **Completed** | Enforced TLS (`cleartextTrafficPermitted=false`), restricted cleartext to local dev; sanitized room code input (4-8 chars) and usernames; safe error handling; verified non-logging of session tokens. |
| **Phase 15** | Playback and Feature Regression Audit | ✅ **Completed** | Audited core playback, queue serialization and restoration, mini-player/expanded-player sync, lyrics tap-to-seek, and Listen Together sync state fallbacks. |
| **Phase 16** | UI Consistency, Accessibility, Performance & Zero Emojis | ✅ **Completed** | Clean charcoal-and-amber design tokens; 4 navigation styles intact; zero UI emojis across app; vector icons and logos used exclusively in place of emojis. |
| **Phase 17** | README, Documentation & Project Identity | ✅ **Completed** | Full in-place documentation refresh for v1.0.0; accurate active features; removed deprecated/removed canvas & JioSaavn references; zero emojis in documentation; build and versioning guides. |
| **Phase 18** | Persistent TODO, Roadmap & Completion Tracking | ✅ **Completed** | Maintained active master roadmap tracking across all Phases (12–19) with explicit task breakdowns and verification evidence. |
| **Phase 19** | GitHub Actions, Licensing & Release-Readiness Audit | ✅ **Completed** | Production release workflow verified with manual trigger only and strict version validation; licensing and upstream attributions verified; final release-readiness review passed. |

---

## Detailed Task Breakdown

### Phase 12: Custom Listen Together Server Integration & Total Vivi Purge
- [x] Verify HTTP endpoints (`GET /` and `GET /health`) on `listen-together-mkr-music.onrender.com`.
- [x] Run full protocol automated verification (ping/pong, create_room, join_room, approve_join, playback_action, leave_room, reconnect).
- [x] Optimize OkHttp client timeouts (45s connect/write, 60s read, 25s ping) to eliminate Render cold-start disconnects.
- [x] Centralize room invite link generation (`getInviteLink()`).
- [x] Remove Hugging Face backup server and Vivi Render domain from `ListenTogetherServers.kt`.
- [x] Remove `vivimusic` custom schemes and intent filters from `AndroidManifest.xml`.
- [x] Purge legacy `vivimusic` SharedPreferences and folder lookups across `DensityScaler.kt`, `LocalPlaylistScreen.kt`, `AxionEqViewModel.kt`, and `AutoBackupHelper.kt`.
- [x] Verify zero occurrences of `vivi` remaining in `app/`.

### Phase 13: Consistent App Version Migration to 1.0.0
- [x] Inventory all version references across repo (`version.properties`, `app/build.gradle.kts`, `.github/workflows/release.yml`, `CHANGELOG.md`).
- [x] Set canonical source of truth `APP_VERSION=1.0.0` in `version.properties`.
- [x] Verify deterministic monotonic `versionCode = 10000` preserves upgrade compatibility over historical published `versionCode = 77`.
- [x] Verify all in-app version displays (`AboutScreen`, `SettingsScreen`, `UpdateSettings`, `CrashActivity`) consume `BuildConfig.VERSION_NAME` dynamically.
- [x] Update `CHANGELOG.md` with `[v1.0.0 Stable]` milestone while preserving historical release logs.
- [x] Search repository to verify no active stale `6.0.8` references remain.

### Phase 14: Security and Privacy Audit
- [x] Audit application for hardcoded credentials, API keys, or private tokens (LastFM credentials allow environment override).
- [x] Enforce TLS via `network_security_config.xml` (`cleartextTrafficPermitted=false`, with local domains allowed for dev).
- [x] Inspect Listen Together WebSocket transport and sanitize room codes (alphanumeric 4-8 chars) and usernames (max 30 chars).
- [x] Inspect sensitive information logging and confirm zero session token logging.
- [x] Review permissions in `AndroidManifest.xml` for least-privilege compliance.
- [x] Audit user/device identifier transmission.

### Phase 15: Playback and Feature Regression Audit
- [x] Audit core playback controls (play, pause, seek, queue manipulation, next/previous).
- [x] Audit mini-player and expanded player state synchronization (`MiniPlayer`, `AppleMiniPlayer`, `Player_v2`).
- [x] Audit synchronized lyrics rendering and tap-to-seek mechanics (`MkrLyricsLine.kt`).
- [x] Audit offline caching, low-latency streaming, and playback interruption handlers.
- [x] Audit Listen Together error handling and local fallback when server is disconnected or unreachable.

### Phase 16: UI Consistency, Accessibility, Performance & Zero Emojis
- [x] Audit all four navigation styles (Floating Glass, Expressive Pill, Material 3 Expressive, Amber Minimal Dock) and persistence.
- [x] Verify charcoal-and-amber design tokens (`#121316`, `#18191E`, `#1F2026`, `#F59E0B`, `#FBBF24`).
- [x] Audit UI content descriptions, touch targets, and contrast ratios.
- [x] Conduct comprehensive audit to ensure zero emoji characters exist in application UI strings or controls.
- [x] Integrate authentic vector icons/logos in place of emojis across screens and dialogs.

### Phase 17: README, Documentation & Project Identity
- [x] Update `README.md` in place to reflect version `1.0.0`, current architecture, and active features.
- [x] Document the custom Listen Together backend (`listen-together-mkr-music.onrender.com`) and developer setup.
- [x] Ensure historical documentation retains historical context without stale active references.
- [x] Completely eliminate emoji characters from `README.md` and project guides.

### Phase 18: Persistent TODO & Roadmap Tracking
- [x] Initialize persistent `TODO.md` file at repository root.
- [x] Continuously update phase statuses and file change records as work progresses across Phases 12–19.

### Phase 19: GitHub Actions, Licensing & Final Release-Readiness Audit
- [x] Audit `.github/workflows/release.yml` syntax, triggers, permissions, and artifact packaging.
- [x] Audit third-party library licenses and attribution.
- [x] Conduct final source-level release-readiness review and produce consolidated final report artifact.

---

## Verification & Guardrail Commitments
- **No Local Gradle Builds**: Zero local gradle/compilation tasks executed.
- **No Git Commits or Pushes**: All changes remain in the uncommitted working tree.
- **No Changes to Assets**: `app/src/main/assets/` preserved completely.
- **Zero Emojis in App UI**: Clean vector-only iconography across the entire application interface.
