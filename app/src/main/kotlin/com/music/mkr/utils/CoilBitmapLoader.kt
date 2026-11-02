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
import androidx.media3.common.util.BitmapLoader
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.music.mkr.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future
import timber.log.Timber

class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) : BitmapLoader {

    companion object {
        private const val MAX_BITMAP_CACHE_SIZE = 16
        private val bitmapCache = object : LinkedHashMap<String, Bitmap>(MAX_BITMAP_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
                return size > MAX_BITMAP_CACHE_SIZE
            }
        }

        @Synchronized
        fun getCachedBitmap(key: String): Bitmap? {
            val bmp = bitmapCache[key]
            return if (bmp != null && !bmp.isRecycled) bmp else null
        }

        @Synchronized
        fun putCachedBitmap(key: String, bitmap: Bitmap) {
            if (key.isNotBlank() && !bitmap.isRecycled) {
                bitmapCache[key] = bitmap
            }
        }
    }
    
    override fun supportsMimeType(mimeType: String): Boolean =
        mimeType.startsWith("image/") || mimeType.isEmpty() || mimeType == "*/*"

    private fun createFallbackBitmap(): Bitmap =
        try {
            val bmp = BitmapFactory.decodeResource(context.resources, com.music.mkr.R.drawable.icon)
            if (bmp != null && !bmp.isRecycled) {
                bmp.copy(Bitmap.Config.ARGB_8888, false) ?: bmp
            } else {
                createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            }
        } catch (_: Exception) {
            createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        }

    private fun Bitmap.copyIfNeeded(): Bitmap {
        return if (isRecycled) {
            createFallbackBitmap()
        } else {
            try {
                copy(Bitmap.Config.ARGB_8888, false) ?: createFallbackBitmap()
            } catch (e: Exception) {
                createFallbackBitmap()
            }
        }
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> =
        scope.future(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                bitmap?.copyIfNeeded() ?: createFallbackBitmap()
            } catch (e: Exception) {
                Timber.tag("CoilBitmapLoader").w(e, "Failed to decode bitmap data")
                createFallbackBitmap()
            }
        }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val uriString = uri.toString()
        val cached = getCachedBitmap(uriString)
        if (cached != null) {
            return Futures.immediateFuture(cached)
        }

        return scope.future(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(if (uri.scheme == "http" || uri.scheme == "https") uriString else uri)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            var result = try {
                context.imageLoader.execute(request)
            } catch (e: Exception) {
                Timber.tag("CoilBitmapLoader").w(e, "Exception executing image request for $uri")
                null
            }

            // If maxresdefault 404s, fall back to hqdefault which is universally available on YouTube
            if ((result == null || result is ErrorResult) && uriString.contains("maxresdefault")) {
                val fallbackUri = uriString
                    .replace("maxresdefault.webp", "hqdefault.webp")
                    .replace("maxresdefault.jpg", "hqdefault.jpg")
                val fallbackRequest = ImageRequest.Builder(context)
                    .data(fallbackUri)
                    .allowHardware(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                result = try {
                    context.imageLoader.execute(fallbackRequest)
                } catch (_: Exception) {
                    null
                }
            }

            when (result) {
                is ErrorResult -> {
                    Timber.tag("CoilBitmapLoader").w(result.throwable, "Failed to load bitmap from uri: $uri")
                    createFallbackBitmap()
                }
                is SuccessResult -> {
                    try {
                        val bitmap = result.image.toBitmap().copyIfNeeded()
                        putCachedBitmap(uriString, bitmap)
                        try {
                            val palette = PlayerColorExtractor.extractFromBitmap(bitmap)
                            PlayerColorExtractor.putCachedPalette(uriString, palette)
                        } catch (_: Exception) {}
                        bitmap
                    } catch (e: Exception) {
                        Timber.tag("CoilBitmapLoader").w(e, "Failed to convert image to bitmap")
                        createFallbackBitmap()
                    }
                }
                null -> createFallbackBitmap()
            }
        }
    }

    override fun loadBitmapFromMetadata(metadata: androidx.media3.common.MediaMetadata): ListenableFuture<Bitmap>? {
        if (metadata.artworkData != null) {
            return decodeBitmap(metadata.artworkData!!)
        }
        val artworkUri = metadata.artworkUri
            ?: metadata.extras?.getString("artwork_uri")?.let { Uri.parse(it) }
        if (artworkUri != null) {
            return loadBitmap(artworkUri)
        }
        return Futures.immediateFuture(createFallbackBitmap())
    }
}
