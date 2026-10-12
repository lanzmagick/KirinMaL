package com.axiel7.moelist.screens.home

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.ListStatusDto
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.data.utils.SeasonCalendar
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<HomeUiState>(), HomeEvent {

    override val mutableUiState = MutableStateFlow(HomeUiState())

    private var lastIsLoggedIn: Boolean? = null
    private var animeRequestJob: Job? = null
    private val mangaJobs = mutableMapOf<HomeMangaSection, Job>()
    private val loadedMangaSections = mutableSetOf<HomeMangaSection>()

    override fun initRequestChain(isLoggedIn: Boolean) {
        lastIsLoggedIn = isLoggedIn
        if (!isLoggedIn) {
            mangaJobs[HomeMangaSection.READING]?.cancel()
            loadedMangaSections.remove(HomeMangaSection.READING)
            mutableUiState.update {
                it.copy(readingManga = emptyList(), recommendedAnimes = emptyList(),
                    isLoadingReadingManga = false, readingMangaError = false)
            }
        }
        HomeMangaSection.entries.forEach { loadMangaSection(it) }
        animeRequestJob?.cancel()
        animeRequestJob = viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            mutableUiState.value.run {
                if (todayAnimes.isEmpty()) getTodayAiringAnimes()
                if (seasonAnimes.isEmpty()) getSeasonAnimes()
                if (isLoggedIn && recommendedAnimes.isEmpty()) getRecommendedAnimes()
            }
            mutableUiState.update { it.copy(isLoading = false) }
        }
    }

    override fun reloadMangaSection(section: HomeMangaSection) {
        loadMangaSection(section, force = true)
    }

    private fun loadMangaSection(section: HomeMangaSection, force: Boolean = false) {
        if (section == HomeMangaSection.READING && lastIsLoggedIn != true) return
        if (!force && (mangaJobs[section]?.isActive == true || section in loadedMangaSections)) return
        mangaJobs[section]?.cancel()
        mangaJobs[section] = viewModelScope.launch {
            setMangaSection(section, loading = true)
            val entries = when (section) {
                HomeMangaSection.READING -> mangaRepository.getUserMangaList(
                    status = ListStatusDto.READING,
                    sort = MediaSort.UPDATED,
                ).data?.map { HomeMangaItem(it.node, it.listStatus) }
                HomeMangaSection.POPULAR, HomeMangaSection.TOP_RATED ->
                    mangaRepository.getMangaRanking(
                        rankingType = if (section == HomeMangaSection.POPULAR)
                            RankingType.POPULARITY else RankingType.SCORE,
                        limit = 25,
                    ).data?.map { HomeMangaItem(it.node) }
            }
            currentCoroutineContext().ensureActive()
            if (entries != null) {
                loadedMangaSections.add(section)
                setMangaSection(section, data = entries.distinctBy { it.node.id }.take(25),
                    loading = false)
            } else {
                setMangaSection(section, loading = false, error = true)
            }
        }
    }

    private fun setMangaSection(
        section: HomeMangaSection,
        data: List<HomeMangaItem>? = null,
        loading: Boolean,
        error: Boolean = false,
    ) {
        mutableUiState.update {
            when (section) {
                HomeMangaSection.READING -> it.copy(
                    readingManga = data ?: it.readingManga,
                    isLoadingReadingManga = loading, readingMangaError = error,
                )
                HomeMangaSection.POPULAR -> it.copy(
                    popularManga = data ?: it.popularManga,
                    isLoadingPopularManga = loading, popularMangaError = error,
                )
                HomeMangaSection.TOP_RATED -> it.copy(
                    topRatedManga = data ?: it.topRatedManga,
                    isLoadingTopRatedManga = loading, topRatedMangaError = error,
                )
            }
        }
    }

    private suspend fun getTodayAiringAnimes() {
        val result = animeRepository.getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 100,
            fields = AnimeRepository.TODAY_FIELDS
        )
        currentCoroutineContext().ensureActive()
        if (result.data != null) {
            val tempList = mutableListOf<AnimeRanking>()
            for (anime in result.data) {
                if (anime.node.broadcast != null
                    && !tempList.contains(anime)
                    && anime.node.broadcast?.dayOfTheWeek == SeasonCalendar.currentJapanWeekday
                    && anime.node.status == MediaStatus.AIRING
                ) {
                    tempList.add(anime)
                }
            }
            tempList.sortByDescending { it.node.broadcast?.startTime }
            mutableUiState.update { it.copy(todayAnimes = tempList) }
        } else {
            showMessage(result.message ?: result.error)
        }
    }

    private suspend fun getSeasonAnimes() {
        val result = animeRepository.getSeasonalAnimes(
            sort = MediaSort.ANIME_NUM_USERS,
            startSeason = uiState.value.currentSeason,
            isNew = true,
            limit = 50,
            fields = "alternative_titles{en,ja},mean,start_season,my_list_status{status}",
        )
        currentCoroutineContext().ensureActive()
        result.data?.let { data -> mutableUiState.update { it.copy(seasonAnimes = data) } }
            ?: showMessage(result.message ?: result.error)
    }

    private suspend fun getRecommendedAnimes() {
        val result = animeRepository.getRecommendedAnimes(
            limit = 25
        )
        currentCoroutineContext().ensureActive()
        result.data?.let { data -> mutableUiState.update { it.copy(recommendedAnimes = data) } }
            ?: showMessage(result.message ?: result.error)
    }

    init {
        defaultPreferencesRepository.nsfw
            .distinctUntilChanged()
            .drop(1)
            .onEach {
                mangaJobs.values.forEach { job -> job.cancel() }
                loadedMangaSections.clear()
                mutableUiState.update { state ->
                    state.copy(todayAnimes = emptyList(), seasonAnimes = emptyList(),
                        recommendedAnimes = emptyList(), readingManga = emptyList(),
                        popularManga = emptyList(), topRatedManga = emptyList())
                }
                lastIsLoggedIn?.let(::initRequestChain)
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.titleLang
            .onEach { value ->
                mutableUiState.update { it.copy(preferredTitle = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)
    }
}
