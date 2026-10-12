package com.axiel7.moelist.screens.userlist.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.screens.userlist.TitleInitialFilter
import com.axiel7.moelist.screens.userlist.UserMediaListEvent
import com.axiel7.moelist.screens.userlist.UserMediaListUiState
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.all_titles
import com.axiel7.moelist.ui.generated.resources.cancel
import com.axiel7.moelist.ui.generated.resources.filter_title_initial
import com.axiel7.moelist.ui.generated.resources.ic_round_filter_list_24
import com.axiel7.moelist.ui.generated.resources.symbol_initials
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleInitialFilterChip(uiState: UserMediaListUiState, event: UserMediaListEvent?) {
    var open by remember { mutableStateOf(false) }
    val selected = uiState.titleInitial
    FilterChip(
        selected = selected.isActive,
        onClick = { open = true },
        label = { Text(if (selected.isActive) selected.label else "A–Z · 1–0 · #") },
        leadingIcon = {
            Icon(
                painter = painterResource(UiRes.drawable.ic_round_filter_list_24),
                contentDescription = stringResource(UiRes.string.filter_title_initial),
                modifier = Modifier.size(20.dp),
            )
        },
    )
    if (open) {
        fun choose(value: TitleInitialFilter) {
            event?.onChangeTitleInitial(value)
            open = false
        }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(UiRes.string.filter_title_initial)) },
            confirmButton = {
                TextButton(onClick = { open = false }) {
                    Text(stringResource(UiRes.string.cancel))
                }
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(44.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FilterChip(
                            selected = selected == TitleInitialFilter.ALL,
                            onClick = { choose(TitleInitialFilter.ALL) },
                            label = { Text(stringResource(UiRes.string.all_titles)) },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("A–Z", style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(TitleInitialFilter.letters, key = { it.name }) { value ->
                        FilterChip(
                            selected = selected == value,
                            onClick = { choose(value) },
                            label = {
                                Text(value.label, modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center)
                            },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("1–0", style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(TitleInitialFilter.numbers, key = { it.name }) { value ->
                        FilterChip(
                            selected = selected == value,
                            onClick = { choose(value) },
                            label = {
                                Text(value.label, modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center)
                            },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FilterChip(
                            selected = selected == TitleInitialFilter.SYMBOLS,
                            onClick = { choose(TitleInitialFilter.SYMBOLS) },
                            label = { Text(stringResource(UiRes.string.symbol_initials)) },
                        )
                    }
                }
            },
        )
    }
}
