/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.models.WatchEndpoint
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.StatPeriod
import com.music.mkr.db.entities.Album
import com.music.mkr.db.entities.Artist
import com.music.mkr.db.entities.SongWithStats
import com.music.mkr.extensions.toMediaItem
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.ListQueue
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.ChoiceChipsRow
import com.music.mkr.ui.component.HideOnScrollFAB
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.LocalAlbumsGrid
import com.music.mkr.ui.component.LocalArtistsGrid
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MkrBadge
import com.music.mkr.ui.component.NavigationTitle
import com.music.mkr.ui.component.PlayingIndicator
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.ui.menu.AlbumMenu
import com.music.mkr.ui.menu.ArtistMenu
import com.music.mkr.ui.menu.SongMenu
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.utils.joinByBullet
import com.music.mkr.utils.makeTimeString
import com.music.mkr.viewmodels.ListeningHabits
import com.music.mkr.viewmodels.StatsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current

    val indexChips by viewModel.indexChips.collectAsState()
    val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
    val mostPlayedSongsStats by viewModel.mostPlayedSongsStats.collectAsState()
    val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsState()
    val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsState()
    val firstEvent by viewModel.firstEvent.collectAsState()
    val currentDate = LocalDateTime.now()

    val totalPlayTime by viewModel.totalPlayTime.collectAsState()
    val allTimePlayTime by viewModel.allTimePlayTime.collectAsState()
    val uniqueSongsCount by viewModel.uniqueSongsCount.collectAsState()
    val uniqueArtistsCount by viewModel.uniqueArtistsCount.collectAsState()
    val uniqueAlbumsCount by viewModel.uniqueAlbumsCount.collectAsState()
    val totalPlaysCount by viewModel.totalPlaysCount.collectAsState()
    val listeningHabits by viewModel.listeningHabits.collectAsState()
    val maxPlays = remember(mostPlayedSongsStats) { mostPlayedSongsStats.maxOfOrNull { it.songCountListened } ?: 1 }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val selectedOption by viewModel.selectedOption.collectAsState()

    val weeklyDates =
        if (currentDate != null && firstEvent != null) {
            generateSequence(currentDate) { it.minusWeeks(1) }
                .takeWhile { it.isAfter(firstEvent?.event?.timestamp?.minusWeeks(1)) }
                .mapIndexed { index, date ->
                    val endDate = date.plusWeeks(1).minusDays(1).coerceAtMost(currentDate)
                    val formatter = DateTimeFormatter.ofPattern("dd MMM")

                    val startDateFormatted = formatter.format(date)
                    val endDateFormatted = formatter.format(endDate)

                    val startMonth = date.month
                    val endMonth = endDate.month
                    val startYear = date.year
                    val endYear = endDate.year

                    val text =
                        when {
                            startYear != currentDate.year -> "$startDateFormatted, $startYear - $endDateFormatted, $endYear"
                            startMonth != endMonth -> "$startDateFormatted - $endDateFormatted"
                            else -> "${date.dayOfMonth} - $endDateFormatted"
                        }
                    Pair(index, text)
                }.toList()
        } else {
            emptyList()
        }

    val monthlyDates =
        if (currentDate != null && firstEvent != null) {
            generateSequence(
                currentDate.plusMonths(1).withDayOfMonth(1).minusDays(1)
            ) { it.minusMonths(1) }
                .takeWhile {
                    it.isAfter(
                        firstEvent
                            ?.event
                            ?.timestamp
                            ?.withDayOfMonth(1),
                    )
                }.mapIndexed { index, date ->
                    val formatter = DateTimeFormatter.ofPattern("MMM")
                    val formattedDate = formatter.format(date)
                    val text =
                        if (date.year != currentDate.year) {
                            "$formattedDate ${date.year}"
                        } else {
                            formattedDate
                        }
                    Pair(index, text)
                }.toList()
        } else {
            emptyList()
        }

    val yearlyDates =
        if (currentDate != null && firstEvent != null) {
            generateSequence(
                currentDate
                    .plusYears(1)
                    .withDayOfYear(1)
                    .minusDays(1),
            ) { it.minusYears(1) }
                .takeWhile {
                    it.isAfter(
                        firstEvent
                            ?.event
                            ?.timestamp,
                    )
                }.mapIndexed { index, date ->
                    Pair(index, "${date.year}")
                }.toList()
        } else {
            emptyList()
        }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues(),
            modifier = Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        ) {
            item(key = "choice_chips") {
                ChoiceChipsRow(
                    chips =
                    when (selectedOption) {
                        OptionStats.WEEKS -> weeklyDates
                        OptionStats.MONTHS -> monthlyDates
                        OptionStats.YEARS -> yearlyDates
                        OptionStats.CONTINUOUS -> {
                            listOf(
                                StatPeriod.ALL.ordinal to stringResource(R.string.filter_all),
                                StatPeriod.WEEK_1.ordinal to pluralStringResource(
                                    R.plurals.n_week,
                                    1,
                                    1
                                ),
                                StatPeriod.MONTH_1.ordinal to pluralStringResource(
                                    R.plurals.n_month,
                                    1,
                                    1
                                ),
                                StatPeriod.MONTH_3.ordinal to pluralStringResource(
                                    R.plurals.n_month,
                                    3,
                                    3
                                ),
                                StatPeriod.MONTH_6.ordinal to pluralStringResource(
                                    R.plurals.n_month,
                                    6,
                                    6
                                ),
                                StatPeriod.YEAR_1.ordinal to pluralStringResource(
                                    R.plurals.n_year,
                                    1,
                                    1
                                ),
                            )
                        }
                    },
                    options =
                    listOf(
                        OptionStats.CONTINUOUS to stringResource(id = R.string.continuous),
                        OptionStats.WEEKS to stringResource(R.string.weeks),
                        OptionStats.MONTHS to stringResource(R.string.months),
                        OptionStats.YEARS to stringResource(R.string.years),
                    ),
                    selectedOption = selectedOption,
                    onSelectionChange = {
                        viewModel.selectedOption.value = it
                        viewModel.indexChips.value = if (it == OptionStats.CONTINUOUS) StatPeriod.ALL.ordinal else 0
                    },
                    currentValue = indexChips,
                    onValueUpdate = { viewModel.indexChips.value = it },
                )
            }

            item(key = "stats_summary") {
                StatsSummarySection(
                    totalPlayTime = totalPlayTime,
                    allTimePlayTime = allTimePlayTime,
                    totalPlaysCount = totalPlaysCount,
                    uniqueSongsCount = uniqueSongsCount,
                    uniqueArtistsCount = uniqueArtistsCount,
                    uniqueAlbumsCount = uniqueAlbumsCount,
                    modifier = Modifier.animateItem()
                )
            }

            if (listeningHabits.activeDays > 0) {
                item(key = "listening_habits") {
                    ListeningHabitsSection(
                        habits = listeningHabits,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item(key = "stats_highlights") {
                val topSong = mostPlayedSongsStats.firstOrNull()
                val topArtist = mostPlayedArtists.firstOrNull()
                val topAlbum = mostPlayedAlbums.firstOrNull()
                StatsHighlightsSection(
                    topArtist = topArtist,
                    topSong = topSong,
                    topAlbum = topAlbum,
                    isPlaying = isPlaying,
                    activeSongId = mediaMetadata?.id,
                    onPlayTopSong = {
                        topSong?.let { song ->
                            if (song.id == mediaMetadata?.id) {
                                playerConnection.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        endpoint = WatchEndpoint(song.id),
                                        preloadItem = mostPlayedSongs.firstOrNull()?.toMediaMetadata(),
                                    ),
                                )
                            }
                        }
                    },
                    navController = navController,
                    modifier = Modifier.animateItem()
                )
            }

            if (mostPlayedSongsStats.isEmpty() && totalPlayTime == 0L) {
                item(key = "stats_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.music_note),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = stringResource(R.string.keep_listening),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (mostPlayedSongsStats.isNotEmpty()) {
                item(key = "mostPlayedSongs_header") {
                    NavigationTitle(
                        title = "${mostPlayedSongsStats.size} ${stringResource(id = R.string.songs)}",
                        modifier = Modifier.animateItem(),
                    )
                }

                itemsIndexed(
                    items = mostPlayedSongsStats,
                    key = { _, song -> "stats_song_${song.id}" },
                ) { index, song ->
                    StatsRankedSongCard(
                        rank = index + 1,
                        song = song,
                        maxPlays = maxPlays,
                        isActive = song.id == mediaMetadata?.id,
                        isPlaying = isPlaying,
                        onClick = {
                            if (song.id == mediaMetadata?.id) {
                                playerConnection.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        endpoint = WatchEndpoint(song.id),
                                        preloadItem = mostPlayedSongs.getOrNull(index)?.toMediaMetadata(),
                                    ),
                                )
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            mostPlayedSongs.getOrNull(index)?.let { originalSong ->
                                menuState.show {
                                    SongMenu(
                                        originalSong = originalSong,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (mostPlayedArtists.isNotEmpty()) {
                item(key = "mostPlayedArtists") {
                    NavigationTitle(
                        title = "${mostPlayedArtists.size} ${stringResource(id = R.string.artists)}",
                        modifier = Modifier.animateItem(),
                    )

                    LazyRow(
                        modifier = Modifier.animateItem(),
                    ) {
                        itemsIndexed(
                            items = mostPlayedArtists,
                            key = { _, artist -> artist.id },
                        ) { index, artist ->
                            LocalArtistsGrid(
                                title = "${index + 1}. ${artist.artist.name}",
                                subtitle =
                                joinByBullet(
                                    pluralStringResource(
                                        R.plurals.n_time,
                                        artist.songCount,
                                        artist.songCount
                                    ),
                                    makeTimeString(artist.timeListened?.toLong()),
                                ),
                                thumbnailUrl = artist.artist.thumbnailUrl,
                                modifier =
                                Modifier
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("artist/${artist.id}")
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                ArtistMenu(
                                                    originalArtist = artist,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    )
                                    .animateItem(),
                            )
                        }
                    }
                }
            }

            if (mostPlayedAlbums.isNotEmpty()) {
                item(key = "mostPlayedAlbums") {
                    NavigationTitle(
                        title = "${mostPlayedAlbums.size} ${stringResource(id = R.string.albums)}",
                        modifier = Modifier.animateItem(),
                    )

                    LazyRow(
                        modifier = Modifier.animateItem(),
                    ) {
                        itemsIndexed(
                            items = mostPlayedAlbums,
                            key = { _, album -> album.id },
                        ) { index, album ->
                            LocalAlbumsGrid(
                                title = "${index + 1}. ${album.album.title}",
                                subtitle =
                                joinByBullet(
                                    pluralStringResource(
                                        R.plurals.n_time,
                                        album.songCountListened!!,
                                        album.songCountListened
                                    ),
                                    makeTimeString(album.timeListened),
                                ),
                                thumbnailUrl = album.album.thumbnailUrl,
                                isActive = album.id == mediaMetadata?.album?.id,
                                isPlaying = isPlaying,
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("album/${album.id}")
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                AlbumMenu(
                                                    originalAlbum = album,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    )
                                    .animateItem(),
                            )
                        }
                    }
                }
            }
        }

        // FAB to shuffle most played songs
        if (mostPlayedSongs.isNotEmpty()) {
            HideOnScrollFAB(
                visible = true,
                lazyListState = lazyListState,
                icon = R.drawable.shuffle,
                onClick = {
                    playerConnection.playQueue(
                        ListQueue(
                            title = context.getString(R.string.most_played_songs),
                            items = mostPlayedSongs.map { it.toMediaMetadata().toMediaItem() }.shuffled()
                        )
                    )
                }
            )
        }

        TopAppBar(
            title = { Text(stringResource(R.string.stats)) },
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain,
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { navController.navigate("settings/listening_summary") }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.history),
                        contentDescription = null,
                    )
                }
                IconButton(
                    onClick = { navController.navigate("wrapped") }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sparks),
                        contentDescription = null,
                    )
                }
            }
        )
    }
}

enum class OptionStats { WEEKS, MONTHS, YEARS, CONTINUOUS }

private fun formatDurationGracefully(millis: Long): String {
    if (millis <= 0) return "0m"
    val totalMinutes = millis / (1000 * 60)
    val totalHours = totalMinutes / 60
    val days = totalHours / 24
    val hours = totalHours % 24
    val minutes = totalMinutes % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

@Composable
private fun StatsSummarySection(
    totalPlayTime: Long,
    allTimePlayTime: Long,
    totalPlaysCount: Int,
    uniqueSongsCount: Int,
    uniqueArtistsCount: Int,
    uniqueAlbumsCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Total Time Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.timer),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.total_listening_time).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = formatDurationGracefully(totalPlayTime),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (allTimePlayTime > 0 && allTimePlayTime != totalPlayTime) {
                    Text(
                        text = "All-time listening: ${formatDurationGracefully(allTimePlayTime)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4 Stat Pill Capsules Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricPill(
                label = "Total Plays",
                value = totalPlaysCount.toString(),
                iconRes = R.drawable.play,
                modifier = Modifier.weight(1f)
            )
            StatMetricPill(
                label = stringResource(R.string.songs),
                value = uniqueSongsCount.toString(),
                iconRes = R.drawable.music_note,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricPill(
                label = stringResource(R.string.artists),
                value = uniqueArtistsCount.toString(),
                iconRes = R.drawable.artist_screen,
                modifier = Modifier.weight(1f)
            )
            StatMetricPill(
                label = stringResource(R.string.albums),
                value = uniqueAlbumsCount.toString(),
                iconRes = R.drawable.album,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatMetricPill(
    label: String,
    value: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ListeningHabitsSection(
    habits: ListeningHabits,
    modifier: Modifier = Modifier
) {
    if (habits.activeDays <= 0) return

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.trending_up),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Listening Patterns & Habits",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Streak & Active Days Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MkrBadge(
                    text = "${habits.activeDays} Active Days",
                    iconRes = R.drawable.ic_calendar_month,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (habits.currentStreak > 0) {
                    MkrBadge(
                        text = "${habits.currentStreak} Day Streak",
                        iconRes = R.drawable.sparks,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                if (habits.peakPeriod.isNotEmpty()) {
                    MkrBadge(
                        text = habits.peakPeriod.split(" ").firstOrNull() ?: habits.peakPeriod,
                        iconRes = R.drawable.bedtime,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Time of Day distribution Breakdown
            Text(
                text = "Time of Day Distribution",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeOfDayColumn(label = "Morning", percent = habits.morningPercent, modifier = Modifier.weight(1f))
                TimeOfDayColumn(label = "Afternoon", percent = habits.afternoonPercent, modifier = Modifier.weight(1f))
                TimeOfDayColumn(label = "Evening", percent = habits.eveningPercent, modifier = Modifier.weight(1f))
                TimeOfDayColumn(label = "Night", percent = habits.nightPercent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeOfDayColumn(
    label: String,
    percent: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val pctInt = (percent * 100).roundToInt()
        Text(
            text = "$pctInt%",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        LinearProgressIndicator(
            progress = { percent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsHighlightsSection(
    topArtist: Artist?,
    topSong: SongWithStats?,
    topAlbum: Album?,
    isPlaying: Boolean,
    activeSongId: String?,
    onPlayTopSong: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (topArtist == null && topSong == null && topAlbum == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        topSong?.let { song ->
            ElevatedCard(
                onClick = onPlayTopSong,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (activeSongId == song.id) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                PlayingIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOP SONG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artistName ?: ""} • ${pluralStringResource(R.plurals.n_time, song.songCountListened, song.songCountListened)} • ${formatDurationGracefully(song.timeListened ?: 0L)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onPlayTopSong,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (activeSongId == song.id && isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        topArtist?.let { artist ->
            ElevatedCard(
                onClick = { navController.navigate("artist/${artist.id}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = artist.artist.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOP ARTIST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = artist.artist.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${artist.songCount} ${stringResource(R.string.songs).lowercase()} • ${formatDurationGracefully(artist.timeListened?.toLong() ?: 0L)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.navigate_next),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        topAlbum?.let { album ->
            ElevatedCard(
                onClick = { navController.navigate("album/${album.id}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = album.album.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOP ALBUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = album.album.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.artists?.joinToString { it.name } ?: ""} • ${pluralStringResource(R.plurals.n_time, album.songCountListened ?: 0, album.songCountListened ?: 0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.navigate_next),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Ranked song card with podium badge styling (#1 gold, #2 silver, #3 bronze)
 * and dynamic listening proportion bar.
 */
@Composable
private fun StatsRankedSongCard(
    rank: Int,
    song: SongWithStats,
    maxPlays: Int,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (maxPlays > 0) (song.songCountListened.toFloat() / maxPlays).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .mkrPressFeedback()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge or Playing Indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        PlayingIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        when (rank) {
                            1 -> {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFB300).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.7f)),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "1",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color(0xFFFFB300)
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF90A4AE).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFFCFD8DC).copy(alpha = 0.7f)),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "2",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color(0xFFCFD8DC)
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFF7043).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFFFF8A65).copy(alpha = 0.7f)),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "3",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color(0xFFFF8A65)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = rank.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Artwork Thumbnail
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    AsyncImage(
                        model = song.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Artist
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        ),
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    song.artistName?.let { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Plays & Duration
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${song.songCountListened} plays",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (song.timeListened != null && song.timeListened > 0) {
                        Text(
                            text = makeTimeString(song.timeListened),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Listening share progress bar
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = if (rank == 1) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
            )
        }
    }
}
