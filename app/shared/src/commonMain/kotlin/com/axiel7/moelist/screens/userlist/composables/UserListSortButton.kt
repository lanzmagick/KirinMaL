package com.axiel7.moelist.screens.userlist.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.ic_round_sort_24
import com.axiel7.moelist.ui.generated.resources.round_check_24
import com.axiel7.moelist.ui.generated.resources.sort_by
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun UserListSortButton(mediaType: MediaType) {
    val preferences: DefaultPreferencesRepository = koinInject()
    val scope = rememberCoroutineScope()
    // Recreate the menu and its collected value when switching Anime/Manga.
    key(mediaType) {
        val sortFlow = remember(preferences, mediaType) {
            when (mediaType) {
                MediaType.ANIME -> preferences.animeListSort
                MediaType.MANGA -> preferences.mangaListSort
            }
        }
        val selectedSort by sortFlow.collectAsStateWithLifecycle(initialValue = null)
        var menuOpened by remember { mutableStateOf(false) }
        val values = if (mediaType == MediaType.ANIME) MediaSort.animeListSortItems
            else MediaSort.mangaListSortItems
        val sortLabel = stringResource(UiRes.string.sort_by)
        val currentLabel = selectedSort?.localized()
        val windowHeight = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.height.toDp()
        }

        Box {
            FilledTonalIconButton(
                onClick = { menuOpened = true },
                enabled = selectedSort != null,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(50),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    painter = painterResource(UiRes.drawable.ic_round_sort_24),
                    contentDescription = currentLabel?.let { "$sortLabel: $it" } ?: sortLabel,
                )
            }
            DropdownMenu(
                expanded = menuOpened,
                onDismissRequest = { menuOpened = false },
                modifier = Modifier.requiredSizeIn(maxHeight = windowHeight / 2),
            ) {
                values.forEach { value ->
                    DropdownMenuItem(
                        text = { Text(value.localized()) },
                        onClick = {
                            menuOpened = false
                            if (value != selectedSort) scope.launch {
                                when (mediaType) {
                                    MediaType.ANIME -> preferences.setAnimeListSort(value)
                                    MediaType.MANGA -> preferences.setMangaListSort(value)
                                }
                            }
                        },
                        leadingIcon = {
                            if (value == selectedSort) Icon(
                                painter = painterResource(UiRes.drawable.round_check_24),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}
