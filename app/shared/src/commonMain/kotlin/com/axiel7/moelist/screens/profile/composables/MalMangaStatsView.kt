package com.axiel7.moelist.screens.profile.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.chapters
import com.axiel7.moelist.ui.generated.resources.completed
import com.axiel7.moelist.ui.generated.resources.days
import com.axiel7.moelist.ui.generated.resources.dropped
import com.axiel7.moelist.ui.generated.resources.manga_stats
import com.axiel7.moelist.ui.generated.resources.mean_score
import com.axiel7.moelist.ui.generated.resources.on_hold
import com.axiel7.moelist.ui.generated.resources.ptr
import com.axiel7.moelist.ui.generated.resources.reading
import com.axiel7.moelist.ui.generated.resources.total_entries_label
import com.axiel7.moelist.ui.generated.resources.total_rereads
import com.axiel7.moelist.ui.generated.resources.volumes
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * MAL website-style Manga Stats, adapted to KirinMaL from KirinNime.
 * Jikan supplies these totals; the UI never substitutes fabricated values.
 */
@Composable
fun MalMangaStatsView(
    stats: UserStats.MangaStats,
    modifier: Modifier = Modifier,
) {
    val statuses = listOf(
        Triple(stringResource(UiRes.string.reading), stats.current, Color(0xFF2DB55D)),
        Triple(stringResource(UiRes.string.completed), stats.completed, Color(0xFF2E5AA7)),
        Triple(stringResource(UiRes.string.on_hold), stats.onHold, Color(0xFFFFC94A)),
        Triple(stringResource(UiRes.string.dropped), stats.dropped, Color(0xFFC64238)),
        Triple(stringResource(UiRes.string.ptr), stats.planned, Color(0xFFC8C8C8)),
    )
    val activeStatuses = statuses.filter { it.second > 0 }
    val totalStatuses = activeStatuses.sumOf { it.second }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(UiRes.string.manga_stats),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${stringResource(UiRes.string.days)}: ${stats.days.toFixed(1)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${stringResource(UiRes.string.mean_score)}: ${stats.meanScore.toFixed(2)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            if (totalStatuses > 0) {
                activeStatuses.forEach { (_, count, color) ->
                    Box(
                        modifier = Modifier
                            .weight(count.toFloat())
                            .height(12.dp)
                            .background(color)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { (name, count, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            Modifier.size(10.dp).background(
                                color,
                                RoundedCornerShape(50)
                            )
                        )
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MangaMetric(stringResource(UiRes.string.total_entries_label), stats.totalEntries)
                MangaMetric(stringResource(UiRes.string.total_rereads), stats.repeat)
                MangaMetric(stringResource(UiRes.string.chapters), stats.chaptersRead)
                MangaMetric(stringResource(UiRes.string.volumes), stats.volumesRead)
            }
        }
    }
}

@Composable
private fun MangaMetric(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Float.toFixed(decimals: Int): String {
    val scale = if (decimals == 1) 10 else 100
    val rounded = (this * scale).roundToInt().coerceAtLeast(0)
    return "${rounded / scale}.${(rounded % scale).toString().padStart(decimals, '0')}"
}
