/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.music.mkr.viewmodels

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.mkr.constants.AlbumFilter
import com.music.mkr.constants.AlbumFilterKey
import com.music.mkr.constants.AlbumSortDescendingKey
import com.music.mkr.constants.AlbumSortType
import com.music.mkr.constants.AlbumSortTypeKey
import com.music.mkr.constants.ArtistFilter
import com.music.mkr.constants.ArtistFilterKey
import com.music.mkr.constants.ArtistSongSortDescendingKey
import com.music.mkr.constants.ArtistSongSortType
import com.music.mkr.constants.ArtistSongSortTypeKey
import com.music.mkr.constants.ArtistSortDescendingKey
import com.music.mkr.constants.ArtistSortType
import com.music.mkr.constants.ArtistSortTypeKey
import com.music.mkr.constants.ArtistSourceFilter
import com.music.mkr.constants.ArtistSourceFilterKey
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.constants.HideYoutubeShortsKey
import com.music.mkr.constants.LibraryFilter
import com.music.mkr.constants.PlaylistSortDescendingKey
import com.music.mkr.constants.PlaylistSortType
import com.music.mkr.constants.PlaylistSortTypeKey
import com.music.mkr.constants.SongFilter
import com.music.mkr.constants.SongFilterKey
import com.music.mkr.constants.SongSortDescendingKey
import com.music.mkr.constants.SongSortType
import com.music.mkr.constants.SongSortTypeKey
import com.music.mkr.constants.TopSize
import com.music.mkr.db.MusicDatabase
import com.music.mkr.extensions.filterExplicit
import com.music.mkr.extensions.filterExplicitAlbums
import com.music.mkr.extensions.filterVideoSongs
import com.music.mkr.extensions.filterYoutubeShorts
import com.music.mkr.extensions.toEnum
import com.music.mkr.playback.DownloadUtil
import com.music.mkr.utils.SyncUtils
import com.music.mkr.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val allSongs =
        context.dataStore.data
            .map {
                Triple(
                    Triple(
                        it[SongFilterKey].toEnum(SongFilter.LIKED),
                        it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                        (it[SongSortDescendingKey] ?: true),
                    ),
                    it[HideExplicitKey] ?: false,
                    it[HideVideoSongsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (filterSort, hideExplicit, hideVideoSongs) ->
                val (filter, sortType, descending) = filterSort
                when (filter) {
                    SongFilter.LIBRARY -> database.songs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                    SongFilter.LIKED -> database.likedSongs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                    SongFilter.DOWNLOADED -> database.downloadedSongs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun syncLikedSongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLikedSongs() }
    }

    fun syncLibrarySongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLibrarySongs() }
    }


}

@HiltViewModel
class LibraryArtistsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private data class ArtistState(
        val filter: ArtistFilter,
        val sortType: ArtistSortType,
        val descending: Boolean,
        val sourceFilter: ArtistSourceFilter
    )

    val allArtists =
        context.dataStore.data
            .map {
                ArtistState(
                    it[ArtistFilterKey].toEnum(ArtistFilter.LIKED),
                    it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                    it[ArtistSortDescendingKey] ?: true,
                    it[ArtistSourceFilterKey].toEnum(ArtistSourceFilter.ALL)
                )
            }.distinctUntilChanged()
            .flatMapLatest { state ->
                when (state.filter) {
                    ArtistFilter.LIKED -> database.artistsBookmarked(state.sortType, state.descending, state.sourceFilter)
                    ArtistFilter.LIBRARY -> database.artists(state.sortType, state.descending, state.sourceFilter)
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncArtistsSubscriptions() }
    }


}

@HiltViewModel
class LibraryAlbumsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val allAlbums =
        context.dataStore.data
            .map {
                Pair(
                    Triple(
                        it[AlbumFilterKey].toEnum(AlbumFilter.ALL),
                        it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                        it[AlbumSortDescendingKey] ?: true,
                    ),
                    it[HideExplicitKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (filterSort, hideExplicit) ->
                val (filter, sortType, descending) = filterSort
                when (filter) {
                    AlbumFilter.LIKED -> database.albumsLiked(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                    AlbumFilter.LIBRARY -> database.albums(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                    AlbumFilter.ALL -> database.albumsAll(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLikedAlbums() }
    }

    val searchQuery = MutableStateFlow("")
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val debouncedSearchQuery = searchQuery.debounce(250)


}

@HiltViewModel
class LibraryPlaylistsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    val allPlaylists =
        context.dataStore.data
            .map {
                Triple(
                    it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE),
                    it[PlaylistSortDescendingKey] ?: true,
                    it[HideYoutubeShortsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortType, descending, hideYoutubeShorts) ->
                database.playlists(sortType, descending).map { it.filterYoutubeShorts(hideYoutubeShorts) }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchQuery = MutableStateFlow("")
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val debouncedSearchQuery = searchQuery.debounce(250)

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncSavedPlaylists() }
    }

    val topValue =
        context.dataStore.data
            .map { it[TopSize] ?: "50" }
            .distinctUntilChanged()

    val recentLikedThumbnails = database.likedSongs(SongSortType.CREATE_DATE, true)
        .map { songs -> songs.take(3).mapNotNull { it.thumbnailUrl } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentDownloadedThumbnails = database.downloadedSongs(SongSortType.CREATE_DATE, true)
        .map { songs -> songs.take(3).mapNotNull { it.thumbnailUrl } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentAlbumsThumbnails = context.dataStore.data
        .map { it[HideExplicitKey] ?: false }
        .distinctUntilChanged()
        .flatMapLatest { hideExplicit ->
            database.albumsLiked(AlbumSortType.CREATE_DATE, true)
                .map { albums -> albums.filterExplicitAlbums(hideExplicit).take(3).mapNotNull { it.album.thumbnailUrl } }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class ArtistSongsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val artistId = savedStateHandle.get<String>("artistId")!!
    val artist =
        database
            .artist(artistId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val songs =
        context.dataStore.data
            .map {
                Triple(
                    it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (it[ArtistSongSortDescendingKey]
                        ?: true),
                    it[HideExplicitKey] ?: false,
                    it[HideVideoSongsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit, hideVideoSongs) ->
                val (sortType, descending) = sortDesc
                database.artistSongs(artistId, sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}


@HiltViewModel
class LibraryViewModel
@Inject
constructor() : ViewModel() {
    private val curScreen = mutableStateOf(LibraryFilter.LIBRARY)
    val filter: MutableState<LibraryFilter> = curScreen
}
