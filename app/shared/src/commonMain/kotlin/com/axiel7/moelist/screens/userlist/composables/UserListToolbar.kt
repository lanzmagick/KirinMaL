package com.axiel7.moelist.screens.userlist.composables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.screens.userlist.UserMediaListEvent
import com.axiel7.moelist.screens.userlist.UserMediaListUiState

@Composable
fun UserListToolbar(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
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
            RandomChip(onClick = { event?.getRandomIdOfList() })
        }
    }
}
