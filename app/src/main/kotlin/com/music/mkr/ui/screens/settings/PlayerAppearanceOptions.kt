/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.music.mkr.R
import com.music.mkr.constants.MiniPlayerBackgroundStyleKey
import com.music.mkr.constants.MiniPlayerDesignOption
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.miniPlayerBackgroundStyles
import com.music.mkr.constants.resolveFullPlayerBackground
import com.music.mkr.constants.resolveMiniPlayerBackground
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.constants.PlayerStyleKey
import com.music.mkr.constants.PureBlackMiniPlayerKey
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import com.music.mkr.ui.component.EnumDialog
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.component.PlayerStyleSelectionDialog
import com.music.mkr.utils.rememberPreference

@Composable
internal fun fullPlayerAppearanceItems(): List<Material3SettingsItem> {
    val (playerStyleName, setPlayerStyleName) = rememberPreference(PlayerStyleKey, PlayerStyle.DEFAULT.name)
    val playerStyle = remember(playerStyleName) { PlayerStyle.fromString(playerStyleName) }
    val (savedBackground, onPlayerBackgroundChange) = rememberPreference(PlayerBackgroundStyleKey, "")
    val playerBackground = resolveFullPlayerBackground(savedBackground, playerStyle)

    var showPlayerDesignDialog by remember { mutableStateOf(false) }
    var showPlayerBackgroundDialog by remember { mutableStateOf(false) }

    if (showPlayerDesignDialog) {
        PlayerStyleSelectionDialog(
            currentStyle = playerStyle,
            onDismiss = { showPlayerDesignDialog = false },
            onSelect = { selectedStyle ->
                setPlayerStyleName(selectedStyle.name)
                showPlayerDesignDialog = false
            }
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = {
                onPlayerBackgroundChange(it.name)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = PlayerBackgroundStyle.entries,
            valueText = { stringResource(it.labelRes()) }
        )
    }

    return listOf(
        Material3SettingsItem(
            key = "appearance_player_style",
            icon = painterResource(R.drawable.palette),
            title = { Text(stringResource(R.string.player_style)) },
            description = { Text(stringResource(playerStyle.titleRes)) },
            onClick = { showPlayerDesignDialog = true }
        ),
        Material3SettingsItem(
            key = "appearance_player_background",
            icon = painterResource(R.drawable.gradient),
            title = { Text(stringResource(R.string.player_background_style)) },
            description = {
                Text(stringResource(playerBackground.labelRes()))
            },
            onClick = { showPlayerBackgroundDialog = true }
        )
    )
}

@Composable
internal fun MiniPlayerAppearanceSettings() {
    val (useAppleMiniPlayer, onUseAppleMiniPlayerChange) = rememberPreference(
        UseAppleMiniPlayerKey,
        defaultValue = MiniPlayerDesignOption.DefaultUseAppleMiniPlayer
    )
    val (useNewMiniPlayerDesign, onUseNewMiniPlayerDesignChange) = rememberPreference(
        UseNewMiniPlayerDesignKey,
        defaultValue = MiniPlayerDesignOption.DefaultUseNewMiniPlayerDesign
    )
    val (pureBlackMiniPlayer, onPureBlackMiniPlayerChange) = rememberPreference(
        PureBlackMiniPlayerKey,
        defaultValue = false
    )
    val (savedMiniPlayerBackground, onMiniPlayerBackgroundChange) = rememberPreference(MiniPlayerBackgroundStyleKey, "")
    val miniPlayerBackground = resolveMiniPlayerBackground(savedMiniPlayerBackground)

    var showMiniPlayerDesignDialog by remember { mutableStateOf(false) }
    var showMiniPlayerBackgroundDialog by remember { mutableStateOf(false) }

    val currentMiniPlayerOption = MiniPlayerDesignOption.fromPreferences(
        useAppleMiniPlayer = useAppleMiniPlayer,
        useNewMiniPlayerDesign = useNewMiniPlayerDesign,
    )

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
            values = MiniPlayerDesignOption.entries,
            valueText = { it.settingLabel() }
        )
    }

    if (showMiniPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showMiniPlayerBackgroundDialog = false },
            onSelect = {
                onMiniPlayerBackgroundChange(it.name)
                showMiniPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.miniplayer_background_style),
            current = miniPlayerBackground,
            values = miniPlayerBackgroundStyles,
            valueText = { stringResource(it.labelRes()) }
        )
    }

    ExpressiveSettingGroup(
        title = stringResource(R.string.mini_player),
        items = listOf(
            Material3SettingsItem(
                key = "appearance_apple_player",
                icon = painterResource(R.drawable.nav_bar),
                title = { Text(stringResource(R.string.mini_player)) },
                description = {
                    Text(currentMiniPlayerOption.settingLabel())
                },
                onClick = { showMiniPlayerDesignDialog = true }
            ),
            Material3SettingsItem(
                key = "appearance_pure_black_mini_player",
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
                key = "appearance_mini_player_background",
                icon = painterResource(R.drawable.palette),
                title = { Text(stringResource(R.string.miniplayer_background_style)) },
                description = {
                    if (pureBlackMiniPlayer) {
                        Text("${stringResource(miniPlayerBackground.labelRes())} (${stringResource(R.string.pure_black_mini_player)})")
                    } else {
                        Text(stringResource(miniPlayerBackground.labelRes()))
                    }
                },
                enabled = !pureBlackMiniPlayer,
                onClick = { if (!pureBlackMiniPlayer) showMiniPlayerBackgroundDialog = true }
            ),
        )
    )
}


@StringRes
private fun PlayerBackgroundStyle.labelRes(): Int = when (this) {
    PlayerBackgroundStyle.DEFAULT -> R.string.follow_theme
    PlayerBackgroundStyle.GRADIENT -> R.string.gradient
    PlayerBackgroundStyle.GLOW_ANIMATED -> R.string.glow_animated
    PlayerBackgroundStyle.LIVE_MESH -> R.string.live_mesh
    PlayerBackgroundStyle.IMMERSIVE_ARTWORK -> R.string.immersive_artwork_background
}

private fun MiniPlayerDesignOption.settingLabel(): String = when (this) {
    MiniPlayerDesignOption.CLASSIC -> "Classic"
    MiniPlayerDesignOption.NEW -> "Expressive"
    MiniPlayerDesignOption.APPLE -> "Capsule"
}
