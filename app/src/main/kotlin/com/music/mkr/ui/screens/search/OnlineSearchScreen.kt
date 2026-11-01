/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.SuggestionItemHeight
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MkrBadge
import com.music.mkr.ui.component.RecentSearchGridItem
import com.music.mkr.ui.component.YouTubeListItem
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.getGroupedShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.utils.YouTubeUrlParser
import com.music.mkr.ui.menu.YouTubeAlbumMenu
import com.music.mkr.ui.menu.YouTubeArtistMenu
import com.music.mkr.ui.menu.YouTubePlaylistMenu
import com.music.mkr.ui.menu.YouTubeSongMenu
import com.music.mkr.viewmodels.OnlineSearchSuggestionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.music.mkr.utils.dataStore
import androidx.datastore.preferences.core.edit
import com.music.mkr.constants.SearchListenHistoryKey
import com.music.mkr.db.entities.Event
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun OnlineSearchScreen(
    query: String,
    onQueryChange: (TextFieldValue) -> Unit,
    navController: NavController,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    pureBlack: Boolean,
    viewModel: OnlineSearchSuggestionViewModel = hiltViewModel(),
) {
    val database = LocalDatabase.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val haptic = LocalHapticFeedback.current
    val isPlaying by (playerConnection?.isEffectivelyPlaying ?: kotlinx.coroutines.flow.flowOf(false)).collectAsState(initial = false)
    val mediaMetadata by (playerConnection?.mediaMetadata ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)

    val coroutineScope = rememberCoroutineScope()
    val viewState by viewModel.viewState.collectAsState()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .drop(1)
            .collect {
                keyboardController?.hide()
            }
    }

    LaunchedEffect(query) {
        snapshotFlow { query }.collectLatest {
            if (YouTubeUrlParser.isYouTubeUrl(it)) {
                viewModel.query.value = it
            } else {
                kotlinx.coroutines.delay(300L)
                viewModel.query.value = it
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues(),
        modifier = Modifier
            .fillMaxSize()
            .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
    ) {
        if (viewState.recentEvents.isNotEmpty() && query.isEmpty()) {
            item(key = "recent_events_header") {
                Text(
                    text = stringResource(R.string.search_listen_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp).animateItem()
                )
            }
            item(key = "recent_events_row") {
                val lazyRowState = rememberLazyListState()
                LazyRow(
                    state = lazyRowState,
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    items(viewState.recentEvents, key = { it.song.song.id }) { event ->
                        RecentSearchGridItem(
                            event = event,
                            isActive = event.song.song.id == mediaMetadata?.id,
                            isPlaying = event.song.song.id == mediaMetadata?.id && isPlaying,
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (event.song.song.id == mediaMetadata?.id) {
                                        playerConnection?.togglePlayPause()
                                    } else {
                                        scope.launch(Dispatchers.IO) {
                                            val metadata = event.song.toMediaMetadata()
                                            database.query {
                                                insert(metadata)
                                            }
                                            context.dataStore.edit { prefs ->
                                                val current = prefs[SearchListenHistoryKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                                                val newList = (listOf(event.song.song.id) + current.filter { it != event.song.song.id }).take(9)
                                                prefs[SearchListenHistoryKey] = newList.joinToString(",")
                                            }
                                        }
                                        playerConnection?.playQueue(
                                            YouTubeQueue.radio(event.song.toMediaMetadata())
                                        )
                                        onDismiss()
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        YouTubeSongMenu(
                                            song = SongItem(
                                                id = event.song.song.id,
                                                title = event.song.song.title,
                                                artists = event.song.artists.map { Artist(id = it.id, name = it.name) },
                                                album = null,
                                                duration = event.song.song.duration,
                                                thumbnail = event.song.song.thumbnailUrl ?: "",
                                                explicit = event.song.song.explicit,
                                                endpoint = WatchEndpoint(videoId = event.song.song.id)                                                
                                            ),
                                            navController = navController,
                                            onDismiss = {
                                                menuState.dismiss()
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            )
                        )
                    }
                }
            }
        }

        if (viewState.history.isNotEmpty()) {
            item(key = "history_header") {
                Text(
                    text = stringResource(R.string.search_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp).animateItem()
                )
            }
        }

        itemsIndexed(viewState.history, key = { index, it -> "history_${it.id}_$index" }) { index, history ->
            SuggestionItem(
                query = history.query,
                online = false,
                shape = getGroupedShape(index, viewState.history.size),
                onClick = {
                    onSearch(history.query)
                    onDismiss()
                },
                onDelete = {
                    database.query {
                        delete(history)
                    }
                },
                onFillTextField = {
                    onQueryChange(TextFieldValue(history.query, TextRange(history.query.length)))
                },
                modifier = Modifier.animateItem(),
                pureBlack = pureBlack
            )
        }

        if (viewState.history.isNotEmpty() && viewState.suggestions.isNotEmpty()) {
            item(key = "history_suggestion_spacer") {
                Spacer(modifier = Modifier.height(16.dp).animateItem())
            }
        }

        if (viewState.suggestions.isNotEmpty()) {
            item(key = "suggestions_header") {
                Text(
                    text = stringResource(R.string.suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp).animateItem()
                )
            }
        }

        itemsIndexed(viewState.suggestions, key = { index, it -> "suggestion_${index}_$it" }) { index, query ->
            SuggestionItem(
                query = query,
                online = true,
                shape = getGroupedShape(index, viewState.suggestions.size),
                onClick = {
                    onSearch(query)
                    onDismiss()
                },
                onFillTextField = {
                    onQueryChange(TextFieldValue(query, TextRange(query.length)))
                },
                modifier = Modifier.animateItem(),
                pureBlack = pureBlack
            )
        }

        if (viewState.suggestions.isNotEmpty()) {
            item(key = "suggestions_bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp).animateItem())
            }
        }

        if (viewState.items.isNotEmpty()) {
            item(key = "search_divider") {
                Text(
                    text = stringResource(if (viewState.isFromLink) R.string.parsed_from_link else R.string.top_result),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp).animateItem()
                )
            }
            item(key = "search_divider_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        itemsIndexed(viewState.items, key = { index, it -> "item_${it.id}_$index" }) { index, item ->
            val isActive = when (item) {
                is SongItem -> mediaMetadata?.id == item.id
                is AlbumItem -> mediaMetadata?.album?.id == item.id
                else -> false
            }
            val openMenu = {
                menuState.show {
                    when (item) {
                        is SongItem -> YouTubeSongMenu(
                            song = item,
                            navController = navController,
                            onDismiss = {
                                menuState.dismiss()
                                onDismiss()
                            }
                        )
                        is AlbumItem -> YouTubeAlbumMenu(
                            albumItem = item,
                            navController = navController,
                            onDismiss = {
                                menuState.dismiss()
                                onDismiss()
                            }
                        )
                        is ArtistItem -> YouTubeArtistMenu(
                            artist = item,
                            onDismiss = {
                                menuState.dismiss()
                                onDismiss()
                            }
                        )
                        is PlaylistItem -> YouTubePlaylistMenu(
                            playlist = item,
                            coroutineScope = scope,
                            onDismiss = {
                                menuState.dismiss()
                                onDismiss()
                            }
                        )
                    }
                }
            }

            val handleItemClick = {
                when (item) {
                    is SongItem -> {
                        if (item.id == mediaMetadata?.id) {
                            playerConnection?.togglePlayPause()
                        } else {
                            scope.launch(Dispatchers.IO) {
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
                            playerConnection?.playQueue(
                                YouTubeQueue.radio(item.toMediaMetadata())
                            )
                            onDismiss()
                        }
                    }
                    is AlbumItem -> {
                        navController.navigate("album/${item.id}")
                        onDismiss()
                    }
                    is ArtistItem -> {
                        navController.navigate("artist/${item.id}")
                        onDismiss()
                    }
                    is PlaylistItem -> {
                        navController.navigate("online_playlist/${item.id}")
                        onDismiss()
                    }
                }
            }

            if (index == 0) {
                SearchSpotlightCard(
                    item = item,
                    isLink = viewState.isFromLink,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    onClick = handleItemClick,
                    onMenuClick = openMenu,
                    pureBlack = pureBlack,
                    modifier = Modifier.animateItem()
                )
            } else {
                YouTubeListItem(
                    item = item,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    shape = listItemShape(index, viewState.items.size),
                    trailingContent = {
                        IconButton(onClick = openMenu) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onClick = handleItemClick,
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                openMenu()
                            }
                        )
                        .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface)
                        .animateItem()
                )
            }
        }
    }
}

@Composable
fun SearchSpotlightCard(
    item: YTItem,
    isLink: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    pureBlack: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .mkrPressFeedback()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (pureBlack) Color(0xFF16171B) else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            if (isLink) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MkrBadge(
                    text = if (isLink) {
                        "Direct Link"
                    } else when (item) {
                        is SongItem -> "Top Song"
                        is AlbumItem -> "Top Album"
                        is ArtistItem -> "Top Artist"
                        is PlaylistItem -> "Top Playlist"
                        else -> "Top Result"
                    },
                    iconRes = if (isLink) R.drawable.globe else R.drawable.sparks,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(32.dp).mkrPressFeedback()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = stringResource(R.string.more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val thumbnailUrl = when (item) {
                    is SongItem -> item.thumbnail
                    is AlbumItem -> item.thumbnail
                    is ArtistItem -> item.thumbnail
                    is PlaylistItem -> item.thumbnail
                    else -> null
                }
                val isCircle = item is ArtistItem

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(if (isCircle) CircleShape else RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val title = when (item) {
                        is SongItem -> item.title
                        is AlbumItem -> item.title
                        is ArtistItem -> item.title
                        is PlaylistItem -> item.title
                        else -> ""
                    }
                    val subtitle = when (item) {
                        is SongItem -> item.artists.joinToString { it.name }
                        is AlbumItem -> item.artists?.joinToString { it.name } ?: item.year?.toString() ?: ""
                        is ArtistItem -> item.subscribers ?: ""
                        is PlaylistItem -> item.author?.name ?: ""
                        else -> ""
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = onClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(42.dp).mkrPressFeedback()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                if (isActive && isPlaying) R.drawable.volume_up else R.drawable.ic_widget_play
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    modifier: Modifier = Modifier,
    query: String,
    online: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onFillTextField: () -> Unit,
    pureBlack: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(SuggestionItemHeight)
            .clip(shape)
            .background(if (pureBlack) Color(0xFF16171B) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f))
            .mkrPressFeedback()
            .clickable(onClick = onClick)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
    ) {
        Icon(
            painterResource(if (online) R.drawable.search else R.drawable.history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp).size(20.dp)
        )

        Text(
            text = query,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (!online) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.alpha(0.6f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(
            onClick = onFillTextField,
            modifier = Modifier.alpha(0.6f),
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_top_left),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
