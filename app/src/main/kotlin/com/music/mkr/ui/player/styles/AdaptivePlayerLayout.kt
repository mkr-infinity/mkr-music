package com.music.mkr.ui.player.styles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun AdaptivePlayerLayout(
    modifier: Modifier,
    artworkFraction: Float,
    artwork: @Composable (Dp) -> Unit,
    header: @Composable () -> Unit = {},
    immersive: Boolean = false,
    controls: @Composable () -> Unit,
) {
    val posture = currentWindowAdaptiveInfo().windowPosture
    val verticalHinge = posture.separatingVerticalHingeBounds.firstOrNull()
    val horizontalHinge = posture.separatingHorizontalHingeBounds.firstOrNull()
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var windowOrigin by remember { mutableStateOf(Offset.Zero) }
    val locate = verticalHinge != null || horizontalHinge != null
    val positionModifier = if (locate) Modifier.onGloballyPositioned { windowOrigin = it.positionInWindow() } else Modifier

    BoxWithConstraints(modifier.then(positionModifier)) {
        val areaWidth = maxWidth
        val areaHeight = maxHeight
        val left = verticalHinge?.let { with(density) { (it.left - windowOrigin.x).toDp() } }
            ?.takeIf { it in 1.dp..areaWidth }
        val right = verticalHinge?.let { with(density) { (it.right - windowOrigin.x).toDp() } }
            ?.takeIf { it in 1.dp..areaWidth }
        val top = horizontalHinge?.let { with(density) { (it.top - windowOrigin.y).toDp() } }
            ?.takeIf { it in 1.dp..areaHeight }
        val bottom = horizontalHinge?.let { with(density) { (it.bottom - windowOrigin.y).toDp() } }
            ?.takeIf { it in 1.dp..areaHeight }

        val cover: @Composable (Modifier, Dp) -> Unit = { pane, size ->
            Box(pane, contentAlignment = Alignment.Center) { artwork(size.coerceAtLeast(0.dp)) }
        }
        val controlPane: @Composable (Modifier, Boolean) -> Unit = { pane, includeHeader ->
            Box(pane, contentAlignment = Alignment.Center) {
                Column(
                    Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
                ) {
                    if (includeHeader) header()
                    controls()
                }
            }
        }

        when {
            left != null && right != null -> {
                val first = if (rtl) areaWidth - right else left
                val second = if (rtl) left else areaWidth - right
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    if (second >= 260.dp) {
                        cover(Modifier.width(first).fillMaxHeight(), minOf(first, areaHeight) * artworkFraction)
                        Spacer(Modifier.width(right - left))
                        controlPane(Modifier.width(second).fillMaxHeight(), true)
                    } else {
                        controlPane(Modifier.width(first).fillMaxHeight(), true)
                        Spacer(Modifier.width(right - left))
                        cover(Modifier.width(second).fillMaxHeight(), minOf(second, areaHeight) * artworkFraction)
                    }
                }
            }
            top != null && bottom != null -> Column(Modifier.fillMaxSize()) {
                cover(Modifier.fillMaxWidth().height(top), minOf(areaWidth, top) * artworkFraction)
                Spacer(Modifier.height(bottom - top))
                controlPane(Modifier.fillMaxWidth().height(areaHeight - bottom), true)
            }
            areaWidth >= 540.dp && (areaWidth > areaHeight || areaWidth >= 800.dp) -> {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    val artworkWidth = if (immersive) areaWidth / 2 else areaWidth * 0.46f
                    cover(Modifier.weight(1f).fillMaxHeight(), minOf(artworkWidth, areaHeight) * artworkFraction)
                    controlPane(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp), true)
                }
            }
            else -> SubcomposeLayout(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) { constraints ->
                val contentWidth = minOf(constraints.maxWidth, 560.dp.roundToPx())
                val contentConstraints = constraints.copy(minWidth = 0, maxWidth = contentWidth, minHeight = 0)
                val topBar = subcompose("header") {
                    Column(Modifier.fillMaxWidth()) { header() }
                }.single().measure(contentConstraints)
                val controlsPanel = subcompose("controls") {
                    Column(Modifier.fillMaxWidth()) { controls() }
                }.single().measure(contentConstraints)
                val minimum = minOf(160.dp, areaHeight * 0.55f, areaWidth)
                val topGap = if (immersive) 8.dp.roundToPx() else 0
                val availableArtworkHeight = areaHeight.roundToPx() - topBar.height - controlsPanel.height - topGap
                val gap = when {
                    !immersive -> 20.dp
                    availableArtworkHeight >= minimum.roundToPx() + 16.dp.roundToPx() -> 16.dp
                    else -> 8.dp
                }.roundToPx()
                val remaining = availableArtworkHeight - gap
                val coverSize = if (immersive) {
                    immersiveStackedArtworkSize(
                        areaWidth, areaHeight, (topBar.height + controlsPanel.height + topGap + gap).toDp(),
                    )
                } else {
                    remaining.toDp().coerceIn(minimum, areaWidth)
                }
                val cover = subcompose("artwork") {
                    Box(contentAlignment = Alignment.Center) { artwork(coverSize * artworkFraction) }
                }.single().measure(contentConstraints)
                val artworkSlot = maxOf(remaining, cover.height)
                val height = topBar.height + topGap + artworkSlot + gap + controlsPanel.height
                layout(constraints.maxWidth, maxOf(constraints.minHeight, height)) {
                    topBar.placeRelative((constraints.maxWidth - topBar.width) / 2, 0)
                    cover.placeRelative(
                        (constraints.maxWidth - cover.width) / 2,
                        topBar.height + topGap + (artworkSlot - cover.height) / 2,
                    )
                    controlsPanel.placeRelative(
                        (constraints.maxWidth - controlsPanel.width) / 2,
                        topBar.height + topGap + artworkSlot + gap,
                    )
                }
            }
        }
    }
}

internal fun immersiveStackedArtworkSize(width: Dp, height: Dp, controlsHeight: Dp): Dp {
    val comfortableMinimum = minOf(160.dp, height * 0.55f)
    return (height - controlsHeight).coerceIn(comfortableMinimum, height * 0.55f).coerceAtMost(width)
}
