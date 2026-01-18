package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

object IconUtils {
    /**
     * Gets the full square icon for a given package name.
     * If it's an adaptive icon, it returns the foreground and background combined without a mask.
     */
    fun getFullSquareIcon(context: Context, packageName: String): Drawable? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            getUnmaskedDrawable(context, drawable)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * If the drawable is an AdaptiveIconDrawable, renders it to a full square bitmap.
     * Otherwise returns the original drawable.
     */
    fun getUnmaskedDrawable(context: Context, drawable: Drawable): Drawable {
        if (drawable is AdaptiveIconDrawable) {
            val background = drawable.background
            val foreground = drawable.foreground

            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512

            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)

            background.let {
                it.setBounds(0, 0, width, height)
                it.draw(canvas)
            }
            foreground.let {
                it.setBounds(0, 0, width, height)
                it.draw(canvas)
            }

            return bitmap.toDrawable(context.resources)
        }
        return drawable
    }
}
