/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.R
import com.music.mkr.db.entities.EventWithSong
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.ui.component.RecentSearchGridItem
import com.music.mkr.ui.component.mkrPressFeedback

/**
 * Curated discovery genres and mood categories for the Explore Grid.
 * Vibrant visual palette without emojis.
 */
data class GenreCategory(
    val id: String,
    val title: String,
    val query: String,
    val gradient: List<Color>,
    @DrawableRes val iconRes: Int
)

val DiscoveryGenreCategories = listOf(
    GenreCategory("pop", "Pop", "Pop", listOf(Color(0xFFEC4899), Color(0xFF9D174D)), R.drawable.sparks),
    GenreCategory("hiphop", "Hip-Hop", "Hip-Hop", listOf(Color(0xFFF59E0B), Color(0xFFB45309)), R.drawable.music_note),
    GenreCategory("rock", "Rock", "Rock", listOf(Color(0xFFEF4444), Color(0xFF991B1B)), R.drawable.equalizer),
    GenreCategory("electronic", "Electronic", "Electronic Dance", listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6)), R.drawable.radio),
    GenreCategory("lofi", "Chill & Lo-Fi", "Chill Lo-Fi", listOf(Color(0xFF06B6D4), Color(0xFF0E7490)), R.drawable.headset_applemusic),
    GenreCategory("rnb", "R&B & Soul", "R&B Soul", listOf(Color(0xFFA855F7), Color(0xFF6B21A8)), R.drawable.favorite),
    GenreCategory("indie", "Indie & Alt", "Indie Alternative", listOf(Color(0xFF10B981), Color(0xFF047857)), R.drawable.album),
    GenreCategory("workout", "Workout", "Workout Energy", listOf(Color(0xFFF97316), Color(0xFFC2410C)), R.drawable.trending_up),
    GenreCategory("focus", "Focus & Study", "Focus Study", listOf(Color(0xFF3B82F6), Color(0xFF1E40AF)), R.drawable.bookmark_star_library),
    GenreCategory("acoustic", "Acoustic", "Acoustic Folk", listOf(Color(0xFF84CC16), Color(0xFF3F6212)), R.drawable.artist)
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.discover_tune),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explore Genres & Vibes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = when {
                maxWidth > 800.dp -> 4
                maxWidth > 540.dp -> 3
                else -> 2
            }
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
                        // Fill extra spacer if last row has fewer columns
                        if (rowItems.size < columns) {
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
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
        modifier = modifier
            .height(68.dp)
            .mkrPressFeedback()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(genre.gradient))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Large semi-transparent watermark icon in background
            Icon(
                painter = painterResource(genre.iconRes),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.BottomEnd)
            )

            // Category title
            Text(
                text = genre.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.TopStart).padding(end = 24.dp)
            )
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
                Surface(
                    onClick = { onQueryClick(item.query) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                    modifier = Modifier.mkrPressFeedback()
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
            items(recentEvents, key = { it.song.song.id }) { event ->
                val isActive = event.song.song.id == currentMediaId
                RecentSearchGridItem(
                    event = event,
                    isActive = isActive,
                    isPlaying = isActive && isPlaying,
                    modifier = Modifier
                        .mkrPressFeedback()
                        .clickable { onEventClick(event) }
                )
            }
        }
    }
}
