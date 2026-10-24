/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.music.mkr.lyrics.LyricsEntry
import com.music.mkr.lyrics.WordTimestamp
import com.music.mkr.ui.screens.settings.LyricsPosition
import com.music.mkr.ui.theme.PlayerArtworkPalette

/**
 * Deprecated legacy LyricsLineV2 composable.
 * Replaced by [MkrLyricsLine] in [LyricsV2].
 */
@Deprecated(
    message = "LyricsLineV2 is deprecated. Use MkrLyricsLine in LyricsV2 instead.",
    level = DeprecationLevel.WARNING
)
@Composable
fun LyricsLineV2(
    entry: LyricsEntry,
    isActive: Boolean,
    isPast: Boolean,
    effectivePlaybackPosition: Long,
    expressiveAccent: Color,
    inactiveAlpha: Float,
    baseFontSize: Float,
    lineHeight: Float,
    showTranslated: Boolean,
    agentAlignment: Alignment.Horizontal = Alignment.Start,
    agentTextAlign: TextAlign = TextAlign.Start,
) {
    val fallbackPalette = PlayerArtworkPalette(
        primaryAccent = expressiveAccent,
        surfaceCharcoal = Color(0xFF121316),
        textPrimary = Color.White,
        textSecondary = Color.White.copy(alpha = inactiveAlpha),
        containerTonal = expressiveAccent.copy(alpha = 0.16f),
        onPrimary = Color.White,
        onSecondary = Color.White
    )

    val lyricsPosition = when (agentTextAlign) {
        TextAlign.Center -> LyricsPosition.CENTER
        TextAlign.Right -> LyricsPosition.RIGHT
        else -> LyricsPosition.LEFT
    }

    MkrLyricsLine(
        entry = entry,
        nextEntryTime = null,
        effectivePlaybackPosition = effectivePlaybackPosition,
        isSynced = true,
        isActive = isActive,
        distanceFromCurrent = if (isActive) 0 else if (isPast) 1 else 2,
        lyricsTextPosition = lyricsPosition,
        palette = fallbackPalette,
        showRomanized = false,
        showTranslated = showTranslated,
        textSize = baseFontSize,
        lineSpacing = lineHeight / baseFontSize,
        onClick = {}
    )
}

/**
 * Deprecated legacy AnimatedWordV2 composable.
 * Word animation is now handled internally by [MkrLyricsLine] and [MkrAnimatedWord].
 */
@Deprecated(
    message = "AnimatedWordV2 is deprecated. Word animations are integrated into MkrLyricsLine.",
    level = DeprecationLevel.WARNING
)
@Composable
fun AnimatedWordV2(
    word: WordTimestamp,
    isLineActive: Boolean,
    isLinePast: Boolean,
    effectivePlaybackPosition: Long,
    expressiveAccent: Color,
    inactiveAlpha: Float,
    fontSize: Float,
    lineHeight: Float,
    isBackground: Boolean = false
) {
    // Delegated inside MkrLyricsLine
}
