package com.music.mkr.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.music.innertube.utils.parseCookieString
import com.music.mkr.BuildConfig
import com.music.mkr.R
import com.music.mkr.constants.AccountEmailKey
import com.music.mkr.constants.InnerTubeCookieKey
import com.music.mkr.github.GitHubViewModel
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.rememberPreference
import com.music.mkr.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNavigate: (String) -> Unit,
    homeViewModel: HomeViewModel,
    gitHubViewModel: GitHubViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
    val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        innerTubeCookie.isNotEmpty() && "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val isStarred by gitHubViewModel.isStarred.collectAsState()
    LaunchedEffect(expanded) {
        if (expanded) {
            isUpdateAvailable = getUpdateAvailableState(context)
            gitHubViewModel.checkStarStatus(context)
        }
    }

    val (accountEmail, _) = rememberPreference(AccountEmailKey, "")
    val accountName by homeViewModel.accountName.collectAsState()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

    val groupContainerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = (-48).dp),
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                BorderStroke(
                    1.dp,
                    Color.Black.copy(alpha = 0.35f)
                ),
                RoundedCornerShape(32.dp)
            )
            .padding(vertical = 4.dp),
    ) {
        // --- 1. Account Hero Header ---
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            onClick = {
                onDismissRequest()
                if (isLoggedIn) {
                    onNavigate("settings/account")
                } else {
                    onNavigate("login")
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoggedIn && !accountImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = accountImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                CircleShape
                            )
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(if (isLoggedIn) R.drawable.account else R.drawable.google),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isLoggedIn) {
                            accountName.ifEmpty { accountEmail.ifEmpty { "Account" } }
                        } else {
                            "Connect Account"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLoggedIn) {
                            accountEmail.ifEmpty { "Google / YouTube Music" }
                        } else {
                            "Sign in for synced library"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.navigate_next),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. Expressive Quick Actions Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickActionModifier = Modifier
                .weight(1f)
                .height(46.dp)

            // Star Repo Button
            Surface(
                onClick = {
                    gitHubViewModel.toggleStar(context) {
                        uriHandler.openUri("https://github.com/mkr-infinity/mkr-music")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f)),
                modifier = quickActionModifier
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(if (isStarred) R.drawable.star else R.drawable.star_border),
                        contentDescription = "Star Repo",
                        tint = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // System Update Button (with Alert indicator)
            Surface(
                onClick = {
                    onDismissRequest()
                    onNavigate("settings/update")
                },
                shape = RoundedCornerShape(16.dp),
                color = if (isUpdateAvailable) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f)),
                modifier = quickActionModifier
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BadgedBox(
                        badge = {
                            if (isUpdateAvailable) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(7.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (isUpdateAvailable) R.drawable.update_alert else R.drawable.system_update_uptodate),
                            contentDescription = "Update",
                            tint = if (isUpdateAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Changelog / Release Notes
            Surface(
                onClick = {
                    onDismissRequest()
                    onNavigate("settings/changelog")
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f)),
                modifier = quickActionModifier
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.changelog_paper),
                        contentDescription = "Release Notes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Share App (Universal APK)
            Surface(
                onClick = {
                    onDismissRequest()
                    val targetApkName = "mkr-music-universal.apk"
                    val shareUrl = "https://github.com/mkr-infinity/mkr-music/releases/latest/download/$targetApkName"
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, shareUrl)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f)),
                modifier = quickActionModifier
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.share_hotspot),
                        contentDescription = "Share App",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. Section 1: Playback & Activity ---
        DropdownMenuIconItem(
            title = stringResource(R.string.history),
            icon = R.drawable.music_history,
            shape = listItemShape(0, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("history")
            }
        )
        DropdownMenuIconItem(
            title = stringResource(R.string.listen_together),
            icon = R.drawable.group_outlined,
            shape = listItemShape(1, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("listen_together_from_topbar")
            }
        )
        DropdownMenuIconItem(
            title = stringResource(R.string.stats),
            icon = R.drawable.stats,
            shape = listItemShape(2, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("stats")
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // --- 4. Section 2: Core Preferences & About ---
        DropdownMenuIconItem(
            title = stringResource(R.string.settings),
            icon = R.drawable.settings,
            shape = listItemShape(0, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("settings")
            }
        )
        DropdownMenuIconItem(
            title = stringResource(R.string.integrations),
            icon = R.drawable.extension,
            shape = listItemShape(1, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("settings/integrations")
            }
        )
        DropdownMenuIconItem(
            title = stringResource(R.string.about),
            icon = R.drawable.icon,
            trailingText = "v${BuildConfig.VERSION_NAME}",
            isAppIcon = true,
            shape = listItemShape(2, 3, 18.dp),
            containerColor = groupContainerColor,
            onClick = {
                onDismissRequest()
                onNavigate("settings/about")
            }
        )
    }
}

@Composable
fun DropdownMenuIconItem(
    title: String,
    icon: Int,
    trailingText: String? = null,
    isAppIcon: Boolean = false,
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = shape,
        color = containerColor,
        border = BorderStroke(0.8.dp, Color.Black.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingIcon = {
                if (isAppIcon) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            trailingIcon = trailingText?.let {
                {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            onClick = onClick,
            modifier = Modifier.height(46.dp)
        )
    }
}
