package games.indiegesindel.sproutlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.AppTileSettingsScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModelFactory

class AppTileSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tileId = intent.getStringExtra("TILE_ID") ?: finish().run { return }
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val baseTheme by settingsManager.baseTheme.collectAsState()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()

            SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
                val appManager = remember { AppManager(context) }
                val viewModel: AppTileSettingsViewModel = ViewModelProvider(
                    this,
                    AppTileSettingsViewModelFactory(appManager, tileId)
                )[AppTileSettingsViewModel::class.java]

                AppTileSettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onDone = { finish() }
                )
            }
        }
    }
}
