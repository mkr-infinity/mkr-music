package com.music.mkr.mkrmusic.updater.downloadmanager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.music.mkr.R
import com.music.mkr.mkrmusic.updater.LATEST_UNIVERSAL_APK_URL
import com.music.mkr.mkrmusic.updater.UNIVERSAL_UPDATE_APK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val MONITOR_WORK_NAME = "apk_update_notification"
private const val DOWNLOAD_ID = "update_download_id"
private const val VERSION = "update_version"
private val enqueueLock = Mutex()

internal suspend fun enqueueUpdateWithNotification(
    context: Context,
    version: String,
): Unit = withContext(Dispatchers.IO) {
    enqueueLock.withLock<Unit> {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val workManager = WorkManager.getInstance(appContext)
        if (workManager.getWorkInfosForUniqueWork(MONITOR_WORK_NAME).get().any { !it.state.isFinished }) {
            return@withLock
        }

        val title = appContext.getString(R.string.update_download_notification_title, version)
        val destination = "MKR Music/${Uri.encode(version)}/$UNIVERSAL_UPDATE_APK"
        val completed = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
        manager.query(completed)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val localUriColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            val sourceColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI)
            while (cursor.moveToNext()) {
                val savedPath = cursor.getString(localUriColumn)?.let { Uri.parse(it).path }
                if (savedPath?.endsWith("/$destination") != true ||
                    cursor.getString(sourceColumn) != LATEST_UNIVERSAL_APK_URL
                ) continue

                val id = cursor.getLong(idColumn)
                val completedApk = manager.getUriForDownloadedFile(id) ?: continue
                val readable = runCatching { manager.openDownloadedFile(id).use { } }.isSuccess
                if (readable) {
                    DownloadNotificationManager.initialize(appContext)
                    DownloadNotificationManager.showDownloadComplete(version, completedApk)
                    return@withLock
                }
            }
        }

        fun request(visibility: Int) = DownloadManager.Request(Uri.parse(LATEST_UNIVERSAL_APK_URL))
            .setTitle(title)
            .setDescription(appContext.getString(R.string.downloading_update))
            .setNotificationVisibility(visibility)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, destination)
            .setMimeType("application/vnd.android.package-archive")

        val id = try {
            manager.enqueue(request(DownloadManager.Request.VISIBILITY_HIDDEN))
        } catch (_: SecurityException) {
            manager.enqueue(request(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
            return@withLock
        }

        try {
            val monitor = OneTimeWorkRequestBuilder<UpdateDownloadMonitor>()
                .setInputData(workDataOf(DOWNLOAD_ID to id, VERSION to version))
                .build()
            workManager.enqueueUniqueWork(MONITOR_WORK_NAME, ExistingWorkPolicy.KEEP, monitor).result.get()
        } catch (error: Exception) {
            val completedApk = manager.getUriForDownloadedFile(id)
            if (completedApk != null) {
                DownloadNotificationManager.initialize(appContext)
                DownloadNotificationManager.showDownloadComplete(version, completedApk)
                return@withLock
            }
            // Restore platform feedback if the monitor cannot be scheduled.
            if (manager.remove(id) == 0) throw error
            manager.enqueue(request(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
        }
    }
}

class UpdateDownloadMonitor(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val id = inputData.getLong(DOWNLOAD_ID, -1L)
        val version = inputData.getString(VERSION) ?: return@withContext Result.failure()
        if (id < 0) return@withContext Result.failure()

        val manager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val notifications = DownloadNotificationManager
        notifications.initialize(applicationContext)
        val query = DownloadManager.Query().setFilterById(id)
        val started = SystemClock.elapsedRealtime()
        var previous: DownloadProgress? = null
        var wasEnabled: Boolean? = null

        try {
            while (true) {
                if (SystemClock.elapsedRealtime() - started >= 8 * 60_000L) {
                    return@withContext Result.retry()
                }
                val result = manager.query(query) ?: return@withContext Result.retry()
                val current = result.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    DownloadProgress(status, updateDownloadPercent(downloaded, total))
                }
                if (current == null) {
                    notifications.cancelNotification()
                    return@withContext Result.success()
                }
                when (current.status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val uri = manager.getUriForDownloadedFile(id)
                        if (uri != null) {
                            manager.openDownloadedFile(id).use { }
                            notifications.showDownloadComplete(version, uri)
                            return@withContext Result.success()
                        }
                        break
                    }
                    DownloadManager.STATUS_FAILED -> break
                    else -> {
                        val enabled = notifications.notificationsEnabled()
                        val waiting = current.status != DownloadManager.STATUS_RUNNING
                        if (enabled != wasEnabled || (enabled && current != previous)) {
                            notifications.updateDownloadProgress(current.percent, version, waiting)
                        }
                        previous = current
                        wasEnabled = enabled
                        delay(if (waiting || !enabled) 5_000L else 1_000L)
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: RuntimeException) {
            Timber.w(error, "Could not read APK update download status")
        }

        notifications.showDownloadFailed(version, applicationContext.getString(R.string.download_failed))
        Result.failure()
    }

    private data class DownloadProgress(val status: Int, val percent: Int?)
}

internal fun updateDownloadPercent(downloaded: Long, total: Long): Int? =
    if (downloaded < 0L || total <= 0L) null
    else (downloaded.toDouble() / total * 100).toInt().coerceIn(0, 99)
