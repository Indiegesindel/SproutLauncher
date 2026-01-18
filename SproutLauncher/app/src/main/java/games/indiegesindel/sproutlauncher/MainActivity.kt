package games.indiegesindel.sproutlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val appTheme by settingsManager.theme.collectAsState()

            SproutLauncherTheme(appTheme = appTheme) {
                val appManager = remember { AppManager(context) }
                val viewModel: MainViewModel = ViewModelProvider(
                    this, 
                    MainViewModelFactory(appManager)
                )[MainViewModel::class.java]

                LaunchedEffect(Unit) {
                    viewModel.loadAppTiles()
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        viewModel.loadAppTiles()
                    }
                }

                MainScreen(viewModel = viewModel)
            }
        }
    }
}

