package com.axiel7.moelist.ui.composables.media

import androidx.compose.ui.graphics.Color

/**
 * Six-stage progress palette shared by anime episode, manga chapter and volume progress.
 * Unknown totals use count milestones so ongoing media without a reported total
 * can still receive a progress color.
 */
fun kirinProgressStageColor(progress: Int, total: Int?, enabled: Boolean): Color? {
    if (!enabled || progress <= 0) return null

    val stage = if (total != null && total > 0) {
        val fraction = (progress.toFloat() / total).coerceIn(0f, 1f)
        when {
            fraction < 0.2f -> 0
            fraction < 0.4f -> 1
            fraction < 0.6f -> 2
            fraction < 0.8f -> 3
            fraction < 1f -> 4
            else -> 5
        }
    } else {
        when {
            progress < 5 -> 0
            progress < 10 -> 1
            progress < 25 -> 2
            progress < 50 -> 3
            progress < 100 -> 4
            else -> 5
        }
    }

    return when (stage) {
        0 -> Color(0xFFFF526B)
        1 -> Color(0xFFFF9D35)
        2 -> Color(0xFFFFD54F)
        3 -> Color(0xFF39DB8A)
        4 -> Color(0xFF00CFFF)
        else -> Color(0xFFB65CFF)
    }
}
