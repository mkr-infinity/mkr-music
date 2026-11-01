/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings.integrations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.R
import com.music.mkr.constants.AccountNameKey
import com.music.mkr.constants.InnerTubeCookieKey
import com.music.mkr.constants.SpotifySessionKey
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { innerTubeCookie.isNotEmpty() }
    val accountName by rememberPreference(AccountNameKey, "")

    val spotifySession by rememberPreference(SpotifySessionKey, "")
    val isSpotifyConnected = remember(spotifySession) { spotifySession.isNotEmpty() }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        ExpressiveSettingGroup(
            title = stringResource(R.string.general),
            items = listOf(
                Material3SettingsItem(
                    key = "integrations_discord",
                    icon = painterResource(R.drawable.discord),
                    title = { Text(stringResource(R.string.discord_integration)) },
                    description = { Text("Display live playing track and album art on Discord") },
                    onClick = {
                        navController.navigate("settings/integrations/discord")
                    }
                ),
                Material3SettingsItem(
                    key = "integrations_lastfm",
                    icon = painterResource(R.drawable.music_note),
                    title = { Text(stringResource(R.string.lastfm_integration)) },
                    description = { Text("Scrobble played tracks to your Last.fm profile") },
                    onClick = {
                        navController.navigate("settings/integrations/lastfm")
                    }
                ),
                Material3SettingsItem(
                    key = "integrations_listen_together",
                    icon = painterResource(R.drawable.group),
                    title = { Text(stringResource(R.string.together)) },
                    description = { Text("Synchronized live group listening sessions with friends") },
                    onClick = {
                        navController.navigate("listen_together")
                    }
                ),
                Material3SettingsItem(
                    key = "integrations_spotify",
                    icon = painterResource(R.drawable.spotify),
                    title = { Text(stringResource(R.string.spotify)) },
                    description = {
                        Text(
                            if (isSpotifyConnected) "Connected • Manage Spotify sync & playlists"
                            else "Sign in to import playlists & library"
                        )
                    },
                    onClick = {
                        navController.navigate("settings/spotify")
                    }
                ),
                Material3SettingsItem(
                    key = "integrations_youtube_music",
                    icon = painterResource(R.drawable.google),
                    title = { Text(stringResource(R.string.youtube_music)) },
                    description = {
                        Text(
                            if (isLoggedIn) (accountName.ifEmpty { "Connected • Sync library & recommendations" })
                            else "Sign in to sync your YouTube Music library"
                        )
                    },
                    onClick = {
                        if (isLoggedIn) {
                            navController.navigate("settings/account")
                        } else {
                            navController.navigate("login")
                        }
                    }
                )
            )
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.integrations)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.back),
                )
            }
        }
    )
}
