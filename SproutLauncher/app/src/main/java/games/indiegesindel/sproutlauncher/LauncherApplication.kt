package games.indiegesindel.sproutlauncher

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.icons.AppIconFetcher
import kotlinx.coroutines.launch

class LauncherApplication : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        with(container) {
            installedAppsRepository.addPackageRemovedListener(tileRepository::removePackage)
            installedAppsRepository.addPackagesChangedListener {
                SingletonImageLoader.get(this@LauncherApplication).memoryCache?.clear()
            }
            installedAppsRepository.start()

            applicationScope.launch {
                val referenced = tileRepository.tiles.value.allIconUris()
                imageStore.pruneIcons(referenced)
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(AppIconFetcher.Factory(context))
                add(AppIconFetcher.Key())
                add(OkHttpNetworkFetcherFactory(callFactory = { container.httpClient }))
            }
            .crossfade(true)
            .build()
}

private fun List<AppTile>.allIconUris(): Set<String> =
    flatMap { tile -> listOfNotNull(tile.iconUri) + tile.groupTiles.allIconUris() }.toSet()
