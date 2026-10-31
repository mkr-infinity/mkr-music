/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.music.mkr.BuildConfig
import com.music.mkr.R
import com.music.mkr.github.GitHubConfig
import com.music.mkr.ui.theme.mkrMusicTheme
import com.music.mkr.utils.CrashHandler
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val crashLog = intent.getStringExtra(CrashHandler.EXTRA_CRASH_LOG) ?: getString(R.string.crash_no_log)
        
        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()
            MaterialTheme(colorScheme = colorScheme) {
                CrashScreen(
                    crashLog = crashLog,
                    onClose = { finishAffinity() },
                    onShare = { shareCrashLog(crashLog) },
                    onCopy = { copyToClipboard(crashLog) },
                    onSubmitGitHub = { reportOnGitHub(crashLog) }
                )
            }
        }
    }

    private fun copyToClipboard(crashLog: String) {
        val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("CrashLog", crashLog)
        cm.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
    
    private fun shareCrashLog(crashLog: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "mkr_music_crash_$timestamp.txt"
            val crashFile = File(cacheDir, fileName)
            crashFile.writeText(crashLog)
            
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.FileProvider",
                crashFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MKR Music Crash Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, getString(R.string.crash_share_title)))
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, crashLog)
                putExtra(Intent.EXTRA_SUBJECT, "MKR Music Crash Report")
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.crash_share_title)))
        }
    }

    private fun reportOnGitHub(crashLog: String) {
        try {
            val title = URLEncoder.encode("[Crash Report] on ${Build.MANUFACTURER} ${Build.MODEL}", "UTF-8")
            val truncatedLog = if (crashLog.length > 2500) crashLog.take(2500) + "\n...[truncated]" else crashLog
            val bodyContent = buildString {
                appendLine("### Issue Summary")
                appendLine("An unexpected crash occurred in MKR Music. Diagnostic info is attached below:")
                appendLine()
                appendLine("### Automatically Detected Device Info")
                appendLine("- **App Version**: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("- **Device Manufacturer**: ${Build.MANUFACTURER}")
                appendLine("- **Device Model**: ${Build.MODEL}")
                appendLine("- **Android OS**: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                appendLine("- **Supported ABIs**: ${Build.SUPPORTED_ABIS.joinToString()}")
                appendLine()
                appendLine("### Crash Log & Stacktrace")
                appendLine("```")
                appendLine(truncatedLog)
                appendLine("```")
            }
            val encodedBody = URLEncoder.encode(bodyContent, "UTF-8")
            val issueUrl = "${GitHubConfig.REPO_URL}/issues/new?title=$title&body=$encodedBody"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl))
            startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard(crashLog)
            Toast.makeText(this, "Could not open browser. Crash log copied to clipboard.", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(
    crashLog: String,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onSubmitGitHub: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "MKR Music Crashed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = onCopy) {
                        Icon(
                            painter = painterResource(R.drawable.content_copy),
                            contentDescription = stringResource(R.string.copy_logs)
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = stringResource(R.string.crash_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "We're sorry! MKR Music encountered an unexpected error. You can submit a 1-tap issue report directly to our GitHub repository with automatically attached device specs and error logs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSubmitGitHub,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.github),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report on GitHub", maxLines = 1)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.crash_share_logs), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error Stacktrace",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            // Crash log container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(16.dp)
            ) {
                Text(
                    text = crashLog,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
