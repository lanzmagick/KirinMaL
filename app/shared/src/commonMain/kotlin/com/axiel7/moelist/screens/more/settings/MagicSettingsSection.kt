package com.axiel7.moelist.screens.more.settings

import androidx.compose.runtime.Composable
import com.axiel7.moelist.data.model.ui.MagicPreferences
import com.axiel7.moelist.ui.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.ic_magic_sparkles_24
import com.axiel7.moelist.ui.generated.resources.magic
import com.axiel7.moelist.ui.generated.resources.magic_effects
import com.axiel7.moelist.ui.generated.resources.magic_effects_summary
import com.axiel7.moelist.ui.generated.resources.magic_enabled
import com.axiel7.moelist.ui.generated.resources.magic_enabled_summary
import com.axiel7.moelist.ui.generated.resources.magic_on_lists
import com.axiel7.moelist.ui.generated.resources.magic_on_lists_summary
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MagicSettingsSection(preferences: MagicPreferences, event: SettingsEvent?) {
    SettingsTitle(text = stringResource(UiRes.string.magic))
    SwitchPreferenceView(
        title = stringResource(UiRes.string.magic_enabled),
        subtitle = stringResource(UiRes.string.magic_enabled_summary),
        value = preferences.enabled,
        icon = UiRes.drawable.ic_magic_sparkles_24,
        onValueChange = { event?.setMagicEnabled(it) },
    )
    SwitchPreferenceView(
        title = stringResource(UiRes.string.magic_on_lists),
        subtitle = stringResource(UiRes.string.magic_on_lists_summary),
        value = preferences.showOnLists,
        enabled = preferences.enabled,
        onValueChange = { event?.setMagicOnLists(it) },
    )
    SwitchPreferenceView(
        title = stringResource(UiRes.string.magic_effects),
        subtitle = stringResource(UiRes.string.magic_effects_summary),
        value = preferences.effectsEnabled,
        enabled = preferences.enabled,
        onValueChange = { event?.setMagicEffectsEnabled(it) },
    )
}
