package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import games.indiegesindel.sproutlauncher.ui.screens.MainScreen
import games.indiegesindel.sproutlauncher.ui.setSproutContent
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val allAppsViewModel: AllAppsViewModel by viewModels { AllAppsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSproutContent {
            MainScreen(viewModel = viewModel, allAppsViewModel = allAppsViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        appContainer.installedAppsRepository.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.resetState()
        }
    }
}
