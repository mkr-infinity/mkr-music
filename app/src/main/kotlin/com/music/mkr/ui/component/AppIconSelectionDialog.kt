/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.music.mkr.R
import com.music.mkr.utils.AppIcon
import com.music.mkr.utils.CreatorAttribution
import com.music.mkr.utils.IconUtils

@Composable
fun AppIconSelectionDialog(
    currentIcon: AppIcon,
    pendingIcon: AppIcon? = null,
    onDismiss: () -> Unit,
    onSelect: (AppIcon) -> Unit,
    onApplyAndRestart: ((AppIcon) -> Unit)? = null
) {
    val icons = remember { AppIcon.entries.toList() }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val dialogMaxWidth = if (screenWidth >= 600.dp) 560.dp else 420.dp
    val listMaxHeight = (screenHeight * 0.58f).coerceIn(240.dp, 520.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(min = 280.dp, max = dialogMaxWidth)
            .fillMaxWidth(0.92f)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Choose App Icon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${icons.size} icons",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalize your launcher appearance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = listMaxHeight)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(icons, key = { _, icon -> icon.key }) { index, icon ->
                            val isActive = icon == currentIcon
                            val isPending = pendingIcon == icon
                            IconCapsuleItem(
                                index = index,
                                icon = icon,
                                isActive = isActive,
                                isPending = isPending,
                                onClick = { onSelect(icon) },
                                onOpenProfile = { attribution ->
                                    IconUtils.openCreatorProfile(context, attribution)
                                }
                            )
                        }
                    }
                }

                // Banner inside dialog when a new pending icon is selected
                AnimatedVisibility(
                    visible = pendingIcon != null && pendingIcon != currentIcon,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pending restart",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${pendingIcon?.title} selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        initialDelayMillis = 1500,
                                        velocity = 25.dp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    pendingIcon?.let { onApplyAndRestart?.invoke(it) }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Restart now", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

/**
 * Capsule-style row with alternating icon-left / icon-right alignment,
 * dynamic text layout adapting to device width without overlap,
 * and an expressive wavy animated border around the app icon.
 */
@Composable
private fun IconCapsuleItem(
    index: Int,
    icon: AppIcon,
    isActive: Boolean,
    isPending: Boolean,
    onClick: () -> Unit,
    onOpenProfile: (CreatorAttribution) -> Unit
) {
    val isIconOnLeft = index % 2 == 0

    val capsuleBg = when {
        isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isPending -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.32f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val capsuleBorder = when {
        isActive -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
        isPending -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = capsuleBg,
        border = capsuleBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .selectable(
                selected = isActive || isPending,
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val iconComposable: @Composable () -> Unit = {
                WavyAnimatedIcon(
                    previewRes = icon.previewDrawableRes,
                    contentDescription = icon.title,
                    isActive = isActive,
                    isPending = isPending
                )
            }

            val textComposable: @Composable () -> Unit = {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isIconOnLeft) Alignment.Start else Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = icon.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isPending -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 2000,
                            velocity = 25.dp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isIconOnLeft) Arrangement.Start else Arrangement.End
                    ) {
                        Text(
                            text = "by ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenProfile(icon.attribution) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(icon.attribution.platform.iconRes),
                                    contentDescription = icon.attribution.platform.platformName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = icon.attribution.username,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (isIconOnLeft) {
                iconComposable()
                Spacer(modifier = Modifier.width(14.dp))
                textComposable()
            } else {
                textComposable()
                Spacer(modifier = Modifier.width(14.dp))
                iconComposable()
            }
        }
    }
}

/**
 * Material Expressive icon preview with smooth sinusoidal wavy animated outline.
 */
@Composable
private fun WavyAnimatedIcon(
    previewRes: Int,
    contentDescription: String,
    isActive: Boolean,
    isPending: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyBorderTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavyPhase"
    )

    val strokeColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isPending -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(66.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) - 4.dp.toPx()
            val waveAmplitude = 2.2.dp.toPx()
            val waveCount = 8

            val path = Path()
            val step = Math.PI / 40.0
            var first = true

            var angle = 0.0
            while (angle <= 2 * Math.PI) {
                val r = baseRadius + waveAmplitude * kotlin.math.sin(waveCount * angle + phase).toFloat()
                val x = center.x + r * kotlin.math.cos(angle).toFloat()
                val y = center.y + r * kotlin.math.sin(angle).toFloat()
                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                angle += step
            }
            path.close()

            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = if (isActive || isPending) 2.2.dp.toPx() else 1.5.dp.toPx()
                )
            )
        }

        Image(
            painter = painterResource(previewRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
        )

        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = "Active icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp)
                )
            }
        } else if (isPending) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = "Pending restart",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
