package com.axiel7.moelist.screens.home

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.utils.SeasonCalendar
import com.axiel7.moelist.screens.home.composables.AiringAnimeHorizontalItem
import com.axiel7.moelist.screens.home.composables.HomeCard
import com.axiel7.moelist.screens.home.composables.HomeMangaCarousel
import com.axiel7.moelist.screens.home.composables.RecentWatchingCarousel
import com.axiel7.moelist.screens.magic.MagicPickerContext
import com.axiel7.moelist.screens.magic.RandomMagicControls
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.toBo
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
import com.axiel7.moelist.ui.composables.PlatformHorizontalScrollbar
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.media.LocalMediaQuickEdit
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.anime_menu
import com.axiel7.moelist.ui.generated.resources.calendar
import com.axiel7.moelist.ui.generated.resources.error_server
import com.axiel7.moelist.ui.generated.resources.ic_round_event_24
import com.axiel7.moelist.ui.generated.resources.ic_round_menu_book_24
import com.axiel7.moelist.ui.generated.resources.ic_round_movie_24
import com.axiel7.moelist.ui.generated.resources.manga_menu
import com.axiel7.moelist.ui.generated.resources.no_recommendations
import com.axiel7.moelist.ui.generated.resources.nothing_today
import com.axiel7.moelist.ui.generated.resources.please_login_to_use_this_feature
import com.axiel7.moelist.ui.generated.resources.recommendations
import com.axiel7.moelist.ui.generated.resources.seasonal_chart
import com.axiel7.moelist.ui.generated.resources.today
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeView(
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    topBarSnapTo: suspend (Float) -> Unit,
    padding: PaddingValues,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editRevision = LocalMediaQuickEdit.current?.revision ?: 0
    LaunchedEffect(editRevision) {
        if (isLoggedIn && editRevision > 0) {
            viewModel.refreshPersonalSections()
        }
    }

    HomeViewContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        isCompactScreen = isCompactScreen,
        navActionManager = navActionManager,
        topBarSnapTo = topBarSnapTo,
        padding = padding,
    )
}

@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    topBarSnapTo: suspend (Float) -> Unit = {},
    padding: PaddingValues = PaddingValues(),
) {
    //val context = LocalContext.current
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val seasonListState = rememberLazyListState()
    val recommendListState = rememberLazyListState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            //context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    // Home search bar is pinned: restore visibility when returning from a tab
    // that supports collapsing the shared top app bar.
    LaunchedEffect(isCompactScreen) {
        if (isCompactScreen) topBarSnapTo(0f)
    }

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    @Composable
    fun MangaSection(section: HomeMangaSection) {
        HomeMangaCarousel(
            section = section,
            entries = when (section) {
                HomeMangaSection.READING -> uiState.readingManga
                HomeMangaSection.RECOMMENDED -> uiState.recommendedManga
            },
            isLoading = when (section) {
                HomeMangaSection.READING -> uiState.isLoadingReadingManga
                HomeMangaSection.RECOMMENDED -> uiState.isLoadingRecommendedManga
            },
            hasError = when (section) {
                HomeMangaSection.READING -> uiState.readingMangaError
                HomeMangaSection.RECOMMENDED -> uiState.recommendedMangaError
            },
            preferredTitle = uiState.preferredTitle,
            hideScore = uiState.hideScore,
            navActionManager = navActionManager,
            onOpenAll = dropUnlessResumed {
                when (section) {
                    HomeMangaSection.READING -> navActionManager.toReadingManga()
                    HomeMangaSection.RECOMMENDED -> navActionManager.toMangaRecommendations()
                }
            },
            onRetry = { event?.reloadMangaSection(section) },
        )
    }

    @Composable
    fun RowScope.ChipRow1() {
        HomeCard(
            text = stringResource(UiRes.string.anime_menu),
            icon = UiRes.drawable.ic_round_movie_24,
            modifier = Modifier.weight(1f),
            onClick = dropUnlessResumed {
                navActionManager.toMediaRanking(MediaType.ANIME)
            },
        )

        HomeCard(
            text = stringResource(UiRes.string.manga_menu),
            icon = UiRes.drawable.ic_round_menu_book_24,
            modifier = Modifier.weight(1f),
            onClick = dropUnlessResumed {
                navActionManager.toMediaRanking(MediaType.MANGA)
            },
        )
    }

    @Composable
    fun RowScope.ChipRow2() {
        HomeCard(
            text = stringResource(UiRes.string.seasonal_chart),
            icon = SeasonCalendar.currentSeason.icon,
            modifier = Modifier.weight(1f),
            onClick = dropUnlessResumed {
                navActionManager.toSeasonChart()
            },
        )

        HomeCard(
            text = stringResource(UiRes.string.calendar),
            icon = UiRes.drawable.ic_round_event_24,
            modifier = Modifier.weight(1f),
            onClick = dropUnlessResumed {
                navActionManager.toCalendar()
            },
        )
    }
 
    Column(
        modifier = Modifier
            // Scroll the Home content only; the search bar stays in Scaffold.topBar.
            // Put Scaffold padding OUTSIDE the scrolling viewport: the Home feed
            // must not scroll behind the pinned search bar/status bar.
            .padding(padding)
            .verticalScroll(scrollState)
    ) {
        // Chips
        if (isCompactScreen) {
            Row(
                modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
            ) {
                ChipRow1()
            }

            Row(
                modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
            ) {
                ChipRow2()
            }
        } else {
            Row(
                modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
            ) {
                ChipRow1()
                ChipRow2()
            }
        }

        if (isLoggedIn) {
            RecentWatchingCarousel(
                entries = uiState.recentWatchingAnime,
                isLoading = uiState.isLoadingRecentWatching,
                hasError = uiState.recentWatchingError,
                preferredTitle = uiState.preferredTitle,
                navActionManager = navActionManager,
                onRetry = { event?.reloadRecentWatching() },
            )
            MangaSection(HomeMangaSection.READING)
            MangaSection(HomeMangaSection.RECOMMENDED)
        }

        // Airing
        HeaderHorizontalList(
            text = stringResource(UiRes.string.today),
            onClick = dropUnlessResumed { navActionManager.toCalendar() }
        )
        if (!uiState.isLoading && uiState.todayAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(UiRes.string.nothing_today),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
                state = airingListState,
                contentPadding = PaddingValues(horizontal = 8.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState)
            ) {
                items(
                    items = uiState.todayAnimes,
                    contentType = { it.node }
                ) {
                    AiringAnimeHorizontalItem(
                        item = it,
                        preferredTitle = uiState.preferredTitle,
                        hideScore = uiState.hideScore,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                        }
                    )
                }
                if (uiState.isLoading) {
                    items(5) {
                        MediaItemDetailedPlaceholder()
                    }
                }
            }

            PlatformHorizontalScrollbar(scrollState = airingListState)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // This Season
        HeaderHorizontalList(
            text = uiState.currentSeason.seasonYearText(),
            onClick = dropUnlessResumed { navActionManager.toSeasonChart() }
        )
        if (!uiState.isLoading && uiState.seasonAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(UiRes.string.error_server),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                state = seasonListState,
                contentPadding = PaddingValues(horizontal = 8.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonListState)
            ) {
                items(
                    items = uiState.seasonAnimes,
                    contentType = { it.node }
                ) {
                    MediaItemVertical(
                        mediaType = MediaType.ANIME,
mediaId = it.node.id,
imageUrl = it.node.mainPicture?.large,
                        title = it.node.title(uiState.preferredTitle),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        badgeContent = it.node.myListStatus?.status?.toBo()?.let { status ->
                            {
                                Icon(
                                    painter = painterResource(status.icon),
                                    contentDescription = status.localized(),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        subtitle = if (!uiState.hideScore) {
                            {
                                SmallScoreIndicator(
                                    score = it.node.mean,
                                )
                            }
                        } else null,
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                        }
                    )
                }
                if (uiState.isLoading) {
                    items(10) {
                        MediaItemVerticalPlaceholder(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            PlatformHorizontalScrollbar(scrollState = seasonListState)
        }

        //Recommended
        HeaderHorizontalList(
            text = stringResource(UiRes.string.recommendations),
            onClick = dropUnlessResumed { navActionManager.toRecommendations() }
        )
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(UiRes.string.please_login_to_use_this_feature),
                    textAlign = TextAlign.Center
                )
            }
        } else if (!uiState.isLoading && uiState.recommendedAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(UiRes.string.no_recommendations),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                state = recommendListState,
                contentPadding = PaddingValues(horizontal = 8.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = recommendListState)
            ) {
                items(
                    items = uiState.recommendedAnimes,
                    contentType = { it.node }
                ) {
                    MediaItemVertical(
                        mediaType = MediaType.ANIME,
mediaId = it.node.id,
imageUrl = it.node.mainPicture?.large,
                        title = it.node.title(uiState.preferredTitle),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        subtitle = if (!uiState.hideScore) {
                            {
                                SmallScoreIndicator(
                                    score = it.node.mean,
                                )
                            }
                        } else null,
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                        }
                    )
                }
                if (uiState.isLoading) {
                    items(10) {
                        MediaItemVerticalPlaceholder()
                    }
                }
            }

            PlatformHorizontalScrollbar(scrollState = recommendListState)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RandomMagicControls(
                context = MagicPickerContext(MediaType.ANIME),
                homeStyle = true,
                onOpen = { navActionManager.toMediaDetails(MediaType.ANIME, it.id) },
            )
            RandomMagicControls(
                context = MagicPickerContext(MediaType.MANGA),
                homeStyle = true,
                onOpen = { navActionManager.toMediaDetails(MediaType.MANGA, it.id) },
            )
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeViewContent(
                uiState = HomeUiState(),
                event = null,
                isLoggedIn = false,
                isCompactScreen = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
