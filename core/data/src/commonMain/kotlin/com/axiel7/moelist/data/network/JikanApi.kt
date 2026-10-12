package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
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

    suspend fun getUserStats(username: String): Response<UserStats> =
        publicClient.get("${JIKAN_API_URL}users/${username.encodeURLPathPart()}/statistics") {
            header(HttpHeaders.Accept, "application/json")
        }.body()

    // Shared public client is isolated from MyAnimeList OAuth headers.
    private val aniList by lazy { AniListCatalogApi(publicClient) }

    suspend fun getAniListGenres(): List<String> = aniList.genres()

    suspend fun getAniListGenreMedia(
        type: MediaType,
        genre: String,
        page: Int,
        showAdult: Boolean,
    ): AniListCatalogPage = aniList.media(type, genre, page, showAdult)
}
