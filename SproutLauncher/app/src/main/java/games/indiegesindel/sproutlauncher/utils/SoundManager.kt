package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import androidx.compose.runtime.staticCompositionLocalOf
import games.indiegesindel.sproutlauncher.R
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

enum class UiSound(@param:RawRes val res: Int) {
    MOVE(R.raw.ui_move),
    MOVE_SETTLE(R.raw.ui_move_settle),
    EDGE(R.raw.ui_edge),
    CONFIRM(R.raw.ui_confirm),
    CONFIRM_SETTLE(R.raw.ui_confirm_settle),
    BACK(R.raw.ui_back),
    LAUNCH(R.raw.ui_launch),
    SHEET_OPEN(R.raw.ui_sheet_open),
    SHEET_CLOSE(R.raw.ui_sheet_close),
    FOLDER_OPEN(R.raw.ui_folder_open),
    FOLDER_CLOSE(R.raw.ui_folder_close),
    ERROR(R.raw.ui_error),
    TOGGLE_ON(R.raw.ui_toggle_on),
    TOGGLE_OFF(R.raw.ui_toggle_off)
}

class SoundManager(context: Context, private val isEnabled: () -> Boolean) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSamples = ConcurrentHashMap.newKeySet<Int>()
    private val samples: Map<UiSound, Int>
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    var navRepeat: Int = 0

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSamples += sampleId
        }
        samples = UiSound.entries.associateWith { soundPool.load(context, it.res, 1) }
    }

    fun play(sound: UiSound, rate: Float = 1f) {
        if (!isEnabled()) return
        val sampleId = samples.getValue(sound)
        if (sampleId in loadedSamples) soundPool.play(sampleId, 1f, 1f, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun playDelayed(sound: UiSound, delayMs: Long) {
        handler.postDelayed({ play(sound) }, delayMs)
    }

    fun playMove(column: Int = 0, row: Int = 0, lowered: Boolean = false) {
        val degree = PENTATONIC[column.mod(PENTATONIC.size)]
        val rowFactor = if (row > 0) ROW_STEP else 1f
        val base = if (lowered) DOCK_FACTOR else 1f
        val repeat = REPEAT_STEP.pow(navRepeat.coerceAtMost(MAX_REPEAT_STEPS))
        play(UiSound.MOVE, degree * rowFactor * base * repeat)
    }

    fun playWake() {
        play(UiSound.TOGGLE_ON)
        playDelayed(UiSound.CONFIRM_SETTLE, 70)
    }

    private companion object {
        val PENTATONIC = floatArrayOf(1f, 9f / 8f, 5f / 4f, 3f / 2f, 5f / 3f)
        const val ROW_STEP = 0.89f
        const val DOCK_FACTOR = 0.75f
        const val REPEAT_STEP = 1.025f
        const val MAX_REPEAT_STEPS = 8
    }
}

val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }
