/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.music.mkr.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MkrSnackbarHost(
    state: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = state, modifier = modifier) { data ->
        val colors = MaterialTheme.colorScheme
        val playlist = data.visuals as? SnackbarManager.PlaylistVisuals
        if (playlist == null) {
            Snackbar(
                snackbarData = data,
                actionOnNewLine = data.visuals.actionLabel != null,
                shape = RoundedCornerShape(28.dp),
                containerColor = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
                actionColor = colors.inversePrimary,
                actionContentColor = colors.inversePrimary,
                dismissActionContentColor = colors.inverseOnSurface,
            )
        } else {
            val isError = playlist.iconRes == R.drawable.error
            val iconShape = MaterialShapes.Cookie6Sided.toShape()
            Snackbar(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.large,
                containerColor = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = iconShape,
                        color = if (isError) colors.errorContainer else colors.primaryContainer,
                        contentColor = if (isError) colors.onErrorContainer else colors.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(painterResource(playlist.iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(playlist.message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
