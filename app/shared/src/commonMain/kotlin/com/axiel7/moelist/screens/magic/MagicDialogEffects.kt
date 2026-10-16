package com.axiel7.moelist.screens.magic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.axiel7.moelist.data.model.media.MediaType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal data class MagicDialogPalette(
    val first: Color,
    val second: Color,
    val base: Color,
    val onAccent: Color,
    val dark: Boolean,
)

internal data class MagicDialogMotion(val orbit: State<Float>, val reveal: State<Float>)

@Composable
internal fun magicDialogPalette(type: MediaType): MagicDialogPalette {
    val scheme = MaterialTheme.colorScheme
    return remember(type, scheme) {
        val dark = scheme.surface.luminance() < 0.5f
        val anime = type == MediaType.ANIME
        MagicDialogPalette(
            first = when {
                anime && dark -> Color(0xFFBCA4FF)
                anime -> Color(0xFF7950BE)
                dark -> Color(0xFFFF90C9)
                else -> Color(0xFFB2387A)
            },
            second = when {
                anime && dark -> Color(0xFF68D5FF)
                anime -> Color(0xFF087E9C)
                dark -> Color(0xFFFFD58B)
                else -> Color(0xFF99600E)
            },
            base = scheme.surfaceContainerHigh,
            onAccent = if (dark) Color(0xFF21152F) else Color.White,
            dark = dark,
        )
    }
}

@Composable
internal fun rememberMagicDialogMotion(
    type: MediaType,
    selectionId: Int?,
    effectsEnabled: Boolean = true,
): MagicDialogMotion {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val active = effectsEnabled && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val orbit = if (active) {
        val transition = rememberInfiniteTransition(label = "magic-dialog-orbit")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(if (type == MediaType.ANIME) 7600 else 8800, easing = LinearEasing)
            ),
            label = "magic-dialog-light",
        )
    } else remember { mutableStateOf(0f) }
    val reveal = remember { Animatable(1f) }
    LaunchedEffect(selectionId, active) {
        if (selectionId != null && active) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(1000))
        } else reveal.snapTo(1f)
    }
    return remember(orbit, reveal) { MagicDialogMotion(orbit, reveal.asState()) }
}

/** Draw-only effects: no per-frame composition, layout, image loading or picker work. */
internal fun Modifier.magicDialogGlass(
    palette: MagicDialogPalette,
    orbit: State<Float>,
    corner: Dp,
    overPoster: Boolean = false,
    enabled: Boolean = true,
): Modifier = if (!enabled) this else drawWithCache {
    val radius = min(corner.toPx(), size.minDimension / 2f)
    val inset = min(1.5.dp.toPx(), size.minDimension / 4f)
    val rim = Path().apply {
        addRoundRect(RoundRect(
            Rect(inset, inset, size.width - inset, size.height - inset),
            CornerRadius((radius - inset).coerceAtLeast(0f)),
        ))
    }
    val measure = PathMeasure().apply { setPath(rim, true) }
    val segment = Path()
    val rimStroke = Stroke(1.dp.toPx())
    val lightStroke = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
    val glowStroke = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
    val background = Brush.linearGradient(
        listOf(lerp(palette.base, palette.first, if (palette.dark) 0.13f else 0.045f),
            palette.base, lerp(palette.base, palette.second, if (palette.dark) 0.09f else 0.03f)),
        start = Offset.Zero, end = Offset(size.width, size.height),
    )
    val rimBrush = Brush.linearGradient(
        listOf(palette.first.copy(alpha = 0.44f), palette.second.copy(alpha = 0.32f)),
        start = Offset.Zero, end = Offset(size.width, size.height),
    )
    val glass = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.13f)
        cubicTo(size.width * 0.65f, size.height * 0.07f,
            size.width * 0.42f, size.height * 0.24f, 0f, size.height * 0.19f)
        close()
    }
    val glassBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (palette.dark) 0.065f else 0.3f), Color.Transparent),
        endY = size.height * 0.28f,
    )
    val starSize = 2.2.dp.toPx()
    val starStroke = 0.9.dp.toPx()

    onDrawWithContent {
        val progress = orbit.value.mod(1f)
        if (!overPoster) {
            drawRoundRect(background, cornerRadius = CornerRadius(radius))
            drawPath(glass, glassBrush)
        }
        // Panel shine stays behind text; only the poster gets a subtle glass overlay.
        if (overPoster) drawContent()
        val shineX = size.width * (progress * 3f - 0.7f)
        val shineWidth = size.width * 0.3f
        if (shineX > -shineWidth && shineX < size.width + shineWidth) {
            drawRect(Brush.linearGradient(
                0f to Color.Transparent,
                0.45f to Color.White.copy(alpha = 0.01f),
                0.5f to Color.White.copy(alpha = if (overPoster) 0.15f else 0.055f),
                0.55f to Color.White.copy(alpha = 0.01f),
                1f to Color.Transparent,
                start = Offset(shineX - shineWidth, 0f),
                end = Offset(shineX + shineWidth, size.height),
            ))
        }
        if (!overPoster) drawContent()
        drawPath(rim, rimBrush, style = rimStroke)
        val length = measure.length
        if (length > 0f) {
            repeat(if (overPoster) 1 else 2) { trail ->
                val head = (progress + trail * 0.53f).mod(1f)
                val tint = if (trail == 0) palette.first else palette.second
                repeat(7) { index ->
                    val start = (head - (index + 1) * 0.018f).mod(1f)
                    val end = (head - index * 0.018f).mod(1f)
                    segment.reset()
                    if (start <= end) {
                        measure.getSegment(start * length, end * length, segment, true)
                    } else {
                        measure.getSegment(start * length, length, segment, true)
                        measure.getSegment(0f, end * length, segment, true)
                    }
                    val fade = 1f - index / 7f
                    drawPath(segment, tint.copy(alpha = fade * 0.1f), style = glowStroke)
                    drawPath(segment, tint.copy(alpha = fade * fade * 0.9f), style = lightStroke)
                }
            }
            repeat(if (overPoster) 3 else 7) { index ->
                val point = measure.getPosition((progress + index * 0.137f).mod(1f) * length)
                val twinkle = (sin((progress * 2f + index * 0.27f) * 2f * PI).toFloat() + 1f) / 2f
                val tint = lerp(palette.first, palette.second, (index % 3) / 2f)
                drawCircle(tint.copy(alpha = 0.12f), starSize * 2f, point)
                drawMagicStar(point, starSize * (0.6f + twinkle * 0.4f),
                    tint.copy(alpha = 0.4f + twinkle * 0.5f), starStroke)
            }
        }
    }
}

/** Ambient halo and a short outward sparkle burst when a new selection is revealed. */
internal fun Modifier.magicPosterHalo(
    palette: MagicDialogPalette,
    motion: MagicDialogMotion,
    enabled: Boolean = true,
): Modifier = if (!enabled) this else drawWithCache {
    val center = Offset(size.width / 2f, size.height / 2f)
    val haloRadius = size.maxDimension * 0.72f
    val halo = Brush.radialGradient(
        listOf(palette.first.copy(alpha = if (palette.dark) 0.22f else 0.13f),
            palette.second.copy(alpha = 0.07f), Color.Transparent),
        center = center, radius = haloRadius,
    )
    val starSize = 3.dp.toPx()
    val starStroke = 1.dp.toPx()
    val travel = 24.dp.toPx()
    onDrawBehind {
        val reveal = motion.reveal.value.coerceIn(0f, 1f)
        val twinkle = (sin(motion.orbit.value * 4f * PI).toFloat() + 1f) / 2f
        drawCircle(halo, haloRadius * (0.94f + twinkle * 0.06f), center)
        if (reveal < 1f) {
            repeat(8) { index ->
                val angle = index * PI / 4f + PI / 8f
                val distance = 5.dp.toPx() + reveal * travel
                val point = center + Offset(
                    cos(angle).toFloat() * (size.width / 2f + distance),
                    sin(angle).toFloat() * (size.height / 2f + distance),
                )
                drawMagicStar(point, starSize * (0.5f + reveal * 0.5f),
                    lerp(palette.first, palette.second, (index % 3) / 2f)
                        .copy(alpha = (1f - reveal) * 0.85f), starStroke)
            }
            drawRoundRect(palette.first.copy(alpha = (1f - reveal) * 0.12f),
                topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                cornerRadius = CornerRadius(20.dp.toPx()))
        }
    }
}

private fun DrawScope.drawMagicStar(center: Offset, radius: Float, color: Color, stroke: Float) {
    drawLine(color, center - Offset(radius, 0f), center + Offset(radius, 0f),
        strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(color, center - Offset(0f, radius), center + Offset(0f, radius),
        strokeWidth = stroke, cap = StrokeCap.Round)
}
