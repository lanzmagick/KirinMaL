package com.axiel7.moelist.screens.ranking.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.screens.ranking.GenreSystemBackHandler
import com.axiel7.moelist.data.network.AniListCatalogApi
import com.axiel7.moelist.data.network.AniListMedia
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import kotlinx.coroutines.CancellationException

/**
 * Exactly ONE additional tab in Anime/Manga Menu. Inside it, genres are bento
 * tiles and AniList results are mapped by idMal into native MAL detail screens.
 */
@Composable
fun AniListGenreBentoView(
    type: MediaType,
    showAdult: Boolean,
    api: JikanApi,
    navActionManager: NavActionManager,
) {
    val defaults = remember { AniListCatalogApi.offlineGenres }
    var genres by remember { mutableStateOf(defaults) }
    var selectedGenre by remember(type) { mutableStateOf<String?>(null) }
    // Android hardware/system Back closes only the selected genre first.
    GenreSystemBackHandler(enabled = selectedGenre != null) {
        selectedGenre = null
    }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var catalogRetry by remember { mutableIntStateOf(0) }

    LaunchedEffect(catalogRetry) {
        try {
            val live = api.getAniListGenres()
            if (live.isNotEmpty()) genres = live
            catalogError = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Bento keeps a usable curated AniList genre list while offline.
            catalogError = e.message ?: "AniList genres could not be refreshed."
        }
    }

    val visibleGenres = genres.filterNot {
        !showAdult && (it.equals("Hentai", true) || it.equals("Erotica", true))
    }

    if (selectedGenre == null) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleGenres, key = { it }) { genre ->
                Surface(
                    onClick = { selectedGenre = genre },
                    modifier = Modifier.fillMaxWidth().height(84.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (catalogError != null) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(onClick = { catalogRetry++ }) {
                            Text("Refresh AniList genres")
                        }
                    }
                }
            }
        }
    } else {
        val genre = selectedGenre ?: return
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
            AniListGenreMediaList(
                type = type,
                genre = genre,
                showAdult = showAdult,
                api = api,
                navActionManager = navActionManager,
            )
        }
    }
}

@Composable
private fun AniListGenreMediaList(
    type: MediaType,
    genre: String,
    showAdult: Boolean,
    api: JikanApi,
    navActionManager: NavActionManager,
) {
    val results = remember(type, genre, showAdult) { mutableStateListOf<AniListMedia>() }
    var page by remember(type, genre, showAdult) { mutableIntStateOf(1) }
    var isLoading by remember(type, genre, showAdult) { mutableStateOf(true) }
    var hasMore by remember(type, genre, showAdult) { mutableStateOf(true) }
    var error by remember(type, genre, showAdult) { mutableStateOf<String?>(null) }
    var retry by remember(type, genre, showAdult) { mutableIntStateOf(0) }
    val canLoadMore by rememberUpdatedState(!isLoading && hasMore && error == null)

    LaunchedEffect(type, genre, showAdult, page, retry) {
        isLoading = true
        error = null
        try {
            val payload = api.getAniListGenreMedia(type, genre, page, showAdult)
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
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(results, key = { it.idMal!! }) { item ->
            MediaItemDetailed(
                title = item.title?.userPreferred ?: "MAL #${item.idMal}",
                imageUrl = item.coverImage?.large,
                subtitle1 = {
                    Text(
                        item.format?.replace("_", " ") ?: "AniList",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                subtitle2 = {
                    Text(
                        item.averageScore?.let { "AniList: $it / 100" } ?: "AniList",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                subtitle3 = {
                    Text("Open MAL details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = dropUnlessResumed {
                    // NEVER pass an AniList internal ID into the MAL repository.
                    navActionManager.toMediaDetails(type, item.idMal!!)
                },
            )
        }
        if (isLoading) item {
            Box(
                modifier = Modifier.fillParentMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
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
            Box(
                modifier = Modifier.fillParentMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No titles with a MyAnimeList ID for this genre.")
            }
        }
    }
}
