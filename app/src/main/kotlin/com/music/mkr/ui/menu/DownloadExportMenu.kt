/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.menu

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.music.mkr.LocalDownloadUtil
import com.music.mkr.R
import com.music.mkr.ui.component.Material3MenuItemData

@Composable
internal fun downloadExportRetryItem(songId: String, offlineReady: Boolean): Material3MenuItemData? {
    if (!offlineReady) return null
    val downloader = LocalDownloadUtil.current
    val failed by remember(downloader, songId) { downloader.hasExportFailure(songId) }
        .collectAsState(initial = false)
    if (!failed) return null
    return Material3MenuItemData(
        title = { Text(stringResource(R.string.download_export_retry)) },
        description = { Text(stringResource(R.string.download_export_failed)) },
        onClick = { downloader.retryExport(songId) },
    )
}
