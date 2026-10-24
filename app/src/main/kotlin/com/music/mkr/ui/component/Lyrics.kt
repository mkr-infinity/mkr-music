/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.lyrics.LyricsEntry
import kotlin.time.Duration.Companion.seconds

/**
 * Legacy lyrics preview duration. Kept for binary compatibility during deprecation cycle.
 */
@Deprecated(
    message = "LyricsPreviewTime is deprecated as preview mode was consolidated into LyricsV2.",
    level = DeprecationLevel.WARNING
)
val LyricsPreviewTime = 2.seconds

/**
 * Deprecated legacy Lyrics composable.
 *
 * All active player styles and lyrics views in MKR Music now use [LyricsV2] and [MkrLyricsLine].
 * This shim delegates directly to [LyricsV2] to drastically reduce APK DEX size,
 * eliminate thousands of synthetic Compose lambda allocations, and optimize memory consumption.
 */
@Deprecated(
    message = "Legacy Lyrics composable is deprecated. Use LyricsV2 for modern synced lyrics and lower DEX footprint.",
    replaceWith = ReplaceWith(
        expression = "LyricsV2(mediaMetadata = playerConnection.mediaMetadata.value, showLyrics = showLyrics, positionProvider = { sliderPositionProvider() ?: 0L }, modifier = modifier)",
        imports = ["com.music.mkr.ui.component.LyricsV2"]
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    showLyrics: Boolean
) {
    if (!showLyrics) return
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    LyricsV2(
        mediaMetadata = mediaMetadata,
        showLyrics = showLyrics,
        positionProvider = { sliderPositionProvider() ?: 0L },
        modifier = modifier
    )
}

/**
 * Deprecated legacy sealed hierarchy for lyrics items.
 * Replaced by [LyricsListItemV2] in [Lyrics_v2.kt].
 */
@Deprecated(
    message = "LyricsListItem is deprecated. Use LyricsListItemV2 in Lyrics_v2.kt instead.",
    level = DeprecationLevel.WARNING
)
sealed class LyricsListItem {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItem()
    data class Indicator(
        val afterLineIndex: Int,
        val gapMs: Long,
        val gapStartMs: Long,
        val gapEndMs: Long,
        val nextAgent: String?
    ) : LyricsListItem()
}
