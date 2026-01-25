package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

object IconUtils {
    fun getFullSquareIcon(context: Context, packageName: String, size: Int = 512): Drawable? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            getUnmaskedDrawable(context, drawable, size)
        } catch (e: Exception) {
            null
        }
    }

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

    fun getShortcutIcon(context: Context, packageName: String, shortcutId: String): Drawable? {
        if (shortcutId.startsWith("legacy:")) return null
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return null
        val query = LauncherApps.ShortcutQuery().apply {
            setPackage(packageName)
            setShortcutIds(listOf(shortcutId))
            setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
        }
        return try {
            val shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle())
            shortcuts?.firstOrNull()?.let {
                launcherApps.getShortcutIconDrawable(it, context.resources.displayMetrics.densityDpi)
            }
        } catch (e: Exception) {
            null
        }
    }
}
