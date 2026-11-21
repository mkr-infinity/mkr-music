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

val MediaItem.metadata: MediaMetadata?
    get() = (localConfiguration?.tag as? MediaMetadata) ?: run {
        if (mediaId.isBlank()) return null
        val titleStr = mediaMetadata.title?.toString()?.takeIf { it.isNotBlank() } ?: return null
        val artistStr = mediaMetadata.artist?.toString()?.takeIf { it.isNotBlank() }
            ?: mediaMetadata.subtitle?.toString().orEmpty()
        val artworkUriStr = mediaMetadata.artworkUri?.toString()?.takeIf { it.isNotBlank() }
            ?: mediaMetadata.extras?.getString("artwork_uri")?.takeIf { it.isNotBlank() }
        val durationMs = mediaMetadata.durationMs?.takeIf { it > 0 }
            ?: mediaMetadata.extras?.getLong("duration")?.takeIf { it > 0 }
            ?: mediaMetadata.extras?.getLong("android.media.metadata.DURATION")?.takeIf { it > 0 }
            ?: 0L
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

internal val Song.sessionArtworkUrl: String?
    get() {
        val trackArtwork = song.thumbnailUrl?.takeIf { it.isNotBlank() }
        val albumArtwork = album?.thumbnailUrl?.takeIf { it.isNotBlank() }
        return if (song.isVideo) trackArtwork ?: albumArtwork else albumArtwork ?: trackArtwork
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
            .setArtworkUri(sessionArtworkUrl?.toUri())
            .setDurationMs(song.duration.takeIf { it > 0 }?.times(1000L))
            .setAlbumTitle(song.albumName ?: album?.title)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(song.title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                putString("artwork_uri", song.thumbnailUrl)
                if (song.duration > 0) {
                    putLong("duration", song.duration * 1000L)
                    putLong("android.media.metadata.DURATION", song.duration * 1000L)
                }
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
            .setArtworkUri(thumbnail.takeIf { it.isNotBlank() }?.toUri())
            .setDurationMs(duration?.takeIf { it > 0 }?.times(1000L))
            .setAlbumTitle(album?.name)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                putString("artwork_uri", thumbnail.takeIf { it.isNotBlank() })
                duration?.let {
                    if (it > 0) {
                        putLong("duration", it * 1000L)
                        putLong("android.media.metadata.DURATION", it * 1000L)
                    }
                }
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
            .setArtworkUri(thumbnailUrl?.takeIf { it.isNotBlank() }?.toUri())
            .setDurationMs(duration.takeIf { it > 0 }?.times(1000L))
            .setAlbumTitle(album?.title)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                thumbnailUrl?.takeIf { it.isNotBlank() }?.let { putString("artwork_uri", it) }
                if (duration > 0) {
                    putLong("duration", duration * 1000L)
                    putLong("android.media.metadata.DURATION", duration * 1000L)
                }
            })
            .build()
    )
    .build()
