package com.axiel7.moelist.screens.home

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus

enum class HomeMangaSection { READING, POPULAR, TOP_RATED }

@Immutable
data class HomeMangaItem(
    val node: MangaNode,
    val listStatus: MyMangaListStatus? = null,
)
