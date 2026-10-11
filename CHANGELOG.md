# Changelog

All notable changes to **MKR Music** are documented in this file.

## [v1.0 Stable] - 2026-10-10

### Visual Design & Themes
- Brand new adaptive icon and expressive Material 3 design system.
- Modernized theme palettes with amber, warm orange, and lime accents alongside pure black AMOLED support.

### App Icons Gallery
- Added 9 selectable app icons categorized into 3 distinct groups in Appearance settings:
  - **Anime Boys**: Neon Beat Boy, Midnight Listener, Retro Rhythm Boy.
  - **Anime Girls**: Neon Harmony Girl, Dreamwave Girl, Retro Melody Girl.
  - **Music Logos**: Minimal Note (Default), Spectrum Rhythm, Pulse Vinyl.
- Real launcher icon switching using Android `activity-alias` components with snackbar confirmation and restart prompt.

### Modern About Screen & Live GitHub Integration
- Premium developer profile hero featuring Mohammad Kaif Raja's profile, avatar, role, and MKR Music badge.
- Live public GitHub API integration with `https://github.com/mkr-infinity/mkr-music` displaying real stars, forks, open issues, and repository activity.
- Recent commits view with SHA, commit message, author, date, and external GitHub links.
- Interactive release changelog viewer.
- Verified developer cards for GitHub, website, Instagram, Telegram, and Buy Me a Coffee.

### Navigation & Playback UI
- Repositioned the listening-history button to the top-right before other action controls in the main `TopAppBar`.

### Cache & Storage Management
- Introduced configurable maximum song cache limits (50, 100 [default], 200, 500, 1000 songs, Custom) alongside byte limits in Storage Settings.
- Implemented smart LRU eviction for streaming cache that protects explicit offline downloads and local library files.

### Cloud CI/CD
- Clean GitHub Actions workflow (`CI & Debug Build`) supporting cloud-hosted APK generation for Universal GMS and Universal FOSS flavors.
