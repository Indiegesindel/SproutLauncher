package games.indiegesindel.sproutlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.MainScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModelFactory
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModelFactory
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.SoundManager

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var allAppsViewModel: AllAppsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)
        val appManager = AppManager(this)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(appManager, settingsManager)
        )[MainViewModel::class.java]
        allAppsViewModel = ViewModelProvider(
            this,
            AllAppsViewModelFactory(appManager, packageManager)
        )[AllAppsViewModel::class.java]

        setContent {
            val context = LocalContext.current
            val baseTheme by settingsManager.baseTheme.collectAsState()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()
            val uiSoundsEnabled by settingsManager.uiSoundsEnabled.collectAsState()
            val soundManager = remember { SoundManager(context) }

            SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
                LaunchedEffect(uiSoundsEnabled) { soundManager.isEnabled = uiSoundsEnabled }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        viewModel.loadAppTiles()
                        allAppsViewModel.loadTiles()
                    }
                }

                DisposableEffect(Unit) {
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
                                val packageName = intent.data?.schemeSpecificPart
                                if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                                    appManager.removeTilesForPackage(packageName)
                                }
                            }
                            viewModel.loadAppTiles()
                            allAppsViewModel.loadApps()
                            allAppsViewModel.loadTiles()
                        }
                    }
                    val filter = IntentFilter().apply {
                        addAction(Intent.ACTION_PACKAGE_ADDED)
                        addAction(Intent.ACTION_PACKAGE_REMOVED)
                        addAction(Intent.ACTION_PACKAGE_REPLACED)
                        addDataScheme("package")
                    }
                    context.registerReceiver(receiver, filter)
                    onDispose {
                        context.unregisterReceiver(receiver)
                        soundManager.release()
                    }
                }

                CompositionLocalProvider(LocalSoundManager provides soundManager) {
                    MainScreen(
                        viewModel = viewModel,
                        allAppsViewModel = allAppsViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.resetState()
        }
    }
}
