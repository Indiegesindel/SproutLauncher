package games.indiegesindel.sproutlauncher.utils

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.InputDevice
import androidx.compose.runtime.staticCompositionLocalOf

class ControllerRumble(private val isEnabled: () -> Boolean) {
    enum class Pulse(val durationMs: Long, val amplitude: Int) {
        EDGE(14, 70),
        CONFIRM(18, 110)
    }

    @Volatile
    var lastDeviceId: Int = NO_DEVICE

    fun pulse(pulse: Pulse) {
        if (!isEnabled() || lastDeviceId == NO_DEVICE) return
        val device = InputDevice.getDevice(lastDeviceId) ?: return
        if (device.sources and (InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK) == 0) return
        val vibrator = device.controllerVibrator() ?: return
        if (!vibrator.hasVibrator()) return
        val amplitude = if (vibrator.hasAmplitudeControl()) pulse.amplitude else VibrationEffect.DEFAULT_AMPLITUDE
        runCatching { vibrator.vibrate(VibrationEffect.createOneShot(pulse.durationMs, amplitude)) }
    }

    @Suppress("DEPRECATION")
    private fun InputDevice.controllerVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) vibratorManager.defaultVibrator else vibrator

    private companion object {
        const val NO_DEVICE = -1
    }
}

val LocalControllerRumble = staticCompositionLocalOf<ControllerRumble?> { null }
