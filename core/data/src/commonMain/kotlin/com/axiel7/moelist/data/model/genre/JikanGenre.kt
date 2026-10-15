package com.axiel7.moelist.data.model.genre

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MalCategoryGroup(val label: String, val jikanFilter: String) {
    GENRES("Genres", "genres"),
    THEMES("Themes", "themes"),
    DEMOGRAPHICS("Demographics", "demographics"),
    EXPLICIT("Explicit Genres", "explicit_genres"),
}

data class MalCategory(
    val name: String,
    val group: MalCategoryGroup,
    val malGenreId: Int,
) {
    val isAdult get() = group == MalCategoryGroup.EXPLICIT || malGenreId == 9
}

@Serializable
data class JikanGenre(
    @SerialName("mal_id") val id: Int,
    val name: String = "",
)

@Serializable
data class JikanGenreMedia(
    @SerialName("mal_id") val id: Int,
    val title: String = "",
    @SerialName("title_english") val englishTitle: String? = null,
    val images: JikanImages? = null,
    val type: String? = null,
    val score: Double? = null,
    val rating: String? = null,
    val genres: List<JikanGenre>? = null,
    @SerialName("explicit_genres") val explicitGenres: List<JikanGenre>? = null,
) {
    val displayTitle get() = title.ifBlank { englishTitle.orEmpty() }
    val imageUrl get() = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl
        ?: images?.webp?.largeImageUrl ?: images?.webp?.imageUrl

    fun isVisible(showAdult: Boolean): Boolean {
        if (id <= 0 || displayTitle.isBlank()) return false
        if (showAdult) return true
        // Fail closed when safety metadata is missing, in addition to Jikan's sfw filter.
        val knownGenres = genres ?: return false
        val knownExplicitGenres = explicitGenres ?: return false
        return knownExplicitGenres.isEmpty() &&
            knownGenres.none { it.id == 9 || it.id == 12 || it.id == 49 } &&
            rating?.startsWith("Rx", ignoreCase = true) != true &&
            rating?.startsWith("R+", ignoreCase = true) != true
    }
}

@Serializable
data class JikanImages(val jpg: JikanImage? = null, val webp: JikanImage? = null)

@Serializable
data class JikanImage(
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("large_image_url") val largeImageUrl: String? = null,
)

data class JikanGenrePage(val entries: List<JikanGenreMedia>, val hasNextPage: Boolean)

@Serializable
internal data class JikanGenresResponse(val data: List<JikanGenre>)

@Serializable
internal data class JikanGenreMediaResponse(
    val data: List<JikanGenreMedia>,
    val pagination: JikanGenrePagination,
)

@Serializable
internal data class JikanGenrePagination(
    @SerialName("has_next_page") val hasNextPage: Boolean,
)
