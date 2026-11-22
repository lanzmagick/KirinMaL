package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaFormat
import kotlin.math.exp

@Composable
fun MediaProgressIndicatorBar(
    progress: Int,
    total: Int?,
    released: Int? = null,
    format: MediaFormat?,
    modifier: Modifier = Modifier,
    isVolumeTracking: Boolean = false,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val fraction = remember(progress, total, released, format, isVolumeTracking) {
        if (progress <= 0) {
            0f
        } else if (total != null && total > 0) {
            (progress.toFloat() / total).coerceIn(0f, 1f)
        } else {
            val safeReleased = maxOf(released ?: progress, progress, 1)
            val rate = when (format) {
                MediaFormat.OVA, MediaFormat.SPECIAL -> 0.35f
                MediaFormat.TV_SPECIAL -> 0.15f
                MediaFormat.TV -> 0.07f
                MediaFormat.MANGA, MediaFormat.NOVEL, MediaFormat.LIGHT_NOVEL -> {
                    if (isVolumeTracking) 0.06f else 0.015f
                }
                else -> 0.07f
            }
            val maxCap =
                if (format == MediaFormat.OVA || format == MediaFormat.SPECIAL) 0.80f else 0.88f
            val horizon = maxCap * (1f - exp(-rate * safeReleased.toFloat()))
            val fillRatio = (progress.toFloat() / safeReleased).coerceIn(0f, 1f)
            fillRatio * horizon
        }
    }

    val completedColor = kirinProgressStageColor(
        progress = progress,
        total = total,
        enabled = LocalRgbProgressEnabled.current
    ) ?: indicatorColor

    Canvas(modifier = modifier.height(4.dp)) {
        val width = size.width
        val stroke = size.height
        if (width <= 0f || stroke <= 0f) return@Canvas

        val y = stroke / 2f
        val radius = stroke / 2f
        val start = Offset(radius, y)
        val end = Offset(width - radius, y)

        drawLine(
            color = trackColor,
            start = start,
            end = end,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        if (fraction <= 0f) return@Canvas

        val completedWidth = (width * fraction).coerceIn(stroke, width)
        val completedEnd = Offset(
            x = (completedWidth - radius).coerceAtLeast(radius),
            y = y
        )

        if ((total == null || total <= 0) && progress > 0) {
            // Keep the source effect's soft tail when the media has no known total.
            val gradient = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to completedColor,
                    0.7f to completedColor,
                    1.0f to Color.Transparent
                ),
                startX = 0f,
                endX = completedWidth
            )
            drawLine(
                brush = gradient,
                start = start,
                end = completedEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = completedColor,
                start = start,
                end = completedEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
