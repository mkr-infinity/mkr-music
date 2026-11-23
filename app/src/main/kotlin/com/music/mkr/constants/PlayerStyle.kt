/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.constants

import androidx.annotation.StringRes
import com.music.mkr.R

enum class PlayerStyle(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    APPLE(
        titleRes = R.string.apple_player,
        descriptionRes = R.string.apple_player_desc
    ),
    MINIMAL(
        titleRes = R.string.player_style_minimal,
        descriptionRes = R.string.player_style_minimal_desc
    ),
    CINEMATIC(
        titleRes = R.string.player_style_cinematic,
        descriptionRes = R.string.player_style_cinematic_desc
    ),
    IMMERSIVE(
        titleRes = R.string.player_style_immersive,
        descriptionRes = R.string.player_style_immersive_desc
    );

    companion object {
        val DEFAULT = CINEMATIC
        val selectableStyles: List<PlayerStyle> = entries.filter { it != APPLE }

        fun fromString(name: String?): PlayerStyle {
            if (name.equals("IMMERSIVE_EXTENDED", ignoreCase = true)) return CINEMATIC
            val style = entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DEFAULT
            return if (style == APPLE) DEFAULT else style
        }
    }
}
