/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.music.mkr.R
import com.music.mkr.constants.DataSaverKey
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.rememberPreference

private const val LibraryArtworkSize = 544
private const val ExpandedArtworkSize = 1200
private const val DecodeSizeStep = 128

internal data class ImmersiveArtworkSource(
    val url: String,
    val cacheOnly: Boolean = false,
)

internal fun immersiveArtworkSources(
    thumbnail: String?,
    itemArtwork: String?,
    displayPixels: Int,
    dataSaver: Boolean,
): List<ImmersiveArtworkSource> {
    val urls = listOfNotNull(thumbnail, itemArtwork).filter { it.isNotBlank() }.distinct()
    val (remote, local) = urls.partition {
        it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
    }
    val sourceSize = if (displayPixels > LibraryArtworkSize) ExpandedArtworkSize else LibraryArtworkSize
    return buildList {
        local.forEach { add(ImmersiveArtworkSource(it)) }
        for (url in remote) {
            val preferred = if (dataSaver || sourceSize == ExpandedArtworkSize) {
                url.resize(sourceSize, sourceSize, isDataSaverEnabled = dataSaver)
            } else {
                url
            }
            add(ImmersiveArtworkSource(preferred))
            if (!dataSaver && preferred.endsWith("/maxresdefault.jpg")) {
                val fallback = preferred.resize(LibraryArtworkSize, LibraryArtworkSize, isDataSaverEnabled = false)
                add(ImmersiveArtworkSource(fallback))
            }
            add(ImmersiveArtworkSource(url, cacheOnly = dataSaver && url != preferred))
        }
    }.distinctBy { it.url }
}

@Composable
internal fun ImmersiveArtwork(
    trackId: String,
    thumbnail: String?,
    itemArtwork: String?,
    animate: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val (dataSaver) = rememberPreference(DataSaverKey, defaultValue = false)
    val context = LocalContext.current
    val placeholder = painterResource(R.drawable.mkr_music_small_icon)

    BoxWithConstraints(modifier.background(backgroundColor)) {
        val density = LocalDensity.current
        val displayPixels = with(density) { minOf(maxWidth, maxHeight).roundToPx() }
            .coerceIn(1, ExpandedArtworkSize)
        val decodePixels = ((displayPixels + DecodeSizeStep - 1) / DecodeSizeStep * DecodeSizeStep)
            .coerceAtMost(ExpandedArtworkSize)
        val sources = remember(thumbnail, itemArtwork, displayPixels > LibraryArtworkSize, dataSaver) {
            immersiveArtworkSources(thumbnail, itemArtwork, displayPixels, dataSaver)
        }

        key(trackId, sources) {
            var sourceIndex by remember { mutableIntStateOf(0) }
            val source = sources.getOrNull(sourceIndex)
            val request = remember(context, source, decodePixels, animate) {
                ImageRequest.Builder(context)
                    .data(source?.url)
                    .size(decodePixels, decodePixels)
                    .scale(Scale.FIT)
                    .networkCachePolicy(if (source?.cacheOnly == true) CachePolicy.DISABLED else CachePolicy.ENABLED)
                    .crossfade(if (animate) 180 else 0)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                placeholder = placeholder,
                fallback = placeholder,
                error = placeholder,
                onError = {
                    if (source != null && sources.getOrNull(sourceIndex) == source && sourceIndex < sources.lastIndex) {
                        sourceIndex++
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
