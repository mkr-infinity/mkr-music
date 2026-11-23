/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.search

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.music.innertube.models.SongItem
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.playback.PlayerConnection
import com.music.mkr.playback.SearchPlayNextResult
import com.music.mkr.playback.queues.Queue
import com.music.mkr.ui.component.ActionButtonEmphasis
import com.music.mkr.ui.component.AnimatedActionButton
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.utils.rememberPreference

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TopSongMatchCard(
    song: SongItem,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val allowAnimation by rememberPreference(InterfaceAnimationsKey, true)
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val animate = allowAnimation && lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    var lastPlayTap by remember(song.id) { mutableLongStateOf(-500L) }
    var lastQueueTap by remember(song.id) { mutableLongStateOf(-500L) }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = colors.surfaceContainer,
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SearchArtwork(song, Modifier.size(48.dp), MaterialTheme.shapes.medium, pixels = 256)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(R.string.search_top_song_match),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            song.artists.joinToString { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onMenu) {
                        Icon(painterResource(R.drawable.more_vert), stringResource(R.string.more_options))
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AnimatedActionButton(
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastPlayTap >= 500L) {
                                lastPlayTap = now
                                onPlay()
                            }
                        },
                        animate = animate,
                        compact = true,
                    ) {
                        Icon(painterResource(R.drawable.play), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.play))
                    }
                    AnimatedActionButton(
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastQueueTap >= 500L) {
                                lastQueueTap = now
                                onPlayNext()
                            }
                        },
                        animate = animate,
                        compact = true,
                        emphasis = ActionButtonEmphasis.Tonal,
                    ) {
                        Icon(painterResource(R.drawable.playlist_play), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.search_add_to_play_next))
                    }
                }
            }
        }
    }
}

internal fun enqueueSearchMatch(connection: PlayerConnection?, item: MediaItem) {
    val result = runCatching { connection?.playNextSearchMatch(item) }.getOrNull()
    SnackbarManager.show(
        when (result) {
            SearchPlayNextResult.Added -> R.string.search_play_next_added
            SearchPlayNextResult.AlreadyCurrent -> R.string.search_play_next_already_current
            SearchPlayNextResult.AlreadyNext -> R.string.search_play_next_already_next
            SearchPlayNextResult.QueueLoading -> R.string.search_play_next_queue_loading
            else -> R.string.search_play_next_failed
        },
    )
}

@Composable
internal fun searchMatchPlayer(connection: PlayerConnection?): (Queue, String) -> Boolean {
    var requestedId by remember(connection) { mutableStateOf<String?>(null) }
    LaunchedEffect(connection) {
        val player = connection ?: return@LaunchedEffect
        player.error.collect { error ->
            if (error != null && requestedId != null && requestedId == player.player.currentMediaItem?.mediaId) {
                requestedId = null
                SnackbarManager.show(R.string.error_playback_failed)
            }
        }
    }
    return { queue, id ->
        requestedId = id
        val accepted = runCatching { connection?.playSearchMatch(queue, id) }.getOrNull() == true
        if (!accepted) {
            requestedId = null
            SnackbarManager.show(R.string.error_playback_failed)
        }
        accepted
    }
}
