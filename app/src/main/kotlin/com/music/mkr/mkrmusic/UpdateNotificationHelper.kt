package com.music.mkr.mkrmusic

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.music.mkr.R
import com.music.mkr.constants.EnableNotificationsKey
import com.music.mkr.utils.dataStore
import com.music.mkr.utils.get

object UpdateNotificationHelper {
    private const val CHANNEL_ID = "updates"
    private const val NOTIFICATION_ID = 1001

    fun showUpdateNotification(context: Context, versionName: String) {
        val notificationsEnabled = context.dataStore.get(EnableNotificationsKey, true)
        if (!notificationsEnabled) return

        val nm = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_updates_title),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        // Direct download URL format from mkrmusicupdater - use the full tag (vX.X.X or bX.X.X) or nightly link
        val apkUrl = if (versionName.contains("nightly", ignoreCase = true)) {
            "https://nightly.link/mkr-infinity/mkr-music/workflows/build.yml/main/mkr-music-gms.zip"
        } else {
            "https://github.com/mkr-infinity/mkr-music/releases/download/$versionName/mkrmusic-universal.apk"
        }
        val intent = Intent(Intent.ACTION_VIEW, apkUrl.toUri())

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, flags)

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.icon)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.mkrmusicnotification)
            .setLargeIcon(largeIcon)
            .setContentTitle(context.getString(R.string.update_available_title))
            .setContentText(versionName)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notif)
        }
    }
}
