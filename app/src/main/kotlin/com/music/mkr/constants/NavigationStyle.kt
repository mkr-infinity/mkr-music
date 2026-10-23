/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.constants

import androidx.annotation.StringRes
import com.music.mkr.R

enum class NavigationStyle(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val isFloating: Boolean,
) {
    FLOATING_GLASS(
        titleRes = R.string.nav_style_floating_glass,
        descriptionRes = R.string.nav_style_floating_glass_desc,
        isFloating = true,
    ),
    AMBER_DOCK(
        titleRes = R.string.nav_style_amber_dock,
        descriptionRes = R.string.nav_style_amber_dock_desc,
        isFloating = true,
    ),
    EXPRESSIVE_PILL(
        titleRes = R.string.nav_style_expressive_pill,
        descriptionRes = R.string.nav_style_expressive_pill_desc,
        isFloating = true,
    ),
    CLEAN_MATERIAL(
        titleRes = R.string.nav_style_clean_material,
        descriptionRes = R.string.nav_style_clean_material_desc,
        isFloating = false,
    );

    companion object {
        val DEFAULT = FLOATING_GLASS
    }
}
