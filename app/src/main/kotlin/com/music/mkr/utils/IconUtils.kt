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
    MINIMAL_LOGOS("Minimal Logos"),
    BOYS_THEME("Boys Theme"),
    GIRLS_THEME("Girls Theme")
}

enum class AppIcon(
    val key: String,
    val title: String,
    val subtitle: String,
    val group: AppIconGroup,
    val aliasName: String,
    @DrawableRes val previewDrawableRes: Int
) {
    LINE_NOTE(
        key = "line_note",
        title = "Line Note",
        subtitle = "Continuous ribbon M with musical note (Default)",
        group = AppIconGroup.MINIMAL_LOGOS,
        aliasName = "com.music.mkr.icon.LineNote",
        previewDrawableRes = R.drawable.ic_icon_line_note
    ),
    M_NOTE(
        key = "m_note",
        title = "M Note",
        subtitle = "Geometric architectural M with suspended music note",
        group = AppIconGroup.MINIMAL_LOGOS,
        aliasName = "com.music.mkr.icon.MNote",
        previewDrawableRes = R.drawable.ic_icon_m_note
    ),
    MUSIC_MONOGRAM(
        key = "music_monogram",
        title = "Music Monogram",
        subtitle = "Minimalist musical monogram with sleek curves",
        group = AppIconGroup.MINIMAL_LOGOS,
        aliasName = "com.music.mkr.icon.MusicMonogram",
        previewDrawableRes = R.drawable.ic_icon_music_monogram
    ),
    NEON_BOY(
        key = "neon_boy",
        title = "Gojo Inspired",
        subtitle = "Dark anime boy icon inspired by Gojo with glowing eyes",
        group = AppIconGroup.BOYS_THEME,
        aliasName = "com.music.mkr.icon.NeonBoy",
        previewDrawableRes = R.drawable.ic_icon_neon_boy
    ),
    MIDNIGHT_BOY(
        key = "midnight_boy",
        title = "Thorfinn Inspired",
        subtitle = "Determined anime boy icon inspired by Thorfinn in headphones",
        group = AppIconGroup.BOYS_THEME,
        aliasName = "com.music.mkr.icon.MidnightBoy",
        previewDrawableRes = R.drawable.ic_icon_midnight_boy
    ),
    RETRO_BOY(
        key = "retro_boy",
        title = "Anime Boy (Original)",
        subtitle = "Signature anime boy in headphones with cool expression",
        group = AppIconGroup.BOYS_THEME,
        aliasName = "com.music.mkr.icon.RetroBoy",
        previewDrawableRes = R.drawable.ic_icon_retro_boy
    ),
    NEON_GIRL(
        key = "neon_girl",
        title = "Suzuka Inspired",
        subtitle = "Anime girl icon inspired by Suzuka with headphones",
        group = AppIconGroup.GIRLS_THEME,
        aliasName = "com.music.mkr.icon.NeonGirl",
        previewDrawableRes = R.drawable.ic_icon_neon_girl
    ),
    DREAMWAVE_GIRL(
        key = "dreamwave_girl",
        title = "Anime Girl (Red)",
        subtitle = "Crimson aesthetic anime girl immersed in rhythm",
        group = AppIconGroup.GIRLS_THEME,
        aliasName = "com.music.mkr.icon.DreamwaveGirl",
        previewDrawableRes = R.drawable.ic_icon_dreamwave_girl
    ),
    RETRO_GIRL(
        key = "retro_girl",
        title = "Anime Girl (Blue)",
        subtitle = "Sapphire aesthetic anime girl lost in melody",
        group = AppIconGroup.GIRLS_THEME,
        aliasName = "com.music.mkr.icon.RetroGirl",
        previewDrawableRes = R.drawable.ic_icon_retro_girl
    );

    companion object {
        val DEFAULT = LINE_NOTE

        fun fromKey(key: String?): AppIcon {
            return when (key) {
                "minimal_note", "pulse_vinyl", "spectrum_rhythm" -> LINE_NOTE
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
        setIcon(context, if (enabled) AppIcon.DEFAULT else AppIcon.M_NOTE)
    }
}
