package com.axiel7.moelist.screens.profile

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.model.ListStatus
import com.axiel7.moelist.ui.base.model.Stat
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<ProfileUiState>(), ProfileEvent {

    override val mutableUiState = MutableStateFlow(ProfileUiState())

    override fun retryMangaStats() {
        mutableUiState.value.user?.name?.takeIf(String::isNotBlank)?.let(::loadMangaStats)
    }

    private fun loadMangaStats(username: String) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingManga = true, mangaStatsError = null) }
            val result = userRepository.getUserStats(username)
            val manga = result.data?.manga
            mutableUiState.update {
                if (manga != null) {
                    it.copy(
                        userMangaStats = manga,
                        isLoadingManga = false,
                        mangaStatsError = null,
                    )
                } else {
                    it.copy(
                        isLoadingManga = false,
                        mangaStatsError = result.message ?: result.error
                            ?: "MAL manga statistics are temporarily unavailable.",
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            setLoading(true)
            val user = userRepository.getMyUser()

            if (user == null || user.message != null || user.error != null) {
                showMessage(user?.message ?: user?.error ?: "Unable to load MAL profile.")
                mutableUiState.update {
                    it.copy(isLoading = false, isLoadingManga = false)
                }
            } else {
                val tempAnimeStatList = mutableListOf<Stat<ListStatus>>()
                user.animeStatistics?.let { stats ->
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.WATCHING,
                            value = stats.numItemsWatching?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.COMPLETED,
                            value = stats.numItemsCompleted?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.ON_HOLD,
                            value = stats.numItemsOnHold?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.DROPPED,
                            value = stats.numItemsDropped?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.PLAN_TO_WATCH,
                            value = stats.numItemsPlanToWatch?.toFloat() ?: 0f
                        )
                    )

                    if (user.picture != null && user.picture != mutableUiState.value.profilePictureUrl) {
                        defaultPreferencesRepository.setProfilePicture(user.picture)
                    }
                }

                mutableUiState.update {
                    it.copy(
                        user = user,
                        profilePictureUrl = user.picture,
                        animeStats = tempAnimeStatList.toImmutableList(),
                        isLoading = false,
                        isLoadingManga = !user.name.isNullOrBlank(),
                    )
                }
                user.name?.takeIf(String::isNotBlank)?.let(::loadMangaStats)
                    ?: mutableUiState.update {
                        it.copy(
                            isLoadingManga = false,
                            mangaStatsError = "MAL username is missing."
                        )
                    }
            }
        }
    }
}