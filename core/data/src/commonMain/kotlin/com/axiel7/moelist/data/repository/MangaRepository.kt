package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.manga.selectMangaRecommendations
import com.axiel7.moelist.data.model.media.ListStatusDto
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class MangaRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Read all pages of the signed-in MAL manga list. Do not return a
     * partially fetched catalog as complete; unknown badges must not become "+".
     */
    suspend fun loadAllUserMangaStatuses(): Map<Int, MyMangaListStatus>? = withContext(dispatcher) {
        try {
            val statuses = mutableMapOf<Int, MyMangaListStatus>()
            var next: String? = null
            var pages = 0
            do {
                val response = if (pages == 0) api.getAllUserMangaStatusEntries()
                    else api.getUserMangaList(next ?: break)
                val entries = response.data ?: return@withContext null
                if (response.error != null) return@withContext null
                entries.forEach { entry ->
                    entry.listStatus?.let { statuses[entry.node.id] = it }
                }
                next = response.paging?.next?.takeIf { it.isNotBlank() }
                pages++
            } while (next != null && pages < 100)
            if (next != null) null else statuses
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val LIST_STATUS_FIELDS =
            "start_date,finish_date,num_times_reread,is_rereading,reread_value,priority,tags,comments"
        private const val MANGA_DETAILS_FIELDS =
            "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                    "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                    "my_list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,source,authors{first_name,last_name}," +
                    "serialization,related_anime{media_type,alternative_titles{en,ja}}," +
                    "related_manga{media_type,alternative_titles{en,ja}}," +
                    "recommendations{alternative_titles{en,ja}},background"
        private const val USER_MANGA_LIST_FIELDS =
            "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,media_type,status"
        private const val SEARCH_FIELDS =
            "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_chapters,start_date," +
                    "my_list_status{status}"
        private const val RANKING_FIELDS =
            "alternative_titles{en,ja},mean,media_type,num_chapters,num_list_users,my_list_status{status}"
        private const val RECOMMENDATIONS_FIELDS =
            "recommendations{alternative_titles{en,ja},mean,media_type,nsfw,my_list_status{status}}"
    }

    /** Recommendations from up to three recently updated Reading (or Completed) manga. */
    suspend fun getRecommendedManga(limit: Int = 25): Response<List<MangaList>> =
        withContext(dispatcher) {
            try {
                val nsfw = defaultPreferencesRepository.nsfwInt()
                var seedResponse = api.getUserMangaList(
                    status = ListStatusDto.READING,
                    sort = MediaSort.UPDATED,
                    limit = 3,
                    nsfw = nsfw,
                    fields = USER_MANGA_LIST_FIELDS,
                )
                if (seedResponse.data == null) {
                    return@withContext Response(message = seedResponse.message ?: "Manga list unavailable")
                }
                if (seedResponse.data.isNullOrEmpty()) {
                    seedResponse = api.getUserMangaList(
                        status = ListStatusDto.COMPLETED,
                        sort = MediaSort.UPDATED,
                        limit = 3,
                        nsfw = nsfw,
                        fields = USER_MANGA_LIST_FIELDS,
                    )
                }
                val seeds = seedResponse.data?.distinctBy { it.node.id }?.take(3)
                    ?: return@withContext Response(message = seedResponse.message ?: "Manga list unavailable")
                if (seeds.isEmpty()) return@withContext Response(data = emptyList())

                val listed = loadAllUserMangaStatuses()
                    ?: return@withContext Response(message = "Manga list unavailable")
                val recommendations = mutableListOf<Recommendations<MangaNode>>()
                var successfulSources = 0
                for (seed in seeds) {
                    try {
                        val details = api.getMangaDetails(seed.node.id, RECOMMENDATIONS_FIELDS)
                        check(details.id == seed.node.id) { "Manga recommendations unavailable" }
                        recommendations.addAll(details.recommendations.orEmpty().distinctBy { it.node.id })
                        successfulSources++
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // One unavailable seed must not hide recommendations from the others.
                    }
                    currentCoroutineContext().ensureActive()
                }
                if (successfulSources == 0) {
                    return@withContext Response(message = "Manga recommendations unavailable")
                }
                Response(data = selectMangaRecommendations(
                    recommendations = recommendations,
                    excludedIds = listed.keys + seeds.map { it.node.id },
                    showAdult = nsfw == 1,
                    limit = limit.coerceIn(1, 100),
                ).map { MangaList(it) })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Response(message = e.message ?: "Manga recommendations unavailable")
            }
        }

    suspend fun getMangaDetails(
        mangaId: Int
    ): MangaDetails? = withContext(dispatcher) {
        return@withContext try {
            api.getMangaDetails(mangaId, MANGA_DETAILS_FIELDS)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserMangaList(
        status: ListStatusDto,
        sort: MediaSort,
        page: String? = null
    ): Response<List<UserMangaList>> = withContext(dispatcher) {
        return@withContext try {
            if (page == null) api.getUserMangaList(
                status = status,
                sort = sort,
                limit = null,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = USER_MANGA_LIST_FIELDS,
            )
            else api.getUserMangaList(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateMangaEntry(
        mangaId: Int,
        status: ListStatusDto? = null,
        @IntRange(0, 10) score: Int? = null,
        chaptersRead: Int? = null,
        volumesRead: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRereading: Boolean? = null,
        numRereads: Int? = null,
        @IntRange(0, 5) rereadValue: Int? = null,
        @IntRange(0, 2) priority: Int? = null,
        tags: String? = null,
        comments: String? = null,
    ): MyMangaListStatus? = withContext(dispatcher) {
        return@withContext try {
            api.updateUserMangaList(
                mangaId,
                status,
                score,
                chaptersRead,
                volumesRead,
                startDate,
                endDate,
                isRereading,
                numRereads,
                rereadValue,
                priority,
                tags,
                comments
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteMangaEntry(
        mangaId: Int
    ): Boolean = withContext(dispatcher) {
        return@withContext try {
            val result = api.deleteMangaEntry(mangaId)
            return@withContext result.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }

    suspend fun searchManga(
        query: String,
        limit: Int,
        offset: Int? = null,
        page: String? = null
    ): Response<List<MangaList>> = withContext(dispatcher) {
        return@withContext try {
            if (page == null) api.getMangaList(
                query = query,
                limit = limit,
                offset = offset,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = SEARCH_FIELDS,
            )
            else api.getMangaList(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaRanking(
        rankingType: RankingType,
        limit: Int,
        page: String? = null
    ): Response<List<MangaRanking>> = withContext(dispatcher) {
        return@withContext try {
            if (page == null) api.getMangaRanking(
                rankingType = rankingType.serialName,
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = RANKING_FIELDS,
            )
            else api.getMangaRanking(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaIdsOfUserList(
        status: ListStatusDto,
        prefetchedList: List<UserMangaList> = emptyList(),
        page: String? = null
    ): Response<List<Int>> = withContext(dispatcher) {
        return@withContext try {
            val result = if (page == null) api.getUserMangaList(
                status = status,
                sort = MediaSort.UPDATED,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "id",
            ) else api.getUserMangaList(page)
            result.error?.let {
                return@withContext Response(error = result.error, message = result.message)
            }
            if (result.paging?.next != null) {
                getMangaIdsOfUserList(
                    status = status,
                    prefetchedList = prefetchedList.plus(result.data.orEmpty()),
                    page = result.paging.next
                )
            } else return@withContext Response(
                data = prefetchedList.plus(result.data.orEmpty()).map { it.node.id }
            )
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }
}
