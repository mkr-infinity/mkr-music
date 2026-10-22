/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.delay

/**
 * Global singleton managing Jump Navigation and smooth Rose-Red highlighting
 * across all Settings screens and sub-options.
 */
object SettingsHighlightManager {
    var targetSettingKey by mutableStateOf<String?>(null)
    var targetTimestamp by mutableLongStateOf(0L)

    // Category expansion registry: maps category keys to expansion callbacks
    private val categoryExpanderCallbacks = mutableStateMapOf<String, () -> Unit>()

    /**
     * Request jump navigation and animated highlight for a specific setting key.
     * Optionally expands a collapsed category if specified.
     */
    fun requestHighlight(key: String, categoryKey: String? = null) {
        categoryKey?.let { expandCategory(it) }
        targetSettingKey = key
        targetTimestamp = System.currentTimeMillis()
    }

    fun registerCategoryExpander(categoryKey: String, onExpand: () -> Unit) {
        categoryExpanderCallbacks[categoryKey] = onExpand
    }

    fun unregisterCategoryExpander(categoryKey: String) {
        categoryExpanderCallbacks.remove(categoryKey)
    }

    fun expandCategory(categoryKey: String) {
        categoryExpanderCallbacks[categoryKey]?.invoke()
    }

    fun clear() {
        targetSettingKey = null
    }
}

/**
 * Modifier extension that automatically brings a setting item into view
 * and displays a warm amber glow when targeted.
 *
 * When interfaceAnimations is disabled:
 * - Uses a static hold and immediate snap-to-clear rather than continuous frame animation
 * - Respects reduced-motion preferences to minimize CPU/GPU load
 */
fun Modifier.settingsHighlight(
    key: String?,
    shape: Shape = RoundedCornerShape(16.dp),
    highlightColor: Color = Color(0xFFF59E0B) // MKR Warm Amber Glow
): Modifier = composed {
    if (key.isNullOrBlank()) return@composed this

    val requester = remember { BringIntoViewRequester() }
    val alphaAnim = remember { Animatable(0f) }
    val (interfaceAnimations) = rememberPreference(InterfaceAnimationsKey, defaultValue = true)

    DisposableEffect(key) {
        onDispose {
            if (SettingsHighlightManager.targetSettingKey == key) {
                SettingsHighlightManager.clear()
            }
        }
    }

    LaunchedEffect(SettingsHighlightManager.targetSettingKey, SettingsHighlightManager.targetTimestamp) {
        if (SettingsHighlightManager.targetSettingKey == key) {
            // Allow initial layout and screen transition to settle
            delay(150)
            runCatching {
                requester.bringIntoView()
            }
            alphaAnim.snapTo(1f)
            delay(350)
            if (interfaceAnimations) {
                alphaAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1350, easing = FastOutSlowInEasing)
                )
            } else {
                delay(700)
                alphaAnim.snapTo(0f)
            }
            if (SettingsHighlightManager.targetSettingKey == key) {
                SettingsHighlightManager.clear()
            }
        }
    }

    val alpha = alphaAnim.value
    this
        .bringIntoViewRequester(requester)
        .then(
            if (alpha > 0f) {
                Modifier
                    .drawBehind {
                        drawRoundRect(
                            color = highlightColor.copy(alpha = alpha * 0.22f),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                    .border(
                        width = 2.dp,
                        color = highlightColor.copy(alpha = alpha * 0.85f),
                        shape = shape
                    )
            } else {
                Modifier
            }
        )
}
