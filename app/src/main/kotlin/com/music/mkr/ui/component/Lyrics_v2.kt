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
import androidx.compose.foundation.lazy.items
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
import com.music.mkr.lyrics.LyricsProviderRegistry
import com.music.mkr.lyrics.LyricsResult
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
    val lyricsAnimationStyle by rememberEnumPreference(LyricsAnimationStyleKey, defaultValue = LyricsAnimationStyle.APPLE_V2)
    val lyricsGlowEffect by rememberPreference(LyricsGlowEffectKey, defaultValue = true)
    val lyricsClick by rememberPreference(LyricsClickKey, defaultValue = true)
    val appleMusicLyricsBlur by rememberPreference(AppleMusicLyricsBlurKey, defaultValue = true)
    val lyricsStandardBlur by rememberPreference(LyricsStandardBlurKey, defaultValue = false)

    val blurLyrics = lyricsStandardBlur || (appleMusicLyricsBlur && (
        lyricsAnimationStyle == LyricsAnimationStyle.APPLE ||
        lyricsAnimationStyle == LyricsAnimationStyle.APPLE_V2 ||
        lyricsAnimationStyle == LyricsAnimationStyle.MKRMUSIC_1 ||
        lyricsAnimationStyle == LyricsAnimationStyle.LYRICS_V2 ||
        lyricsAnimationStyle == LyricsAnimationStyle.FADE ||
        lyricsAnimationStyle == LyricsAnimationStyle.GLOW ||
        lyricsAnimationStyle == LyricsAnimationStyle.SLIDE ||
        lyricsAnimationStyle == LyricsAnimationStyle.METRO_LYRICS
    ))

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
    var localRomanizedOverride by remember(mediaMetadata?.id) { mutableStateOf<Boolean?>(null) }
    val showRomanized = localRomanizedOverride ?: currentSong?.song?.romanizeLyrics ?: true

    val onToggleRomanize: () -> Unit = remember(currentSong?.song, mediaMetadata?.id, showRomanized) {
        {
            val nextVal = !showRomanized
            localRomanizedOverride = nextVal
            coroutineScope.launch(Dispatchers.IO) {
                val song = currentSong?.song
                if (song != null) {
                    database.query {
                        upsert(song.copy(romanizeLyrics = nextVal))
                    }
                } else {
                    val songId = mediaMetadata?.id
                    if (songId != null) {
                        var existing = database.getSongByIdBlocking(songId)
                        if (existing == null) {
                            mediaMetadata?.let { meta -> database.insert(meta) }
                            existing = database.getSongByIdBlocking(songId)
                        }
                        val toSave = existing?.song ?: mediaMetadata?.toSongEntity()
                        toSave?.let { entity ->
                            database.query {
                                upsert(entity.copy(romanizeLyrics = nextVal))
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        LyricsTranslationHelper.setCompositionActive(true)
        onDispose {
            LyricsTranslationHelper.setCompositionActive(false)
            LyricsTranslationHelper.cancelTranslation()
        }
    }

    val currentTrackId = mediaMetadata?.id
    var showProviderSelectorDialog by remember { mutableStateOf(false) }
    val availableProviders = remember { mutableStateListOf<LyricsResult>() }
    var isLoadingProviders by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrackId) {
        availableProviders.clear()
        val cur = currentLyrics
        if (cur != null && cur.id == currentTrackId && cur.lyrics.isNotBlank() && cur.lyrics != LYRICS_NOT_FOUND) {
            val pName = cur.provider.ifBlank { "Current" }
            availableProviders.add(LyricsResult(pName, cur.lyrics))
        }
    }

    LaunchedEffect(showProviderSelectorDialog, currentTrackId) {
        if (showProviderSelectorDialog && mediaMetadata != null) {
            val trackId = mediaMetadata.id
            val cur = currentLyrics
            if (cur != null && cur.id == trackId && cur.lyrics.isNotBlank() && cur.lyrics != LYRICS_NOT_FOUND) {
                if (availableProviders.none { it.providerName.equals(cur.provider, ignoreCase = true) }) {
                    availableProviders.add(LyricsResult(cur.provider, cur.lyrics))
                }
            }
            isLoadingProviders = true
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    com.music.mkr.di.LyricsHelperEntryPoint::class.java
                )
                val lyricsHelper = entryPoint.lyricsHelper()
                withContext(Dispatchers.IO) {
                    lyricsHelper.getAllLyrics(
                        mediaId = trackId,
                        songTitle = mediaMetadata.title,
                        songArtists = mediaMetadata.artists.joinToString { it.name },
                        duration = mediaMetadata.duration,
                        album = mediaMetadata.album?.title
                    ) { result ->
                        if (result.lyrics.isNotBlank() && result.lyrics != LYRICS_NOT_FOUND) {
                            if (availableProviders.none { it.providerName.equals(result.providerName, ignoreCase = true) }) {
                                availableProviders.add(result)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                isLoadingProviders = false
            }
        }
    }
    LaunchedEffect(currentTrackId) {
        if (currentTrackId != null) {
            val existing = currentLyrics
            if (existing != null && existing.id == currentTrackId && existing.lyrics.isNotBlank() && existing.lyrics != LYRICS_NOT_FOUND) {
                // Room database already loaded valid lyrics, avoid duplicate fetching
                return@LaunchedEffect
            }
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
                if (mediaMetadata?.id == currentTrackId &&
                    fetchedLyricsWithProvider.lyrics.isNotBlank() &&
                    fetchedLyricsWithProvider.lyrics != LYRICS_NOT_FOUND
                ) {
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

                // If plain text (not synced), render with gentle active indicator fallback
                val hasSyncedTimestamps = remember(lines) { lines.count { it.time > 0L } > 1 }
                if (lines.isNotEmpty() && !hasSyncedTimestamps) {
                    val totalDurationMs = (mediaMetadata?.duration ?: 0) * 1000L
                    val activeUnsyncedIndex = if (totalDurationMs > 0L) {
                        ((currentPosition.toFloat() / totalDurationMs) * lines.size).toInt().coerceIn(0, lines.lastIndex)
                    } else {
                        -1
                    }

                    val staticDismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                    val staticBottomClearance = maxOf(contentPadding.calculateBottomPadding(), staticDismissedBound) + 24.dp
                    val unsyncedListState = rememberLazyListState()

                    LaunchedEffect(activeUnsyncedIndex, scrollLyrics) {
                        if (scrollLyrics && activeUnsyncedIndex >= 0 && !isGuest) {
                            try {
                                unsyncedListState.animateScrollToItem(maxOf(0, activeUnsyncedIndex))
                            } catch (e: Exception) {
                            }
                        }
                    }

                    LazyColumn(
                        state = unsyncedListState,
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
                                isSynced = false,
                                palette = artworkPalette,
                                isRomanized = showRomanized,
                                onToggleRomanize = onToggleRomanize,
                                onProviderClick = { showProviderSelectorDialog = true }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(lines.size) { index ->
                            val isCurrent = index == activeUnsyncedIndex
                            val nextTime = if (totalDurationMs > 0L) {
                                ((index + 1).toFloat() / lines.size * totalDurationMs).toLong()
                            } else null
                            val lineTime = if (totalDurationMs > 0L) {
                                (index.toFloat() / lines.size * totalDurationMs).toLong()
                            } else 0L

                            MkrLyricsLine(
                                entry = lines[index].copy(time = lineTime),
                                nextEntryTime = nextTime,
                                effectivePlaybackPosition = currentPosition,
                                lyricsOffset = lyricsOffset,
                                isSynced = false,
                                isActive = isCurrent,
                                distanceFromCurrent = if (activeUnsyncedIndex >= 0) kotlin.math.abs(index - activeUnsyncedIndex) else 0,
                                lyricsTextPosition = lyricsTextPosition,
                                palette = artworkPalette,
                                showRomanized = showRomanized,
                                showTranslated = hasActiveTranslations,
                                textSize = lyricsTextSize,
                                lineSpacing = lyricsLineSpacing,
                                lyricsAnimationStyle = lyricsAnimationStyle,
                                glowEffectEnabled = lyricsGlowEffect,
                                blurEnabled = blurLyrics,
                                onClick = {
                                    if (lyricsClick && !isGuest && totalDurationMs > 0L) {
                                        val targetSeek = (index.toFloat() / lines.size * totalDurationMs).toLong()
                                        playerConnection.player.seekTo(targetSeek)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                    return
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
                                isSynced = hasSyncedTimestamps,
                                palette = artworkPalette,
                                isRomanized = showRomanized,
                                onToggleRomanize = onToggleRomanize,
                                onProviderClick = { showProviderSelectorDialog = true }
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

                                    val nextStart = (listIndex + 1 until mergedLyricsList.size)
                                        .firstNotNullOfOrNull { (mergedLyricsList[it] as? LyricsListItemV2.Line)?.entry?.time }
                                        ?: lines.getOrNull(index + 1)?.time

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
                                        lyricsAnimationStyle = lyricsAnimationStyle,
                                        glowEffectEnabled = lyricsGlowEffect,
                                        blurEnabled = blurLyrics,
                                        onClick = {
                                            if (lyricsClick && !isGuest) {
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

    if (showProviderSelectorDialog && mediaMetadata != null) {
        LyricsProviderSelectorDialog(
            currentProvider = currentLyrics?.provider,
            availableProviders = availableProviders,
            isLoading = isLoadingProviders,
            palette = artworkPalette,
            songTitle = mediaMetadata.title,
            songArtist = mediaMetadata.artists.joinToString { it.name },
            onSelectProvider = { selected ->
                coroutineScope.launch(Dispatchers.IO) {
                    database.query {
                        upsert(
                            LyricsEntity(
                                id = mediaMetadata.id,
                                lyrics = selected.lyrics,
                                provider = selected.providerName
                            )
                        )
                    }
                }
                showProviderSelectorDialog = false
            },
            onDismiss = { showProviderSelectorDialog = false }
        )
    }
}

@Composable
private fun LyricsHeader(
    title: String,
    artist: String,
    provider: String?,
    palette: PlayerArtworkPalette,
    isSynced: Boolean = true,
    isRomanized: Boolean? = null,
    onToggleRomanize: (() -> Unit)? = null,
    onProviderClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            if (!isSynced) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceElevated.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.25f)),
                ) {
                    Text(
                        text = "Synced lyrics unavailable for this track",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (!provider.isNullOrBlank() && provider != "Unknown") {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            if (!provider.isNullOrBlank() && provider != "Unknown") {
                val displayName = LyricsProviderRegistry.getDisplayName(provider)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceElevated.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.25f)),
                    modifier = if (onProviderClick != null) Modifier.clickable(onClick = onProviderClick) else Modifier
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lyrics),
                            contentDescription = "Lyrics provider",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Lyrics via $displayName",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (onProviderClick != null) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                painter = painterResource(R.drawable.expand_more),
                                contentDescription = "Change provider",
                                tint = palette.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            if (isRomanized != null && onToggleRomanize != null) {
                if (!provider.isNullOrBlank() && provider != "Unknown") {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isRomanized) palette.primaryAccent.copy(alpha = 0.22f) else palette.surfaceElevated.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, if (isRomanized) palette.primaryAccent else palette.primaryAccent.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable(onClick = onToggleRomanize)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.language_korean_latin),
                            contentDescription = "Toggle Phonetic Romanization",
                            tint = if (isRomanized) palette.primaryAccent else palette.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRomanized) "Phonetics ON" else "Phonetics OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRomanized) palette.primaryAccent else palette.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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

@Composable
private fun LyricsProviderSelectorDialog(
    currentProvider: String?,
    availableProviders: List<LyricsResult>,
    isLoading: Boolean,
    palette: PlayerArtworkPalette,
    songTitle: String,
    songArtist: String,
    onSelectProvider: (LyricsResult) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.surfaceElevated,
            border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.22f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(palette.primaryAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lyrics),
                            contentDescription = null,
                            tint = palette.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose Lyrics Provider",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        if (songTitle.isNotBlank()) {
                            Text(
                                text = songTitle + if (songArtist.isNotBlank()) " - $songArtist" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provider list
                if (availableProviders.isEmpty() && isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = palette.primaryAccent,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching available providers...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                    }
                } else if (availableProviders.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No providers found with lyrics for this track",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableProviders, key = { it.providerName }) { item ->
                            val isSelected = currentProvider != null &&
                                    item.providerName.equals(currentProvider, ignoreCase = true)
                            val syncType = try {
                                LyricsUtils.getSyncType(item.lyrics)
                            } catch (_: Exception) {
                                LyricsUtils.SyncType.NONE
                            }
                            val displayName = LyricsProviderRegistry.getDisplayName(item.providerName)

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) palette.primaryAccent.copy(alpha = 0.16f)
                                else palette.surfaceElevated.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) palette.primaryAccent.copy(alpha = 0.6f)
                                    else palette.primaryAccent.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectProvider(item) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (syncType == LyricsUtils.SyncType.WORD || syncType == LyricsUtils.SyncType.LINE)
                                                R.drawable.sync
                                            else
                                                R.drawable.lyrics
                                        ),
                                        contentDescription = null,
                                        tint = if (isSelected) palette.primaryAccent else palette.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = palette.textPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            val badgeText = when (syncType) {
                                                LyricsUtils.SyncType.WORD -> "Word-by-word synced"
                                                LyricsUtils.SyncType.LINE -> "Line synced"
                                                else -> "Plain text"
                                            }
                                            Text(
                                                text = badgeText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.textSecondary
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            painter = painterResource(R.drawable.check),
                                            contentDescription = "Selected",
                                            tint = palette.primaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading && availableProviders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = palette.primaryAccent,
                            strokeWidth = 1.8.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Checking other providers...",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Close",
                            color = palette.primaryAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
