package com.music.mkr.ui.screens.playlist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.mkr.R
import com.music.mkr.utils.makeTimeString

@Composable
internal fun CollectionHeader(
    title: String,
    songCount: Int,
    durationSeconds: Long?,
    artwork: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        BoxWithConstraints(
            Modifier.widthIn(max = 920.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            val wide = maxWidth >= 560.dp
            val padding = if (wide) 20.dp else 12.dp
            val artworkFraction = (if (wide) 0.29f else 0.31f) / fontScale.coerceAtMost(1.6f)
            val artworkSize = (maxWidth - padding * 2).coerceAtLeast(0.dp) * artworkFraction
            Row(
                modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(padding),
                horizontalArrangement = Arrangement.spacedBy(if (wide) 24.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(artworkSize.coerceAtMost(if (wide) 224.dp else 126.dp))
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                    content = artwork,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        title,
                        style = if (maxWidth >= 560.dp * fontScale) MaterialTheme.typography.headlineLarge
                            else MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        pluralStringResource(R.plurals.n_song, songCount, songCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (durationSeconds != null && durationSeconds > 0) {
                        Text(
                            makeTimeString(durationSeconds * 1000L),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

internal inline fun <T> List<T>.collectionDurationSeconds(duration: (T) -> Int?): Long? {
    var total = 0L
    for (song in this) {
        val seconds = duration(song)?.takeIf { it > 0 } ?: return null
        total += seconds
    }
    return total
}

@Composable
internal fun CollectionCover(thumbnails: List<String>, @DrawableRes fallback: Int) {
    when {
        thumbnails.size >= 4 -> Box(Modifier.fillMaxSize()) {
            val corners = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
            corners.forEachIndexed { index, corner ->
                AsyncImage(thumbnails[index], null, Modifier.align(corner).fillMaxSize(0.5f), contentScale = ContentScale.Crop)
            }
        }
        thumbnails.isNotEmpty() -> AsyncImage(thumbnails.first(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else -> Icon(painterResource(fallback), contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
    }
}
