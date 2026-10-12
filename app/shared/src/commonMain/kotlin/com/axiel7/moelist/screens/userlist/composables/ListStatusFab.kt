package com.axiel7.moelist.screens.userlist.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.model.ListStatus
import com.axiel7.moelist.ui.base.model.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.round_close_24
import org.jetbrains.compose.resources.painterResource

/** Floating bento: no enclosing card; the fifth status spans both columns. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListStatusFab(
    mediaType: MediaType,
    status: ListStatus,
    onStatusChanged: (ListStatus) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(mediaType) { mutableStateOf(false) }
    val options = remember(mediaType) { listStatusValues(mediaType).toList() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            // Restore free-floating pill tiles without the old large background.
            Column(
                modifier = Modifier.width(302.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.chunked(2).forEach { rowOptions ->
                    val isLastWideRow = rowOptions.size == 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowOptions.forEach { item ->
                            val selected = item == status
                            val contentColor = item.onBadgeColor
                            Surface(
                                onClick = {
                                    onStatusChanged(item)
                                    expanded = false
                                },
                                // A single last tile fills both columns; no empty spacer.
                                modifier = Modifier.weight(1f).height(72.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = item.badgeColor,
                                shadowElevation = 2.dp,
                                border = if (selected) BorderStroke(
                                    width = 1.5.dp,
                                    color = contentColor,
                                ) else null,
                            ) {
                                if (isLastWideRow) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(item.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = contentColor,
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = item.localized(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = contentColor,
                                            maxLines = 1,
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(item.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = contentColor,
                                        )
                                        Text(
                                            text = item.localized(),
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = contentColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.animateFloatingActionButton(
                visible = isVisible || expanded,
                alignment = Alignment.BottomEnd,
            ),
            containerColor = status.badgeColor,
            contentColor = status.onBadgeColor,
        ) {
            Icon(
                painter = painterResource(
                    if (expanded) UiRes.drawable.round_close_24 else status.icon
                ),
                contentDescription = if (expanded) "Close status menu" else "Select list status",
            )
        }
    }
}
