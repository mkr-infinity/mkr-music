/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.R
import kotlin.math.abs

/**
 * Directional swipe indicator that provides visual and interactive feedback
 * during mini-player horizontal track-skip gestures.
 *
 * Behavior:
 * - Drag right (offsetX > 0): Reveals "Previous track" pill on the left
 * - Drag left (offsetX < 0): Reveals "Next track" pill on the right
 * - Handles both LTR and RTL layouts correctly so visual placement matches finger reveal
 * - Proportional scaling and alpha reveal up to threshold
 * - Glowing highlight state when crossing skip threshold
 */
@Composable
fun DirectionalSwipeIndicator(
    offsetX: Float,
    threshold: Int,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    modifier: Modifier = Modifier
) {
    if (offsetX == 0f) return

    val isSwipingRight = offsetX > 0f
    val absOffset = abs(offsetX)
    val progress = (absOffset / threshold.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val isTriggered = absOffset >= threshold

    val alpha = (progress * 1.5f).coerceIn(0f, 1f)
    val scale = 0.8f + (0.2f * progress)

    val isAllowed = if (isSwipingRight) canSkipPrevious else canSkipNext

    val amberColor = Color(0xFFF59E0B)
    val amberLight = Color(0xFFFBBF24)
    val charcoalBg = Color(0xFF18191E)

    val activeColor = if (isAllowed) amberColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val borderColor = if (isTriggered && isAllowed) amberLight else activeColor.copy(alpha = 0.35f)

    val layoutDirection = LocalLayoutDirection.current
    val contentAlignment = if (isSwipingRight) {
        if (layoutDirection == LayoutDirection.Rtl) Alignment.CenterEnd else Alignment.CenterStart
    } else {
        if (layoutDirection == LayoutDirection.Rtl) Alignment.CenterStart else Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentAlignment = contentAlignment
    ) {
        Surface(
            modifier = Modifier
                .height(44.dp)
                .scale(scale),
            shape = RoundedCornerShape(22.dp),
            color = charcoalBg.copy(alpha = 0.92f * alpha),
            border = BorderStroke(1.dp, borderColor.copy(alpha = alpha))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSwipingRight) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(activeColor.copy(alpha = 0.15f * alpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_previous),
                            contentDescription = stringResource(R.string.previous_track),
                            tint = activeColor.copy(alpha = alpha),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.previous_track),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = (if (isTriggered && isAllowed) amberLight else Color(0xFFF8FAFC)).copy(alpha = alpha)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.next_track),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = (if (isTriggered && isAllowed) amberLight else Color(0xFFF8FAFC)).copy(alpha = alpha)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(activeColor.copy(alpha = 0.15f * alpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = stringResource(R.string.next_track),
                            tint = activeColor.copy(alpha = alpha),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
