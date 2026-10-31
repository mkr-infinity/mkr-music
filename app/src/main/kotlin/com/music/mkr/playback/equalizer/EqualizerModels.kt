/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.equalizer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * High-fidelity equalizer preset representation in MKR Music.
 * Band levels and gains are measured in millibels (mB, where 100 mB = 1 dB).
 */
@Serializable
data class EqualizerPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isBuiltIn: Boolean = false,
    val bandLevelsMb: List<Int> = emptyList(),
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
)

/**
 * ArchiveTune compatible payload structures for seamless custom EQ import.
 */
@Serializable
data class ArchiveTuneProfile(
    val id: String? = null,
    val name: String? = null,
    val bandCenterFreqHz: List<Int> = emptyList(),
    val bandLevelsMb: List<Int> = emptyList(),
    val outputGainMb: Int = 0,
    val outputGainEnabled: Boolean? = null,
    val bassBoostStrength: Int = 0,
    val bassBoostEnabled: Boolean? = null,
    val virtualizerStrength: Int = 0,
    val virtualizerEnabled: Boolean? = null,
    val autoHeadroomEnabled: Boolean = false,
)

@Serializable
data class ArchiveTunePayload(
    @SerialName("profiles")
    val profiles: List<ArchiveTuneProfile> = emptyList(),
)

object EqualizerDefaults {
    const val PRESET_FLAT_ID = "preset_flat"
    const val PRESET_BASS_BOOST_ID = "preset_bass_boost"
    const val PRESET_PURE_CLARITY_ID = "preset_pure_clarity"
    const val PRESET_VOCAL_CLARITY_ID = "preset_pure_clarity"
    const val PRESET_MKR_SIGNATURE_ID = "preset_mkr_signature"
    const val PRESET_ELECTRONIC_ID = "preset_electronic"
    const val PRESET_ROCK_ID = "preset_rock"

    val PRESET_FLAT = EqualizerPreset(
        id = PRESET_FLAT_ID,
        name = "Flat",
        isBuiltIn = true,
        bandLevelsMb = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        bassBoostStrength = 0,
        virtualizerStrength = 0,
    )

    val PRESET_BASS_BOOST = EqualizerPreset(
        id = PRESET_BASS_BOOST_ID,
        name = "Bass Boost",
        isBuiltIn = true,
        bandLevelsMb = listOf(500, 400, 250, 100, 0, -50, 0, 100, 200, 300),
        bassBoostStrength = 500,
        virtualizerStrength = 0,
    )

    val PRESET_PURE_CLARITY = EqualizerPreset(
        id = PRESET_PURE_CLARITY_ID,
        name = "Pure Clarity",
        isBuiltIn = true,
        bandLevelsMb = listOf(100, 50, -80, -150, -50, 150, 250, 200, 150, 100),
        bassBoostStrength = 0,
        virtualizerStrength = 200,
    )

    val PRESET_MKR_SIGNATURE = EqualizerPreset(
        id = PRESET_MKR_SIGNATURE_ID,
        name = "MKR Signature",
        isBuiltIn = true,
        bandLevelsMb = listOf(250, 200, 80, -120, -50, 120, 180, 100, 280, 450),
        bassBoostStrength = 250,
        virtualizerStrength = 150,
    )

    val PRESET_ELECTRONIC = EqualizerPreset(
        id = PRESET_ELECTRONIC_ID,
        name = "Electronic",
        isBuiltIn = true,
        bandLevelsMb = listOf(350, 280, 120, -50, -150, 50, 180, 300, 400, 500),
        bassBoostStrength = 350,
        virtualizerStrength = 200,
    )

    val PRESET_ROCK = EqualizerPreset(
        id = PRESET_ROCK_ID,
        name = "Rock",
        isBuiltIn = true,
        bandLevelsMb = listOf(300, 220, 150, 50, -100, 120, 200, 250, 320, 380),
        bassBoostStrength = 300,
        virtualizerStrength = 100,
    )

    val BUILT_IN_PRESETS = listOf(
        PRESET_FLAT,
        PRESET_BASS_BOOST,
        PRESET_PURE_CLARITY,
        PRESET_MKR_SIGNATURE,
        PRESET_ELECTRONIC,
        PRESET_ROCK,
    )
}
