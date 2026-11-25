/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import com.music.mkr.ui.screens.Screens
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapsuleNavigationTest {
    @Test
    fun searchResultsAndNestedRoutesKeepTheirParentSelection() {
        val tabs = Screens.MainScreens
        assertTrue(isNavRouteSelected("search/track", Screens.Search.route, tabs))
        assertTrue(isNavRouteSelected("library/favorites", Screens.Library.route, tabs))
        assertFalse(isNavRouteSelected("library/favorites", Screens.Search.route, tabs))
        assertFalse(isNavRouteSelected("library_other", Screens.Library.route, tabs))
        assertFalse(isNavRouteSelected(null, Screens.Home.route, tabs))
    }
}
