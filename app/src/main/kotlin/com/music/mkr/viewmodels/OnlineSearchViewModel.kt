/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.filterYoutubeShorts
import com.music.innertube.pages.SearchSummaryPage
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.constants.HideYoutubeShortsKey
import com.music.mkr.models.ItemsPage
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import com.music.mkr.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSearchViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query = savedStateHandle.get<String>("query").orEmpty()
    private val _filter = MutableStateFlow(savedStateHandle.get<String>("search_filter")?.let { YouTube.SearchFilter(it) })
    val filter = _filter.asStateFlow()

    fun selectFilter(selected: YouTube.SearchFilter?) {
        savedStateHandle["search_filter"] = selected?.value
        _filter.value = selected
    }
    var summaryPage by mutableStateOf<SearchSummaryPage?>(null)
    var summaryMatch by mutableStateOf<SongItem?>(null)
        private set
    var songsMatch by mutableStateOf<SongItem?>(null)
        private set
    val viewStateMap = mutableStateMapOf<String, ItemsPage?>()

    private val failedInitial = mutableStateMapOf<YouTube.SearchFilter?, Boolean>()
    private val failedPages = mutableStateMapOf<String, String>()
    private val initialJobs = mutableSetOf<YouTube.SearchFilter?>()
    private val loadingPages = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            filter.collectLatest { selected ->
                if (selected == null && summaryPage == null ||
                    selected != null && viewStateMap[selected.value] == null
                ) loadInitial(selected)
            }
        }
    }

    fun initialFailed(selected: YouTube.SearchFilter?) = failedInitial[selected] == true

    fun pageFailed(selected: YouTube.SearchFilter?, continuation: String?) =
        selected != null && continuation != null && failedPages[selected.value] == continuation

    fun retryInitial() {
        val selected = filter.value
        if (!initialFailed(selected)) return
        failedInitial.remove(selected)
        viewModelScope.launch { loadInitial(selected) }
    }

    private suspend fun loadInitial(selected: YouTube.SearchFilter?) {
        if (!initialJobs.add(selected)) return
        failedInitial.remove(selected)
        try {
            if (query.isBlank()) {
                if (selected == null) summaryPage = SearchSummaryPage(emptyList())
                else viewStateMap[selected.value] = ItemsPage(emptyList(), null)
                return
            }
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideShorts = context.dataStore.get(HideYoutubeShortsKey, false)
            if (selected == null) {
                val page = YouTube.searchSummary(query).getOrThrow()
                    .filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs).filterYoutubeShorts(hideShorts)
                val match = withContext(Dispatchers.Default) {
                    topSongMatch(query, page.summaries.flatMap { it.items })
                }
                coroutineContext.ensureActive()
                summaryMatch = match
                summaryPage = page
            } else {
                val result = YouTube.search(query, selected).getOrThrow()
                val items = result.items.distinctBy { it.id }
                    .filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs).filterYoutubeShorts(hideShorts)
                val match = if (selected == YouTube.SearchFilter.FILTER_SONG) {
                    withContext(Dispatchers.Default) { topSongMatch(query, items, songsOnly = true) }
                } else null
                coroutineContext.ensureActive()
                if (selected == YouTube.SearchFilter.FILTER_SONG) songsMatch = match
                viewStateMap[selected.value] = ItemsPage(items, result.continuation)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failedInitial[selected] = true
            reportException(e)
        } finally {
            initialJobs.remove(selected)
        }
    }

    fun loadMore(selected: YouTube.SearchFilter?, continuation: String, retry: Boolean = false) {
        if (selected == null || selected != filter.value) return
        val key = selected.value
        val page = viewStateMap[key] ?: return
        if (page.continuation != continuation || (!retry && pageFailed(selected, continuation))) return
        if (!loadingPages.add(continuation)) return
        failedPages.remove(key)
        viewModelScope.launch {
            try {
                val result = YouTube.searchContinuation(continuation).getOrThrow()
                check(result.continuation != continuation) { "Search returned the same continuation" }
                val newItems = result.items
                    .filterExplicit(context.dataStore.get(HideExplicitKey, false))
                    .filterVideoSongs(context.dataStore.get(HideVideoSongsKey, false))
                    .filterYoutubeShorts(context.dataStore.get(HideYoutubeShortsKey, false))
                val match = if (selected == YouTube.SearchFilter.FILTER_SONG) {
                    val candidates = listOfNotNull(songsMatch) + newItems
                    withContext(Dispatchers.Default) { topSongMatch(query, candidates, songsOnly = true) }
                } else null
                ensureActive()
                if (viewStateMap[key]?.continuation != continuation) return@launch
                if (selected == YouTube.SearchFilter.FILTER_SONG) songsMatch = match
                viewStateMap[key] = ItemsPage(
                    (page.items + newItems).distinctBy { it.id },
                    result.continuation,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failedPages[key] = continuation
                reportException(e)
            } finally {
                loadingPages.remove(continuation)
            }
        }
    }
}
