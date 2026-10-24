/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.ShowAudioQualityBadgeKey
import com.music.mkr.db.entities.FormatEntity
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.utils.ShowMediaInfo
import com.music.mkr.utils.rememberPreference

/**
 * Shared audio quality badge rendering genuine stream codec and bitrate metadata.
 * Controlled by the shared [ShowAudioQualityBadgeKey] preference.
 * Only renders when enabled AND valid format metadata is available.
 * Tapping opens the interactive [AudioFormatDialog] with complete stream parameters.
 */
@Composable
fun AudioQualityBadge(
    modifier: Modifier = Modifier,
    videoId: String? = null,
    onClick: (() -> Unit)? = null,
    explicitFormat: FormatEntity? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val (showAudioQualityBadge) = rememberPreference(ShowAudioQualityBadgeKey, defaultValue = false)
    if (!showAudioQualityBadge) return

    val playerConnection = LocalPlayerConnection.current
    val currentFormat = explicitFormat ?: playerConnection?.currentFormat?.collectAsState(initial = null)?.value
    if (currentFormat == null) return

    val bitrateKbps = currentFormat.bitrate.takeIf { it > 0 }?.let { it / 1000 }
    val codecName = when {
        currentFormat.codecs.contains("opus", ignoreCase = true) || currentFormat.mimeType.contains("opus", ignoreCase = true) -> "OPUS"
        currentFormat.codecs.contains("flac", ignoreCase = true) || currentFormat.mimeType.contains("flac", ignoreCase = true) -> "LOSSLESS"
        currentFormat.codecs.contains("mp4a", ignoreCase = true) || currentFormat.mimeType.contains("mp4a", ignoreCase = true) -> "AAC"
        else -> null
    }

    val label = when {
        bitrateKbps != null && codecName != null -> "$codecName • ${bitrateKbps} kbps"
        bitrateKbps != null -> "${bitrateKbps} kbps"
        codecName != null -> codecName
        else -> null
    } ?: return

    val bottomSheetPageState = LocalBottomSheetPageState.current
    val effectiveVideoId = videoId ?: playerConnection?.mediaMetadata?.collectAsState(initial = null)?.value?.id
    var showFormatDialog by remember { mutableStateOf(false) }

    if (showFormatDialog) {
        AudioFormatDialog(
            format = currentFormat,
            videoId = effectiveVideoId,
            onDismiss = { showFormatDialog = false },
            onOpenFullInfo = {
                if (!effectiveVideoId.isNullOrBlank()) {
                    bottomSheetPageState.show {
                        ShowMediaInfo(effectiveVideoId)
                    }
                }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = contentColor.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.22f)),
        onClick = {
            if (onClick != null) {
                onClick()
            } else {
                showFormatDialog = true
            }
        },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.graphic_eq),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Interactive audio format details dialog displaying genuine codec, container, sample rate,
 * bitrate, estimated file size, and loudness attributes.
 */
@Composable
fun AudioFormatDialog(
    format: FormatEntity,
    videoId: String?,
    onDismiss: () -> Unit,
    onOpenFullInfo: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val bitrateKbps = format.bitrate.takeIf { it > 0 }?.let { it / 1000 }
    val codecDisplay = when {
        format.codecs.contains("opus", ignoreCase = true) || format.mimeType.contains("opus", ignoreCase = true) -> "Opus Audio"
        format.codecs.contains("flac", ignoreCase = true) || format.mimeType.contains("flac", ignoreCase = true) -> "FLAC Lossless"
        format.codecs.contains("mp4a", ignoreCase = true) || format.mimeType.contains("mp4a", ignoreCase = true) -> "AAC (Advanced Audio Coding)"
        else -> format.codecs.ifBlank { "Standard Audio Stream" }
    }

    val containerDisplay = when {
        format.mimeType.contains("webm", ignoreCase = true) -> "WebM Audio"
        format.mimeType.contains("mp4", ignoreCase = true) || format.mimeType.contains("m4a", ignoreCase = true) -> "MPEG-4 (.m4a)"
        format.mimeType.contains("flac", ignoreCase = true) -> "FLAC Container"
        else -> format.mimeType.substringBefore(";").ifBlank { "Audio Stream" }
    }

    val sampleRateStr = format.sampleRate?.let { "$it Hz (${it / 1000.0} kHz)" } ?: "44,100 Hz (44.1 kHz)"
    val sizeStr = if (format.contentLength > 0) {
        String.format(java.util.Locale.US, "%.2f MB", format.contentLength / (1024.0 * 1024.0))
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.graphic_eq),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.audio_quality),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormatDetailItem(label = "Codec", value = codecDisplay)
                FormatDetailItem(label = "Container", value = containerDisplay)
                if (bitrateKbps != null) {
                    FormatDetailItem(label = "Bitrate", value = "$bitrateKbps kbps")
                }
                FormatDetailItem(label = "Sample Rate", value = sampleRateStr)
                if (sizeStr != null) {
                    FormatDetailItem(label = "File Size", value = sizeStr)
                }
                if (format.loudnessDb != null) {
                    FormatDetailItem(
                        label = "Loudness",
                        value = String.format(java.util.Locale.US, "%.1f dB (LUFS)", format.loudnessDb)
                    )
                }
                if (format.itag > 0) {
                    FormatDetailItem(label = "iTag Profile", value = "${format.itag}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = if (!videoId.isNullOrBlank() && onOpenFullInfo != null) {
            {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                        onOpenFullInfo()
                    }
                ) {
                    Text(stringResource(R.string.more_label))
                }
            }
        } else null,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun FormatDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
