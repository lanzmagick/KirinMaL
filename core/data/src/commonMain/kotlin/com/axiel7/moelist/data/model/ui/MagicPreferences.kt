package com.axiel7.moelist.data.model.ui

data class MagicPreferences(
    val enabled: Boolean = true,
    val showOnLists: Boolean = true,
    val effectsEnabled: Boolean = true,
) {
    val visibleOnLists get() = enabled && showOnLists
}
