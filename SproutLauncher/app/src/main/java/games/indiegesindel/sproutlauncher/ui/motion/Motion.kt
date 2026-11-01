package games.indiegesindel.sproutlauncher.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp

object Motion {
    fun <T> focus() = spring<T>(dampingRatio = 0.6f, stiffness = 1500f)
    fun <T> press() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    fun <T> snap() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
    fun <T> bumpOut() = tween<T>(durationMillis = 52, easing = FastOutSlowInEasing)
    fun <T> bumpBack() = spring<T>(dampingRatio = 0.5f, stiffness = 1500f)
    fun <T> pop() = spring<T>(dampingRatio = 0.5f, stiffness = 900f)

    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val FOCUS_SCALE = 1.05f
    const val QUICK_ACTION_FOCUS_SCALE = 1.08f
    const val PRESS_SCALE = 0.93f
    const val PRESS_HOLD_MS = 90L
    val BumpDistance = 7.dp

    val RingGap = 5.dp
    val RingWidth = 4.dp
    const val RING_BREATHE_HALF_MS = 600
    const val RING_MIN_ALPHA = 0.55f
    const val RING_GLINT_MS = 420

    const val FOLDER_ZOOM_MS = 340
    const val FOLDER_ITEM_DELAY_MS = 120L
    val FolderBlur = 8.dp

    const val ECHO_FADE_MS = 450
}
