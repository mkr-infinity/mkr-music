package com.music.mkr.ui.player

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Size
import coil3.transform.Transformation
import coil3.request.transformations
import com.music.mkr.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal fun playerBackgroundArtworkUrl(thumbnail: String?, itemArtwork: String?): String? =
    thumbnail?.takeIf { it.isNotBlank() } ?: itemArtwork?.takeIf { it.isNotBlank() }

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
        val original = IntArray(width * height)
        val scratch = IntArray(original.size)
        input.getPixels(original, 0, width, 0, 0, width, height)

        repeat(2) {
            blurRows(original, scratch, width, height)
            blurRows(scratch, original, height, width)
        }

        Bitmap.createBitmap(original, width, height, Bitmap.Config.ARGB_8888)
    }

    // Write each horizontal pass transposed so the same pass can blur the other axis.
    private fun blurRows(source: IntArray, target: IntArray, width: Int, height: Int) {
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
}
