/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.utils.YouTubeUrlParser
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalIsPlayerExpanded
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.constants.PauseSearchHistoryKey
import com.music.mkr.constants.SearchSource
import com.music.mkr.constants.SearchSourceKey
import com.music.mkr.db.entities.SearchHistory
import com.music.mkr.playback.queues.YouTubeQueue
import com.music.mkr.ui.screens.search.suggestions.SuggestionsTabContent
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun SearchScreen(
    navController: NavController,
    pureBlack: Boolean
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isPlayerExpanded = LocalIsPlayerExpanded.current
    val playerConnection = LocalPlayerConnection.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var searchSource by rememberEnumPreference(SearchSourceKey, SearchSource.ONLINE)
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)
    var searchActive by rememberSaveable { mutableStateOf(false) }

    val searchHistory by database.searchHistory().collectAsState(initial = emptyList())
    val focusRequester = remember { FocusRequester() }

    // Dismiss active search overlay on back gesture
    BackHandler(enabled = searchActive) {
        searchActive = false
        focusManager.clearFocus()
    }

    val onSearch: (String) -> Unit = remember {
        { searchQuery ->
            if (searchQuery.isNotBlank()) {
                focusManager.clearFocus()
                keyboardController?.hide()
                searchActive = false

                when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
                    is YouTubeUrlParser.ParsedUrl.Video -> {
                        playerConnection?.playQueue(
                            YouTubeQueue(
                                WatchEndpoint(videoId = parsedUrl.id),
                            ),
                        )
                    }

                    is YouTubeUrlParser.ParsedUrl.Artist -> {
                        navController.navigate("artist/${parsedUrl.id}")
                    }

                    null -> {
                        navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
                    }
                }

                if (!pauseSearchHistory) {
                    coroutineScope.launch(Dispatchers.IO) {
                        database.query {
                            insert(SearchHistory(query = searchQuery))
                        }
                    }
                }
            }
        }
    }

    val onSearchFromSuggestion: (String) -> Unit = remember {
        { searchQuery ->
            if (searchQuery.isNotBlank()) {
                focusManager.clearFocus()
                keyboardController?.hide()
                searchActive = false

                when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
                    is YouTubeUrlParser.ParsedUrl.Video -> {
                        playerConnection?.playQueue(
                            YouTubeQueue(
                                WatchEndpoint(videoId = parsedUrl.id),
                            ),
                        )
                    }

                    is YouTubeUrlParser.ParsedUrl.Artist -> {
                        navController.navigate("artist/${parsedUrl.id}")
                    }

                    null -> {
                        navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
                    }
                }

                if (!pauseSearchHistory) {
                    coroutineScope.launch(Dispatchers.IO) {
                        database.query {
                            insert(SearchHistory(query = searchQuery))
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                MusicSearchHeader(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = onSearch,
                    active = searchActive,
                    onActiveChange = { searchActive = it },
                    searchSource = searchSource,
                    onSearchSourceChange = { searchSource = it },
                    pureBlack = pureBlack,
                    focusRequester = focusRequester,
                    showBackButton = searchActive,
                    onBackClick = {
                        searchActive = false
                        query = TextFieldValue("")
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            }
        },
        containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val bottomPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {
            val isSearchingWithQuery = searchActive && query.text.isNotEmpty()

            AnimatedContent(
                targetState = isSearchingWithQuery,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SearchScreenContentTransition"
            ) { searching ->
                if (searching) {
                    when (searchSource) {
                        SearchSource.LOCAL -> LocalSearchScreen(
                            query = query.text,
                            navController = navController,
                            onDismiss = {
                                searchActive = false
                                focusManager.clearFocus()
                            },
                            pureBlack = pureBlack
                        )
                        SearchSource.ONLINE -> OnlineSearchScreen(
                            query = query.text,
                            onQueryChange = { query = it },
                            navController = navController,
                            onSearch = {
                                onSearchFromSuggestion(it)
                                searchActive = false
                            },
                            onDismiss = {
                                searchActive = false
                                focusManager.clearFocus()
                            },
                            pureBlack = pureBlack
                        )
                    }
                } else {
                    val tabPadding = PaddingValues(bottom = bottomPadding + 88.dp)
                    SuggestionsTabContent(
                        navController = navController,
                        contentPadding = tabPadding,
                        headerContent = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (searchHistory.isNotEmpty()) {
                                    RecentSearchesSection(
                                        history = searchHistory,
                                        onQueryClick = { clickedQuery ->
                                            query = TextFieldValue(clickedQuery, TextRange(clickedQuery.length))
                                            onSearch(clickedQuery)
                                        },
                                        onDelete = { item ->
                                            coroutineScope.launch(Dispatchers.IO) {
                                                database.query { delete(item) }
                                            }
                                        },
                                        onClearAll = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val items = database.searchHistory().first()
                                                database.query {
                                                    items.forEach(::delete)
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                ExploreGenresGrid(
                                    onGenreClick = { genreQuery ->
                                        query = TextFieldValue(genreQuery, TextRange(genreQuery.length))
                                        onSearch(genreQuery)
                                    }
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    )
                }
            }
        }
    }

    // Lifecycle handling for focus and soft keyboard
    DisposableEffect(lifecycleOwner, isPlayerExpanded) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlayerExpanded) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (isPlayerExpanded) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
