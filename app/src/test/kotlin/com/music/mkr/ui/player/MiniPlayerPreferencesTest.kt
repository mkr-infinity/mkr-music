package com.music.mkr.ui.player

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import com.music.mkr.constants.MiniPlayerDesignOption
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import org.junit.Assert.assertEquals
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
}
