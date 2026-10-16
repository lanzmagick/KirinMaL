package com.axiel7.moelist.screens.userlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.ui.ListStyle
import com.axiel7.moelist.screens.userlist.composables.CompactUserMediaListItem
import com.axiel7.moelist.screens.userlist.composables.CompactUserMediaListItemPlaceholder
import com.axiel7.moelist.screens.userlist.composables.GridUserMediaListItem
import com.axiel7.moelist.screens.userlist.composables.GridUserMediaListItemPlaceholder
import com.axiel7.moelist.screens.userlist.composables.MinimalUserMediaListItem
import com.axiel7.moelist.screens.userlist.composables.MinimalUserMediaListItemPlaceholder
import com.axiel7.moelist.screens.userlist.composables.StandardUserMediaListItem
import com.axiel7.moelist.screens.userlist.composables.StandardUserMediaListItemPlaceholder
import com.axiel7.moelist.screens.userlist.composables.UserListToolbar
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.PlatformVerticalScrollbar
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.retry
import com.axiel7.moelist.ui.generated.resources.title_initial_empty
import com.axiel7.moelist.ui.generated.resources.title_initial_load_failed
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserMediaListView(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    isCompactScreen: Boolean,
    modifier: Modifier = Modifier,
    showSortInToolbar: Boolean = true,
    nestedScrollConnection: NestedScrollConnection? = null,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Float = 0f,
    topBarAnimateTo: suspend (Float) -> Unit = {},
    topBarSnapTo: suspend (Float) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    onShowEditSheet: (BaseUserMediaList<out BaseMediaNode>) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val pullRefreshState = rememberPullToRefreshState()
    val visibleMediaList = uiState.visibleMediaList
    val showInitialMessage = uiState.titleInitial.isActive &&
            (visibleMediaList.isEmpty() || uiState.listLoadFailed) &&
            !uiState.isLoading && !uiState.isLoadingMore

    @Composable
    fun InitialFilterMessage() {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(if (uiState.listLoadFailed) UiRes.string.title_initial_load_failed
                    else UiRes.string.title_initial_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.listLoadFailed) {
                TextButton(onClick = {
                    if (uiState.nextPage == null) event?.refreshList() else event?.loadMore()
                }) { Text(stringResource(UiRes.string.retry)) }
            }
        }
    }

    @Composable
    fun StandardItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        StandardUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            preferredTitle = uiState.preferredTitle,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun CompactItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        CompactUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            preferredTitle = uiState.preferredTitle,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun MinimalItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        MinimalUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            preferredTitle = uiState.preferredTitle,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun GridItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        GridUserMediaListItem(
            item = item,
            preferredTitle = uiState.preferredTitle,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { event?.refreshList() },
        modifier = modifier.fillMaxSize(),
        state = pullRefreshState,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullRefreshState,
                isRefreshing = uiState.isLoading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
            )
        }
    ) {
        val listModifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopStart)
            .then(
                if (nestedScrollConnection != null)
                    Modifier.nestedScroll(nestedScrollConnection)
                else Modifier
            )

        if (uiState.listStyle == ListStyle.GRID) {
            val listState = rememberLazyGridState()
            var lastInitial by remember { mutableStateOf(uiState.titleInitial) }
            LaunchedEffect(uiState.titleInitial) {
                if (lastInitial != uiState.titleInitial) {
                    listState.scrollToItem(0)
                    lastInitial = uiState.titleInitial
                }
            }
            LazyVerticalGrid(
                columns = if (uiState.itemsPerRow.value > 0) GridCells.Fixed(uiState.itemsPerRow.value)
                else GridCells.Adaptive(minSize = (MEDIA_POSTER_MEDIUM_WIDTH + 8).dp),
                modifier = listModifier
                    .collapsable(
                        state = listState,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        animateTo = topBarAnimateTo,
                        snapTo = topBarSnapTo,
                    ),
                state = listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                item(
                    span = { GridItemSpan(maxCurrentLineSpan) }
                ) {
                    UserListToolbar(
                        showSort = showSortInToolbar,
                        uiState = uiState,
                        event = event,
                        navActionManager = navActionManager,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
                if (showInitialMessage) item(span = { GridItemSpan(maxLineSpan) }) {
                    InitialFilterMessage()
                }
                items(
                    items = visibleMediaList,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    GridItemView(item = item)
                }
                if (uiState.isLoadingMore) {
                    items(9, contentType = { it }) {
                        GridUserMediaListItemPlaceholder()
                    }
                }
                item(contentType = { 0 }) {
                    if (uiState.canLoadMore && !uiState.titleInitial.isActive) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        LaunchedEffect(true) {
                            event?.loadMore()
                        }
                    }
                }
            }
            PlatformVerticalScrollbar(
                scrollState = listState,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        } else if (isCompactScreen) {
            val listState = rememberLazyListState()
            var lastInitial by remember { mutableStateOf(uiState.titleInitial) }
            LaunchedEffect(uiState.titleInitial) {
                if (lastInitial != uiState.titleInitial) {
                    listState.scrollToItem(0)
                    lastInitial = uiState.titleInitial
                }
            }
            listState.OnBottomReached(buffer = 3) {
                if (!uiState.titleInitial.isActive) event?.loadMore()
            }
            LazyColumn(
                modifier = listModifier
                    .collapsable(
                        state = listState,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        animateTo = topBarAnimateTo,
                        snapTo = topBarSnapTo,
                    ),
                state = listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 8.dp
                ),
            ) {
                item {
                    UserListToolbar(
                        showSort = showSortInToolbar,
                        uiState = uiState,
                        event = event,
                        navActionManager = navActionManager,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (showInitialMessage) item { InitialFilterMessage() }
                when (uiState.listStyle) {
                    ListStyle.STANDARD -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }
                }
            }//:LazyColumn
            PlatformVerticalScrollbar(
                scrollState = listState,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        } else { // tablet ui
            val listState = rememberLazyGridState()
            var lastInitial by remember { mutableStateOf(uiState.titleInitial) }
            LaunchedEffect(uiState.titleInitial) {
                if (lastInitial != uiState.titleInitial) {
                    listState.scrollToItem(0)
                    lastInitial = uiState.titleInitial
                }
            }
            val padding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                top = contentPadding.calculateTopPadding() + 8.dp,
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding() + 8.dp
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listState,
                contentPadding = padding,
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    UserListToolbar(
                        showSort = showSortInToolbar,
                        uiState = uiState,
                        event = event,
                        navActionManager = navActionManager,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
                if (showInitialMessage) item(span = { GridItemSpan(maxLineSpan) }) {
                    InitialFilterMessage()
                }
                when (uiState.listStyle) {
                    ListStyle.STANDARD -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL -> {
                        items(
                            items = visibleMediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }
                }
                item(contentType = { 0 }) {
                    if (uiState.canLoadMore && !uiState.titleInitial.isActive) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        LaunchedEffect(true) {
                            event?.loadMore()
                        }
                    }
                }
            }
            PlatformVerticalScrollbar(
                scrollState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(padding),
            )
        }
    }//:Box
}
