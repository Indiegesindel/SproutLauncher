package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.staticCompositionLocalOf
import games.indiegesindel.sproutlauncher.R
import java.util.concurrent.ConcurrentHashMap

enum class UiSound { CONFIRM, BACK, MOVE }

class SoundManager(context: Context, private val isEnabled: () -> Boolean) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSamples = ConcurrentHashMap.newKeySet<Int>()

    private val samples: Map<UiSound, Int>

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSamples += sampleId
        }
        samples = mapOf(
            UiSound.CONFIRM to soundPool.load(context, R.raw.cursor_confirm, 1),
            UiSound.BACK to soundPool.load(context, R.raw.cursor_back, 1),
            UiSound.MOVE to soundPool.load(context, R.raw.cursor_move, 1)
        )
    }

    fun play(sound: UiSound) {
        if (!isEnabled()) return
        val sampleId = samples.getValue(sound)
        if (sampleId in loadedSamples) soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
    }
}

val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }
