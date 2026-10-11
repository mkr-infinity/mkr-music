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

enum class AppIconGroup(val label: String) {
    OFFICIAL("Official Theme")
}

enum class AppIcon(
    val key: String,
    val title: String,
    val subtitle: String,
    val group: AppIconGroup,
    val aliasName: String,
    @DrawableRes val previewDrawableRes: Int
) {
    OFFICIAL(
        key = "official",
        title = "MKR Music (Official)",
        subtitle = "Authentic emblem with signature colors & musical note",
        group = AppIconGroup.OFFICIAL,
        aliasName = "com.music.mkr.icon.LineNote",
        previewDrawableRes = R.drawable.icon
    );

    companion object {
        val DEFAULT = OFFICIAL

        fun fromKey(key: String?): AppIcon {
            return when (key) {
                "official", "line_note", "minimal_note", "pulse_vinyl", "spectrum_rhythm",
                "m_note", "music_monogram", "neon_boy", "midnight_boy", "retro_boy",
                "neon_girl", "dreamwave_girl", "retro_girl" -> OFFICIAL
                else -> entries.firstOrNull { it.key == key } ?: DEFAULT
            }
        }
    }
}

object IconUtils {
    fun setIcon(context: Context, targetIcon: AppIcon) {
        val pm = context.packageManager
        for (icon in AppIcon.entries) {
            val component = ComponentName(context, icon.aliasName)
            val desiredState = if (icon == targetIcon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                if (pm.getComponentEnabledSetting(component) != desiredState) {
                    pm.setComponentEnabledSetting(
                        component,
                        desiredState,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update component state for ${icon.aliasName}")
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
