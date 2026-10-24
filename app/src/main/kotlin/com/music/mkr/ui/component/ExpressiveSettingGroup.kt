/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.ui.screens.settings.settingsHighlight
import com.music.mkr.utils.listItemShape

/**
 * A group of setting items styled with Material 3 Expressive architecture:
 * - Proper weighted row layout ensuring titles and descriptions have full width and wrap naturally
 * - Rounded grouping or individual stadium capsules for secondary settings
 * - Auto-aligned leading icons and right-aligned trailing controls
 */
@Composable
fun ExpressiveSettingGroup(
    title: String? = null,
    items: List<Material3SettingsItem>,
    modifier: Modifier = Modifier,
    itemMinHeight: Dp? = null,
    useCapsules: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = if (useCapsules) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(2.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .semantics { heading() }
                    .padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
            )
        }
        items.forEachIndexed { index, item ->
            val shape = if (useCapsules) RoundedCornerShape(24.dp) else listItemShape(index, items.size)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .settingsHighlight(item.key, shape)
                    .animateContentSize(),
                shape = shape,
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (item.onClick != null) {
                                Modifier.clickable(
                                    role = Role.Button,
                                    enabled = item.enabled,
                                    onClick = { item.onClick.invoke() }
                                )
                            } else Modifier
                        )
                        .let { if (itemMinHeight != null) it.heightIn(min = itemMinHeight) else it }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val leading = item.leadingContent ?: item.icon?.let { icon ->
                        {
                            if (item.tintIcon) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (item.enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = icon,
                                        contentDescription = null,
                                        tint = if (!item.enabled)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else
                                            MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Image(
                                    painter = icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(item.iconShape ?: RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    if (leading != null) {
                        leading()
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        ProvideTextStyle(
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (!item.enabled)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            item.title()
                        }
                        if (item.description != null) {
                            Spacer(modifier = Modifier.height(3.dp))
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = if (!item.enabled)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                item.description.invoke()
                            }
                        }
                    }

                    val trailing = item.trailingContent ?: if (item.isExternalLink && item.onClick != null) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.hyper_link),
                                contentDescription = null,
                                tint = if (!item.enabled)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (item.onClick != null) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.navigate_next),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else null

                    if (trailing != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier.widthIn(max = 160.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = if (!item.enabled)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                trailing()
                            }
                        }
                    }
                }
            }
        }
    }
}
