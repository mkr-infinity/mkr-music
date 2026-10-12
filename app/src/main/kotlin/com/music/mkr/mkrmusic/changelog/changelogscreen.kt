package com.music.mkr.mkrmusic.changelog

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.R
import com.music.mkr.ui.utils.safeOpenUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val PROJECT_POSTER_URL = "https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png"

/**
 * MKR Music Changelog & Release Notes Screen.
 * Renders the official project poster first, followed by Markdown-formatted release notes.
 * Directly features access to commit history without version chips or local cache files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var releaseMarkdown by remember { mutableStateOf<String?>(null) }
    var releaseTag by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    fun fetchLatestReleaseNotes() {
        isLoading = true
        hasError = false
        coroutineScope.launch(Dispatchers.IO) {
            try {
                var releaseBody: String? = null
                var tag: String? = null

                // 1. Primary Priority: GitHub Releases API (latest release)
                try {
                    val url = URL("https://api.github.com/repos/mkr-infinity/mkr-music/releases/latest")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "MKRMusic-App")
                    connection.setRequestProperty("Accept", "application/vnd.github+json")
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000

                    if (connection.responseCode == 200) {
                        val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(jsonStr)
                        val body = json.optString("body", "").trim()
                        if (body.isNotBlank()) {
                            releaseBody = body
                            tag = json.optString("tag_name", "").trim()
                        }
                    }
                } catch (_: Exception) {}

                // If latest release returned empty body or 404, check releases list
                if (releaseBody.isNullOrBlank()) {
                    try {
                        val url = URL("https://api.github.com/repos/mkr-infinity/mkr-music/releases")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.setRequestProperty("User-Agent", "MKRMusic-App")
                        connection.setRequestProperty("Accept", "application/vnd.github+json")
                        connection.connectTimeout = 8000
                        connection.readTimeout = 8000

                        if (connection.responseCode == 200) {
                            val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                            val jsonArr = JSONArray(jsonStr)
                            for (i in 0 until jsonArr.length()) {
                                val item = jsonArr.getJSONObject(i)
                                val body = item.optString("body", "").trim()
                                if (body.isNotBlank()) {
                                    releaseBody = body
                                    tag = item.optString("tag_name", "").trim()
                                    break
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                // 2. Secondary Priority: Fallback to raw CHANGELOG.md if no release notes available
                if (releaseBody.isNullOrBlank()) {
                    try {
                        val changelogUrl = URL("https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/CHANGELOG.md")
                        val changelogConn = changelogUrl.openConnection() as HttpURLConnection
                        changelogConn.setRequestProperty("User-Agent", "MKRMusic-App")
                        changelogConn.connectTimeout = 8000
                        changelogConn.readTimeout = 8000

                        if (changelogConn.responseCode == 200) {
                            val md = changelogConn.inputStream.bufferedReader().use { it.readText() }.trim()
                            if (md.isNotBlank()) {
                                releaseBody = md
                            }
                        }
                    } catch (_: Exception) {}
                }

                withContext(Dispatchers.Main) {
                    releaseTag = tag
                    releaseMarkdown = if (!releaseBody.isNullOrBlank()) releaseBody else getDefaultReleaseNotes()
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Fallback to bundled notes on network failure so user always sees release notes
                    releaseMarkdown = getDefaultReleaseNotes()
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchLatestReleaseNotes()
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = isLoading,
            onRefresh = { fetchLatestReleaseNotes() }
        ),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.changelog_title)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    // Commits action button
                    IconButton(onClick = { navController.navigate("settings/commits") }) {
                        Icon(
                            painter = painterResource(R.drawable.commit),
                            contentDescription = stringResource(R.string.commits),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // 1. Project Poster First
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(PROJECT_POSTER_URL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "MKR Music Project Poster",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        error = painterResource(R.drawable.icon)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Release Notes Markdown Preview
                if (isLoading && releaseMarkdown == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    MarkdownContent(
                        markdown = releaseMarkdown ?: getDefaultReleaseNotes(),
                        onLinkClick = { url -> uriHandler.safeOpenUri(context, url) }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }

            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Beautiful, lightweight Markdown preview renderer tailored for release notes.
 * Fenced code blocks are excluded so the view stays purely user-facing preview text.
 */
@Composable
private fun MarkdownContent(
    markdown: String,
    onLinkClick: (String) -> Unit
) {
    val cleanLines = remember(markdown) {
        val list = mutableListOf<String>()
        var inCodeFence = false
        for (line in markdown.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                inCodeFence = !inCodeFence
                continue
            }
            if (inCodeFence) {
                continue // Skip lines inside code fences
            }
            list.add(line)
        }
        list
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (line in cleanLines) {
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").replace("`", "").trim(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").replace("`", "").trim(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").replace("`", "").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val bulletText = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        ClickableMarkdownText(
                            rawText = bulletText,
                            onLinkClick = onLinkClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.startsWith("![](") || trimmed.startsWith("![") -> {
                    // Skip inline markdown images; top banner handles visual branding
                }
                else -> {
                    ClickableMarkdownText(
                        rawText = trimmed,
                        onLinkClick = onLinkClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ClickableMarkdownText(
    rawText: String,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val sanitizedText = remember(rawText) { rawText.replace("`", "") }
    val linkRegex = remember { Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)") }
    val boldRegex = remember { Regex("\\*\\*([^*]+)\\*\\*") }

    val annotated = remember(sanitizedText, primaryColor, onSurfaceColor) {
        buildAnnotatedString {
            var currentIndex = 0
            val matches = linkRegex.findAll(sanitizedText).toList()

            if (matches.isEmpty()) {
                // Parse bold tokens
                var textIndex = 0
                val boldMatches = boldRegex.findAll(sanitizedText).toList()
                if (boldMatches.isEmpty()) {
                    append(sanitizedText)
                } else {
                    for (bMatch in boldMatches) {
                        if (bMatch.range.first > textIndex) {
                            append(sanitizedText.substring(textIndex, bMatch.range.first))
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
                            append(bMatch.groupValues[1])
                        }
                        textIndex = bMatch.range.last + 1
                    }
                    if (textIndex < sanitizedText.length) {
                        append(sanitizedText.substring(textIndex))
                    }
                }
            } else {
                for (match in matches) {
                    if (match.range.first > currentIndex) {
                        append(sanitizedText.substring(currentIndex, match.range.first))
                    }
                    val label = match.groupValues[1]
                    val url = match.groupValues[2]
                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(label)
                    }
                    pop()
                    currentIndex = match.range.last + 1
                }
                if (currentIndex < sanitizedText.length) {
                    append(sanitizedText.substring(currentIndex))
                }
            }
        }
    }

    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        ),
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onLinkClick(annotation.item)
                }
        }
    )
}

private fun getDefaultReleaseNotes(): String {
    return """
## ✨ What's New in MKR Music

- ⚡ **Instant Playback Start**: Direct low-latency audio streaming without pre-caching buffer lag.
- ⏱️ **Delayed Song Caching**: Automatically starts background caching only after listening for your configured duration (5s–90s).
- 🎵 **Adaptive Sound Quality**: Dynamic bitrate reporting with exact kbps indicator in the player.
- ⚙️ **Granular Settings Search**: Search any keyword directly to quickly find specific options with zero duplicates.
- 🚀 **Manual Update Workflow**: Seamless GitHub release updates with built-in backup instructions.
- 🎨 **Visual Refinements**: Beautiful streamlined UI with responsive banners and smooth transitions.
    """.trimIndent()
}