package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

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
