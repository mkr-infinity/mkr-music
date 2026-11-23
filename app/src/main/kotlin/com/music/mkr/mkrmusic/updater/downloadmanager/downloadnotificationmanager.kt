package com.music.mkr.mkrmusic.updater.downloadmanager

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.music.mkr.R
import com.music.mkr.constants.EnableNotificationsKey
import com.music.mkr.mkrmusic.updater.getDownloadNotificationsSetting
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get
import java.io.File

object DownloadNotificationManager {
    private lateinit var appContext: Context
    private val manager get() = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    const val CHANNEL_ID = "download_progress_channel"
    private const val NOTIFICATION_ID = 5678

    fun initialize(context: Context) {
        appContext = context.applicationContext
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.download_progress_channel),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = appContext.getString(R.string.download_progress_description)
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
        )
    }

    internal fun notificationsEnabled(): Boolean =
        appContext.dataStore.get(EnableNotificationsKey, true) &&
            getDownloadNotificationsSetting(appContext) &&
            manager.areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED)

    fun showDownloadStarting(version: String, fileSize: String) {
        val detail = if (fileSize.isBlank()) version
        else appContext.getString(R.string.version_file_size, version, fileSize)
        postProgress(appContext.getString(R.string.downloading_update), detail, null)
    }

    fun updateDownloadProgress(progress: Int, version: String) {
        updateDownloadProgress(progress, version, waiting = false)
    }

    internal fun updateDownloadProgress(progress: Int?, version: String, waiting: Boolean) {
        val title = appContext.getString(
            if (waiting) R.string.update_download_waiting else R.string.downloading_update,
        )
        val detail = progress?.let { appContext.getString(R.string.version_progress, version, it) } ?: version
        postProgress(title, detail, progress)
    }

    private fun postProgress(title: String, detail: String, progress: Int?) {
        if (!notificationsEnabled()) {
            cancelNotification()
            return
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val extras = Bundle().apply {
                putBoolean("android.requestPromotedOngoing", true)
                putString("android.shortCriticalText", progress?.let { "$it%" } ?: appContext.getString(R.string.starting))
            }
            Notification.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.mkrmusicnotification)
                .setContentTitle(title)
                .setContentText(detail)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setStyle(progressStyle(progress))
                .addExtras(extras)
                .build()
        } else {
            builder(title, detail)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setProgress(100, progress ?: 0, progress == null)
                .build()
        }
        manager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun progressStyle(progress: Int?): Notification.ProgressStyle =
        Notification.ProgressStyle().apply {
            if (progress == null) {
                setProgressIndeterminate(true)
            } else {
                repeat(4) { index ->
                    val color = if (index % 2 == 0) 0xFF4285F4.toInt() else 0xFF8E24AA.toInt()
                    addProgressSegment(Notification.ProgressStyle.Segment(25).setColor(color))
                }
                setProgress(progress)
            }
        }

    fun showDownloadComplete(version: String, filePath: String) {
        if (!notificationsEnabled()) return
        showDownloadComplete(
            version,
            FileProvider.getUriForFile(appContext, "${appContext.packageName}.FileProvider", File(filePath)),
        )
    }

    internal fun showDownloadComplete(version: String, apkUri: Uri) {
        if (!notificationsEnabled()) {
            cancelNotification()
            return
        }
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingInstall = PendingIntent.getActivity(
            appContext, 0, install, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val title = appContext.getString(R.string.update_ready)
        val detail = appContext.getString(R.string.tap_to_install_version, version)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            Notification.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.mkrmusicnotification)
                .setContentTitle(title)
                .setContentText(detail)
                .setAutoCancel(true)
                .setContentIntent(pendingInstall)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setStyle(progressStyle(100))
                .build()
        } else {
            builder(title, detail)
                .setAutoCancel(true)
                .setContentIntent(pendingInstall)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build()
        }
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showDownloadFailed(version: String, errorMessage: String) {
        if (!notificationsEnabled()) {
            cancelNotification()
            return
        }
        manager.notify(
            NOTIFICATION_ID,
            builder(
                appContext.getString(R.string.update_failed),
                appContext.getString(R.string.failed_to_download_version, version),
            )
                .setStyle(NotificationCompat.BigTextStyle().bigText(errorMessage))
                .setAutoCancel(true)
                .build(),
        )
    }

    fun cancelNotification() {
        manager.cancel(NOTIFICATION_ID)
    }

    private fun builder(title: String, detail: String) =
        NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.mkrmusicnotification)
            .setContentTitle(title)
            .setContentText(detail)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
}
