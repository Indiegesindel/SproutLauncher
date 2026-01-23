package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

object IconUtils {
    /**
     * Gets the full square icon for a given package name.
     * If it's an adaptive icon, it returns the foreground and background combined without a mask.
     */
    fun getFullSquareIcon(context: Context, packageName: String, size: Int = 512): Drawable? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            getUnmaskedDrawable(context, drawable, size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets the monochrome icon for a given package name if available, tinted to the provided color.
     * If monochrome is not available, returns the full square icon.
     */
    fun getThemedIcon(context: Context, packageName: String, tintColor: Color, size: Int = 512): Drawable? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && drawable is AdaptiveIconDrawable) {
                val monochrome = drawable.monochrome
                if (monochrome != null) {
                    val bitmap = createBitmap(size, size)
                    val canvas = Canvas(bitmap)
                    monochrome.setBounds(0, 0, size, size)
                    monochrome.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        tintColor.toArgb(),
                        BlendModeCompat.SRC_IN
                    )
                    monochrome.draw(canvas)
                    return bitmap.toDrawable(context.resources)
                }
            }
            getUnmaskedDrawable(context, drawable, size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * If the drawable is an AdaptiveIconDrawable, renders it to a full square bitmap.
     * Otherwise returns the original drawable.
     */
    fun getUnmaskedDrawable(context: Context, drawable: Drawable, size: Int = 512): Drawable {
        if (drawable is AdaptiveIconDrawable) {
            val background = drawable.background
            val foreground = drawable.foreground

            val bitmap = createBitmap(size, size)
            val canvas = Canvas(bitmap)

            background.let {
                it.setBounds(0, 0, size, size)
                it.draw(canvas)
            }
            foreground.let {
                it.setBounds(0, 0, size, size)
                it.draw(canvas)
            }

            return bitmap.toDrawable(context.resources)
        }
        return drawable
    }
}
