# MKR Music Dev Guide

This guide outlines the process of setting up a local development environment for **MKR Music**.

## Prerequisites
- Java JDK 21
- Android Studio (or modern IDE with Kotlin support)
- Go 1.20+
- protoc
- protoc-gen-go
- Android NDK (r27+)

## Basic Setup

```bash
git clone https://github.com/mkr-infinity/mkr-music
cd mkr-music
git submodule update --init --recursive
cd app
bash generate_proto.sh
cd ..
bash app/src/main/cpp/vibrafp/third_party/build-fftw-android.sh --ndk ~/Android/Sdk/ndk/27.0.12077973 --out app/src/main/cpp/vibrafp/third_party/fftw-android
[ ! -f "app/persistent-debug.keystore" ] && keytool -genkeypair -v -keystore app/persistent-debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" || echo "Keystore already exists."
./gradlew :app:assembleUniversalGmsDebug
ls app/build/outputs/apk/universalGms/debug/app-universal-gms-debug.apk
```

## Versioning & Architecture Notes

- **Canonical Version**: Managed via `version.properties` (`APP_VERSION=1.0.0`). Do not hardcode version numbers in build scripts.
- **Listen Together Server**: Configured in `ListenTogetherServers.kt` pointing to `wss://listen-together-mkr-music.onrender.com`.
- **UI Design System**: Charcoal-and-amber theme identity with zero UI emojis (use vector drawables exclusively).
