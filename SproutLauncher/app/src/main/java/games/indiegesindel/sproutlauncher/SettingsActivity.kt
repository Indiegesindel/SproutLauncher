package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import games.indiegesindel.sproutlauncher.ui.screens.SettingsScreen
import games.indiegesindel.sproutlauncher.ui.setSproutContent

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = appContainer
        setSproutContent {
            SettingsScreen(
                settings = container.settingsRepository,
                imageStore = container.imageStore,
                onBack = ::finish
            )
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
