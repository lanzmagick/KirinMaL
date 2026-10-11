package com.axiel7.moelist.screens.ranking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.genre.JikanGenre
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingAnimeValues
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingMangaValues
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.screens.ranking.list.GenreCatalogListView
import com.axiel7.moelist.screens.ranking.list.MediaRankingListView
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.anime_menu
import com.axiel7.moelist.ui.generated.resources.manga_menu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private data class CatalogTab(
    val ranking: RankingType? = null,
    val genre: JikanGenre? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaRankingView(
    arguments: Route.MediaRanking,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
) {
    val mediaType = arguments.mediaType
    val snackbarHostState = remember { SnackbarHostState() }
    val api: JikanApi = koinInject()
    val preferences: DefaultPreferencesRepository = koinInject()
    val showAdult by preferences.nsfw.collectAsStateWithLifecycle(initialValue = true)
    var genres by remember(mediaType) { mutableStateOf<List<JikanGenre>>(emptyList()) }
    var genresLoading by remember(mediaType) { mutableStateOf(true) }
    var genresError by remember(mediaType) { mutableStateOf<String?>(null) }
    var retryGenres by remember(mediaType) { mutableStateOf(0) }

    LaunchedEffect(mediaType, retryGenres) {
        genresLoading = true
        genresError = null
        try {
            genres = api.getGenres(mediaType).data
                .filter { it.malId > 0 && it.name.isNotBlank() }
                .distinctBy { it.malId }
                .sortedWith(compareBy<JikanGenre> { it.isAdult }.thenBy { it.name })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            genresError = e.message ?: "Could not load MAL genres."
        } finally {
            genresLoading = false
        }
    }

    val rankings = remember(mediaType) {
        if (mediaType == MediaType.ANIME) rankingAnimeValues.toList()
        else rankingMangaValues.toList()
    }
    val visibleGenres = if (showAdult) genres else genres.filterNot { it.isAdult }
    val tabs = rankings.map { CatalogTab(ranking = it) } +
        visibleGenres.map { CatalogTab(genre = it) }
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(
            if (mediaType == MediaType.ANIME) UiRes.string.anime_menu
            else UiRes.string.manga_menu
        ),
        navigateBack = navActionManager::goBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex),
                edgePadding = 12.dp,
                divider = {},
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.scrollToPage(index) } },
                        text = {
                            Text(
                                text = tab.ranking?.localized() ?: tab.genre?.name.orEmpty(),
                                maxLines = 1,
                            )
                        },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()
            if (genresLoading || genresError != null) {
                androidx.compose.foundation.layout.Row(Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
                    if (genresLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                        Text("Loading MAL genres…")
                    } else {
                        Text("MAL genres unavailable", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { retryGenres++ }) { Text("Retry") }
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0,
            ) { page ->
                when (val tab = tabs.getOrNull(page)) {
                    null -> Unit
                    else -> if (tab.ranking != null) {
                        MediaRankingListView(
                            arguments = arguments,
                            rankingType = tab.ranking,
                            isCompactScreen = isCompactScreen,
                            snackbarHostState = snackbarHostState,
                            navActionManager = navActionManager,
                        )
                    } else if (tab.genre != null) {
                        GenreCatalogListView(
                            genre = tab.genre,
                            mediaType = mediaType,
                            isCompactScreen = isCompactScreen,
                            showAdult = showAdult,
                            api = api,
                            navActionManager = navActionManager,
                        )
                    }
                }
            }
        }
    }
}
