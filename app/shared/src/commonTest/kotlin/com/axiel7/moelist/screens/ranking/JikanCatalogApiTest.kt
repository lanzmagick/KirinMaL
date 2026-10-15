package com.axiel7.moelist.screens.ranking

import com.axiel7.moelist.data.model.genre.MalCategoryGroup
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.network.JikanCatalogApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JikanCatalogApiTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val safeMetadata = "\"genres\":[],\"explicit_genres\":[]"

    private fun client(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun entry(id: Int, title: String = "Title $id", metadata: String = safeMetadata) =
        """{"mal_id":$id,"title":"$title","score":8.75,$metadata} """

    private fun page(entries: String, hasNext: Boolean = false) =
        """{"data":[$entries],"pagination":{"has_next_page":$hasNext,"last_visible_page":9}}"""

    @Test
    fun loadsOriginalNamesAndSeparateAnimeMangaIdsWithGroupedCaching() = runTest {
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            val manga = request.url.encodedPath.endsWith("/manga")
            val (id, name) = when (request.url.parameters["filter"]) {
                "genres" -> 1 to "Action"
                "themes" -> 68 to if (manga) "Memoir" else "Organized Crime"
                "demographics" -> (if (manga) 42 else 43) to "Josei"
                "explicit_genres" -> 12 to "Hentai"
                else -> error("A MAL category group was not requested")
            }
            respond("""{"data":[{"mal_id":$id,"name":"$name","count":20}]}""", headers = jsonHeaders)
        }
        client(engine).use { http ->
            val api = JikanCatalogApi(http)
            val anime = api.genres(MediaType.ANIME)
            val manga = api.genres(MediaType.MANGA)
            assertEquals(MalCategoryGroup.entries.toList(), anime.map { it.group })
            assertEquals(43, anime.first { it.name == "Josei" }.malGenreId)
            assertEquals(42, manga.first { it.name == "Josei" }.malGenreId)
            assertEquals("Memoir", manga.first { it.group == MalCategoryGroup.THEMES }.name)
            assertEquals(anime, api.genres(MediaType.ANIME))
            assertEquals(manga, api.genres(MediaType.MANGA))
            assertEquals(8, calls)
        }
    }

    @Test
    fun searchesWithMalGenreIdAndPreservesApiPaginationAndTenPointScore() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/v4/anime", request.url.encodedPath)
            assertEquals("68", request.url.parameters["genres"])
            assertEquals("2", request.url.parameters["page"])
            assertEquals("25", request.url.parameters["limit"])
            assertEquals("members", request.url.parameters["order_by"])
            assertEquals("desc", request.url.parameters["sort"])
            assertEquals("true", request.url.parameters["sfw"])
            assertEquals("9,12,49", request.url.parameters["genres_exclude"])
            respond(page(entry(123), hasNext = true), headers = jsonHeaders)
        }
        client(engine).use { http ->
            val result = JikanCatalogApi(http).media(MediaType.ANIME, 68, 2, false)
            assertEquals(123, result.entries.single().id)
            assertEquals(8.75, result.entries.single().score)
            assertTrue(result.hasNextPage)
        }
    }

    @Test
    fun mangaAllowsExplicitResultsOnlyWhenAdultVisibilityIsEnabled() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/v4/manga", request.url.encodedPath)
            assertEquals("12", request.url.parameters["genres"])
            assertNull(request.url.parameters["sfw"])
            assertNull(request.url.parameters["genres_exclude"])
            respond(page(entry(321, metadata = "\"explicit_genres\":[{\"mal_id\":12}]")), headers = jsonHeaders)
        }
        client(engine).use { http ->
            val result = JikanCatalogApi(http).media(MediaType.MANGA, 12, 1, true)
            assertEquals(321, result.entries.single().id)
            assertFalse(result.hasNextPage)
        }
    }

    @Test
    fun hiddenAdultContentRejectsExplicitEcchiAndUnknownSafetyMetadata() = runTest {
        val rows = listOf(
            entry(1),
            entry(2, metadata = "\"genres\":[],\"explicit_genres\":[{\"mal_id\":12}]"),
            entry(3, metadata = "\"genres\":[{\"mal_id\":9}],\"explicit_genres\":[]"),
            entry(4, metadata = "\"rating\":\"Rx - Hentai\",$safeMetadata"),
            entry(5, metadata = "\"rating\":\"R+ - Mild Nudity\",$safeMetadata"),
            entry(6, metadata = "\"genres\":[]"),
            entry(7, metadata = "\"explicit_genres\":[]"),
        ).joinToString(",")
        val engine = MockEngine { respond(page(rows), headers = jsonHeaders) }
        client(engine).use { http ->
            val api = JikanCatalogApi(http)
            assertEquals(listOf(1), api.media(MediaType.ANIME, 1, 1, false).entries.map { it.id })
            assertEquals((1..7).toList(), api.media(MediaType.ANIME, 1, 1, true).entries.map { it.id })
        }
    }

    @Test
    fun removesDuplicateInvalidAndUntitledResultsWithoutLosingNextPage() = runTest {
        val engine = MockEngine {
            respond(page(listOf(entry(1), entry(1), entry(0), entry(2, " ")).joinToString(","), true),
                headers = jsonHeaders)
        }
        client(engine).use { http ->
            val result = JikanCatalogApi(http).media(MediaType.MANGA, 1, 1, false)
            assertEquals(listOf(1), result.entries.map { it.id })
            assertTrue(result.hasNextPage)
        }
    }

    @Test
    fun fullyFilteredPageStillAllowsLoadingTheNextPage() = runTest {
        val engine = MockEngine {
            respond(page(entry(1, metadata = "\"genres\":[],\"explicit_genres\":[{\"mal_id\":12}]"), true),
                headers = jsonHeaders)
        }
        client(engine).use { http ->
            val result = JikanCatalogApi(http).media(MediaType.MANGA, 1, 1, false)
            assertTrue(result.entries.isEmpty())
            assertTrue(result.hasNextPage)
        }
    }

    @Test
    fun jikanRequestsDoNotInheritMalAuthorizationOrClientId() = runTest {
        val engine = MockEngine { request ->
            assertNull(request.headers[HttpHeaders.Authorization])
            assertNull(request.headers["X-MAL-CLIENT-ID"])
            respond(page(entry(1)), headers = jsonHeaders)
        }
        HttpClient(engine) {
            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer test-mal-token")
                header("X-MAL-CLIENT-ID", "test-mal-client-id")
            }
        }.use { malClient ->
            JikanApi(malClient).getGenreMedia(MediaType.ANIME, 1, 1, false)
        }
    }

    @Test
    fun failedTaxonomyIsNotCachedAsAnEmptyOrPartialCatalog() = runTest {
        var fail = true
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            if (fail && calls == 2) respond("{}", HttpStatusCode.ServiceUnavailable, jsonHeaders)
            else {
                val id = when (request.url.parameters["filter"]) {
                    "genres" -> 1
                    "themes" -> 62
                    "demographics" -> 27
                    else -> 12
                }
                respond("""{"data":[{"mal_id":$id,"name":"Category $id"}]}""", headers = jsonHeaders)
            }
        }
        client(engine).use { http ->
            val api = JikanCatalogApi(http)
            assertFailsWith<ServerResponseException> { api.genres(MediaType.ANIME) }
            fail = false
            assertEquals(4, api.genres(MediaType.ANIME).size)
            assertEquals(6, calls)
            api.genres(MediaType.ANIME)
            assertEquals(6, calls)
        }
    }

    @Test
    fun malformedResponsesAreNotShownAsSuccessfulEmptyResults() = runTest {
        val engine = MockEngine { respond("{}", headers = jsonHeaders) }
        client(engine).use { http ->
            assertFailsWith<JsonConvertException> {
                JikanCatalogApi(http).media(MediaType.ANIME, 1, 1, false)
            }
        }
    }
}
