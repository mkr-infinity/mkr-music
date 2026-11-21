package com.music.mkr.ui.component.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SnackbarManager {

    data class SnackbarEvent(
        val resolveMessage: (Context) -> String,
        val actionLabel: Int? = null,
        val onAction: (() -> Unit)? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val iconRes: Int? = null,
    )

    data class PlaylistVisuals(
        override val message: String,
        override val duration: SnackbarDuration,
        val iconRes: Int,
    ) : SnackbarVisuals {
        override val actionLabel: String? = null
        override val withDismissAction: Boolean = false
    }

    private val _events = MutableSharedFlow<SnackbarEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events = _events.asSharedFlow()

    fun show(message: String) {
        _events.tryEmit(SnackbarEvent(resolveMessage = { message }))
    }

    fun show(
        messageResource: Int,
        actionLabel: Int? = null,
        messageArgs: Any? = null,
        onAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        iconRes: Int? = null,
    ) {
        _events.tryEmit(
            SnackbarEvent(
                messageResource = messageResource,
                actionLabel = actionLabel,
                messageArgs = messageArgs,
                onAction = onAction,
                duration = duration,
                iconRes = iconRes,
            )
        )
    }
}
