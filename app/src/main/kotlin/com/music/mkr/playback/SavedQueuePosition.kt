package com.music.mkr.playback

internal fun restoredQueueIndex(savedIndex: Int, retainedIndices: List<Int>): Int {
    require(retainedIndices.isNotEmpty())
    val next = retainedIndices.indexOfFirst { it >= savedIndex }
    return if (next >= 0) next else retainedIndices.lastIndex
}

internal fun remapSavedShuffleOrder(order: IntArray?, retainedIndices: List<Int>): IntArray? {
    if (order == null || retainedIndices.isEmpty()) return null
    val currentIndices = retainedIndices.withIndex().associate { it.value to it.index }
    val mapped = order.asSequence().mapNotNull { currentIndices[it] }.toList()
    return mapped.takeIf { it.size == currentIndices.size && it.toSet().size == it.size }?.toIntArray()
}
