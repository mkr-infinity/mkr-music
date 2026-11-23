/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import com.music.mkr.constants.DefaultListenTogetherHomeShortcut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HomePreferencesTest {
    @Test
    fun listenTogetherShortcutIsOptIn() {
        assertFalse(DefaultListenTogetherHomeShortcut)
    }

    @Test
    fun refreshKeepsRelativeShelfOrderAndFixedSlots() {
        val initial = listOf("speed_dial", "utility", "keep_listening", "daily_discover")
        val refreshed = listOf("daily_discover", "utility", "covers", "keep_listening", "speed_dial")
        val eligibility: (String) -> String? = { it.takeUnless { key -> key == "utility" } }

        val first = shuffleHomeShelves(initial, 42L, eligibility)
        val afterRefresh = shuffleHomeShelves(refreshed, 42L, eligibility)

        assertEquals("utility", first[1])
        assertEquals("utility", afterRefresh[1])
        assertEquals(first.filterNot { it == "utility" }, afterRefresh.filter { it in initial && it != "utility" })
    }
}
