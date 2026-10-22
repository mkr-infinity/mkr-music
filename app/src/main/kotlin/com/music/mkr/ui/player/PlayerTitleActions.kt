/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.music.mkr.models.MediaMetadata
import com.music.mkr.ui.component.BottomSheetState

/**
 * Centralized title and artist actions shared by all player styles.
 */
@Immutable
class PlayerTitleActions(
    val onTitleClick: () -> Unit,
    val onArtistClick: (artistId: String) -> Unit,
    val onCopyTitle: () -> Unit,
    val onCopyArtists: () -> Unit,
)

@Composable
fun rememberPlayerTitleActions(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    state: BottomSheetState,
): PlayerTitleActions {
    val context = LocalContext.current
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val artistLine = remember(mediaMetadata.artists) {
        mediaMetadata.artists.joinToString(", ") { it.name }
    }

    return remember(mediaMetadata, navController, state, artistLine) {
        PlayerTitleActions(
            onTitleClick = {
                mediaMetadata.album?.let { album ->
                    state.collapseSoft()
                    navController.navigate("album/${album.id}")
                }
            },
            onArtistClick = { artistId ->
                if (artistId.isNotBlank()) {
                    state.collapseSoft()
                    navController.navigate("artist/$artistId")
                }
            },
            onCopyTitle = {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText("Copied Title", mediaMetadata.title)
                )
                Toast.makeText(context, "Copied Title", Toast.LENGTH_SHORT).show()
            },
            onCopyArtists = {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText("Copied Artist", artistLine)
                )
                Toast.makeText(context, "Copied Artist", Toast.LENGTH_SHORT).show()
            },
        )
    }
}
