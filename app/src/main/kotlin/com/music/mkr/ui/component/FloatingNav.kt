/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.NavigationStyle
import com.music.mkr.utils.rememberPreference
import com.music.mkr.ui.screens.Screens

@Composable
internal fun Modifier.navigationGlass(
    shape: Shape,
    pureBlack: Boolean,
    rim: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
): Modifier {
    val colors = MaterialTheme.colorScheme
    val surface = if (pureBlack) Color.Black else colors.surfaceContainerHigh.copy(alpha = 0.96f)
    return background(surface, shape).border(1.dp, rim, shape)
}

@Composable
internal fun Modifier.frostedNavigationCapsule(pureBlack: Boolean): Modifier {
    val colors = MaterialTheme.colorScheme
    val top = if (pureBlack) colors.background else colors.surfaceContainerHighest.copy(alpha = 0.92f)
    val bottom = if (pureBlack) colors.background else colors.surfaceContainerHigh.copy(alpha = 0.82f)
    val highlight = colors.onSurface.copy(alpha = 0.22f)
    val edge = colors.outlineVariant.copy(alpha = 0.52f)
    val fillBrush = remember(top, bottom) { Brush.verticalGradient(listOf(top, bottom)) }
    val rimBrush = remember(highlight, edge) { Brush.verticalGradient(listOf(highlight, edge)) }
    return background(fillBrush, CircleShape).border(1.dp, rimBrush, CircleShape)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun navigationFace(progress: Float): Shape {
    val morph = remember { Morph(MaterialShapes.Cookie12Sided, MaterialShapes.Circle) }
    return remember(morph, progress) {
        object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                val scale = Matrix().apply { scale(size.width, size.height) }
                val path = morph.toPath(progress.coerceIn(0f, 1f))
                path.transform(scale)
                path.translate(size.center - path.getBounds().center)
                return Outline.Generic(path)
            }
        }
    }
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

private fun floatingGlassCapsuleHeight(slim: Boolean) = if (slim) 52.dp else 56.dp
private fun floatingGlassBottomPadding(slim: Boolean) = if (slim) 6.dp else 8.dp

private fun materialNavigationHeight(slim: Boolean) = if (slim) 56.dp else 72.dp

internal fun bottomNavigationHeight(style: NavigationStyle, slim: Boolean): Dp = when (style) {
    NavigationStyle.FLOATING_GLASS -> floatingGlassCapsuleHeight(slim) + floatingGlassBottomPadding(slim)
    NavigationStyle.EXPRESSIVE_PILL -> (if (slim) 56.dp else 64.dp) + if (slim) 8.dp else 12.dp
    NavigationStyle.CLEAN_MATERIAL -> materialNavigationHeight(slim)
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

@OptIn(ExperimentalFoundationApi::class)
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
    val containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val outlineColor = if (pureBlack) Color(0xFF222222) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val pillHeight = if (slimNav) 56.dp else 64.dp
    val bottomPadding = if (slimNav) 8.dp else 12.dp

    val leftItems = remember(navigationItems) {
        navigationItems.filter { it != Screens.Search }
    }
    val searchItem = remember(navigationItems) {
        navigationItems.firstOrNull { it == Screens.Search }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = bottomInset + bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        val pillSpacing = if (maxWidth < 340.dp) 10.dp else 16.dp
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Home, Library, (and any other non-Search tabs)
            if (leftItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(pillHeight)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(containerColor, shape = CircleShape)
                        .border(width = 1.dp, color = outlineColor, shape = CircleShape)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    leftItems.forEach { screen ->
                        val isSelected = remember(currentRoute, screen.route, navigationItems) {
                            isNavRouteSelected(currentRoute, screen.route, navigationItems)
                        }
                        ExpressiveSplitNavItem(
                            screen = screen,
                            isSelected = isSelected,
                            slimNav = slimNav,
                            animate = animate,
                            onClick = { onItemClick(screen, isSelected) }
                        )
                    }
                }
            }

            if (searchItem != null) {
                if (leftItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(pillSpacing))
                }
                val isSearchSelected = remember(currentRoute, searchItem.route, navigationItems) {
                    isNavRouteSelected(currentRoute, searchItem.route, navigationItems)
                }
                Box(
                    modifier = Modifier
                        .size(pillHeight)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(containerColor, shape = CircleShape)
                        .border(width = 1.dp, color = outlineColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveSplitNavItem(
                        screen = searchItem,
                        isSelected = isSearchSelected,
                        slimNav = slimNav,
                        animate = animate,
                        onClick = { onItemClick(searchItem, isSearchSelected) },
                        onLongClick = onSearchLongClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpressiveSplitNavItem(
    screen: Screens,
    isSelected: Boolean,
    slimNav: Boolean,
    animate: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconId = when {
        screen == Screens.Search -> R.drawable.nav_search
        screen == Screens.Library -> R.drawable.nav_library
        screen == Screens.Home -> R.drawable.nav_home
        isSelected -> screen.iconIdActive
        else -> screen.iconIdInactive
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val targetBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
    val backgroundColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = if (animate) tween(durationMillis = 200) else snap(),
        label = "ExpressiveSplitNavBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = if (animate) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            snap()
        },
        label = "ExpressiveSplitNavScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Tab,
                onClick = onClick,
                onLongClick = if (screen == Screens.Search) onLongClick else null
            )
            .let {
                if (screen == Screens.Search) it else it.padding(horizontal = 14.dp, vertical = 10.dp)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.animateContentSize(
                animationSpec = if (animate) {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                } else {
                    snap()
                }
            )
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = stringResource(screen.titleId),
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(
                visible = !slimNav && isSelected && screen != Screens.Search,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = stringResource(screen.titleId),
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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
    anchored: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val animate by rememberPreference(InterfaceAnimationsKey, true)
    val labelScale = LocalDensity.current.fontScale
    val floatingGlass = !compact && !anchored
    val bottomPadding = when {
        floatingGlass -> floatingGlassBottomPadding(slimNav)
        anchored -> 0.dp
        slimNav -> 8.dp
        else -> 14.dp
    }
    val capsuleHeight = when {
        floatingGlass -> floatingGlassCapsuleHeight(slimNav)
        anchored -> materialNavigationHeight(slimNav)
        else -> 56.dp
    }
    val shell = if (anchored) RectangleShape else CircleShape
    val capsuleSurface = when {
        floatingGlass -> Modifier.frostedNavigationCapsule(pureBlack)
        anchored -> Modifier
        else -> Modifier.navigationGlass(shell, pureBlack)
    }
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
            .then(if (anchored) Modifier.background(if (pureBlack) Color.Black else colors.surfaceContainer) else Modifier)
            .padding(horizontal = if (anchored) 0.dp else 12.dp)
            .padding(bottom = bottomInset + bottomPadding),
        contentAlignment = Alignment.Center,
    ) {
        // Icon descriptions carry the labels when the capsule is too small for visible text.
        val labelsFit = !floatingGlass && !slimNav && labelScale <= 1.4f && maxWidth >= 260.dp
        val capsuleWidth = if (floatingGlass) {
            val minimumWidth = (56.dp * navigationItems.size + 20.dp).coerceAtMost(360.dp)
            (maxWidth * 0.88f).coerceIn(minimumWidth, 360.dp).coerceAtMost(maxWidth)
        } else if (anchored) {
            maxWidth.coerceAtMost(640.dp)
        } else {
            maxWidth.coerceAtMost(if (compact) 320.dp else 500.dp)
        }
        BoxWithConstraints(
            modifier = Modifier
                .width(capsuleWidth)
                .height(capsuleHeight)
                .shadow(if (anchored) 0.dp else if (floatingGlass) 2.dp else 3.dp, shell)
                .then(capsuleSurface)
                .padding(horizontal = if (anchored) 0.dp else 6.dp, vertical = if (anchored) 0.dp else if (floatingGlass) 2.dp else 4.dp),
        ) {
            val selectedIndex = navigationItems.indexOfFirst {
                isNavRouteSelected(currentRoute, it.route, navigationItems)
            }
            val itemGap = if (floatingGlass) (maxWidth * 0.018f).coerceIn(2.dp, 6.dp) else 4.dp
            if (floatingGlass && selectedIndex >= 0) {
                val cellWidth = (maxWidth - itemGap * (navigationItems.size - 1)) / navigationItems.size
                val indicatorWidth = (cellWidth * 0.68f).coerceIn(42.dp, 62.dp).coerceAtMost(cellWidth)
                val indicatorHeight = (maxHeight * 0.78f).coerceIn(32.dp, 40.dp).coerceAtMost(maxHeight)
                val targetOffset = (cellWidth + itemGap) * selectedIndex + (cellWidth - indicatorWidth) / 2
                val maxOffset = (maxWidth - indicatorWidth).coerceAtLeast(0.dp)
                val indicatorOffset by animateDpAsState(
                    targetOffset,
                    if (animate) spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium) else snap(),
                    label = "glass_indicator_slide",
                )
                Box(
                    Modifier.offset { IntOffset(indicatorOffset.coerceIn(0.dp, maxOffset).roundToPx(), 0) }
                        .width(indicatorWidth)
                        .height(indicatorHeight)
                        .align(Alignment.CenterStart)
                        .background(colors.secondaryContainer, CircleShape),
                )
            }
            Row(
                modifier = Modifier.fillMaxSize().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(itemGap),
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
                        ownIndicator = compact,
                        anchored = anchored,
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
    ownIndicator: Boolean,
    anchored: Boolean,
    animate: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val targetColor = if (ownIndicator && !anchored && isSelected) colors.primaryContainer else Color.Transparent
    val fill = if (ownIndicator && !anchored) {
        animateColorAsState(targetColor, tween(150), label = "navigation_selection_color").value
    } else targetColor
    val targetMorph = if (isSelected) 1f else 0f
    val morph = if (ownIndicator) {
        animateFloatAsState(targetMorph, tween(220), label = "navigation_selection_shape").value
    } else targetMorph
    val shape = if (ownIndicator) navigationFace(if (animate) morph else targetMorph) else CircleShape
    val label = stringResource(screen.titleId)
    val ink = when {
        !isSelected -> colors.onSurfaceVariant
        ownIndicator -> colors.onPrimaryContainer
        else -> colors.onSecondaryContainer
    }

    Box(
        modifier = modifier.fillMaxHeight()
            .semantics { selected = isSelected }
            .clip(if (anchored) RoundedCornerShape(16.dp) else CircleShape)
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
            .then(if (ownIndicator) Modifier.background(if (animate) fill else targetColor, shape) else Modifier)
            .padding(horizontal = 5.dp)
        val icon: @Composable () -> Unit = {
            val iconId = when {
                screen == Screens.Search -> R.drawable.nav_search
                screen == Screens.Library -> R.drawable.nav_library
                screen == Screens.Home -> R.drawable.nav_home
                isSelected -> screen.iconIdActive
                else -> screen.iconIdInactive
            }
            Icon(
                painterResource(iconId),
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

        if (anchored) {
            val selection = if (animate) morph else targetMorph
            val cookie = navigationFace(0f)
            Column(
                face,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                Box(Modifier.size(if (showLabel) 40.dp else 42.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.fillMaxSize()
                            .graphicsLayer {
                                alpha = selection
                                scaleX = 0.84f + selection * 0.16f
                                scaleY = scaleX
                            }
                            .background(colors.primaryContainer, cookie)
                    )
                    icon()
                }
                if (showLabel) text()
            }
        } else if (inlineLabel) {
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
// Material navigation keeps its edge-aligned placement.
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
) = CapsuleNavigationBar(
    navigationItems, currentRoute, onItemClick, modifier, pureBlack, slimNav,
    onSearchLongClick, bottomInset, compact = false, anchored = true,
)

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
