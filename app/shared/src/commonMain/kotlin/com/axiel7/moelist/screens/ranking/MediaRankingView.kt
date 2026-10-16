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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingAnimeValues
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingMangaValues
import com.axiel7.moelist.screens.ranking.list.MediaRankingListView
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.anime_menu
import com.axiel7.moelist.ui.generated.resources.manga_menu
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaRankingView(
    arguments: Route.MediaRanking,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
) {
    val type = arguments.mediaType
    val snackbar = remember { SnackbarHostState() }
    val rankings = remember(type) {
        if (type == MediaType.ANIME) rankingAnimeValues.toList()
        else rankingMangaValues.toList()
    }
    val pager = rememberPagerState(
        initialPage = rankings.indexOf(arguments.initialRanking).coerceAtLeast(0),
    ) { rankings.size }
    val scope = rememberCoroutineScope()
    val selectedTab = pager.currentPage.coerceIn(0, rankings.lastIndex)

    DefaultScaffoldWithTopAppBar(
        title = stringResource(
            if (type == MediaType.ANIME) UiRes.string.anime_menu
            else UiRes.string.manga_menu
        ),
        navigateBack = { navActionManager.goBack() },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                divider = {},
            ) {
                rankings.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        text = { Text(tab.localized(), maxLines = 1) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0,
            ) { index ->
                MediaRankingListView(
                    arguments = arguments,
                    rankingType = rankings[index],
                    isCompactScreen = isCompactScreen,
                    snackbarHostState = snackbar,
                    navActionManager = navActionManager,
                )
            }
        }
    }
}
