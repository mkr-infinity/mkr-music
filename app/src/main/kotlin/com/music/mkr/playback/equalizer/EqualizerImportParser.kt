/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback.equalizer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.UUID

/**
 * Robust parser and validator for custom equalizer profile files.
 * Provides complete compatibility with ArchiveTune JSON export payloads,
 * profile arrays, and individual profile objects.
 */
object EqualizerImportParser {

    /**
     * Parses and validates raw profile text into a list of [EqualizerPreset].
     * Returns [Result.success] with the parsed presets or [Result.failure] with an
     * understandable error message suitable for user display.
     */
    fun parse(rawText: String): Result<List<EqualizerPreset>> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("The selected equalizer profile file is empty."))
        }

        return try {
            val tokener = JSONTokener(trimmed)
            val jsonRoot = tokener.nextValue()

            val profiles = when (jsonRoot) {
                is JSONObject -> parseJsonObject(jsonRoot)
                is JSONArray -> parseJsonArray(jsonRoot)
                else -> return Result.failure(
                    IllegalArgumentException("Unsupported file structure. Expected a JSON object or array.")
                )
            }

            if (profiles.isEmpty()) {
                Result.failure(IllegalArgumentException("No valid equalizer presets found in the imported file."))
            } else {
                Result.success(profiles)
            }
        } catch (e: JSONException) {
            // Check if user imported AutoEq / Parametric text file as fallback
            val autoEqResult = tryParseAutoEqText(trimmed)
            if (autoEqResult != null) {
                Result.success(listOf(autoEqResult))
            } else {
                Result.failure(IllegalArgumentException("Invalid JSON syntax: ${e.localizedMessage ?: "malformed file"}"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException(e.localizedMessage ?: "Unable to parse equalizer profile."))
        }
    }

    private fun parseJsonObject(obj: JSONObject): List<EqualizerPreset> {
        // ArchiveTune format: { "profiles": [ ... ] }
        if (obj.has("profiles")) {
            val profilesArray = obj.optJSONArray("profiles") ?: return emptyList()
            return parseJsonArray(profilesArray)
        }

        // Single profile format: { "name": "...", "bandLevelsMb": [ ... ] }
        val singlePreset = parseSingleProfileObject(obj)
        return if (singlePreset != null) listOf(singlePreset) else emptyList()
    }

    private fun parseJsonArray(array: JSONArray): List<EqualizerPreset> {
        val list = mutableListOf<EqualizerPreset>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val preset = parseSingleProfileObject(item)
            if (preset != null) {
                list.add(preset)
            }
        }
        return list
    }

    private fun parseSingleProfileObject(obj: JSONObject): EqualizerPreset? {
        val rawName = obj.optString("name", "").trim()
        val name = if (rawName.isNotEmpty()) rawName else "Imported Preset ${UUID.randomUUID().toString().take(4)}"

        // Extract band levels
        val bandLevels = mutableListOf<Int>()
        val bandsArray = obj.optJSONArray("bandLevelsMb")
            ?: obj.optJSONArray("bandLevels")
            ?: obj.optJSONArray("bands")

        if (bandsArray != null) {
            for (i in 0 until bandsArray.length()) {
                val value = bandsArray.optDouble(i, 0.0)
                // Normalize: If values are small (<= 30) they might be raw dB instead of millibels
                val levelMb = if (value in -30.0..30.0 && !obj.has("bandLevelsMb")) {
                    (value * 100).toInt()
                } else {
                    value.toInt()
                }
                bandLevels.add(levelMb.coerceIn(-2000, 2000))
            }
        }

        if (bandLevels.isEmpty()) {
            // If no bands specified, invalid profile
            return null
        }

        val bassStrength = obj.optInt("bassBoostStrength", 0).coerceIn(0, 1000)
        val virtStrength = obj.optInt("virtualizerStrength", 0).coerceIn(0, 1000)

        val id = obj.optString("id", "").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

        return EqualizerPreset(
            id = id,
            name = name,
            isBuiltIn = false,
            bandLevelsMb = bandLevels,
            bassBoostStrength = bassStrength,
            virtualizerStrength = virtStrength,
        )
    }

    /**
     * Fallback parser for standard AutoEQ / EqualizerAPO format if supplied.
     */
    private fun tryParseAutoEqText(text: String): EqualizerPreset? {
        if (!text.contains("GraphicEQ:") && !text.contains("Filter") && !text.contains("Preamp:")) {
            return null
        }
        val lines = text.lines()
        var graphicEqLine = lines.firstOrNull { it.trim().startsWith("GraphicEQ:") }
        if (graphicEqLine != null) {
            val points = graphicEqLine.substringAfter("GraphicEQ:").split(";").mapNotNull { token ->
                val parts = token.trim().split(" ")
                if (parts.size >= 2) {
                    val gainDb = parts[1].toDoubleOrNull() ?: 0.0
                    (gainDb * 100).toInt()
                } else null
            }
            if (points.isNotEmpty()) {
                return EqualizerPreset(
                    id = UUID.randomUUID().toString(),
                    name = "AutoEQ Imported",
                    isBuiltIn = false,
                    bandLevelsMb = points,
                    bassBoostStrength = 0,
                    virtualizerStrength = 0,
                )
            }
        }
        return null
    }
}
