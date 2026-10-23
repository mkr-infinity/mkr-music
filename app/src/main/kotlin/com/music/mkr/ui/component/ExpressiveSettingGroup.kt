/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.utils.listItemShape
import com.music.mkr.ui.screens.settings.settingsHighlight

/**
 * A group of setting items styled exactly like WeatherMaster's SettingSection:
 * - No group title header
 * - Top curve, middle flat, bottom curve per item position
 * - Uses ListItem so icons auto-align: centered on title-only, top-aligned when description present
 */
@Composable
fun ExpressiveSettingGroup(
    title: String? = null,
    items: List<Material3SettingsItem>,
    modifier: Modifier = Modifier,
    itemMinHeight: Dp? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
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
            val shape = listItemShape(index, items.size)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .settingsHighlight(item.key, shape)
                    .animateContentSize(),
                shape = shape,
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                ListItem(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            enabled = item.enabled && item.onClick != null,
                            onClick = { item.onClick?.invoke() }
                        ),
                    leadingContent = item.leadingContent
                        ?: item.icon?.let { icon ->
                            {
                                if (item.tintIcon) {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (item.enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                            ),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
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
                                            .clip(
                                                item.iconShape ?: RoundedCornerShape(8.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        },
                    headlineContent = {
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
                    },
                    supportingContent = item.description,
                    trailingContent = item.trailingContent?.let { trailing ->
                        {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = if (!item.enabled)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                trailing()
                            }
                        }
                    } ?: if (item.isExternalLink && item.onClick != null) {
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
                    } else null,
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

