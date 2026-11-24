/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.music.mkr.R
import com.music.mkr.constants.DataSaverKey
import com.music.mkr.constants.CropAlbumArtKey
import com.music.mkr.constants.HidePlayerThumbnailKey
import com.music.mkr.constants.PlayerThumbnailShadowElevationKey
import com.music.mkr.constants.RotatingThumbnailKey
import com.music.mkr.constants.ShowPlayerThumbnailShadowKey
import com.music.mkr.constants.ThumbnailCornerRadiusKey
import com.music.mkr.models.MediaMetadata
import com.music.mkr.ui.component.LocalPlaybackCookieAngle
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.rememberPreference

private const val LibraryArtworkSize = 544
private const val ExpandedArtworkSize = 1200
private const val DecodeSizeStep = 128

@Composable
internal fun FullPlayerArtwork(
    metadata: MediaMetadata,
    itemArtwork: String?,
    animate: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val hidden by rememberPreference(HidePlayerThumbnailKey, false)
    if (hidden) return

    val crop by rememberPreference(CropAlbumArtKey, false)
    val radius by rememberPreference(ThumbnailCornerRadiusKey, 3f)
    val showShadow by rememberPreference(ShowPlayerThumbnailShadowKey, false)
    val shadow by rememberPreference(PlayerThumbnailShadowElevationKey, 8f)
    val rotate by rememberPreference(RotatingThumbnailKey, false)
    val angle = LocalPlaybackCookieAngle.current
    Surface(
        modifier = modifier,
        shape = if (rotate) CircleShape else RoundedCornerShape(radius.coerceIn(0f, 45f).dp),
        color = backgroundColor,
        shadowElevation = if (showShadow) shadow.coerceAtLeast(0f).dp else 0.dp,
    ) {
        ImmersiveArtwork(
            trackId = metadata.id,
            thumbnail = metadata.thumbnailUrl,
            itemArtwork = itemArtwork,
            animate = animate,
            backgroundColor = Color.Transparent,
            contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
            contentDescription = metadata.title,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                rotationZ = if (rotate && animate) angle?.value ?: 0f else 0f
            },
        )
    }
}

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
            if (preferred.contains("maxresdefault.jpg")) {
                add(ImmersiveArtworkSource(preferred.replace("maxresdefault.jpg", "sddefault.jpg")))
                add(ImmersiveArtworkSource(preferred.replace("maxresdefault.jpg", "hqdefault.jpg")))
                add(ImmersiveArtworkSource(preferred.replace("maxresdefault.jpg", "mqdefault.jpg")))
            } else if (preferred.contains("sddefault.jpg")) {
                add(ImmersiveArtworkSource(preferred.replace("sddefault.jpg", "hqdefault.jpg")))
                add(ImmersiveArtworkSource(preferred.replace("sddefault.jpg", "mqdefault.jpg")))
            } else if (preferred.contains("hqdefault.jpg")) {
                add(ImmersiveArtworkSource(preferred.replace("hqdefault.jpg", "mqdefault.jpg")))
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
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    showPlaceholder: Boolean = true,
) {
    val (dataSaver) = rememberPreference(DataSaverKey, defaultValue = false)
    val context = LocalContext.current
    val placeholder = if (showPlaceholder) painterResource(R.drawable.mkr_music_small_icon) else null

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
            val request = remember(context, source, decodePixels, animate, contentScale) {
                ImageRequest.Builder(context)
                    .data(source?.url)
                    .size(decodePixels, decodePixels)
                    .scale(if (contentScale == ContentScale.Crop) Scale.FILL else Scale.FIT)
                    .networkCachePolicy(if (source?.cacheOnly == true) CachePolicy.DISABLED else CachePolicy.ENABLED)
                    .crossfade(if (animate) 180 else 0)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = contentScale,
                alignment = alignment,
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
