package games.indiegesindel.sproutlauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.data.SteamGridDBManager
import games.indiegesindel.sproutlauncher.ui.screens.SteamGridDBScreen
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDBViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDBViewModelFactory

class SteamGridDBActivity : ComponentActivity() {
    companion object {
        const val EXTRA_INITIAL_QUERY = "INITIAL_QUERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val baseTheme by settingsManager.baseTheme.collectAsState()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()

            SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
                val steamGridDBManager = remember { SteamGridDBManager(context) }
                val initialQuery = remember { intent?.getStringExtra(EXTRA_INITIAL_QUERY).orEmpty() }
                val viewModel: SteamGridDBViewModel = ViewModelProvider(
                    this,
                    SteamGridDBViewModelFactory(steamGridDBManager, context, initialQuery)
                )[SteamGridDBViewModel::class.java]

                SteamGridDBScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onIconSelected = { uri ->
                        val resultIntent = Intent().apply {
                            putExtra("ICON_URI", uri)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }
}
