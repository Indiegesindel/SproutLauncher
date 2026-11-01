package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.InputMode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.utils.ControllerRumble
import games.indiegesindel.sproutlauncher.utils.LocalControllerRumble
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.SoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

suspend fun FocusRequester.requestFocusWithRetry(maxFrames: Int = 30): Boolean {
    repeat(maxFrames) {
        if (runCatching { requestFocus() }.getOrDefault(false)) return true
        withFrameNanos {}
    }
    return false
}

fun Modifier.gamepadActions(
    enabled: Boolean = true,
    onConfirm: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
): Modifier = onKeyEvent { event ->
    if (!enabled || event.type != KeyEventType.KeyDown) return@onKeyEvent false
    val action = when (event.key) {
        Key.Enter, Key.DirectionCenter, Key.ButtonA -> onConfirm
        Key.ButtonX, Key.ButtonStart, Key.Menu -> onMenu
        Key.ButtonB -> onBack
        else -> null
    }
    action?.invoke()
    action != null
}

fun Modifier.trackNavInput(sound: SoundManager?, rumble: ControllerRumble?): Modifier = onPreviewKeyEvent { event ->
    val native = event.nativeKeyEvent
    if (event.type == KeyEventType.KeyDown) {
        rumble?.lastDeviceId = native.deviceId
        if (event.key.toFocusDirection() != null) sound?.navRepeat = native.repeatCount
    }
    false
}

internal fun Key.toFocusDirection(): FocusDirection? = when (this) {
    Key.DirectionUp -> FocusDirection.Up
    Key.DirectionDown -> FocusDirection.Down
    Key.DirectionLeft -> FocusDirection.Left
    Key.DirectionRight -> FocusDirection.Right
    else -> null
}

class EdgeBump {
    val offset = Animatable(Offset.Zero, Offset.VectorConverter)

    suspend fun play(direction: FocusDirection, distancePx: Float) {
        val target = when (direction) {
            FocusDirection.Up -> Offset(0f, -distancePx)
            FocusDirection.Down -> Offset(0f, distancePx)
            FocusDirection.Left -> Offset(-distancePx, 0f)
            FocusDirection.Right -> Offset(distancePx, 0f)
            else -> return
        }
        offset.animateTo(target, Motion.bumpOut())
        offset.animateTo(Offset.Zero, Motion.bumpBack())
    }
}

@Composable
fun rememberEdgeBump(): EdgeBump = remember { EdgeBump() }

@Composable
fun Modifier.edgeAwareNavigation(enabled: Boolean = true, onEdge: (FocusDirection) -> Unit): Modifier {
    val focusManager = LocalFocusManager.current
    return onPreviewKeyEvent { event ->
        if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        val direction = event.key.toFocusDirection() ?: return@onPreviewKeyEvent false
        if (!focusManager.moveFocus(direction)) onEdge(direction)
        true
    }
}

@Composable
fun rememberEdgeFeedback(bump: EdgeBump): (FocusDirection) -> Unit {
    val sound = LocalSoundManager.current
    val rumble = LocalControllerRumble.current
    val scope = rememberCoroutineScope()
    val distance = with(LocalDensity.current) { Motion.BumpDistance.toPx() }
    return remember(bump, sound, rumble, distance) {
        { direction ->
            sound?.play(UiSound.EDGE)
            rumble?.pulse(ControllerRumble.Pulse.EDGE)
            scope.launch { bump.play(direction, distance) }
        }
    }
}

@Composable
fun Modifier.snapScrollToEdges(state: ScrollState): Modifier {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val snap = with(density) { EDGE_SNAP_DISTANCE.toPx() }.toInt()
    val nudge = with(density) { EDGE_NUDGE_DISTANCE.toPx() }.toInt()
    return onPreviewKeyEvent { event ->
        val up = event.key == Key.DirectionUp
        if (event.type != KeyEventType.KeyDown || (!up && event.key != Key.DirectionDown)) return@onPreviewKeyEvent false
        val before = state.value
        scope.launch {
            delay(FOCUS_SETTLE_MS)
            snapshotFlow { state.isScrollInProgress }.first { !it }
            val value = state.value
            val toEdge = if (up) value else state.maxValue - value
            val scrolledThisWay = if (up) value < before else value > before
            if (toEdge in 1..snap && (scrolledThisWay || toEdge <= nudge)) {
                state.animateScrollTo(if (up) 0 else state.maxValue)
            }
        }
        false
    }
}

private val EDGE_SNAP_DISTANCE = 200.dp
private val EDGE_NUDGE_DISTANCE = 32.dp
private const val FOCUS_SETTLE_MS = 40L

@Composable
fun RefocusOnKeyInput(hasFocus: Boolean, target: () -> FocusRequester?) {
    val inputMode = LocalInputModeManager.current.inputMode
    LaunchedEffect(inputMode) {
        if (inputMode == InputMode.Keyboard && !hasFocus) target()?.requestFocusWithRetry()
    }
}
