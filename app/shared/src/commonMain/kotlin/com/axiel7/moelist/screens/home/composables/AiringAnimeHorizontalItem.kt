package com.axiel7.moelist.screens.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.toBo
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.media.LocalMediaQuickEdit
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import org.jetbrains.compose.resources.painterResource
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.ic_round_add_24

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiringAnimeHorizontalItem(
    item: AnimeRanking,
    preferredTitle: TitleLanguage,
    hideScore: Boolean,
    onClick: () -> Unit
) {
    val editor = LocalMediaQuickEdit.current
    val edit = { editor?.open(MediaType.ANIME, item.node.id) }
    val key = MediaType.ANIME to item.node.id
    val overridden = editor?.statuses?.containsKey(key) == true
    val status = if (overridden) editor?.statuses?.get(key)?.status?.toBo()
        else item.node.myListStatus?.status?.toBo()
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .sizeIn(maxWidth = 300.dp, minWidth = 250.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (editor != null) edit else null,
            )
    ) {
        Box(
            contentAlignment = Alignment.BottomStart
        ) {
            MediaPoster(
                url = item.node.mainPicture?.large,
                showShadow = false,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
            )

            if (status != null || editor != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .then(if (editor != null) Modifier.clickable { edit() } else Modifier)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(status?.icon ?: UiRes.drawable.ic_round_add_24),
                        contentDescription = status?.localized() ?: "Add to MAL",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = item.node.title(preferredTitle),
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = item.node.broadcast?.airingInString().orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (!hideScore) {
                SmallScoreIndicator(
                    score = item.node.mean
                )
            }
        }
    }
}