package com.music.mkr.ui.player

import androidx.compose.ui.unit.dp
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.extensions.toEnum
import com.music.mkr.ui.component.playerStylePickerColumns
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FullPlayerAppearanceTest {
    @Test
    fun retiredStylesAndPreviouslyHiddenAppleSelectionResolveToCinematic() {
        for (saved in listOf("MATERIAL_EXTENDED", "MODERN", "CLASSIC", "APPLE", null)) {
            assertEquals(PlayerStyle.CINEMATIC, PlayerStyle.fromString(saved))
        }
    }

    @Test
    fun theFourSelectableStyleIdsStillRoundTrip() {
        val ids = listOf("MINIMAL", "CINEMATIC", "IMMERSIVE", "IMMERSIVE_EXTENDED")
        assertEquals(ids, PlayerStyle.selectableStyles.map { it.name })
        for (id in ids) {
            assertEquals(id, PlayerStyle.fromString(id).name)
        }
    }

    @Test
    fun retiredAppleBackdropFallsBackAndOtherBackgroundIdsRoundTrip() {
        val default = PlayerBackgroundStyle.DEFAULT
        assertEquals(default, "APPLE_MUSIC".toEnum(default))
        val ids = listOf("DEFAULT", "GRADIENT", "BLUR", "GLOW_ANIMATED", "LIVE_MESH")
        assertEquals(ids, PlayerBackgroundStyle.entries.map { it.name })
        for (id in ids) {
            assertEquals(id, id.toEnum(default).name)
        }
    }

    @Test
    fun pickerUsesOneColumnInNarrowWindowsAndAtLargerFontScales() {
        assertEquals(1, playerStylePickerColumns(279.dp, 1f))
        assertEquals(2, playerStylePickerColumns(280.dp, 1f))
        assertEquals(1, playerStylePickerColumns(500.dp, 1.3f))
    }

    @Test
    fun artworkResolutionUsesTheCurrentItemWhenTheSongThumbnailIsMissing() {
        assertEquals("song", playerBackgroundArtworkUrl("song", "item"))
        assertEquals("item", playerBackgroundArtworkUrl(" ", "item"))
        assertNull(playerBackgroundArtworkUrl(null, ""))
    }

    @Test
    fun softwareBlurMixesAdjacentPixelsAndPreservesTheirOpacity() {
        val pixels = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        blurPlayerBackgroundPixels(pixels, width = 2, height = 1, radius = 1)
        assertArrayEquals(intArrayOf(0xFF717171.toInt(), 0xFF8D8D8D.toInt()), pixels)
    }

    @Test
    fun softwareBlurPreservesFlatArtworkEvenWhenTheRadiusExceedsItsDimensions() {
        val color = 0xFF336699.toInt()
        val pixels = IntArray(6) { color }
        blurPlayerBackgroundPixels(pixels, width = 2, height = 3, radius = 14)
        assertArrayEquals(IntArray(6) { color }, pixels)
    }
}
