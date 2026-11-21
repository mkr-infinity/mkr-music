/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import com.music.mkr.constants.DefaultListenTogetherHomeShortcut
import org.junit.Assert.assertFalse
import org.junit.Test

class HomePreferencesTest {
    @Test
    fun listenTogetherShortcutIsOptIn() {
        assertFalse(DefaultListenTogetherHomeShortcut)
    }
}
