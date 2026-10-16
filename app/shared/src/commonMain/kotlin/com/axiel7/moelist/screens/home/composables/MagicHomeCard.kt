package com.axiel7.moelist.screens.home.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.axiel7.moelist.data.model.media.MediaType
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** The Home-only Magic appearance; selection and dialog behavior stay in the caller. */
@Composable
fun MagicHomeCard(
    text: String,
    icon: Painter,
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val phase: State<Float> = if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
        val transition = rememberInfiniteTransition(label = "magic-home-rim")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(if (mediaType == MediaType.ANIME) 7200 else 8600, easing = LinearEasing)
            ),
            label = "magic-home-light",
        )
    } else {
        // No continuous frame work while this navigation entry is in the background.
        remember { mutableStateOf(0f) }
    }
    MagicHomeCardSurface(text, icon, mediaType, phase, modifier, onClick)
}

@Composable
internal fun MagicHomeCardSurface(
    text: String,
    icon: Painter,
    mediaType: MediaType,
    phase: State<Float>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val anime = mediaType == MediaType.ANIME
    val first = when {
        anime && dark -> Color(0xFFBCA4FF)
        anime -> Color(0xFF7950BE)
        dark -> Color(0xFFFF90C9)
        else -> Color(0xFFB2387A)
    }
    val second = when {
        anime && dark -> Color(0xFF68D5FF)
        anime -> Color(0xFF087E9C)
        dark -> Color(0xFFFFD58B)
        else -> Color(0xFF99600E)
    }
    val base = scheme.surfaceContainerHigh

    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        contentColor = scheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val corner = min(18.dp.toPx(), size.minDimension / 2f)
                    val inset = min(1.25.dp.toPx(), size.minDimension / 4f)
                    val rim = Path().apply {
                        addRoundRect(RoundRect(
                            Rect(inset, inset, size.width - inset, size.height - inset),
                            CornerRadius((corner - inset).coerceAtLeast(0f)),
                        ))
                    }
                    val measure = PathMeasure().apply { setPath(rim, true) }
                    val segment = Path()
                    val rimStroke = Stroke(1.dp.toPx())
                    val lightStroke = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
                    val glowStroke = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                    val background = Brush.linearGradient(
                        listOf(lerp(base, first, if (dark) 0.18f else 0.06f), base,
                            lerp(base, second, if (dark) 0.13f else 0.04f)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    val rimBrush = Brush.linearGradient(
                        listOf(first.copy(alpha = 0.38f), second.copy(alpha = 0.34f)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    val glass = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.9f, 0f)
                        cubicTo(size.width * 0.7f, size.height * 0.28f,
                            size.width * 0.38f, size.height * 0.48f, 0f, size.height * 0.58f)
                        close()
                    }
                    val glassBrush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (dark) 0.09f else 0.32f),
                            Color.White.copy(alpha = 0f)),
                    )
                    val sparkleSize = 1.7.dp.toPx()
                    val particleRadius = 0.8.dp.toPx()
                    val sparkleStroke = 0.8.dp.toPx()

                    onDrawBehind {
                        // Read animated state in drawing only, leaving text/layout out of each frame.
                        val progress = (phase.value + if (anime) 0f else 0.22f) % 1f
                        drawRoundRect(background, cornerRadius = CornerRadius(corner))
                        drawPath(glass, glassBrush)

                        // A soft diagonal reflection crosses the glass, followed by a quiet interval.
                        val shineX = size.width * (progress * 2.8f - 0.65f)
                        val shineWidth = size.width * 0.24f
                        if (shineX > -shineWidth && shineX < size.width + shineWidth) {
                            drawRect(Brush.linearGradient(
                                0f to Color.Transparent,
                                0.42f to Color.White.copy(alpha = 0.015f),
                                0.5f to Color.White.copy(alpha = if (dark) 0.12f else 0.25f),
                                0.58f to Color.White.copy(alpha = 0.015f),
                                1f to Color.Transparent,
                                start = Offset(shineX - shineWidth, 0f),
                                end = Offset(shineX + shineWidth, size.height),
                            ))
                        }

                        drawPath(rim, rimBrush, style = rimStroke)
                        val length = measure.length
                        if (length > 0f) {
                            repeat(2) { trail ->
                                val head = (progress + trail * 0.54f) % 1f
                                val tint = if (trail == 0) first else second
                                repeat(8) { index ->
                                    val start = (head - (index + 1) * 0.019f + 1f) % 1f
                                    val end = (head - index * 0.019f + 1f) % 1f
                                    segment.reset()
                                    if (start <= end) {
                                        measure.getSegment(start * length, end * length, segment, true)
                                    } else {
                                        measure.getSegment(start * length, length, segment, true)
                                        measure.getSegment(0f, end * length, segment, true)
                                    }
                                    val fade = 1f - index / 8f
                                    drawPath(segment, tint.copy(alpha = fade * 0.09f), style = glowStroke)
                                    drawPath(segment, tint.copy(alpha = fade * fade * 0.88f), style = lightStroke)
                                }
                                val point = measure.getPosition(head * length)
                                drawCircle(tint.copy(alpha = 0.16f), sparkleSize * 2f, point)
                                drawCircle(lerp(tint, Color.White, 0.45f), particleRadius, point)
                            }

                            // Deterministic particles follow the rounded rim instead of flickering randomly.
                            repeat(5) { index ->
                                val position = (progress + 0.13f + index * 0.173f) % 1f
                                val point = measure.getPosition(position * length)
                                val twinkle = (sin((progress * 2f + index * 0.31f) * 2f * PI).toFloat() + 1f) / 2f
                                val tint = lerp(first, second, index / 4f).copy(alpha = 0.25f + twinkle * 0.5f)
                                if (index % 2 == 0) {
                                    val radius = sparkleSize * (0.6f + twinkle * 0.4f)
                                    drawLine(tint, point - Offset(radius, 0f), point + Offset(radius, 0f),
                                        strokeWidth = sparkleStroke, cap = StrokeCap.Round)
                                    drawLine(tint, point - Offset(0f, radius), point + Offset(0f, radius),
                                        strokeWidth = sparkleStroke, cap = StrokeCap.Round)
                                } else {
                                    drawCircle(tint, particleRadius, point)
                                }
                            }
                        }
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (dark) first else lerp(scheme.onSurface, first, 0.65f),
                modifier = Modifier.padding(end = 8.dp).size(20.dp),
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        }
    }
}
