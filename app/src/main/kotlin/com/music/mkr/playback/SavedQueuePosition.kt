package com.music.mkr.playback

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.withTimeout

internal fun <T> CoroutineScope.requestPlaybackResumption(resolve: suspend () -> T): ListenableFuture<T> =
    future { withTimeout(5_000L) { resolve() } }

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
