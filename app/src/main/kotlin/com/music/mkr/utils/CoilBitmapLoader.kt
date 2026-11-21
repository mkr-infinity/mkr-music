/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.BitmapLoader
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.music.mkr.constants.DataSaverKey
import java.io.IOException
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future

class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) : BitmapLoader {
    private companion object {
        const val ARTWORK_SIZE = 512
        val googleResizeSuffix = Regex("=(?:s|w)\\d+(?:-h\\d+)?(?:-[a-zA-Z0-9]+)*$")
        val videoArtworkPath = Regex("/(vi|vi_webp)/([A-Za-z0-9_-]+)/(default|mqdefault|hqdefault|sddefault|maxresdefault)\\.(jpg|webp)")
    }

    private val bitmapCache = LruCache<Uri, Bitmap>(4)
    private val inFlight = mutableMapOf<Uri, ListenableFuture<Bitmap>>()
    // A non-null future invalidates Media3 1.7's pending artwork even when the new item has no cover.
    private val missingArtwork = Futures.immediateFailedFuture<Bitmap>(IOException("No album artwork"))

    override fun supportsMimeType(mimeType: String): Boolean =
        mimeType.startsWith("image/") || mimeType.isEmpty() || mimeType == "*/*"

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> =
        scope.future(Dispatchers.IO) {
            loadArtwork(data) ?: throw IOException("Could not decode media artwork")
        }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val candidates = artworkCandidates(uri, MkrPrefCache.get(DataSaverKey) == true)
        val key = candidates.first()
        synchronized(inFlight) {
            bitmapCache.get(key)?.takeUnless { it.isRecycled }?.let { return Futures.immediateFuture(it) }
            inFlight[key]?.let { return Futures.nonCancellationPropagating(it) }

            val request = scope.future(Dispatchers.IO) {
                val bitmap = candidates.firstNotNullOfOrNull { loadArtwork(it) }
                    ?: throw IOException("Could not load media artwork")
                bitmapCache.put(key, bitmap)
                bitmap
            }
            inFlight[key] = request
            request.addListener({
                synchronized(inFlight) { inFlight.remove(key) }
            }, MoreExecutors.directExecutor())
            return Futures.nonCancellationPropagating(request)
        }
    }

    private suspend fun loadArtwork(data: Any): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(ARTWORK_SIZE, ARTWORK_SIZE)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .build()
        return try {
            val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
            val bitmap = result.image.toBitmap().takeUnless { it.isRecycled } ?: return null
            val longestEdge = maxOf(bitmap.width, bitmap.height)
            if (longestEdge <= ARTWORK_SIZE) return bitmap

            val ratio = ARTWORK_SIZE.toFloat() / longestEdge
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
                (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
                true,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override fun loadBitmapFromMetadata(metadata: MediaMetadata): ListenableFuture<Bitmap> {
        val trackArtwork = metadata.extras?.getString("artwork_uri")?.takeIf { it.isNotBlank() }?.toUri()
        val uri = metadata.artworkUri?.takeUnless { it.toString().isBlank() } ?: trackArtwork
        val embedded = metadata.artworkData?.takeIf { it.isNotEmpty() }
        if (uri == null) return embedded?.let(::decodeBitmap) ?: missingArtwork

        val request = loadBitmap(uri)
        val alternate = trackArtwork?.takeIf { it != uri }?.let { fallback ->
            Futures.catchingAsync(request, IOException::class.java, { loadBitmap(fallback) }, MoreExecutors.directExecutor())
        } ?: request
        return if (embedded == null) alternate else {
            Futures.catchingAsync(alternate, IOException::class.java, { decodeBitmap(embedded) }, MoreExecutors.directExecutor())
        }
    }

    private fun artworkCandidates(uri: Uri, dataSaver: Boolean): List<Uri> {
        if (uri.scheme != "http" && uri.scheme != "https") return listOf(uri)
        val host = uri.host?.lowercase().orEmpty()
        val path = uri.encodedPath ?: return listOf(uri)
        if (uri.encodedQuery != null || uri.fragment != null) return listOf(uri)

        if (host == "googleusercontent.com" || host.endsWith(".googleusercontent.com") ||
            host == "ggpht.com" || host.endsWith(".ggpht.com")
        ) {
            val suffix = googleResizeSuffix.find(path) ?: return listOf(uri)
            val size = if (dataSaver) 300 else 768
            val originalSize = suffix.value.removePrefix("=s").toIntOrNull()
            if (!dataSaver && originalSize != null && originalSize >= ARTWORK_SIZE) return listOf(uri)
            val fitted = uri.buildUpon().encodedPath(path.substring(0, suffix.range.first) + "=s$size").build()
            return listOf(fitted, uri).distinct()
        }

        if (host != "i.ytimg.com" && host != "img.youtube.com") return listOf(uri)
        val (_, videoId, variant, _) = videoArtworkPath.matchEntire(path)?.destructured ?: return listOf(uri)
        fun videoArtwork(tier: String, webp: Boolean = false) =
            uri.buildUpon().encodedPath("/${if (webp) "vi_webp" else "vi"}/$videoId/$tier.${if (webp) "webp" else "jpg"}").build()

        return when {
            dataSaver -> listOf(videoArtwork("mqdefault", webp = true), videoArtwork("mqdefault"), uri)
            variant == "default" -> listOf(videoArtwork("hqdefault"), uri)
            variant == "mqdefault" -> listOf(videoArtwork("maxresdefault"), uri)
            variant == "maxresdefault" -> listOf(uri, videoArtwork("hqdefault"))
            else -> listOf(uri, videoArtwork(variant))
        }.distinct()
    }
}
