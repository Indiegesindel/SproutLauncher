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
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val confirmId = soundPool.load(context, R.raw.cursor_confirm, 1)
    private val backId = soundPool.load(context, R.raw.cursor_back, 1)
    private val moveId = soundPool.load(context, R.raw.cursor_move, 1)

    fun play(sound: UiSound) {
        val id = when (sound) {
            UiSound.CONFIRM -> confirmId
            UiSound.BACK -> backId
            UiSound.MOVE -> moveId
        }
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

val LocalSoundManager = compositionLocalOf<SoundManager?> { null }
