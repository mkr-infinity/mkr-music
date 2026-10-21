/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.constants.NavigationStyle

@Composable
fun NavigationStyleDialog(
    onDismiss: () -> Unit,
    onSelect: (NavigationStyle) -> Unit,
    current: NavigationStyle,
) {
    ListDialog(
        onDismiss = onDismiss,
        title = { Text(stringResource(R.string.navigation_style)) },
    ) {
        items(NavigationStyle.entries.toTypedArray()) { style ->
            val isSelected = style == current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(style) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                AnimatedRadioButton(
                    selected = isSelected,
                    onClick = null,
                )

                Spacer(Modifier.width(16.dp))

                // Mini preview badge illustrating the style structure
                NavigationStyleMiniPreview(style = style, isSelected = isSelected)

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(style.titleRes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        if (style == NavigationStyle.DEFAULT) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(style.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationStyleMiniPreview(
    style: NavigationStyle,
    isSelected: Boolean,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            NavigationStyle.AMBER_DOCK -> {
                // Dock with glowing amber bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                    }
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.width(14.dp).height(2.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
            }
            NavigationStyle.FLOATING_GLASS -> {
                // Frosted glass capsule with glow
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                        Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                    }
                }
            }
            NavigationStyle.EXPRESSIVE_PILL -> {
                // Pill with expanded active tab
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                    Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                }
            }
            NavigationStyle.CLEAN_MATERIAL -> {
                // Full-width bar with pill indicator
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                }
            }
        }
    }
}
