package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.ui.theme.LocalSproutColors
import kotlin.math.PI
import kotlin.math.sin

val LocalFocusPulse = compositionLocalOf { 0 }

@Composable
fun Modifier.focusRing(focused: Boolean, cornerRadius: Dp?, color: Color = LocalSproutColors.current.focus): Modifier {
    val appear by animateFloatAsState(if (focused) 1f else 0f, Motion.snap(), label = "ringAppear")
    val breathe: State<Float>? = if (focused) {
        rememberInfiniteTransition(label = "ringBreathe").animateFloat(
            initialValue = 1f,
            targetValue = Motion.RING_MIN_ALPHA,
            animationSpec = infiniteRepeatable(tween(Motion.RING_BREATHE_HALF_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "ringAlpha"
        )
    } else null
    val glint = remember { Animatable(1f) }
    val pulse = LocalFocusPulse.current
    LaunchedEffect(focused, pulse) {
        if (!focused) return@LaunchedEffect
        glint.snapTo(0f)
        glint.animateTo(1f, tween(Motion.RING_GLINT_MS, easing = FastOutSlowInEasing))
    }

    return drawWithContent {
        drawContent()
        if (appear <= 0.01f) return@drawWithContent
        val width = Motion.RingWidth.toPx()
        val outset = Motion.RingGap.toPx() + width / 2f
        val ringSize = Size(size.width + outset * 2, size.height + outset * 2)
        val radius = cornerRadius?.let { CornerRadius(it.toPx() + outset) } ?: CornerRadius(ringSize.minDimension / 2f)
        val alpha = appear * (breathe?.value ?: 1f)
        drawRoundRect(color = color, topLeft = Offset(-outset, -outset), size = ringSize, cornerRadius = radius, style = Stroke(width), alpha = alpha)

        val g = glint.value
        if (g < 1f) {
            val p = (g * 1.1f - 0.05f).coerceIn(0f, 1f)
            val stops = arrayOf(
                0f to Color.Transparent,
                (p - 0.07f).coerceIn(0f, 1f) to Color.Transparent,
                p to Color.White,
                (p + 0.09f).coerceIn(0f, 1f) to Color.Transparent,
                1f to Color.Transparent
            )
            drawRoundRect(
                brush = Brush.sweepGradient(*stops, center = center),
                topLeft = Offset(-outset, -outset),
                size = ringSize,
                cornerRadius = radius,
                style = Stroke(width),
                alpha = appear * sin(g * PI).toFloat() * 0.95f
            )
        }
    }
}
