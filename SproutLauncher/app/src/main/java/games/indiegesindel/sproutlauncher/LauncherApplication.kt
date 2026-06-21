package games.indiegesindel.sproutlauncher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import android.content.pm.ResolveInfo
import games.indiegesindel.sproutlauncher.utils.IconUtils

class LauncherApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(ResolveInfoFetcher.Factory())
            }
            .build()
    }
}

class ResolveInfoFetcher(
    private val data: ResolveInfo,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val pm = options.context.packageManager
        val icon = data.loadIcon(pm)
        val unmaskedIcon = IconUtils.getUnmaskedDrawable(options.context, icon)
        return DrawableResult(
            drawable = unmaskedIcon,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<ResolveInfo> {
        override fun create(data: ResolveInfo, options: Options, imageLoader: ImageLoader): Fetcher {
            return ResolveInfoFetcher(data, options)
        }
    }
}
