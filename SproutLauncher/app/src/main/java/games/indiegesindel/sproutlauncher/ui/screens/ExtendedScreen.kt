package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.SettingsActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel

@Composable
fun ExtendedScreen(
    viewModel: ExtendedViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentFilter by viewModel.currentFilter.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val focusedElement by viewModel.focusedElement.collectAsState()
    val selectedTiles by viewModel.selectedTiles.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val tileToRemove by viewModel.tileToRemove.collectAsState()

    val appManager = remember { AppManager(context) }

    val filteredApps = remember(installedApps, selectedTiles, currentFilter) {
        if (currentFilter == ExtendedViewModel.Filter.HOMESCREEN) {
            installedApps.filter { app ->
                selectedTiles.any { it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name }
            }
        } else {
            installedApps
        }
    }

    if (tileToRemove != null) {
        RemoveTileConfirmationDialog(
            onConfirm = { viewModel.confirmRemoveTile() },
            onDismiss = { viewModel.dismissRemoveConfirmation() }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            header = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home"
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            NavigationRailItem(
                selected = currentFilter == ExtendedViewModel.Filter.ALL,
                onClick = { viewModel.setFilter(ExtendedViewModel.Filter.ALL) },
                icon = { Icon(Icons.Default.Apps, contentDescription = "All Apps") },
                label = { Text("All") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
            NavigationRailItem(
                selected = currentFilter == ExtendedViewModel.Filter.HOMESCREEN,
                onClick = { viewModel.setFilter(ExtendedViewModel.Filter.HOMESCREEN) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Homescreen") },
                label = { Text("Homescreen") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            NavigationRailItem(
                selected = false,
                onClick = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
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
}
