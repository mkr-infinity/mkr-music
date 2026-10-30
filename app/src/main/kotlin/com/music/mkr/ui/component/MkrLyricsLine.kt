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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.constants.LyricsAnimationStyle
import com.music.mkr.lyrics.LyricsEntry
import com.music.mkr.lyrics.LyricsUtils
import com.music.mkr.lyrics.WordTimestamp
import com.music.mkr.ui.screens.settings.LyricsPosition
import com.music.mkr.ui.theme.PlayerArtworkPalette

/**
 * Internal token representation for word- and character-level lyrics animation.
 */
internal data class LyricWordToken(
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val hasTrailingSpace: Boolean
)

/**
 * Helper to identify CJK character blocks for unspaced language segmentation.
 */
private fun isCjkChar(c: Char): Boolean {
    val block = Character.UnicodeBlock.of(c)
    return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
            block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
            block == Character.UnicodeBlock.HIRAGANA ||
            block == Character.UnicodeBlock.KATAKANA ||
            block == Character.UnicodeBlock.HANGUL_SYLLABLES ||
            block == Character.UnicodeBlock.HANGUL_JAMO ||
            block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
}

/**
 * Synthesizes proportional word and syllable tokens for standard line-synced LRC files.
 * This ensures that letter-by-letter progressive animation works consistently across all
 * songs and providers, even when word-level timestamps are not originally present.
 */
private fun synthesizeWordTokens(
    text: String,
    lineStartMs: Long,
    nextEntryTime: Long?
): List<LyricWordToken> {
    if (text.isBlank()) return emptyList()

    val trimmedText = text.trim()

    // 1. Determine realistic line duration
    val lineDurationMs = if (nextEntryTime != null && nextEntryTime > lineStartMs) {
        val gap = nextEntryTime - lineStartMs
        if (gap > 4500L) {
            val charBased = (trimmedText.length * 150L).coerceIn(2000L, 5500L)
            minOf(gap - 600L, charBased)
        } else {
            (gap - 80L).coerceAtLeast(800L)
        }
    } else {
        (trimmedText.length * 160L).coerceIn(2200L, 6000L)
    }

    // 2. Parse text into raw units while preserving space delimiter intent
    val wordsWithSpacing = mutableListOf<Pair<String, Boolean>>()
    val rawWords = trimmedText.split(Regex("\\s+")).filter { it.isNotEmpty() }

    for (wIndex in rawWords.indices) {
        val word = rawWords[wIndex]
        val hasTrailingSpaceAfterWord = wIndex < rawWords.lastIndex

        // Check if word consists of CJK characters without internal spaces
        val isAllCjk = word.all { isCjkChar(it) }
        if (isAllCjk && word.length > 1) {
            for (cIndex in word.indices) {
                val isLastChar = cIndex == word.lastIndex
                wordsWithSpacing.add(
                    Pair(
                        word[cIndex].toString(),
                        isLastChar && hasTrailingSpaceAfterWord
                    )
                )
            }
        } else {
            wordsWithSpacing.add(Pair(word, hasTrailingSpaceAfterWord))
        }
    }

    if (wordsWithSpacing.isEmpty()) return emptyList()

    val totalChars = wordsWithSpacing.sumOf { it.first.length }.coerceAtLeast(1)
    var currentOffset = lineStartMs

    return wordsWithSpacing.mapIndexed { index, (unitText, hasSpace) ->
        val proportion = unitText.length.toDouble() / totalChars
        val duration = (lineDurationMs * proportion).toLong().coerceAtLeast(80L)
        val startMs = currentOffset
        val endMs = if (index == wordsWithSpacing.lastIndex) {
            lineStartMs + lineDurationMs
        } else {
            startMs + duration
        }
        currentOffset = endMs
        LyricWordToken(
            text = unitText,
            startTimeMs = startMs,
            endTimeMs = endMs,
            hasTrailingSpace = hasSpace
        )
    }
}

/**
 * Original MKR Music Synchronized Lyric Line.
 *
 * Implements accessible visual hierarchy and smooth letter-by-letter animation:
 * - High-contrast textPrimary with responsive scale on active line.
 * - Vertical amber accent pill on the start edge for unambiguous non-color identification.
 * - Progressive alpha falloff for past and upcoming lines.
 * - Character-by-character liquid sweep reveal across all songs and providers.
 * - Accurate seek, pause/resume, and speed synchronization.
 * - Romanized and translated subtitle presentation.
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
    lyricsAnimationStyle: LyricsAnimationStyle = LyricsAnimationStyle.APPLE_V2,
    glowEffectEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val romanizedText by entry.romanizedTextFlow.collectAsState()
    val translatedText by entry.translatedTextFlow.collectAsState()

    val adjustedPlaybackPosition = effectivePlaybackPosition + lyricsOffset

    val targetAlpha = when {
        !isSynced && isActive -> 1.0f
        !isSynced -> 0.45f
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

    // Determine whether this line has finished singing in playback
    val isLinePast = if (isSynced) {
        if (!entry.words.isNullOrEmpty()) {
            val lastWordEndMs = (entry.words.last().endTime * 1000).toLong()
            adjustedPlaybackPosition >= lastWordEndMs
        } else if (nextEntryTime != null) {
            adjustedPlaybackPosition >= nextEntryTime
        } else {
            adjustedPlaybackPosition >= entry.time + (mainText.length * 160L).coerceIn(2500L, 6000L)
        }
    } else {
        false
    }

    // Prepare tokens for rich-sync or line-synced lyrics
    val tokens = remember(entry.words, mainText, entry.time, nextEntryTime) {
        if (!entry.words.isNullOrEmpty()) {
            val isCjk = LyricsUtils.isJapanese(mainText) || LyricsUtils.isChinese(mainText)
            entry.words.mapIndexed { index, word ->
                val startMs = (word.startTime * 1000).toLong()
                val endMs = (word.endTime * 1000).toLong()
                LyricWordToken(
                    text = word.text.trim(),
                    startTimeMs = startMs,
                    endTimeMs = maxOf(startMs + 50L, endMs),
                    hasTrailingSpace = index < entry.words.size - 1 && !isCjk
                )
            }
        } else if (isSynced && mainText.isNotBlank()) {
            synthesizeWordTokens(
                text = mainText,
                lineStartMs = entry.time,
                nextEntryTime = nextEntryTime
            )
        } else {
            emptyList()
        }
    }

    val enableAnimation = lyricsAnimationStyle != LyricsAnimationStyle.NONE

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
            if (tokens.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = when (textAlign) {
                        TextAlign.Center -> Arrangement.Center
                        TextAlign.Right -> Arrangement.End
                        else -> Arrangement.Start
                    },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    tokens.forEach { token ->
                        MkrAnimatedWord(
                            word = token,
                            isLineActive = isActive,
                            isLinePast = isLinePast,
                            playbackPositionMs = adjustedPlaybackPosition,
                            palette = palette,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            isBackground = entry.isBackground,
                            enableAnimation = enableAnimation,
                            enableGlow = glowEffectEnabled,
                            lineAlpha = animatedAlpha,
                            textAlign = textAlign
                        )
                        if (token.hasTrailingSpace) {
                            Text(
                                text = " ",
                                fontSize = (if (isActive && isSynced) textSize * 1.03f else textSize).sp,
                                lineHeight = (textSize * lineSpacing).sp
                            )
                        }
                    }
                }
            } else {
                // Static display for empty tokens or unsynced lines
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = (if (isActive && isSynced) textSize * 1.03f else textSize).sp,
                        fontWeight = if (isActive && isSynced) FontWeight.Bold else FontWeight.Medium,
                        fontStyle = if (entry.isBackground) FontStyle.Italic else FontStyle.Normal,
                        lineHeight = (textSize * lineSpacing).sp
                    ),
                    color = if (isActive) palette.textPrimary else palette.textPrimary.copy(alpha = animatedAlpha),
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
                    color = palette.textPrimary.copy(alpha = if (isActive) 0.90f else animatedAlpha * 0.65f),
                    textAlign = textAlign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Individual animated word or syllable composable.
 * Renders a base text layer for exact layout bounds, and an overlaid liquid sweep layer
 * that reveals character-by-character in sync with playback timing.
 */
@Composable
private fun MkrAnimatedWord(
    word: LyricWordToken,
    isLineActive: Boolean,
    isLinePast: Boolean,
    playbackPositionMs: Long,
    palette: PlayerArtworkPalette,
    textSize: Float,
    lineSpacing: Float,
    isBackground: Boolean,
    enableAnimation: Boolean,
    enableGlow: Boolean,
    lineAlpha: Float,
    textAlign: TextAlign
) {
    val wordStartMs = word.startTimeMs
    val wordEndMs = word.endTimeMs
    val wordDuration = (wordEndMs - wordStartMs).coerceAtLeast(1L)

    val isWordComplete = isLinePast || (isLineActive && playbackPositionMs >= wordEndMs)
    val isWordActive = isLineActive && playbackPositionMs in wordStartMs until wordEndMs

    val progress = when {
        !enableAnimation -> if (isLineActive) 1f else 0f
        !isLineActive -> 0f
        isWordComplete -> 1f
        playbackPositionMs <= wordStartMs -> 0f
        else -> ((playbackPositionMs - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
    }

    val fontStyle = if (isBackground) FontStyle.Italic else FontStyle.Normal
    val density = LocalDensity.current
    val edgeWidth = with(density) { 6.dp.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // Base dimmed color
    val baseColor = when {
        isWordComplete -> palette.textPrimary
        isLineActive -> palette.textPrimary.copy(alpha = 0.38f)
        else -> palette.textPrimary.copy(alpha = lineAlpha)
    }

    // Active sung color: dynamic artwork primaryAccent
    val activeColor = if (isWordActive) palette.primaryAccent else palette.textPrimary

    // Subtle micro-lift when actively sung
    val sinProgress = if (isWordActive && enableAnimation) {
        kotlin.math.sin(progress * kotlin.math.PI).toFloat()
    } else 0f
    val floatOffset = -1.5f * sinProgress * density.density

    // Glow shadow for the active character edge
    val glowProgress = (progress * 2f).coerceAtMost(1f)
    val glowAlpha = if (enableGlow && isWordActive) glowProgress * 0.45f else 0f
    val glowRadius = if (enableGlow && isWordActive) glowProgress * 10f else 0f

    val fontSize = (if (isLineActive) textSize * 1.03f else textSize).sp
    val lineHeight = (textSize * lineSpacing).sp
    val fontWeight = if (isLineActive) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = floatOffset
        }
    ) {
        // Layer 1: Base text (always present to preserve exact bounds)
        Text(
            text = word.text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                lineHeight = lineHeight
            ),
            color = baseColor,
            textAlign = textAlign
        )

        // Layer 2: Highlighted overlay with letter-by-letter liquid sweep
        if ((isWordComplete || isWordActive) && isLineActive) {
            Text(
                text = word.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    fontStyle = fontStyle,
                    lineHeight = lineHeight,
                    shadow = if (glowAlpha > 0f) {
                        Shadow(
                            color = palette.primaryAccent.copy(alpha = glowAlpha),
                            blurRadius = glowRadius.coerceAtLeast(1f)
                        )
                    } else null
                ),
                color = activeColor,
                textAlign = textAlign,
                modifier = if (isWordActive && progress < 1f && enableAnimation) {
                    Modifier
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            val w = size.width
                            if (isRtl) {
                                val center = w - (w + edgeWidth * 2) * progress + edgeWidth
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                        startX = center - edgeWidth,
                                        endX = center + edgeWidth
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            } else {
                                val center = (w + edgeWidth * 2) * progress - edgeWidth
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.Black, Color.Transparent),
                                        startX = center - edgeWidth,
                                        endX = center + edgeWidth
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            }
                        }
                } else Modifier
            )
        }
    }
}
