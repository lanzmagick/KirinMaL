package com.axiel7.moelist.screens.magic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.toDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MagicPickerViewModel(
    private val context: MagicPickerContext,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val preferences: DefaultPreferencesRepository,
) : ViewModel() {
    private val state = MutableStateFlow(MagicPickerUiState())
    val uiState = state.asStateFlow()
    private val bag = ShuffleBag<BaseMediaNode>(id = { it.id })
    private var cachedKey: Pair<Boolean, TitleLanguage>? = null
    private var request: Job? = null

    fun pick(refresh: Boolean = false, animate: Boolean = true) {
        // A newer pick replaces any in-flight request so an older result cannot overwrite the latest choice.
        request?.cancel()
        state.update { it.copy(isLoading = true, selection = null, error = null) }
        request = viewModelScope.launch {
            try {
                // The pool depends on the preferred title and NSFW setting, so either change invalidates its cache.
                val titleLanguage = preferences.titleLang.first()
                val cacheKey = preferences.nsfw.first() to titleLanguage
                // Reuse the loaded pool for Again; refresh it only on request or when relevant preferences change.
                if (refresh || cachedKey != cacheKey) {
                    // Apply the list's selected initial-letter filter using the user's preferred title language.
                    val pool = loadPool().filter {
                        it.id > 0 && it.title.isNotBlank() &&
                                context.titleInitial.matches(it.title(titleLanguage))
                    }
                    currentCoroutineContext().ensureActive()
                    bag.replace(pool)
                    cachedKey = cacheKey
                }
                // ShuffleBag cycles through the pool without repeating an entry until the current cycle is used up.
                val selection = bag.next()
                if (animate) delay(650) // Let the mystery-box reveal animation read clearly.
                state.update {
                    it.copy(
                        isLoading = false,
                        selection = selection,
                        error = if (selection == null) MagicPickerError.EMPTY else null,
                        preferredTitle = titleLanguage,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                cachedKey = null
                state.update { it.copy(isLoading = false, error = MagicPickerError.UNAVAILABLE) }
            }
        }
    }

    fun close() {
        // Stop pending network work when the dialog is dismissed or a selected entry is opened.
        request?.cancel()
        state.update { it.copy(isLoading = false, selection = null, error = null) }
    }

    private suspend fun loadPool(): List<BaseMediaNode> {
        val status = context.listStatus
        if (status == null) {
            // Discovery samples valid MAL rankings, never guessed numeric IDs.
            val responses = if (context.mediaType == MediaType.ANIME) {
                listOf(
                    animeRepository.getAnimeRanking(
                        RankingType.POPULARITY, 100, AnimeRepository.RANKING_FIELDS,
                    ),
                    animeRepository.getAnimeRanking(
                        RankingType.SCORE, 100, AnimeRepository.RANKING_FIELDS,
                    ),
                )
            } else {
                listOf(
                    mangaRepository.getMangaRanking(RankingType.POPULARITY, 100),
                    mangaRepository.getMangaRanking(RankingType.SCORE, 100),
                )
            }
            val entries = responses.flatMap { it.data.orEmpty() }.map { it.node }
            check(entries.isNotEmpty() || responses.all { it.data != null })
            return entries
        }
        // List mode loads every page for the active Anime or Manga status before picking from it.
        val entries = mutableListOf<BaseMediaNode>()
        val visited = mutableSetOf<String>()
        var page: String? = null
        // Follow MAL's next-page cursor until the selected list status is fully loaded.
        do {
            val response = if (context.mediaType == MediaType.ANIME) {
                animeRepository.getUserAnimeList(status.toDto(), MediaSort.UPDATED, page)
            } else {
                mangaRepository.getUserMangaList(status.toDto(), MediaSort.UPDATED, page)
            }
            currentCoroutineContext().ensureActive()
            val data = response.data ?: error("MAL list unavailable")
            entries.addAll(data.map { it.node })
            page = response.paging?.next
            // Protect against a malformed API response that points back to an already visited page.
            page?.let { check(visited.add(it)) { "MAL repeated a pagination cursor" } }
        } while (page != null)
        return entries
    }
}
