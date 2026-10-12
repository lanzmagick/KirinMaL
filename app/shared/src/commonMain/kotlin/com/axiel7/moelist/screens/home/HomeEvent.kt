package com.axiel7.moelist.screens.home

import androidx.compose.runtime.Stable
import com.axiel7.moelist.ui.base.event.UiEvent

@Stable
interface HomeEvent : UiEvent {
    fun initRequestChain(isLoggedIn: Boolean)
    fun reloadMangaSection(section: HomeMangaSection)
    fun reloadRecentWatching()
    fun refreshPersonalSections()
}
