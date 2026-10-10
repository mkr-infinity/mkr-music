package com.music.mkr.github.network

import com.music.mkr.github.GitHubConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

@Serializable
data class GitHubRepoDto(
    @SerialName("stargazers_count") val stargazersCount: Int = 0,
    @SerialName("forks_count") val forksCount: Int = 0,
    @SerialName("open_issues_count") val openIssuesCount: Int = 0,
    val description: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null
)

@Serializable
data class GitHubCommitAuthor(
    val name: String? = null,
    val date: String? = null
)

@Serializable
data class GitHubCommitInfo(
    val message: String? = null,
    val author: GitHubCommitAuthor? = null
)

@Serializable
data class GitHubCommitDto(
    val sha: String = "",
    val commit: GitHubCommitInfo? = null,
    @SerialName("html_url") val htmlUrl: String? = null
)

@Serializable
data class GitHubReleaseDto(
    val id: Long = 0,
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    val prerelease: Boolean = false
)

@Serializable
data class GitHubTokenRequest(
    val client_id: String,
    val client_secret: String,
    val code: String
)

@Serializable
data class GitHubTokenResponse(
    val access_token: String,
    val scope: String,
    val token_type: String
)

class GitHubService @Inject constructor() {

    private val client = HttpClient(OkHttp) {
        engine {
            preconfigured = OkHttpClient()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getAccessToken(clientId: String, clientSecret: String, code: String): String? {
        return try {
            val result = client.post("https://github.com/login/oauth/access_token") {
                header("Accept", "application/json")
                contentType(ContentType.Application.Json)
                setBody(
                    GitHubTokenRequest(
                        client_id = clientId,
                        client_secret = clientSecret,
                        code = code
                    )
                )
            }
            if (result.status == HttpStatusCode.OK) {
                val response = result.body<GitHubTokenResponse>()
                response.access_token
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "GitHub OAuth token fetch failed")
            null
        }
    }

    suspend fun isRepoStarred(
        token: String,
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME
    ): Boolean {
        return try {
            val response: HttpResponse = client.get("https://api.github.com/user/starred/$owner/$repo") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            response.status == HttpStatusCode.NoContent
        } catch (e: Exception) {
            Timber.e(e, "Check repo starred failed")
            false
        }
    }

    suspend fun starRepo(
        token: String,
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME
    ): Boolean {
        return try {
            val response = client.put("https://api.github.com/user/starred/$owner/$repo") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            response.status == HttpStatusCode.NoContent
        } catch (e: Exception) {
            Timber.e(e, "Star repo failed")
            false
        }
    }

    suspend fun unstarRepo(
        token: String,
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME
    ): Boolean {
        return try {
            val response = client.delete("https://api.github.com/user/starred/$owner/$repo") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            response.status == HttpStatusCode.NoContent
        } catch (e: Exception) {
            Timber.e(e, "Unstar repo failed")
            false
        }
    }

    suspend fun getRepoDetails(
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME
    ): GitHubRepoDto? {
        return try {
            val response = client.get("https://api.github.com/repos/$owner/$repo") {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<GitHubRepoDto>()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Fetch repo details failed")
            null
        }
    }

    suspend fun getRecentCommits(
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME,
        limit: Int = 10
    ): List<GitHubCommitDto> {
        return try {
            val response = client.get("https://api.github.com/repos/$owner/$repo/commits?per_page=$limit") {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<GitHubCommitDto>>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Fetch recent commits failed")
            emptyList()
        }
    }

    suspend fun getReleases(
        owner: String = GitHubConfig.REPO_OWNER,
        repo: String = GitHubConfig.REPO_NAME
    ): List<GitHubReleaseDto> {
        return try {
            val response = client.get("https://api.github.com/repos/$owner/$repo/releases?per_page=5") {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<GitHubReleaseDto>>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Fetch releases failed")
            emptyList()
        }
    }
}
