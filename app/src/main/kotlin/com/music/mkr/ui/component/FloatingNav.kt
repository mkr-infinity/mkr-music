/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.utils.rememberPreference
import com.music.mkr.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun Modifier.navigationGlass(
    shape: Shape,
    pureBlack: Boolean,
    rim: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
): Modifier {
    val colors = MaterialTheme.colorScheme
    val bright = if (pureBlack) Color.Black else colors.surfaceContainerHighest.copy(alpha = 0.88f)
    val base = if (pureBlack) Color.Black else colors.surfaceContainer.copy(alpha = 0.96f)
    val sheen = remember(bright, base) { Brush.verticalGradient(listOf(bright, base)) }
    return background(sheen, shape).border(1.dp, rim, shape)
}

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
// MKR Signature Navigation
// A raised glass dock with a lifting selection and signature underline.
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
                .shadow(5.dp, CircleShape)
                .navigationGlass(CircleShape, pureBlack, rim = amberBorderColor)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
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
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onSearchLongClick = onSearchLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AmberDockItem(
    screen: Screens,
    isSelected: Boolean,
    onClick: () -> Unit,
    slimNav: Boolean,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val lift by animateDpAsState(
        targetValue = if (isSelected) (-2).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_selection_lift",
    )

    val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val targetSlotColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
    } else {
        Color.Transparent
    }
    val slotBgColor by animateColorAsState(
        targetValue = targetSlotColor,
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
            .semantics { selected = isSelected }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Tab,
                onClick = onClick,
                onLongClick = onSearchLongClick.takeIf { screen == Screens.Search },
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .mkrPressFeedback(0.96f, interactionSource, animate)
                .graphicsLayer { translationY = if (animate) lift.toPx() else 0f }
                .background(if (animate) slotBgColor else targetSlotColor, RoundedCornerShape(16.dp))
                .padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = if (slimNav) stringResource(screen.titleId) else null,
                tint = contentColor,
                modifier = Modifier.size(23.dp)
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

            // Signature underline.
            Box(
                modifier = Modifier
                    .width(if (animate) indicatorWidth else if (isSelected) 28.dp else 0.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

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
) = CapsuleNavigationBar(
    navigationItems, currentRoute, onItemClick, modifier, pureBlack, slimNav,
    onSearchLongClick, bottomInset, compact = false,
)

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
) = CapsuleNavigationBar(
    navigationItems, currentRoute, onItemClick, modifier, pureBlack, slimNav,
    onSearchLongClick, bottomInset, compact = true,
)

@Composable
private fun CapsuleNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier,
    pureBlack: Boolean,
    slimNav: Boolean,
    onSearchLongClick: (() -> Unit)?,
    bottomInset: Dp,
    compact: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val labelScale = LocalDensity.current.fontScale
    val bottomPadding = if (slimNav) 8.dp else if (compact) 14.dp else 10.dp
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = bottomInset + bottomPadding),
        contentAlignment = Alignment.Center,
    ) {
        // Icon descriptions carry the labels when the capsule is too small for visible text.
        val labelsFit = !slimNav && labelScale <= 1.4f && maxWidth >= 260.dp
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = if (compact) 320.dp else 500.dp)
                .fillMaxWidth()
                .height(if (slimNav || compact) 56.dp else 66.dp)
                .shadow(3.dp, CircleShape)
                .navigationGlass(CircleShape, pureBlack)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            val selectedIndex = navigationItems.indexOfFirst {
                isNavRouteSelected(currentRoute, it.route, navigationItems)
            }
            if (!compact && selectedIndex >= 0) {
                val cellWidth = (maxWidth - 4.dp * (navigationItems.size - 1)) / navigationItems.size
                val targetOffset = (cellWidth + 4.dp) * selectedIndex + 2.dp
                val indicatorOffset by animateDpAsState(
                    targetOffset,
                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    label = "glass_indicator_slide",
                )
                Box(
                    Modifier.offset { IntOffset((if (animate) indicatorOffset else targetOffset).roundToPx(), 0) }
                        .width((cellWidth - 4.dp).coerceAtLeast(0.dp))
                        .fillMaxHeight()
                        .background(colors.secondaryContainer, RoundedCornerShape(17.dp))
                        .border(1.dp, colors.onSecondaryContainer.copy(alpha = 0.13f), RoundedCornerShape(17.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxSize().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationItems.forEachIndexed { index, screen ->
                    val isSelected = index == selectedIndex
                    val targetWeight = if (compact && labelsFit && isSelected) 1.75f else 1f
                    val weight = if (animate && compact && labelsFit) {
                        animateFloatAsState(
                            targetWeight,
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                            label = "pill_label_expansion",
                        ).value
                    } else targetWeight
                    CapsuleNavigationItem(
                        screen = screen,
                        isSelected = isSelected,
                        showLabel = labelsFit && (!compact || isSelected),
                        inlineLabel = compact,
                        animate = animate,
                        onClick = { onItemClick(screen, isSelected) },
                        onLongClick = onSearchLongClick.takeIf { screen == Screens.Search },
                        modifier = Modifier.weight(weight),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CapsuleNavigationItem(
    screen: Screens,
    isSelected: Boolean,
    showLabel: Boolean,
    inlineLabel: Boolean,
    animate: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val targetColor = if (inlineLabel && isSelected) colors.primaryContainer else Color.Transparent
    val fill by animateColorAsState(targetColor, tween(150), label = "navigation_selection_color")
    val targetCorner = if (isSelected) 15.dp else 24.dp
    val corner by animateDpAsState(targetCorner, tween(150), label = "navigation_selection_shape")
    val shape = RoundedCornerShape(if (animate) corner else targetCorner)
    val label = stringResource(screen.titleId)
    val ink = when {
        !isSelected -> colors.onSurfaceVariant
        inlineLabel -> colors.onPrimaryContainer
        else -> colors.onSecondaryContainer
    }

    Box(
        modifier = modifier.fillMaxHeight()
            .semantics { selected = isSelected }
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(),
                role = Role.Tab,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val face = Modifier.fillMaxSize().padding(horizontal = 2.dp)
            .mkrPressFeedback(0.97f, interaction, animate)
            .background(if (animate) fill else targetColor, shape)
            .padding(horizontal = 5.dp)
        val icon: @Composable () -> Unit = {
            Icon(
                painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive),
                contentDescription = if (showLabel) null else label,
                tint = ink,
                modifier = Modifier.size(22.dp),
            )
        }
        val text: @Composable () -> Unit = {
            Text(
                text = label,
                color = ink,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (inlineLabel) {
            Row(
                face,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                if (animate) {
                    AnimatedVisibility(
                        visible = showLabel,
                        enter = fadeIn(tween(140)),
                        exit = fadeOut(tween(90)),
                    ) { text() }
                } else if (showLabel) text()
            }
        } else {
            Column(
                face,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                icon()
                if (showLabel) text()
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
    val colors = MaterialTheme.colorScheme
    val animate by rememberPreference(InterfaceAnimationsKey, true)
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
            modifier = Modifier.fillMaxWidth().navigationGlass(RectangleShape, pureBlack),
            containerColor = Color.Transparent,
            contentColor = colors.onSurfaceVariant,
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
                        indicatorColor = colors.primaryContainer,
                        selectedIconColor = colors.onPrimaryContainer,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant
                    ),
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = if (slimNav) stringResource(screen.titleId) else null,
                            modifier = Modifier.mkrPressFeedback(0.94f, interactionSource, animate),
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
