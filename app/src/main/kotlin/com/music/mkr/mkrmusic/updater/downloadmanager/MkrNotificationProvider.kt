package com.music.mkr.mkrmusic.updater.downloadmanager

import android.app.Notification
import android.content.Context
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
import com.music.mkr.R
import com.music.mkr.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

/**
 * Custom MediaNotification.Provider for MKR Music:
 * - Adapts notification colors dynamically to the current track's artwork palette.
 * - Handles asynchronous artwork loading without blocking or freezing playback.
 * - Automatically clears previous track colors and applies contrast-safe fallbacks.
 * - Works consistently in both playing and paused states.
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
        val mediaNotification = defaultProvider.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            onNotificationChangedCallback
        )

        return try {
            val player = mediaSession.player
            val isPlaying = player.playWhenReady && player.playbackState == Player.STATE_READY
            val currentMediaItem = player.currentMediaItem
            val mediaMetadata = currentMediaItem?.mediaMetadata
            val mediaId = currentMediaItem?.mediaId
            val artworkUri = mediaMetadata?.artworkUri?.toString()
                ?: mediaMetadata?.extras?.getString("artwork_uri")

            // Look up cached palette for active track
            val cacheKey = mediaId?.takeIf { it.isNotBlank() } ?: artworkUri?.takeIf { it.isNotBlank() }
            val cachedPalette = cacheKey?.let { PlayerColorExtractor.getCachedPalette(it) }

            // Asynchronously resolve palette if not yet available in cache
            if (cachedPalette == null && !artworkUri.isNullOrBlank()) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val palette = PlayerColorExtractor.extractPalette(context, artworkUri, mediaId)
                        withContext(Dispatchers.Main) {
                            val activeItem = mediaSession.player.currentMediaItem
                            if (activeItem?.mediaId == mediaId) {
                                val asyncBuilder = Notification.Builder.recoverBuilder(context, mediaNotification.notification)
                                val dynamicColor = palette.primaryAccent.toArgb()
                                asyncBuilder.setColor(dynamicColor)
                                asyncBuilder.setColorized(true)
                                asyncBuilder.setSmallIcon(R.drawable.mkrmusicnotification)
                                asyncBuilder.setOngoing(mediaSession.player.playWhenReady && mediaSession.player.playbackState == Player.STATE_READY)

                                val updatedNotif = asyncBuilder.build()
                                reattachMediaSessionToken(mediaNotification.notification, updatedNotif)
                                onNotificationChangedCallback.onNotificationChanged(
                                    MediaNotification(mediaNotification.notificationId, updatedNotif)
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.tag("MkrNotificationProvider").w(e, "Failed to asynchronously extract artwork color")
                    }
                }
            }

            // Derive dynamic color: extracted primary accent or brand warm amber fallback
            val accentColor = cachedPalette?.primaryAccent?.toArgb() ?: PlayerColorExtractor.WarmAmber.toArgb()

            val notification = mediaNotification.notification
            val builder = Notification.Builder.recoverBuilder(context, notification)

            // Dynamic color appearance synchronized with artwork
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
