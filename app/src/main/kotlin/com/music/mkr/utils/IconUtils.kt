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
    @DrawableRes val previewDrawableRes: Int,
    val isHumanMade: Boolean = false
) {
    // Human Made Icons (at the top)
    DOT_MATRIX(
        key = "dot_matrix",
        title = "Cyber Dots",
        aliasName = "com.music.mkr.icon.DotMatrix",
        previewDrawableRes = R.drawable.ic_icon_dot_matrix,
        isHumanMade = true
    ),
    SUNSET_PASTEL(
        key = "sunset_pastel",
        title = "Sunset Pastel",
        aliasName = "com.music.mkr.icon.SunsetPastel",
        previewDrawableRes = R.drawable.ic_icon_sunset_pastel,
        isHumanMade = true
    ),
    RETRO_SYNTHWAVE(
        key = "retro_synthwave",
        title = "Retro Synthwave",
        aliasName = "com.music.mkr.icon.RetroSynthwave",
        previewDrawableRes = R.drawable.ic_icon_retro_synthwave,
        isHumanMade = true
    ),
    AURORA_GLOW(
        key = "aurora_glow",
        title = "Aurora Glow",
        aliasName = "com.music.mkr.icon.AuroraGlow",
        previewDrawableRes = R.drawable.ic_icon_aurora_glow,
        isHumanMade = true
    ),
    MATTE_OBSIDIAN(
        key = "matte_obsidian",
        title = "Matte Obsidian",
        aliasName = "com.music.mkr.icon.MatteObsidian",
        previewDrawableRes = R.drawable.ic_icon_matte_obsidian,
        isHumanMade = true
    ),

    // AI Made Icons
    OFFICIAL(
        key = "mkr_official",
        title = "MKR Official",
        aliasName = "com.music.mkr.icon.LineNote",
        previewDrawableRes = R.drawable.ic_icon_mkr_official,
        isHumanMade = false
    ),
    NEON_BEAT(
        key = "neon_beat",
        title = "Neon Beat",
        aliasName = "com.music.mkr.icon.NeonBeat",
        previewDrawableRes = R.drawable.ic_icon_neon_beat,
        isHumanMade = false
    ),
    MIDNIGHT_NOTE(
        key = "midnight_note",
        title = "Midnight Note",
        aliasName = "com.music.mkr.icon.MidnightNote",
        previewDrawableRes = R.drawable.ic_icon_midnight_note,
        isHumanMade = false
    ),
    DREAMWAVE(
        key = "dreamwave",
        title = "Dreamwave",
        aliasName = "com.music.mkr.icon.Dreamwave",
        previewDrawableRes = R.drawable.ic_icon_dreamwave,
        isHumanMade = false
    ),
    MINIMAL_NOTE(
        key = "minimal_note",
        title = "Minimal Note",
        aliasName = "com.music.mkr.icon.MinimalNote",
        previewDrawableRes = R.drawable.ic_icon_minimal_note,
        isHumanMade = false
    ),
    PULSE_VINYL(
        key = "pulse_vinyl",
        title = "Pulse Vinyl",
        aliasName = "com.music.mkr.icon.PulseVinyl",
        previewDrawableRes = R.drawable.ic_icon_pulse_vinyl,
        isHumanMade = false
    ),
    RETRO_MELODY(
        key = "retro_melody",
        title = "Retro Melody",
        aliasName = "com.music.mkr.icon.RetroMelody",
        previewDrawableRes = R.drawable.ic_icon_retro_melody,
        isHumanMade = false
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
                "official", "mkr_official", "line_note" -> OFFICIAL
                "neon_beat", "neon_boy" -> NEON_BEAT
                "midnight_note", "midnight_boy" -> MIDNIGHT_NOTE
                "dreamwave", "dreamwave_girl" -> DREAMWAVE
                "minimal_note", "m_note" -> MINIMAL_NOTE
                "pulse_vinyl", "neon_girl" -> PULSE_VINYL
                "retro_melody", "retro_boy", "retro_girl" -> RETRO_MELODY
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
