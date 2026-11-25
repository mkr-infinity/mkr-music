package com.music.mkr.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.mkr.R

@Composable
internal fun MiniPlayerArtwork(url: String?, contentScale: ContentScale, modifier: Modifier = Modifier) {
    val placeholder = painterResource(R.drawable.mkr_music_small_icon)
    AsyncImage(
        model = url?.takeIf { it.isNotBlank() },
        contentDescription = null,
        placeholder = placeholder,
        fallback = placeholder,
        error = placeholder,
        contentScale = contentScale,
        modifier = modifier,
    )
}

internal fun Modifier.miniPlayerCapsule(
    restingRadius: Dp,
    color: Color,
    borderColor: Color? = null,
    topOnly: Boolean = false,
): Modifier {
    val shape = if (topOnly) RoundedCornerShape(topStart = restingRadius, topEnd = restingRadius)
    else RoundedCornerShape(restingRadius)
    val surface = clip(shape).background(color)
    return if (borderColor == null) surface else surface.border(1.dp, borderColor, shape)
}
