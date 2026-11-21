package com.music.mkr.utils

import com.music.mkr.db.entities.PlaylistSongMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlaylistSyncEntriesTest {
    @Test
    fun oneKnownRemoteCopyCannotAcknowledgeAnUnsentDuplicate() {
        val known = PlaylistSongMap(id = 1, playlistId = "playlist", songId = "song", setVideoId = "known")
        val pending = known.copy(id = 2, position = 1, setVideoId = null)
        val local = listOf(known, pending)
        assertEquals(listOf(pending), unmatchedLocalPlaylistEntries(local, listOf("song" to "known")))
        assertEquals(emptyList<PlaylistSongMap>(), unmatchedLocalPlaylistEntries(
            local, listOf("song" to "known", "song" to "new"),
        ))
    }

    @Test
    fun emptyOrPartialRemoteSnapshotsKeepUnconfirmedLocalAdditions() {
        val a = PlaylistSongMap(id = 1, playlistId = "playlist", songId = "a")
        val b = a.copy(id = 2, songId = "b", position = 1)
        assertEquals(listOf(a, b), unmatchedLocalPlaylistEntries(listOf(a, b), emptyList()))
        assertEquals(listOf(b), unmatchedLocalPlaylistEntries(listOf(a, b), listOf("a" to "remote-a")))
    }

    @Test
    fun snapshotWaitsForAnEditToTheSamePlaylist() = runBlocking {
        val gate = PlaylistEditGate()
        val finishEdit = CompletableDeferred<Unit>()
        val snapshotStarted = CompletableDeferred<Unit>()
        val edit = launch(start = CoroutineStart.UNDISPATCHED) {
            gate.withEdit("playlist") { finishEdit.await() }
        }
        val snapshot = launch(start = CoroutineStart.UNDISPATCHED) {
            gate.withEdit("playlist") { snapshotStarted.complete(Unit) }
        }
        val startedBeforeEditFinished = snapshotStarted.isCompleted
        finishEdit.complete(Unit)
        edit.join()
        snapshot.join()
        assertFalse(startedBeforeEditFinished)
    }
}
