package games.indiegesindel.sproutlauncher

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
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.SettingsTab
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.SoundManager

class SettingsActivity : ComponentActivity() {
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

                DisposableEffect(Unit) {
                    onDispose { soundManager.release() }
                }

                CompositionLocalProvider(LocalSoundManager provides soundManager) {
                    SettingsTab(
                        settingsManager = settingsManager,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
