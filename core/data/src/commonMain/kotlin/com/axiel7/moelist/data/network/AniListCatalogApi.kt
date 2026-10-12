package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.media.MediaType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AniList is a PUBLIC discovery source only. idMal is mandatory before
 * linking to a detail/edit screen; MAL OAuth and list writes remain untouched.
 */
class AniListCatalogApi(private val client: HttpClient) {
    companion object {
        private const val GRAPHQL = "https://graphql.anilist.co"
        val offlineGenres = listOf(
            "Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy",
            "Horror", "Mahou Shoujo", "Mecha", "Music", "Mystery",
            "Psychological", "Romance", "Sci-Fi", "Slice of Life",
            "Sports", "Supernatural", "Thriller", "Hentai"
        )
    }

    suspend fun genres(): List<String> {
        val result: AniListGraphResponse = client.post(GRAPHQL) {
            contentType(ContentType.Application.Json)
            setBody(AniListGraphRequest(query = "query { GenreCollection }"))
        }.body()
        result.requireNoErrors()
        return result.data?.genres.orEmpty().filter { it.isNotBlank() }.distinct()
    }

    suspend fun media(
        type: MediaType,
        genre: String,
        page: Int,
        includeAdult: Boolean,
        filterByTag: Boolean = false,
    ): AniListCatalogPage {
        val filterField = if (filterByTag) "tag" else "genre"
        val adultFilter = if (includeAdult) "" else ", isAdult: false"
        val query = """
            query (${'$'}page: Int!, ${'$'}genre: String!, ${'$'}type: MediaType!) {
              Page(page: ${'$'}page, perPage: 25) {
                pageInfo { hasNextPage }
                media(type: ${' {
                  idMal
                  isAdult
                  title { userPreferred }
                  coverImage { large }
                  averageScore
                  format
                }
              }
            }
        """.trimIndent()
        val response: AniListGraphResponse = client.post(GRAPHQL) {
            contentType(ContentType.Application.Json)
            setBody(
                AniListGraphRequest(
                    query = query,
                    variables = AniListVariables(
                        page = page,
                        genre = genre,
                        type = type.name,
                    ),
                )
            )
        }.body()
        response.requireNoErrors()
        val payload = response.data?.page ?: throw IllegalStateException("AniList returned no genre page.")
        return AniListCatalogPage(
            // AniList-only media IDs may not be used for MAL updates.
            entries = payload.media.filter { (it.idMal ?: 0) > 0 && (includeAdult || !it.isAdult) },
            hasNextPage = payload.pageInfo?.hasNextPage == true,
        )
    }
}

data class AniListCatalogPage(
    val entries: List<AniListMedia>,
    val hasNextPage: Boolean,
)

@Serializable
private data class AniListGraphRequest(
    val query: String,
    val variables: AniListVariables? = null,
)

@Serializable
private data class AniListVariables(
    val page: Int,
    val genre: String,
    val type: String,
)

@Serializable
private data class AniListGraphResponse(
    val data: AniListGraphData? = null,
    val errors: List<AniListGraphError> = emptyList(),
) {
    fun requireNoErrors() {
        if (errors.isNotEmpty()) throw IllegalStateException(
            errors.first().message.ifBlank { "AniList is temporarily unavailable." }
        )
    }
}

@Serializable
private data class AniListGraphError(
    val message: String = "",
)

@Serializable
private data class AniListGraphData(
    @SerialName("GenreCollection") val genres: List<String>? = null,
    @SerialName("Page") val page: AniListGraphPage? = null,
)

@Serializable
private data class AniListGraphPage(
    val pageInfo: AniListPageInfo? = null,
    val media: List<AniListMedia> = emptyList(),
)

@Serializable
private data class AniListPageInfo(
    val hasNextPage: Boolean = false,
)

@Serializable
data class AniListMedia(
    val idMal: Int? = null,
    val isAdult: Boolean = false,
    val title: AniListTitle? = null,
    val coverImage: AniListCover? = null,
    val averageScore: Int? = null,
    val format: String? = null,
)

@Serializable
data class AniListTitle(val userPreferred: String? = null)

@Serializable
data class AniListCover(val large: String? = null)
}type, $filterField: ${' {
                  idMal
                  isAdult
                  title { userPreferred }
                  coverImage { large }
                  averageScore
                  format
                }
              }
            }
        """.trimIndent()
        val response: AniListGraphResponse = client.post(GRAPHQL) {
            contentType(ContentType.Application.Json)
            setBody(
                AniListGraphRequest(
                    query = query,
                    variables = AniListVariables(
                        page = page,
                        genre = genre,
                        type = type.name,
                    ),
                )
            )
        }.body()
        response.requireNoErrors()
        val payload = response.data?.page ?: throw IllegalStateException("AniList returned no genre page.")
        return AniListCatalogPage(
            // AniList-only media IDs may not be used for MAL updates.
            entries = payload.media.filter { (it.idMal ?: 0) > 0 && (includeAdult || !it.isAdult) },
            hasNextPage = payload.pageInfo?.hasNextPage == true,
        )
    }
}

data class AniListCatalogPage(
    val entries: List<AniListMedia>,
    val hasNextPage: Boolean,
)

@Serializable
private data class AniListGraphRequest(
    val query: String,
    val variables: AniListVariables? = null,
)

@Serializable
private data class AniListVariables(
    val page: Int,
    val genre: String,
    val type: String,
)

@Serializable
private data class AniListGraphResponse(
    val data: AniListGraphData? = null,
    val errors: List<AniListGraphError> = emptyList(),
) {
    fun requireNoErrors() {
        if (errors.isNotEmpty()) throw IllegalStateException(
            errors.first().message.ifBlank { "AniList is temporarily unavailable." }
        )
    }
}

@Serializable
private data class AniListGraphError(
    val message: String = "",
)

@Serializable
private data class AniListGraphData(
    @SerialName("GenreCollection") val genres: List<String>? = null,
    @SerialName("Page") val page: AniListGraphPage? = null,
)

@Serializable
private data class AniListGraphPage(
    val pageInfo: AniListPageInfo? = null,
    val media: List<AniListMedia> = emptyList(),
)

@Serializable
private data class AniListPageInfo(
    val hasNextPage: Boolean = false,
)

@Serializable
data class AniListMedia(
    val idMal: Int? = null,
    val isAdult: Boolean = false,
    val title: AniListTitle? = null,
    val coverImage: AniListCover? = null,
    val averageScore: Int? = null,
    val format: String? = null,
)

@Serializable
data class AniListTitle(val userPreferred: String? = null)

@Serializable
data class AniListCover(val large: String? = null)
}genre, sort: POPULARITY_DESC$adultFilter) {
                  idMal
                  isAdult
                  title { userPreferred }
                  coverImage { large }
                  averageScore
                  format
                }
              }
            }
        """.trimIndent()
        val response: AniListGraphResponse = client.post(GRAPHQL) {
            contentType(ContentType.Application.Json)
            setBody(
                AniListGraphRequest(
                    query = query,
                    variables = AniListVariables(
                        page = page,
                        genre = genre,
                        type = type.name,
                    ),
                )
            )
        }.body()
        response.requireNoErrors()
        val payload = response.data?.page ?: throw IllegalStateException("AniList returned no genre page.")
        return AniListCatalogPage(
            // AniList-only media IDs may not be used for MAL updates.
            entries = payload.media.filter { (it.idMal ?: 0) > 0 && (includeAdult || !it.isAdult) },
            hasNextPage = payload.pageInfo?.hasNextPage == true,
        )
    }
}

data class AniListCatalogPage(
    val entries: List<AniListMedia>,
    val hasNextPage: Boolean,
)

@Serializable
private data class AniListGraphRequest(
    val query: String,
    val variables: AniListVariables? = null,
)

@Serializable
private data class AniListVariables(
    val page: Int,
    val genre: String,
    val type: String,
)

@Serializable
private data class AniListGraphResponse(
    val data: AniListGraphData? = null,
    val errors: List<AniListGraphError> = emptyList(),
) {
    fun requireNoErrors() {
        if (errors.isNotEmpty()) throw IllegalStateException(
            errors.first().message.ifBlank { "AniList is temporarily unavailable." }
        )
    }
}

@Serializable
private data class AniListGraphError(
    val message: String = "",
)

@Serializable
private data class AniListGraphData(
    @SerialName("GenreCollection") val genres: List<String>? = null,
    @SerialName("Page") val page: AniListGraphPage? = null,
)

@Serializable
private data class AniListGraphPage(
    val pageInfo: AniListPageInfo? = null,
    val media: List<AniListMedia> = emptyList(),
)

@Serializable
private data class AniListPageInfo(
    val hasNextPage: Boolean = false,
)

@Serializable
data class AniListMedia(
    val idMal: Int? = null,
    val isAdult: Boolean = false,
    val title: AniListTitle? = null,
    val coverImage: AniListCover? = null,
    val averageScore: Int? = null,
    val format: String? = null,
)

@Serializable
data class AniListTitle(val userPreferred: String? = null)

@Serializable
data class AniListCover(val large: String? = null)
