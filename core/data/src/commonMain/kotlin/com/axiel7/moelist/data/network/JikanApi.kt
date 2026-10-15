package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.genre.JikanGenrePage
import com.axiel7.moelist.data.model.genre.MalCategory
import com.axiel7.moelist.data.model.media.MediaType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPathPart
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class JikanApi(client: HttpClient) {

    companion object {
        const val JIKAN_API_URL = "https://api.jikan.moe/v4/"
    }

    /**
     * Public Jikan requests must NOT pass through MAL's OIDC/Auth plugin or
     * X-MAL-CLIENT-ID defaults. Use the injected platform engine but an isolated
     * Ktor pipeline, matching KirinNime's separate Jikan read client.
     */
    private val publicClient = HttpClient(client.engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 12000
            requestTimeoutMillis = 18000
            socketTimeoutMillis = 12000
        }
    }

    private val requestMutex = Mutex()
    private var lastRequest: TimeMark? = null

    private suspend fun waitForRequestSlot() = requestMutex.withLock {
        // Share the limit across statistics, category requests and result pages.
        val elapsed = lastRequest?.elapsedNow()?.inWholeMilliseconds ?: 1100L
        delay((1100L - elapsed).coerceAtLeast(0L))
        lastRequest = TimeSource.Monotonic.markNow()
    }

    suspend fun getUserStats(username: String): Response<UserStats> {
        waitForRequestSlot()
        return publicClient.get("${JIKAN_API_URL}users/${username.encodeURLPathPart()}/statistics") {
            header(HttpHeaders.Accept, "application/json")
        }.body()
    }

    // Shared public client is isolated from MyAnimeList OAuth headers.
    private val catalog by lazy { JikanCatalogApi(publicClient, ::waitForRequestSlot) }

    suspend fun getGenres(type: MediaType): List<MalCategory> = catalog.genres(type)

    suspend fun getGenreMedia(
        type: MediaType,
        genreId: Int,
        page: Int,
        showAdult: Boolean,
    ): JikanGenrePage = catalog.media(type, genreId, page, showAdult)
}
