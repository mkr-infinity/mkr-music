/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.NavigationStyle
import com.music.mkr.utils.rememberPreference
import com.music.mkr.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Immutable
private data class NavItemState(
    val isSelected: Boolean,
    val iconRes: Int
)

@Stable
private fun isRouteSelected(currentRoute: String?, screenRoute: String, navigationItems: List<Screens>): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == screenRoute) return true
    if (screenRoute == Screens.Search.route && (currentRoute == "search" || currentRoute.startsWith("search/"))) {
        return true
    }
    return navigationItems.any { it.route == screenRoute } && 
           currentRoute.startsWith("$screenRoute/")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigationRail(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current
    
    WideNavigationRail(
        modifier = modifier.navigationGlass(RectangleShape, pureBlack),
        colors = WideNavigationRailDefaults.colors(containerColor = Color.Transparent),
    ) {
        
        navigationItems.forEach { screen ->
            val isSelected = remember(currentRoute, screen.route) {
                isRouteSelected(currentRoute, screen.route, navigationItems)
            }
            val iconRes = remember(isSelected, screen) {
                if (isSelected) screen.iconIdActive else screen.iconIdInactive
            }
            
            val isSearchItem = screen == Screens.Search && onSearchLongClick != null
            val interactionSource = remember { MutableInteractionSource() }
            
            // Long press detection using InteractionSource
            if (isSearchItem) {
                LaunchedEffect(interactionSource) {
                    var isLongClick = false
                    interactionSource.interactions.collectLatest { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> {
                                isLongClick = false
                                delay(viewConfiguration.longPressTimeoutMillis)
                                isLongClick = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSearchLongClick.invoke()
                            }
                            is PressInteraction.Release -> {
                                if (!isLongClick) {
                                    onItemClick(screen, isSelected)
                                }
                            }
                            is PressInteraction.Cancel -> {
                                isLongClick = false
                            }
                        }
                    }
                }
            }
            
            WideNavigationRailItem(
                railExpanded = false,
                selected = isSelected,
                onClick = { 
                    onItemClick(screen, isSelected)
                },
                interactionSource = interactionSource,
                colors = WideNavigationRailItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedIndicatorColor = colors.primaryContainer,
                ),
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.mkrPressFeedback(0.94f, interactionSource, animate),
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.titleId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
fun AppNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    navigationStyle: NavigationStyle = NavigationStyle.DEFAULT,
    floatingNav: Boolean = true,
    bottomInset: Dp = 0.dp
) {
    val effectiveStyle = if (!floatingNav && navigationStyle == NavigationStyle.DEFAULT) {
        NavigationStyle.CLEAN_MATERIAL
    } else {
        navigationStyle
    }

    when (effectiveStyle) {
        NavigationStyle.AMBER_DOCK -> AmberDockNavigationBar(
            navigationItems = navigationItems,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
            modifier = modifier,
            pureBlack = pureBlack,
            slimNav = slimNav,
            onSearchLongClick = onSearchLongClick,
            bottomInset = bottomInset
        )
        NavigationStyle.FLOATING_GLASS -> FloatingGlassNavigationBar(
            navigationItems = navigationItems,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
            modifier = modifier,
            pureBlack = pureBlack,
            slimNav = slimNav,
            onSearchLongClick = onSearchLongClick,
            bottomInset = bottomInset
        )
        NavigationStyle.EXPRESSIVE_PILL -> ExpressivePillNavigationBar(
            navigationItems = navigationItems,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
            modifier = modifier,
            pureBlack = pureBlack,
            slimNav = slimNav,
            onSearchLongClick = onSearchLongClick,
            bottomInset = bottomInset
        )
        NavigationStyle.CLEAN_MATERIAL -> CleanMaterialNavigationBar(
            navigationItems = navigationItems,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
            modifier = modifier,
            pureBlack = pureBlack,
            slimNav = slimNav,
            onSearchLongClick = onSearchLongClick,
            bottomInset = bottomInset
        )
    }
}
