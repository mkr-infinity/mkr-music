@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.mkrmusic

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.ui.component.EqualizerDialog
import com.music.mkr.ui.component.mkrPressFeedback
import com.music.mkr.utils.oem.SystemMediaControlResolver
import com.music.mkr.utils.rememberEnumPreference

data class AudioDevice(
    val name: String,
    val type: AudioDeviceType,
    val isConnected: Boolean,
    val isActive: Boolean = false,
    val batteryLevel: Int? = null,
    val deviceId: Int? = null,
)

enum class AudioDeviceType(val label: String) {
    BLUETOOTH("Bluetooth Audio"),
    WIRED_HEADPHONES("Wired Audio"),
    PHONE_SPEAKER("Built-in Speaker"),
    EXTERNAL_SPEAKER("External Speaker"),
    USB_HEADSET("USB-C Audio"),
    HDMI("HDMI Audio"),
}

/**
 * Expressive MKR Music Audio Output & Routing Screen:
 * - Redesigned from the ground up with an original visual hierarchy and polished brand aesthetic.
 * - Current Output Showcase centerpiece displaying actual device names and dynamic equalizer bars.
 * - Fluid Tactile Volume Controller with gestural drag/tap fader, inline mute toggle, and stepped presets.
 * - Distinct Output Destination Tiles for seamless one-tap hardware switching with clear selection feedback.
 * - Quick-access external controls for Bluetooth device pairing and OEM system media output switcher.
 * - Stream quality configuration and full Android hardware routing preservation.
 */
@Composable
fun AudioDeviceBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var audioDevices by remember { mutableStateOf<List<AudioDevice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }
    var isUserDragging by remember { mutableStateOf(false) }
    val maxVolume by remember { mutableStateOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }
    var showEqualizerDialog by rememberSaveable { mutableStateOf(false) }

    val playerConnection = LocalPlayerConnection.current
    val service = playerConnection?.service

    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadDevices(context, service?.preferredDeviceId, onSuccess = { devices ->
                audioDevices = devices
                isLoading = false
            }, onError = { error ->
                errorMessage = error
                isLoading = false
            })
        } else {
            errorMessage = context.getString(R.string.bluetooth_permission_required)
            isLoading = false
        }
    }

    fun refreshDevices() {
        loadDevices(context, service?.preferredDeviceId, onSuccess = { devices ->
            audioDevices = devices
            isLoading = false
        }, onError = { err ->
            errorMessage = err
            isLoading = false
        })
    }

    DisposableEffect(Unit) {
        val volumeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                    if (streamType == AudioManager.STREAM_MUSIC && !isUserDragging) {
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                    }
                }
            }
        }

        val audioDeviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refreshDevices()
            }
        }

        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                volumeChangeReceiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Failed to register volumeChangeReceiver")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                androidx.core.content.ContextCompat.registerReceiver(
                    context,
                    audioDeviceReceiver,
                    IntentFilter().apply {
                        addAction(AudioManager.ACTION_HEADSET_PLUG)
                        addAction(AudioManager.ACTION_HDMI_AUDIO_PLUG)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                        }
                    },
                    androidx.core.content.ContextCompat.RECEIVER_EXPORTED
                )
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to register audioDeviceReceiver")
            }
        }

        if (checkBluetoothPermission(context)) {
            loadDevices(context, service?.preferredDeviceId, onSuccess = { devices ->
                audioDevices = devices
                isLoading = false
            }, onError = { error ->
                errorMessage = error
                isLoading = false
            })
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        val handler = Handler(Looper.getMainLooper())

        val audioDeviceCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : android.media.AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    refreshDevices()
                }
                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    refreshDevices()
                }
            }
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)
        }

        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refreshDevices()
                handler.postDelayed({ refreshDevices() }, 1000)
                handler.postDelayed({ refreshDevices() }, 2500)
            }
        }

        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                bluetoothReceiver,
                IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                    addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                },
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Failed to register bluetoothReceiver")
        }

        val batteryPollingRunnable = object : Runnable {
            override fun run() {
                refreshDevices()
                handler.postDelayed(this, 30000)
            }
        }
        handler.postDelayed(batteryPollingRunnable, 30000)

        onDispose {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
                    audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
                }
                context.unregisterReceiver(volumeChangeReceiver)
                context.unregisterReceiver(audioDeviceReceiver)
                context.unregisterReceiver(bluetoothReceiver)
                handler.removeCallbacksAndMessages(null)
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .animateContentSize()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.home_speaker_devices),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Audio Output",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Playback destinations",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(42.dp),
                                    strokeWidth = 3.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Detecting audio outputs...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.error),
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = errorMessage ?: "Audio routing error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        isLoading = true
                                        refreshDevices()
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text(text = stringResource(R.string.retry), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        val activeDevice = audioDevices.firstOrNull { it.isActive }
                            ?: audioDevices.firstOrNull()

                        // ==========================================
                        // 1. ACTIVE OUTPUT SHOWCASE CARD
                        // ==========================================
                        activeDevice?.let { device ->
                            ActiveOutputShowcaseCard(
                                device = device,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ==========================================
                        // 2. EXPRESSIVE FLUID VOLUME CONTROLLER
                        // ==========================================
                        ExpressiveVolumeCard(
                            currentVolume = currentVolume,
                            maxVolume = maxVolume,
                            onVolumeChange = { newValue ->
                                currentVolume = newValue
                                isUserDragging = true
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newValue.toInt(), 0)
                            },
                            onVolumeFinished = {
                                isUserDragging = false
                                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ==========================================
                        // 3. AVAILABLE AUDIO DESTINATIONS
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Available Outputs",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = "${audioDevices.size} outputs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            audioDevices.forEach { dev ->
                                OutputDestinationTile(
                                    device = dev,
                                    onClick = {
                                        if (!dev.isActive) {
                                            service?.setPreferredAudioDevice(dev.deviceId)
                                            refreshDevices()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ==========================================
                        // 4. EXTERNAL AUDIO ROUTING & BLUETOOTH
                        // ==========================================
                        AudioRoutingActionsCard(
                            onOpenBluetoothSettings = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                }
                            },
                            onOpenEqualizer = {
                                showEqualizerDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // ==========================================
                        // 5. AUDIO STREAM QUALITY SELECTOR
                        // ==========================================
                        val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
                            key = AudioQualityKey,
                            defaultValue = AudioQuality.AUTO
                        )

                        AudioQualityCard(
                            selectedQuality = audioQuality,
                            onQualityChange = onAudioQualityChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showEqualizerDialog) {
        EqualizerDialog(
            onDismiss = { showEqualizerDialog = false }
        )
    }
}

/**
 * Centerpiece Showcase Card for the currently active audio output.
 */
@Composable
private fun ActiveOutputShowcaseCard(
    device: AudioDevice,
    modifier: Modifier = Modifier,
) {
    @DrawableRes val deviceIconRes = when (device.type) {
        AudioDeviceType.BLUETOOTH -> R.drawable.bluetooth
        AudioDeviceType.WIRED_HEADPHONES -> R.drawable.headphones
        AudioDeviceType.USB_HEADSET -> R.drawable.usb
        AudioDeviceType.HDMI -> R.drawable.tv
        AudioDeviceType.PHONE_SPEAKER -> R.drawable.phone_android
        AudioDeviceType.EXTERNAL_SPEAKER -> R.drawable.speaker
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shadowElevation = 3.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle ambient radial glow behind hero
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            radius = 350f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row: Label + Active Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CURRENT OUTPUT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Middle row: Icon with equalizers + Device Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Elevated Icon Badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(deviceIconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.5.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                initialDelayMillis = 2000,
                                velocity = 25.dp
                            )
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = device.type.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (device.batteryLevel != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${device.batteryLevel}% battery",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Dynamic Mini Audio Equalizer Bars
                    ActiveEqualizerVisualizer(
                        modifier = Modifier
                            .height(30.dp)
                            .width(26.dp)
                    )
                }
            }
        }
    }
}

/**
 * Lightweight dynamic equalizer visualizer indicating active playback.
 */
@Composable
private fun ActiveEqualizerVisualizer(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizerTransition")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(560, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.75f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(620, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar4"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3, bar4).forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryColor)
            )
        }
    }
}

/**
 * Expressive tactile volume card with fluid fader capsule, quick mute, and stepped presets.
 */
@Composable
private fun ExpressiveVolumeCard(
    currentVolume: Float,
    maxVolume: Int,
    onVolumeChange: (Float) -> Unit,
    onVolumeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (maxVolume > 0) ((currentVolume / maxVolume) * 100).toInt().coerceIn(0, 100) else 0
    var previousNonZeroVolume by rememberSaveable {
        mutableFloatStateOf(if (currentVolume > 0) currentVolume else (maxVolume * 0.5f))
    }

    if (currentVolume > 0f) {
        previousNonZeroVolume = currentVolume
    }

    var barWidthPx by remember { mutableFloatStateOf(1f) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Dynamic Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            when {
                                currentVolume <= 0f -> R.drawable.volume_off
                                currentVolume < maxVolume * 0.5f -> R.drawable.volume_down
                                else -> R.drawable.volume_up
                            }
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Output Volume",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Tactile Fluid Volume Bar with Drag & Tap
            val fillFraction = if (maxVolume > 0) (currentVolume / maxVolume).coerceIn(0f, 1f) else 0f
            val animatedFillFraction by animateFloatAsState(
                targetValue = fillFraction,
                animationSpec = tween(120),
                label = "volumeFillFraction"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
                    .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                    .pointerInput(maxVolume) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                            val newVol = fraction * maxVolume
                            onVolumeChange(newVol)
                            onVolumeFinished()
                        }
                    }
                    .pointerInput(maxVolume) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                                onVolumeChange(fraction * maxVolume)
                            },
                            onDragEnd = { onVolumeFinished() },
                            onDragCancel = { onVolumeFinished() },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                                onVolumeChange(fraction * maxVolume)
                            }
                        )
                    }
            ) {
                // Active Gradient Fill
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFillFraction)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )

                // Foreground Controls inside the bar
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mute / Unmute Quick Button
                    IconButton(
                        onClick = {
                            if (currentVolume > 0f) {
                                previousNonZeroVolume = currentVolume
                                onVolumeChange(0f)
                            } else {
                                onVolumeChange(previousNonZeroVolume.coerceAtLeast(1f))
                            }
                            onVolumeFinished()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (currentVolume <= 0f) R.drawable.volume_off else R.drawable.volume_mute
                            ),
                            contentDescription = if (currentVolume <= 0f) "Unmute" else "Mute",
                            tint = if (animatedFillFraction > 0.15f) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Level indicator
                    Text(
                        text = "${currentVolume.toInt()} / $maxVolume",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = if (animatedFillFraction > 0.85f) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Stepped Presets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Mute" to 0f,
                    "25%" to (maxVolume * 0.25f),
                    "50%" to (maxVolume * 0.50f),
                    "75%" to (maxVolume * 0.75f),
                    "100%" to maxVolume.toFloat()
                ).forEach { (label, targetVol) ->
                    val isSelected = kotlin.math.abs(currentVolume - targetVol) < 0.6f
                    Surface(
                        onClick = {
                            onVolumeChange(targetVol)
                            onVolumeFinished()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                      else MaterialTheme.colorScheme.onSurfaceVariant,
                        border = if (isSelected) null else BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .mkrPressFeedback()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual destination item tile for selecting/switching audio route.
 */
@Composable
private fun OutputDestinationTile(
    device: AudioDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @DrawableRes val deviceIconRes = when (device.type) {
        AudioDeviceType.BLUETOOTH -> R.drawable.bluetooth
        AudioDeviceType.WIRED_HEADPHONES -> R.drawable.headphones
        AudioDeviceType.USB_HEADSET -> R.drawable.usb
        AudioDeviceType.HDMI -> R.drawable.tv
        AudioDeviceType.PHONE_SPEAKER -> R.drawable.phone_android
        AudioDeviceType.EXTERNAL_SPEAKER -> R.drawable.speaker
    }

    val isActive = device.isActive
    val cardColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBorder = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                     else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        border = cardBorder,
        modifier = modifier
            .fillMaxWidth()
            .mkrPressFeedback()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Device Type Icon Pill
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(deviceIconRes),
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Name and Category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 2000,
                        velocity = 25.dp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = device.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (device.batteryLevel != null) {
                        Text(
                            text = "${device.batteryLevel}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // State Action Indicator
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "Switch",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quick-access card for pairing Bluetooth devices or launching Equalizer.
 */
@Composable
private fun AudioRoutingActionsCard(
    onOpenBluetoothSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "External Controls",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bluetooth Pairing Button
                Surface(
                    onClick = onOpenBluetoothSettings,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .mkrPressFeedback()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.bluetooth),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pair Bluetooth",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Equalizer Button
                Surface(
                    onClick = onOpenEqualizer,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .mkrPressFeedback()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.graphic_eq),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stream quality preference controls.
 */
@Composable
private fun AudioQualityCard(
    selectedQuality: AudioQuality,
    onQualityChange: (AudioQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.audio_quality_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (selectedQuality) {
                        AudioQuality.AUTO -> "Intelligent"
                        AudioQuality.HIGH -> "High (256kbps)"
                        AudioQuality.LOW -> "Data Saver"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AudioQuality.AUTO to stringResource(R.string.audio_quality_auto),
                    AudioQuality.HIGH to stringResource(R.string.audio_quality_high),
                    AudioQuality.LOW to stringResource(R.string.audio_quality_low)
                ).forEach { (quality, label) ->
                    val isChecked = selectedQuality == quality
                    Surface(
                        onClick = { onQualityChange(quality) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (isChecked) MaterialTheme.colorScheme.onPrimary
                                      else MaterialTheme.colorScheme.onSurface,
                        border = if (isChecked) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .mkrPressFeedback()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun loadDevices(
    context: Context,
    preferredDeviceId: Int?,
    onSuccess: (List<AudioDevice>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = mutableListOf<AudioDevice>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

            audioDevices.forEach { deviceInfo ->
                val device = when (deviceInfo.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLE_HEADSET,
                    AudioDeviceInfo.TYPE_BLE_SPEAKER,
                    AudioDeviceInfo.TYPE_HEARING_AID -> {
                        val batteryLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val bluetoothManager = context.getSystemService(
                                        Context.BLUETOOTH_SERVICE
                                    ) as BluetoothManager
                                    val bluetoothAdapter = bluetoothManager.adapter
                                    val pairedDevices = bluetoothAdapter?.bondedDevices
                                    val btDevice = pairedDevices?.find {
                                        it.name == deviceInfo.productName?.toString()
                                    }

                                    @SuppressLint("MissingPermission")
                                    val battery = btDevice?.let { bDev ->
                                        try {
                                            val method = BluetoothDevice::class.java.getMethod("getBatteryLevel")
                                            val level = method.invoke(bDev) as? Int
                                            level
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    if (battery != null && battery in 0..100) battery else null
                                } else null
                            } catch (_: Exception) {
                                null
                            }
                        } else null

                        AudioDevice(
                            name = resolveAudioDeviceName(context, deviceInfo, AudioDeviceType.BLUETOOTH),
                            type = AudioDeviceType.BLUETOOTH,
                            isConnected = true,
                            isActive = false,
                            batteryLevel = batteryLevel,
                            deviceId = deviceInfo.id
                        )
                    }

                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                        AudioDevice(
                            name = resolveAudioDeviceName(context, deviceInfo, AudioDeviceType.WIRED_HEADPHONES),
                            type = AudioDeviceType.WIRED_HEADPHONES,
                            isConnected = true,
                            isActive = false,
                            deviceId = deviceInfo.id
                        )
                    }

                    AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_ACCESSORY -> {
                        AudioDevice(
                            name = resolveAudioDeviceName(context, deviceInfo, AudioDeviceType.USB_HEADSET),
                            type = AudioDeviceType.USB_HEADSET,
                            isConnected = true,
                            isActive = false,
                            deviceId = deviceInfo.id
                        )
                    }

                    AudioDeviceInfo.TYPE_HDMI, AudioDeviceInfo.TYPE_HDMI_ARC, AudioDeviceInfo.TYPE_HDMI_EARC -> {
                        AudioDevice(
                            name = resolveAudioDeviceName(context, deviceInfo, AudioDeviceType.HDMI),
                            type = AudioDeviceType.HDMI,
                            isConnected = true,
                            isActive = false,
                            deviceId = deviceInfo.id
                        )
                    }

                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE, AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                        AudioDevice(
                            name = resolveAudioDeviceName(context, deviceInfo, AudioDeviceType.PHONE_SPEAKER),
                            type = AudioDeviceType.PHONE_SPEAKER,
                            isConnected = true,
                            isActive = false,
                            deviceId = deviceInfo.id
                        )
                    }

                    else -> null
                }
                device?.let { devices.add(it) }
            }

            val activeDevice = determineActiveDevice(audioManager, audioDevices, preferredDeviceId)
            val updatedDevices = devices.map { device ->
                device.copy(isActive = device.deviceId == activeDevice?.id)
            }

            val sortedDevices = updatedDevices.sortedWith(
                compareByDescending<AudioDevice> { it.isActive }
                    .thenBy { it.type.ordinal }
                    .thenBy { it.name }
            )

            onSuccess(sortedDevices.distinctBy { it.name })
        } else {
            loadDevicesLegacy(context, onSuccess, onError)
        }
    } catch (e: Exception) {
        onError("Failed to load devices: ${e.message}")
    }
}

private fun determineActiveDevice(
    audioManager: AudioManager,
    audioDevices: Array<AudioDeviceInfo>,
    preferredDeviceId: Int?
): AudioDeviceInfo? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val preferred = if (preferredDeviceId != null) {
            audioDevices.find { it.id == preferredDeviceId }
        } else null

        if (preferred != null) return preferred

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val mediaAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val currentRoutingSink = runCatching {
                audioManager.getAudioDevicesForAttributes(mediaAttributes).firstOrNull { it.isSink }
            }.getOrNull()
            if (currentRoutingSink != null) {
                val match = audioDevices.find { it.id == currentRoutingSink.id }
                if (match != null) return match
            }
        }

        return when {
            audioDevices.any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                it.type == AudioDeviceInfo.TYPE_HEARING_AID
            } ->
                audioDevices.find {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                    it.type == AudioDeviceInfo.TYPE_HEARING_AID
                }

            audioDevices.any {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
            } ->
                audioDevices.find {
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                }

            audioDevices.any {
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
            } ->
                audioDevices.find {
                    it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                    it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
                }

            audioDevices.any {
                it.type == AudioDeviceInfo.TYPE_HDMI ||
                it.type == AudioDeviceInfo.TYPE_HDMI_ARC ||
                it.type == AudioDeviceInfo.TYPE_HDMI_EARC
            } ->
                audioDevices.find {
                    it.type == AudioDeviceInfo.TYPE_HDMI ||
                    it.type == AudioDeviceInfo.TYPE_HDMI_ARC ||
                    it.type == AudioDeviceInfo.TYPE_HDMI_EARC
                }

            else -> audioDevices.find {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER ||
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE ||
                it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
            }
        }
    }
    return null
}

private fun resolveAudioDeviceName(
    context: Context,
    deviceInfo: AudioDeviceInfo,
    type: AudioDeviceType
): String {
    val productName = deviceInfo.productName?.toString()?.trim()

    return when (type) {
        AudioDeviceType.BLUETOOTH -> {
            if (!productName.isNullOrBlank() && !productName.equals("bluetooth", ignoreCase = true)) {
                productName
            } else {
                "Bluetooth Audio"
            }
        }
        AudioDeviceType.WIRED_HEADPHONES -> {
            if (!productName.isNullOrBlank() &&
                !productName.equals("headset", ignoreCase = true) &&
                !productName.equals("headphones", ignoreCase = true)
            ) {
                productName
            } else {
                context.getString(R.string.wired_headphones)
            }
        }
        AudioDeviceType.USB_HEADSET -> {
            if (!productName.isNullOrBlank() && !productName.equals("usb", ignoreCase = true)) {
                productName
            } else {
                "USB Audio"
            }
        }
        AudioDeviceType.HDMI -> {
            if (!productName.isNullOrBlank()) {
                productName
            } else {
                context.getString(R.string.hdmi)
            }
        }
        AudioDeviceType.PHONE_SPEAKER -> {
            if (!productName.isNullOrBlank() &&
                !productName.equals("speaker", ignoreCase = true) &&
                !productName.equals("phone speaker", ignoreCase = true) &&
                !productName.equals("built-in speaker", ignoreCase = true)
            ) {
                productName
            } else {
                getDeviceModelName()
            }
        }
        AudioDeviceType.EXTERNAL_SPEAKER -> {
            if (!productName.isNullOrBlank()) productName else "External Speaker"
        }
    }
}

private fun getDeviceModelName(): String {
    val model = Build.MODEL?.trim() ?: ""
    val manufacturer = Build.MANUFACTURER?.trim() ?: ""
    return when {
        model.isBlank() && manufacturer.isBlank() -> "Phone Speaker"
        model.startsWith(manufacturer, ignoreCase = true) -> "$model Speaker"
        manufacturer.isNotBlank() && model.isNotBlank() -> "${manufacturer.replaceFirstChar { it.uppercase() }} $model Speaker"
        model.isNotBlank() -> "$model Speaker"
        else -> "Phone Speaker"
    }
}

@Suppress("DEPRECATION")
private fun loadDevicesLegacy(context: Context, onSuccess: (List<AudioDevice>) -> Unit, onError: (String) -> Unit) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = mutableListOf<AudioDevice>()

    if (audioManager.isBluetoothA2dpOn) {
        devices.add(AudioDevice("Bluetooth Device", AudioDeviceType.BLUETOOTH, true, true))
    }
    if (audioManager.isWiredHeadsetOn) {
        devices.add(AudioDevice("Wired Headphones", AudioDeviceType.WIRED_HEADPHONES, true, true))
    }
    if (audioManager.isSpeakerphoneOn) {
        devices.add(AudioDevice("External Speaker", AudioDeviceType.EXTERNAL_SPEAKER, true, true))
    }
    if (devices.isEmpty() || !devices.any { it.isActive }) {
        devices.add(AudioDevice(getDeviceModelName(), AudioDeviceType.PHONE_SPEAKER, true, true))
    }
    onSuccess(devices.filter { it.isActive }.take(1))
}

private fun checkBluetoothPermission(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED
} else true
