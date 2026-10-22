/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.mkrmusic.component

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.mkr.R
import com.music.mkr.mkrmusic.updater.ReleaseMetadata
import com.music.mkr.mkrmusic.updater.ReleasePreviewState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePreviewDialog(
    state: ReleasePreviewState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                when (state) {
                    is ReleasePreviewState.Checking -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Checking for updates...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    is ReleasePreviewState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.info),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Update Check Failed",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.semantics { heading() }
                                    )
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.semantics { role = Role.Button }
                                ) {
                                    Text(stringResource(id = android.R.string.ok))
                                }
                            }
                        }
                    }

                    is ReleasePreviewState.UpToDate -> {
                        UpdateContent(
                            release = state.release,
                            isUpdateAvailable = false,
                            installedVersionName = state.installedVersionName,
                            installedVersionCode = state.installedVersionCode,
                            isVersionCodeUnverified = state.isVersionCodeUnverified,
                            onDownloadClick = { url, name ->
                                triggerApkDownload(context, url, name)
                            },
                            onViewReleaseClick = {
                                try {
                                    uriHandler.openUri(state.release.htmlUrl)
                                } catch (_: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.release.htmlUrl))
                                    context.startActivity(intent)
                                }
                            },
                            onDismiss = onDismiss
                        )
                    }

                    is ReleasePreviewState.UpdateAvailable -> {
                        UpdateContent(
                            release = state.release,
                            isUpdateAvailable = true,
                            installedVersionName = state.installedVersionName,
                            installedVersionCode = state.installedVersionCode,
                            isVersionCodeUnverified = false,
                            onDownloadClick = { url, name ->
                                triggerApkDownload(context, url, name)
                            },
                            onViewReleaseClick = {
                                try {
                                    uriHandler.openUri(state.release.htmlUrl)
                                } catch (_: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.release.htmlUrl))
                                    context.startActivity(intent)
                                }
                            },
                            onDismiss = onDismiss
                        )
                    }

                    is ReleasePreviewState.Idle -> {
                        // Empty or dismissed
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateContent(
    release: ReleaseMetadata,
    isUpdateAvailable: Boolean,
    installedVersionName: String,
    installedVersionCode: Long,
    isVersionCodeUnverified: Boolean,
    onDownloadClick: (url: String, name: String) -> Unit,
    onViewReleaseClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isUpdateAvailable) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isUpdateAvailable) R.drawable.mobile_update
                        else R.drawable.system_update_uptodate
                    ),
                    contentDescription = null,
                    tint = if (isUpdateAvailable) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUpdateAvailable) stringResource(R.string.update_available)
                    else stringResource(R.string.app_up_to_date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = if (isUpdateAvailable) {
                        "${release.tagName} (Build ${release.remoteVersionCode ?: "?"})"
                    } else {
                        "Installed: v$installedVersionName (Build $installedVersionCode)"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Release Metadata Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = release.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                release.publishedAt?.let { dateStr ->
                    Text(
                        text = "Published on $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (release.apkSizeBytes > 0) {
                    val sizeMb = String.format(java.util.Locale.US, "%.1f MB", release.apkSizeBytes / (1024.0 * 1024.0))
                    Text(
                        text = "Universal APK: $sizeMb (${release.apkName ?: "mkr-music-universal.apk"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isVersionCodeUnverified) {
                    Text(
                        text = stringResource(R.string.version_code_unverified),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Release Description (Safe Markdown Preview)
        val descriptionText = release.description.trim()
        if (descriptionText.isNotBlank()) {
            val maxLen = 1400
            val isTruncated = descriptionText.length > maxLen
            val previewText = if (isTruncated) descriptionText.take(maxLen) else descriptionText

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Release Notes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                RenderSafeMarkdown(
                    markdown = previewText,
                    onUrlClick = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (e: Exception) {
                            Timber.w(e, "Cannot open url: $url")
                        }
                    }
                )

                if (isTruncated) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.read_more_github),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onViewReleaseClick() }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Exactly two primary actions: Button 1 = Download APK, Button 2 = View Release
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val hasDirectApk = !release.apkDownloadUrl.isNullOrBlank()

            // Button 1: Download APK
            Button(
                onClick = {
                    release.apkDownloadUrl?.let { url ->
                        onDownloadClick(url, release.apkName ?: "mkr-music-universal.apk")
                    }
                },
                enabled = hasDirectApk,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasDirectApk) stringResource(R.string.download_apk)
                    else stringResource(R.string.no_direct_apk),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Button 2: View Release
            OutlinedButton(
                onClick = onViewReleaseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { role = Role.Button },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(
                    painter = painterResource(R.drawable.open_in_new_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.view_release),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Dismiss action
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .semantics { role = Role.Button }
            ) {
                Text(
                    text = stringResource(R.string.later),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Safely parses markdown lines:
 * - Strips raw HTML tags
 * - Converts headers (#) into styled text
 * - Converts bullet points (-, *, •) into cleanly indented bullet rows
 * - Converts blockquotes (>) into container callouts
 */
@Composable
private fun RenderSafeMarkdown(
    markdown: String,
    onUrlClick: (String) -> Unit
) {
    val lines = remember(markdown) {
        markdown.lines()
            .map { it.trim() }
            .filter { line ->
                // Strip HTML tags like <p>, <img>, <div>
                !line.startsWith("<") && !line.startsWith("</")
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (rawLine in lines) {
            val line = rawLine
                .replace(Regex("<[^>]*>"), "") // strip any inline html
                .trim()

            if (line.isBlank() || line.startsWith("---")) continue

            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                line.startsWith("> ") -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = line.removePrefix("> ").trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                    val content = line.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(14.dp)
                        )
                        Text(
                            text = content.replace("**", ""), // clean markdown bold markers
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                else -> {
                    Text(
                        text = line.replace("**", ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Triggers APK download using Android's DownloadManager, opening the download stream cleanly.
 */
private fun triggerApkDownload(context: Context, downloadUrl: String, filename: String) {
    try {
        val uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri)
            .setTitle("MKR Music Update ($filename)")
            .setDescription("Downloading latest release APK")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setMimeType("application/vnd.android.package-archive")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    } catch (_: Exception) {
        // Fallback to standard browser intent
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    }
}

/**
 * Material 3 Expressive Update Banner Card displayed on the Home feed when an update is available.
 */
@Composable
fun UpdateAvailableHomeCard(
    release: ReleaseMetadata,
    onPreviewClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.mobile_update),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Update Available: ${release.tagName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to preview release notes & download",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onPreviewClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .height(36.dp)
                    .semantics { role = Role.Button }
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
