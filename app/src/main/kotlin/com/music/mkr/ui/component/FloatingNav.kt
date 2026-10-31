/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.mkr.constants.NavigationStyle
import com.music.mkr.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Route selection helper verifying whether the given screen matches the active route.
 */
fun isNavRouteSelected(currentRoute: String?, screenRoute: String, navigationItems: List<Screens>): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == screenRoute) return true
    if (screenRoute == Screens.Search.route && (currentRoute == "search" || currentRoute.startsWith("search/"))) {
        return true
    }
    return navigationItems.any { it.route == screenRoute } &&
            currentRoute.startsWith("$screenRoute/")
}

// ---------------------------------------------------------------------------
// STYLE D: MKR Signature Navigation (SELECTED DEFAULT)
// Sculpted wide console dock with charcoal surface, warm amber rim perimeter,
// elevated charcoal slot, and signature glowing amber underline capsule.
// ---------------------------------------------------------------------------
@Composable
fun AmberDockNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    bottomInset: Dp = 0.dp,
) {
    val containerColor = if (pureBlack) Color(0xFF0C0D0F) else MaterialTheme.colorScheme.surfaceContainer
    val amberBorderColor = if (pureBlack) {
        Color(0x55F59E0B)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomInset + 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x30000000),
                    spotColor = Color(0x40000000)
                )
                .background(containerColor, shape = CircleShape)
                .border(width = 1.dp, color = amberBorderColor, shape = CircleShape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            navigationItems.forEach { screen ->
                val isSelected = remember(currentRoute, screen.route) {
                    isNavRouteSelected(currentRoute, screen.route, navigationItems)
                }

                AmberDockItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = { onItemClick(screen, isSelected) },
                    slimNav = slimNav,
                    modifier = Modifier.weight(1f),
                    onSearchLongClick = onSearchLongClick
                )
            }
        }
    }
}

@Composable
private fun AmberDockItem(
    screen: Screens,
    isSelected: Boolean,
    onClick: () -> Unit,
    slimNav: Boolean,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }

    val isSearchItem = screen == Screens.Search && onSearchLongClick != null
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
                        onSearchLongClick?.invoke()
                    }
                    is PressInteraction.Release -> {
                        if (!isLongClick) onClick()
                    }
                    is PressInteraction.Cancel -> {
                        isLongClick = false
                    }
                }
            }
        }
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "amberPressScale"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "amberIconScale"
    )

    val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    }

    val slotBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "amberSlotBg"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 28.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "amberIndicatorWidth"
    )

    Box(
        modifier = modifier
            .scale(pressScale)
            .clip(RoundedCornerShape(16.dp))
            .background(slotBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(screen.titleId),
                tint = contentColor,
                modifier = Modifier
                    .size(23.dp)
                    .scale(iconScale)
            )

            if (!slimNav) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(screen.titleId),
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Signature glowing amber underline capsule
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STYLE A: Floating Glass Navigation (Lightweight Liquid Glass Dock)
// Authentic liquid translucent glass dock with ambient depth, dual specular
// rim light reflection, subtle top refractive glint, and liquid illuminated active pill.
// ---------------------------------------------------------------------------
@Composable
fun FloatingGlassNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    bottomInset: Dp = 0.dp,
) {
    val isDark = pureBlack || MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val glassShape = CircleShape

    val glassBaseColor = when {
        pureBlack -> Color(0xB8090A0D)
        isDark -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    }

    val glassRimBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color(0x50FFFFFF),
                Color(0x20FFFFFF),
                Color(0x08FFFFFF),
                Color(0x18000000)
            )
        } else {
            listOf(
                Color(0xA0FFFFFF),
                Color(0x40FFFFFF),
                Color(0x14000000),
                Color(0x24000000)
            )
        }
    )

    val specularFlare = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            if (isDark) Color(0x20FFFFFF) else Color(0x45FFFFFF),
            if (isDark) Color(0x45FFFFFF) else Color(0x80FFFFFF),
            if (isDark) Color(0x20FFFFFF) else Color(0x45FFFFFF),
            Color.Transparent
        )
    )

    val dockHeight = if (slimNav) 56.dp else 66.dp
    val bottomPad = bottomInset + if (slimNav) 8.dp else 10.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomPad),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .height(dockHeight)
                .shadow(
                    elevation = 10.dp,
                    shape = glassShape,
                    ambientColor = if (isDark) Color(0x30000000) else Color(0x1A000000),
                    spotColor = if (isDark) Color(0x45000000) else Color(0x25000000)
                )
                .background(glassBaseColor, shape = glassShape)
                .border(width = 1.dp, brush = glassRimBrush, shape = glassShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = remember(currentRoute, screen.route) {
                        isNavRouteSelected(currentRoute, screen.route, navigationItems)
                    }

                    FloatingGlassItem(
                        screen = screen,
                        isSelected = isSelected,
                        onClick = { onItemClick(screen, isSelected) },
                        slimNav = slimNav,
                        modifier = Modifier.weight(1f),
                        onSearchLongClick = onSearchLongClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingGlassItem(
    screen: Screens,
    isSelected: Boolean,
    onClick: () -> Unit,
    slimNav: Boolean,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }

    val isSearchItem = screen == Screens.Search && onSearchLongClick != null
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
                        onSearchLongClick?.invoke()
                    }
                    is PressInteraction.Release -> {
                        if (!isLongClick) onClick()
                    }
                    is PressInteraction.Cancel -> {
                        isLongClick = false
                    }
                }
            }
        }
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassPressScale"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassIconScale"
    )

    val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "glassContentColor"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val activePillBrush = remember(primaryColor) {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.22f),
                primaryColor.copy(alpha = 0.10f)
            )
        )
    }
    val activePillBorder = remember(primaryColor) {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.48f),
                primaryColor.copy(alpha = 0.16f)
            )
        )
    }

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 22.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassIndicatorWidth"
    )

    val itemShape = CircleShape

    Box(
        modifier = modifier
            .scale(pressScale)
            .clip(itemShape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(brush = activePillBrush, shape = itemShape)
                        .border(width = 1.dp, brush = activePillBorder, shape = itemShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = if (slimNav) 8.dp else 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(screen.titleId),
                tint = contentColor,
                modifier = Modifier
                    .size(23.dp)
                    .scale(iconScale)
            )

            if (!slimNav) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(screen.titleId),
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STYLE B: Expressive Expanding Navigation
// Compact centered dock with asymmetric space allocation: active destination
// smoothly expands horizontally showing icon + label, while inactive items collapse.
// ---------------------------------------------------------------------------
@Composable
fun ExpressivePillNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    bottomInset: Dp = 0.dp,
) {
    val containerColor = if (pureBlack) Color(0xFF101012) else MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomInset + 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .height(58.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .background(containerColor, shape = CircleShape)
                .border(width = 1.dp, color = outlineColor, shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            navigationItems.forEach { screen ->
                val isSelected = remember(currentRoute, screen.route) {
                    isNavRouteSelected(currentRoute, screen.route, navigationItems)
                }

                ExpressivePillItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = { onItemClick(screen, isSelected) },
                    slimNav = slimNav,
                    onSearchLongClick = onSearchLongClick
                )
            }
        }
    }
}

@Composable
private fun ExpressivePillItem(
    screen: Screens,
    isSelected: Boolean,
    onClick: () -> Unit,
    slimNav: Boolean,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }

    val isSearchItem = screen == Screens.Search && onSearchLongClick != null
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
                        onSearchLongClick?.invoke()
                    }
                    is PressInteraction.Release -> {
                        if (!isLongClick) onClick()
                    }
                    is PressInteraction.Cancel -> {
                        isLongClick = false
                    }
                }
            }
        }
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressivePressScale"
    )

    val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "expressivePillBg"
    )

    Box(
        modifier = modifier
            .scale(pressScale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = if (isSelected && !slimNav) 14.dp else 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(screen.titleId),
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = !slimNav && isSelected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = stringResource(screen.titleId),
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STYLE C: Material 3 Navigation
// Refined edge-aligned Material 3 NavigationBar with authentic indicator capsule,
// clean labels, top separation divider, and anchored ergonomics.
// ---------------------------------------------------------------------------
@Composable
fun CleanMaterialNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    bottomInset: Dp = 0.dp,
) {
    val containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Subtle top separation border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        )

        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = containerColor,
            contentColor = contentColor,
            windowInsets = WindowInsets(bottom = bottomInset)
        ) {
            navigationItems.forEach { screen ->
                val isSelected = remember(currentRoute, screen.route) {
                    isNavRouteSelected(currentRoute, screen.route, navigationItems)
                }
                val iconRes = remember(isSelected, screen) {
                    if (isSelected) screen.iconIdActive else screen.iconIdInactive
                }

                val isSearchItem = screen == Screens.Search && onSearchLongClick != null
                val interactionSource = remember { MutableInteractionSource() }

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
                                    if (!isLongClick) onItemClick(screen, isSelected)
                                }
                                is PressInteraction.Cancel -> {
                                    isLongClick = false
                                }
                            }
                        }
                    }
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        onItemClick(screen, isSelected)
                    },
                    interactionSource = interactionSource,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(screen.titleId)
                        )
                    },
                    label = if (!slimNav) {
                        {
                            Text(
                                text = stringResource(screen.titleId),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else null
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Backward-compatibility delegator
// ---------------------------------------------------------------------------
@Composable
fun FloatingNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    bottomInset: Dp = 0.dp
) {
    FloatingGlassNavigationBar(
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
