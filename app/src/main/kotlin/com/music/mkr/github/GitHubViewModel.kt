package com.music.mkr.github

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.mkr.constants.HasStarredRepoKey
import com.music.mkr.github.network.GitHubCommitDto
import com.music.mkr.github.network.GitHubReleaseDto
import com.music.mkr.github.network.GitHubRepoDto
import com.music.mkr.github.network.GitHubService
import com.music.mkr.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GitHubViewModel @Inject constructor(
    private val gitHubService: GitHubService
) : ViewModel() {

    private val _isStarred = MutableStateFlow(false)
    val isStarred: StateFlow<Boolean> = _isStarred.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showThankYouDialog = MutableStateFlow(false)
    val showThankYouDialog: StateFlow<Boolean> = _showThankYouDialog.asStateFlow()

    private val _repoDetails = MutableStateFlow<GitHubRepoDto?>(null)
    val repoDetails: StateFlow<GitHubRepoDto?> = _repoDetails.asStateFlow()

    private val _starCount = MutableStateFlow<Int?>(null)
    val starCount: StateFlow<Int?> = _starCount.asStateFlow()

    private val _forksCount = MutableStateFlow<Int?>(null)
    val forksCount: StateFlow<Int?> = _forksCount.asStateFlow()

    private val _issuesCount = MutableStateFlow<Int?>(null)
    val issuesCount: StateFlow<Int?> = _issuesCount.asStateFlow()

    private val _recentCommits = MutableStateFlow<List<GitHubCommitDto>>(emptyList())
    val recentCommits: StateFlow<List<GitHubCommitDto>> = _recentCommits.asStateFlow()

    private val _releases = MutableStateFlow<List<GitHubReleaseDto>>(emptyList())
    val releases: StateFlow<List<GitHubReleaseDto>> = _releases.asStateFlow()

    fun fetchRepoStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val details = gitHubService.getRepoDetails()
                if (details != null) {
                    _repoDetails.value = details
                    _starCount.value = details.stargazersCount
                    _forksCount.value = details.forksCount
                    _issuesCount.value = details.openIssuesCount
                }
                val commits = gitHubService.getRecentCommits(limit = 10)
                _recentCommits.value = commits

                val releasesList = gitHubService.getReleases()
                _releases.value = releasesList
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStarCount() {
        fetchRepoStats()
    }

    fun dismissThankYouDialog() {
        _showThankYouDialog.value = false
    }

    fun checkStarStatus(context: Context) {
        viewModelScope.launch {
            _isStarred.value = context.dataStore[HasStarredRepoKey] ?: false
        }
    }

    fun toggleStar(
        context: Context,
        onNavigateToRepo: () -> Unit
    ) {
        viewModelScope.launch {
            if (_isStarred.value) return@launch

            _isStarred.value = true
            _showThankYouDialog.value = true
            context.dataStore.edit { preferences ->
                preferences[HasStarredRepoKey] = true
            }
            onNavigateToRepo()
        }
    }
}
