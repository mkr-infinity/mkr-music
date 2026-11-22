/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.utils.YouTubeUrlParser
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.db.MusicDatabase
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.music.mkr.db.entities.EventWithSong
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                query
                    .flatMapLatest { query ->
                        if (query.isBlank()) {
                            database.searchHistory().map { history ->
                                SearchSuggestionViewState(
                                    query = query,
                                    history = history,
                                )
                            }
                        } else {
                            val parsedUrl = YouTubeUrlParser.parse(query.trim())
                            if (parsedUrl == null) delay(300)
                            val parsedItem = if (parsedUrl != null) fetchParsedUrlItem(parsedUrl) else null
                            
                            val result = if (parsedUrl != null) null else YouTube.searchSuggestions(query.trim()).getOrNull()
                            coroutineContext.ensureActive()
                            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)

                            database
                                .searchHistory(query.trim())
                                .map { it.take(3) }
                                .map { history ->
                                    SearchSuggestionViewState(
                                        query = query,
                                        history = history,
                                        suggestions =
                                        result
                                            ?.queries
                                            ?.filter { suggestionQuery ->
                                                history.none { it.query == suggestionQuery }
                                            }.orEmpty(),
                                        items = listOfNotNull(parsedItem) +
                                        result
                                            ?.recommendedItems
                                            ?.distinctBy { it.id }
                                            ?.filter { it.id != parsedItem?.id }
                                            ?.filterExplicit(hideExplicit)
                                            ?.filterVideoSongs(hideVideoSongs)
                                            .orEmpty(),
                                        isFromLink = parsedUrl != null
                                    )
                                }
                        }
                    },
                context.dataStore.data
                    .map { prefs ->
                        val ids = prefs[com.music.mkr.constants.SearchListenHistoryKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                        ids.take(9).mapNotNull { id ->
                            database.getSongByIdBlocking(id)?.let { song ->
                                EventWithSong(
                                    com.music.mkr.db.entities.Event(songId = id, timestamp = java.time.LocalDateTime.now(), playTime = 0),
                                    song
                                )
                            }
                        }
                    }
                    .flowOn(kotlinx.coroutines.Dispatchers.IO)
            ) { state, recentEvents ->
                state.copy(recentEvents = recentEvents)
            }.collect {
                _viewState.value = it
            }
        }
    }

    private suspend fun fetchParsedUrlItem(parsedUrl: YouTubeUrlParser.ParsedUrl): YTItem? {
        println("[LINK_PARSE_DEBUG] Fetching metadata for: $parsedUrl")
        return try {
            val item = when (parsedUrl) {
                is YouTubeUrlParser.ParsedUrl.Video -> {
                    YouTube.queue(listOf(parsedUrl.id)).getOrNull()?.firstOrNull()
                }

                is YouTubeUrlParser.ParsedUrl.Artist -> {
                    YouTube.artist(parsedUrl.id).getOrNull()?.artist
                }
            }
            println("[LINK_PARSE_DEBUG] Fetch successful: ${item?.id} (${item?.javaClass?.simpleName})")
            item
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("[LINK_PARSE_DEBUG] Fetch failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

data class SearchSuggestionViewState(
    val query: String = "",
    val history: List<SearchHistory> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
    val recentEvents: List<EventWithSong> = emptyList(),
    val isFromLink: Boolean = false,
) {
    fun forQuery(query: String): SearchSuggestionViewState =
        if (this.query == query) this else SearchSuggestionViewState(query = query, recentEvents = recentEvents)
}
