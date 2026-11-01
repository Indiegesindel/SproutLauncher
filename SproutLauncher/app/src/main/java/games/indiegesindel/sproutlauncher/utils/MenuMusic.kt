package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import games.indiegesindel.sproutlauncher.R

class MenuMusic(context: Context, private val isEnabled: () -> Boolean) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var volume = 0f
    private var target = 0f
    private var isForeground = false

    private val fadeStep = object : Runnable {
        override fun run() {
            val p = player ?: return
            val step = if (target > volume) FADE_IN_STEP else FADE_OUT_STEP
            volume = if (target > volume) minOf(target, volume + step) else maxOf(target, volume - step)
            p.setVolume(volume, volume)
            when {
                volume != target -> handler.postDelayed(this, FADE_TICK_MS)
                volume == 0f -> runCatching { p.pause() }
            }
        }
    }

    private val leave = Runnable { if (!isForeground) fadeTo(0f) }

    fun onForeground() {
        isForeground = true
        handler.removeCallbacks(leave)
        refresh()
    }

    fun onBackground() {
        isForeground = false
        handler.postDelayed(leave, LEAVE_GRACE_MS)
    }

    fun duck() = fadeTo(0f)

    fun refresh() {
        if (!isEnabled()) {
            release()
            return
        }
        if (!isForeground) return
        val p = player ?: create() ?: return
        if (!p.isPlaying) {
            if (audioManager?.isMusicActive == true) return
            p.start()
        }
        fadeTo(LEVEL)
    }

    private fun fadeTo(level: Float) {
        target = level
        handler.removeCallbacks(fadeStep)
        handler.post(fadeStep)
    }

    private fun create(): MediaPlayer? = runCatching {
        MediaPlayer.create(
            appContext,
            R.raw.music_lounge,
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            audioManager?.generateAudioSessionId() ?: 0
        )?.apply {
            isLooping = true
            setVolume(0f, 0f)
            volume = 0f
        }
    }.getOrNull().also { player = it }

    private fun release() {
        handler.removeCallbacks(fadeStep)
        player?.release()
        player = null
        volume = 0f
    }

    private companion object {
        const val LEVEL = 0.45f
        const val LEAVE_GRACE_MS = 250L
        const val FADE_TICK_MS = 30L
        const val FADE_IN_STEP = LEVEL / (1200f / FADE_TICK_MS)
        const val FADE_OUT_STEP = LEVEL / (300f / FADE_TICK_MS)
    }
}
