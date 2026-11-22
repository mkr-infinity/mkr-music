/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import com.music.mkr.R
import com.music.mkr.constants.MiniPlayerArtworkTransitionKey
import com.music.mkr.constants.MiniPlayerBackgroundStyleKey
import com.music.mkr.constants.MiniPlayerCapsuleMorphKey
import com.music.mkr.constants.MiniPlayerDesignOption
import com.music.mkr.constants.MiniPlayerMicroEqualizerKey
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.PlayerBackgroundStyleKey
import com.music.mkr.constants.PlayerStyle
import com.music.mkr.constants.PlayerStyleKey
import com.music.mkr.constants.PureBlackMiniPlayerKey
import com.music.mkr.constants.UseAppleMiniPlayerKey
import com.music.mkr.constants.UseNewMiniPlayerDesignKey
import com.music.mkr.ui.component.EnumDialog
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.component.PlayerStyleSelectionDialog
import com.music.mkr.ui.player.DefaultMiniPlayerMotionEnabled
import com.music.mkr.utils.rememberEnumPreference
import com.music.mkr.utils.rememberPreference

@Composable
internal fun fullPlayerAppearanceItems(): List<Material3SettingsItem> {
    val (playerStyleName, setPlayerStyleName) = rememberPreference(PlayerStyleKey, PlayerStyle.DEFAULT.name)
    val playerStyle = remember(playerStyleName) { PlayerStyle.fromString(playerStyleName) }
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(
        PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )

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
                onPlayerBackgroundChange(it)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = PlayerBackgroundStyle.entries,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                }
            }
        )
    }

    return listOf(
        Material3SettingsItem(
            key = "appearance_player_style",
            icon = painterResource(R.drawable.palette),
            title = { Text(stringResource(R.string.player_style)) },
            description = { Text(stringResource(R.string.player_style_picker_description)) },
            trailingContent = {
                Text(stringResource(playerStyle.titleRes))
            },
            onClick = { showPlayerDesignDialog = true }
        ),
        Material3SettingsItem(
            key = "appearance_player_background",
            icon = painterResource(R.drawable.gradient),
            title = { Text(stringResource(R.string.player_background_style)) },
            description = { Text(stringResource(R.string.player_background_style_description)) },
            trailingContent = {
                Text(
                    when (playerBackground) {
                        PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                        PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                        PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                        PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                        PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                    }
                )
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
    val (miniPlayerBackground, onMiniPlayerBackgroundChange) = rememberEnumPreference(
        MiniPlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )

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
            valueText = {
                when (it) {
                    MiniPlayerDesignOption.CLASSIC -> "Classic mini player"
                    MiniPlayerDesignOption.NEW -> "Expressive mini player"
                    MiniPlayerDesignOption.APPLE -> "Apple-style capsule"
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
            ),
            miniPlayerMotionItem(
                MiniPlayerMicroEqualizerKey,
                "appearance_mini_player_micro_equalizer",
                R.string.mini_player_micro_equalizer,
                R.string.mini_player_micro_equalizer_desc,
                R.drawable.graphic_eq,
            ),
            miniPlayerMotionItem(
                MiniPlayerArtworkTransitionKey,
                "appearance_mini_player_artwork_transition",
                R.string.mini_player_artwork_transition,
                R.string.mini_player_artwork_transition_desc,
                R.drawable.album,
            ),
            miniPlayerMotionItem(
                MiniPlayerCapsuleMorphKey,
                "appearance_mini_player_capsule_morph",
                R.string.mini_player_capsule_morph,
                R.string.mini_player_capsule_morph_desc,
                R.drawable.nav_bar,
            ),
        )
    )
}

@Composable
private fun miniPlayerMotionItem(
    preference: Preferences.Key<Boolean>,
    itemKey: String,
    @StringRes title: Int,
    @StringRes description: Int,
    @DrawableRes icon: Int,
): Material3SettingsItem {
    val (enabled, setEnabled) = rememberPreference(preference, DefaultMiniPlayerMotionEnabled)
    return Material3SettingsItem(
        key = itemKey,
        icon = painterResource(icon),
        title = { Text(stringResource(title)) },
        description = { Text(stringResource(description)) },
        trailingContent = { Switch(checked = enabled, onCheckedChange = setEnabled) },
        onClick = { setEnabled(!enabled) },
    )
}
