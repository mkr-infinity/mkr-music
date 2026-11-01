/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.mkr.db.entities.Event
import java.time.LocalDateTime
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
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.MiniPlayerBottomSpacing
import com.music.mkr.constants.MiniPlayerHeight
import com.music.mkr.constants.NavigationBarHeight
import com.music.mkr.constants.PauseSearchHistoryKey
import com.music.mkr.constants.SearchSource
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.ChipsRow
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.NavigationTitle
import com.music.mkr.ui.component.YouTubeListItem
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.menu.YouTubeArtistMenu
import com.music.mkr.ui.menu.YouTubePlaylistMenu
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.OnlineSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.mkr.ui.component.PlayingIndicator
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.dataStore
import androidx.datastore.preferences.core.edit
import com.music.mkr.constants.SearchListenHistoryKey

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnlineSearchResult(
    navController: NavController,
    viewModel: OnlineSearchViewModel = hiltViewModel(),
    pureBlack: Boolean = false
) {
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current
    val haptic = LocalHapticFeedback.current
    val isPlaying by (playerConnection?.isEffectivelyPlaying ?: kotlinx.coroutines.flow.flowOf(false)).collectAsState(initial = false)
    val mediaMetadata by (playerConnection?.mediaMetadata ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)

    // Whether the search dock is in active suggestion mode
    var searchActive by rememberSaveable { mutableStateOf(false) }

    // Extract query from navigation arguments
    val encodedQuery = navController.currentBackStackEntry?.arguments?.getString("query") ?: ""
    val decodedQuery = remember(encodedQuery) {
        try {
            URLDecoder.decode(encodedQuery, "UTF-8")
        } catch (e: Exception) {
            encodedQuery
        }
    }

    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(decodedQuery, TextRange(decodedQuery.length)))
    }

    val onSearch: (String) -> Unit = remember {
        { searchQuery ->
            if (searchQuery.isNotEmpty()) {
                searchActive = false
                navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}") {
                    popUpTo("search/${URLEncoder.encode(decodedQuery, "UTF-8")}") {
                        inclusive = true
                    }

                    if (!pauseSearchHistory) {
                        coroutineScope.launch(Dispatchers.IO) {
                            database.query {
                                insert(SearchHistory(query = searchQuery))
                            }
                        }
                    }
                }
            }
        }
    }

    // Sync query text when navigating to a new search result
    LaunchedEffect(decodedQuery) {
        query = TextFieldValue(decodedQuery, TextRange(decodedQuery.length))
    }

    // Hardware back when suggestions overlay is open — dismiss it first
    BackHandler(enabled = searchActive) {
        searchActive = false
    }

    val searchFilter by viewModel.filter.collectAsState()
    val searchSummary = viewModel.summaryPage
    val itemsPage by remember(searchFilter) {
        derivedStateOf {
            searchFilter?.value?.let {
                viewModel.viewStateMap[it]
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val ytItemContent: @Composable LazyItemScope.(YTItem, Int, Int) -> Unit = { item: YTItem, index: Int, size: Int ->
        val longClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
                when (item) {
                    is SongItem ->
                        YouTubeSongMenu(
                            song = item,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )

                    is AlbumItem ->
                        YouTubeAlbumMenu(
                            albumItem = item,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )

                    is ArtistItem ->
                        YouTubeArtistMenu(
                            artist = item,
                            onDismiss = menuState::dismiss,
                        )

                    is PlaylistItem ->
                        YouTubePlaylistMenu(
                            playlist = item,
                            coroutineScope = coroutineScope,
                            onDismiss = menuState::dismiss,
                        )
                }
            }
        }

        SearchResultCard(
            item = item,
            isActive = when (item) {
                is SongItem -> mediaMetadata?.id == item.id
                is AlbumItem -> mediaMetadata?.album?.id == item.id
                else -> false
            },
            isPlaying = isPlaying,
            onClick = {
                when (item) {
                    is SongItem -> {
                        if (item.id == mediaMetadata?.id) {
                            playerConnection?.togglePlayPause()
                        } else {
                            playerConnection?.playQueue(
                                YouTubeQueue(
                                    WatchEndpoint(videoId = item.id),
                                    item.toMediaMetadata()
                                )
                            )
                            coroutineScope.launch(Dispatchers.IO) {
                                val metadata = item.toMediaMetadata()
                                database.query {
                                    insert(metadata)
                                }
                                context.dataStore.edit { prefs ->
                                    val current = prefs[SearchListenHistoryKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                                    val newList = (listOf(item.id) + current.filter { it != item.id }).take(9)
                                    prefs[SearchListenHistoryKey] = newList.joinToString(",")
                                }
                            }
                        }
                    }

                    is AlbumItem -> navController.navigate("album/${item.id}")
                    is ArtistItem -> navController.navigate("artist/${item.id}")
                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                }
            },
            onLongClick = longClick,
            onMenuClick = longClick,
            modifier = Modifier.animateItem()
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                MusicSearchHeader(
                    query = query,
                    onQueryChange = { newText ->
                        query = newText
                    },
                    onSearch = { searchQuery ->
                        onSearch(searchQuery)
                        searchActive = false
                    },
                    active = searchActive,
                    onActiveChange = { searchActive = it },
                    searchSource = SearchSource.ONLINE,
                    onSearchSourceChange = {},
                    pureBlack = pureBlack,
                    showBackButton = true,
                    onBackClick = {
                        if (searchActive) {
                            searchActive = false
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }
        },
        containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {
            if (searchActive) {
                OnlineSearchScreen(
                    query = query.text,
                    onQueryChange = { query = it },
                    navController = navController,
                    onSearch = { searchQuery ->
                        onSearch(searchQuery)
                        searchActive = false
                    },
                    onDismiss = { searchActive = false },
                    pureBlack = pureBlack
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    ChipsRow(
                chips = listOf(
                    null to stringResource(R.string.filter_all),
                    FILTER_SONG to stringResource(R.string.filter_songs),
                    FILTER_VIDEO to stringResource(R.string.filter_videos),
                    FILTER_ALBUM to stringResource(R.string.filter_albums),
                    FILTER_ARTIST to stringResource(R.string.filter_artists),
                    FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_community_playlists),
                    FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists),
                ),
                currentValue = searchFilter,
                onValueUpdate = {
                    if (viewModel.filter.value != it) {
                        viewModel.filter.value = it
                    }
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                state = lazyListState,
                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (searchFilter == null) {
                    searchSummary?.summaries?.forEach { summary ->
                        item {
                            NavigationTitle(summary.title)
                        }

                        itemsIndexed(
                            items = summary.items,
                            key = { index, item -> "${summary.title}/${item.id}/$index" },
                        ) { index, item ->
                            ytItemContent(item, index, summary.items.size)
                        }
                    }

                    if (searchSummary?.summaries?.isEmpty() == true) {
                        item {
                            EmptyPlaceholder(
                                icon = R.drawable.search,
                                text = stringResource(R.string.no_results_found),
                                description = "Check your query or try different artist or track names"
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = itemsPage?.items.orEmpty().distinctBy { it.id },
                        key = { _, it -> "filtered_${it.id}" },
                    ) { index, item ->
                        ytItemContent(item, index, itemsPage?.items.orEmpty().distinctBy { it.id }.size)
                    }

                    if (itemsPage?.continuation != null) {
                        item(key = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator()
                            }
                        }
                    }

                    if (itemsPage?.items?.isEmpty() == true) {
                        item {
                            EmptyPlaceholder(
                                icon = R.drawable.search,
                                text = stringResource(R.string.no_results_found),
                                description = "No items match the selected category filter"
                            )
                        }
                    }
                }

                if (searchFilter == null && searchSummary == null || searchFilter != null && itemsPage == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(MiniPlayerHeight + MiniPlayerBottomSpacing + NavigationBarHeight))
                }
            }
        }
    }
}
}
}

/**
 * Bespoke search result card providing immediate visual differentiation
 * across Songs, Albums, Artists, and Playlists.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultCard(
    item: YTItem,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArtist = item is ArtistItem
    val isAlbum = item is AlbumItem
    val isPlaylist = item is PlaylistItem
    val isSong = item is SongItem

    val subtitle = when (item) {
        is SongItem -> {
            val artistStr = item.artists.joinToString { it.name }.ifEmpty { item.album?.name ?: "" }
            val dur = item.duration?.let { makeTimeString(it * 1000L) }
            if (dur != null) "$artistStr • $dur" else artistStr
        }
        is AlbumItem -> {
            val artistStr = item.artists?.joinToString { it.name } ?: ""
            val yearStr = item.year?.toString()
            listOfNotNull(artistStr.takeIf { it.isNotBlank() }, yearStr).joinToString(" • ")
        }
        is ArtistItem -> item.subtext ?: stringResource(R.string.artist)
        is PlaylistItem -> {
            val authorStr = item.author?.name ?: ""
            val countStr = item.songCountText ?: ""
            listOfNotNull(authorStr.takeIf { it.isNotBlank() }, countStr.takeIf { it.isNotBlank() }).joinToString(" • ")
        }
    }

    val typeLabel = when (item) {
        is ArtistItem -> stringResource(R.string.artist)
        is AlbumItem -> stringResource(R.string.album_label)
        is PlaylistItem -> stringResource(R.string.filter_community_playlists)
        is SongItem -> stringResource(R.string.filter_songs)
    }

    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .mkrPressFeedback()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork: Circular for Artists, Squircle for Songs/Albums/Playlists
            val thumbnailShape = if (isArtist) CircleShape else RoundedCornerShape(12.dp)
            val thumbUrl = item.thumbnail
            Surface(
                modifier = Modifier.size(48.dp),
                shape = thumbnailShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (isArtist) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!thumbUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(thumbUrl.resize(120, 120))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    when {
                                        isArtist -> R.drawable.artist_screen
                                        isAlbum -> R.drawable.album
                                        isPlaylist -> R.drawable.playlist_play
                                        else -> R.drawable.music_note
                                    }
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (isActive && isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            PlayingIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    ),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Badge: "Artist", "Album", "Playlist", "Song"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isArtist -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            isAlbum -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            isPlaylist -> MaterialTheme.colorScheme.surfaceContainerHighest
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        }
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = when {
                                isArtist -> MaterialTheme.colorScheme.onTertiaryContainer
                                isAlbum -> MaterialTheme.colorScheme.onSecondaryContainer
                                isPlaylist -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }

                    if (item.explicit) {
                        Icon(
                            painter = painterResource(R.drawable.explicit),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // More Options Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = stringResource(R.string.more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
