/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.mkr.LocalDatabase
import com.music.mkr.R
import com.music.mkr.constants.ShowCachedPlaylistKey
import com.music.mkr.constants.ShowDownloadedPlaylistKey
import com.music.mkr.constants.ShowLikedPlaylistKey
import com.music.mkr.constants.ShowTopPlaylistKey
import com.music.mkr.utils.rememberPreference

private val AmberAccent = Color(0xFFF59E0B)

private data class LibraryShortcutItem(
    val title: String,
    val countText: String,
    val iconRes: Int,
    val iconColor: Color,
    val route: String
)

/**
 * Compact library shortcut cards connecting to real data routes:
 * 1. Liked Songs -> auto_playlist/liked
 * 2. Offline -> auto_playlist/downloaded
 * 3. Cached -> cache_playlist/cached
 * 4. Local Files -> local_playlist/0
 * 5. Top 50 -> top_playlist/50
 */
@Composable
fun LibraryCategoryCardsSection(
    navController: NavController,
    topSize: Int = 50,
    modifier: Modifier = Modifier
) {
    val database = LocalDatabase.current
    val likedSongsCount by database.likedSongsCount().collectAsState(initial = 0)
    val (showLiked) = rememberPreference(ShowLikedPlaylistKey, defaultValue = true)
    val (showDownloaded) = rememberPreference(ShowDownloadedPlaylistKey, defaultValue = true)
    val (showCached) = rememberPreference(ShowCachedPlaylistKey, defaultValue = true)
    val (showTop) = rememberPreference(ShowTopPlaylistKey, defaultValue = true)

    val items = buildList {
        if (showLiked) {
            add(
                LibraryShortcutItem(
                    title = stringResource(R.string.library_liked_songs),
                    countText = if (likedSongsCount > 0) "$likedSongsCount tracks" else stringResource(R.string.library_liked_songs_desc),
                    iconRes = R.drawable.favorite,
                    iconColor = Color(0xFFEF4444),
                    route = "auto_playlist/liked"
                )
            )
        }
        if (showDownloaded) {
            add(
                LibraryShortcutItem(
                    title = stringResource(R.string.library_offline),
                    countText = stringResource(R.string.library_offline_desc),
                    iconRes = R.drawable.offline,
                    iconColor = Color(0xFF06B6D4),
                    route = "auto_playlist/downloaded"
                )
            )
        }
        if (showCached) {
            add(
                LibraryShortcutItem(
                    title = stringResource(R.string.library_cached),
                    countText = stringResource(R.string.library_cached_desc),
                    iconRes = R.drawable.cached,
                    iconColor = AmberAccent,
                    route = "cache_playlist/cached"
                )
            )
        }
        add(
            LibraryShortcutItem(
                title = stringResource(R.string.library_local_files),
                countText = stringResource(R.string.library_local_files_desc),
                iconRes = R.drawable.snippet_folder,
                iconColor = Color(0xFF6366F1),
                route = "local_playlist/0"
            )
        )
        if (showTop) {
            add(
                LibraryShortcutItem(
                    title = stringResource(R.string.library_top_50),
                    countText = "Top $topSize • Most played",
                    iconRes = R.drawable.trending_up,
                    iconColor = Color(0xFF10B981),
                    route = "top_playlist/$topSize"
                )
            )
        }
    }

    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    ShortcutCard(
                        title = item.title,
                        countText = item.countText,
                        iconRes = item.iconRes,
                        iconColor = item.iconColor,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(item.route) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    countText: String,
    @DrawableRes iconRes: Int,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShortcutCardScale"
    )

    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val finalBgColor = remember(surfaceContainerColor, iconColor) {
        surfaceContainerColor.copy(alpha = 0.85f)
    }
    val iconBgColor = remember(iconColor) {
        iconColor.copy(alpha = 0.14f)
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = finalBgColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
