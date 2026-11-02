package com.music.mkr.mkrmusic.updater.downloadmanager

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import com.music.mkr.R
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.utils.CoilBitmapLoader
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.util.Locale

/**
 * Custom MediaNotification.Provider for MKR Music:
 * - Continuously reflects the currently playing track's artwork and dynamic palette.
 * - Synchronously decorates cached artwork and safely intercepts asynchronous artwork loads.
 * - Guards against race conditions and stale async results across rapid track transitions.
 * - Extracts dynamic colors directly from the active track's artwork palette.
 * - Adds Android 16 Status Chips (Live Updates) support on API 36+.
 */
@OptIn(UnstableApi::class)
class MkrNotificationProvider(
    private val context: Context,
    private val scope: CoroutineScope,
    notificationIdProvider: DefaultMediaNotificationProvider.NotificationIdProvider,
    channelId: String,
    channelNameResourceId: Int,
) : MediaNotification.Provider {

    private val defaultProvider = DefaultMediaNotificationProvider(
        context,
        notificationIdProvider,
        channelId,
        channelNameResourceId
    )

    @Volatile
    private var activeMediaId: String? = null

    /**
     * Set the small icon for the notification. This is used by MusicService.
     */
    fun setSmallIcon(iconResId: Int): MkrNotificationProvider {
        defaultProvider.setSmallIcon(iconResId)
        return this
    }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback,
    ): MediaNotification {
        val currentMediaItem = mediaSession.player.currentMediaItem
        val currentMediaId = currentMediaItem?.mediaId
        activeMediaId = currentMediaId

        // Intercept asynchronous notification updates from defaultProvider
        val interceptingCallback = MediaNotification.Provider.Callback { updatedMediaNotification ->
            val playerMediaId = mediaSession.player.currentMediaItem?.mediaId
            if (activeMediaId == currentMediaId && playerMediaId == currentMediaId) {
                val decorated = decorateNotification(mediaSession, updatedMediaNotification, currentMediaId)
                onNotificationChangedCallback.onNotificationChanged(decorated)
            }
        }

        val baseNotification = defaultProvider.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            interceptingCallback
        )

        val decoratedBase = decorateNotification(mediaSession, baseNotification, currentMediaId)

        // If artwork bitmap is not yet in memory, attach directly to the bitmap loader future
        // so the notification is guaranteed to update immediately once the bitmap resolves.
        val artworkUri = currentMediaItem?.mediaMetadata?.artworkUri
            ?: currentMediaItem?.mediaMetadata?.extras?.getString("artwork_uri")?.let { Uri.parse(it) }
        val uriString = artworkUri?.toString()
        if (!uriString.isNullOrBlank() && CoilBitmapLoader.getCachedBitmap(uriString) == null) {
            val bitmapFuture = mediaSession.bitmapLoader.loadBitmap(artworkUri)
            Futures.addCallback(
                bitmapFuture,
                object : FutureCallback<Bitmap> {
                    override fun onSuccess(result: Bitmap) {
                        if (!result.isRecycled) {
                            val playerMediaId = mediaSession.player.currentMediaItem?.mediaId
                            if (activeMediaId == currentMediaId && playerMediaId == currentMediaId) {
                                val updated = decorateNotification(mediaSession, baseNotification, currentMediaId)
                                onNotificationChangedCallback.onNotificationChanged(updated)
                            }
                        }
                    }

                    override fun onFailure(t: Throwable) {
                        Timber.tag("MkrNotificationProvider").w(t, "Bitmap load failed for $uriString")
                    }
                },
                MoreExecutors.directExecutor()
            )
        }

        return decoratedBase
    }

    private fun decorateNotification(
        mediaSession: MediaSession,
        mediaNotification: MediaNotification,
        expectedMediaId: String?,
    ): MediaNotification {
        return try {
            val player = mediaSession.player
            val isPlaying = player.playWhenReady && player.playbackState == Player.STATE_READY
            val currentMediaItem = player.currentMediaItem
            val mediaMetadata = currentMediaItem?.mediaMetadata ?: player.mediaMetadata
            val mediaId = currentMediaItem?.mediaId
            val artworkUri = mediaMetadata.artworkUri?.toString()
                ?: mediaMetadata.extras?.getString("artwork_uri")

            // Look up cached bitmap from CoilBitmapLoader
            val cachedBmp = artworkUri?.let { CoilBitmapLoader.getCachedBitmap(it) }

            // Extract or look up dynamic palette
            val cachedPalette = (artworkUri?.let { PlayerColorExtractor.getCachedPalette(it) }
                ?: mediaId?.let { PlayerColorExtractor.getCachedPalette(it) }
                ?: cachedBmp?.let { bmp ->
                    try {
                        PlayerColorExtractor.extractFromBitmap(bmp).also { pal ->
                            if (!artworkUri.isNullOrBlank()) PlayerColorExtractor.putCachedPalette(artworkUri, pal)
                            if (!mediaId.isNullOrBlank()) PlayerColorExtractor.putCachedPalette(mediaId, pal)
                        }
                    } catch (_: Exception) {
                        null
                    }
                })

            // Derive dynamic accent color or fall back to signature WarmAmber
            val accentColor = cachedPalette?.primaryAccent?.toArgb() ?: PlayerColorExtractor.WarmAmber.toArgb()

            val notification = mediaNotification.notification
            val builder = Notification.Builder.recoverBuilder(context, notification)

            // Explicitly set largeIcon from the cached bitmap to guarantee it is NEVER lost or stripped
            if (cachedBmp != null && !cachedBmp.isRecycled) {
                builder.setLargeIcon(cachedBmp)
            }

            // Apply dynamic colors
            builder.setColor(accentColor)
            builder.setColorized(true)
            builder.setSmallIcon(R.drawable.mkrmusicnotification)
            builder.setOngoing(isPlaying)

            // Android 16 Status Chips implementation (API 36 or "Baklava")
            val isAndroid16 = Build.VERSION.SDK_INT >= 36 || Build.VERSION.CODENAME == "Baklava"
            if (isAndroid16) {
                val durationMs = player.duration
                val currentPosMs = player.currentPosition

                val formattedTime = if (durationMs != C.TIME_UNSET && durationMs > 0) {
                    val totalSeconds = durationMs / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
                } else {
                    null
                }

                builder.setCategory(Notification.CATEGORY_TRANSPORT)
                setRequestPromotedOngoingSafely(builder, true)
                builder.extras.putBoolean("android.requestPromotedOngoing", true)

                if (isPlaying) {
                    setShortCriticalTextSafely(builder, formattedTime ?: context.getString(R.string.playing_status))

                    if (durationMs != C.TIME_UNSET && durationMs > 0) {
                        val remainingMs = durationMs - currentPosMs
                        val endTime = System.currentTimeMillis() + remainingMs
                        builder.setWhen(endTime)
                        builder.setUsesChronometer(true)
                        setChronometerCountDownSafely(builder, true)
                    } else {
                        builder.setWhen(System.currentTimeMillis())
                        builder.setShowWhen(true)
                    }
                } else {
                    setShortCriticalTextSafely(builder, formattedTime ?: context.getString(R.string.paused_status))
                    builder.setShowWhen(false)
                    builder.setUsesChronometer(false)
                }
            }

            val updatedNotification = builder.build()
            reattachMediaSessionToken(mediaNotification.notification, updatedNotification)
            MediaNotification(mediaNotification.notificationId, updatedNotification)
        } catch (e: Exception) {
            Timber.tag("MkrNotificationProvider").e(e, "Error decorating media notification")
            mediaNotification
        }
    }

    private fun reattachMediaSessionToken(source: Notification, target: Notification) {
        if (Build.VERSION.SDK_INT >= 33) {
            source.extras.getParcelable(
                Notification.EXTRA_MEDIA_SESSION,
                android.media.session.MediaSession.Token::class.java
            )?.let {
                target.extras.putParcelable(Notification.EXTRA_MEDIA_SESSION, it)
            }
        } else {
            @Suppress("DEPRECATION")
            source.extras.getParcelable<android.media.session.MediaSession.Token>(
                Notification.EXTRA_MEDIA_SESSION
            )?.let {
                target.extras.putParcelable(Notification.EXTRA_MEDIA_SESSION, it)
            }
        }
    }

    override fun handleCustomCommand(session: MediaSession, action: String, extras: Bundle): Boolean =
        defaultProvider.handleCustomCommand(session, action, extras)

    private fun setShortCriticalTextSafely(builder: Notification.Builder, text: String) {
        try {
            val method = Notification.Builder::class.java.getMethod("setShortCriticalText", CharSequence::class.java)
            method.invoke(builder, text)
        } catch (e: Exception) {
            builder.extras.putCharSequence("android.shortCriticalText", text)
        }
    }

    private fun setChronometerCountDownSafely(builder: Notification.Builder, countDown: Boolean) {
        try {
            val method = Notification.Builder::class.java.getMethod(
                "setChronometerCountDown",
                Boolean::class.javaPrimitiveType
            )
            method.invoke(builder, countDown)
        } catch (e: Exception) {
            builder.extras.putBoolean("android.chronometerCountDown", countDown)
        }
    }

    private fun setRequestPromotedOngoingSafely(builder: Notification.Builder, promoted: Boolean) {
        try {
            val methodNames = arrayOf("setRequestPromotedOngoing", "setPromotedOngoing", "setOngoingActivity")
            for (name in methodNames) {
                try {
                    val method = Notification.Builder::class.java.getMethod(name, Boolean::class.javaPrimitiveType)
                    method.invoke(builder, promoted)
                    break
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
    }
}
