/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.playback.equalizer.EqualizerController
import com.music.mkr.playback.equalizer.EqualizerPreset
import java.util.Locale

/**
 * Built-in MKR Music Hardware Equalizer Bottom Sheet.
 * Direct interface with native Android Equalizer, Bass Boost, and Virtualizer
 * attached to the active playback audio session ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current
    val equalizerController = playerConnection?.equalizerController
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (content.isNullOrBlank()) {
                    Toast.makeText(context, "Selected file is empty", Toast.LENGTH_SHORT).show()
                } else {
                    val result = equalizerController?.importPresets(content)
                    if (result != null && result.isSuccess) {
                        val count = result.getOrDefault(1)
                        Toast.makeText(context, "Successfully imported $count preset(s)", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = result?.exceptionOrNull()?.localizedMessage ?: "Invalid equalizer file format"
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
            ) {
                if (equalizerController == null) {
                    EqualizerUnavailableCard()
                } else {
                    EqualizerContent(
                        controller = equalizerController,
                        onImportClick = {
                            try {
                                importLauncher.launch("*/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to launch file selector", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun EqualizerContent(
    controller: EqualizerController,
    onImportClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isEnabled by controller.isEnabled.collectAsState()
    val isSessionActive by controller.isSessionActive.collectAsState()
    val currentSessionId by controller.currentSessionId.collectAsState()
    val bandCount by controller.bandCount.collectAsState()
    val bandFrequenciesHz by controller.bandFrequenciesHz.collectAsState()
    val bandLevelRangeMb by controller.bandLevelRangeMb.collectAsState()
    val currentBandLevelsMb by controller.currentBandLevelsMb.collectAsState()
    val bassBoostStrength by controller.bassBoostStrength.collectAsState()
    val virtualizerStrength by controller.virtualizerStrength.collectAsState()
    val activePresetId by controller.activePresetId.collectAsState()
    val allPresets by controller.allPresets.collectAsState()
    val playerConnection = LocalPlayerConnection.current
    val mediaMetadata by (playerConnection?.mediaMetadata ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    val songTitle = mediaMetadata?.title

    // 1. Header with Master Switch
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.equalizer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column {
                Text(
                    text = "Equalizer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Hardware acoustic tuning & effects",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { controller.setEnabled(it) },
        )
    }

    // 2. Audio Session Status Banner
    AudioSessionStatusCard(
        isSessionActive = isSessionActive,
        sessionId = currentSessionId,
        bandCount = bandCount,
        songTitle = songTitle,
    )

    Spacer(modifier = Modifier.height(18.dp))

    // 3. Preset Selector Section
    Text(
        text = "Presets",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        allPresets.forEach { preset ->
            val isSelected = preset.id == activePresetId
            PresetChip(
                preset = preset,
                isSelected = isSelected,
                enabled = isEnabled,
                onClick = { controller.applyPreset(preset) },
                onDelete = if (!preset.isBuiltIn) {
                    { controller.deleteCustomPreset(preset.id) }
                } else null,
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 4. Frequency Bands
    Text(
        text = "Frequency Bands",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val minMb = bandLevelRangeMb.first
            val maxMb = bandLevelRangeMb.second

            for (b in 0 until bandCount) {
                val freqHz = bandFrequenciesHz.getOrNull(b) ?: (60 * (b + 1))
                val currentMb = currentBandLevelsMb.getOrNull(b) ?: 0
                val freqText = formatFrequency(freqHz)
                val dbText = formatMillibelsToDb(currentMb)

                BandRow(
                    label = freqText,
                    dbValueText = dbText,
                    currentMb = currentMb,
                    minMb = minMb,
                    maxMb = maxMb,
                    enabled = isEnabled,
                    onValueChange = { newMb ->
                        controller.setBandLevel(b, newMb)
                    },
                )

                if (b < bandCount - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Sound Enhancements (Bass Boost & Virtualizer)
    Text(
        text = "Sound Enhancements",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Bass Boost
            EnhancementSliderRow(
                title = "Bass Boost",
                iconRes = R.drawable.volume_up,
                strength = bassBoostStrength,
                enabled = isEnabled,
                onValueChange = { controller.setBassBoostStrength(it) },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Virtualizer
            EnhancementSliderRow(
                title = "Surround Virtualizer",
                iconRes = R.drawable.headset_applemusic,
                strength = virtualizerStrength,
                enabled = isEnabled,
                onValueChange = { controller.setVirtualizerStrength(it) },
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 6. Action Buttons: Custom Import & Reset
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onImportClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.download),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Import Preset",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Button(
            onClick = { controller.resetToFlat() },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(text = "Reset to Flat")
        }
    }
}

@Composable
private fun AudioSessionStatusCard(
    isSessionActive: Boolean,
    sessionId: Int,
    bandCount: Int,
    songTitle: String?,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSessionActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        },
        animationSpec = tween(300),
        label = "sessionCardColor",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSessionActive) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(if (isSessionActive) R.drawable.graphic_eq else R.drawable.volume_up),
                contentDescription = null,
                tint = if (isSessionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSessionActive) {
                        "Session Active $bandCount Bands"
                    } else {
                        "No Active Audio Session"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isSessionActive) {
                        if (!songTitle.isNullOrBlank()) {
                            "Audio effects active for $songTitle"
                        } else {
                            "Audio effects active for current track"
                        }
                    } else {
                        "Play music to enable live hardware equalizer processing"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: EqualizerPreset,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                ),
            )

            if (onDelete != null) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Delete preset",
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete),
                )
            }
        }
    }
}

@Composable
private fun BandRow(
    label: String,
    dbValueText: String,
    currentMb: Int,
    minMb: Int,
    maxMb: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = dbValueText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                ),
                color = if (currentMb != 0 && enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Slider(
            value = currentMb.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = minMb.toFloat()..maxMb.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
    }
}

@Composable
private fun EnhancementSliderRow(
    title: String,
    iconRes: Int,
    strength: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    val percent = (strength / 10).coerceIn(0, 100)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (enabled && strength > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                ),
                color = if (enabled && strength > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Slider(
            value = strength.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
    }
}

@Composable
private fun EqualizerUnavailableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Audio Service Connecting",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "The audio playback service is currently initializing. Please try opening the equalizer again in a moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatFrequency(hz: Int): String {
    return if (hz >= 1000) {
        val kHz = hz / 1000.0
        if (kHz % 1.0 == 0.0) {
            "${kHz.toInt()} kHz"
        } else {
            String.format(Locale.US, "%.1f kHz", kHz)
        }
    } else {
        "$hz Hz"
    }
}

private fun formatMillibelsToDb(mb: Int): String {
    val db = mb / 100.0
    return if (db > 0) {
        String.format(Locale.US, "+%.1f dB", db)
    } else if (db < 0) {
        String.format(Locale.US, "%.1f dB", db)
    } else {
        "0.0 dB"
    }
}
