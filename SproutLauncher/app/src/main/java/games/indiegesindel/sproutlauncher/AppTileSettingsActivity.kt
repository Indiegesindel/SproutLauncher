package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import games.indiegesindel.sproutlauncher.ui.screens.AppTileSettingsScreen
import games.indiegesindel.sproutlauncher.ui.setSproutContent
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel

class AppTileSettingsActivity : ComponentActivity() {
    private val tileId: String by lazy { intent.getStringExtra(EXTRA_TILE_ID).orEmpty() }
    private val viewModel: AppTileSettingsViewModel by viewModels { AppTileSettingsViewModel.factory(tileId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (tileId.isEmpty()) {
            finish()
            return
        }
        setSproutContent {
            AppTileSettingsScreen(viewModel = viewModel, onClose = ::finish)
        }
    }

    companion object {
        private const val EXTRA_TILE_ID = "TILE_ID"

        fun newIntent(context: Context, tileId: String) =
            Intent(context, AppTileSettingsActivity::class.java).putExtra(EXTRA_TILE_ID, tileId)
    }
}
