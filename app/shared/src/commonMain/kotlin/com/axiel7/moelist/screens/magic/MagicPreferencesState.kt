package com.axiel7.moelist.screens.magic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.ui.MagicPreferences
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import org.koin.compose.koinInject

@Composable
internal fun magicPreferencesState(): State<MagicPreferences> {
    val preferences: DefaultPreferencesRepository = koinInject()
    // Wait for saved choices before displaying Magic or starting decorative animations.
    return preferences.magicPreferences.collectAsStateWithLifecycle(
        initialValue = MagicPreferences(enabled = false, showOnLists = false, effectsEnabled = false),
    )
}
