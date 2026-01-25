package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel

@Composable
fun AppListTab(
    viewModel: ExtendedViewModel
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val focusedElement by viewModel.focusedElement.collectAsState()
    val selectedTiles by viewModel.selectedTiles.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val tileToRemove by viewModel.tileToRemove.collectAsState()

    val appManager = remember { AppManager(context) }

    val filteredApps = remember(installedApps, selectedTiles, currentTab) {
        if (currentTab == ExtendedViewModel.Tab.HOMESCREEN) {
            installedApps.filter { app ->
                selectedTiles.any { 
                    it.packageName == app.activityInfo.packageName && 
                    it.activityName == app.activityInfo.name &&
                    it.shortcutId == null
                }
            }
        } else {
            installedApps
        }
    }

    val shortcuts = remember(selectedTiles, currentTab) {
        if (currentTab == ExtendedViewModel.Tab.HOMESCREEN) {
            selectedTiles.filter { it.shortcutId != null }
        } else {
            emptyList()
        }
    }

    if (tileToRemove != null) {
        RemoveTileConfirmationDialog(
            onConfirm = { viewModel.confirmRemoveTile() },
            onDismiss = { viewModel.dismissRemoveConfirmation() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Grid Content
                Box(modifier = Modifier.weight(1f)) {
                    AllAppsTab(
                        installedApps = filteredApps,
                        selectedTiles = selectedTiles,
                        shortcuts = shortcuts,
                        isLoading = isLoadingApps,
                        appManager = appManager,
                        onTilesChanged = { viewModel.loadTiles() },
                        onRequestRemove = { viewModel.requestRemoveTile(it) },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = {
                            viewModel.setFocusedItemId(it)
                            if (it != null) viewModel.onFocusChanged(FocusedElement.APP_TILE)
                        }
                    )
                }

                // Input Prompts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, bottom = 12.dp, top = 0.dp)
                        .height(32.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (focusedElement) {
                            FocusedElement.NAVIGATION_ITEM -> {
                                ButtonPrompt(button = "A", label = "Select")
                            }

                            FocusedElement.APP_TILE -> {
                                ButtonPrompt(button = "A", label = "Launch")
                                ButtonPrompt(button = "X", label = "Options")
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
