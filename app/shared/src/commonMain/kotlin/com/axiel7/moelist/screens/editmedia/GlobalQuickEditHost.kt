package com.axiel7.moelist.screens.editmedia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.composables.media.MediaQuickEditController
import org.koin.compose.koinInject

/**
 * One instance hosts the existing MAL edit sheet for all media cards.
 * Details are fetched from MAL before opening; AniList IDs are never used.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalQuickEditHost(controller: MediaQuickEditController) {
    val target = controller.target ?: return
    val animeRepository: AnimeRepository = koinInject()
    val mangaRepository: MangaRepository = koinInject()
    var mediaNode by remember(target) { mutableStateOf<BaseMediaNode?>(null) }
    var listStatus by remember(target) { mutableStateOf<BaseMyListStatus?>(null) }
    var loading by remember(target) { mutableStateOf(true) }
    var error by remember(target) { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(target) {
        loading = true
        error = null
        val details = if (target.type == MediaType.ANIME) {
            animeRepository.getAnimeDetails(target.id)
        } else {
            mangaRepository.getMangaDetails(target.id)
        }
        when (details) {
            is com.axiel7.moelist.data.model.anime.AnimeDetails -> {
                mediaNode = details.toAnimeNode()
                listStatus = details.myListStatus
            }
            is com.axiel7.moelist.data.model.manga.MangaDetails -> {
                mediaNode = details.toMangaNode()
                listStatus = details.myListStatus
            }
            else -> error = "Unable to load MyAnimeList details."
        }
        loading = false
    }

    if (mediaNode != null) {
        val node = mediaNode!!
        androidx.compose.runtime.key(target.type, target.id) {
            EditMediaSheet(
                sheetState = sheetState,
                mediaInfo = node,
                myListStatus = listStatus,
                onEdited = { newStatus, removed ->
                    controller.onEdited(target.type, target.id, newStatus, removed)
                },
                onDismissed = controller::dismiss,
            )
        }
    } else if (loading || error != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text("MyAnimeList") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (loading) CircularProgressIndicator()
                    else Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = controller::dismiss) { Text("Close") }
            },
        )
    }
}
