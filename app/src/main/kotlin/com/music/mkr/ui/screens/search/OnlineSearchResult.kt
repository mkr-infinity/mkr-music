/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.YouTube
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.utils.YouTubeUrlParser
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.CropAlbumArtKey
import com.music.mkr.constants.PauseSearchHistoryKey
import com.music.mkr.constants.SearchListenHistoryKey
import com.music.mkr.constants.SearchSource
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.extensions.toMediaItem
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.mkrCombinedClickable
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.menu.YouTubeArtistMenu
import com.music.mkr.ui.menu.YouTubePlaylistMenu
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.OnlineSearchViewModel
import com.music.mkr.viewmodels.withoutTopSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun OnlineSearchResult(
    navController: NavController,
    viewModel: OnlineSearchViewModel = hiltViewModel(),
    pureBlack: Boolean = false,
) {
    val database = LocalDatabase.current
    val menu = LocalMenuState.current
    val connection = LocalPlayerConnection.current
    val haptic = LocalHapticFeedback.current
    val isPlaying by (connection?.isEffectivelyPlaying ?: remember { flowOf(false) }).collectAsStateWithLifecycle(initialValue = false)
    val mediaMetadata by (connection?.mediaMetadata ?: remember { flowOf(null) }).collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val grid = rememberLazyGridState()
    val pauseHistory by rememberPreference(PauseSearchHistoryKey, false)
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(viewModel.query, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(viewModel.query, TextRange(viewModel.query.length)))
    }
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val summary = viewModel.summaryPage
    val page = filter?.value?.let { viewModel.viewStateMap[it] }
    val featured = when (filter) {
        null -> viewModel.summaryMatch ?: viewModel.songsMatch
        FILTER_SONG -> viewModel.songsMatch ?: viewModel.summaryMatch
        else -> null
    }
    val sections = remember(summary, featured?.id) {
        summary?.summaries.orEmpty().mapNotNull { shelf ->
            val results = shelf.items.withoutTopSong(featured?.id).distinctBy { it::class to it.id }
            shelf.copy(items = results).takeIf { results.isNotEmpty() }
        }
    }
    val filtered = remember(page, featured?.id) { page?.items.orEmpty().withoutTopSong(featured?.id) }

    fun dismissInput() {
        searchActive = false
        focus.clearFocus()
        keyboard?.hide()
    }

    fun submit(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        dismissInput()
        if (text == viewModel.query) return
        when (val link = YouTubeUrlParser.parse(text)) {
            is YouTubeUrlParser.ParsedUrl.Video ->
                connection?.playQueue(YouTubeQueue(WatchEndpoint(videoId = link.id)))
            is YouTubeUrlParser.ParsedUrl.Artist -> navController.navigate("artist/${link.id}")
            null -> navController.navigate("search/${Uri.encode(text)}") {
                popUpTo("search/{query}") { inclusive = true }
                launchSingleTop = true
            }
        }
        if (!pauseHistory) {
            scope.launch(Dispatchers.IO) { database.query { insert(SearchHistory(query = text)) } }
        }
    }

    BackHandler(enabled = searchActive, onBack = ::dismissInput)

    LaunchedEffect(searchActive, filter, featured != null) {
        if (!searchActive && featured != null) grid.requestScrollToItem(0)
    }

    LaunchedEffect(grid, filter) {
        snapshotFlow {
            filter?.value?.let { viewModel.viewStateMap[it]?.continuation }?.takeIf {
                grid.layoutInfo.visibleItemsInfo.any { item -> item.key == "continuation" }
            }
        }.collect { continuation -> continuation?.let { viewModel.loadMore(filter, it) } }
    }

    val playFeatured = searchMatchPlayer(connection)

    fun playSong(song: SongItem, fromHero: Boolean) {
        val player = connection ?: run {
            SnackbarManager.show(R.string.error_playback_failed)
            return
        }
        val metadata = song.toMediaMetadata()
        val queue = YouTubeQueue(WatchEndpoint(videoId = song.id), metadata)
        val alreadySelected = song.id == player.mediaMetadata.value?.id
        try {
            if (fromHero) {
                if (!playFeatured(queue, song.id)) return
            } else if (alreadySelected) {
                player.togglePlayPause()
                return
            } else {
                player.playQueue(queue)
            }
        } catch (_: Exception) {
            SnackbarManager.show(R.string.error_playback_failed)
            return
        }
        if (!alreadySelected) {
            scope.launch(Dispatchers.IO) {
                database.query { insert(metadata) }
                context.dataStore.edit { prefs ->
                    val recent = prefs[SearchListenHistoryKey].orEmpty().split(',').filter { it.isNotBlank() && it != song.id }
                    prefs[SearchListenHistoryKey] = (listOf(song.id) + recent).take(9).joinToString(",")
                }
            }
        }
    }

    fun openMenu(item: YTItem) = menu.show {
        when (item) {
            is SongItem -> YouTubeSongMenu(item, navController, onDismiss = menu::dismiss)
            is AlbumItem -> YouTubeAlbumMenu(item, navController, onDismiss = menu::dismiss)
            is ArtistItem -> YouTubeArtistMenu(item, onDismiss = menu::dismiss)
            is PlaylistItem -> YouTubePlaylistMenu(item, coroutineScope = scope, onDismiss = menu::dismiss)
        }
    }

    val resultContent: @Composable (YTItem, Modifier) -> Unit = { item, modifier ->
        SearchResultRow(
            item = item,
            active = when (item) {
                is SongItem -> mediaMetadata?.id == item.id
                is AlbumItem -> mediaMetadata?.album?.id == item.id
                else -> false
            },
            playing = isPlaying,
            onClick = {
                when (item) {
                    is SongItem -> playSong(item, fromHero = false)
                    is AlbumItem -> navController.navigate("album/${item.id}")
                    is ArtistItem -> navController.navigate("artist/${item.id}")
                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                openMenu(item)
            },
            onMenu = { openMenu(item) },
            modifier = modifier,
        )
    }

    val categoryControls: @Composable () -> Unit = {
        SearchCategoryFilters(onlineFilters, filter) {
            if (filter != it) {
                viewModel.selectFilter(it)
                scope.launch { grid.scrollToItem(0) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background,
        topBar = {
            MusicSearchHeader(
                query = query,
                onQueryChange = { query = it },
                onSearch = ::submit,
                active = searchActive,
                onActiveChange = { searchActive = it },
                searchSource = SearchSource.ONLINE,
                onSearchSourceChange = null,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                showBackButton = true,
                onBackClick = { if (searchActive) dismissInput() else navController.popBackStack() },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()), contentAlignment = Alignment.TopCenter) {
            if (searchActive) {
                OnlineSearchScreen(
                    query = query.text,
                    onQueryChange = { query = it },
                    navController = navController,
                    onSearch = ::submit,
                    onDismiss = ::dismissInput,
                    pureBlack = pureBlack,
                )
            } else {
                val bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()
                BoxWithConstraints(Modifier.widthIn(max = 1080.dp).fillMaxSize()) {
                    val keepFiltersVisible = maxHeight >= 260.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
                    Column(Modifier.fillMaxSize()) {
                        if (keepFiltersVisible) Box(Modifier.padding(horizontal = 16.dp)) { categoryControls() }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(320.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)),
                            state = grid,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottom + 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!keepFiltersVisible) {
                                item(key = "categories", span = { GridItemSpan(maxLineSpan) }) { categoryControls() }
                            }
                            if (featured != null) {
                                item(key = "top_song_match", span = { GridItemSpan(maxLineSpan) }) {
                                    TopSongMatchCard(
                                        song = featured,
                                        modifier = Modifier.padding(top = if (keepFiltersVisible) 12.dp else 4.dp),
                                        onPlay = { playSong(featured, fromHero = true) },
                                        onPlayNext = { enqueueSearchMatch(connection, featured.toMediaItem()) },
                                        onMenu = { openMenu(featured) },
                                    )
                                }
                            }
                            if (filter == null) {
                                sections.forEachIndexed { index, shelf ->
                                    item(key = "section-$index", span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            shelf.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                                        )
                                    }
                                    items(shelf.items, key = { "section-$index/${it::class.simpleName}/${it.id}" }) {
                                        resultContent(it, Modifier.animateItem())
                                    }
                                }
                            } else {
                                items(filtered, key = { "filtered/${it::class.simpleName}/${it.id}" }) {
                                    resultContent(it, Modifier.animateItem())
                                }
                                page?.continuation?.let { continuation ->
                                    item(key = "continuation", span = { GridItemSpan(maxLineSpan) }) {
                                        SearchLoadStatus(viewModel.pageFailed(filter, continuation)) {
                                            viewModel.loadMore(filter, continuation, retry = true)
                                        }
                                    }
                                }
                            }

                            val pending = if (filter == null) summary == null else page == null
                            val sourceEmpty = if (filter == null) summary?.summaries?.isEmpty() == true
                                else page != null && page.items.isEmpty() && page.continuation == null
                            if (pending) {
                                item(key = "initial", span = { GridItemSpan(maxLineSpan) }) {
                                    SearchLoadStatus(viewModel.initialFailed(filter), viewModel::retryInitial)
                                }
                            } else if (sourceEmpty && featured == null) {
                                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                                    EmptyPlaceholder(R.drawable.search, stringResource(R.string.no_results_found),
                                        description = stringResource(R.string.search_no_match_hint))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val onlineFilters = listOf<Pair<YouTube.SearchFilter?, Int>>(
    null to R.string.filter_all,
    FILTER_SONG to R.string.filter_songs,
    FILTER_VIDEO to R.string.filter_videos,
    FILTER_ALBUM to R.string.filter_albums,
    FILTER_ARTIST to R.string.filter_artists,
    FILTER_COMMUNITY_PLAYLIST to R.string.filter_community_playlists,
    FILTER_FEATURED_PLAYLIST to R.string.filter_featured_playlists,
)

@Composable
internal fun <T> SearchCategoryFilters(labels: List<Pair<T, Int>>, selected: T, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(labels, key = { it.second }) { (filter, label) ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(label), style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

@Composable
internal fun SearchLoadStatus(failed: Boolean, onRetry: () -> Unit) {
    val status = stringResource(if (failed) R.string.search_load_failed else R.string.search_loading_results)
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 4.dp).heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!failed) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(status, Modifier.weight(1f, fill = false).semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium)
                if (failed) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: YTItem,
    active: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val artworkSize = if (item is AlbumItem) 56.dp else 48.dp
    val label = when (item) {
        is SongItem -> item.artists.joinToString { it.name }.ifBlank { item.album?.name.orEmpty() }
        is AlbumItem -> listOfNotNull(item.artists?.joinToString { it.name }, item.year?.toString()).joinToString(" · ")
        is ArtistItem -> item.subtext ?: stringResource(R.string.artist)
        is PlaylistItem -> listOfNotNull(item.author?.name, item.songCountText).joinToString(" · ")
    }
    val state = stringResource(if (playing) R.string.search_playing_result else R.string.search_current_result)
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = when {
            active -> colors.secondaryContainer
            item is AlbumItem -> colors.surfaceContainerLow
            else -> colors.surface
        },
        contentColor = if (active) colors.onSecondaryContainer else colors.onSurface,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).clip(MaterialTheme.shapes.large)
                    .mkrCombinedClickable(onClick, onLongClick)
                    .semantics {
                        if (active) {
                            selected = true
                            stateDescription = state
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    SearchArtwork(
                        item, Modifier.size(artworkSize),
                        if (item is ArtistItem) CircleShape else MaterialTheme.shapes.medium,
                        pixels = 160,
                    )
                    if (active && playing) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).size(24.dp),
                            color = colors.primaryContainer,
                            contentColor = colors.onPrimaryContainer,
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(painterResource(R.drawable.play), null, Modifier.size(14.dp))
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (label.isNotBlank() || item.explicit) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (item.explicit) Icon(painterResource(R.drawable.explicit), null, Modifier.size(13.dp))
                            if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.more_vert), stringResource(R.string.more_options))
            }
        }
    }
}

@Composable
internal fun SearchArtwork(item: YTItem, modifier: Modifier, shape: Shape, pixels: Int) {
    val (cropAlbumArt) = rememberPreference(CropAlbumArtKey, defaultValue = true)
    val placeholder = when (item) {
        is AlbumItem -> R.drawable.album
        is ArtistItem -> R.drawable.artist_screen
        is PlaylistItem -> R.drawable.playlist_play
        is SongItem -> R.drawable.music_note
    }
    val artwork = remember(item.thumbnail, pixels) { item.thumbnail?.takeIf { it.isNotBlank() }?.resize(pixels, pixels) }
    val fallback = painterResource(placeholder)
    Surface(modifier, shape = shape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
        if (artwork != null) {
            AsyncImage(
                model = artwork,
                contentDescription = null,
                placeholder = fallback,
                error = fallback,
                contentScale = if (item is ArtistItem || cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(fallback, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}
