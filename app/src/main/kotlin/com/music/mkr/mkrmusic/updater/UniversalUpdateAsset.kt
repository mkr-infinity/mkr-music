package com.music.mkr.mkrmusic.updater

import com.music.mkr.github.network.GitHubAssetDto
import java.net.URI

internal const val UNIVERSAL_UPDATE_APK = "mkr-music-universal.apk"
internal const val LATEST_UNIVERSAL_APK_URL =
    "https://github.com/mkr-infinity/mkr-music/releases/latest/download/" + UNIVERSAL_UPDATE_APK

internal fun selectUniversalUpdateAsset(assets: List<GitHubAssetDto>): GitHubAssetDto? =
    assets.firstOrNull { asset ->
        if (asset.name != UNIVERSAL_UPDATE_APK) return@firstOrNull false
        val uri = runCatching { URI(asset.browserDownloadUrl) }.getOrNull()
        uri?.scheme.equals("https", ignoreCase = true) && !uri?.host.isNullOrBlank()
    }
