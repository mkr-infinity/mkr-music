/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.mkr.db.MusicDatabase
import com.music.mkr.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import com.music.mkr.utils.AppleMusicAboutAlbum
import javax.inject.Inject

sealed interface AlbumUiState {
    data object Loading : AlbumUiState
    data object Success : AlbumUiState
    data object Empty : AlbumUiState
    data class Error(val isNotFound: Boolean = false) : AlbumUiState
}

private sealed interface FetchState {
    data object Pending : FetchState
    data object Success : FetchState
    data class Failed(val isNotFound: Boolean = false) : FetchState
}

@HiltViewModel
class AlbumViewModel
@Inject
constructor(
    private val database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val albumId = savedStateHandle.get<String>("albumId")!!
    val playlistId = MutableStateFlow("")
    val albumWithSongs =
        database
            .albumWithSongs(albumId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    var otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())
    var releasesForYou = MutableStateFlow<List<AlbumItem>>(emptyList())
    var description = MutableStateFlow<String?>(null)
    var descriptionRuns = MutableStateFlow<List<com.music.innertube.models.Run>?>(null)

    private var fetchJob: Job? = null
    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Pending)

    val uiState: StateFlow<AlbumUiState> =
        combine(albumWithSongs, _fetchState) { data, fetch ->
            when {
                data != null && data.songs.isNotEmpty() -> AlbumUiState.Success
                fetch is FetchState.Pending -> AlbumUiState.Loading
                fetch is FetchState.Failed && data == null -> AlbumUiState.Error(fetch.isNotFound)
                fetch is FetchState.Success && (data == null || data.songs.isEmpty()) -> AlbumUiState.Empty
                fetch is FetchState.Failed && data != null && data.songs.isEmpty() -> AlbumUiState.Empty
                else -> AlbumUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AlbumUiState.Loading)

    init {
        retry()
    }

    fun retry() {
        if (fetchJob?.isActive == true) return
        fetchJob = viewModelScope.launch {
            _fetchState.value = FetchState.Pending
            val album = database.album(albumId).first()
            if (album?.description != null) {
                description.value = album.description
            }
            if (album?.album?.isLocal == true) {
                _fetchState.value = FetchState.Success
                return@launch
            }

            // Pre-fetch Apple Music description concurrently if we have basic metadata and no description yet
            var appleDescDeferred: Deferred<String?>? = null
            if (album?.description == null && album?.album?.title != null) {
                val artistName = album.artists.firstOrNull()?.name
                if (artistName != null) {
                    appleDescDeferred = async(Dispatchers.IO) {
                        AppleMusicAboutAlbum.fetchAlbumDescription(album.album.title, artistName)
                    }
                }
            }

            YouTube
                .album(albumId)
                .onSuccess {
                    playlistId.value = it.album.playlistId
                    otherVersions.value = it.otherVersions
                    releasesForYou.value = it.releasesForYou
                    if (it.description != null) {
                        description.value = it.description
                    }
                    descriptionRuns.value = it.descriptionRuns
                    database.transaction {
                        if (album == null) {
                            insert(it)
                        } else {
                            update(album.album, it, album.artists)
                        }
                    }
                    _fetchState.value = FetchState.Success

                    val albumArtists = it.album.artists
                    if (albumArtists?.size == 1) {
                        albumArtists.firstOrNull()?.id?.let { artistId ->
                            viewModelScope.launch(Dispatchers.IO) {
                                val artistEntity = database.getArtistById(artistId)
                                if (artistEntity?.thumbnailUrl == null) {
                                    YouTube.artist(artistId).onSuccess { artistPage ->
                                        database.query {
                                            getArtistById(artistId)?.let { currentArtist ->
                                                update(currentArtist, artistPage)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (description.value == null && descriptionRuns.value == null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val appleDescription = if (appleDescDeferred != null) {
                                appleDescDeferred?.await()
                            } else {
                                val artistName = album?.artists?.firstOrNull()?.name 
                                    ?: database.albumWithSongs(albumId).first()?.artists?.firstOrNull()?.name
                                AppleMusicAboutAlbum.fetchAlbumDescription(it.album.title, artistName)
                            }
                            
                            if (appleDescription != null) {
                                description.value = appleDescription
                                val currentAlbum = database.album(albumId).first()
                                if (currentAlbum != null) {
                                    database.query {
                                        update(currentAlbum.album.copy(description = appleDescription))
                                    }
                                }
                            }
                        }
                    } else if (it.description != null && appleDescDeferred != null) {
                        appleDescDeferred?.cancel()
                    }
                }.onFailure {
                    reportException(it)
                    val isNotFound = it.message?.contains("NOT_FOUND") == true
                    _fetchState.value = FetchState.Failed(isNotFound = isNotFound)
                    if (isNotFound) {
                        val albumToDelete = album?.album
                        if (albumToDelete != null) {
                            database.query {
                                delete(albumToDelete)
                            }
                        }
                    }
                }
        }
    }
}
