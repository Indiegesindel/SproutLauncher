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
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel

@Composable
fun ExtendedScreen(
    viewModel: ExtendedViewModel,
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()

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
                selected = currentTab == ExtendedViewModel.Tab.ALL,
                onClick = { viewModel.setTab(ExtendedViewModel.Tab.ALL) },
                icon = { Icon(Icons.Default.Apps, contentDescription = "All Apps") },
                label = { Text("All") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
            NavigationRailItem(
                selected = currentTab == ExtendedViewModel.Tab.HOMESCREEN,
                onClick = { viewModel.setTab(ExtendedViewModel.Tab.HOMESCREEN) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Homescreen") },
                label = { Text("Homescreen") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            NavigationRailItem(
                selected = currentTab == ExtendedViewModel.Tab.SETTINGS,
                onClick = { viewModel.setTab(ExtendedViewModel.Tab.SETTINGS) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) viewModel.onFocusChanged(FocusedElement.NAVIGATION_ITEM)
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                ExtendedViewModel.Tab.ALL, ExtendedViewModel.Tab.HOMESCREEN -> {
                    AppListTab(viewModel = viewModel, onBack = onBack)
                }

                ExtendedViewModel.Tab.SETTINGS -> {
                    SettingsTab(
                        settingsManager = settingsManager,
                        onBack = onBack
                    )
                }
            }
        }
    }
}
