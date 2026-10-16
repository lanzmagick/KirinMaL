package com.axiel7.moelist.main

import androidx.navigation3.runtime.NavKey
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.Route

internal fun listSortMediaType(route: NavKey?, isLoggedIn: Boolean): MediaType? {
    if (!isLoggedIn) return null
    // Anime/Manga tabs are parameterized routes, not singleton objects.
    return when (route) {
        is Route.Tab.Anime -> MediaType.ANIME
        is Route.Tab.Manga -> MediaType.MANGA
        else -> null
    }
}
