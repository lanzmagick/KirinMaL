package com.axiel7.moelist.screens.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.media.LocalMediaQuickEdit
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.error_server
import com.axiel7.moelist.ui.generated.resources.home_recommended_manga
import com.axiel7.moelist.ui.generated.resources.manga_recommendations_hint
import com.axiel7.moelist.ui.generated.resources.no_recommendations
import com.axiel7.moelist.ui.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MangaRecommendationsView(navActionManager: NavActionManager) {
    val viewModel: MangaRecommendationsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editRevision = LocalMediaQuickEdit.current?.revision ?: 0
    val enteredRevision = remember(viewModel) { editRevision }
    DisposableEffect(viewModel) {
        viewModel.activate()
        onDispose { viewModel.deactivate() }
    }
    LaunchedEffect(editRevision) {
        if (editRevision != enteredRevision) viewModel.refresh()
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(UiRes.string.home_recommended_manga),
        navigateBack = navActionManager::goBack,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(MEDIA_POSTER_SMALL_WIDTH.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(UiRes.string.manga_recommendations_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!state.isLoading && state.manga.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(if (state.hasError) UiRes.string.error_server
                        else UiRes.string.no_recommendations), textAlign = TextAlign.Center)
                    if (state.hasError) TextButton(onClick = viewModel::refresh) {
                        Text(stringResource(UiRes.string.retry))
                    }
                }
            }
            items(state.manga, key = { it.id }) { node ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    MediaItemVertical(
                        mediaType = MediaType.MANGA,
                        mediaId = node.id,
                        imageUrl = node.mainPicture?.large ?: node.mainPicture?.medium,
                        title = node.title(state.preferredTitle),
                        minLines = 2,
                        subtitle = if (!state.hideScore) {
                            { SmallScoreIndicator(score = node.mean) }
                        } else null,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.MANGA, node.id)
                        },
                    )
                }
            }
            if (state.isLoading) items(12) { MediaItemVerticalPlaceholder() }
        }
    }
}
