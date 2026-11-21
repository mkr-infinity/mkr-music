package com.music.mkr.playback

internal fun isOfflineDownloadReady(
    memberId: String,
    completedCacheKeys: Map<String, String>,
    readableEntries: Map<String, Long>,
): Boolean = completedCacheKeys[memberId]?.let(readableEntries::containsKey) == true
