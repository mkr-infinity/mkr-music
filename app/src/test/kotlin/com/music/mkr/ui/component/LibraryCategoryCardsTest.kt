/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryCategoryCardsTest {
    @Test
    fun columnsFollowAvailableWidthAndFontScale() {
        assertEquals(1, libraryCategoryColumns(280f, 1f, 5))
        assertEquals(2, libraryCategoryColumns(340f, 1f, 5))
        assertEquals(3, libraryCategoryColumns(540f, 1f, 5))
        assertEquals(1, libraryCategoryColumns(340f, 1.5f, 5))
        assertEquals(2, libraryCategoryColumns(540f, 1f, 2))
    }
}
