/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.pages.BrowseResult
import com.music.mkr.constants.HideExplicitKey
import com.music.mkr.constants.HideVideoSongsKey
import com.music.mkr.constants.HideYoutubeShortsKey
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import com.music.mkr.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YouTubeBrowseViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val browseId = savedStateHandle.get<String>("browseId")?.takeIf { it.isNotBlank() && it != "null" }
    private val params = savedStateHandle.get<String>("params")?.takeIf { it.isNotBlank() && it != "null" }

    val result = MutableStateFlow<BrowseResult?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        load()
    }

    fun load() {
        val targetBrowseId = browseId ?: run {
            error.value = "Invalid browse ID"
            return
        }
        error.value = null
        viewModelScope.launch {
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
            YouTube
                .browse(targetBrowseId, params)
                .onSuccess {
                    result.value = it
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                }.onFailure {
                    error.value = it.localizedMessage ?: "Failed to load"
                    reportException(it)
                }
        }
    }
}
