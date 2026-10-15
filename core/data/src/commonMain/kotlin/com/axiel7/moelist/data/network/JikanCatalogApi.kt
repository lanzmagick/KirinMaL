package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.genre.JikanGenreMediaResponse
import com.axiel7.moelist.data.model.genre.JikanGenrePage
import com.axiel7.moelist.data.model.genre.JikanGenresResponse
import com.axiel7.moelist.data.model.genre.MalCategory
import com.axiel7.moelist.data.model.genre.MalCategoryGroup
import com.axiel7.moelist.data.model.media.MediaType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.hours
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Public MAL taxonomy and discovery. Every returned media ID is already a MAL ID. */
class JikanCatalogApi(
    private val client: HttpClient,
    private val beforeRequest: suspend () -> Unit = {},
) {
    private data class CachedCatalog(val entries: List<MalCategory>, val storedAt: TimeMark)
    private val catalogMutex = Mutex()
    private val catalogs = mutableMapOf<MediaType, CachedCatalog>()

    suspend fun genres(type: MediaType): List<MalCategory> = catalogMutex.withLock {
        catalogs[type]?.takeIf { it.storedAt.elapsedNow() < 6.hours }?.let {
            return@withLock it.entries
        }
        val categories = mutableListOf<MalCategory>()
        for (group in MalCategoryGroup.entries) {
            beforeRequest()
            val response: JikanGenresResponse = client.get(
                "${JikanApi.JIKAN_API_URL}genres/${type.jikanPath}"
            ) {
                parameter("filter", group.jikanFilter)
            }.body()
            categories += response.data
                .filter { it.id > 0 && it.name.isNotBlank() }
                .sortedBy { it.name.lowercase() }
                .map { MalCategory(it.name, group, it.id) }
        }
        check(categories.isNotEmpty()) { "Jikan returned no MyAnimeList categories. Please retry." }
        val result = categories.distinctBy { it.malGenreId }
        // Store only a complete successful taxonomy, never a partial/error response.
        catalogs[type] = CachedCatalog(result, TimeSource.Monotonic.markNow())
        result
    }

    suspend fun media(
        type: MediaType,
        genreId: Int,
        page: Int,
        showAdult: Boolean,
    ): JikanGenrePage {
        require(genreId > 0 && page > 0)
        beforeRequest()
        val response: JikanGenreMediaResponse = client.get(
            "${JikanApi.JIKAN_API_URL}${type.jikanPath}"
        ) {
            parameter("genres", genreId)
            parameter("page", page)
            parameter("limit", 25)
            parameter("order_by", "members")
            parameter("sort", "desc")
            if (!showAdult) {
                parameter("sfw", true)
                parameter("genres_exclude", "9,12,49")
            }
        }.body()
        return JikanGenrePage(
            entries = response.data.filter { it.isVisible(showAdult) }.distinctBy { it.id },
            hasNextPage = response.pagination.hasNextPage,
        )
    }
}

private val MediaType.jikanPath get() = when (this) {
    MediaType.ANIME -> "anime"
    MediaType.MANGA -> "manga"
}
