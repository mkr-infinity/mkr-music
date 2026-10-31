/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.music.mkr.BuildConfig
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.AudioNormalizationKey
import com.music.mkr.constants.AudioOffload
import com.music.mkr.constants.AudioQuality
import com.music.mkr.constants.AudioQualityKey
import com.music.mkr.constants.AutoDownloadOnLikeKey
import com.music.mkr.constants.CrossfadeCurve
import com.music.mkr.constants.CrossfadeCurveKey
import com.music.mkr.constants.CrossfadeDurationKey
import com.music.mkr.constants.CrossfadeEnabledKey
import com.music.mkr.constants.CrossfadeGaplessKey
import com.music.mkr.constants.CrossfadeManualSkipKey
import com.music.mkr.constants.AutoLoadMoreKey
import com.music.mkr.constants.AutoSkipNextOnErrorKey
import com.music.mkr.constants.DisableLoadMoreWhenRepeatAllKey
import com.music.mkr.constants.EnableGoogleCastKey
import com.music.mkr.constants.EnableSponsorBlockKey
import com.music.mkr.constants.HistoryDuration
import com.music.mkr.constants.KeepScreenOn
import com.music.mkr.constants.PauseOnMute
import com.music.mkr.constants.PersistentQueueKey
import com.music.mkr.constants.PersistentShuffleAcrossQueuesKey
import com.music.mkr.constants.PreventDuplicateTracksInQueueKey
import com.music.mkr.constants.RememberShuffleAndRepeatKey
import com.music.mkr.constants.ResumeOnBluetoothConnectKey
import com.music.mkr.constants.SeekExtraSeconds
import com.music.mkr.constants.ShufflePlaylistFirstKey
import com.music.mkr.constants.SimilarContent
import com.music.mkr.constants.SkipSilenceInstantKey
import com.music.mkr.constants.SkipSilenceKey
import com.music.mkr.constants.StopMusicOnTaskClearKey
import com.music.mkr.constants.LiquidMiniPlayerKey
import com.music.mkr.constants.PureBlackMiniPlayerKey
import com.music.mkr.constants.MiniPlayerBackgroundStyleKey
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.constants.PlayerStyleKey
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import com.music.mkr.ui.component.PlayerStyleSelectionDialog
import com.music.mkr.ui.screens.settings.MiniPlayerDesignOption
import com.music.mkr.ui.component.ActionPromptDialog
import com.music.mkr.ui.component.CrossfadeCurvePreview
import com.music.mkr.ui.component.DefaultDialog
import com.music.mkr.ui.component.EnumDialog
import com.music.mkr.ui.component.EqualizerDialog
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val (crossfadeEnabled, onCrossfadeEnabledChange) = rememberPreference(
        CrossfadeEnabledKey,
        defaultValue = false
    )
    val (crossfadeDuration, onCrossfadeDurationChange) = rememberPreference(
        CrossfadeDurationKey,
        defaultValue = 5f
    )
    val (crossfadeCurve, onCrossfadeCurveChange) = rememberEnumPreference(
        CrossfadeCurveKey,
        defaultValue = CrossfadeCurve.EASE_OUT_QUAD
    )
    val (crossfadeGapless, onCrossfadeGaplessChange) = rememberPreference(
        CrossfadeGaplessKey,
        defaultValue = true
    )
    val (crossfadeManualSkip, onCrossfadeManualSkipChange) = rememberPreference(
        CrossfadeManualSkipKey,
        defaultValue = false
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(
        PersistentQueueKey,
        defaultValue = true
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )
    val (skipSilenceInstant, onSkipSilenceInstantChange) = rememberPreference(
        SkipSilenceInstantKey,
        defaultValue = false
    )
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        AudioNormalizationKey,
        defaultValue = true
    )

    val (audioOffload, onAudioOffloadChange) = rememberPreference(
        key = AudioOffload,
        defaultValue = false
    )

    val (enableGoogleCast, onEnableGoogleCastChange) = rememberPreference(
        key = EnableGoogleCastKey,
        defaultValue = true
    )

    val (seekExtraSeconds, onSeekExtraSeconds) = rememberPreference(
        SeekExtraSeconds,
        defaultValue = false
    )

    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(
        AutoLoadMoreKey,
        defaultValue = true
    )
    val (disableLoadMoreWhenRepeatAll, onDisableLoadMoreWhenRepeatAllChange) = rememberPreference(
        DisableLoadMoreWhenRepeatAllKey,
        defaultValue = false
    )
    val (autoDownloadOnLike, onAutoDownloadOnLikeChange) = rememberPreference(
        AutoDownloadOnLikeKey,
        defaultValue = false
    )
    val (similarContentEnabled, similarContentEnabledChange) = rememberPreference(
        key = SimilarContent,
        defaultValue = true
    )
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(
        AutoSkipNextOnErrorKey,
        defaultValue = false
    )
    val (persistentShuffleAcrossQueues, onPersistentShuffleAcrossQueuesChange) = rememberPreference(
        PersistentShuffleAcrossQueuesKey,
        defaultValue = false
    )
    val (rememberShuffleAndRepeat, onRememberShuffleAndRepeatChange) = rememberPreference(
        RememberShuffleAndRepeatKey,
        defaultValue = true
    )
    val (shufflePlaylistFirst, onShufflePlaylistFirstChange) = rememberPreference(
        ShufflePlaylistFirstKey,
        defaultValue = false
    )
    val (preventDuplicateTracksInQueue, onPreventDuplicateTracksInQueueChange) = rememberPreference(
        PreventDuplicateTracksInQueueKey,
        defaultValue = true
    )
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(
        StopMusicOnTaskClearKey,
        defaultValue = false
    )
    val (pauseOnMute, onPauseOnMuteChange) = rememberPreference(
        PauseOnMute,
        defaultValue = false
    )
    val (resumeOnBluetoothConnect, onResumeOnBluetoothConnectChange) = rememberPreference(
        ResumeOnBluetoothConnectKey,
        defaultValue = false
    )
    val (keepScreenOn, onKeepScreenOnChange) = rememberPreference(
        KeepScreenOn,
        defaultValue = false
    )
    val (historyDuration, onHistoryDurationChange) = rememberPreference(
        HistoryDuration,
        defaultValue = 30f
    )
    val (sponsorBlockEnabled, _) = rememberPreference(
        EnableSponsorBlockKey,
        defaultValue = true
    )

    var showAudioQualityDialog by remember {
        mutableStateOf(false)
    }

    if (showAudioQualityDialog) {
        EnumDialog(
            onDismiss = { showAudioQualityDialog = false },
            onSelect = {
                onAudioQualityChange(it)
                showAudioQualityDialog = false
            },
            title = stringResource(R.string.audio_quality),
            current = audioQuality,
            values = AudioQuality.values().toList(),
            valueText = {
                when (it) {
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )
    }



    val (rawPlayerStyle, onPlayerStyleChange) = rememberEnumPreference(
        PlayerStyleKey,
        defaultValue = PlayerStyle.IMMERSIVE
    )
    val playerStyle = remember(rawPlayerStyle) {
        if (rawPlayerStyle == PlayerStyle.APPLE) PlayerStyle.IMMERSIVE else rawPlayerStyle
    }
    val (useAppleMiniPlayer, onUseAppleMiniPlayerChange) = rememberPreference(
        UseAppleMiniPlayerKey,
        defaultValue = false
    )
    val (useNewMiniPlayerDesign, onUseNewMiniPlayerDesignChange) = rememberPreference(
        UseNewMiniPlayerDesignKey,
        defaultValue = true
    )
    val (liquidMiniPlayer, onLiquidMiniPlayerChange) = rememberPreference(
        LiquidMiniPlayerKey,
        defaultValue = true
    )
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(
        PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.GRADIENT
    )

    val (pureBlackMiniPlayer, onPureBlackMiniPlayerChange) = rememberPreference(
        PureBlackMiniPlayerKey,
        defaultValue = false
    )
    val (miniPlayerBackground, onMiniPlayerBackgroundChange) = rememberEnumPreference(
        MiniPlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )

    var showPlayerDesignDialog by remember { mutableStateOf(false) }
    var showMiniPlayerDesignDialog by remember { mutableStateOf(false) }
    var showPlayerBackgroundDialog by remember { mutableStateOf(false) }
    var showMiniPlayerBackgroundDialog by remember { mutableStateOf(false) }
    var showCrossfadeBetaDialog by remember { mutableStateOf(false) }
    var showCrossfadeCurveDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    if (showEqualizerDialog) {
        EqualizerDialog(
            onDismiss = { showEqualizerDialog = false }
        )
    }

    if (showPlayerDesignDialog) {
        PlayerStyleSelectionDialog(
            currentStyle = playerStyle,
            onDismiss = { showPlayerDesignDialog = false },
            onSelect = { selectedStyle ->
                onPlayerStyleChange(selectedStyle)
                if (selectedStyle != PlayerStyle.CLASSIC && selectedStyle != PlayerStyle.APPLE && playerBackground == PlayerBackgroundStyle.APPLE_MUSIC) {
                    onPlayerBackgroundChange(PlayerBackgroundStyle.DEFAULT)
                }
                showPlayerDesignDialog = false
            }
        )
    }

    val currentMiniPlayerOption = remember(useAppleMiniPlayer, useNewMiniPlayerDesign) {
        when {
            useAppleMiniPlayer -> MiniPlayerDesignOption.APPLE
            useNewMiniPlayerDesign -> MiniPlayerDesignOption.NEW
            else -> MiniPlayerDesignOption.CLASSIC
        }
    }

    if (showMiniPlayerDesignDialog) {
        EnumDialog(
            onDismiss = { showMiniPlayerDesignDialog = false },
            onSelect = { option ->
                when (option) {
                    MiniPlayerDesignOption.CLASSIC -> {
                        onUseAppleMiniPlayerChange(false)
                        onUseNewMiniPlayerDesignChange(false)
                    }
                    MiniPlayerDesignOption.NEW -> {
                        onUseAppleMiniPlayerChange(false)
                        onUseNewMiniPlayerDesignChange(true)
                    }
                    MiniPlayerDesignOption.APPLE -> {
                        onUseAppleMiniPlayerChange(true)
                        onUseNewMiniPlayerDesignChange(false)
                    }
                }
                showMiniPlayerDesignDialog = false
            },
            title = stringResource(R.string.mini_player),
            current = currentMiniPlayerOption,
            values = MiniPlayerDesignOption.values().toList(),
            valueText = {
                when (it) {
                    MiniPlayerDesignOption.CLASSIC -> "Classic mini player"
                    MiniPlayerDesignOption.NEW -> "Expressive mini player"
                    MiniPlayerDesignOption.APPLE -> "Apple-style capsule"
                }
            }
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = {
                onPlayerBackgroundChange(it)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = PlayerBackgroundStyle.values().toList(),
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.APPLE_MUSIC -> stringResource(R.string.apple_music)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                }
            }
        )
    }

    if (showMiniPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showMiniPlayerBackgroundDialog = false },
            onSelect = {
                onMiniPlayerBackgroundChange(it)
                showMiniPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.miniplayer_background_style),
            current = miniPlayerBackground,
            values = listOf(
                PlayerBackgroundStyle.DEFAULT,
                PlayerBackgroundStyle.GRADIENT,
                PlayerBackgroundStyle.BLUR,
                PlayerBackgroundStyle.GLOW_ANIMATED,
                PlayerBackgroundStyle.LIVE_MESH
            ),
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                    else -> stringResource(R.string.follow_theme)
                }
            }
        )
    }

    if (showCrossfadeCurveDialog) {
        EnumDialog(
            onDismiss = { showCrossfadeCurveDialog = false },
            onSelect = {
                onCrossfadeCurveChange(it)
                showCrossfadeCurveDialog = false
            },
            title = stringResource(R.string.crossfade_curve),
            current = crossfadeCurve,
            values = CrossfadeCurve.entries,
            valueText = {
                when (it) {
                    CrossfadeCurve.EQUAL_POWER -> stringResource(R.string.crossfade_curve_entry_equal_power)
                    CrossfadeCurve.EASE_OUT_QUAD -> stringResource(R.string.crossfade_curve_entry_ease_out_quad)
                    CrossfadeCurve.EASE_OUT_CUBIC -> stringResource(R.string.crossfade_curve_entry_ease_out_cubic)
                    CrossfadeCurve.SMOOTHSTEP -> stringResource(R.string.crossfade_curve_entry_smoothstep)
                }
            }
        )
    }

    if (showCrossfadeBetaDialog) {
        ActionPromptDialog(
            onDismiss = { showCrossfadeBetaDialog = false },
            title = stringResource(R.string.crossfade_beta_title),
            onCancel = { showCrossfadeBetaDialog = false },
            onConfirm = {
                showCrossfadeBetaDialog = false
                onCrossfadeEnabledChange(true)
            },
            content = {
                Text(stringResource(R.string.crossfade_beta_message))
            }
        )
    }

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )
        // Group 1 — Player Architecture & Styling
        ExpressiveSettingGroup(
            title = "Player Architecture & Styling",
            items = listOf(
                Material3SettingsItem(
                    key = "appearance_player_style",
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.player_style)) },
                    description = { Text("Choose from 8 distinct player designs") },
                    trailingContent = {
                        Text(stringResource(playerStyle.titleRes))
                    },
                    onClick = { showPlayerDesignDialog = true }
                ),
                Material3SettingsItem(
                    key = "appearance_player_background",
                    icon = painterResource(R.drawable.gradient),
                    title = { Text(stringResource(R.string.player_background_style)) },
                    description = { Text("Apple Music fluid mesh, gradient, animated glow or theme") },
                    trailingContent = {
                        Text(
                            when (playerBackground) {
                                PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                                PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                                PlayerBackgroundStyle.APPLE_MUSIC -> stringResource(R.string.apple_music)
                                PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                            }
                        )
                    },
                    onClick = { showPlayerBackgroundDialog = true }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group 2 — Mini Player
        ExpressiveSettingGroup(
            title = stringResource(R.string.mini_player),
            items = listOf(
                Material3SettingsItem(
                    key = "appearance_apple_player",
                    icon = painterResource(R.drawable.nav_bar),
                    title = { Text(stringResource(R.string.mini_player)) },
                    description = { 
                        Text(
                            text = "Classic bar, Expressive pill, or Apple capsule",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when (currentMiniPlayerOption) {
                                        MiniPlayerDesignOption.CLASSIC -> "Classic"
                                        MiniPlayerDesignOption.NEW -> "Expressive"
                                        MiniPlayerDesignOption.APPLE -> "Apple"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    onClick = { showMiniPlayerDesignDialog = true }
                ),
                Material3SettingsItem(
                    key = "appearance_liquid_mini_player",
                    icon = painterResource(R.drawable.sparks),
                    title = { Text("Liquid Mini-Player") },
                    description = { Text("Fluid organic water progress animation on mini-player") },
                    trailingContent = {
                        Switch(
                            checked = liquidMiniPlayer,
                            onCheckedChange = onLiquidMiniPlayerChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (liquidMiniPlayer) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLiquidMiniPlayerChange(!liquidMiniPlayer) }
                ),
                Material3SettingsItem(
                    key = "appearance_pure_black",
                    icon = painterResource(R.drawable.contrast),
                    title = { Text(stringResource(R.string.pure_black_mini_player)) },
                    description = { Text("Use pure AMOLED black background on mini player") },
                    trailingContent = {
                        Switch(
                            checked = pureBlackMiniPlayer,
                            onCheckedChange = onPureBlackMiniPlayerChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (pureBlackMiniPlayer) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPureBlackMiniPlayerChange(!pureBlackMiniPlayer) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.miniplayer_background_style)) },
                    description = { Text("Follow theme, gradient, blur, or animated glow") },
                    trailingContent = {
                        Text(
                            when (miniPlayerBackground) {
                                PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                                PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                                PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                                else -> stringResource(R.string.follow_theme)
                            }
                        )
                    },
                    onClick = { showMiniPlayerBackgroundDialog = true }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group 3 — Audio Quality & Sound
        ExpressiveSettingGroup(
            title = "Audio Quality & Sound",
            items = buildList {
                add(Material3SettingsItem(
                    key = "player_audio_quality",
                    icon = painterResource(R.drawable.graphic_eq),
                    title = { Text(stringResource(R.string.audio_quality)) },
                    trailingContent = {
                        Text(
                            when (audioQuality) {
                                AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                                AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                                AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                            }
                        )
                    },
                    onClick = { showAudioQualityDialog = true }
                ))
                add(Material3SettingsItem(
                    key = "player_equalizer",
                    icon = painterResource(R.drawable.equalizer),
                    title = { Text(stringResource(R.string.equalizer)) },
                    description = { Text("Built-in acoustic tuning, presets & sound effects") },
                    onClick = { showEqualizerDialog = true }
                ))
                add(Material3SettingsItem(
                    key = "player_audio_normalization",
                    icon = painterResource(R.drawable.volume_up),
                    title = { Text(stringResource(R.string.audio_normalization)) },
                    trailingContent = {
                        Switch(
                            checked = audioNormalization,
                            onCheckedChange = onAudioNormalizationChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (audioNormalization) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAudioNormalizationChange(!audioNormalization) }
                ))
                add(Material3SettingsItem(
                    key = "player_audio_offload",
                    icon = painterResource(R.drawable.graphic_eq),
                    title = { Text(stringResource(R.string.audio_offload)) },
                    description = {
                        Text(
                            if (crossfadeEnabled) stringResource(R.string.audio_offload_disabled_by_crossfade)
                            else stringResource(R.string.audio_offload_description)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = if (crossfadeEnabled) false else audioOffload,
                            onCheckedChange = onAudioOffloadChange,
                            enabled = !crossfadeEnabled,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (!crossfadeEnabled && audioOffload) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { if (!crossfadeEnabled) onAudioOffloadChange(!audioOffload) }
                ))
                add(Material3SettingsItem(
                    key = "player_skip_silence",
                    icon = painterResource(R.drawable.fast_forward),
                    title = { Text(stringResource(R.string.skip_silence)) },
                    description = { Text(stringResource(R.string.skip_silence_desc)) },
                    trailingContent = {
                        Switch(
                            checked = skipSilence,
                            onCheckedChange = onSkipSilenceChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (skipSilence) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSkipSilenceChange(!skipSilence) }
                ))
                add(Material3SettingsItem(
                    icon = painterResource(R.drawable.skip_next),
                    title = { Text(stringResource(R.string.skip_silence_instant)) },
                    description = { Text(stringResource(R.string.skip_silence_instant_desc)) },
                    trailingContent = {
                        Switch(
                            checked = skipSilenceInstant,
                            onCheckedChange = { onSkipSilenceInstantChange(it) },
                            enabled = skipSilence,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (skipSilenceInstant) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { if (skipSilence) onSkipSilenceInstantChange(!skipSilenceInstant) }
                ))
                add(Material3SettingsItem(
                    key = "player_seek_seconds",
                    icon = painterResource(R.drawable.arrow_forward),
                    title = { Text(stringResource(R.string.seek_seconds_addup)) },
                    description = { Text(stringResource(R.string.seek_seconds_addup_description)) },
                    trailingContent = {
                        Switch(
                            checked = seekExtraSeconds,
                            onCheckedChange = onSeekExtraSeconds,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (seekExtraSeconds) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSeekExtraSeconds(!seekExtraSeconds) }
                ))
                // Only show Cast setting in GMS builds (not in F-Droid/FOSS)
                if (BuildConfig.CAST_AVAILABLE) {
                    add(Material3SettingsItem(
                        key = "player_google_cast",
                        icon = painterResource(R.drawable.cast),
                        title = { Text(stringResource(R.string.google_cast)) },
                        description = { Text(stringResource(R.string.google_cast_description)) },
                        trailingContent = {
                            Switch(
                                checked = enableGoogleCast,
                                onCheckedChange = onEnableGoogleCastChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (enableGoogleCast) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onEnableGoogleCastChange(!enableGoogleCast) }
                    ))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group 4 — Transitions & Crossfade
        ExpressiveSettingGroup(
            title = stringResource(R.string.crossfade),
            items = buildList {
                add(Material3SettingsItem(
                    key = "player_crossfade",
                    icon = painterResource(R.drawable.linear_scale),
                    title = { Text(stringResource(R.string.crossfade)) },
                    description = { Text(stringResource(R.string.crossfade_desc)) },
                    trailingContent = {
                        Switch(
                            checked = crossfadeEnabled,
                            onCheckedChange = {
                                if (!crossfadeEnabled) {
                                    showCrossfadeBetaDialog = true
                                } else {
                                    onCrossfadeEnabledChange(false)
                                }
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (crossfadeEnabled) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        if (!crossfadeEnabled) {
                            showCrossfadeBetaDialog = true
                        } else {
                            onCrossfadeEnabledChange(false)
                        }
                    }
                ))
                if (crossfadeEnabled) {
                    add(Material3SettingsItem(
                        icon = painterResource(R.drawable.timer),
                        title = { Text(stringResource(R.string.crossfade_duration)) },
                        description = {
                            Column {
                                Text(pluralStringResource(R.plurals.seconds, crossfadeDuration.toInt(), crossfadeDuration.toInt()))
                                Slider(
                                    value = crossfadeDuration,
                                    onValueChange = onCrossfadeDurationChange,
                                    valueRange = 1f..15f,
                                    steps = 14
                                )
                            }
                        }
                    ))
                    add(Material3SettingsItem(
                        icon = painterResource(R.drawable.linear_scale),
                        title = { Text(stringResource(R.string.crossfade_curve)) },
                        description = {
                            Column {
                                Text(
                                    when (crossfadeCurve) {
                                        CrossfadeCurve.EQUAL_POWER -> stringResource(R.string.crossfade_curve_entry_equal_power)
                                        CrossfadeCurve.EASE_OUT_QUAD -> stringResource(R.string.crossfade_curve_entry_ease_out_quad)
                                        CrossfadeCurve.EASE_OUT_CUBIC -> stringResource(R.string.crossfade_curve_entry_ease_out_cubic)
                                        CrossfadeCurve.SMOOTHSTEP -> stringResource(R.string.crossfade_curve_entry_smoothstep)
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                CrossfadeCurvePreview(
                                    curve = crossfadeCurve,
                                    durationSeconds = crossfadeDuration,
                                )
                            }
                        },
                        onClick = { showCrossfadeCurveDialog = true }
                    ))
                    add(Material3SettingsItem(
                        icon = painterResource(R.drawable.album),
                        title = { Text(stringResource(R.string.crossfade_gapless)) },
                        description = { Text(stringResource(R.string.crossfade_gapless_desc)) },
                        trailingContent = {
                            Switch(
                                checked = crossfadeGapless,
                                onCheckedChange = onCrossfadeGaplessChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (crossfadeGapless) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onCrossfadeGaplessChange(!crossfadeGapless) }
                    ))
                    add(Material3SettingsItem(
                        icon = painterResource(R.drawable.fast_forward),
                        title = { Text(stringResource(R.string.crossfade_manual_skip)) },
                        description = { Text(stringResource(R.string.crossfade_manual_skip_desc)) },
                        trailingContent = {
                            Switch(
                                checked = crossfadeManualSkip,
                                onCheckedChange = onCrossfadeManualSkipChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (crossfadeManualSkip) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onCrossfadeManualSkipChange(!crossfadeManualSkip) }
                    ))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveSettingGroup(
            title = stringResource(R.string.queue),
            items = listOf(
                Material3SettingsItem(
                    key = "player_persistent_queue",
                    icon = painterResource(R.drawable.queue_music),
                    title = { Text(stringResource(R.string.persistent_queue)) },
                    description = { Text(stringResource(R.string.persistent_queue_desc)) },
                    trailingContent = {
                        Switch(
                            checked = persistentQueue,
                            onCheckedChange = onPersistentQueueChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (persistentQueue) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPersistentQueueChange(!persistentQueue) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.playlist_add),
                    title = { Text(stringResource(R.string.auto_load_more)) },
                    description = { Text(stringResource(R.string.auto_load_more_desc)) },
                    trailingContent = {
                        Switch(
                            checked = autoLoadMore,
                            onCheckedChange = onAutoLoadMoreChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (autoLoadMore) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAutoLoadMoreChange(!autoLoadMore) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.repeat),
                    title = { Text(stringResource(R.string.disable_load_more_when_repeat_all)) },
                    description = { Text(stringResource(R.string.disable_load_more_when_repeat_all_desc)) },
                    trailingContent = {
                        Switch(
                            checked = disableLoadMoreWhenRepeatAll,
                            onCheckedChange = onDisableLoadMoreWhenRepeatAllChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (disableLoadMoreWhenRepeatAll) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onDisableLoadMoreWhenRepeatAllChange(!disableLoadMoreWhenRepeatAll) }
                ),
                Material3SettingsItem(
                    key = "player_auto_download_on_like",
                    icon = painterResource(R.drawable.download),
                    title = { Text(stringResource(R.string.auto_download_on_like)) },
                    description = { Text(stringResource(R.string.auto_download_on_like_desc)) },
                    trailingContent = {
                        Switch(
                            checked = autoDownloadOnLike,
                            onCheckedChange = onAutoDownloadOnLikeChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (autoDownloadOnLike) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAutoDownloadOnLikeChange(!autoDownloadOnLike) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.similar),
                    title = { Text(stringResource(R.string.enable_similar_content)) },
                    description = { Text(stringResource(R.string.similar_content_desc)) },
                    trailingContent = {
                        Switch(
                            checked = similarContentEnabled,
                            onCheckedChange = similarContentEnabledChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (similarContentEnabled) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { similarContentEnabledChange(!similarContentEnabled) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.shuffle),
                    title = { Text(stringResource(R.string.persistent_shuffle_title)) },
                    description = { Text(stringResource(R.string.persistent_shuffle_desc)) },
                    trailingContent = {
                        Switch(
                            checked = persistentShuffleAcrossQueues,
                            onCheckedChange = onPersistentShuffleAcrossQueuesChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (persistentShuffleAcrossQueues) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPersistentShuffleAcrossQueuesChange(!persistentShuffleAcrossQueues) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.shuffle),
                    title = { Text(stringResource(R.string.remember_shuffle_and_repeat)) },
                    description = { Text(stringResource(R.string.remember_shuffle_and_repeat_desc)) },
                    trailingContent = {
                        Switch(
                            checked = rememberShuffleAndRepeat,
                            onCheckedChange = onRememberShuffleAndRepeatChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (rememberShuffleAndRepeat) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onRememberShuffleAndRepeatChange(!rememberShuffleAndRepeat) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.shuffle),
                    title = { Text(stringResource(R.string.shuffle_playlist_first)) },
                    description = { Text(stringResource(R.string.shuffle_playlist_first_desc)) },
                    trailingContent = {
                        Switch(
                            checked = shufflePlaylistFirst,
                            onCheckedChange = onShufflePlaylistFirstChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (shufflePlaylistFirst) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShufflePlaylistFirstChange(!shufflePlaylistFirst) }
                ),
                Material3SettingsItem(
                    key = "player_prevent_duplicates",
                    icon = painterResource(R.drawable.queue_music),
                    title = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue)) },
                    description = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue_desc)) },
                    trailingContent = {
                        Switch(
                            checked = preventDuplicateTracksInQueue,
                            onCheckedChange = onPreventDuplicateTracksInQueueChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (preventDuplicateTracksInQueue) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPreventDuplicateTracksInQueueChange(!preventDuplicateTracksInQueue) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.skip_next),
                    title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
                    description = { Text(stringResource(R.string.auto_skip_next_on_error_desc)) },
                    trailingContent = {
                        Switch(
                            checked = autoSkipNextOnError,
                            onCheckedChange = onAutoSkipNextOnErrorChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (autoSkipNextOnError) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAutoSkipNextOnErrorChange(!autoSkipNextOnError) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.history),
                    title = { Text(stringResource(R.string.history_duration)) },
                    description = {
                        Column {
                            Text(historyDuration.roundToInt().toString())
                            Slider(
                                value = historyDuration,
                                onValueChange = onHistoryDurationChange,
                                valueRange = 1f..100f
                            )
                        }
                    }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveSettingGroup(
            title = stringResource(R.string.misc),
            items = listOf(
                Material3SettingsItem(
                    key = "player_sponsorblock",
                    icon = painterResource(R.drawable.fast_forward),
                    title = { Text(stringResource(R.string.sponsorblock)) },
                    description = { Text(stringResource(R.string.sponsorblock_desc)) },
                    trailingContent = {
                        Text(
                            if (sponsorBlockEnabled) {
                                stringResource(R.string.enabled)
                            } else {
                                stringResource(R.string.disabled)
                            }
                        )
                    },
                    onClick = { navController.navigate("settings/player/sponsorblock") }
                ),
                Material3SettingsItem(
                    key = "player_stop_on_task_clear",
                    icon = painterResource(R.drawable.clear_all),
                    title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
                    trailingContent = {
                        Switch(
                            checked = stopMusicOnTaskClear,
                            onCheckedChange = onStopMusicOnTaskClearChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (stopMusicOnTaskClear) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onStopMusicOnTaskClearChange(!stopMusicOnTaskClear) }
                ),
                Material3SettingsItem(
                    key = "player_pause_on_mute",
                    icon = painterResource(R.drawable.volume_off_pause),
                    title = { Text(stringResource(R.string.pause_music_when_media_is_muted)) },
                    trailingContent = {
                        Switch(
                            checked = pauseOnMute,
                            onCheckedChange = onPauseOnMuteChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (pauseOnMute) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPauseOnMuteChange(!pauseOnMute) }
                ),
                Material3SettingsItem(
                    key = "player_bluetooth_resume",
                    icon = painterResource(R.drawable.bluetooth),
                    title = { Text(stringResource(R.string.resume_on_bluetooth_connect)) },
                    trailingContent = {
                        Switch(
                            checked = resumeOnBluetoothConnect,
                            onCheckedChange = onResumeOnBluetoothConnectChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (resumeOnBluetoothConnect) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onResumeOnBluetoothConnectChange(!resumeOnBluetoothConnect) }
                ),
                Material3SettingsItem(
                    key = "player_keep_screen_on",
                    icon = painterResource(R.drawable.screenshot),
                    title = { Text(stringResource(R.string.keep_screen_on_when_player_is_expanded)) },
                    trailingContent = {
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = onKeepScreenOnChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (keepScreenOn) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onKeepScreenOnChange(!keepScreenOn) }
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.player_and_audio)) },
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
        }
    )
}
