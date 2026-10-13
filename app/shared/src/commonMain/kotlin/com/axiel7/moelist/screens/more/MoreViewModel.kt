package com.axiel7.moelist.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoreViewModel(
    private val loginRepository: LoginRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : ViewModel(), MoreEvent {
    val showAboutMenu = defaultPreferencesRepository.showAboutMenu.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    override fun logOut() {
        viewModelScope.launch {
            loginRepository.logOut()
        }
    }
}
