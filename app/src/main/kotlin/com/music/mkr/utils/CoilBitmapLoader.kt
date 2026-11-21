/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.media3.common.util.BitmapLoader
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Scale
import coil3.toBitmap
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.music.mkr.R
import com.music.mkr.ui.utils.resize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future
import timber.log.Timber

class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) : BitmapLoader {

    private companion object {
        const val ARTWORK_SIZE = 512
    }

    private val bitmapCache = android.util.LruCache<String, Bitmap>(4)
    private val fallbackBitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.icon, BitmapFactory.Options().apply {
            inScaled = false
        }) ?: createBitmap(128, 128, Bitmap.Config.ARGB_8888)
    }

    override fun supportsMimeType(mimeType: String): Boolean =
        mimeType.startsWith("image/") || mimeType.isEmpty() || mimeType == "*/*"

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> =
        scope.future(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(data, 0, data.size, options)
                val longestEdge = maxOf(options.outWidth, options.outHeight)
                var sampleSize = 1
                while (longestEdge / sampleSize > ARTWORK_SIZE) sampleSize *= 2
                options.inSampleSize = sampleSize
                options.inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: fallbackBitmap
            } catch (e: Exception) {
                Timber.tag("CoilBitmapLoader").w(e, "Failed to decode bitmap data")
                fallbackBitmap
            }
        }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val original = uri.toString()
        val isNetworkArtwork = uri.scheme == "http" || uri.scheme == "https"
        // Match the source URL used by the player and download prefetch. Coil still decodes at notification size.
        val artworkUrl = if (isNetworkArtwork) original.resize(1200, 1200) else original
        bitmapCache.get(artworkUrl)?.takeUnless { it.isRecycled }?.let {
            return Futures.immediateFuture(it)
        }
        return scope.future(Dispatchers.IO) {
            val bitmap = if (isNetworkArtwork) {
                val alternate = artworkUrl.takeIf { artworkUrl.toUri().host == "i.ytimg.com" }
                    ?.replace("/vi_webp/", "/vi/")
                    ?.replace(".webp", ".jpg")
                    ?.replace("maxresdefault.jpg", "hqdefault.jpg")
                listOfNotNull(artworkUrl, original, alternate).distinct().firstNotNullOfOrNull { loadArtwork(it) }
            } else {
                loadArtwork(uri)
            }
            if (bitmap == null) fallbackBitmap else bitmap.also { bitmapCache.put(artworkUrl, it) }
        }
    }

    private suspend fun loadArtwork(data: Any): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(ARTWORK_SIZE, ARTWORK_SIZE)
            .scale(Scale.FIT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        return try {
            when (val result = context.imageLoader.execute(request)) {
                is SuccessResult -> result.image.toBitmap().takeUnless { it.isRecycled }
                is ErrorResult -> {
                    Timber.tag("CoilBitmapLoader").w(result.throwable, "Failed to load artwork")
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag("CoilBitmapLoader").w(e, "Failed to decode artwork")
            null
        }
    }

    override fun loadBitmapFromMetadata(metadata: androidx.media3.common.MediaMetadata): ListenableFuture<Bitmap> {
        metadata.artworkData?.let { return decodeBitmap(it) }
        val artworkUri = metadata.artworkUri
            ?: metadata.extras?.getString("artwork_uri")?.takeIf { it.isNotBlank() }?.toUri()
        return artworkUri?.let(::loadBitmap) ?: Futures.immediateFuture(fallbackBitmap)
    }
}
