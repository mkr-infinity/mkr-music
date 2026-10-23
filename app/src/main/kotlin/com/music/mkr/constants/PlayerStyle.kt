/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.constants

import androidx.annotation.StringRes
import com.music.mkr.R

/**
 * Supported playback architectures in MKR Music:
 * Eight native expressive visual architectures.
 */
enum class PlayerStyle(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    APPLE(
        titleRes = R.string.apple_player,
        descriptionRes = R.string.apple_player_desc
    ),
    CLASSIC(
        titleRes = R.string.player_style_classic,
        descriptionRes = R.string.player_style_classic_desc
    ),
    MODERN(
        titleRes = R.string.player_style_modern,
        descriptionRes = R.string.player_style_modern_desc
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
    ),
    IMMERSIVE_EXTENDED(
        titleRes = R.string.player_style_immersive_extended,
        descriptionRes = R.string.player_style_immersive_extended_desc
    ),
    MATERIAL_EXTENDED(
        titleRes = R.string.player_style_material_extended,
        descriptionRes = R.string.player_style_material_extended_desc
    ),
    EDITORIAL(
        titleRes = R.string.player_style_editorial,
        descriptionRes = R.string.player_style_editorial_desc
    );

    companion object {
        val DEFAULT = IMMERSIVE_EXTENDED
        val selectableStyles: List<PlayerStyle> = entries.filter { it != APPLE }

        fun fromString(name: String?): PlayerStyle {
            val style = entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: IMMERSIVE_EXTENDED
            return if (style == APPLE) IMMERSIVE_EXTENDED else style
        }
    }
}
