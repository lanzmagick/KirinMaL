package com.axiel7.moelist.screens.magic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.cancel
import com.axiel7.moelist.ui.generated.resources.ic_magic_sparkles_24
import com.axiel7.moelist.ui.generated.resources.magic_again
import com.axiel7.moelist.ui.generated.resources.magic_anime
import com.axiel7.moelist.ui.generated.resources.magic_empty
import com.axiel7.moelist.ui.generated.resources.magic_manga
import com.axiel7.moelist.ui.generated.resources.magic_open
import com.axiel7.moelist.ui.generated.resources.magic_picking
import com.axiel7.moelist.ui.generated.resources.magic_surprise
import com.axiel7.moelist.ui.generated.resources.magic_unavailable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MagicPickerDialog(
    type: MediaType,
    state: MagicPickerUiState,
    onAgain: () -> Unit,
    onOpen: (BaseMediaNode) -> Unit,
    onClose: () -> Unit,
) {
    val palette = magicDialogPalette(type)
    val motion = rememberMagicDialogMotion(type, state.selection?.id)
    Dialog(onDismissRequest = onClose) {
        MagicPickerContent(type, state, palette, motion, onAgain, onOpen, onClose)
    }
}

@Composable
internal fun MagicPickerContent(
    type: MediaType,
    state: MagicPickerUiState,
    palette: MagicDialogPalette,
    motion: MagicDialogMotion,
    onAgain: () -> Unit,
    onOpen: (BaseMediaNode) -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.widthIn(max = 360.dp).heightIn(max = 620.dp),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(Modifier.magicDialogGlass(palette, motion.orbit, 32.dp)) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(painterResource(UiRes.drawable.ic_magic_sparkles_24),
                        contentDescription = null, tint = palette.first, modifier = Modifier.size(20.dp))
                    Text(
                        stringResource(if (type == MediaType.ANIME)
                            UiRes.string.magic_anime else UiRes.string.magic_manga),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(stringResource(UiRes.string.magic_surprise),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedContent(
                    targetState = state.selection,
                    label = "magic-reveal",
                    transitionSpec = {
                        (fadeIn(tween(420)) + scaleIn(tween(420), initialScale = 0.92f))
                            .togetherWith(fadeOut(tween(180)))
                    },
                ) { selection ->
                    if (selection == null) {
                        MysteryBox(state.isLoading, palette, motion)
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(Modifier.size(140.dp, 196.dp).magicPosterHalo(palette, motion)) {
                                Box(Modifier.matchParentSize().clip(RoundedCornerShape(18.dp))
                                    .magicDialogGlass(palette, motion.orbit, 18.dp, overPoster = true)) {
                                    MediaPoster(
                                        url = selection.mainPicture?.large ?: selection.mainPicture?.medium,
                                        modifier = Modifier.matchParentSize(),
                                        showShadow = false,
                                    )
                                }
                            }
                            Text(
                                selection.title(state.preferredTitle),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                            selection.mediaFormat?.let {
                                Text(it.localized(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (state.isLoading || state.error != null) Text(
                    stringResource(when {
                        state.isLoading -> UiRes.string.magic_picking
                        state.error == MagicPickerError.EMPTY -> UiRes.string.magic_empty
                        else -> UiRes.string.magic_unavailable
                    }),
                    textAlign = TextAlign.Center,
                )
                state.selection?.let { selection ->
                    Button(onClick = { onOpen(selection) }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.first, contentColor = palette.onAccent)) {
                        Text(stringResource(UiRes.string.magic_open))
                    }
                }
                OutlinedButton(
                    onClick = onAgain,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, palette.first.copy(alpha = if (state.isLoading) 0.18f else 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.first),
                ) { Text(stringResource(UiRes.string.magic_again)) }
                TextButton(onClick = onClose,
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.first)) {
                    Text(stringResource(UiRes.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun MysteryBox(isLoading: Boolean, palette: MagicDialogPalette, motion: MagicDialogMotion) {
    Box(
        modifier = Modifier.size(196.dp).graphicsLayer {
            val pulse = 0.97f + 0.03f * sin(motion.orbit.value * 8f * PI).toFloat()
            scaleX = if (isLoading) pulse else 1f
            scaleY = if (isLoading) pulse else 1f
        }.clip(RoundedCornerShape(30.dp)).background(
            Brush.linearGradient(listOf(lerp(palette.first, Color.White, 0.6f),
                lerp(palette.second, Color.White, 0.64f)))
        ).magicDialogGlass(palette, motion.orbit, 30.dp, overPoster = true),
        contentAlignment = Alignment.Center,
    ) {
        Text("?", fontSize = 80.sp, fontWeight = FontWeight.Black, color = Color(0xFF362147))
        Icon(painterResource(UiRes.drawable.ic_magic_sparkles_24), contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp).size(28.dp), tint = Color.White)
        Icon(painterResource(UiRes.drawable.ic_magic_sparkles_24), contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp).size(24.dp), tint = Color.White)
    }
}
