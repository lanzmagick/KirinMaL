package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.toBo
import com.axiel7.moelist.ui.base.model.ListStatus
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.check_circle_outline_24
import com.axiel7.moelist.ui.generated.resources.ic_round_add_24
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.jetbrains.compose.resources.painterResource

const val MEDIA_ITEM_VERTICAL_HEIGHT = 200

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaItemVertical(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    badgeContent: @Composable (RowScope.() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    subtitle2: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    mediaType: MediaType? = null,
    mediaId: Int? = null,
    onLongClick: (() -> Unit)? = null,
    onBadgeClick: (() -> Unit)? = null,
    badgeStatus: ListStatus? = null,
    onClick: () -> Unit,
) {
    val editor = LocalMediaQuickEdit.current
    val key = if (mediaType != null && mediaId != null) mediaType to mediaId else null
    val canEdit = editor != null && key != null
    val editAction = onLongClick ?: if (canEdit) {
        { editor!!.open(mediaType!!, mediaId!!) }
    } else null
    val badgeAction = onBadgeClick ?: editAction
    val cached = canEdit && editor!!.statuses.containsKey(key)
    val synced = canEdit && editor!!.syncedTypes[key!!.first] == true
    val malStatus = if (cached) editor!!.statuses[key]?.status?.toBo() else null
    val displayedStatus = if (cached || synced) malStatus else badgeStatus
    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp)
            .sizeIn(
                minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = editAction),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            MediaPoster(
                url = imageUrl,
                showShadow = false,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
            )

            if (badgeContent != null || (canEdit && (cached || synced))) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp))
                        .background(displayedStatus?.badgeColor ?: MaterialTheme.colorScheme.primaryContainer)
                        .then(if (badgeAction != null) Modifier.clickable(onClick = badgeAction) else Modifier)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when {
                        cached || synced -> Icon(
                            painter = painterResource(malStatus?.icon ?: UiRes.drawable.ic_round_add_24),
                            contentDescription = malStatus?.localized() ?: "Add to MAL",
                            modifier = Modifier.size(20.dp),
                            tint = malStatus?.onBadgeColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        badgeContent != null -> badgeContent()
                        canEdit -> Icon(
                            painter = painterResource(UiRes.drawable.ic_round_add_24),
                            contentDescription = "Add to MAL",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .width(MEDIA_POSTER_SMALL_WIDTH.dp)
                .padding(top = 8.dp, bottom = 2.dp),
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )

        subtitle?.let { it() }
        subtitle2?.let { it() }
    }
}

@Composable
fun MediaItemVerticalPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .size(
                width = (MEDIA_POSTER_SMALL_WIDTH + 8).dp,
                height = MEDIA_ITEM_VERTICAL_HEIGHT.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
                .defaultPlaceholder(visible = true)
        )

        Text(
            text = "This is a placeholder",
            modifier = Modifier
                .padding(top = 8.dp)
                .defaultPlaceholder(visible = true),
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Preview
@Composable
fun MediaItemVerticalPreview() {
    MoeListTheme {
        Surface {
            MediaItemVertical(
                imageUrl = null,
                title = "This is a very large anime title that should serve as a preview example",
                badgeContent = {
                    Icon(
                        painter = painterResource(UiRes.drawable.check_circle_outline_24),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                subtitle = {
                    SmallScoreIndicator(
                        score = 8.34f,
                    )
                },
                onClick = {}
            )
        }
    }
}
