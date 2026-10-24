package games.indiegesindel.sproutlauncher.ui.icons

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.size.pxOrElse
import games.indiegesindel.sproutlauncher.utils.IconUtils

class AppIconFetcher(
    private val context: Context,
    private val icon: AppIcon,
    private val sizePx: Int
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val drawable = load(icon) ?: error("No icon available for $icon")
        return ImageFetchResult(image = drawable.asImage(), isSampled = false, dataSource = DataSource.DISK)
    }

    private fun load(icon: AppIcon): Drawable? = when (icon) {
        is AppIcon.Package -> packageIcon(icon.packageName)?.let(::unmask)
        is AppIcon.Activity -> activityIcon(icon.packageName, icon.activityName)?.let(::unmask)
        is AppIcon.Shortcut -> shortcutIcon(icon.packageName, icon.shortcutId)
            ?: load(AppIcon.Package(icon.packageName))
        is AppIcon.Themed -> packageIcon(icon.packageName)?.let { drawable ->
            IconUtils.monochrome(context.resources, drawable, icon.tint, sizePx) ?: unmask(drawable)
        }
    }

    private fun unmask(drawable: Drawable) = IconUtils.unmask(context.resources, drawable, sizePx)

    private fun packageIcon(packageName: String): Drawable? = runCatching {
        context.packageManager.getApplicationIcon(packageName)
    }.getOrNull()

    private fun activityIcon(packageName: String, activityName: String): Drawable? = runCatching {
        context.packageManager.getActivityIcon(ComponentName(packageName, activityName))
    }.getOrNull()

    private fun shortcutIcon(packageName: String, shortcutId: String): Drawable? = runCatching {
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setShortcutIds(listOf(shortcutId))
            .setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
        launcherApps.getShortcuts(query, Process.myUserHandle())?.firstOrNull()?.let {
            launcherApps.getShortcutIconDrawable(it, context.resources.displayMetrics.densityDpi)
        }
    }.getOrNull()

    class Factory(private val context: Context) : Fetcher.Factory<AppIcon> {
        override fun create(data: AppIcon, options: Options, imageLoader: ImageLoader): Fetcher {
            val sizePx = options.size.width.pxOrElse { IconUtils.DEFAULT_ICON_SIZE }
                .coerceIn(MIN_SIZE_PX, IconUtils.DEFAULT_ICON_SIZE)
            return AppIconFetcher(context.applicationContext, data, sizePx)
        }
    }

    class Key : Keyer<AppIcon> {
        override fun key(data: AppIcon, options: Options): String = data.toString()
    }

    private companion object {
        const val MIN_SIZE_PX = 64
    }
}
