package com.music.mkr.mkrmusic.updater

import com.music.mkr.github.network.GitHubAssetDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UniversalUpdateAssetTest {
    private fun asset(name: String, url: String = "https://example.com/update.apk") =
        GitHubAssetDto(name = name, browserDownloadUrl = url)

    @Test
    fun prefersCanonicalUniversalOverEarlierAssets() {
        val canonical = asset("mkr-music-universal.apk")
        val assets = listOf(asset("mkr-music-arm64.apk"), asset("older-universal.apk"), canonical)

        assertEquals(canonical, selectUniversalUpdateAsset(assets))
    }

    @Test
    fun rejectsAlternateNamesWhenCanonicalHasNoUsableLink() {
        val fallback = asset("older-UNIVERSAL.APK")
        val assets = listOf(asset("mkr-music-universal.apk", "https://"), fallback)

        assertNull(selectUniversalUpdateAsset(assets + asset("MKR-MUSIC-UNIVERSAL.APK")))
    }

    @Test
    fun skipsSplitApksAndMetadataWithoutForcingADownload() {
        assertNull(
            selectUniversalUpdateAsset(
                listOf(asset("version.json"), asset("mkr-music-arm64.apk"), asset("mkr-music-universal.apk", "")),
            ),
        )
        assertNull(selectUniversalUpdateAsset(emptyList()))
    }
}
