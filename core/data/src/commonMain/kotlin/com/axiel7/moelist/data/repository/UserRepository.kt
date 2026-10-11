package com.axiel7.moelist.data.repository

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.ListStatusDto
import kotlinx.coroutines.CancellationException
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: Api,
    private val jikanApi: JikanApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    companion object {
        private const val USER_FIELDS = "id,name,gender,location,joined_at,anime_statistics"
    }

    suspend fun getMyUser(): User? = withContext(dispatcher) {
        return@withContext try {
            api.getUser(USER_FIELDS)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserStats(
        username: String
    ): Response<UserStats> = withContext(dispatcher) {
        return@withContext try {
            jikanApi.getUserStats(username)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    /**
     * When public Jikan stats fail, compute profile numbers from the complete
     * authenticated MAL manga list. No partial pagination results are used.
     */
    suspend fun getMangaStatisticsFromList(): Response<UserStats.MangaStats> =
        withContext(dispatcher) {
            try {
                val entries = mutableListOf<UserMangaList>()
                val seenIds = mutableSetOf<Int>()
                val seenPages = mutableSetOf<String>()
                var next: String? = null
                var page = 0
                do {
                    val response = if (page == 0) api.getAllUserMangaEntries()
                    else api.getUserMangaList(next!!)
                    if (response.data == null || response.error != null) {
                        return@withContext Response(
                            message = response.message ?: response.error
                                ?: "MAL manga list is unavailable."
                        )
                    }
                    for (entry in response.data) {
                        if (seenIds.add(entry.node.id)) entries.add(entry)
                    }
                    page++
                    next = response.paging?.next
                    if (next != null && (!seenPages.add(next) || page >= 100)) {
                        return@withContext Response(message = "MAL manga pagination was incomplete.")
                    }
                } while (next != null)

                val statuses = entries.mapNotNull { it.listStatus }
                val scored = statuses.map { it.score }.filter { it > 0 }
                Response(
                    data = UserStats.MangaStats(
                        days = null, // Not exposed by MAL's manga list endpoint.
                        meanScore = scored.takeIf { it.isNotEmpty() }
                            ?.let { it.sum().toFloat() / it.size },
                        current = statuses.count { it.status == ListStatusDto.READING },
                        completed = statuses.count { it.status == ListStatusDto.COMPLETED },
                        onHold = statuses.count { it.status == ListStatusDto.ON_HOLD },
                        dropped = statuses.count { it.status == ListStatusDto.DROPPED },
                        planned = statuses.count { it.status == ListStatusDto.PLAN_TO_READ },
                        totalEntries = entries.size,
                        repeat = statuses.sumOf { (it.repeatCount ?: 0).coerceAtLeast(0) },
                        chaptersRead = statuses.sumOf { (it.progress ?: 0).coerceAtLeast(0) },
                        volumesRead = statuses.sumOf { it.numVolumesRead.coerceAtLeast(0) }
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Response(message = e.message ?: "Could not retrieve MAL manga list.")
            }
        }
}