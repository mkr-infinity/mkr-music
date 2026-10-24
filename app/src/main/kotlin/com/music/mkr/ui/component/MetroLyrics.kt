/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.music.mkr.lyrics.LyricsEntry
import com.music.mkr.ui.screens.settings.LyricsPosition
import com.music.mkr.ui.theme.PlayerArtworkPalette

/**
 * Deprecated legacy MetroLyricsLine composable.
 *
 * All synchronized lyrics rendering has been consolidated into [MkrLyricsLine] and [LyricsV2].
 * This lightweight stub preserves signature compatibility while shedding hundreds of lines
 * of dead canvas drawing routines and nested lambda classes to optimize DEX size.
 */
@Deprecated(
    message = "MetroLyricsLine is deprecated. Use MkrLyricsLine in LyricsV2 instead.",
    level = DeprecationLevel.WARNING
)
@Composable
fun MetroLyricsLine(
    entry: LyricsEntry,
    nextEntryTime: Long?,
    effectivePlaybackPosition: Long,
    lyricsOffset: Long = 0L,
    isSynced: Boolean,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    showRomanized: Boolean,
    showTranslated: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionModeActive: Boolean = false,
    isAutoScrollActive: Boolean = false,
    expressiveAccent: Color = textColor,
    bgVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val fallbackPalette = PlayerArtworkPalette(
        primaryAccent = expressiveAccent,
        secondaryAccent = expressiveAccent.copy(alpha = 0.8f),
        surfaceCharcoal = Color(0xFF121316),
        surfaceElevated = Color(0xFF1E2024),
        backgroundGradient = listOf(Color(0xFF121316), Color(0xFF0A0B0C)),
        textPrimary = textColor,
        textSecondary = textColor.copy(alpha = 0.65f),
        isFallback = true
    )

    MkrLyricsLine(
        entry = entry,
        nextEntryTime = nextEntryTime,
        effectivePlaybackPosition = effectivePlaybackPosition,
        lyricsOffset = lyricsOffset,
        isSynced = isSynced,
        isActive = isActive,
        distanceFromCurrent = distanceFromCurrent,
        lyricsTextPosition = lyricsTextPosition,
        palette = fallbackPalette,
        showRomanized = showRomanized,
        showTranslated = showTranslated,
        onClick = onClick,
        modifier = modifier
    )
}
