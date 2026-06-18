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
import androidx.lifecycle.ViewModelProvider
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.ExtendedScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModelFactory
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.SoundManager

class ExtendedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val baseTheme by settingsManager.baseTheme.collectAsState()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()
            val uiSoundsEnabled by settingsManager.uiSoundsEnabled.collectAsState()
            val soundManager = remember { SoundManager(context) }

            SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
                LaunchedEffect(uiSoundsEnabled) { soundManager.isEnabled = uiSoundsEnabled }

                val appManager = remember { AppManager(context) }
                val viewModel: ExtendedViewModel = ViewModelProvider(
                    this,
                    ExtendedViewModelFactory(appManager, packageManager)
                )[ExtendedViewModel::class.java]

                LaunchedEffect(intent) {
                    val filter = intent.getStringExtra("FILTER")
                    if (filter == "HOMESCREEN") {
                        viewModel.setTab(ExtendedViewModel.Tab.HOMESCREEN)
                    } else if (filter == "ALL") {
                        viewModel.setTab(ExtendedViewModel.Tab.ALL)
                    } else if (filter == "SETTINGS") {
                        viewModel.setTab(ExtendedViewModel.Tab.SETTINGS)
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
                            viewModel.loadApps()
                            viewModel.loadTiles()
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
                    ExtendedScreen(
                        viewModel = viewModel,
                        settingsManager = settingsManager,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
