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
    val maxDim = if (isDataSaverEnabled) {
        val req = maxOf(width ?: 544, height ?: 544)
        when {
            req >= 600 -> 300
            req > 200 -> 200
            else -> 120
        }
    } else {
        maxOf(width ?: 1200, height ?: 1200).coerceAtLeast(544)
    }

    val (w, h) = if (width != null && height != null && width > 0 && height > 0) {
        val scale = maxDim.toFloat() / maxOf(width, height)
        val scaledW = (width * scale).toInt().coerceAtLeast(60)
        val scaledH = (height * scale).toInt().coerceAtLeast(60)
        scaledW to scaledH
    } else {
        maxDim to maxDim
    }

    // Handle wNNN-hNNN path segment style parameters
    if (this.contains(Regex("w\\d+-h\\d+"))) {
        return this.replace(Regex("w\\d+-h\\d+"), "w$w-h$h")
    }

    // Handle =sNNN style parameters
    if (this.contains(Regex("=s\\d+"))) {
        return this.replace(Regex("=s\\d+"), if (w == h) "=s$w" else "=w$w-h$h")
    }

    // Safely extract base URL by stripping any parameters like =w120, =s120, =h120, etc.
    val baseUrl = this.split(Regex("=[wshd]"), limit = 2)[0]
    val quality = if (isDataSaverEnabled) "l80" else "l90"
    return "$baseUrl=w$w-h$h-p-$quality-rj"
}

/**
 * Rewrites a YouTube thumbnail URL to request the highest **reliably available** quality tier.
 *
 * Quality ladder (highest → lowest, all guaranteed to exist for every video):
 *   w >= 1200 → maxresdefault.webp (1280×720, only for HD uploads — used ultra HD path)
 *   w >= 800  → hqdefault.webp    (480×360,  ALWAYS available — primary player quality)
 *   w >= 320  → mqdefault.webp    (320×180,  list items / data-saver hero cards)
 *   w <  320  → default.webp      (120×90,   small icons / data-saver list items)
 *
 * Note: maxresdefault.webp returns 404 for videos without a Full HD upload.
 * hqdefault.webp is guaranteed to exist for every YouTube video, so it is used
 * as the primary player thumbnail to avoid blank images in MiniPlayer / Player.
 */
private fun String.resizeYtimg(width: Int?, height: Int?, isDataSaverEnabled: Boolean): String {
    // Extract video ID using regex that matches any standard YouTube thumbnail path
    val videoId = Regex("/vi(?:_webp)?/([^/]+)/").find(this)?.groupValues?.get(1)
    if (videoId == null) {
        Timber.w("[Thumbnail] resizeYtimg: could not extract videoId from: $this")
        return this
    }

    val w = width ?: height ?: if (isDataSaverEnabled) 300 else 1200

    if (isDataSaverEnabled) {
        val result = when {
            w >= 600 -> "https://i.ytimg.com/vi_webp/$videoId/mqdefault.webp"
            else     -> "https://i.ytimg.com/vi_webp/$videoId/default.webp"
        }
        Timber.d("[Thumbnail] resizeYtimg (dataSaver): id=$videoId w=$w → $result")
        return result
    }

    val result = when {
        w >= 800  -> "https://i.ytimg.com/vi_webp/$videoId/hqdefault.webp"  // Always available for every video
        w >= 320  -> "https://i.ytimg.com/vi_webp/$videoId/mqdefault.webp"
        else      -> "https://i.ytimg.com/vi_webp/$videoId/default.webp"
    }
    Timber.d("[Thumbnail] resizeYtimg: id=$videoId w=$w → $result")
    return result
}


