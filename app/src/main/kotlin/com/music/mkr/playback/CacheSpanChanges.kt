/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheEvictor
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.SimpleCache
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow

@androidx.annotation.OptIn(UnstableApi::class)
@Singleton
class CacheSpanChanges @Inject constructor() {
    private val listeners = CopyOnWriteArrayList<(Cache, String) -> Unit>()

    fun observe(evictor: CacheEvictor): CacheEvictor = object : CacheEvictor by evictor {
        override fun onSpanAdded(cache: Cache, span: CacheSpan) {
            evictor.onSpanAdded(cache, span)
            listeners.forEach { it(cache, span.key) }
        }

        override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
            evictor.onSpanRemoved(cache, span)
            listeners.forEach { it(cache, span.key) }
        }
    }

    // Each observer takes one snapshot, then rechecks only keys affected by span changes.
    fun completeEntries(cache: SimpleCache) = flow {
        val pendingKeys = ConcurrentHashMap.newKeySet<String>()
        val changed = Channel<Unit>(Channel.CONFLATED)
        val listener: (Cache, String) -> Unit = { source, key ->
            if (source === cache) {
                pendingKeys.add(key)
                changed.trySend(Unit)
            }
        }
        val complete = mutableMapOf<String, Long>()
        fun refresh(key: String): Boolean {
            val cachedAt = cache.completeAudioCachedAt(key)
            val previous = if (cachedAt == null) complete.remove(key) else complete.put(key, cachedAt)
            return previous != cachedAt
        }

        listeners.add(listener)
        try {
            cache.checkInitialization()
            cache.keys.forEach { refresh(it) }
            emit(complete.toMap())
            for (ignored in changed) {
                var entriesChanged = false
                for (key in pendingKeys.toList()) {
                    if (pendingKeys.remove(key) && refresh(key)) entriesChanged = true
                }
                if (entriesChanged) emit(complete.toMap())
            }
        } finally {
            listeners.remove(listener)
            changed.close()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
internal fun SimpleCache.completeAudioCachedAt(key: String): Long? {
    val resourceLength = ContentMetadata.getContentLength(getContentMetadata(key))
    if (resourceLength <= 0 || !isCached(key, 0, resourceLength)) return null

    var covered = 0L
    var cachedAt = 0L
    for (span in getCachedSpans(key)) {
        if (span.position > covered) return null
        val file = span.file ?: return null
        if (!file.isFile || !file.canRead() || file.length() != span.length) return null
        cachedAt = maxOf(cachedAt, file.lastModified())
        covered = maxOf(covered, span.position + span.length)
        if (covered >= resourceLength) return cachedAt
    }
    return null
}
