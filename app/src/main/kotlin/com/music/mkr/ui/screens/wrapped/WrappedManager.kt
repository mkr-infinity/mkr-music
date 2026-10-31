/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.wrapped

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.music.innertube.YouTube
import com.music.innertube.models.AccountInfo
import com.music.mkr.constants.ArtistSongSortType
import com.music.mkr.db.DatabaseDao
import com.music.mkr.db.entities.Artist
import com.music.mkr.db.entities.PlaylistEntity
import com.music.mkr.db.entities.SongWithStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.Calendar
import java.util.UUID

sealed class PlaylistCreationState {
    object Idle : PlaylistCreationState()
    object Creating : PlaylistCreationState()
    object Success : PlaylistCreationState()
}

class WrappedManager(
    private val databaseDao: DatabaseDao,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(WrappedState())
    val state = _state.asStateFlow()

    fun createPlaylist(imageResName: String) {
        if (_state.value.playlistCreationState != PlaylistCreationState.Idle) return

        _state.update { it.copy(playlistCreationState = PlaylistCreationState.Creating) }
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val fromTimestamp = Calendar.getInstance().apply {
                        set(WrappedConstants.YEAR, Calendar.JANUARY, 1, 0, 0, 0)
                    }.timeInMillis
                    val toTimestamp = Calendar.getInstance().apply {
                        set(WrappedConstants.YEAR, Calendar.DECEMBER, 31, 23, 59, 59)
                    }.timeInMillis
                    val allSongs = databaseDao.mostPlayedSongsStats(fromTimestamp, toTimeStamp = toTimestamp, limit = -1).first()

                    val playlistId = UUID.randomUUID().toString()

                    val drawableId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
                    val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
                    val file = File(context.cacheDir, "$playlistId.png")
                    FileOutputStream(file).use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }

                    val newPlaylist = PlaylistEntity(
                        id = playlistId,
                        name = WrappedConstants.PLAYLIST_NAME,
                        thumbnailUrl = file.toURI().toString(),
                        bookmarkedAt = LocalDateTime.now(),
                        isEditable = true
                    )
                    databaseDao.insert(newPlaylist)

                    val createdPlaylist = databaseDao.playlist(playlistId).first()
                    if (createdPlaylist != null) {
                        val songIds = allSongs.map { it.id }
                        databaseDao.addSongToPlaylist(createdPlaylist, songIds)
                    } else {
                        Timber.tag("WrappedManager")
                            .e("Failed to retrieve created playlist with id: $playlistId")
                    }
                }
                _state.update { it.copy(playlistCreationState = PlaylistCreationState.Success) }
            } catch (e: Exception) {
                Timber.tag("WrappedManager").e(e, "Error saving wrapped playlist")
                _state.update { it.copy(playlistCreationState = PlaylistCreationState.Idle) }
            }
        }
    }

    private suspend fun generatePlaylistMap() {
        val topSongs = _state.value.topSongs
        val topArtists = _state.value.topArtists
        if (topSongs.isEmpty()) {
            Timber.tag("WrappedManager").w("Cannot generate playlist map, top songs list is empty.")
            _state.update { it.copy(trackMap = emptyMap()) }
            return
        }

        withContext(Dispatchers.IO) {
            val playlistMap = mutableMapOf<WrappedScreenType, String>()

            // Intro Part: Random song from top 6-30 (or any available song)
            val introSongPool = if (topSongs.size > 5) topSongs.subList(5, topSongs.size) else topSongs
            val introSong = introSongPool.randomOrNull()?.id ?: topSongs.lastOrNull()?.id ?: topSongs.firstOrNull()?.id.orEmpty()
            playlistMap[WrappedScreenType.Welcome] = introSong
            playlistMap[WrappedScreenType.MinutesTease] = introSong
            playlistMap[WrappedScreenType.MinutesReveal] = introSong

            // Music Part: Top 1 song
            val topSong = topSongs.firstOrNull()
            val topSongId = topSong?.id.orEmpty()
            playlistMap[WrappedScreenType.TotalSongs] = topSongId
            playlistMap[WrappedScreenType.TopSongReveal] = topSongId
            playlistMap[WrappedScreenType.Top5Songs] = topSongId

            // Album Part: Random song from top album
            val topAlbum = _state.value.topAlbum
            val albumSong = topAlbum?.let { album ->
                val albumSongs = databaseDao.albumSongs(album.id).firstOrNull() ?: emptyList()
                albumSongs.randomOrNull()?.id
            } ?: topSongId // Fallback to top song if no album songs
            playlistMap[WrappedScreenType.TotalAlbums] = albumSong
            playlistMap[WrappedScreenType.TopAlbumReveal] = albumSong
            playlistMap[WrappedScreenType.Top5Albums] = albumSong

            // Artist Part: Top artist's song with specific rule
            val topArtist = topArtists.firstOrNull()
            val fromTimestamp = Calendar.getInstance().apply {
                set(WrappedConstants.YEAR, Calendar.JANUARY, 1, 0, 0, 0)
            }.timeInMillis
            val toTimestamp = Calendar.getInstance().apply {
                set(WrappedConstants.YEAR, Calendar.DECEMBER, 31, 23, 59, 59)
            }.timeInMillis

            val artistSong = topArtist?.let { artist ->
                val artistTopSongs = databaseDao.artistSongs(
                    artistId = artist.id,
                    sortType = ArtistSongSortType.PLAY_TIME,
                    descending = true,
                    fromTimeStamp = fromTimestamp,
                    toTimeStamp = toTimestamp
                ).firstOrNull() ?: emptyList()
                if (artistTopSongs.isNotEmpty()) {
                    val artistTopSong = artistTopSongs.first()
                    if (artistTopSong.id == topSongId) {
                        // Overlap: Use the artist's second song.
                        // If a second song doesn't exist, use a random song from their list.
                        artistTopSongs.getOrNull(1)?.id ?: artistTopSongs.filter { it.id != topSongId }.randomOrNull()?.id ?: artistTopSong.id
                    } else {
                        artistTopSong.id
                    }
                } else {
                    // Data anomaly: Fallback to the user's top song.
                    topSongId
                }
            } ?: topSongId // Fallback if no top artist.
            playlistMap[WrappedScreenType.TotalArtists] = artistSong
            playlistMap[WrappedScreenType.TopArtistReveal] = artistSong
            playlistMap[WrappedScreenType.Top5Artists] = artistSong

            // End Part
            val endSongPool = if (topSongs.size > 2) topSongs.subList(2, minOf(5, topSongs.size)) else topSongs
            val endSong = endSongPool.randomOrNull()?.id ?: topSongId
            playlistMap[WrappedScreenType.Playlist] = endSong
            playlistMap[WrappedScreenType.Conclusion] = "2-p9DM2Xvsc"

            Timber.tag("WrappedManager").d("Generated Playlist Map: $playlistMap")
            _state.update { it.copy(trackMap = playlistMap) }
        }
    }

    suspend fun prepare() {
        if (_state.value.isDataReady) return
        Timber.tag("WrappedManager").d("Starting Wrapped data preparation")

        try {
            val fromTimestamp = Calendar.getInstance().apply {
                set(WrappedConstants.YEAR, Calendar.JANUARY, 1, 0, 0, 0)
            }.timeInMillis

            val toTimestamp = Calendar.getInstance().apply {
                set(WrappedConstants.YEAR, Calendar.DECEMBER, 31, 23, 59, 59)
            }.timeInMillis

            withContext(Dispatchers.IO) {
                val accountInfoDeferred = async { YouTube.accountInfo().getOrNull() }
                val topSongsDeferred = async { databaseDao.mostPlayedSongsStats(fromTimestamp, toTimeStamp = toTimestamp, limit = 30).firstOrNull() ?: emptyList() }
                val topArtistsDeferred = async { databaseDao.mostPlayedArtists(fromTimestamp, toTimeStamp = toTimestamp, limit = 5).firstOrNull() ?: emptyList() }
                val topAlbumsDeferred = async { databaseDao.mostPlayedAlbums(fromTimestamp, toTimeStamp = toTimestamp, limit = 5).firstOrNull() ?: emptyList() }
                val uniqueSongCountDeferred = async { databaseDao.getUniqueSongCountInRange(fromTimestamp, toTimestamp).firstOrNull() ?: 0 }
                val uniqueArtistCountDeferred = async { databaseDao.getUniqueArtistCountInRange(fromTimestamp, toTimestamp).firstOrNull() ?: 0 }
                val uniqueAlbumCountDeferred = async { databaseDao.getUniqueAlbumCountInRange(fromTimestamp, toTimestamp).firstOrNull() ?: 0 }
                val totalPlayTimeMsDeferred = async { databaseDao.getTotalPlayTimeInRange(fromTimestamp, toTimestamp).firstOrNull() ?: 0L }

                val results = awaitAll(
                    accountInfoDeferred,
                    topSongsDeferred,
                    topArtistsDeferred,
                    topAlbumsDeferred,
                    uniqueSongCountDeferred,
                    uniqueArtistCountDeferred,
                    uniqueAlbumCountDeferred,
                    totalPlayTimeMsDeferred
                )

                @Suppress("UNCHECKED_CAST")
                val topSongsResult = results[1] as List<SongWithStats>
                @Suppress("UNCHECKED_CAST")
                val topAlbumsResult = results[3] as List<com.music.mkr.db.entities.Album>
                @Suppress("UNCHECKED_CAST")
                val topArtistsResult = results[2] as List<Artist>
                _state.update {
                    it.copy(
                        accountInfo = results[0] as AccountInfo?,
                        topSongs = topSongsResult,
                        topArtists = topArtistsResult,
                        top5Albums = topAlbumsResult,
                        topAlbum = topAlbumsResult.firstOrNull(),
                        uniqueSongCount = (results[4] as? Int) ?: 0,
                        uniqueArtistCount = (results[5] as? Int) ?: 0,
                        totalAlbums = (results[6] as? Int) ?: 0,
                        totalMinutes = ((results[7] as? Long) ?: 0L) / 1000 / 60
                    )
                }
            }

            generatePlaylistMap()
        } catch (e: Exception) {
            Timber.tag("WrappedManager").e(e, "Error during Wrapped data preparation")
        } finally {
            _state.update { it.copy(isDataReady = true) }
            Timber.tag("WrappedManager").d("Wrapped data preparation finished")
        }
    }
}
