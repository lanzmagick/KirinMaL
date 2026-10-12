package com.axiel7.moelist.screens.ranking.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.genre.AniListFilterKind
import com.axiel7.moelist.data.model.genre.MalAniListCatalog
import com.axiel7.moelist.data.model.genre.MalAniListCategory
import com.axiel7.moelist.data.model.genre.MalCategoryGroup
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.network.AniListMedia
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import kotlinx.coroutines.CancellationException

/**
 * One Genres tab only, with MAL's categories displayed in a two-column bento.
 * AniList discovers titles, then idMal routes edits to MyAnimeList.
 */
@Composable
fun AniListGenreBentoView(
    type: MediaType,
    showAdult: Boolean,
    api: JikanApi,
    selectedGenreId: Int,
    onGenreSelected: (Int) -> Unit,
    navActionManager: NavActionManager,
) {
    val catalog = remember(type) { MalAniListCatalog.forType(type) }
    val selected = catalog.firstOrNull { it.malGenreId == selectedGenreId }
    val visible = remember(catalog, showAdult) {
        catalog.filter { showAdult || !it.isAdult }
    }

    // Keep Explicit fully above the phone's gesture/navigation bar with a visible
    // resting gap in both Anime and Manga. The system inset alone is not enough.
    val safeBottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()

    if (selected == null) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = safeBottom + 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MalCategoryGroup.entries.forEach { group ->
                val entries = visible.filter { it.group == group }
                if (entries.isNotEmpty()) {
                    item(
                        key = group.name,
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                        )
                    }
                    items(
                        items = entries,
                        key = { group.name + ":" + it.malGenreId },
                    ) { entry ->
                        Surface(
                            onClick = { onGenreSelected(entry.malGenreId) },
                            modifier = Modifier.fillMaxWidth().height(84.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        val selectedGenre = selected ?: return
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = selectedGenre.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            AniListGenreMediaList(
                type = type,
                category = selectedGenre,
                showAdult = showAdult,
                api = api,
                navActionManager = navActionManager,
                bottomContentPadding = safeBottom + 64.dp,
            )
        }
    }
}

@Composable
private fun AniListGenreMediaList(
    type: MediaType,
    category: MalAniListCategory,
    showAdult: Boolean,
    api: JikanApi,
    navActionManager: NavActionManager,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
) {
    val results = remember(type, category.malGenreId, showAdult) { mutableStateListOf<AniListMedia>() }
    var page by remember(type, category.malGenreId, showAdult) { mutableIntStateOf(1) }
    var isLoading by remember(type, category.malGenreId, showAdult) { mutableStateOf(true) }
    var hasMore by remember(type, category.malGenreId, showAdult) { mutableStateOf(true) }
    var error by remember(type, category.malGenreId, showAdult) { mutableStateOf<String?>(null) }
    var retry by remember(type, category.malGenreId, showAdult) { mutableIntStateOf(0) }
    val canLoadMore by rememberUpdatedState(!isLoading && hasMore && error == null)

    LaunchedEffect(type, category.malGenreId, showAdult, page, retry) {
        isLoading = true
        error = null
        try {
            val payload = api.getAniListGenreMedia(
                type = type,
                genre = category.aniListFilter,
                page = page,
                showAdult = showAdult,
                filterByTag = category.filterKind == AniListFilterKind.TAG,
            )
            val knownIds = results.mapNotNullTo(mutableSetOf()) { it.idMal }
            payload.entries.filter { knownIds.add(it.idMal!!) }.forEach(results::add)
            hasMore = payload.hasNextPage
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            hasMore = false
            error = e.message ?: "AniList is temporarily unavailable."
        } finally {
            isLoading = false
        }
    }

    val listState = rememberLazyListState()
    listState.OnBottomReached(buffer = 4) {
        if (canLoadMore) page++
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomContentPadding),
    ) {
        items(results, key = { it.idMal!! }) { item ->
            MediaItemDetailed(
                mediaType = type,
                mediaId = item.idMal!!,
                title = item.title?.userPreferred ?: "MAL #${item.idMal}",
                imageUrl = item.coverImage?.large,
                subtitle1 = {
                    Text(item.format?.replace("_", " ") ?: "AniList",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                subtitle2 = {
                    Text(item.averageScore?.let { "AniList: $it / 100" } ?: "AniList",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                subtitle3 = {
                    Text("MyAnimeList", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = dropUnlessResumed {
                    navActionManager.toMediaDetails(type, item.idMal!!)
                },
            )
        }
        if (isLoading) item {
            Box(Modifier.fillParentMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        if (error != null) item {
            Column(
                modifier = Modifier.fillParentMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = {
                    if (page != 1) { page = 1; results.clear() }
                    retry++
                }) { Text("Retry AniList") }
            }
        }
        if (!isLoading && results.isEmpty() && error == null) item {
            Box(Modifier.fillParentMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Text("No AniList titles with a matching MyAnimeList ID for ${category.name}.")
            }
        }
    }
}
