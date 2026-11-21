/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.music.mkr.LocalDatabase
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.ShowCachedPlaylistKey
import com.music.mkr.constants.ShowDownloadedPlaylistKey
import com.music.mkr.constants.ShowLikedPlaylistKey
import com.music.mkr.utils.rememberPreference

private data class LibraryShortcutItem(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val route: String,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryCategoryCardsSection(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val database = LocalDatabase.current
    val likedSongsCount by database.likedSongsCount().collectAsState(initial = 0)
    val showLiked by rememberPreference(ShowLikedPlaylistKey, true)
    val showDownloads by rememberPreference(ShowDownloadedPlaylistKey, true)
    val showCached by rememberPreference(ShowCachedPlaylistKey, true)
    val animationsEnabled by rememberPreference(InterfaceAnimationsKey, true)
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val animate = animationsEnabled && lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    val cookieShape = MaterialShapes.Cookie6Sided.toShape()

    val items = buildList {
        if (showLiked) add(
            LibraryShortcutItem(
                stringResource(R.string.library_liked_songs),
                pluralStringResource(R.plurals.n_song, likedSongsCount, likedSongsCount),
                R.drawable.favorite,
                "auto_playlist/liked",
            )
        )
        if (showDownloads) add(
            LibraryShortcutItem(
                stringResource(R.string.library_downloads),
                stringResource(R.string.library_downloads_desc),
                R.drawable.download,
                "auto_playlist/downloaded",
            )
        )
        if (showCached) add(
            LibraryShortcutItem(
                stringResource(R.string.cached_playlist),
                stringResource(R.string.library_cached_songs_desc),
                R.drawable.cached,
                "cache_playlist/cached",
            )
        )
        add(
            LibraryShortcutItem(
                stringResource(R.string.history),
                stringResource(R.string.library_history_desc),
                R.drawable.history,
                "history",
            )
        )
        add(
            LibraryShortcutItem(
                stringResource(R.string.library_local_files),
                stringResource(R.string.library_local_files_desc),
                R.drawable.snippet_folder,
                "local_playlist/0",
            )
        )
    }

    val columns = if (LocalDensity.current.fontScale >= 1.4f) 1 else 2
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    ShortcutCard(
                        item = item,
                        badgeShape = cookieShape,
                        animationsEnabled = animate,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                    )
                }
                if (rowItems.size < columns) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    item: LibraryShortcutItem,
    badgeShape: Shape,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val badgeTilt = animateFloatAsState(
        targetValue = if (animationsEnabled && isPressed) -5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "LibraryShortcutBadgeTilt",
    )
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraLarge
    Surface(
        modifier = modifier.clip(shape).clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            role = Role.Button,
            onClick = onClick,
        ).mkrPressFeedback(
            scaleDown = 0.98f,
            interactionSource = interactionSource,
            animationsEnabled = animationsEnabled,
        ),
        shape = shape,
        color = colors.surfaceContainer,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.size(38.dp).graphicsLayer {
                    rotationZ = if (animationsEnabled) badgeTilt.value else 0f
                },
                shape = badgeShape,
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(item.iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}
