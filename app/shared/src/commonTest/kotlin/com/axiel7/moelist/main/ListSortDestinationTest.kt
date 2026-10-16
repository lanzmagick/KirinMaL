package com.axiel7.moelist.main

import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListSortDestinationTest {
    @Test
    fun loggedInAnimeAndMangaTabsExposeTheirOriginalDropdown() {
        assertEquals(MediaType.ANIME, listSortMediaType(Route.Tab.Anime(MediaType.ANIME), true))
        assertEquals(MediaType.MANGA, listSortMediaType(Route.Tab.Manga(MediaType.MANGA), true))
    }

    @Test
    fun restoredParameterizedTabRoutesStillExposeTheDropdown() {
        val anime = Route.Tab.Anime(MediaType.ANIME)
        val manga = Route.Tab.Manga(MediaType.MANGA)
        assertEquals(MediaType.ANIME, listSortMediaType(anime.copy(), true))
        assertEquals(MediaType.MANGA, listSortMediaType(manga.copy(), true))
    }

    @Test
    fun loginScreensDoNotExposeListControls() {
        assertNull(listSortMediaType(Route.Tab.Anime(MediaType.ANIME), false))
        assertNull(listSortMediaType(Route.Tab.Manga(MediaType.MANGA), false))
    }

    @Test
    fun otherDestinationsKeepTheirExistingControls() {
        val routes = listOf(
            null,
            Route.Tab.Home,
            Route.Tab.More,
            Route.Search(MediaType.ANIME),
            Route.Search(MediaType.MANGA),
            Route.MediaDetails(MediaType.ANIME, 1),
            Route.MediaDetails(MediaType.MANGA, 2),
            Route.ReadingManga,
            Route.WatchingAnime,
            Route.Settings,
        )
        routes.forEach { assertNull(listSortMediaType(it, true), "Unexpected dropdown on $it") }
    }
}
