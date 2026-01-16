package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build

object IconUtils {
    /**
     * Gets the full square icon for a given package name.
     * If it's an adaptive icon, it returns the foreground and background combined without a mask.
     */
    fun getFullSquareIcon(context: Context, packageName: String): Drawable? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            getUnmaskedDrawable(drawable)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * If the drawable is an AdaptiveIconDrawable, renders it to a full square bitmap.
     * Otherwise returns the original drawable.
     */
    fun getUnmaskedDrawable(drawable: Drawable): Drawable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            val background = drawable.background
            val foreground = drawable.foreground

            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            background?.let {
                it.setBounds(0, 0, width, height)
                it.draw(canvas)
            }
            foreground?.let {
                it.setBounds(0, 0, width, height)
                it.draw(canvas)
            }

            return BitmapDrawable(null, bitmap)
        }
        return drawable
    }
}
