/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.equalizer

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * High-performance audio effect controller for MKR Music.
 * Directly interfaces with native Android Equalizer, BassBoost, and Virtualizer
 * attached to the active ExoPlayer audioSessionId.
 */
@Singleton
class EqualizerController @Inject constructor(
    @ApplicationContext private val context: Context,
    val repository: EqualizerPresetRepository,
) {
    private companion object {
        private const val TAG = "EqualizerController"
        private const val DEFAULT_BAND_COUNT = 5
        private val FALLBACK_FREQS = listOf(60, 230, 910, 3600, 14000)
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _currentSessionId = MutableStateFlow(0)
    val currentSessionId: StateFlow<Int> = _currentSessionId.asStateFlow()

    private val _bandCount = MutableStateFlow(DEFAULT_BAND_COUNT)
    val bandCount: StateFlow<Int> = _bandCount.asStateFlow()

    private val _bandFrequenciesHz = MutableStateFlow(FALLBACK_FREQS)
    val bandFrequenciesHz: StateFlow<List<Int>> = _bandFrequenciesHz.asStateFlow()

    private val _bandLevelRangeMb = MutableStateFlow(-1500 to 1500)
    val bandLevelRangeMb: StateFlow<Pair<Int, Int>> = _bandLevelRangeMb.asStateFlow()

    private val _currentBandLevelsMb = MutableStateFlow(List(DEFAULT_BAND_COUNT) { 0 })
    val currentBandLevelsMb: StateFlow<List<Int>> = _currentBandLevelsMb.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    val isEnabled: StateFlow<Boolean> = repository.isEnabled
    val activePresetId: StateFlow<String> = repository.activePresetId
    val allPresets: StateFlow<List<EqualizerPreset>> = repository.allPresets

    init {
        // Load initial values from repository
        val initialPreset = repository.getActivePreset()
        val savedLevels = repository.getSavedBandLevels() ?: initialPreset.bandLevelsMb
        _currentBandLevelsMb.value = resampleLevels(savedLevels, DEFAULT_BAND_COUNT)
        _bassBoostStrength.value = repository.getSavedBassBoost().takeIf { it > 0 } ?: initialPreset.bassBoostStrength
        _virtualizerStrength.value = repository.getSavedVirtualizer().takeIf { it > 0 } ?: initialPreset.virtualizerStrength
    }

    /**
     * Connects audio effects to the active audio session ID.
     */
    @Synchronized
    fun attachSession(sessionId: Int) {
        if (sessionId <= 0) {
            Timber.tag(TAG).w("attachSession called with invalid sessionId: $sessionId")
            return
        }

        if (_currentSessionId.value == sessionId && equalizer != null) {
            Timber.tag(TAG).d("Session $sessionId already active; reapplying current parameters")
            applyCurrentParametersToEffects()
            return
        }

        releaseEffects()
        _currentSessionId.value = sessionId

        try {
            equalizer = Equalizer(0, sessionId).also { eq ->
                val numBands = eq.numberOfBands.toInt().coerceAtLeast(1)
                _bandCount.value = numBands

                val range = eq.bandLevelRange
                if (range.size >= 2) {
                    _bandLevelRangeMb.value = range[0].toInt() to range[1].toInt()
                }

                val freqs = mutableListOf<Int>()
                for (b in 0 until numBands) {
                    // Center freq is in milliHertz, convert to Hertz
                    val freqHz = (eq.getCenterFreq(b.toShort()) / 1000).coerceAtLeast(20)
                    freqs.add(freqHz)
                }
                _bandFrequenciesHz.value = freqs
            }
            Timber.tag(TAG).i("Equalizer attached to session $sessionId (${_bandCount.value} bands)")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize Equalizer on session $sessionId")
            equalizer = null
        }

        try {
            bassBoost = BassBoost(0, sessionId)
            Timber.tag(TAG).i("BassBoost attached to session $sessionId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize BassBoost on session $sessionId")
            bassBoost = null
        }

        try {
            virtualizer = Virtualizer(0, sessionId)
            Timber.tag(TAG).i("Virtualizer attached to session $sessionId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize Virtualizer on session $sessionId")
            virtualizer = null
        }

        _isSessionActive.value = (equalizer != null || bassBoost != null || virtualizer != null)
        applyCurrentParametersToEffects()
    }

    /**
     * Cleanly detaches effects from the session when playback pauses or stops.
     */
    @Synchronized
    fun detachSession() {
        if (!_isSessionActive.value && _currentSessionId.value == 0) return
        Timber.tag(TAG).d("Detaching audio effect session ${_currentSessionId.value}")
        releaseEffects()
        _isSessionActive.value = false
        _currentSessionId.value = 0
    }

    /**
     * Completely release all hardware effect resources.
     */
    fun release() {
        detachSession()
    }

    private fun releaseEffects() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error releasing Equalizer instance")
        } finally {
            equalizer = null
        }

        try {
            bassBoost?.release()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error releasing BassBoost instance")
        } finally {
            bassBoost = null
        }

        try {
            virtualizer?.release()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error releasing Virtualizer instance")
        } finally {
            virtualizer = null
        }
    }

    private fun applyCurrentParametersToEffects() {
        val enabled = repository.isEnabled.value

        equalizer?.let { eq ->
            try {
                eq.enabled = enabled
                val (minMb, maxMb) = _bandLevelRangeMb.value
                val count = _bandCount.value
                val levels = resampleLevels(_currentBandLevelsMb.value, count)
                for (b in 0 until count) {
                    val level = levels.getOrElse(b) { 0 }.coerceIn(minMb, maxMb)
                    eq.setBandLevel(b.toShort(), level.toShort())
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error applying band levels to hardware equalizer")
            }
        }

        bassBoost?.let { bb ->
            try {
                val strength = _bassBoostStrength.value
                bb.enabled = enabled && strength > 0
                if (bb.strengthSupported) {
                    bb.setStrength(strength.toShort())
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error applying bass boost")
            }
        }

        virtualizer?.let { virt ->
            try {
                val strength = _virtualizerStrength.value
                virt.enabled = enabled && strength > 0
                if (virt.strengthSupported) {
                    virt.setStrength(strength.toShort())
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error applying virtualizer")
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        repository.setEnabled(enabled)
        applyCurrentParametersToEffects()
    }

    fun applyPreset(preset: EqualizerPreset) {
        repository.setActivePresetId(preset.id)
        val count = _bandCount.value
        val resampled = resampleLevels(preset.bandLevelsMb, count)
        _currentBandLevelsMb.value = resampled
        _bassBoostStrength.value = preset.bassBoostStrength
        _virtualizerStrength.value = preset.virtualizerStrength

        repository.saveBandLevels(resampled)
        repository.saveBassBoost(preset.bassBoostStrength)
        repository.saveVirtualizer(preset.virtualizerStrength)

        applyCurrentParametersToEffects()
    }

    fun setBandLevel(bandIndex: Int, levelMb: Int) {
        val (minMb, maxMb) = _bandLevelRangeMb.value
        val clamped = levelMb.coerceIn(minMb, maxMb)
        val count = _bandCount.value
        val current = _currentBandLevelsMb.value.toMutableList()
        while (current.size < count) current.add(0)

        if (bandIndex in 0 until count) {
            current[bandIndex] = clamped
            _currentBandLevelsMb.value = current
            repository.saveBandLevels(current)

            // Direct hardware update for real-time responsiveness
            equalizer?.let { eq ->
                try {
                    if (repository.isEnabled.value) {
                        eq.setBandLevel(bandIndex.toShort(), clamped.toShort())
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed setting hardware band $bandIndex to $clamped mB")
                }
            }
        }
    }

    fun setBassBoostStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _bassBoostStrength.value = clamped
        repository.saveBassBoost(clamped)

        bassBoost?.let { bb ->
            try {
                val enabled = repository.isEnabled.value
                bb.enabled = enabled && clamped > 0
                if (bb.strengthSupported) {
                    bb.setStrength(clamped.toShort())
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed setting hardware bass boost")
            }
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _virtualizerStrength.value = clamped
        repository.saveVirtualizer(clamped)

        virtualizer?.let { virt ->
            try {
                val enabled = repository.isEnabled.value
                virt.enabled = enabled && clamped > 0
                if (virt.strengthSupported) {
                    virt.setStrength(clamped.toShort())
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed setting hardware virtualizer")
            }
        }
    }

    fun resetToFlat() {
        applyPreset(EqualizerDefaults.PRESET_FLAT)
    }

    fun deleteCustomPreset(presetId: String) {
        repository.deleteCustomPreset(presetId)
    }

    fun importPresets(rawJson: String): Result<Int> {
        val result = repository.importFromText(rawJson)
        if (result.isSuccess) {
            val active = repository.getActivePreset()
            applyPreset(active)
        }
        return result
    }

    companion object {
        fun resampleLevels(source: List<Int>, targetCount: Int): List<Int> {
            if (targetCount <= 0) return emptyList()
            if (source.isEmpty()) return List(targetCount) { 0 }
            if (source.size == targetCount) return source
            return List(targetCount) { index ->
                val ratio = index.toFloat() / (targetCount - 1).coerceAtLeast(1)
                val sourceIndex = (ratio * (source.size - 1)).roundToInt().coerceIn(0, source.size - 1)
                source[sourceIndex]
            }
        }
    }
}
