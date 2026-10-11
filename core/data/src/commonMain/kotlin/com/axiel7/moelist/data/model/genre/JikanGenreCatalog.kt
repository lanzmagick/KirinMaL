package com.axiel7.moelist.data.model.genre

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** MAL genre and theme IDs returned by the Jikan v4 genre directory. */
@Serializable
data class JikanGenreResponse(
    val data: List<JikanGenre> = emptyList(),
)

@Serializable
data class JikanGenre(
    @SerialName("mal_id") val malId: Int,
    val name: String,
    val type: String = "",
    val count: Int = 0,
) {
    val isAdult: Boolean
        get() = type.contains("explicit", ignoreCase = true) ||
            name.equals("Hentai", ignoreCase = true) ||
            name.equals("Erotica", ignoreCase = true)
}

@Serializable
data class JikanGenreMediaResponse(
    val data: List<JikanGenreMedia> = emptyList(),
    val pagination: JikanGenrePagination? = null,
)

@Serializable
data class JikanGenrePagination(
    @SerialName("has_next_page") val hasNextPage: Boolean = false,
)

@Serializable
data class JikanGenreMedia(
    @SerialName("mal_id") val malId: Int,
    val title: String,
    val score: Double? = null,
    val type: String? = null,
    val images: JikanGenreMediaImages? = null,
) {
    val imageUrl: String?
        get() = images?.jpg?.largeImageUrl
            ?: images?.jpg?.imageUrl
            ?: images?.webp?.largeImageUrl
            ?: images?.webp?.imageUrl
}

@Serializable
data class JikanGenreMediaImages(
    val jpg: JikanGenreImageType? = null,
    val webp: JikanGenreImageType? = null,
)

@Serializable
data class JikanGenreImageType(
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("large_image_url") val largeImageUrl: String? = null,
)
