/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.valentinilk.shimmer.shimmer
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalDownloadUtil
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.UseExpressiveAlbumDesignKey
import com.music.mkr.utils.rememberPreference
import com.music.mkr.db.entities.Album
import com.music.mkr.extensions.togglePlayPause
import com.music.mkr.playback.queues.LocalAlbumRadio
import com.music.mkr.ui.component.EmptyPlaceholder
import androidx.compose.material3.IconButton
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MkrAmbientBackdrop
import com.music.mkr.ui.component.MkrBadge
import com.music.mkr.ui.component.NavigationTitle
import com.music.mkr.ui.component.PlayingIndicator
import com.music.mkr.ui.component.SongListItem
import com.music.mkr.ui.component.YouTubeGridItem
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.ui.component.shimmer.ButtonPlaceholder
import com.music.mkr.ui.component.shimmer.ListItemPlaceHolder
import com.music.mkr.ui.component.shimmer.ShimmerHost
import com.music.mkr.ui.component.shimmer.TextPlaceholder
import com.music.mkr.ui.menu.AlbumMenu
import com.music.mkr.ui.menu.SelectionSongMenu
import com.music.mkr.ui.menu.SongMenu
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.theme.LocalPlayerArtworkPalette
import com.music.mkr.ui.utils.HeaderDownloadItem
import com.music.mkr.ui.utils.HeaderDownloadProgressIndicator
import com.music.mkr.ui.utils.HeaderDownloadState
import com.music.mkr.ui.utils.ItemWrapper
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.headerDownloadState
import com.music.mkr.ui.utils.resize
import com.music.mkr.ui.utils.sendAddMissingDownloads
import com.music.mkr.ui.utils.sendRemoveDownloads
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.AlbumUiState
import com.music.mkr.viewmodels.AlbumViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val artworkPalette = LocalPlayerArtworkPalette.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val useExpressiveAlbumDesign by rememberPreference(key = UseExpressiveAlbumDesignKey, defaultValue = true)

    val systemBarsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val accentColor = artworkPalette?.primaryAccent ?: MaterialTheme.colorScheme.primary

    val wrappedSongs = remember(albumWithSongs, hideExplicit) {
        val filteredSongs = if (hideExplicit) {
            albumWithSongs?.songs?.filter { !it.song.explicit } ?: emptyList()
        } else {
            albumWithSongs?.songs ?: emptyList()
        }
        filteredSongs.map { item -> ItemWrapper(item) }.toMutableStateList()
    }

    var selection by remember { mutableStateOf(false) }

    if (selection) {
        BackHandler {
            selection = false
            wrappedSongs.forEach { it.isSelected = false }
        }
    }

    val downloadUtil = LocalDownloadUtil.current
    var downloads by remember { mutableStateOf<Map<String, Download>>(emptyMap()) }
    var downloadState by remember { mutableStateOf<HeaderDownloadState>(HeaderDownloadState.None) }
    val globalDownloadState = remember(downloads) {
        val activeDownloads = downloads.values.filter {
            it.state == Download.STATE_DOWNLOADING ||
                    it.state == Download.STATE_QUEUED ||
                    it.state == Download.STATE_RESTARTING ||
                    it.state == Download.STATE_STOPPED
        }
        if (activeDownloads.isEmpty()) {
            HeaderDownloadState.None
        } else {
            var progressTotal = 0f
            var hasRunning = false
            var hasPaused = false
            activeDownloads.forEach { download ->
                val progress = download.percentDownloaded.takeIf { it >= 0f }?.div(100f) ?: 0f
                progressTotal += progress.coerceIn(0f, 1f)
                if (download.state == Download.STATE_STOPPED) {
                    hasPaused = hasPaused || download.stopReason == 1
                } else {
                    hasRunning = true
                }
            }
            HeaderDownloadState.Partial(
                progress = progressTotal / activeDownloads.size,
                paused = hasPaused && !hasRunning,
            )
        }
    }

    LaunchedEffect(albumWithSongs) {
        val songIds = albumWithSongs?.songs?.filterNot { it.song.isLocal }?.map { it.id }.orEmpty()
        if (songIds.isEmpty()) {
            downloads = emptyMap()
            downloadState = HeaderDownloadState.None
            return@LaunchedEffect
        }
        downloadUtil.downloads.collect { currentDownloads ->
            downloads = currentDownloads
            downloadState = headerDownloadState(songIds, currentDownloads)
        }
    }

    val lazyListState = rememberLazyListState()

    val showTopBarTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 250
        }
    }

    val transparentAppBar by remember {
        derivedStateOf {
            !selection && !showTopBarTitle
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
    ) {
        // Subtle ambient atmospheric glow derived from dynamic colors
        if (useExpressiveAlbumDesign) {
            MkrAmbientBackdrop(accentColor = accentColor, height = 360.dp, alpha = 0.15f)
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 24.dp,
            ),
        ) {
            val currentAlbumWithSongs = albumWithSongs
            val hasSongs = currentAlbumWithSongs?.songs?.isNotEmpty() == true

            if (hasSongs && currentAlbumWithSongs != null) {
                item(key = "album_header") {
                    val isBookmarked = currentAlbumWithSongs.album.bookmarkedAt != null
                    val totalDuration = currentAlbumWithSongs.songs.sumOf { it.song.duration }

                    AlbumHeroHeader(
                        album = currentAlbumWithSongs.album,
                        artists = currentAlbumWithSongs.artists,
                        songsCount = wrappedSongs.size,
                        totalDurationSeconds = totalDuration,
                        isBookmarked = isBookmarked,
                        downloadState = downloadState,
                        globalDownloadState = globalDownloadState,
                        isPlaying = isPlaying && currentAlbumWithSongs.songs.any { it.id == mediaMetadata?.id },
                        onPlay = {
                            playerConnection.playQueue(LocalAlbumRadio(currentAlbumWithSongs))
                        },
                        onShuffle = {
                            playerConnection.playQueue(
                                LocalAlbumRadio(
                                    currentAlbumWithSongs.copy(
                                        songs = currentAlbumWithSongs.songs.shuffled()
                                    )
                                )
                            )
                        },
                        onToggleBookmark = {
                            database.query {
                                update(currentAlbumWithSongs.album.toggleLike())
                            }
                        },
                        onDownloadClick = {
                            if (!currentAlbumWithSongs.album.isLocal && currentAlbumWithSongs.songs.any { !it.song.isLocal }) {
                                when (downloadState) {
                                    HeaderDownloadState.Completed -> {
                                        sendRemoveDownloads(
                                            context = context,
                                            songIds = currentAlbumWithSongs.songs.map { it.id },
                                        )
                                    }
                                    is HeaderDownloadState.Partial -> {
                                        sendRemoveDownloads(
                                            context = context,
                                            songIds = currentAlbumWithSongs.songs.map { it.id },
                                            downloads = downloads,
                                        )
                                    }
                                    HeaderDownloadState.None -> {
                                        sendAddMissingDownloads(
                                            context = context,
                                            songs = currentAlbumWithSongs.songs.map {
                                                HeaderDownloadItem(id = it.id, title = it.song.title)
                                            },
                                            downloads = downloads,
                                        )
                                        navController.navigate("auto_playlist/downloaded?tab=progress")
                                    }
                                }
                            }
                        },
                        onDownloadProgressClick = {
                            navController.navigate("auto_playlist/downloaded?tab=progress")
                        },
                        onMoreClick = {
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = Album(
                                        currentAlbumWithSongs.album,
                                        currentAlbumWithSongs.artists,
                                    ),
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = systemBarsTopPadding + 16.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
                    )
                }

                // Section header
                item(key = "songs_header") {
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
                            text = pluralStringResource(R.plurals.n_song, wrappedSongs.size, wrappedSongs.size),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tracks list
                itemsIndexed(
                    items = wrappedSongs,
                    key = { _, songWrapper -> songWrapper.item.id },
                ) { index, songWrapper ->
                    val isSongActive = songWrapper.item.id == mediaMetadata?.id

                    SongListItem(
                        song = songWrapper.item,
                        albumIndex = index + 1,
                        isActive = isSongActive,
                        isPlaying = isPlaying,
                        showInLibraryIcon = true,
                        isSelected = songWrapper.isSelected && selection,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        SongMenu(
                                            originalSong = songWrapper.item,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (!selection) {
                                        if (isSongActive) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                LocalAlbumRadio(currentAlbumWithSongs, startIndex = index),
                                            )
                                        }
                                    } else {
                                        songWrapper.isSelected = !songWrapper.isSelected
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!selection) {
                                        selection = true
                                    }
                                    wrappedSongs.forEach { it.isSelected = false }
                                    songWrapper.isSelected = true
                                },
                            )
                    )
                }

                // Other versions carousel
                if (otherVersions.isNotEmpty()) {
                    item(key = "other_versions_header") {
                        NavigationTitle(
                            title = stringResource(R.string.other_versions),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    item(key = "other_versions_list") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = otherVersions.distinctBy { it.id },
                                key = { it.id },
                            ) { item ->
                                YouTubeGridItem(
                                    item = item,
                                    isActive = mediaMetadata?.album?.id == item.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = rememberCoroutineScope(),
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = { navController.navigate("album/${item.id}") },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeAlbumMenu(
                                                        albumItem = item,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            } else {
                when (val state = uiState) {
                    AlbumUiState.Loading,
                    AlbumUiState.Success -> {
                        item(key = "album_shimmer") {
                            AlbumShimmerSkeleton(systemBarsTopPadding = systemBarsTopPadding)
                        }
                    }
                    AlbumUiState.Empty -> {
                        item(key = "album_empty") {
                            AlbumStateCard(
                                iconRes = R.drawable.album,
                                title = stringResource(R.string.empty_album),
                                description = stringResource(R.string.empty_album_desc),
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(top = systemBarsTopPadding + 48.dp, start = 24.dp, end = 24.dp)
                            )
                        }
                    }
                    is AlbumUiState.Error -> {
                        item(key = "album_error") {
                            AlbumStateCard(
                                iconRes = R.drawable.error,
                                title = stringResource(
                                    if (state.isNotFound) R.string.album_not_found else R.string.error_unknown
                                ),
                                description = stringResource(
                                    if (state.isNotFound) R.string.album_not_found_desc else R.string.network_error
                                ),
                                onRetry = viewModel::retry,
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(top = systemBarsTopPadding + 48.dp, start = 24.dp, end = 24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Top App Bar
        val topAppBarColors = if (transparentAppBar) {
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

        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            colors = topAppBarColors,
            scrollBehavior = scrollBehavior,
            title = {
                if (selection) {
                    val count = wrappedSongs.count { it.isSelected }
                    Text(
                        text = pluralStringResource(R.plurals.n_song, count, count),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                } else if (showTopBarTitle) {
                    Text(
                        text = albumWithSongs?.album?.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            navigationIcon = {
                com.music.mkr.ui.component.IconButton(
                    onClick = {
                        if (selection) {
                            selection = false
                            wrappedSongs.forEach { it.isSelected = false }
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onLongClick = {
                        if (!selection) {
                            navController.backToMain()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            if (selection) R.drawable.close else R.drawable.arrow_back,
                        ),
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            actions = {
                if (selection) {
                    val count = wrappedSongs.count { it.isSelected }
                    androidx.compose.material3.Checkbox(
                        checked = count == wrappedSongs.size && wrappedSongs.isNotEmpty(),
                        onCheckedChange = { checked ->
                            wrappedSongs.forEach { it.isSelected = checked }
                        }
                    )

                    IconButton(
                        onClick = {
                            menuState.show {
                                SelectionSongMenu(
                                    songSelection = wrappedSongs
                                        .filter { it.isSelected }
                                        .map { it.item },
                                    onDismiss = menuState::dismiss,
                                    clearAction = {
                                        selection = false
                                        wrappedSongs.forEach { it.isSelected = false }
                                    },
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
                        )
                    }
                } else {
                    albumWithSongs?.let { currentAlbum ->
                        IconButton(
                            onClick = {
                                menuState.show {
                                    AlbumMenu(
                                        originalAlbum = Album(
                                            currentAlbum.album,
                                            currentAlbum.artists,
                                        ),
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more_options),
                            )
                        }
                    }
                }
            },
        )
    }
}

/**
 * Original MKR Music Album Hero Header with responsive dual-pane layout,
 * prominent artwork card, elegant metadata, and screen-specific button aesthetics.
 */
@Composable
private fun AlbumHeroHeader(
    album: com.music.mkr.db.entities.AlbumEntity,
    artists: List<com.music.mkr.db.entities.ArtistEntity>,
    songsCount: Int,
    totalDurationSeconds: Int,
    isBookmarked: Boolean,
    downloadState: HeaderDownloadState,
    globalDownloadState: HeaderDownloadState,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadProgressClick: () -> Unit,
    onMoreClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Artwork Card
                AlbumArtworkCard(
                    thumbnailUrl = album.thumbnailUrl,
                    modifier = Modifier
                        .size(220.dp)
                )

                // Info & Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AlbumMetadataContent(
                        album = album,
                        artists = artists,
                        songsCount = songsCount,
                        totalDurationSeconds = totalDurationSeconds,
                        onArtistClick = onArtistClick,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AlbumActionButtons(
                        isPlaying = isPlaying,
                        isBookmarked = isBookmarked,
                        downloadState = downloadState,
                        globalDownloadState = globalDownloadState,
                        onPlay = onPlay,
                        onShuffle = onShuffle,
                        onToggleBookmark = onToggleBookmark,
                        onDownloadClick = onDownloadClick,
                        onDownloadProgressClick = onDownloadProgressClick,
                        onMoreClick = onMoreClick,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Centered Artwork Card
                AlbumArtworkCard(
                    thumbnailUrl = album.thumbnailUrl,
                    modifier = Modifier
                        .size(210.dp)
                )

                // Title and Metadata
                AlbumMetadataContent(
                    album = album,
                    artists = artists,
                    songsCount = songsCount,
                    totalDurationSeconds = totalDurationSeconds,
                    onArtistClick = onArtistClick,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Album Specific Button Aesthetics
                AlbumActionButtons(
                    isPlaying = isPlaying,
                    isBookmarked = isBookmarked,
                    downloadState = downloadState,
                    globalDownloadState = globalDownloadState,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    onToggleBookmark = onToggleBookmark,
                    onDownloadClick = onDownloadClick,
                    onDownloadProgressClick = onDownloadProgressClick,
                    onMoreClick = onMoreClick,
                )
            }
        }
    }
}

@Composable
private fun AlbumArtworkCard(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl.resize(800, 800))
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
                    painter = painterResource(R.drawable.album),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AlbumMetadataContent(
    album: com.music.mkr.db.entities.AlbumEntity,
    artists: List<com.music.mkr.db.entities.ArtistEntity>,
    songsCount: Int,
    totalDurationSeconds: Int,
    onArtistClick: (String) -> Unit,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Album Title
        Text(
            text = album.title,
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

        // Clickable Artists Link
        if (artists.isNotEmpty()) {
            val artistAnnotatedString = remember(artists) {
                buildAnnotatedString {
                    artists.fastForEachIndexed { index, artist ->
                        withLink(
                            LinkAnnotation.Clickable(artist.id) {
                                onArtistClick(artist.id)
                            }
                        ) {
                            append(artist.name)
                        }
                        if (index != artists.lastIndex) {
                            append(", ")
                        }
                    }
                }
            }

            Text(
                text = artistAnnotatedString,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign
            )
        }

        // Metadata Pills Row (Year, Songs Count, Duration)
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            album.year?.let { year ->
                MkrBadge(
                    text = year.toString(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (songsCount > 0) {
                MkrBadge(
                    text = pluralStringResource(R.plurals.n_song, songsCount, songsCount),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalDurationSeconds > 0) {
                MkrBadge(
                    text = makeTimeString(totalDurationSeconds * 1000L),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Screen-specific button aesthetics for Album:
 * - Visually prominent, soft rounded primary Play pill button.
 * - Carefully styled tonal Shuffle button.
 * - Refined circular quick actions (Favorite/Bookmark, Download, Progress, Overflow).
 */
@Composable
private fun AlbumActionButtons(
    isPlaying: Boolean,
    isBookmarked: Boolean,
    downloadState: HeaderDownloadState,
    globalDownloadState: HeaderDownloadState,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadProgressClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary Action Row: Play & Shuffle Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prominent Play Button (Filled Primary Pill)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onPlay),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Shuffle Button (Tonal Rounded Pill)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onShuffle),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
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
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.shuffle),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Secondary Music Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bookmark / Add to Library
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onToggleBookmark),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = stringResource(if (isBookmarked) R.string.remove_from_library else R.string.add_to_library),
                        modifier = Modifier.size(20.dp),
                        tint = if (isBookmarked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Download Album Action
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onDownloadClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = downloadState) {
                        HeaderDownloadState.Completed -> {
                            Icon(
                                painter = painterResource(R.drawable.offline),
                                contentDescription = stringResource(R.string.remove_download),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        is HeaderDownloadState.Partial -> {
                            HeaderDownloadProgressIndicator(
                                progress = state.progress,
                                paused = state.paused,
                                icon = R.drawable.download,
                            )
                        }
                        HeaderDownloadState.None -> {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = stringResource(R.string.action_download),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Download Progress Queue Link
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onDownloadProgressClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val globalProgress = (globalDownloadState as? HeaderDownloadState.Partial)?.progress ?: 0f
                    val globalPaused = (globalDownloadState as? HeaderDownloadState.Partial)?.paused ?: false
                    HeaderDownloadProgressIndicator(
                        progress = globalProgress,
                        paused = globalPaused,
                        icon = R.drawable.list,
                    )
                }
            }

            // More Options
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .mkrPressFeedback()
                    .clickable(onClick = onMoreClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = stringResource(R.string.more_options),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Custom Shimmer Skeleton for Album Screen.
 */
@Composable
private fun AlbumShimmerSkeleton(
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
            // Album Artwork Placeholder
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .shimmer()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )

            // Title Placeholder
            TextPlaceholder(height = 26.dp, modifier = Modifier.fillMaxWidth(0.6f))
            TextPlaceholder(height = 16.dp, modifier = Modifier.fillMaxWidth(0.35f))

            // Action Buttons Placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(50.dp))
                ButtonPlaceholder(modifier = Modifier.weight(1f).height(50.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Song rows placeholder
            repeat(6) {
                ListItemPlaceHolder()
            }
        }
    }
}

/**
 * Clean Empty and Error Card for Album Screen.
 */
@Composable
private fun AlbumStateCard(
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
            shape = RoundedCornerShape(28.dp),
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
