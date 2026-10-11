package com.axiel7.moelist.screens.ranking.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.genre.JikanGenre
import com.axiel7.moelist.data.model.genre.JikanGenreMedia
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import kotlinx.coroutines.CancellationException

/**
 * Genre pages use real Jikan/MAL genre IDs; never show fake or unfiltered results.
 * Compose state is scoped by type/genre so swiping to a different tab resets paging.
 */
@Composable
fun GenreCatalogListView(
    genre: JikanGenre,
    mediaType: MediaType,
    isCompactScreen: Boolean,
    showAdult: Boolean,
    api: JikanApi,
    navActionManager: NavActionManager,
) {
    val media = remember(mediaType, genre.malId, showAdult) {
        mutableStateListOf<JikanGenreMedia>()
    }
    var page by remember(mediaType, genre.malId, showAdult) { mutableIntStateOf(1) }
    var hasMore by remember(mediaType, genre.malId, showAdult) { mutableStateOf(true) }
    var loading by remember(mediaType, genre.malId, showAdult) { mutableStateOf(true) }
    var error by remember(mediaType, genre.malId, showAdult) { mutableStateOf<String?>(null) }
    var retryVersion by remember(mediaType, genre.malId, showAdult) { mutableIntStateOf(0) }
    // OnBottomReached holds a long-lived callback: read the latest flags.
    val mayLoadMore by rememberUpdatedState(!loading && hasMore && error == null)

    LaunchedEffect(mediaType, genre.malId, showAdult, page, retryVersion) {
        loading = true
        error = null
        try {
            val result = api.getGenreMedia(mediaType, genre.malId, page, showAdult)
            val known = media.mapTo(mutableSetOf()) { it.malId }
            result.data.filter { known.add(it.malId) }.forEach(media::add)
            hasMore = result.pagination?.hasNextPage == true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            hasMore = false
            error = e.message ?: "Could not load this MAL genre."
        } finally {
            loading = false
        }
    }

    @Composable
    fun GenreItem(item: JikanGenreMedia) {
        MediaItemDetailed(
            title = item.title,
            imageUrl = item.imageUrl,
            subtitle1 = { Text(item.type ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            subtitle2 = {
                Text(
                    item.score?.let { "★ $it" } ?: "★ —",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            subtitle3 = { Text(genre.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(mediaType, item.malId)
            },
        )
    }

    if (isCompactScreen) {
        val listState = rememberLazyListState()
        listState.OnBottomReached(buffer = 4) {
            if (mayLoadMore) page++
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            items(media, key = { it.malId }) { GenreItem(it) }
            if (loading) item {
                Box(Modifier.fillParentMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            if (error != null) item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = {
                        if (page > 1) page = 1
                        media.clear()
                        retryVersion++
                    }) { Text("Retry") }
                }
            }
            if (!loading && media.isEmpty() && error == null) item {
                Box(Modifier.fillParentMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No titles found for ${genre.name}.")
                }
            }
        }
    } else {
        val gridState = rememberLazyGridState()
        gridState.OnBottomReached(buffer = 4) {
            if (mayLoadMore) page++
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            items(media, key = { it.malId }) { GenreItem(it) }
            if (loading) item {
                Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            if (error != null) item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = {
                        if (page > 1) page = 1
                        media.clear()
                        retryVersion++
                    }) { Text("Retry") }
                }
            }
            if (!loading && media.isEmpty() && error == null) item {
                Text("No titles found for ${genre.name}.")
            }
        }
    }
}
