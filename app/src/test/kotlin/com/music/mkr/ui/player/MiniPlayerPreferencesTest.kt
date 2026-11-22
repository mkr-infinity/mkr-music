package com.music.mkr.ui.player

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import com.music.mkr.constants.MiniPlayerArtworkTransitionKey
import com.music.mkr.constants.MiniPlayerCapsuleMorphKey
import com.music.mkr.constants.MiniPlayerDesignOption
import com.music.mkr.constants.MiniPlayerMicroEqualizerKey
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniPlayerPreferencesTest {
    @Test
    fun restoredLiquidPreferenceLeavesTheExpressiveDefault() {
        val saved = preferencesOf(booleanPreferencesKey("liquidMiniPlayer") to true)

        assertEquals(
            MiniPlayerDesignOption.NEW,
            MiniPlayerDesignOption.fromPreferences(
                useAppleMiniPlayer = saved[UseAppleMiniPlayerKey]
                    ?: MiniPlayerDesignOption.DefaultUseAppleMiniPlayer,
                useNewMiniPlayerDesign = saved[UseNewMiniPlayerDesignKey]
                    ?: MiniPlayerDesignOption.DefaultUseNewMiniPlayerDesign,
            ),
        )
    }

    @Test
    fun savedStyleFlagsKeepTheirExistingMeaning() {
        assertEquals(MiniPlayerDesignOption.CLASSIC, MiniPlayerDesignOption.fromPreferences(false, false))
        assertEquals(MiniPlayerDesignOption.NEW, MiniPlayerDesignOption.fromPreferences(false, true))
        assertEquals(MiniPlayerDesignOption.APPLE, MiniPlayerDesignOption.fromPreferences(true, false))
        assertEquals(MiniPlayerDesignOption.APPLE, MiniPlayerDesignOption.fromPreferences(true, true))
    }

    @Test
    fun motionOptionsDefaultOnAndStoreExplicitOffIndependently() {
        val keys = listOf(MiniPlayerMicroEqualizerKey, MiniPlayerArtworkTransitionKey, MiniPlayerCapsuleMorphKey)
        val unset = preferencesOf()
        for (key in keys) {
            assertTrue(unset[key] ?: DefaultMiniPlayerMotionEnabled)
            val saved = preferencesOf(key to false)
            for (animation in keys) {
                assertEquals(animation != key, saved[animation] ?: DefaultMiniPlayerMotionEnabled)
            }
        }
    }

    @Test
    fun equalizerAnimatesOnlyWhenPlaybackAndMotionAreActive() {
        val motion = MiniPlayerMotion(
            playing = true,
            allowed = true,
            equalizer = true,
            artworkTransition = true,
            capsuleMorph = true,
        )

        assertTrue(motion.animateEqualizer)
        assertFalse(motion.copy(playing = false).animateEqualizer)
        assertFalse(motion.copy(equalizer = false).animateEqualizer)
        val hiddenOrReducedMotion = motion.copy(allowed = false)
        assertFalse(hiddenOrReducedMotion.animateEqualizer)
        assertFalse(hiddenOrReducedMotion.animateArtwork)
        assertFalse(hiddenOrReducedMotion.animateCapsule)
    }

    @Test
    fun artworkAndMorphCanBeDisabledSeparately() {
        val motion = MiniPlayerMotion(
            playing = true,
            allowed = true,
            equalizer = true,
            artworkTransition = true,
            capsuleMorph = true,
        )

        assertFalse(motion.copy(artworkTransition = false).animateArtwork)
        assertTrue(motion.copy(artworkTransition = false).animateCapsule)
        assertTrue(motion.copy(capsuleMorph = false).animateArtwork)
        assertFalse(motion.copy(capsuleMorph = false).animateCapsule)
    }
}
