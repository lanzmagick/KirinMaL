package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.genre.JikanGenreResponse
import com.axiel7.moelist.data.model.genre.JikanGenreMediaResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPathPart

class JikanApi(private val client: HttpClient) {

    companion object {
        const val JIKAN_API_URL = "https://api.jikan.moe/v4/"
    }

    private fun HttpRequestBuilder.removeOfficialApiHeaders() = headers.apply {
        remove(HttpHeaders.Authorization)
        remove("X-MAL-CLIENT-ID")
    }

    suspend fun getUserStats(
        username: String
    ): Response<UserStats> = client.get("${JIKAN_API_URL}users/${username.encodeURLPathPart()}/statistics") {
        removeOfficialApiHeaders()
    }.body()

    /** MAL's full current taxonomy: genres, explicit genres, themes and demographics. */
    suspend fun getGenres(mediaType: MediaType): JikanGenreResponse =
        client.get("${JIKAN_API_URL}genres/${mediaType.name.lowercase()}") {
            removeOfficialApiHeaders()
        }.body()

    /** Jikan genre-search results; sfw=false retains adult genres and titles. */
    suspend fun getGenreMedia(
        mediaType: MediaType,
        genreId: Int,
        page: Int,
        showAdult: Boolean,
    ): JikanGenreMediaResponse =
        client.get("${JIKAN_API_URL}${mediaType.name.lowercase()}") {
            removeOfficialApiHeaders()
            parameter("genres", genreId)
            parameter("page", page)
            parameter("limit", 25)
            parameter("sfw", !showAdult)
            parameter("order_by", "popularity")
            parameter("sort", "asc")
        }.body()
}