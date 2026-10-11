package com.music.mkr.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.music.mkr.R
import com.music.mkr.github.GitHubConfig
import com.music.mkr.ui.utils.safeOpenUri

/**
 * Centered native Donate Dialog for MKR Music.
 * Presents exactly three payment methods in order: UPI -> Buy Me a Coffee -> Buy Me a Tea.
 */
@Composable
fun DonateDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showUpiAmountDialog by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val debounceClick: (() -> Unit) -> Unit = { action ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 700L) {
            lastClickTime = now
            action()
        }
    }

    if (showUpiAmountDialog) {
        UpiAmountDialog(
            onDismiss = { showUpiAmountDialog = false },
            onProceed = { amount ->
                showUpiAmountDialog = false
                onDismiss()
                launchUpiPayment(context, amount)
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog Title (Upper-Left)
                Text(
                    text = "Donate",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stacked Payment Options in one rounded container
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. UPI Row
                        DonateOptionRow(
                            iconRes = R.drawable.ic_currency_rupee,
                            title = "UPI",
                            subtitle = GitHubConfig.DEV_UPI_ID,
                            onClick = {
                                debounceClick {
                                    showUpiAmountDialog = true
                                }
                            }
                        )

                        HorizontalDivider(
                            thickness = 0.8.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )

                        // 2. Buy Me a Coffee Row
                        DonateOptionRow(
                            iconRes = R.drawable.ic_coffee_cup,
                            title = "Buy Me a Coffee",
                            subtitle = "buymeacoffee.com/mkr_infinity",
                            onClick = {
                                debounceClick {
                                    uriHandler.safeOpenUri(context, GitHubConfig.DEV_BUYMEACOFFEE_URL)
                                    onDismiss()
                                }
                            }
                        )

                        HorizontalDivider(
                            thickness = 0.8.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )

                        // 3. Buy Me a Tea Row
                        DonateOptionRow(
                            iconRes = R.drawable.ic_tea_cup,
                            title = "Buy Me a Tea",
                            subtitle = "buymeatea.in/mkr_infinity",
                            onClick = {
                                debounceClick {
                                    uriHandler.safeOpenUri(context, GitHubConfig.DEV_BUYMEATEA_URL)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Cancel Action (Bottom-Right aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonateOptionRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Clean secondary dialog for selecting/entering a donation amount before launching the UPI app.
 */
@Composable
private fun UpiAmountDialog(
    onDismiss: () -> Unit,
    onProceed: (Int) -> Unit
) {
    val presetAmounts = listOf(10, 25, 50, 100, 200)
    var selectedAmount by remember { mutableIntStateOf(50) }
    var customAmountText by remember { mutableStateOf("") }
    var isCustomActive by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Contribute via UPI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select or enter an amount to support MKR Music:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preset amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.take(3).forEach { amount ->
                        FilterChip(
                            selected = !isCustomActive && selectedAmount == amount,
                            onClick = {
                                isCustomActive = false
                                selectedAmount = amount
                                customAmountText = ""
                            },
                            label = { Text("₹$amount", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.drop(3).forEach { amount ->
                        FilterChip(
                            selected = !isCustomActive && selectedAmount == amount,
                            onClick = {
                                isCustomActive = false
                                selectedAmount = amount
                                customAmountText = ""
                            },
                            label = { Text("₹$amount", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom amount field
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(5)
                        customAmountText = digits
                        isCustomActive = digits.isNotEmpty()
                    },
                    prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("Custom Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                val finalAmount = if (isCustomActive) {
                    customAmountText.toIntOrNull() ?: 0
                } else {
                    selectedAmount
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100)
                    ) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (finalAmount > 0) {
                                onProceed(finalAmount)
                            }
                        },
                        enabled = finalAmount > 0,
                        shape = RoundedCornerShape(100)
                    ) {
                        Text(
                            text = if (finalAmount > 0) "Pay ₹$finalAmount" else "Proceed",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds and launches the standard upi://pay intent with recipient details.
 * Falls back to copying the UPI ID to clipboard if no UPI application is installed.
 */
private fun launchUpiPayment(context: Context, amount: Int) {
    val upiUri = Uri.parse("upi://pay").buildUpon()
        .appendQueryParameter("pa", GitHubConfig.DEV_UPI_ID)
        .appendQueryParameter("pn", GitHubConfig.DEV_UPI_NAME)
        .appendQueryParameter("tn", GitHubConfig.DEV_UPI_NOTE)
        .appendQueryParameter("am", amount.toString())
        .appendQueryParameter("cu", "INR")
        .build()

    val intent = Intent(Intent.ACTION_VIEW, upiUri)
    val chooser = Intent.createChooser(intent, "Pay ₹$amount with UPI")

    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("UPI ID", GitHubConfig.DEV_UPI_ID))
        Toast.makeText(
            context,
            "No UPI app found. Copied ${GitHubConfig.DEV_UPI_ID} to clipboard!",
            Toast.LENGTH_LONG
        ).show()
    }
}
