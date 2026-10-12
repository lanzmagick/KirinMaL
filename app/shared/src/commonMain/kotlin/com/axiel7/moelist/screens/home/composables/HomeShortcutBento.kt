package com.axiel7.moelist.screens.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.utils.SeasonCalendar
import com.axiel7.moelist.screens.magic.MagicPickerContext
import com.axiel7.moelist.screens.magic.RandomMagicControls
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.anime_menu
import com.axiel7.moelist.ui.generated.resources.calendar
import com.axiel7.moelist.ui.generated.resources.ic_round_event_24
import com.axiel7.moelist.ui.generated.resources.ic_round_menu_book_24
import com.axiel7.moelist.ui.generated.resources.ic_round_movie_24
import com.axiel7.moelist.ui.generated.resources.manga_menu
import com.axiel7.moelist.ui.generated.resources.seasonal_chart
import org.jetbrains.compose.resources.stringResource

private enum class HomeShortcut {
    ANIME_MENU, MANGA_MENU, SEASONAL_CHART, CALENDAR, MAGIC_ANIME, MAGIC_MANGA
}

@Composable
fun HomeShortcutBento(
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    modifier: Modifier = Modifier,
) {
    val columns = if (isCompactScreen) 2 else 3
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeShortcut.entries.chunked(columns).forEach { shortcuts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                shortcuts.forEach { shortcut ->
                    key(shortcut) {
                        val cardModifier = Modifier.weight(1f)
                        when (shortcut) {
                            HomeShortcut.ANIME_MENU -> HomeCard(
                                text = stringResource(UiRes.string.anime_menu),
                                icon = UiRes.drawable.ic_round_movie_24,
                                modifier = cardModifier,
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaRanking(MediaType.ANIME)
                                },
                            )
                            HomeShortcut.MANGA_MENU -> HomeCard(
                                text = stringResource(UiRes.string.manga_menu),
                                icon = UiRes.drawable.ic_round_menu_book_24,
                                modifier = cardModifier,
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaRanking(MediaType.MANGA)
                                },
                            )
                            HomeShortcut.SEASONAL_CHART -> HomeCard(
                                text = stringResource(UiRes.string.seasonal_chart),
                                icon = SeasonCalendar.currentSeason.icon,
                                modifier = cardModifier,
                                onClick = dropUnlessResumed { navActionManager.toSeasonChart() },
                            )
                            HomeShortcut.CALENDAR -> HomeCard(
                                text = stringResource(UiRes.string.calendar),
                                icon = UiRes.drawable.ic_round_event_24,
                                modifier = cardModifier,
                                onClick = dropUnlessResumed { navActionManager.toCalendar() },
                            )
                            HomeShortcut.MAGIC_ANIME, HomeShortcut.MAGIC_MANGA -> {
                                val type = if (shortcut == HomeShortcut.MAGIC_ANIME)
                                    MediaType.ANIME else MediaType.MANGA
                                RandomMagicControls(
                                    context = MagicPickerContext(type),
                                    homeStyle = true,
                                    modifier = cardModifier,
                                    onOpen = { navActionManager.toMediaDetails(type, it.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
