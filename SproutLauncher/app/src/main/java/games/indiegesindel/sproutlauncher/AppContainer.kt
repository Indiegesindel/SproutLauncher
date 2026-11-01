package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.pm.LauncherApps
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.InstalledAppsRepository
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.data.SteamGridDbClient
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.utils.ControllerRumble
import games.indiegesindel.sproutlauncher.utils.MenuMusic
import games.indiegesindel.sproutlauncher.utils.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val ioDispatcher = Dispatchers.IO

    val httpClient: OkHttpClient by lazy { OkHttpClient() }

    val tileRepository: TileRepository by lazy {
        TileRepository(appContext.getSharedPreferences(TileRepository.PREFS_NAME, Context.MODE_PRIVATE))
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            prefs = appContext.getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE),
            shortScreen = appContext.resources.configuration.smallestScreenWidthDp < SHORT_SCREEN_DP
        )
    }

    val installedAppsRepository: InstalledAppsRepository by lazy {
        InstalledAppsRepository(
            launcherApps = appContext.getSystemService(LauncherApps::class.java),
            scope = applicationScope,
            ioDispatcher = ioDispatcher
        )
    }

    val imageStore: ImageStore by lazy {
        ImageStore(appContext.filesDir, appContext.contentResolver, ioDispatcher)
    }

    val steamGridDbClient: SteamGridDbClient by lazy {
        SteamGridDbClient(httpClient, { settingsRepository.steamGridDbApiKey.value }, ioDispatcher)
    }

    val soundManager: SoundManager by lazy {
        SoundManager(appContext) { settingsRepository.uiSoundsEnabled.value }
    }

    val menuMusic: MenuMusic by lazy {
        MenuMusic(appContext) { settingsRepository.menuMusicEnabled.value }.also { music ->
            settingsRepository.menuMusicEnabled.flow.drop(1).onEach { music.refresh() }.launchIn(applicationScope)
        }
    }

    val controllerRumble: ControllerRumble by lazy {
        ControllerRumble { settingsRepository.controllerRumbleEnabled.value }
    }
}

private const val SHORT_SCREEN_DP = 400

val Context.appContainer: AppContainer
    get() = (applicationContext as LauncherApplication).container
