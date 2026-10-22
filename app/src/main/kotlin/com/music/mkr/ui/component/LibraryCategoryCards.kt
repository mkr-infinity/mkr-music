/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.mkr.R

private val AmberAccent = Color(0xFFF59E0B)

/**
 * Five primary library categories connecting to real data routes:
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Liked Songs & Offline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryCategoryCard(
                titleRes = R.string.library_liked_songs,
                descriptionRes = R.string.library_liked_songs_desc,
                iconRes = R.drawable.favorite,
                accentColor = Color(0xFFEF4444),
                onClick = { navController.navigate("auto_playlist/liked") },
                modifier = Modifier.weight(1f)
            )

            LibraryCategoryCard(
                titleRes = R.string.library_offline,
                descriptionRes = R.string.library_offline_desc,
                iconRes = R.drawable.offline,
                accentColor = Color(0xFF06B6D4),
                onClick = { navController.navigate("auto_playlist/downloaded") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Cached & Local Files
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryCategoryCard(
                titleRes = R.string.library_cached,
                descriptionRes = R.string.library_cached_desc,
                iconRes = R.drawable.cached,
                accentColor = AmberAccent,
                onClick = { navController.navigate("cache_playlist/cached") },
                modifier = Modifier.weight(1f)
            )

            LibraryCategoryCard(
                titleRes = R.string.library_local_files,
                descriptionRes = R.string.library_local_files_desc,
                iconRes = R.drawable.snippet_folder,
                accentColor = Color(0xFF6366F1),
                onClick = { navController.navigate("local_playlist/0") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Top 50
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryCategoryCard(
                titleRes = R.string.library_top_50,
                descriptionRes = R.string.library_top_50_desc,
                iconRes = R.drawable.trending_up,
                accentColor = Color(0xFF10B981),
                onClick = { navController.navigate("top_playlist/$topSize") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LibraryCategoryCard(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @DrawableRes iconRes: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LibraryCategoryCardScale"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with tinted circular background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(titleRes),
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
