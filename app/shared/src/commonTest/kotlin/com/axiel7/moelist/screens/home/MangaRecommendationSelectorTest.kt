package com.axiel7.moelist.screens.home

import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.selectMangaRecommendations
import com.axiel7.moelist.data.model.media.BasicMyListStatus
import com.axiel7.moelist.data.model.media.ListStatusDto
import kotlin.test.Test
import kotlin.test.assertEquals

class MangaRecommendationSelectorTest {
    private fun recommendation(id: Int, votes: Int = 1, nsfw: String? = "white") =
        Recommendations(MangaNode(id, "Manga $id", nsfw = nsfw), votes)

    @Test
    fun combinesVotesAcrossSourcesWithoutDuplicateTitles() {
        val result = selectMangaRecommendations(listOf(
            recommendation(1, 9), recommendation(2, 3),
            recommendation(2, 7), recommendation(3, 0),
        ), emptySet(), showAdult = true, limit = 25)
        assertEquals(listOf(2, 1, 3), result.map { it.id })
    }

    @Test
    fun excludesOwnedTitlesAndInvalidEntries() {
        val alreadyReading = Recommendations(
            MangaNode(3, "Already reading", myListStatus = BasicMyListStatus(ListStatusDto.READING)), 50,
        )
        val blank = Recommendations(MangaNode(4, "  "), 50)
        val result = selectMangaRecommendations(listOf(
            recommendation(0), recommendation(1), recommendation(2), alreadyReading, blank,
        ), setOf(1), showAdult = true, limit = 25)
        assertEquals(listOf(2), result.map { it.id })
    }

    @Test
    fun hiddenAdultContentAlsoExcludesUnknownSafetyMetadata() {
        val source = listOf(recommendation(1), recommendation(2, nsfw = "gray"),
            recommendation(3, nsfw = "black"), recommendation(4, nsfw = null))
        assertEquals(listOf(1), selectMangaRecommendations(source, emptySet(), false, 25).map { it.id })
        assertEquals(listOf(1, 2, 3, 4), selectMangaRecommendations(source, emptySet(), true, 25).map { it.id })
    }

    @Test
    fun tiesHaveStableOrderingAndTheLimitIsRespected() {
        val source = listOf(recommendation(8), recommendation(2), recommendation(5))
        assertEquals(listOf(2, 5), selectMangaRecommendations(source, emptySet(), true, 2).map { it.id })
        assertEquals(emptyList<MangaNode>(), selectMangaRecommendations(source, emptySet(), true, 0))
    }

    @Test
    fun largeVoteTotalsDoNotOverflow() {
        val source = listOf(recommendation(1, Int.MAX_VALUE), recommendation(2, Int.MAX_VALUE),
            recommendation(2, Int.MAX_VALUE))
        assertEquals(listOf(2, 1), selectMangaRecommendations(source, emptySet(), true, 25).map { it.id })
    }
}
