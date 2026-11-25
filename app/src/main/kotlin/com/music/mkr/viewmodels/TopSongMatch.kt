/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import java.text.Normalizer
import java.util.Locale

private val separators = Regex("[^\\p{L}\\p{M}\\p{N}+#]+")
private val apostrophes = Regex("['’]")

private val featuredCredit = Regex("\\s+(?:featuring|feat|ft)\\s+.+$")
private val versionDetail = Regex("""(?:\s+[-–—]\s+|\(|\[)([^)\]]+)[)\]]?$""")
private val featuredWord = Regex("""\b(?:featuring|ft)\b""")
private val versionWords = mapOf(
    "live" to "live", "cover" to "cover", "remix" to "remix", "acoustic" to "acoustic",
    "slowed" to "slowed", "reverb" to "slowed", "instrumental" to "instrumental",
    "karaoke" to "karaoke", "nightcore" to "nightcore", "unofficial" to "unofficial",
    "reupload" to "unofficial",
)
private val releaseDetails = setOf("original", "studio version", "album version", "official audio")
private val trailingVersion = Regex(
    "(?:\\s+(?:version|edit|mix|recording))?$",
)

private fun normalized(text: String): String =
    separators.replace(Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase(Locale.ROOT).replace(apostrophes, ""), " ")
        .trim().replace(featuredWord, "feat")

private data class VersionedTitle(val base: String, val tags: Set<String> = emptySet())

private fun versionedTitle(raw: String): VersionedTitle {
    val full = normalized(raw).replace(featuredCredit, "")
    val detail = versionDetail.find(raw)
    if (detail != null) {
        val description = normalized(detail.groupValues[1])
        val tags = description.split(' ').mapNotNull(versionWords::get).toSet()
        val base = normalized(raw.substring(0, detail.range.first)).replace(featuredCredit, "")
        if (base.isNotBlank() && (tags.isNotEmpty() || description in releaseDetails)) return VersionedTitle(base, tags)
    }

    val words = full.replace(trailingVersion, "").split(' ')
    val suffix = words.takeLastWhile { it in versionWords }
    return if (suffix.isNotEmpty() && suffix.size < words.size) {
        VersionedTitle(words.dropLast(suffix.size).joinToString(" "), suffix.mapNotNull(versionWords::get).toSet())
    } else VersionedTitle(full)
}

/** Scores provider tracks only. Album and linked-artist metadata are evidence, not proof of a studio recording. */
internal fun topSongMatch(
    query: String,
    items: List<YTItem>,
    songsOnly: Boolean = false,
    catalogNames: List<String> = emptyList(),
): SongItem? {
    val normalizedQuery = normalized(query)
    if (normalizedQuery.isBlank()) return null
    val requested = versionedTitle(query)
    val titleQuery = normalizedQuery.replace(featuredCredit, "")
    val requestedFeature = versionedTitle(normalizedQuery.substringAfter(" feat ", "")).base
    val catalogQuery = catalogNames.any { normalized(it) == titleQuery } ||
        items.any { (it is ArtistItem || it is AlbumItem) && normalized(it.title) == titleQuery }
    var best: SongItem? = null
    var bestScore = Int.MIN_VALUE

    for (song in items.filterIsInstance<SongItem>()) {
        if (!song.isTopSongCandidate(songsOnly)) continue
        val rawTitle = normalized(song.title)
        val title = versionedTitle(song.title)
        val credit = song.artists.map { normalized(it.name) }.filter { it.isNotBlank() }
        val fullCredit = credit.joinToString(" ")
        val featuredArtists = versionedTitle(rawTitle.substringAfter(" feat ", "")).base
        if (requestedFeature.isNotEmpty() &&
            !(" $fullCredit $featuredArtists ").contains(" $requestedFeature ")
        ) continue
        val queryWithoutCredit = sequenceOf(credit.firstOrNull().orEmpty(), fullCredit)
            .filter { it.isNotBlank() }
            .mapNotNull { artist ->
                when {
                    titleQuery.endsWith(" $artist") -> titleQuery.removeSuffix(" $artist")
                    titleQuery.startsWith("$artist ") -> titleQuery.removePrefix("$artist ")
                    else -> null
                }
            }.firstOrNull()
        val requestedTags = queryWithoutCredit?.let { versionedTitle(it).tags } ?: requested.tags
        val comparedTitle = queryWithoutCredit?.let { versionedTitle(it).base } ?: requested.base
        val exactRaw = titleQuery == rawTitle || titleQuery == rawTitle.replace(featuredCredit, "")
        val titleMatch = when {
            exactRaw -> 130
            comparedTitle == title.base && queryWithoutCredit != null -> 125
            comparedTitle == title.base -> 119
            catalogQuery || titleQuery in credit || titleQuery == song.album?.name?.let(::normalized) -> continue
            strongPrefix(comparedTitle, title.base) -> 80
            editsWithinLimit(comparedTitle, title.base) -> 76
            else -> continue
        }
        if (!exactRaw && requestedTags.isNotEmpty() && !title.tags.containsAll(requestedTags)) continue
        val versionScore = when {
            requestedTags.isNotEmpty() && title.tags.containsAll(requestedTags) -> 18
            title.tags.isNotEmpty() -> -16
            else -> 0
        }
        val score = titleMatch + versionScore + (if (song.album != null) 4 else 0) +
            (if (song.artists.firstOrNull()?.id != null) 2 else 0)
        if (score >= 74 && score > bestScore) {
            best = song
            bestScore = score
        }
    }
    return best
}

private fun SongItem.isTopSongCandidate(songsOnly: Boolean): Boolean =
    id.isNotBlank() && artists.any { it.name.isNotBlank() } && !isVideoSong &&
        (musicVideoType != null || album != null || songsOnly) &&
        !title.startsWith("Podcast:", ignoreCase = true)

internal fun List<YTItem>.withoutTopSong(id: String?): List<YTItem> =
    if (id == null) this else filterNot { it is SongItem && it.id == id }

private fun strongPrefix(query: String, title: String): Boolean =
    query.length >= 5 && title.startsWith("$query ") &&
        (query.contains(' ') || query.length * 5 >= title.length * 3)

private fun editsWithinLimit(query: String, title: String): Boolean {
    val limit = when {
        query.length >= 11 -> 2
        query.length >= 5 -> 1
        else -> return false
    }
    if (query.length > 100 || title.length > 100 || kotlin.math.abs(query.length - title.length) > limit) return false
    var previousPrevious = IntArray(title.length + 1)
    var previous = IntArray(title.length + 1) { it }
    var current = IntArray(title.length + 1)
    for (i in query.indices) {
        current[0] = i + 1
        for (j in title.indices) {
            current[j + 1] = minOf(
                previous[j + 1] + 1,
                current[j] + 1,
                previous[j] + if (query[i] == title[j]) 0 else 1,
            )
            if (i > 0 && j > 0 && query[i] == title[j - 1] && query[i - 1] == title[j]) {
                current[j + 1] = minOf(current[j + 1], previousPrevious[j - 1] + 1)
            }
        }
        val swap = previousPrevious
        previousPrevious = previous
        previous = current
        current = swap
    }
    return previous[title.length] <= limit
}
