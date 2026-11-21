/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        val label = data.visuals.actionLabel
        val close = data.visuals.withDismissAction
        Snackbar(
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            actionOnNewLine = label != null,
            action = if (label == null) null else ({
                TextButton(onClick = data::performAction) { Text(label, color = colors.inversePrimary) }
            }),
            dismissAction = if (!close) null else ({
                IconButton(onClick = data::dismiss) {
                    Icon(
                        painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.close),
                        tint = colors.inverseOnSurface,
                    )
                }
            }),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                if (playlist != null) {
                    val isError = playlist.iconRes == R.drawable.error
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = MaterialShapes.Cookie6Sided.toShape(),
                        color = if (isError) colors.errorContainer else colors.primaryContainer,
                        contentColor = if (isError) colors.onErrorContainer else colors.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(painterResource(playlist.iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Text(
                    data.visuals.message,
                    modifier = Modifier.weight(1f).heightIn(max = 120.dp).verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
