/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.datastore.preferences.core.edit
import com.music.mkr.R
import com.music.mkr.constants.AppIconKey
import com.music.mkr.constants.PendingAppIconKey
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

enum class CreatorPlatform(
    val platformName: String,
    @DrawableRes val iconRes: Int
) {
    TELEGRAM("Telegram", R.drawable.telegram),
    GITHUB("GitHub", R.drawable.github)
}

data class CreatorAttribution(
    val username: String,
    val platform: CreatorPlatform,
    val profileUrl: String
)

/**
 * Centralized registry of all MKR Music launcher icons.
 *
 * To add a new icon:
 * 1. Add PNG resources in app/src/main/res/drawable/ic_icon_{name}.png (512x512 preview)
 *    and adaptive/legacy mipmaps in app/src/main/res/mipmap-* and app/src/main/res/mipmap-anydpi-v26/
 * 2. Add an <activity-alias android:name=".icon.{AliasName}" ... android:enabled="false" /> in AndroidManifest.xml
 * 3. Add an entry to this AppIcon enum with key, title, alias, preview res, and creator attribution.
 *
 * To remove an icon:
 * 1. Remove the entry from this enum.
 * 2. Remove the activity-alias from AndroidManifest.xml.
 * 3. Remove the unused resource files.
 */
enum class AppIcon(
    val key: String,
    val title: String,
    val aliasName: String,
    @DrawableRes val previewDrawableRes: Int,
    val attribution: CreatorAttribution,
    val isDefault: Boolean = false
) {
    DOT_MATRIX(
        key = "dot_matrix",
        title = "Cyber Dots",
        aliasName = "com.music.mkr.icon.DotMatrix",
        previewDrawableRes = R.drawable.ic_icon_dot_matrix,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        ),
        isDefault = true
    ),
    SUNSET_PASTEL(
        key = "sunset_pastel",
        title = "Sunset Pastel",
        aliasName = "com.music.mkr.icon.SunsetPastel",
        previewDrawableRes = R.drawable.ic_icon_sunset_pastel,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    RETRO_SYNTHWAVE(
        key = "retro_synthwave",
        title = "Retro Synthwave",
        aliasName = "com.music.mkr.icon.RetroSynthwave",
        previewDrawableRes = R.drawable.ic_icon_retro_synthwave,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    AURORA_GLOW(
        key = "aurora_glow",
        title = "Aurora Glow",
        aliasName = "com.music.mkr.icon.AuroraGlow",
        previewDrawableRes = R.drawable.ic_icon_aurora_glow,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    MATTE_OBSIDIAN(
        key = "matte_obsidian",
        title = "Matte Obsidian",
        aliasName = "com.music.mkr.icon.MatteObsidian",
        previewDrawableRes = R.drawable.ic_icon_matte_obsidian,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    NEON_CRACK(
        key = "neon_crack",
        title = "Neon Crack",
        aliasName = "com.music.mkr.icon.NeonCrack",
        previewDrawableRes = R.drawable.ic_icon_neon_crack,
        attribution = CreatorAttribution(
            username = "@ultimatefinale",
            platform = CreatorPlatform.TELEGRAM,
            profileUrl = "https://t.me/ultimatefinale"
        )
    ),
    VINYL_NATURE(
        key = "vinyl_nature",
        title = "Vinyl Nature",
        aliasName = "com.music.mkr.icon.VinylNature",
        previewDrawableRes = R.drawable.ic_icon_vinyl_nature,
        attribution = CreatorAttribution(
            username = "@ultimatefinale",
            platform = CreatorPlatform.TELEGRAM,
            profileUrl = "https://t.me/ultimatefinale"
        )
    ),
    WISTERIA_GLOW(
        key = "wisteria_glow",
        title = "Wisteria Glow",
        aliasName = "com.music.mkr.icon.WisteriaGlow",
        previewDrawableRes = R.drawable.ic_icon_wisteria_glow,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    SAKURA_DUSK(
        key = "sakura_dusk",
        title = "Sakura Dusk",
        aliasName = "com.music.mkr.icon.SakuraDusk",
        previewDrawableRes = R.drawable.ic_icon_sakura_dusk,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    ),
    FOREST_GLOW(
        key = "forest_glow",
        title = "Forest Glow",
        aliasName = "com.music.mkr.icon.ForestGlow",
        previewDrawableRes = R.drawable.ic_icon_forest_glow,
        attribution = CreatorAttribution(
            username = "@mkr-infinity",
            platform = CreatorPlatform.GITHUB,
            profileUrl = "https://github.com/mkr-infinity/"
        )
    );

    companion object {
        val DEFAULT = DOT_MATRIX

        fun fromKey(key: String?): AppIcon {
            return when (key) {
                "dot_matrix", "cyber_dots" -> DOT_MATRIX
                "sunset_pastel" -> SUNSET_PASTEL
                "retro_synthwave", "synthwave_sun" -> RETRO_SYNTHWAVE
                "aurora_glow" -> AURORA_GLOW
                "matte_obsidian" -> MATTE_OBSIDIAN
                "neon_crack" -> NEON_CRACK
                "vinyl_nature" -> VINYL_NATURE
                "wisteria_glow" -> WISTERIA_GLOW
                "sakura_dusk" -> SAKURA_DUSK
                "forest_glow" -> FOREST_GLOW
                else -> entries.firstOrNull { it.key == key } ?: DEFAULT
            }
        }
    }
}

object IconUtils {
    private val restartLock = AtomicBoolean(false)

    /**
     * Toggles launcher activity-aliases to enable targetIcon and disable all others.
     * Uses PackageManager.DONT_KILL_APP so application continues running smoothly.
     */
    fun applyIcon(context: Context, targetIcon: AppIcon): Boolean {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            for (icon in AppIcon.entries) {
                val desiredState = if (icon == targetIcon) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                val aliasShort = icon.aliasName.substringAfterLast(".")
                val targetClasses = listOf(
                    icon.aliasName,
                    "$pkg.icon.$aliasShort"
                ).distinct()

                for (className in targetClasses) {
                    val component = ComponentName(pkg, className)
                    try {
                        if (pm.getComponentEnabledSetting(component) != desiredState) {
                            pm.setComponentEnabledSetting(
                                component,
                                desiredState,
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Error toggling component state for $className")
                    }
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply icon: ${targetIcon.key}")
            false
        }
    }

    /**
     * Backward-compatibility alias for applyIcon
     */
    fun setIcon(context: Context, targetIcon: AppIcon) {
        applyIcon(context, targetIcon)
    }

    /**
     * Legacy toggle compatibility
     */
    fun setIcon(context: Context, enabled: Boolean) {
        applyIcon(context, AppIcon.DEFAULT)
    }

    /**
     * Gracefully restarts the application using Android's standard activity restart mechanism.
     * Never calls Runtime.getRuntime().exit(0) or force-kill APIs.
     */
    fun restartAppGracefully(activity: Activity) {
        if (!restartLock.compareAndSet(false, true)) return
        try {
            val pm = activity.packageManager
            val intent = pm.getLaunchIntentForPackage(activity.packageName)
            val componentName = intent?.component
            val restartIntent = Intent.makeRestartActivityTask(componentName)
            activity.startActivity(restartIntent)
            activity.finish()
        } catch (e: Exception) {
            Timber.e(e, "Failed to gracefully restart activity")
            restartLock.set(false)
        }
    }

    /**
     * Checks and applies any pending icon on application startup.
     */
    suspend fun applyPendingIconIfAny(context: Context) {
        try {
            val pendingKey = context.dataStore.data.first()[PendingAppIconKey]
            if (!pendingKey.isNullOrBlank()) {
                val pendingIcon = AppIcon.fromKey(pendingKey)
                val success = applyIcon(context, pendingIcon)
                if (success) {
                    context.dataStore.edit {
                        it[AppIconKey] = pendingIcon.key
                        it.remove(PendingAppIconKey)
                    }
                    Timber.i("Applied pending app icon on startup: ${pendingIcon.key}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply pending icon on startup")
        }
    }

    /**
     * Opens creator profile independently.
     * Supports deep links (e.g. Telegram client) with browser fallback.
     */
    fun openCreatorProfile(context: Context, attribution: CreatorAttribution) {
        try {
            when (attribution.platform) {
                CreatorPlatform.TELEGRAM -> {
                    val handle = attribution.username.removePrefix("@")
                    val tgUri = Uri.parse("tg://resolve?domain=$handle")
                    val tgIntent = Intent(Intent.ACTION_VIEW, tgUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(tgIntent)
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(attribution.profileUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
                CreatorPlatform.GITHUB -> {
                    val gitIntent = Intent(Intent.ACTION_VIEW, Uri.parse(attribution.profileUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(gitIntent)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open profile: ${attribution.profileUrl}")
        }
    }
}
