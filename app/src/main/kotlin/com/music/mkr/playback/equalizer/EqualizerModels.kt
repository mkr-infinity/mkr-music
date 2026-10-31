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
    const val PRESET_VOCAL_CLARITY_ID = "preset_vocal_clarity"

    val PRESET_FLAT = EqualizerPreset(
        id = PRESET_FLAT_ID,
        name = "Flat",
        isBuiltIn = true,
        bandLevelsMb = listOf(0, 0, 0, 0, 0),
        bassBoostStrength = 0,
        virtualizerStrength = 0,
    )

    val PRESET_BASS_BOOST = EqualizerPreset(
        id = PRESET_BASS_BOOST_ID,
        name = "Bass Boost",
        isBuiltIn = true,
        bandLevelsMb = listOf(600, 350, 0, 100, 200),
        bassBoostStrength = 600,
        virtualizerStrength = 0,
    )

    val PRESET_VOCAL_CLARITY = EqualizerPreset(
        id = PRESET_VOCAL_CLARITY_ID,
        name = "Vocal Clarity",
        isBuiltIn = true,
        bandLevelsMb = listOf(-200, 0, 450, 550, 300),
        bassBoostStrength = 0,
        virtualizerStrength = 250,
    )

    val BUILT_IN_PRESETS = listOf(
        PRESET_FLAT,
        PRESET_BASS_BOOST,
        PRESET_VOCAL_CLARITY,
    )
}
