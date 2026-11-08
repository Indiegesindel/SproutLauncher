package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import androidx.annotation.RawRes
import androidx.compose.runtime.staticCompositionLocalOf
import games.indiegesindel.sproutlauncher.R
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

enum class UiSound(@param:RawRes val res: Int, val gain: Float = 1f) {
    MOVE(R.raw.ui_move),
    EDGE(R.raw.ui_edge),
    CONFIRM(R.raw.ui_confirm),
    BACK(R.raw.ui_back),
    LAUNCH(R.raw.ui_launch),
    SHEET_OPEN(R.raw.ui_sheet_open),
    SHEET_CLOSE(R.raw.ui_sheet_close),
    FOLDER_OPEN(R.raw.ui_folder_open),
    FOLDER_CLOSE(R.raw.ui_folder_close),
    ERROR(R.raw.ui_error),
    TOGGLE_ON(R.raw.ui_toggle_on),
    TOGGLE_OFF(R.raw.ui_toggle_off),
    WAKE(R.raw.ui_wake)
}

class SoundManager(context: Context, private val isEnabled: () -> Boolean) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSamples = ConcurrentHashMap.newKeySet<Int>()
    private val samples: Map<UiSound, Int>

    @Volatile
    var navRepeat: Int = 0
    private var lastMoveAt = 0L

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSamples += sampleId
        }
        samples = UiSound.entries.associateWith { soundPool.load(context, it.res, 1) }
    }

    fun play(sound: UiSound, rate: Float = 1f, volume: Float = 1f) {
        if (!isEnabled()) return
        val sampleId = samples.getValue(sound)
        val v = (sound.gain * volume).coerceIn(0f, 1f)
        if (sampleId in loadedSamples) soundPool.play(sampleId, v, v, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun playMove(column: Int = 0, row: Int = 0, lowered: Boolean = false) {
        val now = SystemClock.uptimeMillis()
        val repeating = navRepeat > 0
        if (repeating && now - lastMoveAt < MIN_REPEAT_GAP_MS) return
        lastMoveAt = now
        val degree = when (MOVE_PITCH) {
            MovePitch.FLAT -> 1f + Random.nextFloat() * JITTER - JITTER / 2
            MovePitch.GENTLE -> GENTLE_STEPS[column.mod(GENTLE_STEPS.size)] * if (row > 0) ROW_STEP else 1f
        }
        val base = if (lowered) DOCK_FACTOR else 1f
        play(UiSound.MOVE, degree * base, if (repeating) REPEAT_VOLUME else 1f)
    }

    fun playWake() = play(UiSound.WAKE)

    private enum class MovePitch { FLAT, GENTLE }

    private companion object {
        val MOVE_PITCH = MovePitch.FLAT
        val GENTLE_STEPS = floatArrayOf(1f, 9f / 8f, 5f / 4f)
        const val JITTER = 0.03f
        const val ROW_STEP = 0.94f
        const val DOCK_FACTOR = 0.94f
        const val MIN_REPEAT_GAP_MS = 60L
        const val REPEAT_VOLUME = 0.7f
    }
}

val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }
