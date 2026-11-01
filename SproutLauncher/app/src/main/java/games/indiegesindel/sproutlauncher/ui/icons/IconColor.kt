package games.indiegesindel.sproutlauncher.ui.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object IconColor {
    private val cache = ConcurrentHashMap<Any, Int>()
    private const val SAMPLE_PX = 24
    private const val NONE = Color.TRANSPARENT

    @ColorInt
    suspend fun of(context: Context, model: Any): Int? {
        cache[model]?.let { return it.takeIf { c -> c != NONE } }
        val request = ImageRequest.Builder(context)
            .data(model)
            .size(SAMPLE_PX)
            .allowHardware(false)
            .build()
        val result = SingletonImageLoader.get(context).execute(request) as? SuccessResult
        val color = result?.image?.toBitmap()?.let(::dominant) ?: NONE
        cache[model] = color
        return color.takeIf { it != NONE }
    }

    @ColorInt
    internal fun dominant(bitmap: Bitmap): Int {
        val buckets = 12
        val weight = FloatArray(buckets)
        val r = FloatArray(buckets)
        val g = FloatArray(buckets)
        val b = FloatArray(buckets)
        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            val c = bitmap.getPixel(x, y)
            if (Color.alpha(c) < 128) continue
            Color.colorToHSV(c, hsv)
            val w = hsv[1] * hsv[2]
            if (w < 0.08f) continue
            val i = (hsv[0] / 360f * buckets).toInt().coerceIn(0, buckets - 1)
            weight[i] += w
            r[i] += Color.red(c) * w
            g[i] += Color.green(c) * w
            b[i] += Color.blue(c) * w
        }
        val best = weight.indices.maxByOrNull { weight[it] } ?: return NONE
        val total = weight[best]
        if (total <= 0f) return NONE
        return Color.rgb((r[best] / total).roundToInt(), (g[best] / total).roundToInt(), (b[best] / total).roundToInt())
    }
}
