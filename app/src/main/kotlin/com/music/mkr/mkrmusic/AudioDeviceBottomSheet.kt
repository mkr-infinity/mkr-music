@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.ui.component.EqualizerDialog
import com.music.mkr.ui.component.mkrPressFeedback
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
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.audio_output_title), style = MaterialTheme.typography.headlineSmall)
                when {
                    isLoading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 12.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.audio_output_detecting))
                    }

                    errorMessage != null -> Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(errorMessage.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = {
                                errorMessage = null
                                isLoading = true
                                refreshDevices()
                            }) { Text(stringResource(R.string.retry)) }
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            audioDevices.forEach { device ->
                                OutputDestinationTile(device, enabled = service != null) {
                                    service?.setPreferredAudioDevice(device.deviceId)
                                    refreshDevices()
                                }
                            }
                        }

                        ExpressiveVolumeCard(
                            currentVolume = currentVolume,
                            maxVolume = maxVolume,
                            onVolumeChange = { newValue ->
                                isUserDragging = true
                                currentVolume = newValue
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newValue.toInt(), 0)
                            },
                            onVolumeFinished = {
                                isUserDragging = false
                                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                            },
                        )

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                }
                            }) { Text(stringResource(R.string.audio_output_pair_bluetooth)) }
                            FilledTonalButton(onClick = { showEqualizerDialog = true }) {
                                Text(stringResource(R.string.equalizer))
                            }
                        }

                        val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
                            key = AudioQualityKey,
                            defaultValue = AudioQuality.AUTO,
                        )
                        Text(stringResource(R.string.audio_quality_title), style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                AudioQuality.AUTO to R.string.audio_quality_auto,
                                AudioQuality.HIGH to R.string.audio_quality_high,
                                AudioQuality.LOW to R.string.audio_quality_low,
                            ).forEach { (quality, label) ->
                                FilterChip(
                                    selected = audioQuality == quality,
                                    onClick = { onAudioQualityChange(quality) },
                                    label = { Text(stringResource(label)) },
                                )
                            }
                        }
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

@Composable
private fun OutputDestinationTile(device: AudioDevice, enabled: Boolean, onClick: () -> Unit) {
    val icon = when (device.type) {
        AudioDeviceType.BLUETOOTH -> R.drawable.bluetooth
        AudioDeviceType.WIRED_HEADPHONES -> R.drawable.headphones
        AudioDeviceType.USB_HEADSET -> R.drawable.usb
        AudioDeviceType.HDMI -> R.drawable.tv
        AudioDeviceType.PHONE_SPEAKER -> R.drawable.phone_android
        AudioDeviceType.EXTERNAL_SPEAKER -> R.drawable.speaker
    }
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier.fillMaxWidth().mkrPressFeedback(interactionSource = interaction),
        onClick = onClick,
        enabled = enabled && !device.isActive,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.large,
        color = if (device.isActive) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (device.isActive) colors.onPrimaryContainer else colors.onSurface,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(
                        stringResource(R.string.audio_output_selected).takeIf { device.isActive },
                        device.type.label.takeUnless { it.equals(device.name, ignoreCase = true) },
                        device.batteryLevel?.let { "$it%" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (device.isActive) {
                Icon(painterResource(R.drawable.check), contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ExpressiveVolumeCard(
    currentVolume: Float,
    maxVolume: Int,
    onVolumeChange: (Float) -> Unit,
    onVolumeFinished: () -> Unit,
) {
    var previousNonZeroVolume by rememberSaveable { mutableFloatStateOf(maxVolume * 0.5f) }
    LaunchedEffect(currentVolume) {
        if (currentVolume > 0f) previousNonZeroVolume = currentVolume
    }
    val percent = if (maxVolume > 0) (currentVolume * 100 / maxVolume).toInt() else 0
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                stringResource(R.string.audio_output_volume_percent, percent),
                style = MaterialTheme.typography.titleSmall,
            )
            Slider(
                value = currentVolume.coerceIn(0f, maxVolume.coerceAtLeast(1).toFloat()),
                onValueChange = onVolumeChange,
                onValueChangeFinished = onVolumeFinished,
                valueRange = 0f..maxVolume.coerceAtLeast(1).toFloat(),
                steps = (maxVolume - 1).coerceAtLeast(0),
                enabled = maxVolume > 0,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = {
                    onVolumeChange(if (currentVolume > 0f) 0f else previousNonZeroVolume.coerceIn(0f, maxVolume.toFloat()))
                    onVolumeFinished()
                }, enabled = maxVolume > 0) {
                    Text(stringResource(if (currentVolume > 0f) R.string.audio_output_mute else R.string.audio_output_unmute))
                }
                listOf(25, 50, 75, 100).forEach { preset ->
                    TextButton(onClick = {
                        onVolumeChange(maxVolume * preset / 100f)
                        onVolumeFinished()
                    }, enabled = maxVolume > 0) { Text("$preset%") }
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
