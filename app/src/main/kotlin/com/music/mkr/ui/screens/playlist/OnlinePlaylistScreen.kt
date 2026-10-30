/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachReversed
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.db.entities.Playlist
import com.music.mkr.db.entities.PlaylistEntity
import com.music.mkr.db.entities.PlaylistSongMap
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.YouTubePlaylistQueue
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.ExpandableText
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MkrAmbientBackdrop
import com.music.mkr.ui.component.MkrBadge
import com.music.mkr.ui.component.NavigationTitle
import com.music.mkr.ui.component.YouTubeGridItem
import com.music.mkr.ui.component.YouTubeListItem
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.ui.component.shimmer.ButtonPlaceholder
import com.music.mkr.ui.component.shimmer.ListItemPlaceHolder
import com.music.mkr.ui.component.shimmer.ShimmerHost
import com.music.mkr.ui.component.shimmer.TextPlaceholder
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.menu.YouTubeArtistMenu
import com.music.mkr.ui.menu.YouTubePlaylistMenu
import com.music.mkr.ui.menu.YouTubeSelectionSongMenu
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.ui.theme.LocalPlayerArtworkPalette
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.OnlinePlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnlinePlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()
    val artworkPalette = LocalPlayerArtworkPalette.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val dbPlaylist by viewModel.dbPlaylist.collectAsState()
    val relatedItems by viewModel.relatedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()

    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    val filteredSongs = remember(songs, query) {
        if (query.text.isEmpty()) {
            songs.mapIndexed { i, s -> i to s }
        } else {
            songs.mapIndexed { i, s -> i to s }.filter {
                it.second.title.contains(query.text, true) ||
                        it.second.artists.fastAny { a -> a.name.contains(query.text, true) }
            }
        }
    }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }

    LaunchedEffect(filteredSongs) {
        selection.fastForEachReversed { songId ->
            if (filteredSongs.find { it.second.id == songId } == null) {
                selection.remove(songId)
            }
        }
    }

    val systemBarsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val accentColor = artworkPalette?.primaryAccent ?: MaterialTheme.colorScheme.primary

    val showTopBarTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 240
        }
    }

    val transparentAppBar by remember {
        derivedStateOf {
            !isSearching && !inSelectMode && !showTopBarTitle
        }
    }

    if (isSearching) {
        BackHandler {
            isSearching = false
            query = TextFieldValue()
        }
    } else if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // Subtle ambient atmospheric glow
        MkrAmbientBackdrop(accentColor = accentColor, height = 340.dp, alpha = 0.16f)

        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .union(WindowInsets.ime)
                .asPaddingValues(),
        ) {
            val currentPlaylist = playlist
            if (currentPlaylist == null || songs.isEmpty()) {
                if (isLoading) {
                    item(key = "loading_placeholder") {
                        OnlinePlaylistShimmerSkeleton(systemBarsTopPadding = systemBarsTopPadding)
                    }
                } else {
                    item(key = "error_placeholder") {
                        OnlinePlaylistStateCard(
                            iconRes = R.drawable.error,
                            title = error ?: stringResource(R.string.playlist_not_found),
                            description = stringResource(R.string.network_error),
                            onRetry = viewModel::retry,
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(top = systemBarsTopPadding + 48.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                }
            } else {
                if (!isSearching) {
                    item(key = "playlist_header") {
                        OnlinePlaylistHeroHeader(
                            playlist = currentPlaylist,
                            songs = songs,
                            dbPlaylist = dbPlaylist,
                            isPlaying = isPlaying && mediaMetadata?.album?.id == currentPlaylist.id,
                            onPlay = {
                                if (songs.isNotEmpty()) {
                                    val isRadio = currentPlaylist.id.startsWith("RD") || currentPlaylist.id.startsWith("VLRD")
                                    val queue = if (isRadio) {
                                        YouTubeQueue(
                                            endpoint = WatchEndpoint(
                                                videoId = songs.firstOrNull()?.id,
                                                playlistId = currentPlaylist.id
                                            ),
                                            preloadItem = songs.firstOrNull()?.toMediaMetadata()
                                        )
                                    } else {
                                        YouTubePlaylistQueue(
                                            playlistId = currentPlaylist.id,
                                            playlistTitle = currentPlaylist.title,
                                            initialSongs = songs,
                                            initialContinuation = viewModel.continuation
                                        )
                                    }
                                    playerConnection.playQueue(queue)
                                }
                            },
                            onShuffle = {
                                if (songs.isNotEmpty()) {
                                    val isRadio = currentPlaylist.id.startsWith("RD") || currentPlaylist.id.startsWith("VLRD")
                                    val queue = if (isRadio) {
                                        val randomSong = songs.randomOrNull()
                                        YouTubeQueue(
                                            endpoint = WatchEndpoint(
                                                videoId = randomSong?.id,
                                                playlistId = currentPlaylist.id
                                            ),
                                            preloadItem = randomSong?.toMediaMetadata()
                                        )
                                    } else {
                                        YouTubePlaylistQueue(
                                            playlistId = currentPlaylist.id,
                                            playlistTitle = currentPlaylist.title,
                                            initialSongs = songs.shuffled(),
                                            initialContinuation = viewModel.continuation
                                        )
                                    }
                                    playerConnection.playQueue(queue)
                                }
                            },
                            onToggleBookmark = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (dbPlaylist != null) {
                                        database.withTransaction {
                                            val savedPlaylist = dbPlaylist!!.playlist
                                            update(savedPlaylist, currentPlaylist)
                                            update(savedPlaylist.toggleLike())
                                        }
                                    } else {
                                        database.withTransaction {
                                            val playlistEntity = PlaylistEntity(
                                                name = currentPlaylist.title,
                                                browseId = currentPlaylist.id,
                                                thumbnailUrl = currentPlaylist.thumbnail,
                                                isEditable = currentPlaylist.isEditable,
                                                remoteSongCount = currentPlaylist.songCountText?.let {
                                                    Regex("""\d+""").find(it)?.value?.toIntOrNull()
                                                },
                                                playEndpointParams = currentPlaylist.playEndpoint?.params,
                                                shuffleEndpointParams = currentPlaylist.shuffleEndpoint?.params,
                                                radioEndpointParams = currentPlaylist.radioEndpoint?.params
                                            ).toggleLike()
                                            insert(playlistEntity)
                                            songs.map { it.toMediaMetadata() }
                                                .onEach { insert(it) }
                                                .mapIndexed { index, song ->
                                                    PlaylistSongMap(
                                                        songId = song.id,
                                                        playlistId = playlistEntity.id,
                                                        position = index,
                                                        setVideoId = song.setVideoId
                                                    )
                                                }
                                                .forEach { insert(it) }
                                        }
                                    }
                                }
                            },
                            onSearchTrigger = {
                                isSearching = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = systemBarsTopPadding + 16.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
                        )
                    }
                }

                // Section title for songs
                item(key = "songs_section_title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.songs),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        MkrBadge(
                            text = pluralStringResource(R.plurals.n_song, filteredSongs.size, filteredSongs.size),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Songs list
                itemsIndexed(
                    items = filteredSongs,
                    key = { _, (_, songItem) -> songItem.id }
                ) { index, (_, songItem) ->
                    val onCheckedChange: (Boolean) -> Unit = {
                        if (it) {
                            selection.add(songItem.id)
                        } else {
                            selection.remove(songItem.id)
                        }
                    }

                    YouTubeListItem(
                        item = songItem,
                        isActive = mediaMetadata?.id == songItem.id,
                        isPlaying = isPlaying,
                        isSelected = inSelectMode && songItem.id in selection,
                        albumIndex = index + 1,
                        shape = listItemShape(index, filteredSongs.size),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !hideExplicit || !songItem.explicit,
                                onClick = {
                                    if (inSelectMode) {
                                        onCheckedChange(songItem.id !in selection)
                                    } else if (songItem.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        val isRadio = currentPlaylist.id.startsWith("RD") || currentPlaylist.id.startsWith("VLRD")
                                        val queue = if (isRadio) {
                                            YouTubeQueue(
                                                endpoint = WatchEndpoint(
                                                    videoId = songItem.id,
                                                    playlistId = currentPlaylist.id
                                                ),
                                                preloadItem = songItem.toMediaMetadata()
                                            )
                                        } else {
                                            YouTubePlaylistQueue(
                                                playlistId = currentPlaylist.id,
                                                playlistTitle = currentPlaylist.title,
                                                initialSongs = filteredSongs.map { it.second },
                                                initialContinuation = viewModel.continuation,
                                                startIndex = index
                                            )
                                        }
                                        playerConnection.playQueue(queue)
                                    }
                                },
                                onLongClick = {
                                    if (!inSelectMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        inSelectMode = true
                                        onCheckedChange(true)
                                    }
                                }
                            ),
                        trailingContent = {
                            if (inSelectMode) {
                                Checkbox(
                                    checked = songItem.id in selection,
                                    onCheckedChange = onCheckedChange
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            YouTubeSongMenu(songItem, navController, menuState::dismiss)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                }

                // Loading More indicator
                if (isLoadingMore) {
                    item(key = "loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }

                // Related items / "You might also like"
                if (relatedItems.isNotEmpty() && !isSearching) {
                    item(key = "related_title") {
                        NavigationTitle(
                            title = stringResource(R.string.you_might_also_like),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    item(key = "related_items") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = relatedItems,
                                key = { it.id }
                            ) { item ->
                                YouTubeGridItem(
                                    item = item,
                                    modifier = Modifier
                                        .width(160.dp)
                                        .combinedClickable(
                                            onClick = {
                                                when (item) {
                                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                    is AlbumItem -> navController.navigate("album/${item.browseId}")
                                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                                    is SongItem -> playerConnection.playQueue(
                                                        YouTubeQueue(WatchEndpoint(videoId = item.id))
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    when (item) {
                                                        is PlaylistItem -> YouTubePlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                        is SongItem -> YouTubeSongMenu(
                                                            song = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                        is AlbumItem -> YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                        is ArtistItem -> YouTubeArtistMenu(
                                                            artist = item,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        // Top App Bar
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = {
                if (inSelectMode) {
                    Text(
                        text = pluralStringResource(R.plurals.n_song, selection.size, selection.size),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else if (isSearching) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                } else {
                    AnimatedContent(
                        targetState = showTopBarTitle,
                        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        label = "TopAppBarTitle"
                    ) { show ->
                        if (show) {
                            Text(
                                text = playlist?.title.orEmpty(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (isSearching) {
                            isSearching = false
                            query = TextFieldValue()
                        } else if (inSelectMode) {
                            onExitSelectionMode()
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onLongClick = {
                        if (!isSearching && !inSelectMode) {
                            navController.backToMain()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            if (isSearching || inSelectMode) R.drawable.close else R.drawable.arrow_back
                        ),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                if (inSelectMode) {
                    Checkbox(
                        checked = selection.size == filteredSongs.size && selection.isNotEmpty(),
                        onCheckedChange = {
                            if (selection.size == filteredSongs.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                selection.addAll(filteredSongs.map { it.second.id })
                            }
                        }
                    )
                    IconButton(
                        enabled = selection.isNotEmpty(),
                        onClick = {
                            menuState.show {
                                YouTubeSelectionSongMenu(
                                    songSelection = filteredSongs.filter { it.second.id in selection }
                                        .map { it.second },
                                    onDismiss = menuState::dismiss,
                                    clearAction = onExitSelectionMode
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null
                        )
                    }
                } else if (!isSearching) {
                    playlist?.let { currentPl ->
                        IconButton(
                            onClick = {
                                menuState.show {
                                    YouTubePlaylistMenu(
                                        playlist = currentPl,
                                        songs = songs,
                                        coroutineScope = coroutineScope,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                    }
                }
            },
            colors = if (transparentAppBar) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Original MKR Music Online Playlist Hero Header:
 * - Curated Playlist / Mixtape vibe.
 * - Elevated Squircle Artwork.
 * - Compact, modern playlist-oriented button aesthetics (Compact Play pill, Compact Shuffle pill,
 *   Squircle Favorite/Bookmark button, Squircle Search trigger).
 * - Responsive side-by-side on wide screens.
 */
@Composable
private fun OnlinePlaylistHeroHeader(
    playlist: PlaylistItem,
    songs: List<SongItem>,
    dbPlaylist: Playlist?,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSearchTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork Card
                PlaylistArtworkCard(
                    thumbnailUrl = playlist.thumbnail,
                    modifier = Modifier.size(200.dp)
                )

                // Info & Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlaylistMetadataContent(
                        playlist = playlist,
                        songs = songs,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OnlinePlaylistActionButtons(
                        isPlaying = isPlaying,
                        isBookmarked = dbPlaylist?.playlist?.bookmarkedAt != null,
                        onPlay = onPlay,
                        onShuffle = onShuffle,
                        onToggleBookmark = onToggleBookmark,
                        onSearchTrigger = onSearchTrigger
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Artwork Card
                PlaylistArtworkCard(
                    thumbnailUrl = playlist.thumbnail,
                    modifier = Modifier.size(180.dp)
                )

                // Info & Metadata
                PlaylistMetadataContent(
                    playlist = playlist,
                    songs = songs,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Online Playlist Button Aesthetics
                OnlinePlaylistActionButtons(
                    isPlaying = isPlaying,
                    isBookmarked = dbPlaylist?.playlist?.bookmarkedAt != null,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    onToggleBookmark = onToggleBookmark,
                    onSearchTrigger = onSearchTrigger
                )
            }
        }
    }
}

@Composable
private fun PlaylistArtworkCard(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl.resize(600, 600))
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
                    painter = painterResource(R.drawable.library_music),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PlaylistMetadataContent(
    playlist: PlaylistItem,
    songs: List<SongItem>,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    val totalDuration = remember(songs) { songs.sumOf { it.duration ?: 0 } }
    val hasExplicit = remember(songs) { songs.any { it.explicit } }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Title
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign
        )

        // Curator / Author
        playlist.author?.name?.let { authorName ->
            Text(
                text = authorName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign
            )
        }

        // Badges row: count, duration, explicit
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (songs.isNotEmpty()) {
                MkrBadge(
                    text = pluralStringResource(R.plurals.n_song, songs.size, songs.size),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalDuration > 0) {
                MkrBadge(
                    text = makeTimeString(totalDuration * 1000L),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (hasExplicit) {
                MkrBadge(
                    text = "Explicit",
                    iconRes = R.drawable.explicit,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Description if available
        val description = playlist.description
        if (!description.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    ExpandableText(
                        text = description,
                        collapsedMaxLines = 3,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Screen-specific button aesthetics for Online Playlist:
 * - Compact, modern playlist-oriented controls:
 *   - Play button: Compact, high-energy pill (height 46dp) with Play/Pause icon + label.
 *   - Shuffle button: Compact tonal pill (height 46dp) with Shuffle icon + label.
 *   - Save / Bookmark button: Compact squircle icon button (46dp x 46dp, radius 16dp).
 *   - Search in playlist button: Compact squircle icon button (46dp x 46dp, radius 16dp).
 */
@Composable
private fun OnlinePlaylistActionButtons(
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSearchTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play Button (Compact Primary Pill)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .mkrPressFeedback()
                .clickable(onClick = onPlay),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Shuffle Button (Compact Tonal Pill)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .mkrPressFeedback()
                .clickable(onClick = onShuffle),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = stringResource(R.string.shuffle),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.shuffle),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Save / Bookmark Squircle Button
        Surface(
            modifier = Modifier
                .size(46.dp)
                .mkrPressFeedback()
                .clickable(onClick = onToggleBookmark),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (isBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isBookmarked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Trigger Squircle Button
        Surface(
            modifier = Modifier
                .size(46.dp)
                .mkrPressFeedback()
                .clickable(onClick = onSearchTrigger),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = stringResource(R.string.search),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Shimmer skeleton for Online Playlist Screen.
 */
@Composable
private fun OnlinePlaylistShimmerSkeleton(
    systemBarsTopPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    ShimmerHost(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = systemBarsTopPadding + 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Artwork Placeholder
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .shimmer()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )

            // Title Placeholder
            TextPlaceholder(height = 26.dp, modifier = Modifier.fillMaxWidth(0.55f))
            TextPlaceholder(height = 16.dp, modifier = Modifier.fillMaxWidth(0.35f))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(46.dp))
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(46.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item Placeholders
            repeat(6) {
                ListItemPlaceHolder()
            }
        }
    }
}

/**
 * Clean State Card for Online Playlist Screen.
 */
@Composable
private fun OnlinePlaylistStateCard(
    @androidx.annotation.DrawableRes iconRes: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.mkrPressFeedback()
                    ) {
                        Text(
                            text = stringResource(R.string.retry),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
