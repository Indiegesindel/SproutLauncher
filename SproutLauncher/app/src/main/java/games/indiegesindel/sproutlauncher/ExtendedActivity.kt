package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ui.screens.AllAppsTab
import games.indiegesindel.sproutlauncher.ui.screens.HomeTab
import games.indiegesindel.sproutlauncher.ui.screens.SettingsTab
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState

class ExtendedActivity : ComponentActivity() {
    enum class Tab { Home, All, Settings }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val appTheme by settingsManager.theme.collectAsState()

            SproutLauncherTheme(appTheme = appTheme) {
                val configuration = LocalConfiguration.current
                val isTablet = configuration.smallestScreenWidthDp >= 600
                
                val appManager = remember { AppManager(context) }
                var selectedTiles by remember { mutableStateOf(appManager.getAppTiles()) }
                
                val startTab = remember {
                    intent.getStringExtra("EXTRA_TAB")?.let { tabName ->
                        try { Tab.valueOf(tabName) } catch (e: Exception) { Tab.Home }
                    } ?: Tab.Home
                }
                var currentTab by rememberSaveable { mutableStateOf(startTab) }
                var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }

                val installedApps = remember {
                    val intent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    packageManager.queryIntentActivities(intent, 0)
                        .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
                }

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
                                    selected = currentTab == Tab.Home,
                                    onClick = {
                                        currentTab = Tab.Home
                                        focusedItemId = null
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                NavigationDrawerItem(
                                    selected = currentTab == Tab.All,
                                    onClick = {
                                        currentTab = Tab.All
                                        focusedItemId = null
                                    },
                                    icon = { Icon(Icons.Default.Apps, contentDescription = "All") },
                                    label = { Text("All") },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                NavigationDrawerItem(
                                    selected = currentTab == Tab.Settings,
                                    onClick = {
                                        currentTab = Tab.Settings
                                        focusedItemId = null
                                    },
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
                                    selected = currentTab == Tab.Home,
                                    onClick = {
                                        currentTab = Tab.Home
                                        focusedItemId = null
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationRailItem(
                                    selected = currentTab == Tab.All,
                                    onClick = {
                                        currentTab = Tab.All
                                        focusedItemId = null
                                    },
                                    icon = { Icon(Icons.Default.Apps, contentDescription = "All") },
                                    label = { Text("All") }
                                )
                                NavigationRailItem(
                                    selected = currentTab == Tab.Settings,
                                    onClick = {
                                        currentTab = Tab.Settings
                                        focusedItemId = null
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }

                        when (currentTab) {
                            Tab.Home -> HomeTab(
                                installedApps = installedApps,
                                selectedTiles = selectedTiles,
                                appManager = appManager,
                                onTilesChanged = { selectedTiles = it },
                                focusedItemId = focusedItemId,
                                onFocusItemIdChanged = { focusedItemId = it }
                            )
                            Tab.All -> AllAppsTab(
                                installedApps = installedApps,
                                selectedTiles = selectedTiles,
                                appManager = appManager,
                                onTilesChanged = { selectedTiles = it },
                                focusedItemId = focusedItemId,
                                onFocusItemIdChanged = { focusedItemId = it }
                            )
                            Tab.Settings -> SettingsTab(settingsManager)
                        }
                    }
                }
            }
        }
    }
}
