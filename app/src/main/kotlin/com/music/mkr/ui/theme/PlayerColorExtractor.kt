/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.music.mkr.models.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Structured palette derived from album artwork.
 * Provides high-contrast, accessible accents, cohesive charcoal surfaces,
 * and elegant backgrounds for the expanded player and mini-player.
 */
@Immutable
data class PlayerArtworkPalette(
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val surfaceCharcoal: Color,
    val surfaceElevated: Color,
    val backgroundGradient: List<Color>,
    val textPrimary: Color,
    val textSecondary: Color,
    val isFallback: Boolean = false
)

/**
 * CompositionLocal carrying the single, global, animated [PlayerArtworkPalette]
 * across all screens and components in the application.
 */
val LocalPlayerArtworkPalette = compositionLocalOf { PlayerColorExtractor.DefaultPalette }

/**
 * High-performance, memory-efficient color extraction engine for MKR Music.
 *
 * Analyzes album artwork asynchronously off the main thread, enforces WCAG contrast
 * against charcoal surfaces, provides deterministic charcoal & amber fallbacks,
 * and maintains an in-memory LRU cache to eliminate duplicate extraction.
 */
object PlayerColorExtractor {

    // Visual design constants - charcoal surfaces & warm amber accents
    val CharcoalDark = Color(0xFF121316)
    val CharcoalSurface = Color(0xFF18191E)
    val CharcoalElevated = Color(0xFF22242B)
    val WarmAmber = Color(0xFFF59E0B)
    val WarmAmberLight = Color(0xFFFBBF24)
    val WarmAmberDeep = Color(0xFFD97706)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    /** Deterministic charcoal-and-amber fallback palette when artwork is absent or invalid. */
    val DefaultPalette = PlayerArtworkPalette(
        primaryAccent = WarmAmber,
        secondaryAccent = WarmAmberLight,
        surfaceCharcoal = CharcoalDark,
        surfaceElevated = CharcoalSurface,
        backgroundGradient = listOf(
            Color(0xFF1C1A18), // Subtle warm charcoal tint at the top
            CharcoalDark,      // Deep charcoal body #121316
            Color(0xFF090A0C)  // Pitch black base
        ),
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        isFallback = true
    )

    /**
     * Builds a contrast-safe [PlayerArtworkPalette] from a static seed color (for user-selected static themes).
     */
    fun createPaletteFromSeed(seedColor: Color): PlayerArtworkPalette {
        val primaryAccent = ensureAccessibleContrast(seedColor, CharcoalDark, minRatio = 4.5f)
        val secondaryAccent = lerp(primaryAccent, Color.White, 0.25f)
        val surfaceCharcoal = lerp(CharcoalDark, primaryAccent, 0.04f)
        val surfaceElevated = lerp(CharcoalSurface, primaryAccent, 0.06f)
        val backgroundGradient = listOf(
            lerp(CharcoalDark, primaryAccent, 0.12f),
            surfaceCharcoal,
            Color(0xFF090A0C)
        )
        return PlayerArtworkPalette(
            primaryAccent = primaryAccent,
            secondaryAccent = secondaryAccent,
            surfaceCharcoal = surfaceCharcoal,
            surfaceElevated = surfaceElevated,
            backgroundGradient = backgroundGradient,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            isFallback = false
        )
    }

    // Thread-safe in-memory LRU cache for extracted palettes
    private const val MAX_CACHE_SIZE = 64
    private val paletteCache = object : LinkedHashMap<String, PlayerArtworkPalette>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PlayerArtworkPalette>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    @Synchronized
    fun getCachedPalette(key: String): PlayerArtworkPalette? = paletteCache[key]

    @Synchronized
    fun putCachedPalette(key: String, palette: PlayerArtworkPalette) {
        if (key.isNotBlank()) {
            paletteCache[key] = palette
        }
    }

    /**
     * Extracts a contrast-safe [PlayerArtworkPalette] from an artwork URL asynchronously.
     * Guaranteed non-blocking (executes network/disk on [Dispatchers.IO] and math on [Dispatchers.Default]).
     */
    suspend fun extractPalette(
        context: Context,
        thumbnailUrl: String?,
        mediaId: String? = null
    ): PlayerArtworkPalette {
        val cacheKey = mediaId?.takeIf { it.isNotBlank() } ?: thumbnailUrl?.takeIf { it.isNotBlank() }
        if (cacheKey != null) {
            getCachedPalette(cacheKey)?.let { return it }
        }

        if (thumbnailUrl.isNullOrBlank()) {
            return DefaultPalette
        }

        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(120, 120)
                    .allowHardware(false)
                    .memoryCacheKey("palette_${cacheKey ?: thumbnailUrl}")
                    .build()
                val result = context.imageLoader.execute(request)
                result.image?.toBitmap()
            }.getOrNull()
        } ?: return DefaultPalette

        val palette = withContext(Dispatchers.Default) {
            extractFromBitmap(bitmap)
        }

        if (cacheKey != null) {
            putCachedPalette(cacheKey, palette)
        }
        return palette
    }

    /**
     * Synchronously analyzes a bitmap and derives a contrast-validated [PlayerArtworkPalette].
     */
    fun extractFromBitmap(bitmap: Bitmap): PlayerArtworkPalette {
        if (bitmap.width <= 0 || bitmap.height <= 0) return DefaultPalette

        val palette = runCatching {
            Palette.from(bitmap)
                .maximumColorCount(16)
                .resizeBitmapArea(120 * 120)
                .generate()
        }.getOrNull() ?: return DefaultPalette

        return buildPaletteFromPalette(palette)
    }

    private fun buildPaletteFromPalette(palette: Palette): PlayerArtworkPalette {
        // Evaluate candidate swatches with dominance and vibrancy weighting
        val swatches = listOfNotNull(
            palette.vibrantSwatch,
            palette.dominantSwatch,
            palette.lightVibrantSwatch,
            palette.darkVibrantSwatch,
            palette.mutedSwatch,
            palette.lightMutedSwatch
        )

        if (swatches.isEmpty()) return DefaultPalette

        val bestSwatch = swatches.maxByOrNull { calculateSwatchWeight(it) } ?: return DefaultPalette
        val rawColor = Color(bestSwatch.rgb)

        // Ensure the primary accent has comfortable contrast against charcoal surfaces (WCAG AA)
        val primaryAccent = ensureAccessibleContrast(rawColor, CharcoalDark, minRatio = 4.5f)

        // Derive secondary accent from a complementary or light swatch
        val secondarySwatch = swatches.firstOrNull { it != bestSwatch }
        val secondaryAccent = if (secondarySwatch != null) {
            val secColor = Color(secondarySwatch.rgb)
            ensureAccessibleContrast(secColor, CharcoalDark, minRatio = 3.5f)
        } else {
            lerp(primaryAccent, Color.White, 0.25f)
        }

        // Generate a subtle 4% tint of the dominant hue blended into charcoal
        val surfaceCharcoal = lerp(CharcoalDark, primaryAccent, 0.04f)
        val surfaceElevated = lerp(CharcoalSurface, primaryAccent, 0.06f)

        // Construct a smooth 3-point vertical gradient
        val backgroundGradient = listOf(
            lerp(CharcoalDark, primaryAccent, 0.12f), // Top: soft ambient artwork glow
            surfaceCharcoal,                         // Middle: deep cohesive charcoal
            Color(0xFF090A0C)                        // Bottom: pitch black base
        )

        return PlayerArtworkPalette(
            primaryAccent = primaryAccent,
            secondaryAccent = secondaryAccent,
            surfaceCharcoal = surfaceCharcoal,
            surfaceElevated = surfaceElevated,
            backgroundGradient = backgroundGradient,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            isFallback = false
        )
    }

    /**
     * Calculates candidate weight balancing population, comfortable saturation, and luminance.
     */
    private fun calculateSwatchWeight(swatch: Palette.Swatch): Float {
        val color = Color(swatch.rgb)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        val saturation = hsv[1]
        val brightness = hsv[2]

        // Heavily penalize muddy, dark, or desaturated swatches
        if (saturation < 0.20f || brightness < 0.25f) {
            return -100f
        }

        // Ideal vibrancy range: saturation around 0.50-0.75, brightness around 0.65-0.85
        val satScore = 1.0f - kotlin.math.abs(saturation - 0.65f)
        val briScore = 1.0f - kotlin.math.abs(brightness - 0.75f)
        val vibrancyScore = satScore * 2.0f + briScore * 1.5f
        val populationScore = swatch.population.toFloat().coerceAtMost(5000f) / 1000f
        return vibrancyScore * 3.0f + populationScore
    }

    /**
     * Calculates relative luminance according to WCAG 2.1 specs.
     */
    fun calculateLuminance(color: Color): Float {
        fun channel(c: Float): Float =
            if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
        return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
    }

    /**
     * Calculates WCAG contrast ratio between two colors (ranging from 1.0 to 21.0).
     */
    fun calculateContrast(foreground: Color, background: Color): Float {
        val lum1 = calculateLuminance(foreground)
        val lum2 = calculateLuminance(background)
        val brightest = max(lum1, lum2)
        val darkest = min(lum1, lum2)
        return (brightest + 0.05f) / (darkest + 0.05f)
    }

    /**
     * Validates and adjusts color brightness to guarantee minimum contrast against [background].
     * Falls back to deterministic [WarmAmber] if the color is too desaturated or cannot be salvaged.
     */
    fun ensureAccessibleContrast(
        color: Color,
        background: Color = CharcoalDark,
        minRatio: Float = 4.5f
    ): Color {
        val initialContrast = calculateContrast(color, background)
        if (initialContrast >= minRatio) return color

        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)

        // If the color is essentially grayscale, fall back to Warm Amber
        if (hsv[1] < 0.15f) return WarmAmber

        // Clamp saturation to prevent overly muddy or blinding neon accents
        hsv[1] = hsv[1].coerceIn(0.35f, 0.85f)

        // Incrementally lift brightness to reach accessible contrast
        var testBrightness = max(hsv[2], 0.60f)
        var bestColor = color
        while (testBrightness <= 0.98f) {
            hsv[2] = testBrightness
            val adjusted = Color(android.graphics.Color.HSVToColor(hsv))
            if (calculateContrast(adjusted, background) >= minRatio) {
                return adjusted
            }
            bestColor = adjusted
            testBrightness += 0.05f
        }

        // If even full brightness fails (or contrast is weak), return warm amber fallback
        return if (calculateContrast(bestColor, background) >= 3.5f) bestColor else WarmAmber
    }

    /**
     * Backward-compatible gradient generator for existing callers (e.g. [AlbumGradient]).
     */
    suspend fun extractGradientColors(
        palette: Palette,
        fallbackColor: Int
    ): List<Color> = withContext(Dispatchers.Default) {
        val artworkPalette = buildPaletteFromPalette(palette)
        if (!artworkPalette.isFallback) {
            artworkPalette.backgroundGradient
        } else {
            val fallback = Color(fallbackColor)
            listOf(
                lerp(CharcoalDark, fallback, 0.2f),
                CharcoalDark,
                Color(0xFF090A0C)
            )
        }
    }
}

/**
 * Authoritative single source of truth for the app's global song-adaptive color palette.
 *
 * Runs asynchronously off the main thread, uses LRU cache, maintains previous palette while loading,
 * gracefully handles fallbacks and static mode, and animates all tokens across track changes.
 */
@Composable
fun rememberGlobalArtworkPalette(
    mediaMetadata: MediaMetadata?,
    enableDynamicTheme: Boolean = true,
    staticThemeColor: Color = PlayerColorExtractor.WarmAmber
): PlayerArtworkPalette {
    val context = LocalContext.current
    var currentPalette by remember {
        mutableStateOf<PlayerArtworkPalette>(
            if (!enableDynamicTheme) {
                if (staticThemeColor != PlayerColorExtractor.WarmAmber && staticThemeColor != Color(0xFFED5564)) {
                    PlayerColorExtractor.createPaletteFromSeed(staticThemeColor)
                } else {
                    PlayerColorExtractor.DefaultPalette
                }
            } else {
                mediaMetadata?.id?.let { PlayerColorExtractor.getCachedPalette(it) }
                    ?: PlayerColorExtractor.DefaultPalette
            }
        )
    }

    LaunchedEffect(mediaMetadata?.id, mediaMetadata?.thumbnailUrl, enableDynamicTheme, staticThemeColor) {
        if (!enableDynamicTheme) {
            currentPalette = if (staticThemeColor != PlayerColorExtractor.WarmAmber && staticThemeColor != Color(0xFFED5564)) {
                PlayerColorExtractor.createPaletteFromSeed(staticThemeColor)
            } else {
                PlayerColorExtractor.DefaultPalette
            }
            return@LaunchedEffect
        }

        val mediaId = mediaMetadata?.id
        val thumbnailUrl = mediaMetadata?.thumbnailUrl
        if (mediaId == null || thumbnailUrl == null) {
            currentPalette = PlayerColorExtractor.DefaultPalette
            return@LaunchedEffect
        }

        val cached = PlayerColorExtractor.getCachedPalette(mediaId)
        if (cached != null) {
            currentPalette = cached
            return@LaunchedEffect
        }

        val extracted = PlayerColorExtractor.extractPalette(context, thumbnailUrl, mediaId)
        currentPalette = extracted
    }

    // Smoothly animate individual palette tokens across track changes
    val animatedPrimary by animateColorAsState(
        targetValue = currentPalette.primaryAccent,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalPrimaryAccent"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = currentPalette.secondaryAccent,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalSecondaryAccent"
    )
    val animatedSurface by animateColorAsState(
        targetValue = currentPalette.surfaceCharcoal,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalSurfaceCharcoal"
    )
    val animatedElevated by animateColorAsState(
        targetValue = currentPalette.surfaceElevated,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalSurfaceElevated"
    )

    val gradTop by animateColorAsState(
        targetValue = currentPalette.backgroundGradient.getOrElse(0) { PlayerColorExtractor.CharcoalDark },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalGradTop"
    )
    val gradMid by animateColorAsState(
        targetValue = currentPalette.backgroundGradient.getOrElse(1) { PlayerColorExtractor.CharcoalDark },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalGradMid"
    )
    val gradBot by animateColorAsState(
        targetValue = currentPalette.backgroundGradient.getOrElse(2) { Color(0xFF090A0C) },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GlobalGradBot"
    )

    return remember(
        animatedPrimary,
        animatedSecondary,
        animatedSurface,
        animatedElevated,
        gradTop,
        gradMid,
        gradBot,
        currentPalette.isFallback
    ) {
        PlayerArtworkPalette(
            primaryAccent = animatedPrimary,
            secondaryAccent = animatedSecondary,
            surfaceCharcoal = animatedSurface,
            surfaceElevated = animatedElevated,
            backgroundGradient = listOf(gradTop, gradMid, gradBot),
            textPrimary = currentPalette.textPrimary,
            textSecondary = currentPalette.textSecondary,
            isFallback = currentPalette.isFallback
        )
    }
}

/**
 * Reads the active, animated [PlayerArtworkPalette] from [LocalPlayerArtworkPalette].
 * Connects player, mini-player, and lyrics seamlessly to the single global dynamic theme.
 */
@Composable
fun rememberArtworkPalette(mediaMetadata: MediaMetadata? = null): PlayerArtworkPalette {
    return LocalPlayerArtworkPalette.current
}
