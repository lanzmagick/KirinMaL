package com.axiel7.moelist.screens.recommendations

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.state.UiState
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

@Immutable
data class MangaRecommendationsUiState(
    val manga: List<MangaNode> = emptyList(),
    val preferredTitle: TitleLanguage = TitleLanguage.ROMAJI,
    val hideScore: Boolean = false,
    val hasError: Boolean = false,
    override val isLoading: Boolean = true,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}

class MangaRecommendationsViewModel(
    private val mangaRepository: MangaRepository,
    preferences: DefaultPreferencesRepository,
) : BaseViewModel<MangaRecommendationsUiState>() {
    override val mutableUiState = MutableStateFlow(MangaRecommendationsUiState())
    private var isVisible = false
    private var request: Job? = null

    fun activate() {
        isVisible = true
        refresh()
    }

    fun deactivate() {
        isVisible = false
        request?.cancel()
        mutableUiState.update {
            it.copy(manga = emptyList(), isLoading = false, hasError = false, message = null)
        }
    }

    fun refresh() {
        if (!isVisible) return
        request?.cancel()
        mutableUiState.update { it.copy(manga = emptyList(), isLoading = true, hasError = false) }
        request = viewModelScope.launch {
            val response = mangaRepository.getRecommendedManga(limit = 100)
            currentCoroutineContext().ensureActive()
            mutableUiState.update {
                it.copy(manga = response.data.orEmpty().map { entry -> entry.node },
                    isLoading = false, hasError = response.data == null)
            }
        }
    }

    init {
        preferences.nsfw.distinctUntilChanged().drop(1)
            .onEach { refresh() }.launchIn(viewModelScope)
        preferences.titleLang.onEach { value ->
            mutableUiState.update { it.copy(preferredTitle = value) }
        }.launchIn(viewModelScope)
        preferences.hideScores.onEach { value ->
            mutableUiState.update { it.copy(hideScore = value) }
        }.launchIn(viewModelScope)
    }
}
