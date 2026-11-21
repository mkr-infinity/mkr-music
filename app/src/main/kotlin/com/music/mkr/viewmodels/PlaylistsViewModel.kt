/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.music.mkr.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.response.AddItemYouTubePlaylistResponse
import com.music.mkr.R
import com.music.mkr.constants.AddToPlaylistSortDescendingKey
import com.music.mkr.constants.AddToPlaylistSortTypeKey
import com.music.mkr.constants.PlaylistSortType
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.PlaylistEntity
import com.music.mkr.di.ApplicationScope
import com.music.mkr.extensions.toEnum
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.utils.SyncUtils
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.call.body
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    private val syncUtils: SyncUtils,
    @ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {
    val allPlaylists =
        context.dataStore.data
            .map {
                it[AddToPlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE) to (it[AddToPlaylistSortDescendingKey]
                    ?: true)
            }.distinctUntilChanged()
            .flatMapLatest { (sortType, descending) ->
                database.playlists(sortType, descending)
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Suspend function that waits for sync to complete
    suspend fun sync() {
        syncUtils.syncSavedPlaylists()
    }

    internal fun addSongs(
        destination: PlaylistEntity,
        duplicateChoice: DuplicateChoice?,
        syncRemote: Boolean,
        prepare: suspend () -> List<String>,
    ) = appScope.async(Dispatchers.IO) {
        var playlistName = destination.name
        var savedLocally = false
        fun show(message: Int, icon: Int) {
            SnackbarManager.show(message, messageArgs = playlistName, iconRes = icon)
        }

        try {
            checkNotNull(database.getPlaylistEntity(destination.id))
            val songIds = prepare()
            check(songIds.isNotEmpty())
            val (playlist, idsToAdd, duplicateCount) = database.appendSongsFromPicker(
                destination.id,
                songIds,
                allowDuplicates = duplicateChoice?.let { it == DuplicateChoice.AddAnyway },
            )
            playlistName = playlist.name
            if (duplicateCount > 0) return@async DuplicateSongs(songIds, duplicateCount)
            if (idsToAdd.isEmpty()) {
                show(R.string.playlist_add_already_present, R.drawable.info)
                return@async null
            }

            savedLocally = true
            val browseId = playlist.browseId
            if (syncRemote && browseId != null && playlist.isEditable) {
                for (batch in idsToAdd.chunked(50)) {
                    if (database.getPlaylistEntity(playlist.id) == null) {
                        show(R.string.playlist_add_failed, R.drawable.error)
                        return@async null
                    }
                    val response = YouTube.addVideosToPlaylist(browseId, batch).body<AddItemYouTubePlaylistResponse>()
                    val addedIds = response.playlistEditResults.map { it.playlistEditVideoAddedResultData.videoId }
                    check(response.status == "STATUS_SUCCEEDED" && addedIds.sorted() == batch.sorted()) {
                        "Remote playlist did not confirm all song additions"
                    }
                }
            }
            if (database.getPlaylistEntity(playlist.id) == null) {
                show(R.string.playlist_add_failed, R.drawable.error)
                return@async null
            }
            show(
                when {
                    browseId != null && (!syncRemote || !playlist.isEditable) -> R.string.playlist_add_local_only
                    else -> R.string.playlist_add_success
                },
                R.drawable.check,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportException(e)
            val localCopyExists = savedLocally &&
                runCatching { database.getPlaylistEntity(destination.id) }.getOrNull() != null
            show(
                if (localCopyExists) R.string.playlist_add_sync_failed else R.string.playlist_add_failed,
                R.drawable.error,
            )
        }
        null
    }
}

internal enum class DuplicateChoice { Skip, AddAnyway }

internal data class DuplicateSongs(val songIds: List<String>, val count: Int)
