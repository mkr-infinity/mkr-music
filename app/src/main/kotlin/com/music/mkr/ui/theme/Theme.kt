/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score

import androidx.compose.runtime.getValue
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.SelectedFontKey
import com.music.mkr.constants.CustomFontPathKey
import com.music.mkr.constants.AppFont
import com.music.mkr.utils.rememberPreference
import androidx.compose.ui.text.font.FontFamily
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

val DefaultThemeColor = Color(0xFFF59E0B)

@Composable
fun mkrMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    artworkPalette: PlayerArtworkPalette? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val selectedFontValue by rememberPreference(SelectedFontKey, AppFont.SYSTEM.value)
    val customFontPath by rememberPreference(CustomFontPathKey, "")

    val brandFont = remember(selectedFontValue, customFontPath) {
        when (AppFont.fromValue(selectedFontValue)) {
            AppFont.SYSTEM -> FontFamily.Default
            AppFont.GOOGLE_SANS -> GoogleSansFontFamily
            AppFont.SANS_FLEX -> SansFlexFontFamily
            AppFont.OUTFIT -> OutfitFontFamily
            AppFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
            AppFont.CUSTOM -> {
                try {
                    if (customFontPath.isNotEmpty() && java.io.File(customFontPath).exists()) {
                        val typeface = AndroidTypeface.createFromFile(customFontPath)
                        FontFamily(ComposeTypeface(typeface))
                    } else {
                        FontFamily.Default
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    FontFamily.Default
                }
            }
        }
    }

    val typography = remember(brandFont) {
        getTypography(brandFont = brandFont, plainFont = brandFont)
    }

    val effectivePalette = remember(artworkPalette, themeColor) {
        artworkPalette ?: if (themeColor != DefaultThemeColor && themeColor != Color(0xFFED5564)) {
            PlayerColorExtractor.createPaletteFromSeed(themeColor)
        } else {
            PlayerColorExtractor.DefaultPalette
        }
    }

    val colorScheme = remember(effectivePalette, darkTheme, pureBlack) {
        if (darkTheme) {
            val onPrimary = if (PlayerColorExtractor.calculateLuminance(effectivePalette.primaryAccent) > 0.45f) {
                Color(0xFF0F172A)
            } else {
                Color.White
            }
            val onSecondary = if (PlayerColorExtractor.calculateLuminance(effectivePalette.secondaryAccent) > 0.45f) {
                Color(0xFF0F172A)
            } else {
                Color.White
            }
            ColorScheme(
                primary = effectivePalette.primaryAccent,
                onPrimary = onPrimary,
                primaryContainer = lerp(Color(0xFF181A20), effectivePalette.primaryAccent, 0.16f),
                onPrimaryContainer = Color(0xFFF8FAFC),
                inversePrimary = effectivePalette.primaryAccent,
                secondary = effectivePalette.secondaryAccent,
                onSecondary = onSecondary,
                secondaryContainer = lerp(Color(0xFF181A20), effectivePalette.secondaryAccent, 0.12f),
                onSecondaryContainer = Color(0xFFF8FAFC),
                tertiary = effectivePalette.secondaryAccent,
                onTertiary = onSecondary,
                tertiaryContainer = lerp(Color(0xFF181A20), effectivePalette.secondaryAccent, 0.10f),
                onTertiaryContainer = Color(0xFFF8FAFC),
                background = if (pureBlack) Color.Black else Color(0xFF121316),
                onBackground = Color(0xFFF8FAFC),
                surface = if (pureBlack) Color.Black else Color(0xFF121316),
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = if (pureBlack) Color(0xFF101012) else Color(0xFF1E2026),
                onSurfaceVariant = Color(0xFF94A3B8),
                surfaceTint = effectivePalette.primaryAccent,
                inverseSurface = Color(0xFFF8FAFC),
                inverseOnSurface = Color(0xFF121316),
                error = Color(0xFFEF4444),
                onError = Color.White,
                errorContainer = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                onErrorContainer = Color(0xFFFCA5A5),
                outline = Color.White.copy(alpha = 0.12f),
                outlineVariant = Color.White.copy(alpha = 0.08f),
                scrim = Color.Black,
                surfaceBright = if (pureBlack) Color(0xFF141418) else Color(0xFF22242B),
                surfaceDim = if (pureBlack) Color.Black else Color(0xFF121316),
                surfaceContainer = if (pureBlack) Color(0xFF121214) else Color(0xFF181A20),
                surfaceContainerHigh = if (pureBlack) Color(0xFF18181C) else Color(0xFF1F2026),
                surfaceContainerHighest = if (pureBlack) Color(0xFF202024) else Color(0xFF262830),
                surfaceContainerLow = if (pureBlack) Color.Black else Color(0xFF121316),
                surfaceContainerLowest = if (pureBlack) Color.Black else Color(0xFF0C0D0F),
            )
        } else {
            val onPrimary = if (PlayerColorExtractor.calculateLuminance(effectivePalette.primaryAccent) > 0.45f) {
                Color(0xFF0F172A)
            } else {
                Color.White
            }
            val onSecondary = if (PlayerColorExtractor.calculateLuminance(effectivePalette.secondaryAccent) > 0.45f) {
                Color(0xFF0F172A)
            } else {
                Color.White
            }
            ColorScheme(
                primary = effectivePalette.primaryAccent,
                onPrimary = onPrimary,
                primaryContainer = lerp(Color.White, effectivePalette.primaryAccent, 0.14f),
                onPrimaryContainer = Color(0xFF0F172A),
                inversePrimary = effectivePalette.secondaryAccent,
                secondary = effectivePalette.secondaryAccent,
                onSecondary = onSecondary,
                secondaryContainer = lerp(Color.White, effectivePalette.secondaryAccent, 0.12f),
                onSecondaryContainer = Color(0xFF1E293B),
                tertiary = effectivePalette.secondaryAccent,
                onTertiary = onSecondary,
                tertiaryContainer = lerp(Color.White, effectivePalette.secondaryAccent, 0.10f),
                onTertiaryContainer = Color(0xFF1E293B),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                surfaceTint = effectivePalette.primaryAccent,
                inverseSurface = Color(0xFF1E293B),
                inverseOnSurface = Color(0xFFF8FAFC),
                error = Color(0xFFDC2626),
                onError = Color.White,
                errorContainer = Color(0xFFFEE2E2),
                onErrorContainer = Color(0xFF991B1B),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0),
                scrim = Color.Black,
                surfaceBright = Color.White,
                surfaceDim = Color(0xFFE2E8F0),
                surfaceContainer = Color(0xFFF1F5F9),
                surfaceContainerHigh = Color(0xFFE2E8F0),
                surfaceContainerHighest = Color(0xFFCBD5E1),
                surfaceContainerLow = Color(0xFFF8FAFC),
                surfaceContainerLowest = Color.White,
            )
        }
    }

    val interfaceAnimations by rememberPreference(InterfaceAnimationsKey, true)
    val animatedColorScheme = if (!interfaceAnimations) {
        colorScheme
    } else {
        animateColorScheme(
            targetColorScheme = colorScheme,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    CompositionLocalProvider(
        LocalPlayerArtworkPalette provides effectivePalette
    ) {
        MaterialExpressiveTheme(
            colorScheme = animatedColorScheme,
            typography = typography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

@Composable
private fun animateColorScheme(
    targetColorScheme: ColorScheme,
    animationSpec: FiniteAnimationSpec<Color>,
): ColorScheme =
    ColorScheme(
        primary = animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary").value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary").value,
        secondary = animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary").value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary").value,
        onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value,
        background = animateColorAsState(targetColorScheme.background, animationSpec, label = "background").value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground").value,
        surface = animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface").value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface").value,
        error = animateColorAsState(targetColorScheme.error, animationSpec, label = "error").value,
        onError = animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError").value,
        errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer").value,
        outline = animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline").value,
        outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant").value,
        scrim = animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim").value,
        surfaceBright = animateColorAsState(targetColorScheme.surfaceBright, animationSpec, label = "surfaceBright").value,
        surfaceDim = animateColorAsState(targetColorScheme.surfaceDim, animationSpec, label = "surfaceDim").value,
        surfaceContainer = animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "surfaceContainer").value,
        surfaceContainerLow = animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec, label = "surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec, label = "surfaceContainerLowest").value,
        surfaceContainerHigh = animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest").value,
    )

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
    val extractedColors = Palette.from(this)
        .maximumColorCount(64)
        .generate()
        .swatches
        .associate { it.rgb to it.population }

    val orderedColors = Score.score(extractedColors, 2, 0xff4285f4.toInt(), true)
        .sortedByDescending { Color(it).luminance() }

    return if (orderedColors.size >= 2)
        listOf(Color(orderedColors[0]), Color(orderedColors[1]))
    else
        listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
