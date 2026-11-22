/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.viewmodels

import com.music.innertube.models.Album
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.SongItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopSongMatchTest {
    private fun track(id: String, title: String, artist: String = "Nora Vale") = SongItem(
        id, title, listOf(Artist(artist, artist)),
        album = Album("Night Letters", "album"),
        thumbnail = "",
    )

    @Test
    fun exactTitleAndArtistOutrankProviderOrder() {
        val other = track("other", "Silver Rain", "East Hall")
        val wanted = track("wanted", "Silver Rain")
        assertEquals(wanted, topSongMatch("  NORA VALE — silver rain!  ", listOf(other, wanted)))
    }

    @Test
    fun defaultsToStudioAndRespectsExplicitVersions() {
        val live = track("live", "Silver Rain (Live at the Bridge)")
        val remix = track("remix", "Silver Rain – Echo Remix")
        val studio = track("studio", "Silver Rain")
        val results = listOf(live, remix, studio)
        assertEquals(studio, topSongMatch("silver rain", results))
        assertEquals(live, topSongMatch("silver rain live", results))
        assertEquals(remix, topSongMatch("nora vale silver rain remix", results))
        assertNull(topSongMatch("silver rain karaoke", results))
    }

    @Test
    fun recognizesMinorTyposMultilingualTitlesAndFeaturingCredits() {
        val wanted = track("t", "Silver Rain")
        assertEquals(wanted, topSongMatch("Silvre Rain", listOf(wanted)))
        val multilingual = track("m", "夜の雨")
        assertEquals(multilingual, topSongMatch("夜の雨！", listOf(multilingual)))
        val guest = track("g", "Silver Rain (featuring June)")
        assertEquals(guest, topSongMatch("silver rain ft. June", listOf(wanted, guest)))
    }

    @Test
    fun videoAndWeakOrCatalogQueriesStayOutOfFeaturedPosition() {
        val song = track("track", "Silver Rain")
        val video = song.copy(id = "video", musicVideoType = "MUSIC_VIDEO_TYPE_OMV")
        val artist = ArtistItem("artist", "Nora Vale", null)
        val album = AlbumItem("album", "playlist", title = "Silver", artists = null, thumbnail = "")
        assertEquals(song, topSongMatch("Silver Rain", listOf(video, song)))
        assertNull(topSongMatch("Silver Rain", listOf(video)))
        assertNull(topSongMatch("Nora Vale", listOf(song, artist)))
        assertNull(topSongMatch("Silver", listOf(song, album)))
        assertNull(topSongMatch("blue skies", listOf(song)))
        assertNull(topSongMatch("  ", listOf(song)))
    }

    @Test
    fun deduplicationUsesTheTrackIdAndPreservesDifferentRecordings() {
        val first = track("one", "Silver Rain")
        val second = track("two", "Silver Rain")
        val collection = ArtistItem("one", "Nora Vale", null)
        assertEquals(listOf(second, collection), listOf(first, second, first, collection).withoutTopSong("one"))
    }

    @Test
    fun staleSuggestionsAreHiddenWhileTheCurrentQueryLoads() {
        val old = SearchSuggestionViewState(query = "Silver", suggestions = listOf("Silver Rain"), isFromLink = true)
        assertEquals(old, old.forQuery("Silver"))
        val pending = old.forQuery("River")
        assertEquals("River", pending.query)
        assertTrue(pending.suggestions.isEmpty())
        assertEquals(false, pending.isFromLink)
    }
}
