/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import com.music.mkr.R
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.screens.Screens
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.mkrmusic.updater.getUpdateAvailableState
import com.music.mkr.mkrmusic.updater.getAutoUpdateCheckSetting
import com.music.mkr.mkrmusic.updater.checkForUpdate
import com.music.mkr.mkrmusic.updater.saveUpdateAvailableState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val autoUpdateSetting = remember { getAutoUpdateCheckSetting(context) }
    var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }

    DisposableEffect(context) {
        val sharedPrefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "update_available") {
                isUpdateAvailable = getUpdateAvailableState(context)
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        isUpdateAvailable = getUpdateAvailableState(context)
        onDispose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (autoUpdateSetting) {
            checkForUpdate(
                context = context,
                onSuccess = { _, isAvailable, _, _, _, _, _, _ ->
                    saveUpdateAvailableState(context, isAvailable)
                },
                onError = {}
            )
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
        )

        // MKR Music Hero Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("settings/about") },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.mkr_music_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MKR Music",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "v1.0 Stable",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Crafted by Mohammad Kaif Raja",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.navigate_next),
                    contentDescription = "About",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Group 1: Important / Account
        ExpressiveSettingGroup(
            title = "Account & Updates",
            itemMinHeight = 64.dp,
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = painterResource(if (isUpdateAvailable) R.drawable.update_alert else R.drawable.system_update_uptodate),
                        title = { Text(stringResource(R.string.system_update)) },
                        description = {
                            if (isUpdateAvailable) {
                                Text(
                                    text = stringResource(R.string.update_available),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(stringResource(R.string.app_update_uptodate))
                            }
                        },
                        onClick = { navController.navigate("settings/update") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.google),
                        title = { Text(stringResource(R.string.account)) },
                        description = { Text(stringResource(R.string.setting_account_desc)) },
                        onClick = { navController.navigate("settings/account") }
                    )
                )
            }
        )

        // Group 2: Media & Player Experience
        ExpressiveSettingGroup(
            itemMinHeight = 64.dp,
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.appearance)) },
                        description = { Text(stringResource(R.string.setting_appearance_desc)) },
                        onClick = { navController.navigate("settings/appearance") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.earbud_case),
                        title = { Text(stringResource(R.string.player_and_audio)) },
                        description = { Text(stringResource(R.string.setting_player_desc)) },
                        onClick = { navController.navigate("settings/player") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.group),
                        title = { Text(stringResource(R.string.listen_together)) },
                        description = { Text(stringResource(R.string.setting_listen_together_desc)) },
                        onClick = { navController.navigate(Screens.ListenTogether.route) }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.content)) },
                        description = { Text(stringResource(R.string.setting_content_desc)) },
                        onClick = { navController.navigate("settings/content") }
                    )
                )
            }
        )

        // Group 3: Features & Data
        ExpressiveSettingGroup(
            itemMinHeight = 64.dp,
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.translate),
                        title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                        description = { Text(stringResource(R.string.setting_ai_lyrics_translation_desc)) },
                        onClick = { navController.navigate("settings/ai") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.security),
                        title = { Text(stringResource(R.string.privacy)) },
                        description = { Text(stringResource(R.string.setting_privacy_desc)) },
                        onClick = { navController.navigate("settings/privacy") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.history),
                        title = { Text(stringResource(R.string.listening_summary)) },
                        description = { Text(stringResource(R.string.setting_listening_summary_desc)) },
                        onClick = { navController.navigate("settings/listening_summary") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.storage),
                        title = { Text(stringResource(R.string.storage)) },
                        description = { Text(stringResource(R.string.setting_storage_desc)) },
                        onClick = { navController.navigate("settings/storage") }
                    )
                )
            }
        )

        // Group 4: System & Support
        ExpressiveSettingGroup(
            itemMinHeight = 64.dp,
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.restore),
                        title = { Text(stringResource(R.string.backup_restore)) },
                        description = { Text(stringResource(R.string.setting_backup_restore_desc)) },
                        onClick = { navController.navigate("settings/backup_restore") }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.info),
                        title = { Text(stringResource(R.string.about)) },
                        description = { Text(stringResource(R.string.setting_about_desc)) },
                        onClick = { navController.navigate("settings/about") }
                    )
                )
            }
        )
        
        Spacer(modifier = Modifier.height(50.dp))
    }

    TopAppBar(
        title = {
//            Text(stringResource(R.string.settings))
                },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}
