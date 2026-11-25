/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.mkr.BuildConfig
import com.music.mkr.LocalPlayerAwareWindowInsets
import com.music.mkr.R
import com.music.mkr.github.GitHubConfig
import com.music.mkr.github.GitHubViewModel
import com.music.mkr.ui.component.DialogBasic
import com.music.mkr.ui.component.ExpressiveSettingGroup
import com.music.mkr.ui.component.IconButton
import com.music.mkr.ui.component.Material3SettingsItem
import com.music.mkr.ui.component.DonateDialog
import com.music.mkr.ui.utils.backToMain
import com.music.mkr.ui.utils.safeOpenUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: (() -> Unit)? = null,
    gitHubViewModel: GitHubViewModel = hiltViewModel()
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }

    if (showSupportDialog) {
        DonateDialog(onDismiss = { showSupportDialog = false })
    }

    val installedDate = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val installTime = packageInfo.firstInstallTime
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(installTime))
        } catch (_: Exception) {
            "Installed"
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Hero Section: Developer Profile & Social Media Hub
        DeveloperHeroCard(onSupportClick = { showSupportDialog = true })

        Spacer(modifier = Modifier.height(16.dp))

        // Application Details & Legal
        ExpressiveSettingGroup(
            title = "App Information",
            items = listOf(
                Material3SettingsItem(
                    key = "about_app",
                    icon = painterResource(R.drawable.deployed_app_update),
                    title = { Text(stringResource(R.string.installed_date_title)) },
                    trailingContent = { Text(installedDate) }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Information Card with Default App Logo placed at the last of About section
        AppVersionTile(
            appName = "MKR Music",
            buildDetails = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • Stable Universal Release",
            onGithubClick = { uriHandler.safeOpenUri(context, GitHubConfig.REPO_URL) }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }

    TopAppBar(
        title = { Text("About") },
        navigationIcon = {
            IconButton(
                onClick = { onBack?.invoke() ?: navController.navigateUp() },
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeveloperHeroCard(
    onSupportClick: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Expressive Avatar with Cookie Halo & App Badge
            Box(
                modifier = Modifier.size(94.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape())
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp)
                        .align(Alignment.Center)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(GitHubConfig.DEV_AVATAR_URL)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.dev),
                        error = painterResource(R.drawable.dev),
                        fallback = painterResource(R.drawable.dev),
                        contentDescription = GitHubConfig.DEV_NAME,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "MKR Music Badge",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Developer Name
            Text(
                text = GitHubConfig.DEV_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Role Pill
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
            ) {
                Text(
                    text = GitHubConfig.DEV_ROLE,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Expressive 3-Button Social Dock
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialLinkTile(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.github,
                        title = "GitHub",
                        badgeShape = MaterialShapes.Cookie6Sided.toShape(),
                        containerTint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_GITHUB_URL) }
                    )
                    SocialLinkTile(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.instagram,
                        title = "Instagram",
                        badgeShape = MaterialShapes.Cookie4Sided.toShape(),
                        containerTint = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_INSTAGRAM_URL) }
                    )
                    SocialLinkTile(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.telegram,
                        title = "Telegram",
                        badgeShape = MaterialShapes.Clover8Leaf.toShape(),
                        containerTint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = { uriHandler.safeOpenUri(context, GitHubConfig.DEV_TELEGRAM_URL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Support Button with animated chainsaw spike border inside the card
            ChainsawSupportButton(onClick = onSupportClick)
        }
    }
}

@Composable
private fun ChainsawSupportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chainsaw_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chainsaw_phase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackBorderColor = primaryColor.copy(alpha = 0.55f)
    val spikeColor = primaryColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .drawWithContent {
                drawContent()

                val inset = 4.dp.toPx()
                val w = size.width - 2 * inset
                val h = size.height - 2 * inset
                if (w > 0 && h > 0) {
                    val r = h / 2f
                    val straightLen = (w - 2 * r).coerceAtLeast(0f)
                    val arcLen = Math.PI.toFloat() * r
                    val perimeter = 2 * straightLen + 2 * arcLen

                    // Draw the smooth guide track along the perimeter
                    val strokeWidth = 1.5.dp.toPx()
                    drawRoundRect(
                        color = trackBorderColor,
                        topLeft = Offset(inset, inset),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(r, r),
                        style = Stroke(width = strokeWidth)
                    )

                    // Draw moving chainsaw teeth/spikes around the track
                    val toothSpacing = 16.dp.toPx()
                    val toothCount = (perimeter / toothSpacing).toInt().coerceAtLeast(14)
                    val step = perimeter / toothCount
                    val toothHeight = 3.8.dp.toPx()
                    val toothBack = 3.2.dp.toPx()
                    val toothFwd = 2.2.dp.toPx()

                    fun getTrackPoint(dist: Float): Triple<Offset, Offset, Offset> {
                        val d = (dist % perimeter + perimeter) % perimeter
                        return when {
                            d <= straightLen -> {
                                val pt = Offset(inset + r + d, inset)
                                val tang = Offset(1f, 0f)
                                val norm = Offset(0f, -1f)
                                Triple(pt, tang, norm)
                            }
                            d <= straightLen + arcLen -> {
                                val s = d - straightLen
                                val theta = -Math.PI.toFloat() / 2f + (s / r)
                                val cosT = kotlin.math.cos(theta)
                                val sinT = kotlin.math.sin(theta)
                                val pt = Offset(inset + (w - r) + r * cosT, inset + r + r * sinT)
                                val tang = Offset(-sinT, cosT)
                                val norm = Offset(cosT, sinT)
                                Triple(pt, tang, norm)
                            }
                            d <= 2 * straightLen + arcLen -> {
                                val s = d - (straightLen + arcLen)
                                val pt = Offset(inset + (w - r) - s, inset + h)
                                val tang = Offset(-1f, 0f)
                                val norm = Offset(0f, 1f)
                                Triple(pt, tang, norm)
                            }
                            else -> {
                                val s = d - (2 * straightLen + arcLen)
                                val theta = Math.PI.toFloat() / 2f + (s / r)
                                val cosT = kotlin.math.cos(theta)
                                val sinT = kotlin.math.sin(theta)
                                val pt = Offset(inset + r + r * cosT, inset + r + r * sinT)
                                val tang = Offset(-sinT, cosT)
                                val norm = Offset(cosT, sinT)
                                Triple(pt, tang, norm)
                            }
                        }
                    }

                    val spikePath = Path()
                    for (i in 0 until toothCount) {
                        val currentDist = (i + phase) * step
                        val (basePt, tang, norm) = getTrackPoint(currentDist)

                        val backPt = Offset(
                            basePt.x - tang.x * toothBack,
                            basePt.y - tang.y * toothBack
                        )
                        val tipPt = Offset(
                            basePt.x + tang.x * (toothFwd * 0.45f) + norm.x * toothHeight,
                            basePt.y + tang.y * (toothFwd * 0.45f) + norm.y * toothHeight
                        )
                        val frontPt = Offset(
                            basePt.x + tang.x * toothFwd,
                            basePt.y + tang.y * toothFwd
                        )

                        spikePath.reset()
                        spikePath.moveTo(backPt.x, backPt.y)
                        spikePath.lineTo(tipPt.x, tipPt.y)
                        spikePath.lineTo(frontPt.x, frontPt.y)
                        spikePath.close()

                        drawPath(
                            path = spikePath,
                            color = spikeColor,
                            style = Fill
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(50.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.buymeacoffee),
                    contentDescription = "Support",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Donate & Support",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SocialLinkTile(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    badgeShape: Shape,
    containerTint: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 68.dp)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(badgeShape)
                    .background(containerTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AppVersionTile(
    appName: String,
    buildDetails: String,
    onGithubClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(R.drawable.icon),
                    contentDescription = "MKR Music",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Universal",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            supportingContent = {
                Text(
                    text = buildDetails,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onGithubClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.github),
                        contentDescription = "GitHub Repository",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}