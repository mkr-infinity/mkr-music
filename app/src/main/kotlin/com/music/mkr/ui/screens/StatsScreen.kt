/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.StatPeriod
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
import com.music.mkr.ui.component.LocalSongsGrid
import com.music.mkr.ui.component.NavigationTitle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.mkr.db.entities.Artist
import com.music.mkr.db.entities.SongWithStats
import com.music.mkr.ui.menu.AlbumMenu
import com.music.mkr.ui.menu.ArtistMenu
import com.music.mkr.ui.menu.SongMenu
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.utils.joinByBullet
import com.music.mkr.utils.makeTimeString
import com.music.mkr.viewmodels.StatsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                    uniqueSongsCount = uniqueSongsCount,
                    uniqueArtistsCount = uniqueArtistsCount,
                    uniqueAlbumsCount = uniqueAlbumsCount,
                    modifier = Modifier.animateItem()
                )
            }

            item(key = "stats_highlights") {
                val topSong = mostPlayedSongsStats.firstOrNull()
                val topArtist = mostPlayedArtists.firstOrNull()
                StatsHighlightsSection(
                    topArtist = topArtist,
                    topSong = topSong,
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

            item(key = "mostPlayedSongs") {
                NavigationTitle(
                    title = "${mostPlayedSongsStats.size} ${stringResource(id = R.string.songs)}",
                    modifier = Modifier.animateItem(),
                )

                LazyRow(
                    modifier = Modifier.animateItem(),
                ) {
                    itemsIndexed(
                        items = mostPlayedSongsStats,
                        key = { _, song -> song.id },
                    ) { index, song ->
                        LocalSongsGrid(
                            title = "${index + 1}. ${song.title}",
                            subtitle =
                            joinByBullet(
                                pluralStringResource(
                                    R.plurals.n_time,
                                    song.songCountListened,
                                    song.songCountListened,
                                ),
                                makeTimeString(song.timeListened),
                            ),
                            thumbnailUrl = song.thumbnailUrl,
                            isActive = song.id == mediaMetadata?.id,
                            isPlaying = isPlaying,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (song.id == mediaMetadata?.id) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    endpoint = WatchEndpoint(song.id),
                                                    preloadItem = mostPlayedSongs[index].toMediaMetadata(),
                                                ),
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            SongMenu(
                                                originalSong = mostPlayedSongs[index],
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

            item(key = "mostPlayedAlbums") {
                NavigationTitle(
                    title = "${mostPlayedAlbums.size} ${stringResource(id = R.string.albums)}",
                    modifier = Modifier.animateItem(),
                )

                if (mostPlayedAlbums.isNotEmpty()) {
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

@Composable
private fun StatsSummarySection(
    totalPlayTime: Long,
    allTimePlayTime: Long,
    uniqueSongsCount: Int,
    uniqueArtistsCount: Int,
    uniqueAlbumsCount: Int,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.total_listening_time),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = makeTimeString(totalPlayTime) ?: "0m",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (allTimePlayTime > 0 && allTimePlayTime != totalPlayTime) {
                        Text(
                            text = "All-time: ${makeTimeString(allTimePlayTime)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    label = stringResource(R.string.songs),
                    value = uniqueSongsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = stringResource(R.string.artists),
                    value = uniqueArtistsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = stringResource(R.string.albums),
                    value = uniqueAlbumsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsHighlightsSection(
    topArtist: Artist?,
    topSong: SongWithStats?,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (topArtist == null && topSong == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        topArtist?.let { artist ->
            StatsHighlightCard(
                title = "Top Artist",
                mainText = artist.artist.name,
                subText = "${artist.songCount} ${stringResource(R.string.songs).lowercase()} • ${makeTimeString(artist.timeListened?.toLong())}",
                imageUrl = artist.artist.thumbnailUrl,
                useCircleShape = true,
                onClick = { navController.navigate("artist/${artist.id}") }
            )
        }
        topSong?.let { song ->
            StatsHighlightCard(
                title = "Top Song",
                mainText = song.title,
                subText = "${pluralStringResource(R.plurals.n_time, song.songCountListened, song.songCountListened)} • ${makeTimeString(song.timeListened)}",
                imageUrl = song.thumbnailUrl,
                useCircleShape = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun StatsHighlightCard(
    title: String,
    mainText: String,
    subText: String,
    imageUrl: String?,
    useCircleShape: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                    .size(54.dp)
                    .clip(if (useCircleShape) CircleShape else RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
