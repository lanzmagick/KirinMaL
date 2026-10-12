package com.axiel7.moelist.screens.userlist.composables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.screens.magic.MagicPickerContext
import com.axiel7.moelist.screens.magic.RandomMagicControls
import com.axiel7.moelist.screens.userlist.UserMediaListEvent
import com.axiel7.moelist.screens.userlist.UserMediaListUiState
import com.axiel7.moelist.ui.base.model.ListStatus
import com.axiel7.moelist.ui.base.navigation.NavActionManager

@Composable
fun UserListToolbar(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortChip(uiState, event)
        TitleInitialFilterChip(uiState, event)
        if (uiState.showRandomButton) {
            RandomMagicControls(
                context = MagicPickerContext(
                    mediaType = uiState.mediaType,
                    listStatus = uiState.listStatus ?: if (uiState.mediaType == MediaType.ANIME)
                        ListStatus.WATCHING else ListStatus.READING,
                    titleInitial = uiState.titleInitial,
                ),
                onDefaultRandom = { event?.getRandomIdOfList() },
                onOpen = { navActionManager.toMediaDetails(uiState.mediaType, it.id) },
            )
        }
    }
}
