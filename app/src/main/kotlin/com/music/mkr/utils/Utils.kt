/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.audiofx.AudioEffect
import android.widget.Toast
import com.music.mkr.R
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.compose.ui.graphics.Shape
import java.util.Locale
fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
}

@Suppress("DEPRECATION")
fun setAppLocale(context: Context, locale: Locale) {
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun listItemShape(index: Int, count: Int, radius: Dp = 16.dp): Shape {
    val smoothness = 60
    return when {
        count == 1 -> AbsoluteSmoothCornerShape(
            cornerRadiusTL = 16.dp, smoothnessAsPercentTL = smoothness,
            cornerRadiusTR = 16.dp, smoothnessAsPercentTR = smoothness,
            cornerRadiusBL = 16.dp, smoothnessAsPercentBL = smoothness,
            cornerRadiusBR = 16.dp, smoothnessAsPercentBR = smoothness
        )
        index == 0 -> AbsoluteSmoothCornerShape(
            cornerRadiusTL = radius, smoothnessAsPercentTL = smoothness,
            cornerRadiusTR = radius, smoothnessAsPercentTR = smoothness,
            cornerRadiusBL = 0.dp, smoothnessAsPercentBL = 0,
            cornerRadiusBR = 0.dp, smoothnessAsPercentBR = 0
        )
        index == count - 1 -> AbsoluteSmoothCornerShape(
            cornerRadiusTL = 0.dp, smoothnessAsPercentTL = 0,
            cornerRadiusTR = 0.dp, smoothnessAsPercentTR = 0,
            cornerRadiusBL = radius, smoothnessAsPercentBL = smoothness,
            cornerRadiusBR = radius, smoothnessAsPercentBR = smoothness
        )
        else -> RectangleShape
    }
}

fun openSystemEqualizer(context: Context, audioSessionId: Int = 0) {
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
        if (audioSessionId != 0) {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        }
        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(
            context,
            context.getString(R.string.no_system_equalizer),
            Toast.LENGTH_SHORT
        ).show()
    }
}
