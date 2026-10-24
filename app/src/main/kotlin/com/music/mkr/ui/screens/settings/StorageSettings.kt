/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.DisableSongCacheKey
import com.music.mkr.constants.SongCacheDelaySecondsKey
import com.music.mkr.constants.MaxImageCacheSizeKey
import com.music.mkr.constants.MaxSongCacheSizeKey
import com.music.mkr.constants.CustomDownloadFolderUriKey
import com.music.mkr.constants.CustomDownloadFolderNameKey
import com.music.mkr.constants.CustomDownloadFolderPathKey
import com.music.mkr.extensions.tryOrNull
import com.music.mkr.ui.component.ActionPromptDialog
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.formatFileSize
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encodeUtf8
import timber.log.Timber
import kotlin.math.roundToInt

@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class, DelicateCoilApi::class)
@Composable
fun StorageSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val imageDiskCache = context.imageLoader.diskCache ?: return
    val playerCache = LocalPlayerConnection.current?.service?.playerCache ?: return
    val downloadCache = LocalPlayerConnection.current?.service?.downloadCache ?: return

    val coroutineScope = rememberCoroutineScope()
    val songCacheString = stringResource(R.string.song_cache).lowercase()
    val imageCacheString = stringResource(R.string.image_cache).lowercase()
    val (maxImageCacheSize, onMaxImageCacheSizeChange) = rememberPreference(
        key = MaxImageCacheSizeKey,
        defaultValue = 250
    )
    val (maxSongCacheSize, onMaxSongCacheSizeChange) = rememberPreference(
        key = MaxSongCacheSizeKey,
        defaultValue = 500
    )
    val (disableSongCache, onDisableSongCacheChange) = rememberPreference(
        key = DisableSongCacheKey,
        defaultValue = false
    )
    val (songCacheDelaySeconds, onSongCacheDelaySecondsChange) = rememberPreference(
        key = SongCacheDelaySecondsKey,
        defaultValue = 10
    )
    val (customDownloadFolderUri, onCustomDownloadFolderUriChange) = rememberPreference(
        key = CustomDownloadFolderUriKey,
        defaultValue = ""
    )
    val (customDownloadFolderName, onCustomDownloadFolderNameChange) = rememberPreference(
        key = CustomDownloadFolderNameKey,
        defaultValue = ""
    )
    val (customDownloadFolderPath, onCustomDownloadFolderPathChange) = rememberPreference(
        key = CustomDownloadFolderPathKey,
        defaultValue = ""
    )
    var showResetFolderDialog by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)

                if (customDownloadFolderUri.isNotBlank() && customDownloadFolderUri != treeUri.toString()) {
                    try {
                        context.contentResolver.releasePersistableUriPermission(
                            customDownloadFolderUri.toUri(),
                            takeFlags
                        )
                    } catch (_: Exception) {}
                }

                val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
                val folderName = docId?.substringAfterLast(':')?.ifBlank { null }
                    ?: treeUri.lastPathSegment
                    ?: "Custom Folder"

                val resolvedPath = runCatching {
                    val raw = treeUri.path.orEmpty()
                    if (raw.contains(":")) {
                        val sub = raw.substringAfter(':')
                        if (raw.contains("primary")) {
                            "/storage/emulated/0/$sub"
                        } else {
                            val volume = raw.substringBefore(':').substringAfterLast('/')
                            if (volume.isNotBlank()) "/storage/$volume/$sub" else null
                        }
                    } else null
                }.getOrNull().orEmpty()

                onCustomDownloadFolderUriChange(treeUri.toString())
                onCustomDownloadFolderNameChange(folderName)
                onCustomDownloadFolderPathChange(resolvedPath)

                val sp = context.getSharedPreferences("mkr_storage_prefs", Context.MODE_PRIVATE)
                sp.edit()
                    .putString("custom_download_uri", treeUri.toString())
                    .putString("custom_download_name", folderName)
                    .putString("custom_download_path", resolvedPath)
                    .apply()
            } catch (e: Exception) {
                Timber.w(e, "Failed to persist SAF permission for custom download folder")
            }
        }
    }

    val isCustomFolderActive = remember(customDownloadFolderUri) {
        if (customDownloadFolderUri.isBlank()) {
            false
        } else {
            try {
                val parsed = customDownloadFolderUri.toUri()
                context.contentResolver.persistedUriPermissions.any {
                    it.uri == parsed && (it.isReadPermission || it.isWritePermission)
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    var showCustomSizeDialog by remember { mutableStateOf(false) }

    var clearDownloads by remember { mutableStateOf(false) }
    var clearCacheDialog by remember { mutableStateOf(false) }
    var clearImageCacheDialog by remember { mutableStateOf(false) }
    android.util.Log.d("StorageSettings", "Recomposed: clearImageCacheDialog = $clearImageCacheDialog")

    // State for the confirmation dialog
    var showCacheWarningDialog by remember { mutableStateOf(false) }
    var cacheType by remember { mutableStateOf("") }
    var cacheUsage by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var onConfirmAction by remember { mutableStateOf<() -> Unit>({}) }


    var imageCacheSize by remember {
        androidx.compose.runtime.mutableLongStateOf(imageDiskCache.size)
    }
    var playerCacheSize by remember {
        androidx.compose.runtime.mutableLongStateOf(tryOrNull { playerCache.cacheSpace } ?: 0)
    }
    var downloadCacheSize by remember {
        mutableLongStateOf(tryOrNull { downloadCache.cacheSpace } ?: 0)
    }
    val imageCacheProgress by animateFloatAsState(
        targetValue = (imageCacheSize.toFloat() / (maxImageCacheSize * 1024 * 1024L)).coerceIn(
            0f,
            1f
        ),
        label = "imageCacheProgress",
    )
    val playerCacheProgress by animateFloatAsState(
        targetValue = (playerCacheSize.toFloat() / (maxSongCacheSize * 1024 * 1024L)).coerceIn(
            0f,
            1f
        ),
        label = "playerCacheProgress",
    )

    LaunchedEffect(disableSongCache) {
        if (disableSongCache) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    playerCache.keys.forEach { key ->
                        playerCache.removeResource(key)
                    }
                    val nonDownloaded = database.downloadedSongsByNameAsc().first().filter { !it.song.isDownloaded }
                    nonDownloaded.forEach { song ->
                        database.query { update(song.song.copy(dateDownload = null)) }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(maxImageCacheSize) {
        SingletonImageLoader.reset()
        if (maxImageCacheSize == 0) {
            coroutineScope.launch(Dispatchers.IO) {
                imageDiskCache.clear()
            }
        }
    }
    LaunchedEffect(maxSongCacheSize) {
        if (maxSongCacheSize == 0) {
            coroutineScope.launch(Dispatchers.IO) {
                playerCache.keys.forEach { key ->
                    playerCache.removeResource(key)
                }
            }
        }
    }



    LaunchedEffect(imageDiskCache) {
        while (isActive) {
            delay(2000)
            imageCacheSize = imageDiskCache.size
        }
    }
    LaunchedEffect(playerCache) {
        while (isActive) {
            delay(2000)
            playerCacheSize = tryOrNull { playerCache.cacheSpace } ?: 0
        }
    }
    LaunchedEffect(downloadCache) {
        while (isActive) {
            delay(2000)
            downloadCacheSize = tryOrNull { downloadCache.cacheSpace } ?: 0
        }
    }

    if (clearDownloads) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_all_downloads),
            onDismiss = { clearDownloads = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    downloadCache.keys.forEach { key ->
                        downloadCache.removeResource(key)
                    }
                }
                clearDownloads = false
            },
            onCancel = { clearDownloads = false },
            content = {
                Text(text = stringResource(R.string.clear_downloads_dialog))
            }
        )
    }
    if (showResetFolderDialog) {
        ActionPromptDialog(
            title = "Reset Download Location",
            onDismiss = { showResetFolderDialog = false },
            onConfirm = {
                try {
                    if (customDownloadFolderUri.isNotBlank()) {
                        context.contentResolver.releasePersistableUriPermission(
                            customDownloadFolderUri.toUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                } catch (_: Exception) {}
                onCustomDownloadFolderUriChange("")
                onCustomDownloadFolderNameChange("")
                onCustomDownloadFolderPathChange("")
                val sp = context.getSharedPreferences("mkr_storage_prefs", Context.MODE_PRIVATE)
                sp.edit()
                    .remove("custom_download_uri")
                    .remove("custom_download_name")
                    .remove("custom_download_path")
                    .apply()
                showResetFolderDialog = false
            },
            onCancel = { showResetFolderDialog = false },
            content = {
                Text(text = "Are you sure you want to reset the download location to the default internal app storage?")
            }
        )
    }
    if (clearCacheDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_song_cache),
            onDismiss = { clearCacheDialog = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    playerCache.keys.forEach { key ->
                        playerCache.removeResource(key)
                    }
                }
                clearCacheDialog = false
            },
            onCancel = { clearCacheDialog = false },
            content = {
                Text(text = stringResource(R.string.clear_song_cache_dialog))
            }
        )
    }
    if (clearImageCacheDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_image_cache),
            onDismiss = { clearImageCacheDialog = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    val urlsToPreserve = mutableSetOf<String>()
                    val downloadedSongs = try {
                        database.downloadedSongsByNameAsc().first()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    downloadedSongs.forEach { song ->
                        song.song.thumbnailUrl?.let { urlsToPreserve.add(it.encodeUtf8().sha256().hex()) }
                        song.album?.thumbnailUrl?.let { urlsToPreserve.add(it.encodeUtf8().sha256().hex()) }
                    }
                    val directory = imageDiskCache.directory.toFile()
                    if (directory.exists() && directory.isDirectory) {
                        directory.listFiles()?.forEach { file ->
                            if (file.isFile && !file.name.startsWith("journal")) {
                                val isPreserved = urlsToPreserve.any { hash -> file.name.startsWith(hash) }
                                if (!isPreserved) {
                                    file.delete()
                                }
                            }
                        }
                    }
                    imageDiskCache.clear()
                }
                clearImageCacheDialog = false
            },
            onCancel = { clearImageCacheDialog = false },
            content = {
                Text(text = stringResource(R.string.clear_image_cache_dialog))
            }
        )
    }

    // Confirmation Dialog
    if (showCacheWarningDialog) {
        AlertDialog(
            onDismissRequest = { showCacheWarningDialog = false },
            title = { Text(stringResource(R.string.cache_size_warning_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.cache_size_warning_message,
                        formatFileSize(cacheUsage),
                        cacheType
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmAction()
                        showCacheWarningDialog = false
                    }
                ) {
                    Text(
                        stringResource(R.string.cache_size_warning_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCacheWarningDialog = false }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
    }

    if (showCustomSizeDialog) {
        var tempInput by remember { mutableStateOf(if (maxSongCacheSize > 0) maxSongCacheSize.toString() else "500") }
        AlertDialog(
            onDismissRequest = { showCustomSizeDialog = false },
            title = { Text("Custom Cache Size Limit") },
            text = {
                Column {
                    Text("Enter maximum cache size in megabytes (1 - 10000 MB):")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempInput = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        suffix = { Text("MB") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sizeMb = tempInput.toIntOrNull() ?: 500
                        val clamped = sizeMb.coerceIn(1, 10000)
                        val newLimitInBytes = clamped * 1024 * 1024L
                        if (newLimitInBytes < playerCacheSize) {
                            cacheUsage = playerCacheSize
                            cacheType = songCacheString
                            onConfirmAction = { onMaxSongCacheSizeChange(clamped) }
                            showCacheWarningDialog = true
                        } else {
                            onMaxSongCacheSizeChange(clamped)
                        }
                        showCustomSizeDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSizeDialog = false }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
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
        ExpressiveSettingGroup(
            title = stringResource(R.string.storage),
            items = listOf(
                Material3SettingsItem(
                    key = "storage_downloads",
                    icon = painterResource(R.drawable.storage),
                    title = { Text(stringResource(R.string.downloaded_songs)) },
                    trailingContent = {
                        Text(text = formatFileSize(downloadCacheSize))
                    }
                ),
                Material3SettingsItem(
                    key = "storage_download_location",
                    icon = painterResource(R.drawable.snippet_folder),
                    title = { Text("Download Location") },
                    description = {
                        Text(
                            text = if (isCustomFolderActive) {
                                customDownloadFolderName.ifBlank { "Custom Directory" }
                            } else if (customDownloadFolderUri.isNotBlank()) {
                                "Permission revoked (Using internal storage fallback)"
                            } else {
                                "Default: App Internal Storage"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isCustomFolderActive && customDownloadFolderUri.isNotBlank()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    trailingContent = if (customDownloadFolderUri.isNotBlank()) {
                        {
                            IconButton(
                                onClick = { showResetFolderDialog = true },
                                onLongClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = "Reset to default location",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    onClick = {
                        folderPickerLauncher.launch(null)
                    }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.clear_all),
                    title = { Text(stringResource(R.string.clear_all_downloads)) },
                    onClick = {
                        clearDownloads = true
                    }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.song_cache),
            items = listOf(
                Material3SettingsItem(
                    key = "storage_disable_song_cache",
                    icon = painterResource(R.drawable.cached),
                    title = { Text("Disable Song Caching") },
                    description = {
                        Text(
                            if (disableSongCache)
                                "Caching is disabled. Songs stream directly without saving to offline storage."
                            else
                                "Streamed songs are cached to disk for offline listening"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = disableSongCache,
                            onCheckedChange = { checked ->
                                onDisableSongCacheChange(checked)
                                if (checked) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        playerCache.keys.forEach { key ->
                                            playerCache.removeResource(key)
                                        }
                                        try {
                                            val nonDownloaded = database.downloadedSongsByNameAsc().first().filter { !it.song.isDownloaded }
                                            nonDownloaded.forEach { song ->
                                                database.query { update(song.song.copy(dateDownload = null)) }
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (disableSongCache) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        val newState = !disableSongCache
                        onDisableSongCacheChange(newState)
                        if (newState) {
                            coroutineScope.launch(Dispatchers.IO) {
                                playerCache.keys.forEach { key ->
                                    playerCache.removeResource(key)
                                }
                                try {
                                    val nonDownloaded = database.downloadedSongsByNameAsc().first().filter { !it.song.isDownloaded }
                                    nonDownloaded.forEach { song ->
                                        database.query { update(song.song.copy(dateDownload = null)) }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                ),
                Material3SettingsItem(
                    key = "storage_cache_delay",
                    enabled = !disableSongCache,
                    icon = painterResource(R.drawable.timer),
                    title = { Text("Cache After Listening") },
                    description = {
                        if (disableSongCache) {
                            Text(
                                text = "Inactive (Song caching is disabled)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        } else {
                            Column {
                                val delaySec = songCacheDelaySeconds.coerceIn(5, 90)
                                val formattedDelay = when {
                                    delaySec == 10 -> "10 seconds (Default - Instant Start)"
                                    delaySec < 60 -> "$delaySec seconds"
                                    delaySec == 60 -> "1 minute"
                                    delaySec == 90 -> "1.5 minutes (Max)"
                                    else -> "1m ${delaySec - 60}s"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formattedDelay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = delaySec.toFloat(),
                                    onValueChange = { onSongCacheDelaySecondsChange(it.roundToInt()) },
                                    valueRange = 5f..90f,
                                    steps = 16
                                )
                                Text(
                                    text = "Plays songs instantly without buffering lag. Caching begins in the background only after listening for the selected time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                ),
                Material3SettingsItem(
                    key = "storage_cache_limit",
                    enabled = !disableSongCache,
                    icon = painterResource(R.drawable.cached),
                    title = { Text(stringResource(R.string.max_song_cache_size)) },
                    description = {
                        if (disableSongCache) {
                            Text(
                                text = "Inactive (Song caching is disabled)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        } else {
                            val songCacheValues =
                                remember { listOf(0, 50, 100, 200, 500, 1000, 2048, 4096, -1) }
                            val isPreset = maxSongCacheSize in songCacheValues
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            maxSongCacheSize == 0 -> stringResource(R.string.disable)
                                            maxSongCacheSize == -1 -> stringResource(R.string.unlimited)
                                            !isPreset -> "Custom: ${formatFileSize(maxSongCacheSize * 1024 * 1024L)}"
                                            else -> formatFileSize(maxSongCacheSize * 1024 * 1024L)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    TextButton(onClick = { showCustomSizeDialog = true }) {
                                        Text("Custom")
                                    }
                                }
                                val sliderIndex = if (isPreset) {
                                    songCacheValues.indexOf(maxSongCacheSize).toFloat()
                                } else {
                                    songCacheValues.indexOf(500).toFloat()
                                }
                                Slider(
                                    value = sliderIndex,
                                    onValueChange = {
                                        val newValue = songCacheValues[it.roundToInt()]
                                        val newLimitInBytes = if (newValue == -1) {
                                            Long.MAX_VALUE
                                        } else {
                                            newValue * 1024 * 1024L
                                        }

                                        if (newLimitInBytes < playerCacheSize) {
                                            cacheUsage = playerCacheSize
                                            cacheType = songCacheString
                                            onConfirmAction = { onMaxSongCacheSizeChange(newValue) }
                                            showCacheWarningDialog = true
                                        } else {
                                            onMaxSongCacheSizeChange(newValue)
                                        }
                                    },
                                    steps = songCacheValues.size - 2,
                                    valueRange = 0f..(songCacheValues.size - 1).toFloat()
                                )
                                LinearProgressIndicator(
                                    progress = { playerCacheProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    strokeCap = StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text(
                                    text = if (maxSongCacheSize == -1) {
                                        formatFileSize(playerCacheSize)
                                    } else {
                                        "${formatFileSize(playerCacheSize)} / ${
                                            formatFileSize(
                                                maxSongCacheSize * 1024 * 1024L
                                            )
                                        }"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                ),
                Material3SettingsItem(
                    key = "storage_clear_song_cache",
                    icon = painterResource(R.drawable.clear_all),
                    title = { Text(stringResource(R.string.clear_song_cache)) },
                    description = if (disableSongCache) { { Text("Cache is currently disabled") } } else null,
                    onClick = {
                        clearCacheDialog = true
                    }
                )
            )
        )

        ExpressiveSettingGroup(
            title = stringResource(R.string.image_cache),
            items = listOf(
                Material3SettingsItem(
                    key = "storage_max_image_cache",
                    icon = painterResource(R.drawable.manage_search),
                    title = { Text(stringResource(R.string.max_image_cache_size)) },
                    description = {
                        val imageCacheValues =
                            remember { listOf(0, 50, 100, 250, 500, 1024, 2048, 4096) }
                        Column {
                            Text(
                                text = when (maxImageCacheSize) {
                                    0 -> stringResource(R.string.disable)
                                    else -> formatFileSize(maxImageCacheSize * 1024 * 1024L)
                                }
                            )
                            val sliderIndex = if (imageCacheValues.contains(maxImageCacheSize)) {
                                imageCacheValues.indexOf(maxImageCacheSize).toFloat()
                            } else {
                                imageCacheValues.indexOf(250).toFloat().coerceAtLeast(0f)
                            }
                            Slider(
                                value = sliderIndex,
                                onValueChange = {
                                    val newValue = imageCacheValues[it.roundToInt()]
                                    val newLimitInBytes = newValue * 1024 * 1024L

                                    if (newLimitInBytes < imageCacheSize) {
                                        cacheUsage = imageCacheSize
                                        cacheType = imageCacheString
                                        onConfirmAction = { onMaxImageCacheSizeChange(newValue) }
                                        showCacheWarningDialog = true
                                    } else {
                                        onMaxImageCacheSizeChange(newValue)
                                    }
                                },
                                steps = imageCacheValues.size - 2,
                                valueRange = 0f..(imageCacheValues.size - 1).toFloat()
                            )
                            LinearProgressIndicator(
                                progress = { imageCacheProgress },
                                modifier = Modifier.fillMaxWidth(),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "${formatFileSize(imageCacheSize)} / ${
                                    formatFileSize(
                                        maxImageCacheSize * 1024 * 1024L
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                ),
                Material3SettingsItem(
                    key = "storage_clear_image_cache",
                    icon = painterResource(R.drawable.clear_all),
                    title = { Text(stringResource(R.string.clear_image_cache)) },
                    onClick = {
                        android.util.Log.d("StorageSettings", "Clear image cache button clicked!")
                        clearImageCacheDialog = true
                    }
                )
            )
        )
        Spacer(Modifier.padding(bottom = 30.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.storage)) },
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
