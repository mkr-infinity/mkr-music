/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.R
import com.music.mkr.constants.PlayerStyle

private val MkrCharcoalDark = Color(0xFF121316)
private val MkrCharcoalCard = Color(0xFF18191E)
private val MkrAmberAccent = Color(0xFFF59E0B)

/**
 * Redesigned Player Style selection dialog featuring visual preview cards,
 * clear descriptions, and MKR charcoal-and-amber capsule styling for all 8 styles.
 */
@Composable
fun PlayerStyleSelectionDialog(
    currentStyle: PlayerStyle,
    onDismiss: () -> Unit,
    onSelect: (PlayerStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MkrAmberAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.palette),
                            contentDescription = null,
                            tint = MkrAmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.player_style),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "9 Native Playback Interfaces",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item(key = "header_apple") {
                        Text(
                            text = "Signature Design",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MkrAmberAccent,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                        )
                    }
                    item(key = PlayerStyle.APPLE.name) {
                        PlayerStyleCard(
                            style = PlayerStyle.APPLE,
                            isSelected = currentStyle == PlayerStyle.APPLE,
                            onClick = { onSelect(PlayerStyle.APPLE) }
                        )
                    }
                    item(key = "header_expressive") {
                        Text(
                            text = "Expressive Architectures (8 Styles)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(
                        items = PlayerStyle.entries.filter { it != PlayerStyle.APPLE },
                        key = { it.name }
                    ) { style ->
                        PlayerStyleCard(
                            style = style,
                            isSelected = style == currentStyle,
                            onClick = { onSelect(style) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(android.R.string.ok),
                    color = MkrAmberAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun PlayerStyleCard(
    style: PlayerStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MkrAmberAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "styleCardBorder"
    )

    val cardBg = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Mini visual layout preview capsule
                    StylePreviewBadge(style = style, isSelected = isSelected)

                    Column {
                        Text(
                            text = stringResource(style.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MkrAmberAccent.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Active Style",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MkrAmberAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MkrAmberAccent,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(style.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Visual miniature representation reflecting the distinct architecture of each player style.
 */
@Composable
private fun StylePreviewBadge(
    style: PlayerStyle,
    isSelected: Boolean
) {
    val badgeBg = if (isSelected) MkrAmberAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            PlayerStyle.APPLE -> {
                // Apple signature: rounded art, dynamic pill play/pause, volume track
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                        Box(Modifier.size(width = 14.dp, height = 10.dp).clip(RoundedCornerShape(3.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Box(Modifier.size(width = 24.dp, height = 2.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
                }
            }
            PlayerStyle.CLASSIC -> {
                // Classic circular control cluster
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(Modifier.size(14.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                    Box(Modifier.size(6.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.MODERN -> {
                // Modern tonal pill controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(width = 8.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(Modifier.size(14.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                    Box(Modifier.size(width = 8.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.MINIMAL -> {
                // Minimal sharp square controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.CINEMATIC -> {
                // Cinematic widescreen with large bouncy center
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(Modifier.size(18.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                    Box(Modifier.size(5.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.IMMERSIVE -> {
                // Immersive ambient glow with lossless equalizer badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.equalizer),
                        contentDescription = null,
                        tint = if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(Modifier.size(width = 20.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.IMMERSIVE_EXTENDED -> {
                // Immersive Extended with volume and queue preview
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(Modifier.size(width = 16.dp, height = 3.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                    Icon(
                        painter = painterResource(R.drawable.volume_up),
                        contentDescription = null,
                        tint = if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Box(Modifier.size(width = 24.dp, height = 3.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            PlayerStyle.MATERIAL_EXTENDED -> {
                // Material 3 Expressive wavy slider and pills
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(width = 6.dp, height = 10.dp).clip(RoundedCornerShape(3.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                        Box(Modifier.size(width = 6.dp, height = 10.dp).clip(RoundedCornerShape(3.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Box(Modifier.size(width = 26.dp, height = 2.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary))
                }
            }
            PlayerStyle.EDITORIAL -> {
                // Editorial typography layout with die-cut frame
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(1.dp, if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) MkrAmberAccent else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
