package com.axiel7.moelist.screens.ranking

import androidx.compose.runtime.Composable

/** Intercepts platform Back only while a genre detail is open. */
@Composable
expect fun GenreSystemBackHandler(enabled: Boolean, onBack: () -> Unit)
