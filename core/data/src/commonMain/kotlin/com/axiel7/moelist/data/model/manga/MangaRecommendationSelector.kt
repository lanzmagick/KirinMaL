package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.anime.Recommendations

/** Merge title-based MAL recommendations, excluding titles already in the user's list. */
fun selectMangaRecommendations(
    recommendations: List<Recommendations<MangaNode>>,
    excludedIds: Set<Int>,
    showAdult: Boolean,
    limit: Int,
): List<MangaNode> {
    val nodes = linkedMapOf<Int, MangaNode>()
    val votes = mutableMapOf<Int, Long>()
    recommendations.forEach { recommendation ->
        val node = recommendation.node
        if (node.id <= 0 || node.title.isBlank() || node.id in excludedIds ||
            node.myListStatus != null || (!showAdult && node.nsfw != "white")) return@forEach
        nodes.getOrPut(node.id) { node }
        votes[node.id] = (votes[node.id] ?: 0L) +
                recommendation.numRecommendations.coerceAtLeast(0).toLong()
    }
    return nodes.values.sortedWith(
        compareByDescending<MangaNode> { votes[it.id] ?: 0L }.thenBy { it.id }
    ).take(limit.coerceAtLeast(0))
}
