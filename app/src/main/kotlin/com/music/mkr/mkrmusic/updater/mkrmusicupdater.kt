/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.mkrmusic.updater


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.music.mkr.BuildConfig
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.github.network.GitHubAssetDto
import com.music.mkr.mkrmusic.updater.downloadmanager.enqueueUpdateWithNotification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ActionButtonEmphasis
import com.music.mkr.ui.component.AnimatedActionButton
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.utils.rememberPreference
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import coil3.compose.AsyncImage
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.ViewGroup
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<MkrUpdateStatus>(MkrUpdateStatus.NoUpdate(BuildConfig.VERSION_NAME)) }
    var checkingDownload by remember { mutableStateOf(false) }

    val currentVersion = BuildConfig.VERSION_NAME
    val autoUpdateCheckEnabled = getAutoUpdateCheckSetting(context)

    fun triggerUpdateCheck() {
        status = MkrUpdateStatus.Checking
        scope.launch {
            // Add a small delay for visual feedback as in Med
            delay(1000L)
            checkForUpdate(
                context = context,
                onSuccess = { tag, isAvailable, changelog, size, date, description, imageUrl, apkUrl ->
                    saveLastCheckedTime(context, LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")))
                    saveUpdateAvailableState(context, isAvailable)
                    status = if (isAvailable) {
                        MkrUpdateStatus.Available(
                            version = tag,
                            changelog = changelog,
                            size = size,
                            releaseDate = date,
                            description = description,
                            imageUrl = imageUrl,
                            apkUrl = apkUrl
                        )
                    } else {
                        MkrUpdateStatus.NoUpdate(tag)
                    }
                },
                onError = {
                    status = MkrUpdateStatus.Error(context.getString(R.string.cant_check_updates))
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (autoUpdateCheckEnabled) {
            triggerUpdateCheck()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = MaterialTheme.colorScheme
    val (animationsEnabled) = rememberPreference(InterfaceAnimationsKey, true)
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val animate = animationsEnabled && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val actionsMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.5f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.app_updates_title), style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                color = colors.surfaceContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .heightIn(max = actionsMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val currentStatus = status) {
                        is MkrUpdateStatus.Idle, is MkrUpdateStatus.Checking, is MkrUpdateStatus.NoUpdate, is MkrUpdateStatus.Error -> {
                            val btnText = if (currentStatus is MkrUpdateStatus.Error) stringResource(R.string.try_again) else stringResource(R.string.check_for_update)
                            AnimatedActionButton(
                                onClick = { triggerUpdateCheck() },
                                enabled = currentStatus !is MkrUpdateStatus.Checking,
                                animate = animate,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.refresh),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = btnText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        is MkrUpdateStatus.Available -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val releaseUrl = "https://github.com/mkr-infinity/mkr-music/releases/tag/${currentStatus.version}"
                                val hasDirectApk = !currentStatus.apkUrl.isNullOrBlank()

                                // Button 1: Download APK
                                AnimatedActionButton(
                                    onClick = {
                                        if (!checkingDownload) {
                                            checkingDownload = true
                                            scope.launch {
                                                try {
                                                    downloadLatestUniversalUpdate(context)
                                                } finally {
                                                    checkingDownload = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = hasDirectApk && !checkingDownload,
                                    animate = animate,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.download),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            checkingDownload -> stringResource(R.string.checking_for_updates)
                                            hasDirectApk -> stringResource(R.string.download_apk)
                                            else -> stringResource(R.string.no_direct_apk)
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                // Button 2: View Release
                                AnimatedActionButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mkr-infinity/mkr-music/releases"))
                                            context.startActivity(fallbackIntent)
                                        }
                                    },
                                    animate = animate,
                                    emphasis = ActionButtonEmphasis.Tonal,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.open_in_new_icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.view_release),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                AnimatedActionButton(
                                    onClick = { navController.navigateUp() },
                                    animate = animate,
                                    emphasis = ActionButtonEmphasis.Quiet,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.later),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = colors.surface,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 700.dp)
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val lastChecked = getLastCheckedTime(context)
                    val headingStatus = if (status is MkrUpdateStatus.NoUpdate && lastChecked.isBlank()) {
                        MkrUpdateStatus.Idle
                    } else {
                        status
                    }
                    val title = stringResource(
                        when (headingStatus) {
                            MkrUpdateStatus.Idle -> R.string.system_update
                            MkrUpdateStatus.Checking -> R.string.checking_for_updates
                            is MkrUpdateStatus.Available -> R.string.update_available
                            is MkrUpdateStatus.NoUpdate -> R.string.app_up_to_date
                            is MkrUpdateStatus.Error -> R.string.update_check_error_title
                        }
                    )
                    UpdateStatusHeading(headingStatus, title, Modifier.padding(bottom = 12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        color = colors.surfaceContainerLow,
                    ) {
                        Column(
                            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                stringResource(R.string.current_version_v, currentVersion),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.update_build_version, BuildConfig.VERSION_CODE),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                            Text(
                                if (lastChecked.isBlank()) stringResource(R.string.update_not_checked_yet)
                                else stringResource(R.string.last_checked, lastChecked),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                            val available = status as? MkrUpdateStatus.Available
                            if (available != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.update_to_v, available.version),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.primary,
                                )
                                Text(
                                    stringResource(R.string.update_size, available.size.toString()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (status is MkrUpdateStatus.Checking) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (animate) {
                                val strokeWidthPx = with(LocalDensity.current) { 4.dp.toPx() }
                                val stroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }
                                LinearWavyProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = colors.primary,
                                    trackColor = colors.surfaceContainerHighest,
                                    stroke = stroke,
                                    trackStroke = stroke,
                                    amplitude = 0.55f,
                                    waveSpeed = 16.dp,
                                )
                            } else {
                                Box(
                                    Modifier.fillMaxWidth().height(4.dp)
                                        .background(colors.surfaceContainerHighest, MaterialTheme.shapes.small)
                                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        Modifier.width(32.dp).height(4.dp)
                                            .background(colors.primary, MaterialTheme.shapes.small),
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    val contentModifier = Modifier.fillMaxWidth()

                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.TopStart
                    ) {
                        AnimatedContent(
                            targetState = status,
                            transitionSpec = {
                                if (animate) {
                                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
                                } else {
                                    (EnterTransition.None togetherWith ExitTransition.None).using(null)
                                }
                            },
                            label = "statusTransition"
                        ) { currentStatus ->
                            when (currentStatus) {
                                is MkrUpdateStatus.Checking -> {
                                    Text(
                                        text = stringResource(R.string.update_checking_details),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant,
                                    )
                                }

                                is MkrUpdateStatus.NoUpdate -> {
                                    Text(
                                        text = stringResource(R.string.update_support_note),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant,
                                    )
                                }

                                is MkrUpdateStatus.Error -> {
                                    Surface(
                                        shape = MaterialTheme.shapes.large,
                                        color = colors.errorContainer,
                                        contentColor = colors.onErrorContainer,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = currentStatus.message,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                            )
                                            Text(
                                                stringResource(R.string.check_internet_connection),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }

                                is MkrUpdateStatus.Available -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        // Backup & Restore warning capsule card
                                        Surface(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            color = colors.surfaceContainer,
                                            contentColor = colors.onSurface,
                                            border = BorderStroke(1.dp, colors.outlineVariant),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.backup),
                                                        contentDescription = null,
                                                        tint = colors.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "Before updating: Back up your data",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.onSurface
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "If an in-place update fails due to a signature mismatch, uninstalling will delete the app's internal files. Export a backup to your Downloads folder first so your playlists and settings remain safe.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = colors.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                AnimatedActionButton(
                                                    onClick = { navController.navigate("settings/backup_restore") },
                                                    animate = animate,
                                                    emphasis = ActionButtonEmphasis.Tonal,
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    Text(
                                                        text = "Back up data",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                        val desc = currentStatus.description.orEmpty()
                                        if (desc.isNotBlank() || currentStatus.changelog.isNotEmpty()) {
                                            Text(
                                                stringResource(R.string.whats_new),
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
                                            )
                                        }
                                        if (desc.isNotBlank()) {
                                            val primaryColor = MaterialTheme.colorScheme.primary
                                            val annotatedText = desc.trim().parseMarkdownAndUrls(primaryColor)
                                            ClickableText(
                                                text = annotatedText,
                                                onClick = { offset ->
                                                    annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                                                        ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        if (currentStatus.changelog.isNotEmpty()) {
                                            currentStatus.changelog.forEach { section ->
                                                if (section.title.isNotBlank()) {
                                                    Text(
                                                        text = section.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.onSurface,
                                                        modifier = Modifier.padding(bottom = 16.dp)
                                                    )
                                                }
                                                if (!section.description.isNullOrBlank()) {
                                                    val primaryColor = MaterialTheme.colorScheme.primary
                                                    val annotatedText = section.description.trim().parseMarkdownAndUrls(primaryColor)
                                                    ClickableText(
                                                        text = annotatedText,
                                                        onClick = { offset ->
                                                            annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                                                                ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                                                            }
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                                    )
                                                }
                                                section.items.filter { it.isNotBlank() }.forEach { item ->
                                                    val primaryColor = MaterialTheme.colorScheme.primary
                                                    val annotatedText = "• ${item.trim()}".parseMarkdownAndUrls(primaryColor)
                                                    ClickableText(
                                                        text = annotatedText,
                                                        onClick = { offset ->
                                                            annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                                                                ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                                                            }
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                                    )
                                                }
                                                // Dynamic SDUI blocks rendered beautifully
                                                section.blocks?.filter { b ->
                                                    when (b.type) {
                                                        "image", "video" -> !b.url.isNullOrBlank()
                                                        "row", "column" -> !b.children.isNullOrEmpty()
                                                        else -> true
                                                    }
                                                }?.forEach { block ->
                                                    RenderSDUIBlock(block = block)
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                is MkrUpdateStatus.Idle -> {}
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpdateStatusHeading(
    status: MkrUpdateStatus,
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (icon, badgeColor, iconColor) = when (status) {
        MkrUpdateStatus.Idle -> Triple(R.drawable.mobile_update, colors.secondaryContainer, colors.onSecondaryContainer)
        MkrUpdateStatus.Checking -> Triple(R.drawable.refresh, colors.secondaryContainer, colors.onSecondaryContainer)
        is MkrUpdateStatus.Available -> Triple(R.drawable.download, colors.primary, colors.onPrimary)
        is MkrUpdateStatus.NoUpdate -> Triple(R.drawable.check, colors.tertiaryContainer, colors.onTertiaryContainer)
        is MkrUpdateStatus.Error -> Triple(R.drawable.error, colors.errorContainer, colors.onErrorContainer)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialShapes.Cookie12Sided.toShape(),
                color = badgeColor,
                contentColor = iconColor,
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
    }
}


// Utility functions for SharedPreferences uses now view model
const val PREFS_NAME = "settings"
const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
const val KEY_LAST_CHECKED_TIME = "last_checked_time"
const val KEY_BETA_UPDATES = "beta_updates"
const val KEY_UPDATE_AVAILABLE = "update_available"

fun getUpdateAvailableState(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_UPDATE_AVAILABLE, false)
}

fun saveUpdateAvailableState(context: Context, available: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_UPDATE_AVAILABLE, available).apply()
}

fun getAutoUpdateCheckSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_AUTO_UPDATE_CHECK, true)
}

fun saveAutoUpdateCheckSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_AUTO_UPDATE_CHECK, enabled).apply()
}

const val KEY_UPDATE_NOTIFICATIONS = "update_notifications"

fun getUpdateNotificationsSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_UPDATE_NOTIFICATIONS, true)
}

fun saveUpdateNotificationsSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_UPDATE_NOTIFICATIONS, enabled).apply()
}

const val KEY_DOWNLOAD_NOTIFICATIONS = "download_notifications"

fun getDownloadNotificationsSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_DOWNLOAD_NOTIFICATIONS, true)
}

fun saveDownloadNotificationsSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_DOWNLOAD_NOTIFICATIONS, enabled).apply()
}

fun saveLastCheckedTime(context: Context, timestamp: String) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putString(KEY_LAST_CHECKED_TIME, timestamp).apply()
}

fun getLastCheckedTime(context: Context): String {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getString(KEY_LAST_CHECKED_TIME, "") ?: ""
}

fun getBetaUpdatesSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_BETA_UPDATES, false)
}

fun saveBetaUpdatesSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_BETA_UPDATES, enabled).apply()
}

// ──────────────────────────────────────────────────────────────────────────
// 9 PM daily gate for beta/nightly update checks
// ──────────────────────────────────────────────────────────────────────────
const val KEY_LAST_NIGHTLY_CHECK_DAY = "last_nightly_check_day"

/**
 * Returns true only if BOTH conditions are met:
 *  1. The current local time is 9:00 PM (21:00) or later.
 *  2. The nightly update check has NOT already run today.
 *
 * This prevents the nightly CI check from firing on every single app launch.
 * Once it returns true and the check runs, call [markNightlyCheckDone] so
 * subsequent launches today are silently skipped.
 */
fun shouldRunNightlyCheck(context: Context): Boolean {
    val now = java.time.LocalTime.now()
    val ninepm = java.time.LocalTime.of(21, 0)
    if (now.isBefore(ninepm)) return false          // before 9 PM — skip
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val todayEpochDay = java.time.LocalDate.now().toEpochDay()
    val lastCheckedDay = prefs.getLong(KEY_LAST_NIGHTLY_CHECK_DAY, -1L)
    return lastCheckedDay != todayEpochDay           // already ran today — skip
}

/**
 * Records that the nightly check ran today so [shouldRunNightlyCheck] returns
 * false for all remaining launches until midnight.
 */
fun markNightlyCheckDone(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong(KEY_LAST_NIGHTLY_CHECK_DAY, java.time.LocalDate.now().toEpochDay())
        .apply()
}

private fun formatGitHubDate(githubDate: String): String = try {
    val githubFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")
    val dateTime = LocalDateTime.parse(githubDate, githubFormatter)
    dateTime.format(displayFormatter)
} catch (e: Exception) {
    githubDate
}

// Robust version comparison: returns true if latestVersion > currentVersion
fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
    val latestRun = Regex("-r(\\d+)").find(latestVersion)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val currentRun = Regex("-r(\\d+)").find(currentVersion)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    val latestBase = latestVersion.removePrefix("b").removePrefix("v").substringBefore("-r")
    val currentBase = currentVersion.removePrefix("b").removePrefix("v").substringBefore("-r")

    val latestParts = latestBase.split(".").map { it.toIntOrNull() ?: 0 }
    val currentParts = currentBase.split(".").map { it.toIntOrNull() ?: 0 }
    
    for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
        val latest = latestParts.getOrElse(i) { 0 }
        val current = currentParts.getOrElse(i) { 0 }
        when {
            latest > current -> return true
            latest < current -> return false
        }
    }
    
    if (latestBase == currentBase) {
        if (latestRun > currentRun) return true
        if (latestRun < currentRun) return false
        
        val latestIsBeta = latestVersion.startsWith("b")
        val currentIsBeta = currentVersion.startsWith("b")
        if (currentIsBeta && !latestIsBeta) return true
    }
    
    return false
}

internal suspend fun downloadLatestUniversalUpdate(context: Context) {
    var version: String? = null
    var error = R.string.update_check_error_title
    checkForUpdate(
        context = context,
        onSuccess = { tag, available, _, _, _, _, _, apkUrl ->
            when {
                !available -> error = R.string.app_up_to_date
                apkUrl.isNullOrBlank() -> error = R.string.no_direct_apk
                else -> version = tag
            }
        },
        onError = { },
    )
    val latestVersion = version
    if (latestVersion == null) {
        android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    try {
        enqueueUpdateWithNotification(context, latestVersion)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        val opened = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LATEST_UNIVERSAL_APK_URL)))
        }.isSuccess
        if (!opened) {
            android.widget.Toast.makeText(context, R.string.download_failed, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

// Fetches latest published release from GitHub and compares deterministic version codes
suspend fun checkForUpdate(
    context: Context,
    onSuccess: (tag: String, isAvailable: Boolean, changelog: List<ChangelogSection>, size: String, date: String, description: String?, imageUrl: String?, apkUrl: String?) -> Unit,
    onError: () -> Unit,
) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/mkr-infinity/mkr-music/releases/latest")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "MKR-Music-Android")

            val json = try {
                connection.useCaches = false
                if (connection.responseCode == 200) connection.inputStream.bufferedReader().use { it.readText() }
                else null
            } finally {
                connection.disconnect()
            }
            if (json == null) {
                withContext(Dispatchers.Main) { onError() }
                return@withContext
            }

            val targetRelease = JSONObject(json)

            val (currentVersion, currentVersionCode) = try {
                val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else @Suppress("DEPRECATION") pInfo.versionCode.toLong()
                (pInfo.versionName ?: BuildConfig.VERSION_NAME) to code
            } catch (_: Exception) {
                BuildConfig.VERSION_NAME to 10000L
            }

            val targetTagName = targetRelease.getString("tag_name")
            val assets = targetRelease.optJSONArray("assets") ?: JSONArray()

            // Safe Version Code Extraction:
            var remoteVersionCode: Long? = null
            // 1. Check for version.json in release assets
            for (k in 0 until assets.length()) {
                val a = assets.getJSONObject(k)
                if (a.getString("name").equals("version.json", ignoreCase = true)) {
                    try {
                        val vUrl = URL(a.getString("browser_download_url"))
                        val rawMeta = vUrl.openStream().bufferedReader().use { it.readText() }
                        val vObj = JSONObject(rawMeta)
                        val pkg = when {
                            vObj.has("packageName") -> vObj.optString("packageName")
                            vObj.has("package_id") -> vObj.optString("package_id")
                            else -> null
                        }
                        if (pkg == null || pkg == context.packageName || pkg == "com.kaif.music") {
                            val code = when {
                                vObj.has("versionCode") -> vObj.optLong("versionCode", -1L)
                                vObj.has("version_code") -> vObj.optLong("version_code", -1L)
                                else -> -1L
                            }
                            if (code > 0) {
                                remoteVersionCode = code
                            }
                        }
                    } catch (_: Exception) {}
                    break
                }
            }

            // 2. Strict semantic version calculation fallback: MAJOR * 10000 + MINOR * 100 + PATCH
            if (remoteVersionCode == null) {
                val semverPattern = Pattern.compile("^v?([0-9]+)\\.([0-9]+)\\.([0-9]+)$")
                val m = semverPattern.matcher(targetTagName.trim())
                if (m.matches()) {
                    val major = m.group(1)?.toLongOrNull()
                    val minor = m.group(2)?.toLongOrNull()
                    val patch = m.group(3)?.toLongOrNull()
                    if (major != null && minor != null && patch != null && minor in 0..99 && patch in 0..99) {
                        remoteVersionCode = major * 10000L + minor * 100L + patch
                    }
                }
            }

            // An update is available ONLY when the remote release's version code is strictly greater
            val isAvailable = remoteVersionCode != null && remoteVersionCode > currentVersionCode

            val apkAsset = selectUniversalUpdateAsset(
                buildList {
                    for (index in 0 until assets.length()) {
                        val asset = assets.optJSONObject(index) ?: continue
                        add(
                            GitHubAssetDto(
                                name = asset.optString("name"),
                                size = asset.optLong("size", 0L),
                                browserDownloadUrl = asset.optString("browser_download_url"),
                            )
                        )
                    }
                }
            )
            val apkSizeInMB = apkAsset?.size?.takeIf { it > 0 }?.let {
                String.format(java.util.Locale.US, "%.1f", it / (1024.0 * 1024.0))
            }.orEmpty()

            // Parse release notes directly from GitHub release body
            val changelogList = mutableListOf<ChangelogSection>()
            val body = targetRelease.optString("body", "").trim()
            var description: String? = null
            val imageUrl: String? = "https://raw.githubusercontent.com/mkr-infinity/mkr-music/main/assets/banner.png"

            if (body.isNotBlank()) {
                val lines = body.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("<") }
                
                var currentSectionTitle = context.getString(R.string.changelog)
                val currentItems = mutableListOf<String>()

                for (line in lines) {
                    if (line.startsWith("#")) {
                        if (currentItems.isNotEmpty()) {
                            changelogList.add(ChangelogSection(currentSectionTitle, currentItems.toList()))
                            currentItems.clear()
                        }
                        currentSectionTitle = line.trimStart('#', ' ')
                    } else if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•")) {
                        currentItems.add(line.trimStart('-', '*', '•', ' '))
                    } else if (!line.startsWith("---")) {
                        if (description == null) {
                            description = line
                        } else {
                            currentItems.add(line)
                        }
                    }
                }
                if (currentItems.isNotEmpty()) {
                    changelogList.add(ChangelogSection(currentSectionTitle, currentItems.toList()))
                }
            }

            if (changelogList.isEmpty()) {
                changelogList.add(ChangelogSection(context.getString(R.string.changelog), listOf("Release: $targetTagName")))
            }

            val publishedAt = targetRelease.optString("published_at", "")
            val formattedReleaseDate = formatGitHubDate(publishedAt)

            withContext(Dispatchers.Main) {
                onSuccess(
                    targetTagName,
                    isAvailable,
                    changelogList,
                    apkSizeInMB,
                    formattedReleaseDate,
                    description,
                    imageUrl,
                    if (isAvailable) apkAsset?.browserDownloadUrl else null
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e("UpdateCheck", "Error checking for updates: ${e.message}", e)
            withContext(Dispatchers.Main) { onError() }
        }
    }
}
fun String.extractUrls(): List<Pair<IntRange, String>> {
    val urlPattern = Pattern.compile(
        "(?:^|[\\s])((https?://|www\\.|pic\\.)[\\w-]+(\\.[\\w-]+)+([/?].*)?)"
    )
    val matcher = urlPattern.matcher(this)
    val urlList = mutableListOf<Pair<IntRange, String>>()

    while (matcher.find()) {
        val url = matcher.group(1)?.trim() ?: continue
        val range = IntRange(matcher.start(1), matcher.end(1) - 1)
        // Ensure URL has proper scheme
        val fullUrl = if (url.startsWith("http")) url else "https://$url"
        urlList.add(range to fullUrl)
    }

    return urlList
}

// Helper to dynamically parse robust HTML payloads and raw URLs flawlessly into Compose UI strings
fun String.parseMarkdownAndUrls(primaryColor: Color): AnnotatedString {
    val cleanHtml = this.replace("\n", "<br>") // ensure plain newlines break correctly too just in case
    val spanned = HtmlCompat.fromHtml(cleanHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
    
    return buildAnnotatedString {
        val plainText = spanned.toString()
        append(plainText)
        
        // Map Native HTML Spans to Compose Spans
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                    }
                }
                is URLSpan -> {
                    addStyle(SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline), start, end)
                    addStringAnnotation("URL", span.url, start, end)
                }
                is RelativeSizeSpan -> {
                    addStyle(SpanStyle(fontSize = TextUnit(span.sizeChange, TextUnitType.Em)), start, end)
                }
                is ForegroundColorSpan -> {
                    addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }
            }
        }
        
        // Safely extract raw text-based URLs that didn't use an <a href> tag natively
        val rawUrls = plainText.extractUrls()
        rawUrls.forEach { (range, url) ->
            addStyle(
                SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline),
                range.first, range.last + 1
            )
            addStringAnnotation("URL", url, range.first, range.last + 1)
        }
    }
}

// JSON Block parser
fun parseSDUIBlock(json: JSONObject): SDUIBlock {
    val type = json.optString("type", "text")
    val url = json.optString("url").takeIf { it.isNotEmpty() }
    
    var modifierParams: SDUIModifier? = null
    if (json.has("modifier")) {
        val modJson = json.getJSONObject("modifier")
        modifierParams = SDUIModifier(
            weight = if (modJson.has("weight")) modJson.getDouble("weight").toFloat() else null,
            heightDp = if (modJson.has("height")) modJson.getInt("height") else null,
            fillMaxWidth = modJson.optBoolean("fillMaxWidth", false),
            paddingDp = if (modJson.has("padding")) modJson.getInt("padding") else null,
            aspectRatio = if (modJson.has("aspectRatio")) modJson.getDouble("aspectRatio").toFloat() else null
        )
    }
    
    var children: List<SDUIBlock>? = null
    if (json.has("children")) {
        val childrenArray = json.getJSONArray("children")
        val parsedList = mutableListOf<SDUIBlock>()
        for (i in 0 until childrenArray.length()) {
            parsedList.add(parseSDUIBlock(childrenArray.getJSONObject(i)))
        }
        children = parsedList
    }
    
    return SDUIBlock(type, url, modifierParams, children)
}

// Dynamic Jetpack Compose layout renderer
@Composable
fun RowScope.RowSDUIItem(child: SDUIBlock) {
    val w = child.modifierParams?.weight
    val childModifier = if (w != null) Modifier.weight(w) else Modifier
    RenderSDUIBlock(child, parentModifier = childModifier)
}

@Composable
fun RenderSDUIBlock(block: SDUIBlock, parentModifier: Modifier = Modifier) {
    var mod = parentModifier
    block.modifierParams?.let { params ->
        if (params.fillMaxWidth) mod = mod.fillMaxWidth()
        if (params.heightDp != null) mod = mod.height(params.heightDp.dp)
        if (params.paddingDp != null) mod = mod.padding(params.paddingDp.dp)
        if (params.aspectRatio != null) mod = mod.aspectRatio(params.aspectRatio)
    }

    when (block.type) {
        "row" -> {
            if (block.children.isNullOrEmpty()) return
            Row(modifier = mod.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                block.children?.forEach { child -> RowSDUIItem(child) }
            }
        }
        "image" -> {
           if (block.url.isNullOrBlank()) return
           AsyncImage(
                model = block.url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = mod.clip(RoundedCornerShape(12.dp))
            )
        }
        "video" -> {
            if (block.url.isNullOrBlank()) return
            val context = LocalContext.current
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(block.url ?: "")
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ALL
                }
            }
            DisposableEffect(exoPlayer) {
                onDispose { exoPlayer.release() }
            }
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // looping aesthetics
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = mod.clip(RoundedCornerShape(12.dp))
            )
        }
    }
}
