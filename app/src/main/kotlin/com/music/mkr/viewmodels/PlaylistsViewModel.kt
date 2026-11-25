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
import com.music.innertube.utils.completed
import com.music.mkr.R
import com.music.mkr.constants.AddToPlaylistSortDescendingKey
import com.music.mkr.constants.AddToPlaylistSortTypeKey
import com.music.mkr.constants.PlaylistSortType
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.PlaylistEntity
import com.music.mkr.di.ApplicationScope
import com.music.mkr.extensions.toEnum
import com.music.mkr.utils.SyncUtils
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.reportException
import com.music.mkr.utils.unmatchedLocalPlaylistEntries
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
        fun feedback(message: Int, icon: Int) = PlaylistAddResult.Feedback(message, icon, playlistName)

        try {
            checkNotNull(database.getPlaylistEntity(destination.id))
            val songIds = prepare()
            check(songIds.isNotEmpty())
            return@async syncUtils.withPlaylistEdit(destination.id) {
                val (playlist, idsToAdd, duplicateCount) = database.appendSongsFromPicker(
                    destination.id,
                    songIds,
                    allowDuplicates = duplicateChoice?.let { it == DuplicateChoice.AddAnyway },
                )
                playlistName = playlist.name
                if (duplicateCount > 0) return@withPlaylistEdit PlaylistAddResult.Duplicates(DuplicateSongs(songIds, duplicateCount))
                val browseId = playlist.browseId
                val canSync = syncRemote && browseId != null && playlist.isEditable
                savedLocally = idsToAdd.isNotEmpty()
                val requestedIds = songIds.toHashSet()
                val retryIds = if (duplicateChoice == DuplicateChoice.Skip && canSync && browseId != null) {
                    val local = database.playlistMappings(playlist.id)
                    val possibleRetries = local.filter { it.setVideoId == null && it.songId in requestedIds }
                        .map { it.songId }.filterNot { it in idsToAdd }.toSet()
                    if (possibleRetries.isEmpty()) emptyList() else {
                        savedLocally = true
                        val remote = YouTube.playlist(browseId).completed(requireComplete = true).getOrThrow()
                            .songs.map { it.id to it.setVideoId }
                        unmatchedLocalPlaylistEntries(local, remote)
                            .filter { it.songId in possibleRetries }.map { it.songId }
                    }
                } else emptyList()
                val remoteIds = idsToAdd + retryIds
                if (remoteIds.isEmpty()) {
                    return@withPlaylistEdit feedback(R.string.playlist_add_already_present, R.drawable.info)
                }

                if (canSync && browseId != null) {
                    for (batch in remoteIds.chunked(50)) {
                        if (database.getPlaylistEntity(playlist.id) == null) {
                            return@withPlaylistEdit feedback(R.string.playlist_add_failed, R.drawable.error)
                        }
                        val response = YouTube.addVideosToPlaylist(browseId, batch).body<AddItemYouTubePlaylistResponse>()
                        val addedIds = response.playlistEditResults.map { it.playlistEditVideoAddedResultData.videoId }
                        check(response.status == "STATUS_SUCCEEDED" && addedIds.sorted() == batch.sorted()) {
                            "Remote playlist did not confirm all song additions"
                        }
                    }
                }
                if (database.getPlaylistEntity(playlist.id) == null) {
                    return@withPlaylistEdit feedback(R.string.playlist_add_failed, R.drawable.error)
                }
                feedback(
                    when {
                        browseId != null && (!syncRemote || !playlist.isEditable) -> R.string.playlist_add_local_only
                        else -> R.string.playlist_add_success
                    },
                    R.drawable.check,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportException(e)
            val localCopyExists = savedLocally &&
                runCatching { database.getPlaylistEntity(destination.id) }.getOrNull() != null
            feedback(
                if (localCopyExists) R.string.playlist_add_sync_failed else R.string.playlist_add_failed,
                R.drawable.error,
            )
        }
    }
}

internal enum class DuplicateChoice { Skip, AddAnyway }

internal data class DuplicateSongs(val songIds: List<String>, val count: Int)

internal sealed interface PlaylistAddResult {
    data class Duplicates(val songs: DuplicateSongs) : PlaylistAddResult
    data class Feedback(val message: Int, val icon: Int, val playlistName: String) : PlaylistAddResult {
        val added: Boolean get() = icon == R.drawable.check
    }
}
