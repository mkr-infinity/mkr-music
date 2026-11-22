package com.music.mkr.ui.player

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.constants.MiniPlayerArtworkTransitionKey
import com.music.mkr.constants.MiniPlayerCapsuleMorphKey
import com.music.mkr.constants.MiniPlayerMicroEqualizerKey
import com.music.mkr.utils.rememberPreference
import kotlin.math.PI
import kotlin.math.sin

internal const val DefaultMiniPlayerMotionEnabled = true

internal data class MiniPlayerMotion(
    val playing: Boolean,
    val allowed: Boolean,
    val equalizer: Boolean,
    val artworkTransition: Boolean,
    val capsuleMorph: Boolean,
) {
    val animateEqualizer: Boolean get() = allowed && equalizer && playing
    val animateArtwork: Boolean get() = allowed && artworkTransition
    val animateCapsule: Boolean get() = allowed && capsuleMorph
}

@Composable
internal fun rememberMiniPlayerMotion(visible: Boolean): MiniPlayerMotion {
    val (interfaceAnimations) = rememberPreference(InterfaceAnimationsKey, true)
    val (equalizer) = rememberPreference(MiniPlayerMicroEqualizerKey, DefaultMiniPlayerMotionEnabled)
    val (artwork) = rememberPreference(MiniPlayerArtworkTransitionKey, DefaultMiniPlayerMotionEnabled)
    val (capsule) = rememberPreference(MiniPlayerCapsuleMorphKey, DefaultMiniPlayerMotionEnabled)
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val allowed = visible && interfaceAnimations &&
        lifecycle.isAtLeast(Lifecycle.State.RESUMED) && ValueAnimator.areAnimatorsEnabled()
    val playing = if (allowed && (equalizer || capsule)) miniPlayerIsPlaying() else false
    return MiniPlayerMotion(playing, allowed, equalizer, artwork, capsule)
}

@Composable
private fun miniPlayerIsPlaying(): Boolean {
    val service = LocalPlayerConnection.current?.service ?: return false
    val cast = service.castConnectionHandler
    val casting = cast?.isCasting?.collectAsStateWithLifecycle()?.value == true
    if (casting && cast != null) {
        val playing by cast.castIsPlaying.collectAsStateWithLifecycle()
        val buffering by cast.castIsBuffering.collectAsStateWithLifecycle()
        return playing && !buffering
    }

    val player by service.playerFlow.collectAsStateWithLifecycle()
    val activePlayer = player ?: return false
    var playing by remember(activePlayer) { mutableStateOf(activePlayer.isPlaying) }
    DisposableEffect(activePlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }
        }
        activePlayer.addListener(listener)
        playing = activePlayer.isPlaying
        onDispose { activePlayer.removeListener(listener) }
    }
    return playing
}

@Composable
internal fun MiniPlayerTitle(
    motion: MiniPlayerMotion,
    color: Color,
    title: @Composable () -> Unit,
) {
    if (!motion.equalizer) {
        title()
        return
    }

    val minTitleWidth = 96.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < minTitleWidth) {
            title()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.weight(1f)) { title() }
                MicroEqualizer(color, animate = motion.animateEqualizer)
            }
        }
    }
}

@Composable
private fun MicroEqualizer(color: Color, animate: Boolean) {
    val phase = if (animate) {
        rememberInfiniteTransition(label = "miniPlayerEqualizer").animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
            label = "miniPlayerEqualizerPhase",
        )
    } else null

    Canvas(Modifier.size(width = 13.dp, height = 12.dp)) {
        val angle = phase?.value
        val stroke = 2.dp.toPx()
        val radius = CornerRadius(stroke / 2f)
        repeat(3) { index ->
            val fraction = if (angle == null) {
                if (index == 1) 0.55f else 0.32f
            } else {
                0.60f + 0.28f * sin(angle + index * 1.4f)
            }
            val height = size.height * fraction
            drawRoundRect(
                color = color,
                topLeft = Offset((index + 0.5f) * size.width / 3f - stroke / 2f, size.height - height),
                size = Size(stroke, height),
                cornerRadius = radius,
            )
        }
    }
}

private data class MiniPlayerArtworkState(val trackId: String?, val url: String?)

@Composable
internal fun MiniPlayerArtwork(
    trackId: String?,
    url: String?,
    motion: MiniPlayerMotion,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val artwork = MiniPlayerArtworkState(trackId, url?.takeIf { it.isNotBlank() })
    if (motion.animateArtwork) {
        AnimatedContent(
            targetState = artwork,
            modifier = modifier,
            contentKey = { it.trackId },
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            label = "miniPlayerArtwork",
        ) { target ->
            MiniPlayerArtworkImage(target.url, contentScale, Modifier.fillMaxSize())
        }
    } else {
        MiniPlayerArtworkImage(artwork.url, contentScale, modifier.fillMaxSize())
    }
}

@Composable
private fun MiniPlayerArtworkImage(url: String?, contentScale: ContentScale, modifier: Modifier) {
    val placeholder = painterResource(R.drawable.mkr_music_small_icon)
    AsyncImage(
        model = url,
        contentDescription = null,
        placeholder = placeholder,
        fallback = placeholder,
        error = placeholder,
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
internal fun Modifier.miniPlayerCapsule(
    motion: MiniPlayerMotion,
    restingRadius: Dp,
    playingRadius: Dp,
    color: Color,
    borderColor: Color? = null,
    topOnly: Boolean = false,
): Modifier {
    if (!motion.animateCapsule) {
        val shape = if (topOnly) RoundedCornerShape(topStart = restingRadius, topEnd = restingRadius)
        else RoundedCornerShape(restingRadius)
        val surface = clip(shape).background(color)
        return if (borderColor == null) surface else surface.border(1.dp, borderColor, shape)
    }

    val radius = animateDpAsState(
        targetValue = if (motion.playing) playingRadius else restingRadius,
        animationSpec = tween(220),
        label = "miniPlayerCapsule",
    )
    return graphicsLayer {
        val corner = radius.value
        shape = if (topOnly) RoundedCornerShape(topStart = corner, topEnd = corner)
        else RoundedCornerShape(corner)
        clip = true
    }.background(color).then(
        if (borderColor == null) Modifier else Modifier.drawWithContent {
            drawContent()
            val stroke = 1.dp.toPx()
            val inset = stroke / 2f
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(inset, inset),
                size = Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f)),
                cornerRadius = CornerRadius((radius.value.toPx() - inset).coerceAtLeast(0f)),
                style = Stroke(stroke),
            )
        }
    )
}
