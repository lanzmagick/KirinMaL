package com.axiel7.moelist.screens.home.composables

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.utils.UNKNOWN_CHAR
import com.axiel7.moelist.ui.base.model.ListStatus
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.toBo
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
import com.axiel7.moelist.ui.composables.PlatformHorizontalScrollbar
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.episodes
import com.axiel7.moelist.ui.generated.resources.error_server
import com.axiel7.moelist.ui.generated.resources.home_recent_watching
import com.axiel7.moelist.ui.generated.resources.no_recent_watching
import com.axiel7.moelist.ui.generated.resources.retry
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecentWatchingCarousel(
    entries: List<UserAnimeList>,
    isLoading: Boolean,
    hasError: Boolean,
    preferredTitle: TitleLanguage,
    navActionManager: NavActionManager,
    onRetry: () -> Unit,
) {
    val listState = rememberLazyListState()
    HeaderHorizontalList(
        stringResource(UiRes.string.home_recent_watching),
        onClick = dropUnlessResumed { navActionManager.toWatchingAnime() },
    )
    if (hasError) Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(UiRes.string.error_server), modifier = Modifier.weight(1f))
        TextButton(onClick = onRetry) { Text(stringResource(UiRes.string.retry)) }
    }
    if (!isLoading && entries.isEmpty()) {
        if (!hasError) Box(
            modifier = Modifier.fillMaxWidth().height(MEDIA_POSTER_SMALL_HEIGHT.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(UiRes.string.no_recent_watching), textAlign = TextAlign.Center)
        }
    } else {
        LazyRow(
            state = listState,
            modifier = Modifier.sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        ) {
            items(entries, key = { it.node.id }) { item ->
                val status = item.listStatus?.status?.toBo() ?: ListStatus.WATCHING
                MediaItemVertical(
                    mediaType = MediaType.ANIME,
                    mediaId = item.node.id,
                    imageUrl = item.node.mainPicture?.large ?: item.node.mainPicture?.medium,
                    title = item.node.title(preferredTitle),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    minLines = 2,
                    badgeStatus = status,
                    badgeContent = {
                        Icon(painterResource(status.icon), contentDescription = status.localized(),
                            modifier = Modifier.size(20.dp), tint = status.onBadgeColor)
                    },
                    subtitle = {
                        val total = item.node.numEpisodes?.takeIf { it > 0 }
                        Text(
                            "${item.listStatus?.progress ?: 0} / ${total ?: UNKNOWN_CHAR} " +
                                    stringResource(UiRes.string.episodes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                    },
                )
            }
            if (isLoading && entries.isEmpty()) items(5) {
                MediaItemVerticalPlaceholder(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        PlatformHorizontalScrollbar(scrollState = listState)
    }
}
