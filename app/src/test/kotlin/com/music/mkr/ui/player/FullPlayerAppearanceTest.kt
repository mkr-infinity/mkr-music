package com.music.mkr.ui.player

import androidx.compose.ui.unit.dp
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.extensions.toEnum
import com.music.mkr.ui.component.playerStylePickerColumns
import com.music.mkr.ui.player.styles.ImmersiveArtworkSource
import com.music.mkr.ui.player.styles.immersiveArtworkSources
import com.music.mkr.ui.player.styles.immersiveStackedArtworkSize
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
    fun stackedImmersiveCoverRetainsSpaceWhenControlsExceedTheViewport() {
        val size = immersiveStackedArtworkSize(360.dp, 280.dp, 680.dp)
        assertEquals(154.dp, size)
        assertEquals(110.dp, immersiveStackedArtworkSize(110.dp, 280.dp, 680.dp))
        assertEquals(380.dp, immersiveStackedArtworkSize(420.dp, 720.dp, 340.dp))
    }

    @Test
    fun largeImmersiveCoverRequestsTheLargerSourceAndKeepsTheOriginal() {
        val original = "https://lh3.googleusercontent.com/cover=w544-h544"
        val large = "https://lh3.googleusercontent.com/cover=w1200-h1200"
        assertEquals(
            listOf(ImmersiveArtworkSource(large), ImmersiveArtworkSource(original)),
            immersiveArtworkSources(original, original, displayPixels = 900, dataSaver = false),
        )
        assertEquals(
            listOf(ImmersiveArtworkSource(original)),
            immersiveArtworkSources(original, null, displayPixels = 400, dataSaver = false),
        )
    }

    @Test
    fun immersiveCoverHasALowerYouTubeTierWhenMaxResolutionIsMissing() {
        val hq = "https://i.ytimg.com/vi/track/hqdefault.jpg"
        val max = "https://i.ytimg.com/vi/track/maxresdefault.jpg"
        assertEquals(
            listOf(ImmersiveArtworkSource(max), ImmersiveArtworkSource(hq)),
            immersiveArtworkSources(max, null, displayPixels = 900, dataSaver = false),
        )
    }

    @Test
    fun dataSaverKeepsLocalAndCachedArtworkAvailableWithoutFetchingTheLargeOriginal() {
        val original = "https://lh3.googleusercontent.com/cover=w1200-h1200"
        val small = "https://lh3.googleusercontent.com/cover=w300-h300"
        val local = "content://media/albumart/42"
        assertEquals(
            listOf(
                ImmersiveArtworkSource(local),
                ImmersiveArtworkSource(small),
                ImmersiveArtworkSource(original, cacheOnly = true),
            ),
            immersiveArtworkSources(original, local, displayPixels = 900, dataSaver = true),
        )
        assertEquals(emptyList<ImmersiveArtworkSource>(), immersiveArtworkSources(" ", null, 900, true))
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
