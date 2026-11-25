/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        derivedStateOf {
            val scrollingUp = if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
            scrollingUp
        }
    }.value
}

@Composable
fun LazyGridState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        derivedStateOf {
            val scrollingUp = if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
            scrollingUp
        }
    }.value
}

@Composable
fun ScrollState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousScrollOffset = value
        derivedStateOf {
            val scrollingUp = previousScrollOffset >= value
            previousScrollOffset = value
            scrollingUp
        }
    }.value
}
