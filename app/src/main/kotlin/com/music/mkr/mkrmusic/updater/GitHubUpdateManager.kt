/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.mkrmusic.updater

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.music.mkr.BuildConfig
import com.music.mkr.github.GitHubConfig
import com.music.mkr.github.network.GitHubReleaseDto
import com.music.mkr.github.network.GitHubService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gitHubService: GitHubService
) {
    companion object {
        private const val TAG = "GitHubUpdateManager"
        private const val CACHE_EXPIRATION_MS = 60 * 60 * 1000L // 1 hour
        private val SEMVER_REGEX = Pattern.compile("^v?([0-9]+)\\.([0-9]+)\\.([0-9]+)$")
    }

    private val _updateState = MutableStateFlow<ReleasePreviewState>(ReleasePreviewState.Idle)
    val updateState: StateFlow<ReleasePreviewState> = _updateState.asStateFlow()

    private var lastCheckedEpoch: Long = 0L

    /**
     * Checks for the latest release from the official repository:
     * https://api.github.com/repos/mkr-infinity/mkr-music/releases/latest
     *
     * An update is reported as available ONLY when the remote release's verified
     * versionCode is strictly greater than the installed app's versionCode.
     */
    fun checkForUpdates(scope: CoroutineScope, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastCheckedEpoch < CACHE_EXPIRATION_MS && _updateState.value !is ReleasePreviewState.Idle && _updateState.value !is ReleasePreviewState.Error) {
            Timber.tag(TAG).d("Returning cached update state")
            return
        }

        _updateState.value = ReleasePreviewState.Checking

        scope.launch(Dispatchers.IO) {
            try {
                val latestDto = gitHubService.getLatestRelease(
                    owner = GitHubConfig.REPO_OWNER,
                    repo = GitHubConfig.REPO_NAME
                )

                if (latestDto == null) {
                    _updateState.value = ReleasePreviewState.Error("Unable to fetch latest release from GitHub.")
                    return@launch
                }

                val (installedVersionName, installedVersionCode) = getInstalledVersionInfo(context)
                val releaseMetadata = parseReleaseMetadata(latestDto)

                lastCheckedEpoch = System.currentTimeMillis()

                val remoteCode = releaseMetadata.remoteVersionCode
                if (remoteCode != null && remoteCode > installedVersionCode) {
                    _updateState.value = ReleasePreviewState.UpdateAvailable(
                        release = releaseMetadata,
                        installedVersionName = installedVersionName,
                        installedVersionCode = installedVersionCode
                    )
                } else {
                    _updateState.value = ReleasePreviewState.UpToDate(
                        release = releaseMetadata,
                        installedVersionName = installedVersionName,
                        installedVersionCode = installedVersionCode,
                        isVersionCodeUnverified = remoteCode == null
                    )
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error checking for GitHub updates")
                _updateState.value = ReleasePreviewState.Error(
                    e.localizedMessage ?: "Failed to connect to GitHub releases."
                )
            }
        }
    }

    private suspend fun parseReleaseMetadata(dto: GitHubReleaseDto): ReleaseMetadata {
        val tagName = dto.tagName
        val name = dto.name?.ifBlank { null } ?: tagName
        val body = dto.body.orEmpty()
        val publishedAt = formatIsoDate(dto.publishedAt)
        val htmlUrl = dto.htmlUrl ?: "https://github.com/${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}/releases/latest"

        // Step 1: Check for explicit version.json asset produced by the release workflow
        var remoteVersionCode: Long? = null
        val versionJsonAsset = dto.assets.firstOrNull { it.name.equals("version.json", ignoreCase = true) }
        if (versionJsonAsset != null && versionJsonAsset.browserDownloadUrl.isNotBlank()) {
            try {
                withContext(Dispatchers.IO) {
                    val rawJson = URL(versionJsonAsset.browserDownloadUrl).openStream().bufferedReader().use { it.readText() }
                    val json = JSONObject(rawJson)
                    val pkg = when {
                        json.has("packageName") -> json.optString("packageName")
                        json.has("package_id") -> json.optString("package_id")
                        else -> null
                    }
                    if (pkg == null || pkg == context.packageName || pkg == "com.kaif.music") {
                        val code = when {
                            json.has("versionCode") -> json.optLong("versionCode", -1L)
                            json.has("version_code") -> json.optLong("version_code", -1L)
                            else -> -1L
                        }
                        if (code > 0) {
                            remoteVersionCode = code
                        }
                    } else {
                        Timber.tag(TAG).w("Rejecting version.json: Package mismatch ($pkg vs ${context.packageName})")
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to parse remote version.json asset")
            }
        }

        // Step 2: Fallback to strict semantic version formula: MAJOR * 10000 + MINOR * 100 + PATCH
        if (remoteVersionCode == null) {
            val matcher = SEMVER_REGEX.matcher(tagName.trim())
            if (matcher.matches()) {
                val major = matcher.group(1)?.toLongOrNull()
                val minor = matcher.group(2)?.toLongOrNull()
                val patch = matcher.group(3)?.toLongOrNull()
                if (major != null && minor != null && patch != null && minor in 0..99 && patch in 0..99) {
                    remoteVersionCode = major * 10000L + minor * 100L + patch
                }
            }
        }

        // Step 3: Select compatible APK asset (prefer universal)
        val apkAsset = dto.assets.firstOrNull { it.name.equals("mkr-music-universal.apk", ignoreCase = true) }
            ?: dto.assets.firstOrNull { it.name.endsWith("-universal.apk", ignoreCase = true) }
            ?: dto.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }

        return ReleaseMetadata(
            name = name,
            tagName = tagName,
            description = body,
            publishedAt = publishedAt,
            htmlUrl = htmlUrl,
            remoteVersionCode = remoteVersionCode,
            apkName = apkAsset?.name,
            apkDownloadUrl = apkAsset?.browserDownloadUrl,
            apkSizeBytes = apkAsset?.size ?: 0L,
            isPrerelease = dto.prerelease
        )
    }

    private fun getInstalledVersionInfo(context: Context): Pair<String, Long> {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val code = PackageInfoCompat.getLongVersionCode(pInfo)
            val name = pInfo.versionName ?: BuildConfig.VERSION_NAME
            name to code
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME to 10000L
        }
    }

    private fun formatIsoDate(isoDate: String?): String? {
        if (isoDate.isNullOrBlank()) return null
        return try {
            val githubFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")
            val dateTime = LocalDateTime.parse(isoDate, githubFormatter)
            dateTime.format(displayFormatter)
        } catch (_: Exception) {
            isoDate
        }
    }
}

data class ReleaseMetadata(
    val name: String,
    val tagName: String,
    val description: String,
    val publishedAt: String?,
    val htmlUrl: String,
    val remoteVersionCode: Long?,
    val apkName: String?,
    val apkDownloadUrl: String?,
    val apkSizeBytes: Long,
    val isPrerelease: Boolean
)

sealed interface ReleasePreviewState {
    data object Idle : ReleasePreviewState
    data object Checking : ReleasePreviewState
    data class UpdateAvailable(
        val release: ReleaseMetadata,
        val installedVersionName: String,
        val installedVersionCode: Long
    ) : ReleasePreviewState
    data class UpToDate(
        val release: ReleaseMetadata,
        val installedVersionName: String,
        val installedVersionCode: Long,
        val isVersionCodeUnverified: Boolean = false
    ) : ReleasePreviewState
    data class Error(val message: String) : ReleasePreviewState
}
