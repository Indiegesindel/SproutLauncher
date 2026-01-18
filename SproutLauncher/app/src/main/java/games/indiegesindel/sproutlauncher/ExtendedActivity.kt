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
import games.indiegesindel.sproutlauncher.ui.screens.ExtendedScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModelFactory

class ExtendedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val appTheme by settingsManager.theme.collectAsState()

            SproutLauncherTheme(appTheme = appTheme) {
                val appManager = remember { AppManager(context) }
                val viewModel: ExtendedViewModel = ViewModelProvider(
                    this,
                    ExtendedViewModelFactory(appManager, packageManager)
                )[ExtendedViewModel::class.java]

                ExtendedScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
