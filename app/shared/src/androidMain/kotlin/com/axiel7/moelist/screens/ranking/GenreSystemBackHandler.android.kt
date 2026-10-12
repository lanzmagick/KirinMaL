package com.axiel7.moelist.screens.ranking

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun GenreSystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
