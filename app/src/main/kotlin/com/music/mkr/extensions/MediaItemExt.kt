/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.extensions

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import com.music.innertube.models.SongItem
import com.music.mkr.db.entities.Song
import com.music.mkr.models.MediaMetadata
import com.music.mkr.models.toMediaMetadata
import com.music.mkr.ui.utils.resize

val MediaItem.metadata: MediaMetadata?
    get() = (localConfiguration?.tag as? MediaMetadata) ?: run {
        if (mediaId.isBlank()) return null
        val titleStr = mediaMetadata.title?.toString()?.takeIf { it.isNotBlank() } ?: return null
        val artistStr = (mediaMetadata.artist ?: mediaMetadata.subtitle)?.toString() ?: ""
        val artworkUriStr = mediaMetadata.artworkUri?.toString()
            ?: mediaMetadata.extras?.getString("artwork_uri")
        val durationMs = mediaMetadata.extras?.getLong("duration") ?: 0L
        MediaMetadata(
            id = mediaId,
            title = titleStr,
            artists = if (artistStr.isNotBlank()) {
                artistStr.split(", ").map { MediaMetadata.Artist(id = null, name = it.trim()) }
            } else emptyList(),
            duration = (durationMs / 1000).toInt(),
            thumbnailUrl = artworkUriStr,
            album = mediaMetadata.albumTitle?.let { MediaMetadata.Album(id = "", title = it.toString()) }
        )
    }

fun Song.toMediaItem() = MediaItem.Builder()
    .setMediaId(song.id)
    .setUri(song.id)
    .setCustomCacheKey(song.id)
    .setTag(toMediaMetadata())
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(song.title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(song.thumbnailUrl?.toUri())
            .setAlbumTitle(song.albumName)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(song.title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                putString("artwork_uri", song.thumbnailUrl)
            })
            .build()
    )
    .build()

fun SongItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(toMediaMetadata())
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(thumbnail.resize(1200, 1200).toUri())
            .setAlbumTitle(album?.name)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                putString("artwork_uri", thumbnail.resize(1200, 1200))
            })
            .build()
    )
    .build()

fun MediaMetadata.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(this)
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(thumbnailUrl?.toUri())
            .setAlbumTitle(album?.title)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                thumbnailUrl?.let { putString("artwork_uri", it) }
            })
            .build()
    )
    .build()
