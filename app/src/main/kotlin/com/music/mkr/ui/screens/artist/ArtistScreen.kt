/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.artist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.valentinilk.shimmer.shimmer
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalListenTogetherManager
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.ShowArtistDescriptionKey
import com.music.mkr.constants.ShowArtistSubscriberCountKey
import com.music.mkr.constants.ShowMonthlyListenersKey
import com.music.mkr.db.entities.ArtistEntity
import com.music.mkr.extensions.toMediaItem
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.ListQueue
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.AlbumGridItem
import com.music.mkr.ui.component.ExpandableText
import androidx.compose.material3.IconButton
import com.music.mkr.ui.component.LinkSegment
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MkrAmbientBackdrop
import com.music.mkr.ui.component.MkrBadge
import com.music.mkr.ui.component.NavigationTitle
import com.music.mkr.ui.component.PlayingIndicator
import com.music.mkr.ui.component.SongListItem
import com.music.mkr.ui.component.YouTubeGridItem
import com.music.mkr.ui.component.YouTubeListItem
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.ui.component.shimmer.ButtonPlaceholder
import com.music.mkr.ui.component.shimmer.ListItemPlaceHolder
import com.music.mkr.ui.component.shimmer.ShimmerHost
import com.music.mkr.ui.component.shimmer.TextPlaceholder
import com.music.mkr.ui.menu.AlbumMenu
import com.music.mkr.ui.menu.SongMenu
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.menu.YouTubeArtistMenu
import com.music.mkr.ui.menu.YouTubePlaylistMenu
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.ui.theme.LocalPlayerArtworkPalette
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.isScrollingUp
import com.music.mkr.ui.utils.resize
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.ArtistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val listenTogetherManager = LocalListenTogetherManager.current
    val isGuest = listenTogetherManager?.isInRoom == true && !listenTogetherManager.isHost
    val artworkPalette = LocalPlayerArtworkPalette.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsState()
    val librarySongs by viewModel.librarySongs.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val showArtistDescription by rememberPreference(key = ShowArtistDescriptionKey, defaultValue = true)
    val showArtistSubscriberCount by rememberPreference(key = ShowArtistSubscriberCountKey, defaultValue = true)
    val showMonthlyListeners by rememberPreference(key = ShowMonthlyListenersKey, defaultValue = true)

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLocal by rememberSaveable { mutableStateOf(false) }

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
            !showTopBarTitle
        }
    }

    LaunchedEffect(libraryArtist) {
        showLocal = libraryArtist?.artist?.isLocal == true
    }

    val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name ?: ""
    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
    val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // Soft atmospheric ambient glow
        MkrAmbientBackdrop(accentColor = accentColor, height = 360.dp, alpha = 0.16f)

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 24.dp
            ),
        ) {
            if (artistPage == null && !showLocal) {
                item(key = "artist_shimmer") {
                    ArtistShimmerSkeleton(systemBarsTopPadding = systemBarsTopPadding)
                }
            } else {
                item(key = "artist_hero_header") {
                    ArtistHeroHeader(
                        artistName = artistName,
                        thumbnailUrl = thumbnail,
                        subscriberCount = if (showArtistSubscriberCount) artistPage?.subscriberCountText else null,
                        monthlyListeners = if (showMonthlyListeners) artistPage?.monthlyListenerCount else null,
                        description = if (!showLocal && showArtistDescription) artistPage?.description else null,
                        descriptionRuns = if (!showLocal && showArtistDescription) artistPage?.descriptionRuns else null,
                        isSubscribed = isSubscribed,
                        hasRadio = !showLocal && !isGuest && artistPage?.artist?.radioEndpoint != null,
                        hasShuffle = !showLocal && !isGuest && artistPage?.artist?.shuffleEndpoint != null || (librarySongs.isNotEmpty() && !isGuest),
                        hasLocalOption = librarySongs.isNotEmpty() && libraryArtist?.artist?.isLocal != true,
                        showLocal = showLocal,
                        onSubscribeToggle = {
                            database.transaction {
                                val currentArtist = libraryArtist?.artist
                                if (currentArtist != null) {
                                    update(currentArtist.toggleLike())
                                } else {
                                    artistPage?.artist?.let {
                                        insert(
                                            ArtistEntity(
                                                id = it.id,
                                                name = it.title,
                                                channelId = it.channelId,
                                                thumbnailUrl = it.thumbnail,
                                            ).toggleLike()
                                        )
                                    }
                                }
                            }
                        },
                        onRadioClick = {
                            artistPage?.artist?.radioEndpoint?.let { radioEndpoint ->
                                playerConnection.playQueue(YouTubeQueue(radioEndpoint))
                            }
                        },
                        onShuffleClick = {
                            if (!showLocal) {
                                artistPage?.artist?.shuffleEndpoint?.let { shuffleEndpoint ->
                                    playerConnection.playQueue(YouTubeQueue(shuffleEndpoint))
                                }
                            } else {
                                val shuffledSongs = librarySongs.shuffled()
                                if (shuffledSongs.isNotEmpty()) {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = artistName.ifEmpty { "Artist" },
                                            items = shuffledSongs.map { it.toMediaItem() }
                                        )
                                    )
                                }
                            }
                        },
                        onToggleLocalView = {
                            showLocal = !showLocal
                            if (!showLocal && artistPage == null) {
                                viewModel.fetchArtistsFromYTM()
                            }
                        },
                        onShareClick = {
                            artistPage?.artist?.shareLink?.let { link ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Artist Link", link)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = systemBarsTopPadding + 16.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
                    )
                }

                if (showLocal) {
                    // Local Songs Section
                    if (librarySongs.isNotEmpty()) {
                        item(key = "local_songs_title") {
                            NavigationTitle(
                                title = stringResource(R.string.songs),
                                modifier = Modifier.padding(top = 8.dp),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/songs")
                                }
                            )
                        }

                        val filteredLibrarySongs = if (hideExplicit) {
                            librarySongs.filter { !it.song.explicit }
                        } else {
                            librarySongs
                        }

                        itemsIndexed(
                            items = filteredLibrarySongs,
                            key = { index, item -> "local_song_${item.id}_$index" }
                        ) { index, song ->
                            val artistsSubtitle = song.artists.joinToString { it.name }.ifEmpty { song.song.albumName ?: artistName }
                            ArtistTrackLeaderboardCard(
                                rank = index + 1,
                                title = song.song.title,
                                subtitle = artistsSubtitle,
                                thumbnailUrl = song.song.thumbnailUrl,
                                durationText = if (song.song.duration > 0) makeTimeString(song.song.duration.toLong() * 1000L) else null,
                                explicit = song.song.explicit,
                                isActive = song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onMenuClick = {
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (song.id == mediaMetadata?.id) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = artistName.ifEmpty { "Artist" },
                                                        items = librarySongs.map { it.toMediaItem() },
                                                        startIndex = index
                                                    )
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    )
                            )
                        }
                    }

                    // Local Albums Section
                    if (libraryAlbums.isNotEmpty()) {
                        item(key = "local_albums_title") {
                            NavigationTitle(
                                title = stringResource(R.string.albums),
                                modifier = Modifier.padding(top = 16.dp),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/albums")
                                }
                            )
                        }

                        item(key = "local_albums_list") {
                            val filteredLibraryAlbums = if (hideExplicit) {
                                libraryAlbums.filter { !it.album.explicit }
                            } else {
                                libraryAlbums
                            }

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = filteredLibraryAlbums,
                                    key = { "local_album_${it.id}" }
                                ) { album ->
                                    AlbumGridItem(
                                        album = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
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
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Online Artist Sections
                    artistPage?.sections?.fastForEach { section ->
                        if (section.items.isNotEmpty()) {
                            item(key = "section_${section.title}") {
                                NavigationTitle(
                                    title = section.title,
                                    modifier = Modifier.padding(top = 12.dp),
                                    onClick = section.moreEndpoint?.let {
                                        {
                                            navController.navigate(
                                                "artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}",
                                            )
                                        }
                                    },
                                )
                            }
                        }

                        if ((section.items.firstOrNull() as? SongItem)?.album != null) {
                            // Song list representation as leaderboard
                            val distinctSongs = section.items.distinctBy { it.id }

                            itemsIndexed(
                                items = distinctSongs,
                                key = { _, it -> "youtube_song_${it.id}" },
                            ) { index, songItem ->
                                val song = songItem as SongItem
                                val durationStr = song.duration?.let { dur ->
                                    makeTimeString(dur.toLong() * 1000L)
                                }
                                val subtitleText = listOfNotNull(
                                    song.album?.name,
                                    song.viewCountText
                                ).joinToString(" • ")

                                ArtistTrackLeaderboardCard(
                                    rank = index + 1,
                                    title = song.title,
                                    subtitle = subtitleText.ifEmpty { artistName },
                                    thumbnailUrl = song.thumbnail,
                                    durationText = durationStr,
                                    explicit = song.explicit,
                                    isActive = mediaMetadata?.id == song.id,
                                    isPlaying = isPlaying,
                                    onMenuClick = {
                                        menuState.show {
                                            YouTubeSongMenu(
                                                song = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .combinedClickable(
                                            onClick = {
                                                if (song.id == mediaMetadata?.id) {
                                                    playerConnection.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue(
                                                            WatchEndpoint(videoId = song.id),
                                                            song.toMediaMetadata()
                                                        ),
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeSongMenu(
                                                        song = song,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        )
                                )
                            }
                        } else {
                            // Horizontal grid for albums/singles/playlists
                            item(key = "section_grid_${section.title}") {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(
                                        items = section.items.distinctBy { it.id },
                                        key = { "youtube_item_${it.id}" },
                                    ) { item ->
                                        YouTubeGridItem(
                                            item = item,
                                            isActive = when (item) {
                                                is SongItem -> mediaMetadata?.id == item.id
                                                is AlbumItem -> mediaMetadata?.album?.id == item.id
                                                else -> false
                                            },
                                            isPlaying = isPlaying,
                                            coroutineScope = coroutineScope,
                                            thumbnailRatio = 1f,
                                            modifier = Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is SongItem ->
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue(
                                                                        WatchEndpoint(videoId = item.id),
                                                                        item.toMediaMetadata()
                                                                    ),
                                                                )

                                                            is AlbumItem -> navController.navigate("album/${item.id}")
                                                            is ArtistItem -> navController.navigate("artist/${item.id}")
                                                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
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
                                                    },
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button: Play All
        val isScrollingUp = lazyListState.isScrollingUp()
        val canPlayAll = !isGuest && (
                (showLocal && librarySongs.isNotEmpty()) ||
                        (!showLocal && artistPage?.sections?.any {
                            (it.items.firstOrNull() as? SongItem)?.album != null
                        } == true)
                )

        if (canPlayAll) {
            AnimatedVisibility(
                visible = isScrollingUp,
                enter = slideInVertically { it * 2 },
                exit = slideOutVertically { it * 2 },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(
                        LocalPlayerAwareWindowInsets.current
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    )
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (showLocal) {
                            if (librarySongs.isNotEmpty()) {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = artistName.ifEmpty { "Artist" },
                                        items = librarySongs.map { it.toMediaItem() }
                                    )
                                )
                            }
                        } else if (artistPage != null) {
                            val songSection = artistPage.sections.find { section ->
                                (section.items.firstOrNull() as? SongItem)?.album != null
                            }
                            val moreEndpoint = songSection?.moreEndpoint
                            if (moreEndpoint != null) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = YouTube.artistItems(moreEndpoint).getOrNull()
                                    withContext(Dispatchers.Main) {
                                        val songs = if (result != null && result.items.isNotEmpty()) {
                                            result.items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                                        } else {
                                            songSection.items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                                        }
                                        if (songs.isNotEmpty()) {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = artistPage.artist.title,
                                                    items = songs
                                                )
                                            )
                                        }
                                    }
                                }
                            } else if (songSection != null) {
                                val songs = songSection.items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                                if (songs.isNotEmpty()) {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = artistPage.artist.title,
                                            items = songs
                                        )
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.mkrPressFeedback()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = stringResource(R.string.play),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.play),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )

        // Top App Bar
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = {
                if (showTopBarTitle) {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                com.music.mkr.ui.component.IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.artistPage?.artist?.shareLink?.let { link ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Artist Link", link)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.link),
                        contentDescription = null,
                    )
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
                )
            }
        )
    }
}

/**
 * Original MKR Music Artist Hero Header:
 * - Profile-oriented aesthetic with expressive circular portrait.
 * - Distinctive Primary Artist Actions (Subscribe/Follow pill, Radio, Shuffle).
 * - Cute stat capsules for subscribers and monthly listeners.
 * - Responsive layout for both phones and tablets/wide screens.
 */
@Composable
private fun ArtistHeroHeader(
    artistName: String,
    thumbnailUrl: String?,
    subscriberCount: String?,
    monthlyListeners: String?,
    description: String?,
    descriptionRuns: List<com.music.innertube.models.Run>?,
    isSubscribed: Boolean,
    hasRadio: Boolean,
    hasShuffle: Boolean,
    hasLocalOption: Boolean,
    showLocal: Boolean,
    onSubscribeToggle: () -> Unit,
    onRadioClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onToggleLocalView: () -> Unit,
    onShareClick: () -> Unit,
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
                // Large Circular Avatar
                ArtistAvatar(
                    thumbnailUrl = thumbnailUrl,
                    modifier = Modifier.size(170.dp)
                )

                // Info & Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArtistInfoAndBadges(
                        artistName = artistName,
                        subscriberCount = subscriberCount,
                        monthlyListeners = monthlyListeners,
                        textAlign = TextAlign.Start
                    )

                    // Bio if available
                    if (!description.isNullOrEmpty() || !descriptionRuns.isNullOrEmpty()) {
                        ArtistBioBox(description = description, descriptionRuns = descriptionRuns)
                    }

                    // Button Aesthetics for Artist
                    ArtistActionButtons(
                        isSubscribed = isSubscribed,
                        hasRadio = hasRadio,
                        hasShuffle = hasShuffle,
                        hasLocalOption = hasLocalOption,
                        showLocal = showLocal,
                        onSubscribeToggle = onSubscribeToggle,
                        onRadioClick = onRadioClick,
                        onShuffleClick = onShuffleClick,
                        onToggleLocalView = onToggleLocalView,
                        onShareClick = onShareClick
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Centered Circular Avatar
                ArtistAvatar(
                    thumbnailUrl = thumbnailUrl,
                    modifier = Modifier.size(150.dp)
                )

                // Artist Name & Stat Badges
                ArtistInfoAndBadges(
                    artistName = artistName,
                    subscriberCount = subscriberCount,
                    monthlyListeners = monthlyListeners,
                    textAlign = TextAlign.Center
                )

                // Bio Box
                if (!description.isNullOrEmpty() || !descriptionRuns.isNullOrEmpty()) {
                    ArtistBioBox(description = description, descriptionRuns = descriptionRuns)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Artist Action Buttons
                ArtistActionButtons(
                    isSubscribed = isSubscribed,
                    hasRadio = hasRadio,
                    hasShuffle = hasShuffle,
                    hasLocalOption = hasLocalOption,
                    showLocal = showLocal,
                    onSubscribeToggle = onSubscribeToggle,
                    onRadioClick = onRadioClick,
                    onShuffleClick = onShuffleClick,
                    onToggleLocalView = onToggleLocalView,
                    onShareClick = onShareClick
                )
            }
        }
    }
}

@Composable
private fun ArtistAvatar(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
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
                    painter = painterResource(R.drawable.artist_screen),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ArtistInfoAndBadges(
    artistName: String,
    subscriberCount: String?,
    monthlyListeners: String?,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Text(
                text = "VERIFIED ARTIST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }

        Text(
            text = artistName,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign
        )

        // Stat Badges
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            subscriberCount?.let { subscribers ->
                MkrBadge(
                    text = "${subscribers.split(' ').firstOrNull() ?: ""} ${stringResource(R.string.subscribers)}",
                    iconRes = R.drawable.artist_screen,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            monthlyListeners?.let { listeners ->
                MkrBadge(
                    text = "${listeners.split(' ').firstOrNull() ?: ""} ${stringResource(R.string.monthly_listeners)}",
                    iconRes = R.drawable.graphic_eq,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun ArtistBioBox(
    description: String?,
    descriptionRuns: List<com.music.innertube.models.Run>?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.about_artist),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            ExpandableText(
                text = description.orEmpty(),
                runs = descriptionRuns?.map {
                    LinkSegment(
                        text = it.text,
                        url = it.navigationEndpoint?.urlEndpoint?.url
                    )
                },
                collapsedMaxLines = 3
            )
        }
    }
}

/**
 * Screen-specific button aesthetics for Artist:
 * - More expressive profile-oriented action buttons.
 * - Distinctive primary Follow/Subscribe pill button with toggle state.
 * - Attractive compact icon buttons for secondary controls.
 */
@Composable
private fun ArtistActionButtons(
    isSubscribed: Boolean,
    hasRadio: Boolean,
    hasShuffle: Boolean,
    hasLocalOption: Boolean,
    showLocal: Boolean,
    onSubscribeToggle: () -> Unit,
    onRadioClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onToggleLocalView: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subscribe / Follow Button (Distinctive Primary Pill)
            Surface(
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onSubscribeToggle),
                shape = RoundedCornerShape(24.dp),
                color = if (isSubscribed) {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (isSubscribed) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                border = if (isSubscribed) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                } else null,
                shadowElevation = if (isSubscribed) 0.dp else 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(if (isSubscribed) R.drawable.subscribed else R.drawable.subscribe),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(if (isSubscribed) R.string.subscribed else R.string.subscribe),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Radio Button
            if (hasRadio) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .mkrPressFeedback()
                        .clickable(onClick = onRadioClick),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.radio),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.radio),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            // Shuffle Button
            if (hasShuffle) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .mkrPressFeedback()
                        .clickable(onClick = onShuffleClick),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
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
            }
        }

        // Secondary Options Row (Local switch & Share)
        if (hasLocalOption) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .height(38.dp)
                        .mkrPressFeedback()
                        .clickable(onClick = onToggleLocalView),
                    shape = RoundedCornerShape(19.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(if (showLocal) R.drawable.language else R.drawable.library_music),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showLocal) stringResource(R.string.online) else stringResource(R.string.local),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shimmer skeleton for Artist Screen.
 */
@Composable
private fun ArtistShimmerSkeleton(
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
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .shimmer()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )

            // Name Placeholder
            TextPlaceholder(height = 28.dp, modifier = Modifier.fillMaxWidth(0.5f))
            TextPlaceholder(height = 16.dp, modifier = Modifier.fillMaxWidth(0.35f))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(48.dp))
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Placeholders
            repeat(5) {
                ListItemPlaceHolder()
            }
        }
    }
}

/**
 * Leaderboard-style Track Card for Artist's biggest/top songs:
 * - Dynamic Rank numbering: Podium badge styling (#1 gold amber, #2 silver slate, #3 warm bronze, #4+ subtle).
 * - Live animated PlayingIndicator when track is active.
 * - Squircle thumbnail with smooth corners.
 * - Bold title with active primary coloration.
 * - Explicit badge and metadata subtitle.
 * - Duration capsule and context menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArtistTrackLeaderboardCard(
    rank: Int,
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    durationText: String?,
    explicit: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .mkrPressFeedback(),
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
            // Rank Badge or Live Visualizer
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

            // Artwork Thumbnail (Squircle)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                if (!thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl.resize(120, 120))
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
                            painter = painterResource(R.drawable.music_note),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata: Title, Subtitle, Explicit badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    ),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (explicit) {
                        Icon(
                            painter = painterResource(R.drawable.explicit),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Duration capsule if present
            if (!durationText.isNullOrEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, end = 2.dp)
                ) {
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // More Options Menu Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
