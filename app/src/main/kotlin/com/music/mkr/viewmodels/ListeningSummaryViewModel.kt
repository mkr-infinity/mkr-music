/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.mkr.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject

import com.music.mkr.db.entities.Song
import kotlinx.coroutines.flow.StateFlow

data class ListeningMood(
    val title: String,
    val quote: String,
    val emoji: String,
    val tag: String
)

data class DayUsageData(
    val dayName: String,
    val timestamp: Long,
    val totalMs: Long,
    val songsMs: Long,
    val artistsMs: Long,
    val albumsMs: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListeningSummaryViewModel @Inject constructor(
    val database: MusicDatabase
) : ViewModel() {

    val weekOffset = MutableStateFlow(0)

    val listeningMood: StateFlow<ListeningMood> = weekOffset.flatMapLatest { offset ->
        val weekStart = weekStartMs(offset)
        database.mostPlayedSongs(fromTimeStamp = weekStart, limit = 25).map { songs ->
            determineMood(songs)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ListeningMood(
            title = "Your Musical Mood",
            quote = "Hit play on some tunes to discover your weekly musical personality and vibe!",
            emoji = "🎵",
            tag = "Ready to Listen"
        )
    )

    private fun determineMood(songs: List<Song>): ListeningMood {
        if (songs.isEmpty()) {
            return ListeningMood(
                title = "Your Musical Mood",
                quote = "Hit play on some tunes to discover your weekly musical personality and vibe!",
                emoji = "🎵",
                tag = "Ready to Listen"
            )
        }

        var sadScore = 0
        var hypeScore = 0
        var chillScore = 0
        var loveScore = 0
        var funnyScore = 0

        val sadWords = setOf("sad", "hurt", "cry", "crying", "tears", "lonely", "alone", "broken", "pain", "dark", "die", "dead", "miss", "grief", "goodbye", "heartbreak", "bleed", "lost", "blue", "slowed", "acoustic", "depress", "sorrow")
        val hypeWords = setOf("hype", "dance", "party", "fire", "hard", "beat", "rock", "fast", "run", "speed", "power", "wild", "jump", "drop", "bass", "trap", "rave", "remix", "super", "bang", "crazy", "rage")
        val chillWords = setOf("chill", "lofi", "lo-fi", "relax", "calm", "sleep", "dream", "night", "quiet", "breeze", "sunset", "coffee", "peace", "soft", "ambient", "cloud", "rain", "slow", "vibe")
        val loveWords = setOf("love", "lover", "loving", "heart", "baby", "kiss", "sweet", "darling", "forever", "hold", "romantic", "romance", "together", "crush", "beloved", "mine", "angel")
        val funnyWords = setOf("meme", "funny", "joke", "fun", "lol", "silly", "game", "troll", "anime", "bizarre", "parody", "cat", "dog", "monkey")

        for (song in songs) {
            val titleTokens = song.title.lowercase().split(Regex("[\\s,._\\-]+"))
            for (token in titleTokens) {
                if (sadWords.contains(token)) sadScore += 2
                if (hypeWords.contains(token)) hypeScore += 2
                if (chillWords.contains(token)) chillScore += 2
                if (loveWords.contains(token)) loveScore += 2
                if (funnyWords.contains(token)) funnyScore += 2
            }
        }

        val maxScore = maxOf(sadScore, hypeScore, chillScore, loveScore, funnyScore)
        return when {
            maxScore > 0 && sadScore == maxScore -> ListeningMood(
                title = "Your favorite songs are melancholic & deep",
                quote = "You've been leaning into raw emotional melodies and introspective lyrics. Let the music heal what words cannot.",
                emoji = "🌧️",
                tag = "Emotional & Deep"
            )
            maxScore > 0 && hypeScore == maxScore -> ListeningMood(
                title = "Your favorite songs are pure hype & energy",
                quote = "High BPM, explosive rhythm, and unstoppable momentum! Your playlist could easily power a stadium.",
                emoji = "⚡",
                tag = "High Voltage"
            )
            maxScore > 0 && chillScore == maxScore -> ListeningMood(
                title = "Your favorite songs are calm & dreamy",
                quote = "Laid-back rhythms and soothing atmospheres. You know exactly how to unwind and let the world drift away.",
                emoji = "☕",
                tag = "Chill & Lo-Fi"
            )
            maxScore > 0 && loveScore == maxScore -> ListeningMood(
                title = "Your favorite songs are sweet & romantic",
                quote = "Heartfelt lyrics and warm harmonies. Your listening sessions are bathed in pure romantic warmth.",
                emoji = "💖",
                tag = "Romantic & Warm"
            )
            maxScore > 0 && funnyScore == maxScore -> ListeningMood(
                title = "Your favorite songs are quirky & playful",
                quote = "Unfiltered fun and catchy grooves! You listen to what makes you smile without taking things too seriously.",
                emoji = "🎉",
                tag = "Playful & Vibey"
            )
            else -> ListeningMood(
                title = "Your favorite songs are eclectic & diverse",
                quote = "A masterclass in taste! You effortlessly float across different genres and moods without missing a beat.",
                emoji = "✨",
                tag = "Eclectic Taste"
            )
        }
    }

    private fun weekStartMs(offset: Int): Long {
        val monday = LocalDate.now()
            .minusWeeks(offset.toLong())
            .with(DayOfWeek.MONDAY)
        return monday.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    private val dayMs = 24L * 60 * 60 * 1000

    /** Today's total play time in ms */
    val todayPlayTimeMs: Flow<Long> = run {
        val todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val todayEnd = todayStart + dayMs
        database.getPlayTimeForDay(todayStart, todayEnd)
    }

    /** Per-day breakdown for the selected week (Mon–Sun), updates when weekOffset changes */
    val weekDailyData = weekOffset.flatMapLatest { offset ->
        val weekStart = weekStartMs(offset)
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        val dayFlows: List<Flow<DayUsageData>> = (0 until 7).map { dayIndex ->
            val from = weekStart + dayIndex * dayMs
            val to = from + dayMs
            val name = dayNames[dayIndex]
            combine(
                database.getPlayTimeForDay(from, to),
                database.getSongsPlayTimeForDay(from, to),
                database.getArtistPlayTimeForDay(from, to),
                database.getAlbumPlayTimeForDay(from, to)
            ) { total: Long, songs: Long, artists: Long, albums: Long ->
                DayUsageData(name, from, total, songs, artists, albums)
            }
        }

        combine(dayFlows[0], dayFlows[1], dayFlows[2], dayFlows[3], dayFlows[4], dayFlows[5], dayFlows[6]) {
                arr -> arr.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList<DayUsageData>())

    /** Total play time for the selected week */
    val weekTotalMs = weekDailyData
        .map { days -> days.sumOf { it.totalMs } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    fun goToPreviousWeek() { weekOffset.value++ }
    fun goToNextWeek() { if (weekOffset.value > 0) weekOffset.value-- }
    fun isCurrentWeek(): Boolean = weekOffset.value == 0
    fun setWeekFromEpoch(epochMilli: Long) {
        val selectedDate = Instant.ofEpochMilli(epochMilli).atZone(ZoneOffset.UTC).toLocalDate()
        val currentMonday = LocalDate.now().with(DayOfWeek.MONDAY)
        val selectedMonday = selectedDate.with(DayOfWeek.MONDAY)
        val weeksDiff = ChronoUnit.WEEKS.between(selectedMonday, currentMonday).toInt()
        weekOffset.value = weeksDiff.coerceAtLeast(0)
    }
}
