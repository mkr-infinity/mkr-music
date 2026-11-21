/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import com.music.mkr.utils.listItemShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.mkr.LocalDatabase
import com.music.mkr.R
import com.music.mkr.constants.AddToPlaylistSortDescendingKey
import com.music.mkr.constants.AddToPlaylistSortTypeKey
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.ListThumbnailSize
import com.music.mkr.constants.PlaylistSortType
import com.music.mkr.db.entities.Song
import com.music.mkr.models.ItemsPage
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.ui.component.CreatePlaylistDialog
import com.music.mkr.ui.component.ListDialog
import com.music.mkr.ui.component.ListItem
import com.music.mkr.ui.component.PlaylistListItem
import com.music.mkr.ui.component.SortHeader
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import com.music.mkr.utils.reportException
import com.music.mkr.viewmodels.PlaylistsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AddToPlaylistDialogOnline(
    isVisible: Boolean,
    allowSyncing: Boolean = true,
    initialTextFieldValue: String? = null,
    songs: SnapshotStateList<Song>, // list of song ids. Songs should be inserted to database in this function.
    onDismiss: () -> Unit,
    onProgressStart: (Boolean) -> Unit,
    onPercentageChange: (Int) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val viewStateMap = remember { mutableStateMapOf<String, ItemsPage?>() }
    val (sortType, onSortTypeChange) = rememberEnumPreference(
        AddToPlaylistSortTypeKey,
        PlaylistSortType.NAME
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(
        AddToPlaylistSortDescendingKey,
        false
    )
    val playlists by viewModel.allPlaylists.collectAsState()
    val animationsEnabled by rememberPreference(InterfaceAnimationsKey, true)

    var showCreatePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedSongs = songs.toList()
    val (addingSongs, addSongs) = playlistAddHandler(isVisible, viewModel, onDismiss, syncRemote = false) {
        database.withTransaction {
            selectedSongs.asReversed().forEach { insert(it.toMediaMetadata()) }
        }
        selectedSongs.asReversed().map { it.id }
    }
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredPlaylists = remember(playlists, searchQuery) {
        if (searchQuery.isBlank()) {
            playlists
        } else {
            playlists.filter { it.playlist.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    if (isVisible) {
        val totalItemsCount = 1 + filteredPlaylists.size
        ListDialog(
            onDismiss = onDismiss,
            title = if (addingSongs) ({ PlaylistAddProgress() }) else null,
        ) {
            item {
                ListItem(
                    title = stringResource(R.string.create_playlist),
                    thumbnailContent = {
                        Image(
                            painter = painterResource(id = R.drawable.playlist_add),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    shape = listItemShape(index = 0, count = totalItemsCount),
                    modifier = Modifier.clickable(enabled = !addingSongs) {
                        showCreatePlaylistDialog = true
                    }
                )
            }

            if (playlists.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                text = stringResource(R.string.search),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = stringResource(R.string.close),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            if (playlists.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        SortHeader(
                            sortType = sortType,
                            sortDescending = sortDescending,
                            onSortTypeChange = onSortTypeChange,
                            onSortDescendingChange = onSortDescendingChange,
                            sortTypeText = { sortType ->
                                when (sortType) {
                                    PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                                    PlaylistSortType.NAME -> R.string.sort_by_name
                                    PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                                    PlaylistSortType.LAST_UPDATED -> R.string.sort_by_last_updated
                                }
                            },
                        )
                    }
                }
            }

            if (filteredPlaylists.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_results_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            itemsIndexed(filteredPlaylists) { index, playlist ->
                PlaylistListItem(
                    playlist = playlist,
                    shape = listItemShape(index = index + 1, count = totalItemsCount),
                    modifier = playlistTargetClick(
                        enabled = !addingSongs,
                        animationsEnabled = animationsEnabled,
                    ) { addSongs(playlist) }
                )
            }

            item {
                ListItem(
                    modifier = Modifier.clickable(enabled = !addingSongs) {
                        coroutineScope.launch(Dispatchers.IO) {
                            onDismiss()
                            val songsTot = songs.count().toDouble()
                            var  songsIdx = 0.toDouble()
                            onProgressStart(true)
                            songs.reversed().forEach{
                                    song ->
                                var allArtists = ""
                                song.artists.forEach {
                                        artist ->
                                    allArtists += " ${URLDecoder.decode(artist.name, StandardCharsets.UTF_8.toString())}"
                                }
                                val query = "${song.title} - $allArtists"

                                coroutineScope.launch {
                                    try {
                                        YouTube.search(query, YouTube.SearchFilter.FILTER_SONG)
                                            .onSuccess { result ->
                                                val items = result.items.distinctBy { it.id }
                                                if (items.isNotEmpty()) {
                                                    viewStateMap[YouTube.SearchFilter.FILTER_SONG.value] =
                                                        ItemsPage(items, result.continuation)
                                                    val itemsPage = viewStateMap.entries.firstOrNull()?.value
                                                    val firstSong = itemsPage?.items?.firstOrNull() as? SongItem
                                                    if (firstSong != null) {
                                                        val firstSongMedia = firstSong.toMediaMetadata()
                                                        val firstSongEnt = firstSong.toMediaMetadata().toSongEntity()
                                                        withContext(Dispatchers.IO) {
                                                            try {
                                                                database.insert(firstSongMedia)
                                                                database.query {
                                                                    update(firstSongEnt.toggleLike())
                                                                }
                                                            } catch (e: Exception) {
                                                                Timber.tag("Exception inserting song in database:")
                                                                    .e(e.toString())
                                                            }
                                                        }
                                                    }
                                                    viewStateMap.clear()
                                                }
                                                songsIdx += 1
                                            }
                                            .onFailure {
                                                reportException(it)
                                                songsIdx += 1
                                            }

                                        if (songsIdx.toInt() == songsTot.toInt() - 1) {
                                            onProgressStart(false)
                                        }
                                        onPercentageChange(((songsIdx / songsTot) * 100).toInt())

                                    } catch (e: Exception){
                                        Timber.tag("ERROR").v(e.toString())
                                    }

                                }

                            }

                        }
                    },
                    title = stringResource(R.string.liked_songs),
                    thumbnailContent = {
                        Image(
                            painter = painterResource(id = R.drawable.favorite), // The XML image
                            contentDescription = null,
                            modifier = Modifier.size(40.dp), // Adjust size as needed
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground) // Optional tinting
                        )
                    },
                    trailingContent = {}
                )
            }

            item {
                Text(
                    text = stringResource(R.string.playlist_add_local_to_synced_note),
                    fontSize = TextUnit(12F, TextUnitType.Sp),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            initialTextFieldValue = initialTextFieldValue,
            allowSyncing = allowSyncing
        )
    }
}
