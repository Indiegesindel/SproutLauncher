package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.compositionLocalOf
import games.indiegesindel.sproutlauncher.R

enum class UiSound { CONFIRM, BACK, MOVE }

class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    var isEnabled: Boolean = true

    private val loadedIds = mutableSetOf<Int>()
    private val confirmId: Int
    private val backId: Int
    private val moveId: Int

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds.add(sampleId)
        }
        confirmId = soundPool.load(context, R.raw.cursor_confirm, 1)
        backId = soundPool.load(context, R.raw.cursor_back, 1)
        moveId = soundPool.load(context, R.raw.cursor_move, 1)
    }

    fun play(sound: UiSound) {
        if (!isEnabled) return
        val id = when (sound) {
            UiSound.CONFIRM -> confirmId
            UiSound.BACK -> backId
            UiSound.MOVE -> moveId
        }
        if (id in loadedIds) {
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

val LocalSoundManager = compositionLocalOf<SoundManager?> { null }
