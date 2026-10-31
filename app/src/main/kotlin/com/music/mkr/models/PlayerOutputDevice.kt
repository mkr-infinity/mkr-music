/*
 * MKR Music (2026)
 * GPL-3.0 License
 */

package com.music.mkr.models

import android.media.AudioDeviceInfo
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.music.mkr.R

enum class PlayerOutputDevice(
    @DrawableRes val iconRes: Int,
    val isProduct: Boolean,
    val defaultName: String,
) {
    Bluetooth(R.drawable.bluetooth, isProduct = true, defaultName = "Bluetooth"),
    Usb(R.drawable.usb, isProduct = true, defaultName = "USB"),
    Headset(R.drawable.headphones, isProduct = false, defaultName = "Headset"),
    Hdmi(R.drawable.home_speaker_devices, isProduct = false, defaultName = "HDMI"),
    BuiltinSpeaker(R.drawable.speaker, isProduct = false, defaultName = "Speaker"),
    Unknown(R.drawable.speaker, isProduct = false, defaultName = "Speaker"),
    ;

    companion object {
        fun from(info: AudioDeviceInfo?): PlayerOutputDevice =
            when (info?.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER,
                AudioDeviceInfo.TYPE_HEARING_AID -> Bluetooth

                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_USB_ACCESSORY -> Usb

                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> Headset

                AudioDeviceInfo.TYPE_HDMI,
                AudioDeviceInfo.TYPE_HDMI_ARC,
                AudioDeviceInfo.TYPE_HDMI_EARC -> Hdmi

                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE,
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> BuiltinSpeaker

                else -> Unknown
            }
    }
}

@Immutable
data class ActiveOutputDevice(
    val type: PlayerOutputDevice,
    val name: String,
)
