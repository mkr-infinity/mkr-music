/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.mkr.LocalDownloadUtil
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.playback.SongDownloadStage
import com.music.mkr.playback.SongDownloadStatus
import com.music.mkr.utils.rememberPreference
import java.text.NumberFormat
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
internal fun SongDownloadCapsule(
    songId: String,
    modifier: Modifier = Modifier,
    showCompletedIcon: Boolean = true,
    idleContent: @Composable () -> Unit = {},
) = key(songId) {
    val downloadUtil = LocalDownloadUtil.current
    val download by remember(downloadUtil, songId) { downloadUtil.observeSongDownload(songId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val status = download
    var visibleAttempt by remember { mutableStateOf<Long?>(null) }
    val attempt = status?.attempt
    LaunchedEffect(attempt, status?.stage) {
        when (status?.stage) {
            SongDownloadStage.Queued, SongDownloadStage.Paused, SongDownloadStage.Downloading ->
                visibleAttempt = attempt
            SongDownloadStage.Saving -> Unit
            SongDownloadStage.Completed -> {
                if (visibleAttempt == attempt) delay(1_100)
                visibleAttempt = null
            }
            else -> visibleAttempt = null
        }
    }

    if (status == null || (status.stage == SongDownloadStage.Completed && visibleAttempt != status.attempt)) {
        if (showCompletedIcon && status != null) {
            Icon(
                painter = painterResource(R.drawable.offline),
                contentDescription = stringResource(R.string.filter_downloaded),
                modifier = Modifier.size(18.dp).padding(end = 2.dp),
            )
        }
        idleContent()
    } else {
        key(status.attempt) {
            MkrLiquidDownloadCapsule(status, modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MkrLiquidDownloadCapsule(
    status: SongDownloadStatus,
    modifier: Modifier = Modifier,
) {
    val (animationsEnabled) = rememberPreference(InterfaceAnimationsKey, true)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val animate = animationsEnabled && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val stage = status.stage
    val isComplete = stage == SongDownloadStage.Completed
    val isPaused = stage == SongDownloadStage.Paused
    val isFailed = stage == SongDownloadStage.Failed || stage == SongDownloadStage.SaveFailed
    val isDownloading = stage == SongDownloadStage.Downloading
    val progress = status.progress
    val targetFill = if (isComplete) 1f else (progress ?: 0f).coerceIn(0f, 0.99f)
    val fill = if (animate && !isPaused && (progress != null || isComplete)) {
        animateFloatAsState(
            targetValue = targetFill,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "Download liquid level",
        )
    } else rememberUpdatedState(targetFill)
    val phase = if (animate && isDownloading && targetFill > 0f) {
        rememberInfiniteTransition(label = "Download liquid waves").animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(2_800, easing = LinearEasing)),
            label = "Download wave phase",
        )
    } else rememberUpdatedState(0f)

    var completionCheck by remember { mutableStateOf(false) }
    LaunchedEffect(isComplete, animate) {
        completionCheck = false
        if (isComplete) {
            if (animate) delay(360)
            completionCheck = true
        }
    }

    val percent = if (isComplete || (progress != null && progress < 1f && (isDownloading || isPaused))) {
        val whole = if (isComplete) 100 else ((progress ?: 0f) * 100).toInt().coerceIn(0, 99)
        val locale = LocalConfiguration.current.locales[0]
        remember(locale, whole) { NumberFormat.getPercentInstance(locale).format(whole / 100.0) }
    } else null

    val description = when (stage) {
        SongDownloadStage.Queued -> stringResource(R.string.download_capsule_queued)
        SongDownloadStage.Downloading -> percent?.let { stringResource(R.string.download_capsule_progress, it) }
            ?: stringResource(R.string.download_capsule_unknown)
        SongDownloadStage.Paused -> percent?.let { stringResource(R.string.download_capsule_paused_progress, it) }
            ?: stringResource(R.string.download_capsule_paused)
        SongDownloadStage.Saving -> stringResource(R.string.download_capsule_saving)
        SongDownloadStage.Completed -> stringResource(R.string.download_capsule_completed)
        SongDownloadStage.Failed -> stringResource(R.string.download_capsule_failed)
        SongDownloadStage.SaveFailed -> stringResource(R.string.download_capsule_save_failed)
    }
    val label = when (stage) {
        SongDownloadStage.Downloading -> percent ?: stringResource(R.string.download_capsule_active)
        SongDownloadStage.Paused -> percent ?: description
        SongDownloadStage.Completed -> if (completionCheck) null else percent
        SongDownloadStage.Failed -> stringResource(R.string.download_capsule_failed_label)
        else -> description
    }
    val icon = when (stage) {
        SongDownloadStage.Queued -> R.drawable.timer
        SongDownloadStage.Paused -> R.drawable.pause
        SongDownloadStage.Failed, SongDownloadStage.SaveFailed -> R.drawable.error
        SongDownloadStage.Downloading -> if (percent == null) R.drawable.download else null
        SongDownloadStage.Completed -> if (completionCheck) R.drawable.done else null
        SongDownloadStage.Saving -> null
    }

    val colors = MaterialTheme.colorScheme
    val shape = MaterialShapes.Pill.toShape()
    val track = if (isFailed) colors.errorContainer else colors.surfaceContainerHighest
    val ink = if (isFailed) colors.onErrorContainer else colors.onSurface
    val primaryWave = lerp(track, colors.primaryContainer, 0.85f)
    val secondaryWave = lerp(track, colors.secondaryContainer, 0.65f)

    Surface(
        modifier = modifier
            .widthIn(min = 44.dp, max = 96.dp)
            .clearAndSetSemantics {
                if (isDownloading || isPaused || stage == SongDownloadStage.Saving || isComplete) {
                    stateDescription = description
                    progressBarRangeInfo = when {
                        isComplete -> ProgressBarRangeInfo(1f, 0f..1f)
                        progress != null -> ProgressBarRangeInfo(targetFill, 0f..1f)
                        else -> ProgressBarRangeInfo.Indeterminate
                    }
                } else {
                    contentDescription = description
                }
                if (isComplete || isFailed) liveRegion = LiveRegionMode.Polite
            },
        shape = shape,
        color = track,
        contentColor = ink,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .drawWithCache {
                    val back = Path()
                    val front = Path()
                    val crest = 2.5.dp.toPx()
                    val rtl = layoutDirection == LayoutDirection.Rtl
                    onDrawBehind {
                        val visibleFill = fill.value.coerceIn(0f, 1f)
                        if (visibleFill > 0f) {
                            val edge = visibleFill * size.width
                            val amplitude = minOf(crest, edge, size.width - edge)
                            val t = phase.value
                            back.setLiquidBoundary(edge, size.height, amplitude, t + 2f)
                            front.setLiquidBoundary(edge, size.height, amplitude * 0.8f, -t)
                            scale(scaleX = if (rtl) -1f else 1f, scaleY = 1f) {
                                drawPath(back, secondaryWave)
                                drawPath(front, primaryWave)
                            }
                        }
                    }
                }
                .padding(horizontal = 7.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(12.dp))
            if (label != null) Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun Path.setLiquidBoundary(edge: Float, height: Float, amplitude: Float, phase: Float) {
    reset()
    moveTo(0f, 0f)
    lineTo(edge + amplitude * sin(phase), 0f)
    cubicTo(
        edge + amplitude * sin(phase + 2.1f), height / 3f,
        edge + amplitude * sin(phase + 4.2f), height * (2f / 3f),
        edge + amplitude * sin(phase + 5.8f), height,
    )
    lineTo(0f, height)
    close()
}
