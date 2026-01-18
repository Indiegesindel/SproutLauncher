package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.ExtendedActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel

@Composable
fun ExtendedScreen(
    viewModel: ExtendedViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600

    val currentTab by viewModel.currentTab.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val selectedTiles by viewModel.selectedTiles.collectAsState()
    val isLoadingTiles by viewModel.isLoadingTiles.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()

    val appManager = remember { AppManager(context) }
    val settingsManager = remember { SettingsManager(context) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTablet) {
                PermanentDrawerSheet(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(200.dp)
                        .displayCutoutPadding(),
                    drawerContainerColor = Color.Transparent,
                    drawerTonalElevation = 0.dp
                ) {
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        selected = currentTab == ExtendedActivity.Tab.Home,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        selected = currentTab == ExtendedActivity.Tab.All,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.All) },
                        icon = { Icon(Icons.Default.Apps, contentDescription = "All") },
                        label = { Text("All") },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        selected = currentTab == ExtendedActivity.Tab.Settings,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            } else {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .displayCutoutPadding(),
                    containerColor = Color.Transparent
                ) {
                    NavigationRailItem(
                        selected = currentTab == ExtendedActivity.Tab.Home,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationRailItem(
                        selected = currentTab == ExtendedActivity.Tab.All,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.All) },
                        icon = { Icon(Icons.Default.Apps, contentDescription = "All") },
                        label = { Text("All") }
                    )
                    NavigationRailItem(
                        selected = currentTab == ExtendedActivity.Tab.Settings,
                        onClick = { viewModel.setTab(ExtendedActivity.Tab.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }

            // Tab Content
            when (currentTab) {
                ExtendedActivity.Tab.Home -> {
                    HomeTab(
                        installedApps = installedApps,
                        selectedTiles = selectedTiles,
                        isLoading = isLoadingTiles || isLoadingApps,
                        appManager = appManager,
                        onTilesChanged = { viewModel.loadTiles() },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = { viewModel.setFocusedItemId(it) }
                    )
                }
                ExtendedActivity.Tab.All -> {
                    AllAppsTab(
                        installedApps = installedApps,
                        selectedTiles = selectedTiles,
                        isLoading = isLoadingApps,
                        appManager = appManager,
                        onTilesChanged = { viewModel.loadTiles() },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = { viewModel.setFocusedItemId(it) }
                    )
                }
                ExtendedActivity.Tab.Settings -> {
                    SettingsTab(settingsManager = settingsManager)
                }
            }
        }
    }
}
