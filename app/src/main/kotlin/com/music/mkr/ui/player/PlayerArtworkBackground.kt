package com.music.mkr.ui.player

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.transformations
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import coil3.transform.Transformation
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.ui.player.styles.ImmersiveArtwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal fun playerBackgroundArtworkUrl(thumbnail: String?, itemArtwork: String?): String? =
    thumbnail?.takeIf { it.isNotBlank() } ?: itemArtwork?.takeIf { it.isNotBlank() }

@Composable
internal fun ImmersiveArtworkBackground(
    trackId: String,
    thumbnail: String?,
    itemArtwork: String?,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val shadeTrailingPane = maxWidth > maxHeight || maxWidth >= 832.dp
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        ImmersiveArtwork(
            trackId = trackId,
            thumbnail = thumbnail,
            itemArtwork = itemArtwork,
            animate = animate,
            backgroundColor = Color.Transparent,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            showPlaceholder = false,
            modifier = Modifier.fillMaxSize(),
        )
        val shade = Color(0xFF101014)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to shade.copy(alpha = 0.52f),
                    0.14f to Color.Transparent,
                    0.34f to Color.Transparent,
                    0.57f to shade.copy(alpha = 0.77f),
                    1f to shade.copy(alpha = 0.95f),
                )
            )
        )
        if (shadeTrailingPane) {
            val edge = shade.copy(alpha = 0.80f)
            val stops = if (rtl) arrayOf(0f to edge, 0.5f to edge, 0.8f to Color.Transparent)
                else arrayOf(0.2f to Color.Transparent, 0.5f to edge, 1f to edge)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(*stops)))
        }
    }
}

@Composable
internal fun rememberPlayerBackgroundPainter(
    artworkUrl: String?,
    blurRadius: Int,
    crossfade: Boolean,
): Painter {
    val fallback = remember { ColorPainter(PlayerColorExtractor.CharcoalDark) }
    if (artworkUrl == null) return fallback

    val context = LocalContext.current
    val request = remember(context, artworkUrl, blurRadius, crossfade) {
        ImageRequest.Builder(context)
            .data(artworkUrl)
            .size(128, 128)
            .scale(Scale.FIT)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .transformations(PlayerBackgroundBlur(blurRadius))
            .crossfade(crossfade)
            .build()
    }
    return rememberAsyncImagePainter(
        model = request,
        placeholder = fallback,
        error = fallback,
        onError = { Timber.w("Full-player backdrop image failed; using the ambient fallback") },
    )
}

// Blur the cached thumbnail-sized bitmap once instead of allocating a screen-sized effect layer.
private class PlayerBackgroundBlur(private val radius: Int) : Transformation() {
    override val cacheKey = "player-backdrop-box-v1:$radius"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap = withContext(Dispatchers.Default) {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        blurPlayerBackgroundPixels(pixels, width, height, radius)
        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}

internal fun blurPlayerBackgroundPixels(pixels: IntArray, width: Int, height: Int, radius: Int) {
    val scratch = IntArray(pixels.size)
    repeat(2) {
        blurBackgroundRows(pixels, scratch, width, height, radius)
        blurBackgroundRows(scratch, pixels, height, width, radius)
    }
}

// Write each horizontal pass transposed so the same pass can blur the other axis.
private fun blurBackgroundRows(
    source: IntArray,
    target: IntArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    val samples = radius * 2 + 1
    for (y in 0 until height) {
        val row = y * width
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0

        for (x in -radius..radius) {
            val pixel = source[row + x.coerceIn(0, width - 1)]
            alpha += pixel ushr 24
            red += (pixel ushr 16) and 255
            green += (pixel ushr 8) and 255
            blue += pixel and 255
        }
        for (x in 0 until width) {
            target[x * height + y] = ((alpha / samples) shl 24) or
                ((red / samples) shl 16) or ((green / samples) shl 8) or (blue / samples)

            val outgoing = source[row + (x - radius).coerceAtLeast(0)]
            val incoming = source[row + (x + radius + 1).coerceAtMost(width - 1)]
            alpha += (incoming ushr 24) - (outgoing ushr 24)
            red += ((incoming ushr 16) and 255) - ((outgoing ushr 16) and 255)
            green += ((incoming ushr 8) and 255) - ((outgoing ushr 8) and 255)
            blue += (incoming and 255) - (outgoing and 255)
        }
    }
}
