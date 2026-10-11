/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
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
import com.music.mkr.ui.component.DialogBasic
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.component.DonateDialog
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.safeOpenUri
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
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }

    if (showSupportDialog) {
        DonateDialog(onDismiss = { showSupportDialog = false })
    }

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

        // Top Hero Section: Developer Profile & Social Media Hub
        DeveloperHeroCard(onSupportClick = { showSupportDialog = true })

        Spacer(modifier = Modifier.height(16.dp))

        // Application Details & Legal
        ExpressiveSettingGroup(
            title = "App Information",
            items = listOf(
                Material3SettingsItem(
                    key = "about_app",
                    icon = painterResource(R.drawable.deployed_app_update),
                    title = { Text(stringResource(R.string.installed_date_title)) },
                    trailingContent = { Text(installedDate) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.license_mkr),
                    title = { Text("Open Source License") },
                    description = { Text("GNU General Public License v3.0 (GPL-3.0)") },
                    onClick = { uriHandler.safeOpenUri(context, "${GitHubConfig.REPO_URL}/blob/main/LICENSE") },
                    isExternalLink = true
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Information Card with Default App Logo placed at the last of About section
        AppVersionTile(
            appName = "MKR Music",
            buildDetails = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • Stable Universal Release",
            onGithubClick = { uriHandler.safeOpenUri(context, GitHubConfig.REPO_URL) }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }

    TopAppBar(
        title = { Text("About") },
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
    onSupportClick: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Avatar with app logo badge
            Box(
                modifier = Modifier.size(86.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp)
                        .align(Alignment.Center)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(GitHubConfig.DEV_AVATAR_URL)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.dev),
                        error = painterResource(R.drawable.dev),
                        fallback = painterResource(R.drawable.dev),
                        contentDescription = GitHubConfig.DEV_NAME,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Official MKR Music badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "MKR Music Badge",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Developer Name (Centered, Bold, Never truncated)
            Text(
                text = GitHubConfig.DEV_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Role pill: Tech Enthusiast & Android Developer
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                tonalElevation = 1.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = GitHubConfig.DEV_ROLE,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Social Links Box: 2 in one line (2x2 grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SocialLinkPill(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.github,
                        title = "GitHub",
                        subtitle = GitHubConfig.DEV_HANDLE,
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_GITHUB_URL) }
                    )
                    SocialLinkPill(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.instagram,
                        title = "Instagram",
                        subtitle = "@mkr_infinity",
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_INSTAGRAM_URL) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SocialLinkPill(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.telegram,
                        title = "Telegram",
                        subtitle = "@mkr_infinity",
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_TELEGRAM_URL) }
                    )
                    SocialLinkPill(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.globe,
                        title = "Website",
                        subtitle = "mkr-infinity",
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_WEBSITE_URL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Support Capsule inside the card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = onSupportClick),
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.buymeacoffee),
                        contentDescription = "Support",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Donate & Support",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLinkPill(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AppVersionTile(
    appName: String,
    buildDetails: String,
    onGithubClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(R.drawable.icon),
                    contentDescription = "MKR Music",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Universal",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onGithubClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.github),
                        contentDescription = "GitHub Repository",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}