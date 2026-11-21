/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.menu

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.db.entities.Playlist
import com.music.mkr.ui.component.DefaultDialog
import com.music.mkr.viewmodels.DuplicateChoice
import com.music.mkr.viewmodels.DuplicateSongs
import com.music.mkr.viewmodels.PlaylistsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun playlistAddHandler(
    isVisible: Boolean,
    viewModel: PlaylistsViewModel,
    onDismiss: () -> Unit,
    syncRemote: Boolean = true,
    prepare: suspend () -> List<String>,
): Pair<Boolean, (Playlist) -> Unit> {
    val scope = rememberCoroutineScope()
    val pickerVisible by rememberUpdatedState(isVisible)
    val dismissPicker by rememberUpdatedState(onDismiss)
    var activePlaylist by remember { mutableStateOf<Playlist?>(null) }
    var duplicateRequest by remember { mutableStateOf<DuplicateSongs?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun submit(playlist: Playlist, choice: DuplicateChoice? = null) {
        if (busy) return
        val prepared = duplicateRequest?.songIds
        duplicateRequest = null
        activePlaylist = playlist
        busy = true
        val operation = viewModel.addSongs(playlist.playlist, choice, syncRemote) { prepared ?: prepare() }
        scope.launch {
            try {
                val duplicates = operation.await()
                if (pickerVisible) {
                    if (duplicates == null) dismissPicker() else duplicateRequest = duplicates
                }
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) duplicateRequest = null
    }
    val request = duplicateRequest
    val destination = activePlaylist
    if (isVisible && request != null && destination != null) {
        DefaultDialog(
            title = { Text(stringResource(R.string.duplicates)) },
            onDismiss = { duplicateRequest = null },
            buttons = {
                TextButton(onClick = { duplicateRequest = null }) { Text(stringResource(android.R.string.cancel)) }
                TextButton(onClick = { submit(destination, DuplicateChoice.Skip) }) {
                    Text(stringResource(R.string.skip_duplicates))
                }
                TextButton(onClick = { submit(destination, DuplicateChoice.AddAnyway) }) {
                    Text(stringResource(R.string.add_anyway))
                }
            },
        ) {
            Text(
                if (request.count == 1) stringResource(R.string.duplicates_description_single)
                else stringResource(R.string.duplicates_description_multiple, request.count)
            )
        }
    }
    return busy to { playlist: Playlist -> if (duplicateRequest == null) submit(playlist) }
}

@Composable
internal fun PlaylistAddProgress() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }
    if (visible) {
        Row(
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.playlist_add_pending), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun playlistTargetClick(
    enabled: Boolean,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(
        targetValue = if (animationsEnabled && pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "playlist_target_press",
    )
    return Modifier
        .clickable(
            enabled = enabled,
            role = Role.Button,
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick,
        )
        .graphicsLayer {
            scaleX = if (animationsEnabled) scale.value else 1f
            scaleY = scaleX
        }
}
