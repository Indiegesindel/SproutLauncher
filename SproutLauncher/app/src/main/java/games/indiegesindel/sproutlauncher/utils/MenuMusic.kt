package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import games.indiegesindel.sproutlauncher.R
import java.nio.ByteOrder

class MenuMusic(
    context: Context,
    private val isEnabled: () -> Boolean,
    private val customSources: () -> List<String>
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val lock = Object()
    @Volatile private var worker: Thread? = null
    @Volatile private var track: AudioTrack? = null
    @Volatile private var shouldPlay = false
    private var playlist: List<String> = emptyList()
    private var volume = 0f
    private var target = 0f
    private var isForeground = false

    private val fadeStep = object : Runnable {
        override fun run() {
            val step = if (target > volume) FADE_IN_STEP else FADE_OUT_STEP
            volume = if (target > volume) minOf(target, volume + step) else maxOf(target, volume - step)
            track?.setVolume(volume)
            when {
                volume != target -> handler.postDelayed(this, FADE_TICK_MS)
                volume == 0f -> setPlaying(false)
            }
        }
    }

    private val leave = Runnable { if (!isForeground) fadeTo(0f) }
    private val retry = Runnable { refresh() }

    fun onForeground() {
        isForeground = true
        handler.removeCallbacks(leave)
        refresh()
    }

    fun onBackground() {
        isForeground = false
        handler.removeCallbacks(retry)
        handler.postDelayed(leave, LEAVE_GRACE_MS)
    }

    fun duck() = fadeTo(0f)

    fun refresh() {
        if (!isEnabled()) {
            release()
            return
        }
        val sources = customSources()
        if (sources != playlist) {
            playlist = sources
            stopWorker()
        }
        if (!isForeground) return
        if (!shouldPlay || worker == null) {
            handler.removeCallbacks(retry)
            if (audioManager?.isMusicActive == true) {
                handler.postDelayed(retry, BUSY_RETRY_MS)
                return
            }
            ensureWorker()
            setPlaying(true)
        }
        fadeTo(LEVEL)
    }

    private fun fadeTo(level: Float) {
        target = level
        handler.removeCallbacks(fadeStep)
        handler.post(fadeStep)
    }

    private fun setPlaying(play: Boolean) = synchronized(lock) {
        shouldPlay = play
        lock.notifyAll()
    }

    private fun ensureWorker() {
        if (worker != null) return
        val sources = playlist
        worker = Thread({
            runCatching { run(sources) }.onFailure { Log.w(TAG, "music stopped", it) }
            if (worker === Thread.currentThread()) worker = null
        }, "MenuMusic").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun stopWorker() = synchronized(lock) {
        worker = null
        lock.notifyAll()
    }

    private fun alive() = worker === Thread.currentThread()

    private fun run(sources: List<String>) {
        var queue: List<Source> = sources.map { Source.File(Uri.parse(it)) }.ifEmpty { listOf(Source.BuiltIn) }
        var index = 0
        var failuresInARow = 0
        val output = Output()
        try {
            while (alive()) {
                if (stream(queue[index], output)) {
                    failuresInARow = 0
                } else if (++failuresInARow >= queue.size) {
                    if (queue == listOf(Source.BuiltIn)) return
                    Log.w(TAG, "no custom track is playable, falling back to the built-in loop")
                    queue = listOf(Source.BuiltIn)
                    index = 0
                    failuresInARow = 0
                    continue
                }
                index = (index + 1) % queue.size
            }
        } finally {
            output.release()
        }
    }

    private sealed interface Source {
        data object BuiltIn : Source
        data class File(val uri: Uri) : Source
    }

    private fun stream(source: Source, output: Output): Boolean {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            when (source) {
                Source.BuiltIn -> appContext.resources.openRawResourceFd(R.raw.music_lounge).use {
                    extractor.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                }
                is Source.File -> extractor.setDataSource(appContext, source.uri, null)
            }
            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return false
            val format = extractor.getTrackFormat(trackIndex)
            extractor.selectTrack(trackIndex)
            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else -1L
            val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec = decoder
            decoder.configure(format, null, null, 0)
            decoder.start()
            var sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var isFloat = false
            var framesLeft = if (durationUs > 0) durationUs * sampleRate / 1_000_000L else Long.MAX_VALUE
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            while (alive()) {
                if (!inputDone) {
                    val i = decoder.dequeueInputBuffer(CODEC_TIMEOUT_US)
                    if (i >= 0) {
                        val n = extractor.readSampleData(decoder.getInputBuffer(i)!!, 0)
                        if (n < 0) {
                            decoder.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(i, 0, n, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val o = decoder.dequeueOutputBuffer(info, CODEC_TIMEOUT_US)
                if (o == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val out = decoder.outputFormat
                    sampleRate = out.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    channels = out.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    isFloat = out.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                        out.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                    if (durationUs > 0) framesLeft = durationUs * sampleRate / 1_000_000L
                    continue
                }
                if (o < 0) continue
                val buffer = decoder.getOutputBuffer(o)!!.order(ByteOrder.nativeOrder())
                buffer.position(info.offset)
                buffer.limit(info.offset + info.size)
                val bytesPerFrame = (if (isFloat) 4 else 2) * channels
                val frames = minOf(info.size.toLong() / bytesPerFrame, framesLeft).toInt()
                if (frames > 0) {
                    val outChannels = minOf(channels, 2)
                    val pcm = output.scratch(frames * outChannels)
                    if (isFloat) {
                        val f = buffer.slice().order(ByteOrder.nativeOrder()).asFloatBuffer()
                        for (fr in 0 until frames) for (c in 0 until outChannels) {
                            pcm[fr * outChannels + c] = (f.get(fr * channels + c).coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                        }
                    } else {
                        val s = buffer.slice().order(ByteOrder.nativeOrder()).asShortBuffer()
                        if (channels == outChannels) {
                            s.get(pcm, 0, frames * channels)
                        } else {
                            for (fr in 0 until frames) for (c in 0 until outChannels) pcm[fr * outChannels + c] = s.get(fr * channels + c)
                        }
                    }
                    framesLeft -= frames
                    output.write(pcm, frames * outChannels, sampleRate, outChannels)
                }
                decoder.releaseOutputBuffer(o, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 || framesLeft <= 0) return true
            }
            return true
        } catch (e: InterruptedException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "can't play $source", e)
            return false
        } finally {
            runCatching { codec?.stop() }
            codec?.release()
            extractor.release()
        }
    }

    private inner class Output {
        private var buffer = ShortArray(0)
        private var own: AudioTrack? = null
        private var rate = 0
        private var channels = 0

        fun scratch(size: Int): ShortArray {
            if (buffer.size < size) buffer = ShortArray(size)
            return buffer
        }

        fun write(pcm: ShortArray, size: Int, sampleRate: Int, channelCount: Int) {
            var offset = 0
            while (offset < size && alive()) {
                val t = trackFor(sampleRate, channelCount)
                if (t == null) {
                    Thread.sleep(RETRY_MS)
                    continue
                }
                synchronized(lock) {
                    if (!shouldPlay && alive()) {
                        t.pause()
                        while (!shouldPlay && alive()) lock.wait()
                    }
                }
                if (!alive()) return
                if (t.playState != AudioTrack.PLAYSTATE_PLAYING) t.play()
                val written = t.write(pcm, offset, size - offset)
                if (written < 0) {
                    Log.w(TAG, "write failed ($written), rebuilding track")
                    release()
                    Thread.sleep(RETRY_MS)
                    continue
                }
                offset += written
            }
        }

        private fun trackFor(sampleRate: Int, channelCount: Int): AudioTrack? {
            val current = own
            if (current != null && rate == sampleRate && channels == channelCount) return current
            release()
            val created = runCatching { createTrack(sampleRate, channelCount) }
                .onFailure { Log.w(TAG, "track failed", it) }
                .getOrNull() ?: return null
            created.setVolume(volume)
            rate = sampleRate
            channels = channelCount
            own = created
            if (alive()) track = created
            return created
        }

        fun release() {
            val t = own ?: return
            own = null
            if (track === t) track = null
            runCatching { t.stop() }
            t.release()
        }
    }

    private fun createTrack(sampleRate: Int, channelCount: Int): AudioTrack {
        val channelMask = if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        val minBuffer = AudioTrack.getMinBufferSize(sampleRate, channelMask, AudioFormat.ENCODING_PCM_16BIT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBuffer * 2, MIN_BUFFER_BYTES))
            .build()
    }

    private fun release() {
        handler.removeCallbacks(fadeStep)
        handler.removeCallbacks(retry)
        stopWorker()
        shouldPlay = false
        volume = 0f
    }

    private companion object {
        const val TAG = "MenuMusic"
        const val LEVEL = 0.45f
        const val LEAVE_GRACE_MS = 250L
        const val FADE_TICK_MS = 30L
        const val FADE_IN_STEP = LEVEL / (1200f / FADE_TICK_MS)
        const val FADE_OUT_STEP = LEVEL / (300f / FADE_TICK_MS)
        const val CODEC_TIMEOUT_US = 10_000L
        const val MIN_BUFFER_BYTES = 32 * 1024
        const val RETRY_MS = 500L
        const val BUSY_RETRY_MS = 700L
    }
}
