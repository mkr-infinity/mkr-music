/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.utils

import com.music.mkr.constants.DataSaverKey
import com.music.mkr.utils.MkrPrefCache
import timber.log.Timber

/**
 * Resizes a Google CDN or YouTube thumbnail URL to the requested dimensions.
 * Uses domain-independent and parameter-based matching to maximize quality
 * and ensure fetching logic does not break if YouTube changes hostnames or paths.
 */
fun String.resize(
    width: Int? = null,
    height: Int? = null,
): String {
    // Detect Google CDN URLs (including googleusercontent.com, ggpht.com, or any domain using Google parameter format like '=w120')
    val isGoogleCdn = this.contains("googleusercontent.com") || 
                      this.contains("ggpht.com") || 
                      this.contains(Regex("=[wshd]\\d+"))

    // Detect YouTube video thumbnails (containing i.ytimg.com, img.youtube.com, or path segment /vi/)
    val isYtimg = this.contains("ytimg") || this.contains("youtube.com") || this.contains("/vi/")

    val isDataSaverEnabled = MkrPrefCache.get(DataSaverKey) == true

    return when {
        isGoogleCdn -> resizeGoogleCdn(width, height, isDataSaverEnabled)
        isYtimg -> resizeYtimg(width, height, isDataSaverEnabled)
        else -> this
    }
}

/**
 * Rewrites a Google CDN URL to request high resolution dimensions.
 * Uses generic regex to locate parameters, ensuring robustness.
 */
private fun String.resizeGoogleCdn(width: Int?, height: Int?, isDataSaverEnabled: Boolean): String {
    if (isDataSaverEnabled) {
        val req = maxOf(width ?: 544, height ?: 544)
        val maxDim = when {
            req >= 600 -> 300
            req > 200 -> 200
            else -> 120
        }
        val (w, h) = if (width != null && height != null && width > 0 && height > 0) {
            val scale = maxDim.toFloat() / maxOf(width, height)
            val scaledW = (width * scale).toInt().coerceAtLeast(60)
            val scaledH = (height * scale).toInt().coerceAtLeast(60)
            scaledW to scaledH
        } else {
            maxDim to maxDim
        }

        if (this.contains(Regex("w\\d+-h\\d+"))) {
            return this.replace(Regex("w\\d+-h\\d+"), "w$w-h$h")
        }
        if (this.contains(Regex("=s\\d+"))) {
            return this.replace(Regex("=s\\d+"), if (w == h) "=s$w" else "=w$w-h$h")
        }
        val baseUrl = this.split(Regex("=[wshd]"), limit = 2)[0]
        return "$baseUrl=w$w-h$h-p-l80-rj"
    } else {
        // Data Saver disabled: normal uncompressed quality without lossy tags
        val w = (width ?: height ?: 1200).coerceAtLeast(544)
        val h = (height ?: width ?: 1200).coerceAtLeast(544)
        if (this.contains(Regex("w\\d+-h\\d+"))) {
            return this.replace(Regex("w\\d+-h\\d+"), "w$w-h$h")
        }
        if (this.contains(Regex("=s\\d+"))) {
            return this.replace(Regex("=s\\d+"), if (w == h) "=s$w" else "=w$w-h$h")
        }
        val baseUrl = this.split(Regex("=[wshd]"), limit = 2)[0]
        return "$baseUrl=w$w-h$h"
    }
}

/**
 * Rewrites a YouTube thumbnail URL to request the appropriate quality tier.
 * When Data Saver is ON, compresses and uses less data via compact WebP thumbnails.
 * When Data Saver is OFF, uses full standard uncompressed artwork (maxresdefault/hqdefault JPG).
 */
private fun String.resizeYtimg(width: Int?, height: Int?, isDataSaverEnabled: Boolean): String {
    val videoId = Regex("/vi(?:_webp)?/([^/]+)/").find(this)?.groupValues?.get(1)
    if (videoId == null) {
        Timber.w("[Thumbnail] resizeYtimg: could not extract videoId from: $this")
        return this
    }

    if (isDataSaverEnabled) {
        val w = width ?: height ?: 300
        val result = when {
            w >= 600 -> "https://i.ytimg.com/vi_webp/$videoId/mqdefault.webp"
            else     -> "https://i.ytimg.com/vi_webp/$videoId/default.webp"
        }
        Timber.d("[Thumbnail] resizeYtimg (dataSaver): id=$videoId w=$w → $result")
        return result
    }

    // Normal full quality (uncompressed JPG without forced downsampling)
    val w = width ?: height ?: 1200
    val result = when {
        w >= 800  -> "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"
        w >= 320  -> "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
        else      -> "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"
    }
    Timber.d("[Thumbnail] resizeYtimg (normal): id=$videoId w=$w → $result")
    return result
}


