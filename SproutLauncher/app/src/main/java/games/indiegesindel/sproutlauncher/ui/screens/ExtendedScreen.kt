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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Apps", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        FilterChip(
                            selected = currentFilter == ExtendedViewModel.Filter.ALL,
                            onClick = { viewModel.setFilter(ExtendedViewModel.Filter.ALL) },
                            label = { Text("All") },
                            modifier = Modifier.onFocusChanged {
                                if (it.isFocused) viewModel.onFocusChanged(FocusedElement.FILTER)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = currentFilter == ExtendedViewModel.Filter.ALL,
                                borderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = currentFilter == ExtendedViewModel.Filter.HOMESCREEN,
                            onClick = { viewModel.setFilter(ExtendedViewModel.Filter.HOMESCREEN) },
                            label = { Text("Homescreen") },
                            modifier = Modifier.onFocusChanged {
                                if (it.isFocused) viewModel.onFocusChanged(FocusedElement.FILTER)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = currentFilter == ExtendedViewModel.Filter.HOMESCREEN,
                                borderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
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
                                contentDescription = "Settings"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
