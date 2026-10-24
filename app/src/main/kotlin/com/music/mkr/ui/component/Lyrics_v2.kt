package com.music.mkr.ui.component

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.music.mkr.LocalDatabase
import com.music.mkr.LocalPlayerConnection
import com.music.mkr.LocalListenTogetherManager
import com.music.mkr.lyrics.LyricsTranslationHelper
import com.music.mkr.utils.rememberPreference
import com.music.mkr.constants.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.music.mkr.R
import com.music.mkr.db.entities.LyricsEntity
import com.music.mkr.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.music.mkr.lyrics.LyricsEntry
import com.music.mkr.lyrics.LyricsUtils
import com.music.mkr.models.MediaMetadata
import com.music.mkr.ui.component.MkrLyricsLine
import com.music.mkr.ui.component.LocalMenuState
import com.music.mkr.ui.component.LocalBottomSheetPageState
import com.music.mkr.ui.menu.LyricsMenu
import com.music.mkr.ui.utils.ShowOffsetDialog
import com.music.mkr.ui.theme.rememberArtworkPalette
import com.music.mkr.ui.theme.PlayerColorExtractor
import com.music.mkr.ui.theme.PlayerArtworkPalette
import com.music.mkr.ui.screens.settings.LyricsPosition
import com.music.mkr.utils.rememberEnumPreference
import androidx.compose.ui.text.style.TextOverflow
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LyricsListItemV2 {
    data class Line(val index: Int, val entry: LyricsEntry) : LyricsListItemV2()
    data class Indicator(val index: Int, val gapMs: Long, val gapStartMs: Long, val gapEndMs: Long) : LyricsListItemV2()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LyricsV2(
    mediaMetadata: MediaMetadata?,
    showLyrics: Boolean,
    positionProvider: () -> Long,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (!showLyrics) return

    val playerConnection = LocalPlayerConnection.current ?: return
    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
    
    val listenTogetherManager = LocalListenTogetherManager.current
    val isGuest = listenTogetherManager?.isInRoom == true && !listenTogetherManager.isHost

    val scrollLyrics by rememberPreference(LyricsScrollKey, true)
    
    val romanizeJapaneseLyrics by rememberPreference(LyricsRomanizeJapaneseKey, true)
    val romanizeKoreanLyrics by rememberPreference(LyricsRomanizeKoreanKey, true)
    val romanizeRussianLyrics by rememberPreference(LyricsRomanizeRussianKey, true)
    val romanizeUkrainianLyrics by rememberPreference(LyricsRomanizeUkrainianKey, true)
    val romanizeSerbianLyrics by rememberPreference(LyricsRomanizeSerbianKey, true)
    val romanizeBulgarianLyrics by rememberPreference(LyricsRomanizeBulgarianKey, true)
    val romanizeBelarusianLyrics by rememberPreference(LyricsRomanizeBelarusianKey, true)
    val romanizeKyrgyzLyrics by rememberPreference(LyricsRomanizeKyrgyzKey, true)
    val romanizeMacedonianLyrics by rememberPreference(LyricsRomanizeMacedonianKey, true)
    val romanizeCyrillicByLine by rememberPreference(LyricsRomanizeCyrillicByLineKey, false)
    val romanizeAsMain by rememberPreference(LyricsRomanizeAsMainKey, false)
    val romanizeChineseLyrics by rememberPreference(LyricsRomanizeChineseKey, true)
    val romanizeHindiLyrics by rememberPreference(LyricsRomanizeHindiKey, true)
    val romanizePunjabiLyrics by rememberPreference(LyricsRomanizePunjabiKey, true)

    val openRouterApiKey by rememberPreference(OpenRouterApiKey, "")
    val deeplApiKey by rememberPreference(DeeplApiKey, "")
    val aiProvider by rememberPreference(AiProviderKey, "OpenRouter")
    val openRouterBaseUrl by rememberPreference(OpenRouterBaseUrlKey, "https://openrouter.ai/api/v1/chat/completions")
    val openRouterModel by rememberPreference(OpenRouterModelKey, "google/gemini-2.5-flash-lite")
    val translateLanguage by rememberPreference(TranslateLanguageKey, "en")
    val translateMode by rememberPreference(TranslateModeKey, "Literal")
    val deeplFormality by rememberPreference(DeeplFormalityKey, "default")

    val translationStatus by LyricsTranslationHelper.status.collectAsState()
    val hasActiveTranslations by LyricsTranslationHelper.hasActiveTranslations.collectAsState()

    val lyricsTextSize by rememberPreference(LyricsTextSizeKey, 24f)
    val lyricsLineSpacing by rememberPreference(LyricsLineSpacingKey, 1.35f)
    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)

    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    val artworkPalette = rememberArtworkPalette(mediaMetadata)
    val adaptivePrimary = artworkPalette.textPrimary
    val lyrics = remember(currentLyrics) { currentLyrics?.lyrics?.trim() }
    val context = LocalContext.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val lyricsOffset = (currentSong?.song?.lyricsOffset ?: 0).toLong()
    val showRomanized = currentSong?.song?.romanizeLyrics ?: true

    DisposableEffect(Unit) {
        LyricsTranslationHelper.setCompositionActive(true)
        onDispose {
            LyricsTranslationHelper.setCompositionActive(false)
            LyricsTranslationHelper.cancelTranslation()
        }
    }

    val currentTrackId = mediaMetadata?.id
    LaunchedEffect(currentTrackId) {
        if (currentTrackId != null && (currentLyrics == null || currentLyrics?.id != currentTrackId)) {
            delay(300)
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    com.music.mkr.di.LyricsHelperEntryPoint::class.java
                )
                val lyricsHelper = entryPoint.lyricsHelper()
                val fetchedLyricsWithProvider = withContext(Dispatchers.IO) {
                    lyricsHelper.getLyrics(mediaMetadata)
                }
                if (mediaMetadata?.id == currentTrackId) {
                    database.query {
                        upsert(
                            LyricsEntity(
                                currentTrackId,
                                fetchedLyricsWithProvider.lyrics,
                                fetchedLyricsWithProvider.provider
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Handled gracefully, empty state will show if not found
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            lyrics == null -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = artworkPalette.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, artworkPalette.primaryAccent.copy(alpha = 0.20f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            color = artworkPalette.primaryAccent,
                            trackColor = artworkPalette.primaryAccent.copy(alpha = 0.2f),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Loading lyrics...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = artworkPalette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = mediaMetadata?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = artworkPalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            lyrics == LYRICS_NOT_FOUND || lyrics.isBlank() -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = artworkPalette.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, artworkPalette.primaryAccent.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.padding(horizontal = 28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(artworkPalette.primaryAccent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.lyrics),
                                contentDescription = null,
                                tint = artworkPalette.primaryAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = stringResource(R.string.lyrics_not_found),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = artworkPalette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No synchronized lyrics found for \"${mediaMetadata?.title ?: "this track"}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = artworkPalette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                menuState.show {
                                    LyricsMenu(
                                        lyricsProvider = { currentLyrics },
                                        songProvider = { currentSong?.song },
                                        mediaMetadataProvider = { mediaMetadata ?: MediaMetadata(id = "", title = "", artists = emptyList(), duration = 0) },
                                        onDismiss = menuState::dismiss,
                                        onShowOffsetDialog = {
                                            bottomSheetPageState.show {
                                                ShowOffsetDialog(songProvider = { currentSong?.song })
                                            }
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = artworkPalette.primaryAccent,
                                contentColor = PlayerColorExtractor.CharcoalDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lyrics Options",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            else -> {
                val lines = remember(lyrics, coroutineScope) {
                    if (lyrics.startsWith("[")) {
                        val parsedLines = LyricsUtils.parseLyrics(lyrics)

                        val isRussianLyrics = romanizeRussianLyrics && !romanizeCyrillicByLine && LyricsUtils.isRussian(lyrics)
                        val isUkrainianLyrics = romanizeUkrainianLyrics && !romanizeCyrillicByLine && LyricsUtils.isUkrainian(lyrics)
                        val isSerbianLyrics = romanizeSerbianLyrics && !romanizeCyrillicByLine && LyricsUtils.isSerbian(lyrics)
                        val isBulgarianLyrics = romanizeBulgarianLyrics && !romanizeCyrillicByLine && LyricsUtils.isBulgarian(lyrics)
                        val isBelarusianLyrics = romanizeBelarusianLyrics && !romanizeCyrillicByLine && LyricsUtils.isBelarusian(lyrics)
                        val isKyrgyzLyrics = romanizeKyrgyzLyrics && !romanizeCyrillicByLine && LyricsUtils.isKyrgyz(lyrics)
                        val isMacedonianLyrics = romanizeMacedonianLyrics && !romanizeCyrillicByLine && LyricsUtils.isMacedonian(lyrics)

                        parsedLines.map { entry ->
                            val newEntry = LyricsEntry(entry.time, entry.text, entry.words, agent = entry.agent, isBackground = entry.isBackground)

                            if (romanizeJapaneseLyrics && LyricsUtils.isJapanese(entry.text) && !LyricsUtils.isChinese(entry.text)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeJapanese(entry.text) }
                            }
                            if (romanizeKoreanLyrics && LyricsUtils.isKorean(entry.text)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeKorean(entry.text) }
                            }
                            if (romanizeRussianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isRussian(entry.text) else isRussianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeUkrainianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isUkrainian(entry.text) else isUkrainianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeSerbianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isSerbian(entry.text) else isSerbianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeBulgarianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isBulgarian(entry.text) else isBulgarianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeBelarusianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isBelarusian(entry.text) else isBelarusianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeKyrgyzLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isKyrgyz(entry.text) else isKyrgyzLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeMacedonianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isMacedonian(entry.text) else isMacedonianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(entry.text) }
                            }
                            else if (romanizeChineseLyrics && LyricsUtils.isChinese(entry.text)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeChinese(entry.text) }
                            }
                            else if (romanizeHindiLyrics && LyricsUtils.isHindi(entry.text)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeHindi(entry.text) }
                            }
                            else if (romanizePunjabiLyrics && LyricsUtils.isPunjabi(entry.text)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizePunjabi(entry.text) }
                            }
                            newEntry
                        }
                    } else {
                        val isRussianLyrics = romanizeRussianLyrics && !romanizeCyrillicByLine && LyricsUtils.isRussian(lyrics)
                        val isUkrainianLyrics = romanizeUkrainianLyrics && !romanizeCyrillicByLine && LyricsUtils.isUkrainian(lyrics)
                        val isSerbianLyrics = romanizeSerbianLyrics && !romanizeCyrillicByLine && LyricsUtils.isSerbian(lyrics)
                        val isBulgarianLyrics = romanizeBulgarianLyrics && !romanizeCyrillicByLine && LyricsUtils.isBulgarian(lyrics)
                        val isBelarusianLyrics = romanizeBelarusianLyrics && !romanizeCyrillicByLine && LyricsUtils.isBelarusian(lyrics)
                        val isKyrgyzLyrics = romanizeKyrgyzLyrics && !romanizeCyrillicByLine && LyricsUtils.isKyrgyz(lyrics)
                        val isMacedonianLyrics = romanizeMacedonianLyrics && !romanizeCyrillicByLine && LyricsUtils.isMacedonian(lyrics)

                        lyrics.split("\n").mapIndexed { index, line ->
                            val newEntry = LyricsEntry(index * 100L, line, emptyList())

                            if (romanizeJapaneseLyrics && LyricsUtils.isJapanese(line) && !LyricsUtils.isChinese(line)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeJapanese(line) }
                            }
                            if (romanizeKoreanLyrics && LyricsUtils.isKorean(line)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeKorean(line) }
                            }
                            if (romanizeRussianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isRussian(line) else isRussianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeUkrainianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isUkrainian(line) else isUkrainianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeSerbianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isSerbian(line) else isSerbianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeBulgarianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isBulgarian(line) else isBulgarianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeBelarusianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isBelarusian(line) else isBelarusianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeKyrgyzLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isKyrgyz(line) else isKyrgyzLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeMacedonianLyrics && (if (romanizeCyrillicByLine) LyricsUtils.isMacedonian(line) else isMacedonianLyrics)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeCyrillic(line) }
                            }
                            else if (romanizeChineseLyrics && LyricsUtils.isChinese(line)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeChinese(line) }
                            }
                            else if (romanizeHindiLyrics && LyricsUtils.isHindi(line)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizeHindi(line) }
                            }
                            else if (romanizePunjabiLyrics && LyricsUtils.isPunjabi(line)) {
                                coroutineScope.launch { newEntry.romanizedTextFlow.value = LyricsUtils.romanizePunjabi(line) }
                            }
                            newEntry
                        }
                    }
                }
                
                LaunchedEffect(lines, currentLyrics, translateLanguage, translateMode) {
                    if (lines.isNotEmpty() && currentLyrics != null) {
                        LyricsTranslationHelper.loadTranslationsFromDatabase(
                            lyrics = lines,
                            lyricsEntity = currentLyrics,
                            targetLanguage = translateLanguage,
                            mode = translateMode
                        )
                    }
                }
                
                LaunchedEffect(showLyrics, lines.size) {
                    LyricsTranslationHelper.manualTrigger.collect {
                        val effectiveApiKey = if (aiProvider == "DeepL") deeplApiKey else openRouterApiKey
                        if (showLyrics && lines.isNotEmpty() && effectiveApiKey.isNotBlank()) {
                            LyricsTranslationHelper.translateLyrics(
                                lyrics = lines,
                                targetLanguage = translateLanguage,
                                apiKey = openRouterApiKey,
                                baseUrl = openRouterBaseUrl,
                                model = openRouterModel,
                                mode = translateMode,
                                scope = coroutineScope,
                                context = context,
                                provider = aiProvider,
                                deeplApiKey = deeplApiKey,
                                deeplFormality = deeplFormality,
                                useStreaming = true,
                                songId = mediaMetadata?.id ?: "",
                                database = database
                            )
                        } else if (effectiveApiKey.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.ai_api_key_required), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    LyricsTranslationHelper.clearTranslationsTrigger.collect {
                        lines.forEach { it.translatedTextFlow.value = null }
                    }
                }

                // If plain text (not synced), just render a static list with MKR styling
                if (lines.isNotEmpty() && lines.all { it.time == 0L }) {
                    val staticDismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                    val staticBottomClearance = maxOf(contentPadding.calculateBottomPadding(), staticDismissedBound) + 24.dp
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 32.dp,
                            bottom = staticBottomClearance,
                            start = 20.dp,
                            end = 20.dp
                        )
                    ) {
                        item {
                            LyricsHeader(
                                title = mediaMetadata?.title.orEmpty(),
                                artist = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                                provider = currentLyrics?.provider,
                                palette = artworkPalette
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(lines.size) { index ->
                            MkrLyricsLine(
                                entry = lines[index],
                                nextEntryTime = null,
                                effectivePlaybackPosition = 0L,
                                isSynced = false,
                                isActive = false,
                                distanceFromCurrent = 0,
                                lyricsTextPosition = lyricsTextPosition,
                                palette = artworkPalette,
                                showRomanized = showRomanized,
                                showTranslated = hasActiveTranslations,
                                textSize = lyricsTextSize,
                                lineSpacing = lyricsLineSpacing,
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                    return
                }

                // lazy list state moved below to allow initializing with playback line index

                var currentPosition by remember { mutableLongStateOf(positionProvider()) }
                
                LaunchedEffect(Unit) {
                    var lastPlayerPos = playerConnection.player.currentPosition
                    var lastUpdateTime = System.currentTimeMillis()
                    
                    while (true) {
                       withFrameMillis {
                            val now = System.currentTimeMillis()
                            val playerPos = playerConnection.player.currentPosition
                            val isPlaying = playerConnection.player.isPlaying
                            val providerPos = positionProvider()
                            
                            if (kotlin.math.abs(providerPos - playerPos) > 1000L) {
                                currentPosition = providerPos
                                lastPlayerPos = playerPos
                                lastUpdateTime = now
                            } else {
                                if (playerPos != lastPlayerPos || !isPlaying) {
                                    lastPlayerPos = playerPos
                                    lastUpdateTime = now
                                }
                                val elapsed = now - lastUpdateTime
                                currentPosition = lastPlayerPos + (if (isPlaying) elapsed else 0L)
                            }
                        }
                    }
                }

                // Scroll anchor: single line index used to keep the list auto-scrolling
                val currentLineIndex by remember(lines, lyricsOffset) {
                    derivedStateOf { LyricsUtils.findCurrentLineIndex(lines, currentPosition + lyricsOffset) }
                }

                // Active set: all lines currently being sung (handles v1 + bg sharing same timestamp)
                val activeLineIndices by remember(lines, lyricsOffset) {
                    derivedStateOf { LyricsUtils.findActiveLineIndices(lines, currentPosition + lyricsOffset) }
                }

                val mergedLyricsList = remember(lines) {
                    val result = mutableListOf<LyricsListItemV2>()
                    if (lines.isEmpty()) return@remember result

                    lines.forEachIndexed { i, entry ->
                        if (entry.text.isNotBlank()) {
                            result.add(LyricsListItemV2.Line(i, entry))
                        }
                        if (i < lines.size - 1) {
                            val nextStart = lines[i + 1].time
                            val currentEnd = if (!entry.words.isNullOrEmpty()) {
                                (entry.words.last().endTime * 1000).toLong()
                            } else if (entry.text.isBlank()) {
                                entry.time
                            } else {
                                null
                            }

                            if (currentEnd != null && currentEnd < nextStart) {
                                val gap = nextStart - currentEnd
                                if (gap > 4000L) {
                                    result.add(
                                        LyricsListItemV2.Indicator(
                                            i,
                                            gap,
                                            currentEnd,
                                            nextStart
                                        )
                                    )
                                }
                            }
                        }
                    }
                    result
                }

                val headerOffset = 1

                val initialIndex = remember(currentLineIndex, lines) {
                    maxOf(0, currentLineIndex - 1) + headerOffset
                }

                // Subscribed synced lyrics
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                val isDragged by listState.interactionSource.collectIsDraggedAsState()
                var isAutoScrollEnabled by remember { mutableStateOf(scrollLyrics) }
                var lastManualScrollTime by remember { mutableLongStateOf(0L) }

                LaunchedEffect(scrollLyrics) { 
                    if (!isDragged && !isGuest) {
                        isAutoScrollEnabled = scrollLyrics 
                    }
                }

                LaunchedEffect(isDragged) {
                    if (isDragged && !isGuest) {
                        isAutoScrollEnabled = false
                        lastManualScrollTime = System.currentTimeMillis()
                    }
                }

                LaunchedEffect(isAutoScrollEnabled, lastManualScrollTime) {
                    if (!isAutoScrollEnabled) {
                        delay(4000L)
                        isAutoScrollEnabled = true
                    }
                }

                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val estimatedLyricsAreaHeight = screenHeight * 0.6f

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val dismissedBottomBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                    val effectiveBottomClearance = maxOf(contentPadding.calculateBottomPadding(), dismissedBottomBound)
                    val targetTopRatio = 0.32f
                    val topPadding = (estimatedLyricsAreaHeight * targetTopRatio) + contentPadding.calculateTopPadding()
                    val bottomPadding = (estimatedLyricsAreaHeight * (1f - targetTopRatio)) + effectiveBottomClearance

                    // Smooth Spring Scroll Physics (Anchored comfortably at ~32% Top Viewport Position)
                    LaunchedEffect(currentLineIndex, isAutoScrollEnabled, mergedLyricsList) {
                        if (currentLineIndex != -1 && isAutoScrollEnabled && !isGuest) {
                            val actualListIndex = mergedLyricsList.indexOfFirst { 
                                it is LyricsListItemV2.Line && it.index == currentLineIndex 
                            }.takeIf { it != -1 } ?: currentLineIndex
                            val targetIndex = maxOf(0, actualListIndex) + headerOffset
                            try {
                                val itemInfo =
                                    listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
                                if (itemInfo != null) {
                                    val viewportHeight =
                                        listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                                    val targetTopOffset = viewportHeight * targetTopRatio
                                    val center =
                                        listState.layoutInfo.viewportStartOffset + targetTopOffset
                                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                                    val offset = itemCenter - center

                                    if (kotlin.math.abs(offset) > 5) {
                                        listState.animateScrollBy(
                                            value = offset.toFloat(),
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                            )
                                        )
                                    }
                                } else {
                                    val distance =
                                        kotlin.math.abs(targetIndex - listState.firstVisibleItemIndex)
                                    if (distance > 15) {
                                        listState.scrollToItem(targetIndex)
                                    } else {
                                        listState.animateScrollToItem(targetIndex, 0)
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy =
                                    androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        0.10f to Color.Black,
                                        0.85f to Color.Black,
                                        1.0f to Color.Transparent
                                    ),
                                    blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                                )
                            },
                        // Top-anchored content padding
                        contentPadding = PaddingValues(
                            top = topPadding,
                            bottom = bottomPadding
                        )
                    ) {
                        item {
                            LyricsHeader(
                                title = mediaMetadata?.title.orEmpty(),
                                artist = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                                provider = currentLyrics?.provider,
                                palette = artworkPalette
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(mergedLyricsList.size) { listIndex ->
                            val item = mergedLyricsList[listIndex]
                            when (item) {
                                is LyricsListItemV2.Line -> {
                                    val index = item.index
                                    val line = item.entry
                                    val isActive = index in activeLineIndices
                                    val distanceFromActive =
                                        kotlin.math.abs(index - currentLineIndex)

                                    val nextStart =
                                        (mergedLyricsList.getOrNull(listIndex + 1) as? LyricsListItemV2.Line)?.entry?.time

                                    MkrLyricsLine(
                                        entry = line,
                                        nextEntryTime = nextStart,
                                        effectivePlaybackPosition = currentPosition,
                                        lyricsOffset = lyricsOffset,
                                        isSynced = true,
                                        isActive = isActive,
                                        distanceFromCurrent = distanceFromActive,
                                        lyricsTextPosition = lyricsTextPosition,
                                        palette = artworkPalette,
                                        showRomanized = showRomanized,
                                        showTranslated = hasActiveTranslations,
                                        textSize = lyricsTextSize,
                                        lineSpacing = lyricsLineSpacing,
                                        onClick = {
                                            if (!isGuest) {
                                                playerConnection.player.seekTo(line.time - lyricsOffset)
                                                isAutoScrollEnabled = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )
                                }

                                is LyricsListItemV2.Indicator -> {
                                    val effectivePos = currentPosition + lyricsOffset
                                    val indicatorVisible =
                                        effectivePos >= item.gapStartMs && effectivePos <= item.gapEndMs - 650L
                                    IntervalIndicator(
                                        gapStartMs = item.gapStartMs,
                                        gapEndMs = item.gapEndMs - 650L,
                                        currentPositionMs = effectivePos,
                                        visible = indicatorVisible,
                                        color = artworkPalette.primaryAccent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(1f)
                            .padding(top = 56.dp)
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                when (val status = translationStatus) {
                                    is LyricsTranslationHelper.TranslationStatus.Translating -> {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = stringResource(R.string.ai_translating_lyrics),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                    is LyricsTranslationHelper.TranslationStatus.Error -> {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.error),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = status.message,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                    is LyricsTranslationHelper.TranslationStatus.Success -> {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.check),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = stringResource(R.string.ai_lyrics_translated),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
                                    }
                                    is LyricsTranslationHelper.TranslationStatus.Idle -> {
                                        // No status display
                                    }
                                }
                            }
                        }
                    }

                    val resumeContentColor = if (PlayerColorExtractor.calculateLuminance(artworkPalette.primaryAccent) > 0.45f) {
                        PlayerColorExtractor.CharcoalDark
                    } else {
                        Color.White
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .imePadding()
                            .padding(bottom = effectiveBottomClearance + 16.dp)
                    ) {
                        AnimatedVisibility(
                            visible = !isAutoScrollEnabled,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            Button(
                                onClick = {
                                    isAutoScrollEnabled = true
                                    if (currentLineIndex != -1) {
                                        coroutineScope.launch {
                                            try {
                                                val actualListIndex = mergedLyricsList.indexOfFirst {
                                                    it is LyricsListItemV2.Line && it.index == currentLineIndex
                                                }.takeIf { it != -1 } ?: currentLineIndex
                                                val targetIndex = maxOf(0, actualListIndex) + headerOffset
                                                listState.animateScrollToItem(targetIndex, 0)
                                            } catch (e: Exception) {
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = artworkPalette.primaryAccent,
                                    contentColor = resumeContentColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.sync),
                                    contentDescription = "Resume Sync",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resume Sync",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsHeader(
    title: String,
    artist: String,
    provider: String?,
    palette: PlayerArtworkPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!provider.isNullOrBlank() && provider != "Unknown") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surfaceElevated.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.25f)),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(palette.primaryAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lyrics via $provider",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (artist.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = artist,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun IntervalIndicator(
    gapStartMs: Long,
    gapEndMs: Long,
    currentPositionMs: Long,
    visible: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(0f) }
    val rowHeightPx = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            rowHeightPx.animateTo(1f, tween(200))
            alpha.animateTo(1f, tween(200))
        } else {
            alpha.animateTo(0f, tween(200))
            rowHeightPx.animateTo(0f, tween(200))
        }
    }

    val targetHeightDp = 72.dp

    val progress = if (gapEndMs > gapStartMs) {
        ((currentPositionMs - gapStartMs).toFloat() / (gapEndMs - gapStartMs).toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "intervalProgress"
    )

    Box(
        modifier = modifier
            .height(targetHeightDp * rowHeightPx.value)
            .padding(top = 16.dp * rowHeightPx.value)
            .graphicsLayer {
                this.alpha = alpha.value
                this.clip = true
            },
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .size(36.dp)
                .alpha(alpha.value),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}
