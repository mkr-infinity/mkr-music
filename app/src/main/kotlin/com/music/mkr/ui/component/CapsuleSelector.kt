/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class CapsuleOption<T>(
    val value: T,
    val title: String,
    @DrawableRes val iconRes: Int? = null,
    val subtitle: String? = null
)

/**
 * CapsuleSelectorRow provides an expressive Material 3 capsule-segmented selector
 * with high-contrast active states, charcoal-amber styling, and dynamic theme support.
 */
@Composable
fun <T> CapsuleSelectorRow(
    options: List<CapsuleOption<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    pureBlack: Boolean = false
) {
    val scrollState = rememberScrollState()
    val rowModifier = if (isScrollable) {
        modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedOption
            val itemModifier = if (isScrollable) {
                Modifier
            } else {
                Modifier.weight(1f)
            }

            CapsulePillItem(
                option = option,
                isSelected = isSelected,
                onClick = { onOptionSelected(option.value) },
                pureBlack = pureBlack,
                modifier = itemModifier
            )
        }
    }
}

@Composable
private fun <T> CapsulePillItem(
    option: CapsuleOption<T>,
    isSelected: Boolean,
    onClick: () -> Unit,
    pureBlack: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val activeBgColor = if (pureBlack) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    }

    val inactiveBgColor = if (pureBlack) {
        Color(0xFF121316)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) activeBgColor else inactiveBgColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "capsuleBgColor"
    )

    val activeBorderColor = MaterialTheme.colorScheme.primary
    val inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) activeBorderColor else inactiveBorderColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "capsuleBorderColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 180),
        label = "capsuleContentColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(animatedBgColor, RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .semantics {
                this.role = Role.RadioButton
                this.selected = isSelected
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (option.iconRes != null) {
                Icon(
                    painter = painterResource(id = option.iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = option.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
