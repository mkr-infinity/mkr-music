/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.mkr.BuildConfig
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.R
import com.music.mkr.github.GitHubConfig
import com.music.mkr.github.GitHubViewModel
import com.music.mkr.github.network.GitHubCommitDto
import com.music.mkr.ui.component.DialogBasic
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.safeOpenUri
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: (() -> Unit)? = null,
    gitHubViewModel: GitHubViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        gitHubViewModel.fetchRepoStats()
    }

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }

    val starCount by gitHubViewModel.starCount.collectAsState()
    val forksCount by gitHubViewModel.forksCount.collectAsState()
    val issuesCount by gitHubViewModel.issuesCount.collectAsState()
    val recentCommits by gitHubViewModel.recentCommits.collectAsState()

    val installedDate = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val installTime = packageInfo.firstInstallTime
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(installTime))
        } catch (_: Exception) {
            "Installed"
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Hero Section: Developer Profile & Branding
        DeveloperHeroCard(
            onDeveloperClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_GITHUB_URL) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // App Version Card
        AppVersionTile(
            appName = "MKR Music",
            versionLabel = GitHubConfig.VERSION_LABEL,
            buildDetails = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • ${if (BuildConfig.IS_NIGHTLY) "Nightly" else "Stable"}",
            onGithubClick = { uriHandler.safeOpenUri(context, GitHubConfig.REPO_URL) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Live GitHub Repo Metrics
        Text(
            text = "Repository Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        RepoMetricsRow(
            stars = starCount,
            forks = forksCount,
            issues = issuesCount,
            onStarsClick = { uriHandler.safeOpenUri(context, GitHubConfig.REPO_URL) },
            onForksClick = { uriHandler.safeOpenUri(context, "${GitHubConfig.REPO_URL}/network/members") },
            onIssuesClick = { uriHandler.safeOpenUri(context, "${GitHubConfig.REPO_URL}/issues") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Commits Section
        if (recentCommits.isNotEmpty()) {
            RecentCommitsSection(
                commits = recentCommits.take(5),
                onCommitClick = { commit ->
                    val url = commit.htmlUrl ?: "${GitHubConfig.REPO_URL}/commit/${commit.sha}"
                    uriHandler.safeOpenUri(context, url)
                },
                onViewAllClick = {
                    uriHandler.safeOpenUri(context, "${GitHubConfig.REPO_URL}/commits")
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Connect & Developer Links
        ExpressiveSettingGroup(
            title = "Connect & Support",
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.github),
                    title = { Text("Developer GitHub") },
                    description = { Text(GitHubConfig.DEV_HANDLE) },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_GITHUB_URL) },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.github_main),
                    title = { Text("Official Repository") },
                    description = { Text("mkr-infinity/mkr-music") },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.REPO_URL) },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.buymeacoffee),
                    title = { Text("Support & Donate") },
                    description = { Text("Buy Me a Coffee (@mkr_infinity)") },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_BUYMEACOFFEE_URL) },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.web_link),
                    title = { Text("Developer Website") },
                    description = { Text("mkr-infinity.github.io") },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_WEBSITE_URL) },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.telegram),
                    title = { Text("Telegram Channel") },
                    description = { Text("t.me/mkr_infinity") },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_TELEGRAM_URL) },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.instagram),
                    title = { Text("Instagram") },
                    description = { Text("@mkr_infinity") },
                    onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_INSTAGRAM_URL) },
                    isExternalLink = true
                )
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Application Details & Legal
        ExpressiveSettingGroup(
            title = "App Information",
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.deployed_app_update),
                    title = { Text(stringResource(R.string.installed_date_title)) },
                    trailingContent = { Text(installedDate) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.info),
                    title = { Text(stringResource(R.string.version_code)) },
                    trailingContent = { Text(BuildConfig.VERSION_CODE.toString()) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.license_mkr),
                    title = { Text("License (GPL-3.0)") },
                    description = { Text("GNU General Public License v3.0") },
                    onClick = { uriHandler.safeOpenUri(context, "${GitHubConfig.REPO_URL}/blob/main/LICENSE") },
                    isExternalLink = true
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.github_main),
                    title = { Text("Upstream Project") },
                    description = { Text("Based on ViVi Music (GPL-3.0)") },
                    onClick = { uriHandler.safeOpenUri(context, "https://github.com/vivizzz007/vivi-music") },
                    isExternalLink = true
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    TopAppBar(
        title = { Text("About MKR Music") },
        navigationIcon = {
            IconButton(
                onClick = { onBack?.invoke() ?: navController.navigateUp() },
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}

@Composable
private fun DeveloperHeroCard(
    onDeveloperClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDeveloperClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(GitHubConfig.DEV_AVATAR_URL)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.dev),
                    error = painterResource(R.drawable.dev),
                    contentDescription = GitHubConfig.DEV_NAME,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = GitHubConfig.DEV_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = GitHubConfig.DEV_HANDLE,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = GitHubConfig.DEV_ROLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppVersionTile(
    appName: String,
    versionLabel: String,
    buildDetails: String,
    onGithubClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(R.drawable.mkr_music_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = versionLabel,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            supportingContent = {
                Text(
                    text = buildDetails,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onGithubClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.github),
                        contentDescription = "GitHub Repository",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )
    }
}

@Composable
private fun RepoMetricsRow(
    stars: Int?,
    forks: Int?,
    issues: Int?,
    onStarsClick: () -> Unit,
    onForksClick: () -> Unit,
    onIssuesClick: () -> Unit
) {
    val animatedStars by animateIntAsState(
        targetValue = stars ?: 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "starsAnimation"
    )
    val animatedForks by animateIntAsState(
        targetValue = forks ?: 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "forksAnimation"
    )
    val animatedIssues by animateIntAsState(
        targetValue = issues ?: 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "issuesAnimation"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            label = "Stars",
            count = if (stars != null) NumberFormat.getInstance().format(animatedStars) else "—",
            iconRes = R.drawable.star,
            modifier = Modifier.weight(1f),
            onClick = onStarsClick
        )
        MetricCard(
            label = "Forks",
            count = if (forks != null) NumberFormat.getInstance().format(animatedForks) else "—",
            iconRes = R.drawable.network_node,
            modifier = Modifier.weight(1f),
            onClick = onForksClick
        )
        MetricCard(
            label = "Issues",
            count = if (issues != null) NumberFormat.getInstance().format(animatedIssues) else "—",
            iconRes = R.drawable.info,
            modifier = Modifier.weight(1f),
            onClick = onIssuesClick
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    count: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentCommitsSection(
    commits: List<GitHubCommitDto>,
    onCommitClick: (GitHubCommitDto) -> Unit,
    onViewAllClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Commits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewAllClick)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            commits.forEachIndexed { index, commit ->
                val shortSha = commit.sha.take(7)
                val message = commit.commit?.message?.lines()?.firstOrNull() ?: "Commit $shortSha"
                val author = commit.commit?.author?.name ?: "mkr-infinity"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCommitClick(commit) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = shortSha,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "by $author",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (index < commits.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}