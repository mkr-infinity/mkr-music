/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.utils.rememberPreference

internal fun playerStylePickerColumns(width: Dp, fontScale: Float): Int =
    if (width >= 280.dp && fontScale < 1.3f) 2 else 1

@Composable
fun PlayerStyleSelectionDialog(
    currentStyle: PlayerStyle,
    onDismiss: () -> Unit,
    onSelect: (PlayerStyle) -> Unit,
) {
    var draft by rememberSaveable(currentStyle) { mutableStateOf(currentStyle) }
    val (interfaceAnimations) = rememberPreference(InterfaceAnimationsKey, true)
    val animate = interfaceAnimations && android.animation.ValueAnimator.areAnimatorsEnabled()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).widthIn(max = 560.dp).fillMaxWidth(),
        title = { Text(stringResource(R.string.player_style)) },
        text = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(playerStylePickerColumns(maxWidth, LocalDensity.current.fontScale)),
                    modifier = Modifier.heightIn(max = 440.dp).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(PlayerStyle.selectableStyles, key = { it.name }) { style ->
                        PlayerStyleCard(style, selected = draft == style, animate = animate) { draft = style }
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        confirmButton = {
            Button(
                onClick = { onSelect(draft) },
                enabled = draft != currentStyle,
            ) {
                Text(stringResource(R.string.player_style_apply))
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
private fun PlayerStyleCard(
    style: PlayerStyle,
    selected: Boolean,
    animate: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interactions = remember { MutableInteractionSource() }
    val radius by animateDpAsState(
        if (selected) 26.dp else 18.dp,
        animationSpec = if (animate) spring(dampingRatio = 1f) else snap(),
        label = "playerStyleCorners",
    )
    val border by animateColorAsState(
        if (selected) colors.primary else colors.outlineVariant,
        animationSpec = if (animate) spring() else snap(),
        label = "playerStyleBorder",
    )
    val shape = RoundedCornerShape(radius)

    Surface(
        shape = shape,
        color = if (selected) colors.secondaryContainer else colors.surfaceContainer,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().mkrPressFeedback(0.98f, interactions, animate),
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .selectable(
                    selected = selected,
                    interactionSource = interactions,
                    indication = LocalIndication.current,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayerStylePreview(style, Modifier.fillMaxWidth())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(style.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(if (selected) R.drawable.check else R.drawable.radio_button_unchecked),
                    contentDescription = null,
                    tint = if (selected) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = stringResource(style.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Static miniatures use the same artwork placement and controls as the live layouts. */
@Composable
private fun PlayerStylePreview(style: PlayerStyle, modifier: Modifier) {
    val immersive = style == PlayerStyle.IMMERSIVE
    val cinematic = style == PlayerStyle.CINEMATIC
    val colors = MaterialTheme.colorScheme
    val base = if (immersive) PlayerColorExtractor.CharcoalDark else colors.surfaceContainerLowest
    val detail = if (immersive) PlayerColorExtractor.DefaultPalette.textPrimary else colors.onSurfaceVariant
    val accent = if (immersive) PlayerColorExtractor.DefaultPalette.primaryAccent else colors.primary
    val art = if (immersive) detail.copy(alpha = 0.13f) else colors.primaryContainer

    Box(modifier = modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        val depth = detail.copy(alpha = if (immersive) 0.10f else 0.16f)
        Canvas(
            Modifier
                .widthIn(max = 172.dp)
                .fillMaxWidth()
                .aspectRatio(0.86f)
                .clip(RoundedCornerShape(14.dp))
        ) {
            val w = size.width
            val h = size.height
            fun block(x: Float, y: Float, width: Float, height: Float, color: Color, radius: Float = 0.02f) {
                drawRoundRect(
                    color, Offset(x * w, y * h), Size(width * w, height * h), CornerRadius(radius * w),
                )
            }
            drawRect(base)
            if (immersive) {
                drawCircle(art, w * 0.72f, Offset(w * 0.72f, h * 0.20f))
                drawCircle(depth, w * 0.58f, Offset(w * 0.20f, h * 0.40f))
                block(0.09f, 0.08f, 0.10f, 0.07f, depth, 0.05f)
                block(0.35f, 0.10f, 0.30f, 0.025f, detail.copy(alpha = 0.56f))
                block(0.81f, 0.08f, 0.10f, 0.07f, depth, 0.05f)
                block(0.12f, 0.55f, 0.18f, 0.033f, depth)
            } else {
                val artWidth = if (cinematic) 0.72f else 0.66f
                val artLeft = (1f - artWidth) / 2f
                block(artLeft, 0.045f, artWidth, artWidth * w / h, art, if (cinematic) 0.10f else 0.065f)
                drawCircle(depth, w * 0.21f, Offset(w * 0.50f, h * 0.045f + w * artWidth / 2f))
                drawCircle(accent, w * 0.053f, Offset(w * 0.50f, h * 0.045f + w * artWidth / 2f))
            }

            val titleY = if (immersive) 0.62f else 0.69f
            block(0.12f, titleY, 0.46f, 0.028f, detail)
            block(0.12f, titleY + 0.048f, 0.28f, 0.018f, detail.copy(alpha = 0.55f))
            block(0.12f, titleY + 0.092f, 0.76f, 0.018f, depth)
            block(0.12f, titleY + 0.092f, 0.33f, 0.018f, accent)
            val transportY = titleY + 0.18f
            val playWidth = if (cinematic) 0.24f else 0.18f
            block(0.50f - playWidth / 2, transportY - 0.034f, playWidth, 0.11f, accent, 0.075f)
            val playIcon = Path().apply {
                moveTo(w * 0.483f, h * (transportY - 0.004f))
                lineTo(w * 0.526f, h * (transportY + 0.021f))
                lineTo(w * 0.483f, h * (transportY + 0.046f))
                close()
            }
            drawPath(playIcon, if (immersive) base else colors.onPrimary)
            block(0.26f, transportY, 0.065f, 0.027f, detail)
            block(0.675f, transportY, 0.065f, 0.027f, detail)
            block(0.12f, transportY, 0.043f, 0.027f, detail.copy(alpha = 0.55f))
            block(0.837f, transportY, 0.043f, 0.027f, detail.copy(alpha = 0.55f))
        }
    }
}
