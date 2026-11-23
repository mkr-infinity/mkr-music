/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.ui.player

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.navigation.NavController
import com.music.mkr.LocalListenTogetherManager
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.R
import com.music.mkr.constants.ListItemHeight
import com.music.mkr.constants.PlayerBackgroundStyle
import com.music.mkr.constants.QueueEditLockKey
import com.music.mkr.constants.ShowCommentButtonKey
import com.music.mkr.extensions.getQueueWindows
import com.music.mkr.extensions.metadata
import com.music.mkr.extensions.move
import com.music.mkr.extensions.toggleRepeatMode
import com.music.mkr.listentogether.RoomRole
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import android.view.ViewTreeObserver
import com.music.mkr.models.ActiveOutputDevice
import com.music.mkr.models.PlayerOutputDevice
import com.music.mkr.utils.oem.SystemMediaControlResolver
import com.music.mkr.models.MediaMetadata
import com.music.mkr.ui.component.ActionPromptDialog
import com.music.mkr.ui.component.BottomSheet
import com.music.mkr.ui.component.BottomSheetState
import com.music.mkr.ui.component.EmptyPlaceholder
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.MediaMetadataListItem
import com.music.mkr.ui.menu.PlayerMenu
import com.music.mkr.ui.menu.QueueMenu
import com.music.mkr.ui.menu.SelectionMediaMetadataMenu
import com.music.mkr.ui.screens.CommentSheet
import com.music.mkr.ui.utils.ShowMediaInfo
import com.music.mkr.ui.utils.resize
import com.music.mkr.ui.component.snackbar.SnackbarManager
import com.music.mkr.utils.listItemShape
import com.music.mkr.utils.makeTimeString
import com.music.mkr.utils.rememberPreference
import androidx.compose.runtime.produceState
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.music.mkr.mkrmusic.isBluetoothHeadphoneConnected
import com.music.mkr.mkrmusic.AudioDeviceBottomSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.layout.PaddingValues
import coil3.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Queue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    background: Color,
    onBackgroundColor: Color,
    TextBackgroundColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    pureBlack: Boolean,
    showInlineLyrics: Boolean,
    playerBackground: PlayerBackgroundStyle = PlayerBackgroundStyle.DEFAULT,
    onToggleLyrics: () -> Unit = {},
) {
    val context = LocalContext.current
    var showAudioDeviceBottomSheet by remember { mutableStateOf(false) }
    val isBluetoothConnected = isBluetoothHeadphoneConnected(context)
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboard.current
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val playerConnection = LocalPlayerConnection.current ?: return
    val audioDevice by playerConnection.activeAudioDevice.collectAsState()

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) playerConnection.refreshActiveDevice()
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose { view.viewTreeObserver.removeOnWindowFocusChangeListener(listener) }
    }

    // Listen Together state (reactive)
    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsState(initial = com.music.mkr.listentogether.RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    // Cast state - safely access castConnectionHandler to prevent crashes during service lifecycle changes
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }
    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }

    val selection = viewModel<QueueSelection>()
    val onExitSelectionMode = {
        selection.active = false
        selection.uids = emptySet()
    }
    LaunchedEffect(state.isCollapsed) {
        if (state.isCollapsed) onExitSelectionMode()
    }
    var locked by rememberPreference(QueueEditLockKey, defaultValue = true)

    val (showCommentButton) = rememberPreference(
        ShowCommentButtonKey,
        defaultValue = true
    )

    val snackbarHostState = remember { SnackbarHostState() }
    var dismissJob: Job? by remember { mutableStateOf(null) }

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerValue by remember { mutableFloatStateOf(30f) }
    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
        playerConnection.service.sleepTimer.isActive
    }
    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        background = {
            Box(Modifier.fillMaxSize().background(Color.Unspecified))
        },
        collapsedContent = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .windowInsetsPadding(
                            WindowInsets.systemBars
                                .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                        ),
                ) {
                    // Queue button (pill)
                    Surface(
                        onClick = { state.expandSoft() },
                        shape = RoundedCornerShape(16.dp),
                        color = TextBackgroundColor.copy(alpha = 0.12f),
                        modifier = Modifier
                            .height(46.dp)
                            .weight(1f),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.queue_music),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = TextBackgroundColor,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.queue),
                                color = TextBackgroundColor,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Center utility buttons: Audio device & Sleep timer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = { showAudioDeviceBottomSheet = true },
                            shape = CircleShape,
                            color = TextBackgroundColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isBluetoothConnected) R.drawable.headphones else R.drawable.home_speaker_devices,
                                    ),
                                    contentDescription = stringResource(R.string.audio_devices),
                                    modifier = Modifier.size(20.dp),
                                    tint = TextBackgroundColor,
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                if (!isListenTogetherGuest) {
                                    if (sleepTimerEnabled) {
                                        playerConnection.service.sleepTimer.clear()
                                    } else {
                                        showSleepTimerDialog = true
                                    }
                                }
                            },
                            shape = if (sleepTimerEnabled) RoundedCornerShape(20.dp) else CircleShape,
                            color = if (sleepTimerEnabled) TextBackgroundColor.copy(alpha = 0.22f) else TextBackgroundColor.copy(alpha = 0.12f),
                            modifier = if (sleepTimerEnabled) Modifier.height(46.dp) else Modifier.size(46.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = if (sleepTimerEnabled) 10.dp else 0.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.sleep_timer),
                                    contentDescription = stringResource(R.string.sleep_timer),
                                    modifier = Modifier.size(20.dp),
                                    tint = TextBackgroundColor,
                                )
                                if (sleepTimerEnabled) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = makeTimeString(sleepTimerTimeLeft),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextBackgroundColor,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Lyrics button (pill)
                    Surface(
                        onClick = onToggleLyrics,
                        shape = RoundedCornerShape(16.dp),
                        color = TextBackgroundColor.copy(alpha = 0.12f),
                        modifier = Modifier
                            .height(46.dp)
                            .weight(1f),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.lyrics),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = TextBackgroundColor,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.lyrics),
                                color = TextBackgroundColor,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            
            if (showAudioDeviceBottomSheet) {
                AudioDeviceBottomSheet(onDismiss = { showAudioDeviceBottomSheet = false })
            }

            if (showSleepTimerDialog) {
                ActionPromptDialog(
                    titleBar = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.sleep_timer),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    },
                    onDismiss = { showSleepTimerDialog = false },
                    onConfirm = {
                        showSleepTimerDialog = false
                        playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
                    },
                    onCancel = {
                        showSleepTimerDialog = false
                    },
                    onReset = {
                        sleepTimerValue = 30f // Default value
                    },
                    content = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.minute,
                                    sleepTimerValue.roundToInt(),
                                    sleepTimerValue.roundToInt()
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Spacer(Modifier.height(16.dp))

                            Slider(
                                value = sleepTimerValue,
                                onValueChange = { sleepTimerValue = it },
                                valueRange = 5f..120f,
                                steps = (120 - 5) / 5 - 1,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    showSleepTimerDialog = false
                                    playerConnection.service.sleepTimer.start(-1)
                                }
                            ) {
                                Text(stringResource(R.string.end_of_song))
                            }
                        }
                    }
                )
            }
        },
    ) {
        BackHandler(enabled = selection.active && state.isExpanded, onBack = onExitSelectionMode)
        val unknownTrack = stringResource(R.string.unknown)
        val queueTitle by playerConnection.queueTitle.collectAsState()
        val radioChips by playerConnection.radioChips.collectAsState()
        val queueWindows by playerConnection.queueWindows.collectAsState()
        val queueOrder = remember(queueWindows) { queueWindows.map { it.uid } }
        val selectedItems by remember(playerConnection, selection) {
            derivedStateOf {
                if (selection.uids.isEmpty()) emptyList()
                else queueWindows.filter { it.uid in selection.uids }
            }
        }
        LaunchedEffect(queueOrder) {
            if (selection.uids.isNotEmpty()) selection.uids = selection.uids.intersect(queueOrder)
        }
        val automix by playerConnection.service.automixItems.collectAsState()
        val mutableQueueWindows = remember(playerConnection) { queueWindows.toMutableStateList() }
        val queueLength = remember(queueWindows) {
            var seconds = 0L
            for (window in queueWindows) {
                val duration = window.mediaItem.metadata?.duration?.takeIf { it > 0 } ?: return@remember null
                seconds += duration
            }
            seconds
        }
        var dragInfo by remember { mutableStateOf<QueueDrag?>(null) }

        val currentPlayingUid = remember(currentWindowIndex, queueWindows) {
            if (currentWindowIndex in queueWindows.indices) {
                queueWindows[currentWindowIndex].uid
            } else null
        }

        val reorderableState = rememberReorderableLazyListState(
            lazyListState = lazyListState,
            scrollThresholdPadding = PaddingValues(vertical = ListItemHeight),
        ) onMove@{ from, to ->
            val player = playerConnection.player
            if (locked || isListenTogetherGuest || !player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)) {
                return@onMove
            }
            val fromIndex = mutableQueueWindows.indexOfFirst { it.uid.hashCode() == from.key }
            val toIndex = mutableQueueWindows.indexOfFirst { it.uid.hashCode() == to.key }
            if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return@onMove

            if (dragInfo == null && mutableQueueWindows.map { it.uid } != queueOrder) return@onMove
            val drag = dragInfo ?: QueueDrag(
                player,
                player.shuffleModeEnabled,
                queueOrder,
                mutableQueueWindows[fromIndex].uid,
            ).also { dragInfo = it }
            if (player !== drag.player || player.shuffleModeEnabled != drag.shuffled ||
                queueOrder != drag.order || mutableQueueWindows[fromIndex].uid != drag.moved
            ) return@onMove

            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            mutableQueueWindows.move(fromIndex, toIndex)
        }

        LaunchedEffect(reorderableState.isAnyItemDragging) {
            if (reorderableState.isAnyItemDragging) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                dragInfo?.let { drag ->
                    val player = playerConnection.player
                    val from = drag.order.indexOf(drag.moved)
                    val to = mutableQueueWindows.indexOfFirst { it.uid == drag.moved }
                    val valid = !locked && !isListenTogetherGuest && player === drag.player &&
                        player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS) &&
                        player.shuffleModeEnabled == drag.shuffled &&
                        player.currentMediaItemIndex in 0 until player.currentTimeline.windowCount &&
                        player.getQueueWindows().map { it.uid } == drag.order
                    if (valid && from >= 0 && to >= 0 && from != to) {
                        if (!drag.shuffled) {
                            player.moveMediaItem(from, to)
                        } else {
                            val timeline = player.currentTimeline
                            val window = Timeline.Window()
                            val indices = (0 until timeline.windowCount).associateBy { timeline.getWindow(it, window).uid }
                            val order = mutableQueueWindows.mapNotNull { indices[it.uid] }
                            if (order.size == timeline.windowCount) {
                                player.setShuffleOrder(DefaultShuffleOrder(order.toIntArray(), System.currentTimeMillis()))
                            }
                        }
                    } else {
                        mutableQueueWindows.clear()
                        mutableQueueWindows.addAll(playerConnection.queueWindows.value)
                    }
                    dragInfo = null
                }
            }
        }

        LaunchedEffect(queueWindows, shuffleModeEnabled) {
            val drag = dragInfo
            val keepDragPreview = drag != null && queueOrder == drag.order && shuffleModeEnabled == drag.shuffled
            if (!keepDragPreview && mutableQueueWindows.toList() != queueWindows) {
                mutableQueueWindows.clear()
                mutableQueueWindows.addAll(queueWindows)
            }
        }

        val header: @Composable (Boolean) -> Unit = { bounded ->
            Column(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { },
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                    )
                    if (bounded) {
                        IconButton(
                            onClick = { state.collapseSoft() },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.close),
                            )
                        }
                    }
                }

                // Current song track row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    AsyncImage(
                        model = mediaMetadata?.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = mediaMetadata?.title.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    val likeDescription = if (currentSong?.song?.liked == true) stringResource(R.string.action_remove_like) else stringResource(R.string.action_like)
                    IconButton(
                        onClick = playerConnection::toggleLike,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (currentSong?.song?.liked == true) R.drawable.favorite
                                else R.drawable.favorite_border
                            ),
                            contentDescription = likeDescription,
                            tint = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                // Action pill bar: Lock, Menu, Clear queue, Audio Output device, and song count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        IconButton(
                            onClick = { locked = !locked },
                            modifier = Modifier.size(38.dp),
                        ) {
                            Icon(
                                painter = painterResource(if (locked) R.drawable.lock else R.drawable.lock_open),
                                contentDescription = if (locked) stringResource(R.string.unlock_queue) else stringResource(R.string.lock_queue),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = {
                                menuState.show {
                                    PlayerMenu(
                                        mediaMetadata = mediaMetadata,
                                        navController = navController,
                                        playerBottomSheetState = playerBottomSheetState,
                                        onShowDetailsDialog = {
                                            mediaMetadata?.id?.let {
                                                bottomSheetPageState.show {
                                                    ShowMediaInfo(it)
                                                }
                                            }
                                        },
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            },
                            modifier = Modifier.size(38.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more_options),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            enabled = !isListenTogetherGuest,
                            onClick = {
                                val player = playerConnection.player
                                val active = player.currentMediaItemIndex
                                if (active in 0 until player.mediaItemCount &&
                                    player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                                ) {
                                    if (active + 1 < player.mediaItemCount) {
                                        player.removeMediaItems(active + 1, player.mediaItemCount)
                                    }
                                    if (active > 0) player.removeMediaItems(0, active)
                                    selection.uids = emptySet()
                                    selection.active = false
                                }
                            },
                            modifier = Modifier.size(38.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = stringResource(R.string.clear),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Text(
                        text = pluralStringResource(R.plurals.n_song, queueWindows.size, queueWindows.size) +
                            (queueLength?.takeIf { it > 0 }?.let { "  •  " + makeTimeString(it * 1000L) } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }

                // Connected action controls: Shuffle, Repeat, Radio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToggleButton(
                        checked = shuffleModeEnabled,
                        onCheckedChange = { checked ->
                            val player = playerConnection.player
                            if (player.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
                                player.shuffleModeEnabled = checked
                            }
                        },
                        enabled = !isListenTogetherGuest,
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.shuffle),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.shuffle),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    ToggleButton(
                        checked = repeatMode != Player.REPEAT_MODE_OFF,
                        onCheckedChange = {
                            playerConnection.player.toggleRepeatMode()
                        },
                        enabled = !isListenTogetherGuest,
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                when (repeatMode) {
                                    Player.REPEAT_MODE_ALL -> R.drawable.repeat
                                    Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                    else -> R.drawable.repeat
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.repeat),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    ToggleButton(
                        checked = false,
                        onCheckedChange = {
                            SnackbarManager.show(R.string.starting_radio)
                            playerConnection.startRadioSeamlessly()
                        },
                        enabled = !isListenTogetherGuest,
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.radio),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.start_an_radio),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = queueTitle ?: stringResource(R.string.next_in_queue),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AnimatedVisibility(visible = radioChips.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(radioChips, key = { it.title }) { chip ->
                            val chipBg = if (chip.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val chipTextCol = if (chip.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .clickable(enabled = !isListenTogetherGuest && !chip.isSelected) {
                                        playerConnection.selectRadioChip(chip)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = chip.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = chipTextCol,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }



                AnimatedVisibility(
                    visible = selection.active,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    val count = selectedItems.size
                    Row(
                        modifier =
                        Modifier
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onExitSelectionMode,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.close),
                            )
                        }
                        Text(
                            text = pluralStringResource(R.plurals.n_selected, count, count),
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = count > 0 && count == queueWindows.size,
                            onCheckedChange = { checked ->
                                selection.uids = if (checked) {
                                    playerConnection.queueWindows.value.mapTo(hashSetOf()) { it.uid }
                                } else emptySet()
                            }
                        )
                        IconButton(
                            enabled = count > 0,
                            onClick = {
                                menuState.show {
                                    val items = selectedItems
                                    if (items.isEmpty()) {
                                        LaunchedEffect(Unit) { menuState.dismiss() }
                                    } else {
                                        SelectionMediaMetadataMenu(
                                            songSelection = remember(items) { items.mapNotNull { it.mediaItem.metadata } },
                                            onDismiss = menuState::dismiss,
                                            clearAction = onExitSelectionMode,
                                            currentItems = items,
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                                tint = LocalContentColor.current,
                            )
                        }
                    }
                }
            }

        }

        QueueViewport(state, background) { scrollHeader, bounded ->
            LaunchedEffect(currentPlayingUid, currentWindowIndex, scrollHeader, mutableQueueWindows.size) {
                if (dragInfo == null && currentPlayingUid != null) {
                    val index = mutableQueueWindows.indexOfFirst { it.uid == currentPlayingUid }
                    if (index >= 0) {
                        lazyListState.scrollToItem(if (scrollHeader && index > 0) index + 1 else index)
                    }
                }
            }
            if (!scrollHeader) header(bounded)

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(top = 8.dp, bottom = ListItemHeight + 8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(state.preUpPostDownNestedScrollConnection)
                ) {
                    if (scrollHeader) item(key = "queue_header") { header(bounded) }
                    item(key = "queue_top_spacer") {
                        Spacer(
                            modifier =
                                Modifier
                                    .animateContentSize()
                                    .height(if (selection.active) 48.dp else 0.dp),
                        )
                    }

                    if (mutableQueueWindows.isEmpty()) {
                        item(key = "empty_queue") {
                            EmptyPlaceholder(
                                icon = R.drawable.queue_music,
                                text = stringResource(R.string.playlist_is_empty),
                                description = "Play a track or album to add songs to your queue",
                                modifier = Modifier.padding(top = 48.dp)
                            )
                        }
                    }

                    itemsIndexed(
                        items = mutableQueueWindows,
                        key = { _, item -> item.uid.hashCode() },
                    ) { index, window ->
                        ReorderableItem(
                            state = reorderableState,
                            key = window.uid.hashCode(),
                        ) { isDragging ->
                            val metadata = window.mediaItem.metadata
                            val displayMetadata = metadata ?: window.mediaItem.queueDisplayMetadata(unknownTrack)
                            val isActive = window.uid == currentPlayingUid
                            val dragScale by animateFloatAsState(
                                targetValue = if (isDragging) 1.02f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "queueDragScale"
                            )
                            val dragElevation by animateDpAsState(
                                targetValue = if (isDragging) 6.dp else 0.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "queueDragElevation"
                            )
                            val dismissBoxState =
                                rememberSwipeToDismissBoxState(
                                    positionalThreshold = { totalDistance -> totalDistance }
                                )

                            var processedDismiss by remember { mutableStateOf(false) }
                            LaunchedEffect(dismissBoxState.currentValue) {
                                val dv = dismissBoxState.currentValue
                                if (!processedDismiss && !locked && !isListenTogetherGuest && (
                                            dv == SwipeToDismissBoxValue.StartToEnd ||
                                                    dv == SwipeToDismissBoxValue.EndToStart
                                            )
                                ) {
                                    processedDismiss = true
                                    val player = playerConnection.player
                                    val itemIndex = player.queueWindowIndices(setOf(window.uid)).singleOrNull()
                                    if (itemIndex == null || !player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)) {
                                        dismissBoxState.reset()
                                        return@LaunchedEffect
                                    }
                                    val removed = window.mediaItem
                                    val timeline = player.currentTimeline
                                    val neighbor = Timeline.Window()
                                    val previousUid = if (itemIndex > 0) timeline.getWindow(itemIndex - 1, neighbor).uid else null
                                    val nextUid = if (itemIndex + 1 < timeline.windowCount) {
                                        timeline.getWindow(itemIndex + 1, neighbor).uid
                                    } else null
                                    player.removeMediaItem(itemIndex)
                                    dismissJob?.cancel()
                                    dismissJob = coroutineScope.launch {
                                        val snackbarResult = snackbarHostState.showSnackbar(
                                            message = context.getString(
                                                R.string.removed_song_from_playlist,
                                                displayMetadata.title,
                                            ),
                                            actionLabel = context.getString(R.string.undo),
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (snackbarResult == SnackbarResult.ActionPerformed &&
                                            playerConnection.player === player &&
                                            player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                                        ) {
                                            val insertion = nextUid?.let { player.queueWindowIndices(setOf(it)).singleOrNull() }
                                                ?: previousUid?.let { player.queueWindowIndices(setOf(it)).singleOrNull()?.plus(1) }
                                                ?: 0.takeIf { player.mediaItemCount == 0 }
                                            if (insertion != null) player.addMediaItem(insertion, removed)
                                        }
                                    }
                                }
                                if (dv == SwipeToDismissBoxValue.Settled) {
                                    processedDismiss = false
                                }
                            }

                            val onCheckedChange: (Boolean) -> Unit = { checked ->
                                val uid = window.uid
                                if (!checked) {
                                    selection.uids = selection.uids - uid
                                } else if (playerConnection.queueWindows.value.any { it.uid == uid }) {
                                    selection.uids = selection.uids + uid
                                }
                            }

                            val content: @Composable () -> Unit = {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                    .animateItem()
                                    .graphicsLayer {
                                        scaleX = dragScale
                                        scaleY = dragScale
                                        shadowElevation = dragElevation.toPx()
                                    },
                                ) {
                                    MediaMetadataListItem(
                                        mediaMetadata = displayMetadata,
                                        isSelected = false,
                                        isActive = isActive,
                                        isPlaying = isPlaying && isActive,
                                        shape = listItemShape(index, mutableQueueWindows.size),
                                        trailingContent = {
                                            if (selection.active) {
                                                Checkbox(
                                                    checked = window.uid in selection.uids,
                                                    onCheckedChange = onCheckedChange
                                                )
                                            } else {
                                                if (!isListenTogetherGuest && metadata != null) {
                                                    IconButton(
                                                        onClick = {
                                                            menuState.show {
                                                                QueueMenu(
                                                                    mediaMetadata = metadata,
                                                                    navController = navController,
                                                                    playerBottomSheetState = playerBottomSheetState,
                                                                    onShowDetailsDialog = {
                                                                        window.mediaItem.mediaId.let {
                                                                            bottomSheetPageState.show {
                                                                                ShowMediaInfo(it)
                                                                            }
                                                                        }
                                                                    },
                                                                    onDismiss = menuState::dismiss,
                                                                )
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.more_vert),
                                                            contentDescription = null,
                                                        )
                                                    }
                                                }
                                                if (!locked && !isListenTogetherGuest) {
                                                    IconButton(
                                                        onClick = { },
                                                        modifier = Modifier.draggableHandle()
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.drag_handle),
                                                            contentDescription = "Reorder",
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .background(background)
                                                .combinedClickable(
                                                    onClick = {
                                                        if (selection.active) {
                                                            onCheckedChange(window.uid !in selection.uids)
                                                        } else if (!isListenTogetherGuest) {
                                                            val player = playerConnection.player
                                                            val target = player.queueWindowIndices(setOf(window.uid)).singleOrNull()
                                                                ?: return@combinedClickable
                                                            if (target == player.currentMediaItemIndex) {
                                                                if (isCasting) {
                                                                    if (castIsPlaying) {
                                                                        castHandler?.pause()
                                                                    } else {
                                                                        castHandler?.play()
                                                                    }
                                                                } else {
                                                                    playerConnection.togglePlayPause()
                                                                }
                                                            } else {
                                                                if (isCasting) {
                                                                    val mediaId =
                                                                        window.mediaItem.mediaId
                                                                    val navigated =
                                                                        castHandler?.navigateToMediaIfInQueue(
                                                                            mediaId
                                                                        ) ?: false
                                                                    if (!navigated) {
                                                                        player.seekToDefaultPosition(target)
                                                                    }
                                                                } else {
                                                                    if (!playerConnection.service.manualSeekToIndexWithCrossfade(target)) {
                                                                        player.seekToDefaultPosition(target)
                                                                        player.playWhenReady = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!selection.active) {
                                                            haptic.performHapticFeedback(
                                                                HapticFeedbackType.LongPress
                                                            )
                                                            selection.active = true
                                                            onCheckedChange(true)
                                                        }
                                                    },
                                                ),
                                    )
                                }
                            }

                            if (locked) {
                                content()
                            } else {
                                val dismissDirection = dismissBoxState.dismissDirection
                                val isDismissing = dismissDirection != null
                                val dismissBgColor by animateColorAsState(
                                    targetValue = when (dismissBoxState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd,
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.90f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f)
                                    },
                                    label = "queueDismissBg"
                                )
                                val dismissIconTint by animateColorAsState(
                                    targetValue = when (dismissBoxState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd,
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    label = "queueDismissIconTint"
                                )

                                SwipeToDismissBox(
                                    state = dismissBoxState,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(listItemShape(index, mutableQueueWindows.size))
                                                .background(dismissBgColor)
                                                .padding(horizontal = 24.dp),
                                            contentAlignment = if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                                Alignment.CenterStart
                                            } else {
                                                Alignment.CenterEnd
                                            }
                                        ) {
                                            if (isDismissing) {
                                                Icon(
                                                    painter = painterResource(R.drawable.delete),
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = dismissIconTint,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.animateItem()
                                ) {
                                    content()
                                }
                            }
                        }
                    }

                    if (automix.isNotEmpty()) {
                        item(key = "automix_divider") {
                            Text(
                                text = stringResource(R.string.similar_content),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                            )
                        }

                        itemsIndexed(
                            items = automix,
                            key = { _, it -> it.mediaId },
                        ) { index, item ->
                            val metadata = item.metadata
                            Row(
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                MediaMetadataListItem(
                                    mediaMetadata = metadata ?: item.queueDisplayMetadata(unknownTrack),
                                    shape = listItemShape(index, automix.size),
                                    trailingContent = {
                                        if (!isListenTogetherGuest) {
                                            IconButton(
                                                onClick = {
                                                    playerConnection.service.playNextAutomix(
                                                        item,
                                                        index,
                                                    )
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.playlist_play),
                                                    contentDescription = null,
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    playerConnection.service.addToQueueAutomix(
                                                        item,
                                                        index,
                                                    )
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.queue_music),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    if (!isListenTogetherGuest) {
                                                        playerConnection.service.playNextAutomix(
                                                            item,
                                                            index,
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    if (metadata != null) menuState.show {
                                                        QueueMenu(
                                                            mediaMetadata = metadata,
                                                            navController = navController,
                                                            playerBottomSheetState = playerBottomSheetState,
                                                            onShowDetailsDialog = {
                                                                item.mediaId.let {
                                                                    bottomSheetPageState.show {
                                                                        ShowMediaInfo(it)
                                                                    }
                                                                }
                                                            },
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier =
                    Modifier
                        .padding(bottom = ListItemHeight)
                        .align(Alignment.BottomCenter),
                )
            }
        }
    }

    if (showCommentSheet) {
        CommentSheet(
            videoId = mediaMetadata?.id ?: "",
            onDismiss = { showCommentSheet = false }
        )
    }
}

@Composable
private fun QueueViewport(
    state: BottomSheetState,
    background: Color,
    content: @Composable ColumnScope.(scrollHeader: Boolean, bounded: Boolean) -> Unit,
) {
    val posture = currentWindowAdaptiveInfo().windowPosture
    val verticalHinge = posture.separatingVerticalHingeBounds.firstOrNull()
    val horizontalHinge = posture.separatingHorizontalHingeBounds.firstOrNull()
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    var origin by remember { mutableStateOf(Offset.Zero) }
    val locate = if (verticalHinge != null || horizontalHinge != null) Modifier.onGloballyPositioned {
        val position = it.positionInWindow()
        val slide = with(density) { (state.expandedBound - state.value).toPx() }
        origin = Offset(position.x, position.y - slide)
    } else Modifier

    BoxWithConstraints(Modifier.fillMaxSize().then(locate)) {
        val safeLeft = insets.calculateLeftPadding(direction).coerceAtMost(maxWidth)
        val safeRight = (maxWidth - insets.calculateRightPadding(direction)).coerceAtLeast(safeLeft)
        val safeTop = insets.calculateTopPadding().coerceAtMost(maxHeight)
        val safeBottom = (maxHeight - insets.calculateBottomPadding()).coerceAtLeast(safeTop)
        var left = safeLeft
        var right = safeRight
        var top = safeTop
        var bottom = safeBottom

        if (verticalHinge != null) {
            val hingeLeft = with(density) { (verticalHinge.left - origin.x).toDp() }
            val hingeRight = with(density) { (verticalHinge.right - origin.x).toDp() }
            if (hingeLeft < right && hingeRight > left) {
                val leftEdge = hingeLeft.coerceIn(left, right)
                val rightEdge = hingeRight.coerceIn(left, right)
                val leftWidth = leftEdge - left
                val rightWidth = right - rightEdge
                val rtl = direction == LayoutDirection.Rtl
                val endWidth = if (rtl) leftWidth else rightWidth
                val startWidth = if (rtl) rightWidth else leftWidth
                val preferEnd = endWidth >= 320.dp || endWidth >= startWidth
                if (preferEnd != rtl) left = rightEdge else right = leftEdge
            }
        }
        if (horizontalHinge != null) {
            val hingeTop = with(density) { (horizontalHinge.top - origin.y).toDp() }
            val hingeBottom = with(density) { (horizontalHinge.bottom - origin.y).toDp() }
            if (hingeTop < bottom && hingeBottom > top) {
                val topEdge = hingeTop.coerceIn(top, bottom)
                val bottomEdge = hingeBottom.coerceIn(top, bottom)
                val lowerHeight = bottom - bottomEdge
                if (lowerHeight >= 300.dp || lowerHeight >= topEdge - top) top = bottomEdge else bottom = topEdge
            }
        }

        val availableWidth = right - left
        val panelHeight = bottom - top
        val fontScale = density.fontScale.coerceAtLeast(1f)
        val minimumPanelWidth = (360.dp * fontScale).coerceAtMost(500.dp)
        val trailingPanelWidth = (availableWidth * 0.5f).coerceIn(minimumPanelWidth, 500.dp)
        val fitsMediumWindow = availableWidth >= 600.dp && panelHeight >= 400.dp &&
            availableWidth - trailingPanelWidth >= 180.dp
        val panelWidth = if (availableWidth >= 800.dp || fitsMediumWindow) trailingPanelWidth else availableWidth
        val bounded = panelWidth < safeRight - safeLeft || panelHeight < safeBottom - safeTop
        val panelLeft = if (direction == LayoutDirection.Rtl) left else right - panelWidth
        val closeLabel = stringResource(R.string.close)
        val backdrop = if (bounded) {
            Modifier.clickable(
                interactionSource = null,
                indication = null,
                onClickLabel = closeLabel,
            ) { state.collapseSoft() }.background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
        } else Modifier.background(background)
        Box(Modifier.fillMaxSize().then(backdrop))

        Surface(
            modifier = Modifier.absoluteOffset(x = panelLeft, y = top).size(panelWidth, panelHeight),
            shape = if (bounded) RoundedCornerShape(20.dp) else RectangleShape,
            color = background,
        ) {
            Column(Modifier.fillMaxSize()) {
                content(panelHeight < 420.dp * fontScale, bounded)
            }
        }
    }
}

internal class QueueSelection : ViewModel() {
    var active by mutableStateOf(false)
    var uids by mutableStateOf(emptySet<Any>())
}

private data class QueueDrag(
    val player: Player,
    val shuffled: Boolean,
    val order: List<Any>,
    val moved: Any,
)

internal fun Player.queueWindowIndices(uids: Set<Any>): List<Int> {
    val timeline = currentTimeline
    val window = Timeline.Window()
    // Resolve against the current timeline in descending order so removals cannot shift later indices.
    return (timeline.windowCount - 1 downTo 0).filter { timeline.getWindow(it, window).uid in uids }
}

private fun MediaItem.queueDisplayMetadata(unknownTrack: String): MediaMetadata =
    MediaMetadata(
        id = mediaId,
        title = mediaMetadata.title?.toString()?.takeIf { it.isNotBlank() } ?: unknownTrack,
        artists = mediaMetadata.artist?.toString()?.takeIf { it.isNotBlank() }
            ?.let { listOf(MediaMetadata.Artist(id = null, name = it)) }.orEmpty(),
        duration = -1,
        thumbnailUrl = mediaMetadata.artworkUri?.toString(),
    )
