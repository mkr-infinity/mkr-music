package com.music.mkr.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.music.mkr.R
import com.music.mkr.constants.InterfaceAnimationsKey
import com.music.mkr.github.GitHubConfig
import com.music.mkr.ui.component.snackbar.MkrSnackbarHost
import com.music.mkr.ui.utils.safeOpenUri
import com.music.mkr.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val animationsEnabled by rememberPreference(InterfaceAnimationsKey, true)
    val feedback = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2400)
            copied = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(horizontal = 20.dp),
            ) {
                Text(stringResource(R.string.support_mkr_title), style = MaterialTheme.typography.headlineSmall)
                MkrSnackbarHost(feedback, modifier = Modifier.fillMaxWidth())
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.support_mkr_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(R.string.support_upi_id), style = MaterialTheme.typography.labelMedium)
                            Text(GitHubConfig.DEV_UPI_ID, style = MaterialTheme.typography.titleMedium)
                            AnimatedActionButton(
                                onClick = {
                                    copied = runCatching {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", GitHubConfig.DEV_UPI_ID))
                                    }.isSuccess
                                    feedback.currentSnackbarData?.dismiss()
                                    if (!copied) {
                                        scope.launch {
                                            feedback.showSnackbar(context.getString(R.string.support_upi_copy_failed))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                animate = animationsEnabled,
                                compact = true,
                                emphasis = ActionButtonEmphasis.Primary,
                            ) {
                                Icon(painterResource(if (copied) R.drawable.check else R.drawable.content_copy), null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(if (copied) R.string.support_upi_copied else R.string.support_copy_upi_id),
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.support_upi_helper), style = MaterialTheme.typography.bodyMedium)
                    AnimatedActionButton(
                        onClick = {
                            uriHandler.safeOpenUri(context, GitHubConfig.DEV_BUYMEACOFFEE_URL)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        animate = animationsEnabled,
                        compact = true,
                        emphasis = ActionButtonEmphasis.Tonal,
                    ) {
                        Icon(painterResource(R.drawable.buymeacoffee), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.support_buy_me_a_coffee))
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.done))
                    }
                }
            }
        }
    }
}
