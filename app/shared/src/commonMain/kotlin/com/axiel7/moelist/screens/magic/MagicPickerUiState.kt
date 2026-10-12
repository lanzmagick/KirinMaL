package com.axiel7.moelist.screens.magic

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.screens.userlist.TitleInitialFilter
import com.axiel7.moelist.ui.base.model.ListStatus

data class MagicPickerContext(
    val mediaType: MediaType,
    val listStatus: ListStatus? = null,
    val titleInitial: TitleInitialFilter = TitleInitialFilter.ALL,
)

enum class MagicPickerError { EMPTY, UNAVAILABLE }

data class MagicPickerUiState(
    val isLoading: Boolean = false,
    val selection: BaseMediaNode? = null,
    val error: MagicPickerError? = null,
    val preferredTitle: TitleLanguage = TitleLanguage.ROMAJI,
)
