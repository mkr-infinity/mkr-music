<p align="center">
  <img src="assets/banner.png" alt="MKR Music Banner" width="100%" />
</p>

<p align="center">
  <a href="https://github.com/mkr-infinity/mkr-music/releases"><img src="https://img.shields.io/badge/Version-v6.0.8%20Stable-00E676?style=for-the-badge&logo=android&logoColor=white" alt="Version v6.0.8 Stable"/></a>
  <a href="https://github.com/mkr-infinity/mkr-music/actions"><img src="https://img.shields.io/badge/Build-Passing-3498DB?style=for-the-badge&logo=githubactions&logoColor=white" alt="Build Status"/></a>
  <a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-8E44AD?style=for-the-badge" alt="License: GPL v3"/></a>
  <a href="https://github.com/mkr-infinity"><img src="https://img.shields.io/badge/Developer-%40mkr--infinity-E74C3C?style=for-the-badge&logo=github&logoColor=white" alt="Developer @mkr-infinity"/></a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-F39C12?style=for-the-badge" alt="Android 8.0+"/>
</p>

<div align="center">

# MKR Music

### *Elevate Your Soundscape — Elegant, Lightweight & Boundless*

**MKR Music** is a premier open-source, ad-free music streaming and offline player crafted for Android.  
Engineered with **Jetpack Compose**, **Material 3 Expressive**, and **AndroidX Media3 ExoPlayer**, MKR Music merges studio-grade acoustics with modern visual aesthetics.

Featuring seamless **dual-engine streaming** (JioSaavn & YouTube Music), a full **Android adaptive icon system**, smart **offline caching**, real-time **synchronized lyrics**, and tailored **acoustic enhancements**.

[**Download Latest APK**](https://github.com/mkr-infinity/mkr-music/releases) • [**Report an Issue**](https://github.com/mkr-infinity/mkr-music/issues) • [**Developer Website**](https://mkr-infinity.github.io)

---

</div>

<div align="center">

### 🌟 Key Highlights

| Dual Streaming Engines | Adaptive Launcher Icons | Pro-Grade Acoustics |
| :---: | :---: | :---: |
| **JioSaavn & YouTube Music** with automatic fallback for uninterrupted listening | **7 Handcrafted Themes** adapting naturally to any launcher mask shape | **Loudness Enhancer & MKR EQ** tuned for acoustic clarity and deep bass |

| Smart Offline Caching | Synchronized Lyrics | Modern Expressive UI |
| :---: | :---: | :---: |
| Automatically caches tracks so your history is always available offline | Word-by-word and line-by-line synchronized lyrics with Apple-style glow | Floating navigation capsule, dynamic color palettes, and AMOLED pure black |

</div>

---

## 🚀 Key Features

### 🎧 Dual-Engine High-Fidelity Playback
- **JioSaavn & YouTube Music Integration**: Stream from either source or let MKR Music intelligently fallback if a track isn't reachable on one platform.
- **Media3 ExoPlayer Backbone**: Ultra-fast buffering, gapless transitions, audio crossfade, and background streaming.
- **Tuned Acoustic Equalizer**: Custom `LoudnessEnhancer` calibrated to eliminate muddy distortion while preserving vocal punch, alongside parametric EQ presets (*MKR Signature, 3D Stage, Bass Boost, Dolby Atmos, Dirac Audio*).

### 🎨 Handcrafted Adaptive Icon Suite
MKR Music implements full Android 8.0+ (`API 26`) adaptive icon specifications across all selectable themes:
- **MKR Official**: The signature gold emblem with musical notes & headphones (*Default*).
- **Neon Beat**: Vibrant emerald character styling.
- **Midnight Note**: Warm bronze acoustic aesthetic.
- **Dreamwave**: Crimson character art.
- **Minimal Note**: Sleek monochrome silver note.
- **Pulse Vinyl**: Cool mint vinyl aesthetic.
- **Retro Melody**: Electric sapphire character art.
- *Adapts naturally to device masks (Circle, Squircle, Rounded Square, Teardrop) with zero forced cropping.*

### 💾 Smart Offline Caching & Data Protection
- **Automatic Audio Caching**: Streamed tracks are cached locally, allowing you to replay songs anytime without consuming data.
- **System Update Safety**: Built-in "Before updating" warning screen with direct links to Backup & Restore, safeguarding your playlists and settings before in-place APK updates.
- **External Exporting**: Easily export your library to `Downloads/mkrmusic` for easy migration across devices.

### 📜 Real-Time Synced Lyrics
- Word-by-word and line-by-line synchronized scrolling lyrics powered by **LRCLIB**, **KuGou**, and **BetterLyrics**.
- Dynamic glowing effects, background blur, and customizable typography.

### 🌐 Discord Rich Presence & Connected Features
- Real-time **Discord RPC** showing your currently playing track, artist, album art, and elapsed time with customizable activities (*Playing, Listening, Watching, Competing*).
- **Listen Together**: Stream simultaneously with friends via synced web sessions.
- **Weekly Listening Summary & Moods**: Discover your listening stats categorized into dynamic musical moods.

### 🧹 Battery & Performance Efficiency
- **Stop Music on Task Clear**: Swiping MKR Music away from your recent apps terminates background processes cleanly, conserving battery on older devices.
- **Ultra-Light Footprint**: Optimized image caching pipeline via Coil 3 and memory management for smooth performance on 2GB+ devices.

---

## 📱 System Requirements

<div align="center">

| Specification | Requirement |
| :--- | :--- |
| **Minimum OS** | Android 8.0 (API Level 26) |
| **Target OS** | Android 15 / 16 (API Level 35+) |
| **Architectures** | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| **Memory** | Optimized for devices with 2GB RAM or higher |
| **Google Services** | Not required — operates completely independently |

</div>

---

## 📦 Release APK Variants

MKR Music builds and distributes three targeted APK variants via GitHub Actions:

| Variant | Target Architecture | Filename | Description |
| :--- | :--- | :--- | :--- |
| **ARMv7 (32-bit)** | `armeabi-v7a` | `mkr-music-armv7.apk` | Optimized for legacy 32-bit Android devices |
| **ARMv8 (64-bit)** | `arm64-v8a` | `mkr-music-arm64.apk` | Peak performance on modern 64-bit Android smartphones |
| **Universal** | All Architectures | `mkr-music-universal.apk` | All-in-one package supporting `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` |

---

## 🛠️ Architecture & Tech Stack

```
MKR Music
 ├── UI Layer        ── Jetpack Compose • Material 3 Expressive • Navigation Compose
 ├── Audio Core      ── AndroidX Media3 ExoPlayer • MediaSession • LoudnessEnhancer
 ├── Data Engine     ── Room Database • DataStore Preferences • SQLCipher
 ├── Network Layer   ── Ktor Client • OkHttp • Kotlinx Serialization
 ├── Image Engine    ── Coil 3 with low-RAM downsampling
 └── Background      ── Android WorkManager • AutoBackupWorker
```

---

## 🚀 Building & CI/CD via GitHub Actions

To ensure reproducible builds, MKR Music is configured to build exclusively via GitHub Actions:

1. Navigate to the **[Actions tab](https://github.com/mkr-infinity/mkr-music/actions)**.
2. Select the **Production Release Build** workflow.
3. Click **Run workflow** on the `main` branch.
4. Download the compiled release APK artifacts directly from the workflow run or GitHub Releases.

---

## 👨‍💻 Developer & Connect

<div align="center">

Crafted with dedication by **Mohammad Kaif Raja**:

[![Website](https://img.shields.io/badge/Website-mkr--infinity.github.io-4CAF50?style=for-the-badge&logo=googlechrome&logoColor=white)](https://mkr-infinity.github.io)
[![GitHub](https://img.shields.io/badge/GitHub-%40mkr--infinity-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mkr-infinity)
[![Repository](https://img.shields.io/badge/Repository-mkr--music-blue?style=for-the-badge&logo=git&logoColor=white)](https://github.com/mkr-infinity/mkr-music)
[![Instagram](https://img.shields.io/badge/Instagram-%40mkr__infinity-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/mkr_infinity)
[![Telegram](https://img.shields.io/badge/Telegram-%40mkr__infinity-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/mkr_infinity)
[![Buy Me A Coffee](https://img.shields.io/badge/Support-Buy%20Me%20A%20Coffee-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=black)](https://buymeacoffee.com/mkr_infinity)

</div>

---

## 🙏 Credits & Inspiration

MKR Music was inspired by and builds upon the ideas and codebases of these open-source projects:

- **ViVi Music**: Created with passion by **vivizzz007** and community contributors. Sincere thanks for the wonderful inspiration and foundations!
- **InnerTune**: Developed by **Zion Huang** and contributors.
- **ViMusic**: Developed by **vfsfitvnm** and contributors.

---

<div align="center">

## ⚖️ License

MKR Music is open-source software licensed under the [GNU General Public License v3.0 (GPL-3.0)](LICENSE).

*Copyright © 2026 Mohammad Kaif Raja ([@mkr-infinity](https://github.com/mkr-infinity))*

</div>
