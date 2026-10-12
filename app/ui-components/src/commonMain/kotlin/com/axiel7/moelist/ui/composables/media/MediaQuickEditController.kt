package com.axiel7.moelist.ui.composables.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.MediaType

data class MediaQuickEditTarget(val type: MediaType, val id: Int)

/** One editor for Home, Rankings, Seasonal, search and AniList-linked results. */
class MediaQuickEditController {
    var target by mutableStateOf<MediaQuickEditTarget?>(null)
        private set

    val statuses = mutableStateMapOf<Pair<MediaType, Int>, BaseMyListStatus?>()

    fun open(type: MediaType, id: Int) {
        if (id > 0) target = MediaQuickEditTarget(type, id)
    }

    fun dismiss() { target = null }

    fun onEdited(type: MediaType, id: Int, status: BaseMyListStatus?, removed: Boolean) {
        statuses[type to id] = if (removed) null else status
        dismiss()
    }
}

val LocalMediaQuickEdit = compositionLocalOf<MediaQuickEditController?> { null }
