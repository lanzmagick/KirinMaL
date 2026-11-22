package com.axiel7.moelist.ui.composables.media

import androidx.compose.runtime.staticCompositionLocalOf

/** RGB progress tint is scoped to the user's media lists. */
val LocalRgbProgressEnabled = staticCompositionLocalOf { false }
