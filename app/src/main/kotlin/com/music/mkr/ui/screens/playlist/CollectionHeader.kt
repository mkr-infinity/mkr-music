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
    durationSeconds: Int,
    artwork: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        BoxWithConstraints(
            Modifier.widthIn(max = 920.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val wide = maxWidth >= 440.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
            val artworkSize = if (wide) (maxWidth * 0.31f).coerceIn(132.dp, 200.dp)
                else (maxWidth * 0.63f).coerceAtMost(176.dp)
            val cover: @Composable () -> Unit = {
                Box(
                    modifier = Modifier.size(artworkSize).clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                    content = artwork,
                )
            }
            val details: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                    Text(
                        pluralStringResource(R.plurals.n_song, songCount, songCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (durationSeconds > 0) {
                        Text(
                            makeTimeString(durationSeconds * 1000L),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            val surface = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(18.dp)
            if (wide) {
                Row(surface, horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                    cover()
                    Box(Modifier.weight(1f)) { details() }
                }
            } else {
                Column(surface, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    cover()
                    details()
                }
            }
        }
    }
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
