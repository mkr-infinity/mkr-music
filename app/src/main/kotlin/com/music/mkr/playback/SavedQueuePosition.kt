package com.music.mkr.playback

internal fun restoredQueueIndex(savedIndex: Int, retainedIndices: List<Int>): Int {
    require(retainedIndices.isNotEmpty())
    val next = retainedIndices.indexOfFirst { it >= savedIndex }
    return if (next >= 0) next else retainedIndices.lastIndex
}
