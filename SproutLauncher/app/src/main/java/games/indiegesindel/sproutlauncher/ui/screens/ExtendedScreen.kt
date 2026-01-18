package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.SettingsActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.viewmodels.ExtendedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendedScreen(
    viewModel: ExtendedViewModel
) {
    val context = LocalContext.current
    val currentFilter by viewModel.currentFilter.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val focusedElement by viewModel.focusedElement.collectAsState()
    val selectedTiles by viewModel.selectedTiles.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Select
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .width(250.dp)
                        .onFocusChanged { if (it.isFocused) viewModel.onFocusChanged(FocusedElement.FILTER) }
                ) {
                    OutlinedTextField(
                        value = when (currentFilter) {
                            ExtendedViewModel.Filter.ALL -> "All apps"
                            ExtendedViewModel.Filter.HOMESCREEN -> "Apps on Homescreen"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All apps") },
                            onClick = {
                                viewModel.setFilter(ExtendedViewModel.Filter.ALL)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Apps on Homescreen") },
                            onClick = {
                                viewModel.setFilter(ExtendedViewModel.Filter.HOMESCREEN)
                                expanded = false
                            }
                        )
                    }
                }

                // Settings Button
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused) viewModel.onFocusChanged(FocusedElement.SETTINGS_BUTTON)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Grid Content
            Box(modifier = Modifier.weight(1f)) {
                AllAppsTab(
                    installedApps = filteredApps,
                    selectedTiles = selectedTiles,
                    isLoading = isLoadingApps,
                    appManager = appManager,
                    onTilesChanged = { viewModel.loadTiles() },
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
                        FocusedElement.FILTER -> {
                            ButtonPrompt(button = "A", label = "Filter")
                        }
                        FocusedElement.SETTINGS_BUTTON -> {
                            ButtonPrompt(button = "A", label = "Launch")
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
