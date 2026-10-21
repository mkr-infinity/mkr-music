/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.lyrics.LyricsEntry
import com.music.mkr.lyrics.WordTimestamp
import com.music.mkr.ui.screens.settings.LyricsPosition
import com.music.mkr.ui.theme.PlayerArtworkPalette

/**
 * Original MKR Music Synchronized Lyric Line.
 *
 * Implements accessible visual hierarchy:
 * - High-contrast textPrimary with responsive scale on active line.
 * - Vertical amber accent pill on the start edge for unambiguous non-color identification.
 * - Progressive alpha falloff for past and upcoming lines.
 * - Word-by-word timing synchronization when word timestamps exist.
 * - Smooth tap-to-seek interaction with ripple feedback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MkrLyricsLine(
    entry: LyricsEntry,
    nextEntryTime: Long?,
    effectivePlaybackPosition: Long,
    lyricsOffset: Long = 0L,
    isSynced: Boolean,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    palette: PlayerArtworkPalette,
    showRomanized: Boolean,
    showTranslated: Boolean,
    textSize: Float = 26f,
    lineSpacing: Float = 1.35f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val romanizedText by entry.romanizedTextFlow.collectAsState()
    val translatedText by entry.translatedTextFlow.collectAsState()

    val targetAlpha = when {
        !isSynced -> 1.0f
        isActive -> 1.0f
        distanceFromCurrent == 1 -> 0.55f
        distanceFromCurrent == 2 -> 0.35f
        else -> 0.20f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 250),
        label = "mkrLineAlpha"
    )

    val targetScale = if (isActive && isSynced) 1.02f else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 250),
        label = "mkrLineScale"
    )

    val alignment = when {
        entry.isBackground -> Alignment.CenterHorizontally
        entry.agent == "v1" -> Alignment.Start
        entry.agent == "v2" -> Alignment.End
        else -> when (lyricsTextPosition) {
            LyricsPosition.LEFT -> Alignment.Start
            LyricsPosition.CENTER -> Alignment.CenterHorizontally
            LyricsPosition.RIGHT -> Alignment.End
        }
    }

    val textAlign = when {
        entry.isBackground -> TextAlign.Center
        entry.agent == "v1" -> TextAlign.Left
        entry.agent == "v2" -> TextAlign.Right
        else -> when (lyricsTextPosition) {
            LyricsPosition.LEFT -> TextAlign.Left
            LyricsPosition.CENTER -> TextAlign.Center
            LyricsPosition.RIGHT -> TextAlign.Right
        }
    }

    val rawText = entry.text
    val mainText = if (entry.isBackground) {
        rawText.removePrefix("(").removeSuffix(")")
    } else {
        rawText
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primaryAccent),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical active pill indicator (accessible non-color cue)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .graphicsLayer {
                    alpha = if (isActive && isSynced) 1f else 0f
                }
                .background(palette.primaryAccent, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = alignment
        ) {
            val words = entry.words
            val hasWordTimings = !words.isNullOrEmpty()

            if (isSynced && isActive && hasWordTimings) {
                // Word-by-word synchronized highlighting
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = when (textAlign) {
                        TextAlign.Center -> Arrangement.Center
                        TextAlign.Right -> Arrangement.End
                        else -> Arrangement.Start
                    },
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    words!!.forEachIndexed { wordIndex, word ->
                        val wordStartMs = (word.startTime * 1000).toLong()
                        val wordEndMs = (word.endTime * 1000).toLong()
                        val isWordPassed = effectivePlaybackPosition >= wordEndMs
                        val isWordCurrent = effectivePlaybackPosition in wordStartMs until wordEndMs

                        val wordColor = when {
                            isWordCurrent -> palette.primaryAccent
                            isWordPassed -> palette.textPrimary
                            else -> palette.textPrimary.copy(alpha = 0.40f)
                        }

                        val wordWeight = when {
                            isWordCurrent -> FontWeight.ExtraBold
                            isWordPassed -> FontWeight.Bold
                            else -> FontWeight.Normal
                        }

                        Text(
                            text = word.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = (textSize * 1.04f).sp,
                                fontWeight = wordWeight,
                                fontStyle = if (entry.isBackground) FontStyle.Italic else FontStyle.Normal,
                                lineHeight = (textSize * lineSpacing).sp
                            ),
                            color = wordColor,
                            textAlign = textAlign
                        )

                        if (wordIndex < words.size - 1) {
                            Text(
                                text = " ",
                                fontSize = (textSize * 1.04f).sp,
                                lineHeight = (textSize * lineSpacing).sp
                            )
                        }
                    }
                }
            } else {
                // Standard line text display
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = (if (isActive && isSynced) textSize * 1.03f else textSize).sp,
                        fontWeight = if (isActive && isSynced) FontWeight.Bold else FontWeight.Medium,
                        fontStyle = if (entry.isBackground) FontStyle.Italic else FontStyle.Normal,
                        lineHeight = (textSize * lineSpacing).sp
                    ),
                    color = palette.textPrimary.copy(alpha = animatedAlpha),
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Romanized subtitle (if enabled and present)
            if (showRomanized && !romanizedText.isNullOrBlank()) {
                Text(
                    text = romanizedText!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (textSize * 0.65f).coerceAtLeast(13f).sp,
                        lineHeight = (textSize * 0.85f).coerceAtLeast(18f).sp
                    ),
                    color = palette.textSecondary.copy(alpha = animatedAlpha * 0.85f),
                    textAlign = textAlign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )
            }

            // Translated subtitle (if enabled and present)
            if (showTranslated && !translatedText.isNullOrBlank()) {
                Text(
                    text = translatedText!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (textSize * 0.65f).coerceAtLeast(13f).sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = (textSize * 0.85f).coerceAtLeast(18f).sp
                    ),
                    color = palette.primaryAccent.copy(alpha = if (isActive) 0.92f else animatedAlpha * 0.65f),
                    textAlign = textAlign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )
            }
        }
    }
}
