/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.compose.foundation.interaction.MutableInteractionSource
import com.music.mkr.ui.component.mkrClickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.db.entities.EventWithSong
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.ui.component.RecentSearchGridItem
import com.music.mkr.ui.component.mkrPressFeedback

/**
 * Curated discovery genres and mood categories for the Explore Grid.
 */
data class GenreCategory(
    val id: String,
    val title: String,
    val query: String,
    @DrawableRes val iconRes: Int
)

val DiscoveryGenreCategories = listOf(
    GenreCategory("pop", "Pop", "Pop", R.drawable.sparks),
    GenreCategory("hiphop", "Hip-Hop", "Hip-Hop", R.drawable.music_note),
    GenreCategory("rock", "Rock", "Rock", R.drawable.equalizer),
    GenreCategory("electronic", "Electronic", "Electronic Dance", R.drawable.radio),
    GenreCategory("lofi", "Chill & Lo-Fi", "Chill Lo-Fi", R.drawable.headset_applemusic),
    GenreCategory("rnb", "R&B & Soul", "R&B Soul", R.drawable.favorite),
    GenreCategory("indie", "Indie & Alt", "Indie Alternative", R.drawable.album),
    GenreCategory("workout", "Workout", "Workout Energy", R.drawable.trending_up),
    GenreCategory("focus", "Focus & Study", "Focus Study", R.drawable.bookmark_star_library),
    GenreCategory("acoustic", "Acoustic", "Acoustic Folk", R.drawable.artist)
)

/**
 * Expressive category exploration grid adapting columns to phone vs tablet widths.
 */
@Composable
fun ExploreGenresGrid(
    onGenreClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.search_explore_genres),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp).semantics { heading() },
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val minimum = 145.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
            val columns = ((maxWidth + 10.dp) / (minimum + 10.dp)).toInt().coerceIn(1, 4)
            val rows = DiscoveryGenreCategories.chunked(columns)

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { genre ->
                            GenreTileCard(
                                genre = genre,
                                onClick = { onGenreClick(genre.query) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun GenreTileCard(
    genre: GenreCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.mkrClickable(onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            Modifier.heightIn(min = 58.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(genre.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(genre.title, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/**
 * Compact, interactive recent search chips deck with instant re-search and deletion.
 */
@Composable
fun RecentSearchesSection(
    history: List<SearchHistory>,
    onQueryClick: (String) -> Unit,
    onDelete: (SearchHistory) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.history),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.search_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            TextButton(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.clear),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(history.take(12), key = { it.id }) { item ->
                val suggestionPressInteraction = remember { MutableInteractionSource() }
                Surface(
                    interactionSource = suggestionPressInteraction,
                    onClick = { onQueryClick(item.query) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                    modifier = Modifier.mkrPressFeedback(interactionSource = suggestionPressInteraction)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.history),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.query,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onDelete(item) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * "Jump Back In" horizontal carousel for recent listening history.
 */
@Composable
fun RecentListensSection(
    recentEvents: List<EventWithSong>,
    currentMediaId: String?,
    isPlaying: Boolean,
    onEventClick: (EventWithSong) -> Unit,
    onEventLongClick: (EventWithSong) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentEvents.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_listening_history_watch),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.search_listen_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(recentEvents, key = { index, it -> "${it.event.id}_$index" }) { _, event ->
                val isActive = event.song.song.id == currentMediaId
                RecentSearchGridItem(
                    event = event,
                    isActive = isActive,
                    isPlaying = isActive && isPlaying,
                    modifier = Modifier
                        .mkrClickable { onEventClick(event) }
                )
            }
        }
    }
}
