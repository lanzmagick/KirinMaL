package com.axiel7.moelist.screens.magic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.LoadingDialog
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.cancel
import com.axiel7.moelist.ui.generated.resources.ic_magic_sparkles_24
import com.axiel7.moelist.ui.generated.resources.ic_round_casino_24
import com.axiel7.moelist.ui.generated.resources.magic
import com.axiel7.moelist.ui.generated.resources.magic_anime
import com.axiel7.moelist.ui.generated.resources.magic_empty
import com.axiel7.moelist.ui.generated.resources.magic_manga
import com.axiel7.moelist.ui.generated.resources.magic_unavailable
import com.axiel7.moelist.ui.generated.resources.random
import com.axiel7.moelist.ui.generated.resources.random_anime
import com.axiel7.moelist.ui.generated.resources.random_manga
import com.axiel7.moelist.ui.generated.resources.retry
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private enum class Presentation { DIRECT, MAGIC }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RandomMagicControls(
    context: MagicPickerContext,
    onOpen: (BaseMediaNode) -> Unit,
    homeStyle: Boolean = false,
    onDefaultRandom: (() -> Unit)? = null,
) {
    var presentation by remember(context) { mutableStateOf<Presentation?>(null) }
    @Composable
    fun PickerButton(label: String, icon: DrawableResource, onClick: () -> Unit) {
        if (homeStyle) {
            OutlinedButton(onClick = onClick, shapes = ButtonDefaults.shapes()) {
                Icon(painterResource(icon), contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(20.dp))
                Text(label)
            }
        } else {
            AssistChip(onClick = onClick, label = { Text(label) }, leadingIcon = {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
            })
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PickerButton(
            stringResource(if (!homeStyle) UiRes.string.random
                else if (context.mediaType == MediaType.ANIME) UiRes.string.random_anime
                else UiRes.string.random_manga),
            UiRes.drawable.ic_round_casino_24,
            onClick = {
                if (onDefaultRandom != null) onDefaultRandom()
                else presentation = Presentation.DIRECT
            },
        )
        PickerButton(
            stringResource(if (!homeStyle) UiRes.string.magic
                else if (context.mediaType == MediaType.ANIME) UiRes.string.magic_anime
                else UiRes.string.magic_manga),
            UiRes.drawable.ic_magic_sparkles_24,
            onClick = { presentation = Presentation.MAGIC },
        )
    }
    val mode = presentation ?: return
    val viewModel: MagicPickerViewModel = koinViewModel(
        key = "magic-${context.mediaType}-${context.listStatus}-${context.titleInitial}",
    ) { parametersOf(context) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    fun close() {
        presentation = null
        viewModel.close()
    }
    DisposableEffect(viewModel) { onDispose { viewModel.close() } }
    LaunchedEffect(mode) { viewModel.pick(refresh = true, animate = mode == Presentation.MAGIC) }
    LaunchedEffect(mode, state.selection) {
        if (mode == Presentation.DIRECT) state.selection?.let { selection ->
            close()
            onOpen(selection)
        }
    }
    if (mode == Presentation.MAGIC) {
        MagicPickerDialog(
            type = context.mediaType,
            state = state,
            onAgain = { viewModel.pick(refresh = state.error != null) },
            onOpen = { selection -> close(); onOpen(selection) },
            onClose = ::close,
        )
    } else if (state.isLoading) {
        LoadingDialog(onDismiss = ::close)
    } else if (state.error != null) {
        AlertDialog(
            onDismissRequest = ::close,
            title = { Text(stringResource(if (context.mediaType == MediaType.ANIME)
                UiRes.string.random_anime else UiRes.string.random_manga)) },
            text = { Text(stringResource(if (state.error == MagicPickerError.EMPTY)
                UiRes.string.magic_empty else UiRes.string.magic_unavailable)) },
            confirmButton = { TextButton(onClick = { viewModel.pick(refresh = true, animate = false) }) {
                Text(stringResource(UiRes.string.retry))
            } },
            dismissButton = { TextButton(onClick = ::close) { Text(stringResource(UiRes.string.cancel)) } },
        )
    }
}
