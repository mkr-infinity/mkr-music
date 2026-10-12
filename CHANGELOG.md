# Changelog

<p align="center">
  <img src="https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

All notable changes to **MKR Music** are documented in this file.

## [v1.1.0] - 2026-10-12

### ⚡ Playback & Streaming Engine
- **Instant Playback Start**: Direct low-latency streaming pipeline bypassing pre-caching buffer delays for instantaneous audio playback.
- **Configurable Delayed Caching**: New Storage & Cache setting allowing background caching only after continuous listening (adjustable slider from 5s to 90s, default 10s).
- **Zero Playback Interference**: Background caching runs decoupled via low-priority `CacheWriter` on `Dispatchers.IO` without affecting active playback threads.

### 💖 Support & Contributions
- **Chainsaw Spikes Action Button**: Custom-designed animated moving chainsaw teeth border on the Donate & Support button with smooth loop animation and zero text clipping.
- **Direct UPI Copy Card**: One-tap copy for UPI ID and Verified Banking Name.
- **Tactile Copy Feedback**: Spring-animated press feedback with instant "Copied ✓" status indication.
- **Supported Apps & Remark**: App recommendation badges for PhonePe, Google Pay, Paytm, BHIM, and Banking Apps with one-touch note copy (`"Support MKR Music"`).
- **Gratitude Appreciation System**: 6 rotating gratitude toast messages displaying randomly upon copy with automatic 4-second dismiss and instant reset on re-click.
- **Alternative Support**: Direct link to Buy Me a Coffee (`https://buymeacoffee.com/mkr_infinity`).

### ⚙️ Settings & System Updates
- **Granular Keyword Search**: Settings search engine now indexes sub-options and granular keywords (e.g., searching "hide" displays all hide toggles) with automatic deduplication.
- **Simplified Update Experience**: Removed in-app background downloaders and flavor switchers in favor of direct GitHub Releases link with clear backup and installation instructions.
- **Streamlined Release Notes**: Project poster banner preview with live Markdown rendering and direct Commits history viewer.
- **Clean Settings Toolbar**: Removed redundant top-right About capsule for a cleaner, distraction-free header.

### 🎨 Onboarding & Interface
- **Balanced Onboarding Hero**: Centered MKR Music logo with tagline "Welcome to the world of music" and elegant developer badge.
- **Responsive Layout**: Removed spinning animation and version tag for a clean, consistent presentation across all screen aspect ratios.
- **About Developer Links**: Refined borders on developer social badges (GitHub, Instagram, Telegram, Website).

---

## [v1.0 Stable] - 2026-10-10

### 🎨 Visual Design & Themes
- Brand new adaptive icon and expressive Material 3 design system.
- Modernized theme palettes with amber, warm orange, and lime accents alongside pure black AMOLED support.

### 🖼️ App Icons Gallery
- Added 9 selectable app icons categorized into 3 distinct groups in Appearance settings:
  - **Anime Boys**: Neon Beat Boy, Midnight Listener, Retro Rhythm Boy.
  - **Anime Girls**: Neon Harmony Girl, Dreamwave Girl, Retro Melody Girl.
  - **Music Logos**: Minimal Note (Default), Spectrum Rhythm, Pulse Vinyl.
- Real launcher icon switching using Android `activity-alias` components with snackbar confirmation and restart prompt.

### 🌟 Modern About Screen & Live GitHub Integration
- Premium developer profile hero featuring developer profile, avatar, role, and MKR Music badge.
- Live public GitHub API integration with `https://github.com/mkr-infinity/mkr-music` displaying real stars, forks, open issues, and repository activity.
- Recent commits view with SHA, commit message, author, date, and external GitHub links.
- Interactive release changelog viewer.
- Verified developer cards for GitHub, website, Instagram, Telegram, and Buy Me a Coffee.

### 🎵 Navigation & Playback UI
- Repositioned the listening-history button to the top-right before other action controls in the main `TopAppBar`.

### 💾 Cache & Storage Management
- Introduced configurable maximum song cache limits (50, 100 [default], 200, 500, 1000 songs, Custom) alongside byte limits in Storage Settings.
- Implemented smart LRU eviction for streaming cache that protects explicit offline downloads and local library files.

### ☁️ Cloud CI/CD
- Clean GitHub Actions workflow (`CI & Debug Build`) supporting cloud-hosted APK generation for Universal GMS and Universal FOSS flavors.
