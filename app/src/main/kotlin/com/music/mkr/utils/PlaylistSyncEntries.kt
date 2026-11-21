package com.music.mkr.utils

import com.music.mkr.db.entities.PlaylistSongMap

internal fun unmatchedLocalPlaylistEntries(
    local: List<PlaylistSongMap>,
    remote: List<Pair<String, String?>>,
): List<PlaylistSongMap> {
    val knownRemoteIds = local.mapNotNull { it.setVideoId }.toSet()
    val unclaimed = remote
        .filter { (_, remoteId) -> remoteId !in knownRemoteIds }
        .groupingBy { it.first }
        .eachCount()
        .toMutableMap()
    return local.filter { entry ->
        if (entry.setVideoId != null) return@filter false
        val available = unclaimed[entry.songId] ?: 0
        if (available == 0) return@filter true
        unclaimed[entry.songId] = available - 1
        false
    }
}
