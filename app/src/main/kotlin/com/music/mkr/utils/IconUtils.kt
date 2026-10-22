/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import com.music.mkr.R
import timber.log.Timber

enum class AppIcon(
    val key: String,
    val title: String,
    val aliasName: String,
    @DrawableRes val previewDrawableRes: Int
) {
    DOT_MATRIX(
        key = "dot_matrix",
        title = "Cyber Dots",
        aliasName = "com.music.mkr.icon.DotMatrix",
        previewDrawableRes = R.drawable.ic_icon_dot_matrix
    ),
    SUNSET_PASTEL(
        key = "sunset_pastel",
        title = "Sunset Pastel",
        aliasName = "com.music.mkr.icon.SunsetPastel",
        previewDrawableRes = R.drawable.ic_icon_sunset_pastel
    ),
    RETRO_SYNTHWAVE(
        key = "retro_synthwave",
        title = "Retro Synthwave",
        aliasName = "com.music.mkr.icon.RetroSynthwave",
        previewDrawableRes = R.drawable.ic_icon_retro_synthwave
    ),
    AURORA_GLOW(
        key = "aurora_glow",
        title = "Aurora Glow",
        aliasName = "com.music.mkr.icon.AuroraGlow",
        previewDrawableRes = R.drawable.ic_icon_aurora_glow
    ),
    MATTE_OBSIDIAN(
        key = "matte_obsidian",
        title = "Matte Obsidian",
        aliasName = "com.music.mkr.icon.MatteObsidian",
        previewDrawableRes = R.drawable.ic_icon_matte_obsidian
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
                else -> entries.firstOrNull { it.key == key } ?: DEFAULT
            }
        }
    }
}

object IconUtils {
    fun setIcon(context: Context, targetIcon: AppIcon) {
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
                    // Ignore non-matching class name variant
                }
            }
        }
    }

    /**
     * Legacy toggle compatibility
     */
    fun setIcon(context: Context, enabled: Boolean) {
        setIcon(context, AppIcon.DEFAULT)
    }
}
