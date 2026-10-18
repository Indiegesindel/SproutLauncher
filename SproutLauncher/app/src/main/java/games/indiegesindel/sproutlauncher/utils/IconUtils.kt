package games.indiegesindel.sproutlauncher.utils

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

object IconUtils {
    const val DEFAULT_ICON_SIZE = 512

    fun unmask(resources: Resources, drawable: Drawable, size: Int = DEFAULT_ICON_SIZE): Drawable {
        if (drawable !is AdaptiveIconDrawable) return drawable
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        listOfNotNull(drawable.background, drawable.foreground).forEach { layer ->
            layer.setBounds(0, 0, size, size)
            layer.draw(canvas)
        }
        return bitmap.toDrawable(resources)
    }

    fun monochrome(resources: Resources, drawable: Drawable, @ColorInt tint: Int, size: Int = DEFAULT_ICON_SIZE): Drawable? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || drawable !is AdaptiveIconDrawable) return null
        val layer = drawable.monochrome ?: return null
        val bitmap = createBitmap(size, size)
        layer.setBounds(0, 0, size, size)
        layer.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tint, BlendModeCompat.SRC_IN)
        layer.draw(Canvas(bitmap))
        return bitmap.toDrawable(resources)
    }
}
