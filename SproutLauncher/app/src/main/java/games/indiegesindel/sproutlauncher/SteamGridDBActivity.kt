package games.indiegesindel.sproutlauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import games.indiegesindel.sproutlauncher.ui.screens.SteamGridDbScreen
import games.indiegesindel.sproutlauncher.ui.setSproutContent
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDbViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SteamGridDBActivity : ComponentActivity() {
    private val viewModel: SteamGridDbViewModel by viewModels {
        SteamGridDbViewModel.factory(intent.getStringExtra(EXTRA_INITIAL_QUERY).orEmpty())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSproutContent {
            SteamGridDbScreen(viewModel = viewModel, onBack = ::finish)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val uri = viewModel.uiState.map { it.pickedIconUri }.filterNotNull().first()
                setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_ICON_URI, uri))
                finish()
            }
        }
    }

    class PickIcon : ActivityResultContract<String, String?>() {
        override fun createIntent(context: Context, input: String) =
            Intent(context, SteamGridDBActivity::class.java).putExtra(EXTRA_INITIAL_QUERY, input)

        override fun parseResult(resultCode: Int, intent: Intent?): String? =
            intent?.getStringExtra(EXTRA_ICON_URI)?.takeIf { resultCode == Activity.RESULT_OK }
    }

    private companion object {
        const val EXTRA_INITIAL_QUERY = "INITIAL_QUERY"
        const val EXTRA_ICON_URI = "ICON_URI"
    }
}
