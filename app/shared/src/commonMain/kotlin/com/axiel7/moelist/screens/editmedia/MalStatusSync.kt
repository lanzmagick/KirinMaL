package com.axiel7.moelist.screens.editmedia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.composables.media.MediaQuickEditController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Hydrate all MyAnimeList statuses immediately after account login. */
@Composable
fun MalStatusSync(controller: MediaQuickEditController) {
    val animeRepository: AnimeRepository = koinInject()
    val mangaRepository: MangaRepository = koinInject()
    LaunchedEffect(controller) {
        launch {
            animeRepository.loadAllUserAnimeStatuses()?.let { statuses ->
                controller.seedFromMal(MediaType.ANIME, statuses)
            }
        }
        launch {
            mangaRepository.loadAllUserMangaStatuses()?.let { statuses ->
                controller.seedFromMal(MediaType.MANGA, statuses)
            }
        }
    }
}
