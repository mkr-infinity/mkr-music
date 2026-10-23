/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
 * Tapping opens the contextual [ShowMediaInfo] audio/codec details bottom sheet.
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

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = contentColor.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.22f)),
        onClick = {
            if (onClick != null) {
                onClick()
            } else if (!effectiveVideoId.isNullOrBlank()) {
                bottomSheetPageState.show {
                    ShowMediaInfo(effectiveVideoId)
                }
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
