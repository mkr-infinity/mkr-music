/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.equalizer

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerPresetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        private const val TAG = "EqualizerPresetRepository"
        private const val PREFS_NAME = "mkr_equalizer_presets"
        private const val KEY_CUSTOM_PRESETS = "custom_presets_json"
        private const val KEY_ACTIVE_PRESET_ID = "active_preset_id"
        private const val KEY_ENABLED = "equalizer_enabled"
        private const val KEY_BASS_BOOST = "bass_boost_strength"
        private const val KEY_VIRTUALIZER = "virtualizer_strength"
        private const val KEY_MANUAL_LEVELS = "manual_band_levels"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _customPresets = MutableStateFlow<List<EqualizerPreset>>(emptyList())
    val customPresets: StateFlow<List<EqualizerPreset>> = _customPresets.asStateFlow()

    private val _allPresets = MutableStateFlow<List<EqualizerPreset>>(EqualizerDefaults.BUILT_IN_PRESETS)
    val allPresets: StateFlow<List<EqualizerPreset>> = _allPresets.asStateFlow()

    private val _activePresetId = MutableStateFlow(EqualizerDefaults.PRESET_FLAT_ID)
    val activePresetId: StateFlow<String> = _activePresetId.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    init {
        loadFromStorage()
    }

    private fun loadFromStorage() {
        try {
            _isEnabled.value = prefs.getBoolean(KEY_ENABLED, true)
            val savedPresetId = prefs.getString(KEY_ACTIVE_PRESET_ID, EqualizerDefaults.PRESET_FLAT_ID)
                ?: EqualizerDefaults.PRESET_FLAT_ID
            _activePresetId.value = savedPresetId

            val customJson = prefs.getString(KEY_CUSTOM_PRESETS, null)
            val customList = if (!customJson.isNullOrBlank()) {
                decodePresetsJson(customJson)
            } else {
                emptyList()
            }
            _customPresets.value = customList
            _allPresets.value = EqualizerDefaults.BUILT_IN_PRESETS + customList
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading equalizer presets from storage")
            _customPresets.value = emptyList()
            _allPresets.value = EqualizerDefaults.BUILT_IN_PRESETS
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
    }

    fun setActivePresetId(presetId: String) {
        _activePresetId.value = presetId
        prefs.edit { putString(KEY_ACTIVE_PRESET_ID, presetId) }
    }

    fun getActivePreset(): EqualizerPreset {
        val id = _activePresetId.value
        return _allPresets.value.firstOrNull { it.id == id } ?: EqualizerDefaults.PRESET_FLAT
    }

    fun saveCustomPreset(preset: EqualizerPreset) {
        val current = _customPresets.value.toMutableList()
        val index = current.indexOfFirst { it.id == preset.id }
        if (index >= 0) {
            current[index] = preset
        } else {
            current.add(preset)
        }
        persistCustomPresets(current)
    }

    fun deleteCustomPreset(presetId: String) {
        val current = _customPresets.value.filterNot { it.id == presetId }
        persistCustomPresets(current)
        if (_activePresetId.value == presetId) {
            setActivePresetId(EqualizerDefaults.PRESET_FLAT_ID)
        }
    }

    fun importFromText(rawText: String): Result<Int> {
        val parseResult = EqualizerImportParser.parse(rawText)
        if (parseResult.isFailure) {
            return Result.failure(parseResult.exceptionOrNull() ?: IllegalArgumentException("Failed to parse equalizer presets."))
        }

        val importedList = parseResult.getOrNull() ?: emptyList()
        if (importedList.isEmpty()) {
            return Result.failure(IllegalArgumentException("No valid equalizer presets found in file."))
        }

        val current = _customPresets.value.toMutableList()
        var addedCount = 0

        for (imported in importedList) {
            val uniqueId = if (current.any { it.id == imported.id }) {
                UUID.randomUUID().toString()
            } else {
                imported.id
            }
            val sanitized = imported.copy(id = uniqueId, isBuiltIn = false)
            current.add(sanitized)
            addedCount++
        }

        persistCustomPresets(current)

        // Automatically activate first imported preset
        importedList.firstOrNull()?.let { firstImported ->
            val matching = current.lastOrNull { it.name == firstImported.name }
            if (matching != null) {
                setActivePresetId(matching.id)
            }
        }

        return Result.success(addedCount)
    }

    private fun persistCustomPresets(list: List<EqualizerPreset>) {
        _customPresets.value = list
        _allPresets.value = EqualizerDefaults.BUILT_IN_PRESETS + list
        val json = encodePresetsJson(list)
        prefs.edit { putString(KEY_CUSTOM_PRESETS, json) }
    }

    private fun encodePresetsJson(presets: List<EqualizerPreset>): String {
        val array = JSONArray()
        for (preset in presets) {
            val obj = JSONObject()
            obj.put("id", preset.id)
            obj.put("name", preset.name)
            obj.put("bassBoostStrength", preset.bassBoostStrength)
            obj.put("virtualizerStrength", preset.virtualizerStrength)
            val bands = JSONArray()
            preset.bandLevelsMb.forEach { bands.put(it) }
            obj.put("bandLevelsMb", bands)
            array.put(obj)
        }
        return array.toString()
    }

    private fun decodePresetsJson(jsonStr: String): List<EqualizerPreset> {
        val result = mutableListOf<EqualizerPreset>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val id = obj.optString("id", UUID.randomUUID().toString())
            val name = obj.optString("name", "Custom Preset")
            val bassStrength = obj.optInt("bassBoostStrength", 0)
            val virtStrength = obj.optInt("virtualizerStrength", 0)
            val bandsArray = obj.optJSONArray("bandLevelsMb")
            val levels = mutableListOf<Int>()
            if (bandsArray != null) {
                for (b in 0 until bandsArray.length()) {
                    levels.add(bandsArray.optInt(b, 0))
                }
            }
            result.add(
                EqualizerPreset(
                    id = id,
                    name = name,
                    isBuiltIn = false,
                    bandLevelsMb = levels,
                    bassBoostStrength = bassStrength,
                    virtualizerStrength = virtStrength,
                )
            )
        }
        return result
    }

    fun saveBandLevels(levels: List<Int>) {
        val array = JSONArray()
        levels.forEach { array.put(it) }
        prefs.edit { putString(KEY_MANUAL_LEVELS, array.toString()) }
    }

    fun getSavedBandLevels(): List<Int>? {
        val raw = prefs.getString(KEY_MANUAL_LEVELS, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Int>()
            for (i in 0 until array.length()) {
                list.add(array.getInt(i))
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun saveBassBoost(strength: Int) {
        prefs.edit { putInt(KEY_BASS_BOOST, strength.coerceIn(0, 1000)) }
    }

    fun getSavedBassBoost(): Int = prefs.getInt(KEY_BASS_BOOST, 0)

    fun saveVirtualizer(strength: Int) {
        prefs.edit { putInt(KEY_VIRTUALIZER, strength.coerceIn(0, 1000)) }
    }

    fun getSavedVirtualizer(): Int = prefs.getInt(KEY_VIRTUALIZER, 0)
}
