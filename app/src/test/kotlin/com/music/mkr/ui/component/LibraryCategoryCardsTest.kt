/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryCategoryCardsTest {
    @Test
    fun completeRowsContainTwoCardsAndFinalCardKeepsItsOwnRow() {
        assertEquals(listOf(listOf(1, 2), listOf(3, 4), listOf(5)), libraryCategoryRows((1..5).toList()))
        assertEquals(listOf(listOf(1, 2)), libraryCategoryRows(listOf(1, 2)))
        assertEquals(emptyList<List<Int>>(), libraryCategoryRows(emptyList<Int>()))
    }
}
