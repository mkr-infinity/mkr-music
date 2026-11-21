package com.music.mkr.playback

import android.content.Context
import android.util.AtomicFile
import com.music.mkr.di.ApplicationScope
import com.music.mkr.models.PersistPlayerState
import com.music.mkr.models.PersistQueue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class QueuePersistence @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope scope: CoroutineScope,
) {
    internal data class SavedQueue(val queue: PersistQueue, val shuffleOrder: IntArray?)
    internal data class Snapshot(val savedQueue: SavedQueue, val automix: PersistQueue, val state: PersistPlayerState)

    private val pending = Channel<Snapshot>(Channel.CONFLATED)
    @Volatile private var latest: Snapshot? = null

    init {
        scope.launch(Dispatchers.IO) {
            for (snapshot in pending) {
                write(MusicService.PERSISTENT_QUEUE_FILE, snapshot.savedQueue.queue, snapshot.savedQueue.shuffleOrder)
                write(MusicService.PERSISTENT_AUTOMIX_FILE, snapshot.automix)
                write(MusicService.PERSISTENT_PLAYER_STATE_FILE, snapshot.state)
            }
        }
    }

    internal fun save(snapshot: Snapshot) {
        latest = snapshot
        pending.trySend(snapshot)
    }

    internal fun readQueue(): SavedQueue? = latest?.savedQueue ?: read(MusicService.PERSISTENT_QUEUE_FILE) { stream ->
        val queue = stream.readObject() as PersistQueue
        // Older snapshots end after the queue object.
        val order = runCatching { stream.readObject() as? IntArray }.getOrNull()
        SavedQueue(queue, order)
    }

    internal fun readAutomix(): PersistQueue? = latest?.automix ?: read(MusicService.PERSISTENT_AUTOMIX_FILE) {
        it.readObject() as PersistQueue
    }

    internal fun readState(): PersistPlayerState? = latest?.state ?: read(MusicService.PERSISTENT_PLAYER_STATE_FILE) {
        it.readObject() as PersistPlayerState
    }

    private fun <T> read(name: String, readObject: (ObjectInputStream) -> T): T? {
        val file = File(context.filesDir, name)
        if (!file.isFile) return null
        return runCatching { ObjectInputStream(file.inputStream()).use(readObject) }
            .onFailure { Timber.w(it, "Could not read playback snapshot %s", name) }
            .getOrNull()
    }

    private fun write(name: String, value: Any, extra: IntArray? = null) {
        val file = AtomicFile(File(context.filesDir, name))
        runCatching {
            val output = file.startWrite()
            try {
                ObjectOutputStream(output).apply {
                    writeObject(value)
                    if (extra != null) writeObject(extra)
                    flush()
                }
                file.finishWrite(output)
            } catch (error: Exception) {
                file.failWrite(output)
                throw error
            }
        }.onFailure { Timber.w(it, "Could not save playback snapshot %s", name) }
    }
}
