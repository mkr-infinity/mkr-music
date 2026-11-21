/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.cache.SimpleCache
import com.music.mkr.R
import com.music.mkr.constants.DisableSongCacheKey
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.Song
import com.music.mkr.di.PlayerCache
import com.music.mkr.extensions.filterExplicit
import com.music.mkr.extensions.filterVideoSongs
import com.music.mkr.playback.CacheSpanChanges
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CachePlaylistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    @PlayerCache private val playerCache: SimpleCache,
    cacheChanges: CacheSpanChanges,
) : ViewModel() {
    sealed interface State {
        data object Loading : State
        data object Error : State
        data class Ready(val songs: List<Song>) : State
    }

    private val reload = MutableStateFlow(0)
    private val preferences = context.dataStore.data
        .map { Triple(it[DisableSongCacheKey] ?: false, it[HideExplicitKey] ?: false, it[HideVideoSongsKey] ?: false) }
        .distinctUntilChanged()

    val state = combine(reload, preferences) { _, settings -> settings }
        .flatMapLatest { (disabled, hideExplicit, hideVideoSongs) ->
            val entries = if (disabled) flowOf(emptyMap<String, Long>()) else cacheChanges.completeEntries(playerCache)
            val songs = entries.map { it.keys }.distinctUntilChanged().flatMapLatest { ids ->
                val queries = ids.chunked(500).map(database::observeSongsByIds)
                if (queries.isEmpty()) flowOf(emptyList()) else combine(queries) { it.flatMap { songs -> songs } }
            }
            combine(songs, entries) { rows, complete ->
                State.Ready(
                    rows.filter { it.id in complete }
                        .sortedByDescending { complete[it.id] }
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs),
                ) as State
            }.onStart { emit(State.Loading) }
                .catch {
                    reportException(it)
                    emit(State.Error)
                }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State.Loading)

    fun retry() {
        reload.update { it + 1 }
    }

    fun removeSongFromCache(songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                playerCache.removeResource(songId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reportException(e)
                SnackbarManager.show(R.string.library_cache_remove_error)
            }
        }
    }
}
