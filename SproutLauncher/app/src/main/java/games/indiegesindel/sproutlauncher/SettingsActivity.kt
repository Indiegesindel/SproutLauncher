package games.indiegesindel.sproutlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.SettingsScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val baseTheme by settingsManager.baseTheme.collectAsState()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()

            SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
                SettingsScreen(
                    settingsManager = settingsManager,
                    onBack = { finish() }
                )
            }
        }
    }
}
