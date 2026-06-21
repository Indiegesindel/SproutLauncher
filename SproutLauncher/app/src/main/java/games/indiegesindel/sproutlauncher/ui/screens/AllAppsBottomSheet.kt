package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppGridItem
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AllAppsBottomSheet(
    viewModel: AllAppsViewModel,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    appManager: AppManager,
    onTilesChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val focusManager = LocalFocusManager.current
    val currentTab by viewModel.currentTab.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val selectedTiles by viewModel.selectedTiles.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val tileToRemove by viewModel.tileToRemove.collectAsState()

    // Footer-prompt state: APP_TILE on grid items, NONE on header chips / Settings.
    var focusedElement by remember { mutableStateOf(FocusedElement.NONE) }

    // Expand to near-full height past the first row, shrink back at the top.
    var isExpanded by remember { mutableStateOf(false) }
    val sheetFraction by animateFloatAsState(
        targetValue = if (isExpanded) 0.96f else 0.78f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "sheetExpand"
    )

    // When opened, focus moves to the "All Apps" chip. Retry a few frames to fix race condition
    val entryFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        var attempt = 0
        while (attempt < 10 && runCatching { entryFocusRequester.requestFocus() }.isFailure) {
            withFrameNanos {}
            attempt++
        }
    }

    BackHandler { onDismiss() }

    val filteredApps = remember(installedApps, selectedTiles, currentTab) {
        if (currentTab == AllAppsViewModel.Tab.HOMESCREEN) {
            installedApps.filter { app ->
                fun isAppOnHomeScreen(tiles: List<AppTile>): Boolean {
                    return tiles.any { tile ->
                        if (tile.isGroup) isAppOnHomeScreen(tile.groupTiles)
                        else tile.packageName == app.activityInfo.packageName &&
                                tile.activityName == app.activityInfo.name &&
                                tile.shortcutId == null
                    }
                }
                isAppOnHomeScreen(selectedTiles)
            }
        } else {
            installedApps
        }
    }

    val shortcuts = remember(selectedTiles, currentTab) {
        if (currentTab == AllAppsViewModel.Tab.HOMESCREEN) {
            fun getAllShortcuts(tiles: List<AppTile>): List<AppTile> {
                val result = mutableListOf<AppTile>()
                for (tile in tiles) {
                    if (tile.isGroup) result.addAll(getAllShortcuts(tile.groupTiles))
                    else if (tile.shortcutId != null) result.add(tile)
                }
                return result
            }
            getAllShortcuts(selectedTiles)
        } else {
            emptyList()
        }
    }

    val isHomeScreen = currentTab == AllAppsViewModel.Tab.HOMESCREEN

    if (tileToRemove != null) {
        RemoveTileConfirmationDialog(
            onConfirm = {
                viewModel.confirmRemoveTile()
                onTilesChanged()
            },
            onDismiss = { viewModel.dismissRemoveConfirmation() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK ||
                     event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_B)
                ) {
                    soundManager?.play(UiSound.BACK)
                    onDismiss()
                    true
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .focusable(false)
    ) {
        // Dimmed scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Sheet
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(sheetFraction)
                    // Redirect Up-exits from the top row to the header chips; cancel
                    // every other exit so the D-pad can't escape to the home screen behind the sheet.
                    .focusProperties {
                        exit = { direction ->
                            if (direction == FocusDirection.Up) entryFocusRequester
                            else FocusRequester.Cancel
                        }
                    },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentTab == AllAppsViewModel.Tab.ALL,
                                onClick = {
                                    soundManager?.play(UiSound.CONFIRM)
                                    viewModel.setTab(AllAppsViewModel.Tab.ALL)
                                },
                                label = { Text("All Apps") },
                                modifier = Modifier
                                    .focusRequester(entryFocusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            soundManager?.play(UiSound.MOVE)
                                            focusedElement = FocusedElement.NONE
                                        }
                                    }
                            )
                            FilterChip(
                                selected = currentTab == AllAppsViewModel.Tab.HOMESCREEN,
                                onClick = {
                                    soundManager?.play(UiSound.CONFIRM)
                                    viewModel.setTab(AllAppsViewModel.Tab.HOMESCREEN)
                                },
                                label = { Text("Homescreen") },
                                modifier = Modifier
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            soundManager?.play(UiSound.MOVE)
                                            focusedElement = FocusedElement.NONE
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    soundManager?.play(UiSound.CONFIRM)
                                    onSettingsClick()
                                },
                                modifier = Modifier
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            soundManager?.play(UiSound.MOVE)
                                            focusedElement = FocusedElement.NONE
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider()

                        // App grid
                        Box(modifier = Modifier.weight(1f)) {
                            AppGrid(
                                filteredApps = filteredApps,
                                shortcuts = shortcuts,
                                selectedTiles = selectedTiles,
                                isLoading = isLoadingApps,
                                isHomeScreen = isHomeScreen,
                                appManager = appManager,
                                onTilesChanged = {
                                    viewModel.loadTiles()
                                    onTilesChanged()
                                },
                                onRequestRemove = { viewModel.requestRemoveTile(it) },
                                hasGroups = selectedTiles.any { it.isGroup },
                                onDismiss = onDismiss,
                                enabled = tileToRemove == null,
                                onExpand = { isExpanded = true },
                                onCollapse = { isExpanded = false },
                                onAppTileFocused = { focusedElement = FocusedElement.APP_TILE },
                                onLabelFocused = { focusedElement = FocusedElement.NONE }
                            )
                        }
                    }

                    PromptsFooter(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(2f)
                    ) {
                        when (focusedElement) {
                            FocusedElement.APP_TILE -> {
                                ButtonPrompt(button = "A", label = "Launch")
                                ButtonPrompt(button = "X", label = "Options")
                                ButtonPrompt(button = "B", label = "Close")
                            }
                            else -> {
                                ButtonPrompt(button = "B", label = "Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(
    filteredApps: List<android.content.pm.ResolveInfo>,
    shortcuts: List<AppTile>,
    selectedTiles: List<AppTile>,
    isLoading: Boolean,
    isHomeScreen: Boolean,
    appManager: AppManager,
    onTilesChanged: (List<AppTile>) -> Unit,
    onRequestRemove: (AppTile) -> Unit,
    hasGroups: Boolean,
    onDismiss: () -> Unit,
    enabled: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onAppTileFocused: () -> Unit,
    onLabelFocused: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val gridState = rememberLazyGridState()
    val showAppsHeader = isHomeScreen && filteredApps.isNotEmpty()

    // Expand once scrolled past the first row, collapse back when returned to the top.
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index > 0) onExpand()
                else if (index == 0 && offset == 0) onCollapse()
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("allapps_loading")
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showAppsHeader) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        items(
            filteredApps,
            key = { app -> "${app.activityInfo.packageName}_${app.activityInfo.name}" }
        ) { app ->
            val packageName = app.activityInfo.packageName
            val activityName = app.activityInfo.name
            val itemId = "${packageName}_${activityName}"
            val tile = remember(selectedTiles, packageName, activityName) {
                fun findTile(tiles: List<AppTile>): AppTile? {
                    for (tile in tiles) {
                        if (tile.isGroup) findTile(tile.groupTiles)?.let { return it }
                        else if (tile.packageName == packageName &&
                            tile.activityName == activityName &&
                            tile.shortcutId == null) return tile
                    }
                    return null
                }
                findTile(selectedTiles)
            }
            val isSelected = tile != null

            AppGridItem(
                app = app,
                isOnHomeScreen = isSelected,
                tile = tile,
                onToggleHomeScreen = {
                    if (!isSelected) {
                        val newTile = AppTile(
                            packageName = packageName,
                            activityName = activityName,
                            label = app.loadLabel(pm).toString()
                        )
                        appManager.addAppTile(newTile)
                        onTilesChanged(appManager.getAppTiles())
                    } else {
                        tile?.let { onRequestRemove(it) }
                    }
                },
                onEdit = {
                    tile?.let {
                        val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                            putExtra("TILE_ID", it.id)
                        }
                        context.startActivity(intent)
                    }
                },
                id = itemId,
                onFocused = { onAppTileFocused() },
                isInMultiSelectMode = false,
                isSelected = false,
                onDismiss = onDismiss,
                showGroupOptions = false,
                hasGroups = hasGroups,
                enabled = enabled
            )
        }

        if (isHomeScreen && shortcuts.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ShortcutsHintBlock(enabled = enabled, onFocused = onLabelFocused)
            }
        } else if (shortcuts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Shortcuts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(shortcuts, key = { tile -> "shortcut_${tile.id}" }) { tile ->
                val itemId = "shortcut_${tile.id}"
                AppGridItem(
                    app = null,
                    isOnHomeScreen = true,
                    tile = tile,
                    onToggleHomeScreen = { onRequestRemove(tile) },
                    onEdit = {
                        val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                            putExtra("TILE_ID", tile.id)
                        }
                        context.startActivity(intent)
                    },
                    id = itemId,
                    onFocused = { onAppTileFocused() },
                    isInMultiSelectMode = false,
                    isSelected = false,
                    onDismiss = onDismiss,
                    showGroupOptions = false,
                    hasGroups = hasGroups,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun ShortcutsHintBlock(enabled: Boolean, onFocused: () -> Unit) {
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    var isFocused by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .onGloballyPositioned { size = it.size }
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    soundManager?.play(UiSound.MOVE)
                    onFocused()
                    scope.launch {
                        runCatching {
                            if (size == IntSize.Zero) {
                                bringIntoViewRequester.bringIntoView()
                            } else {
                                val extraBottom = with(density) { 96.dp.toPx() }
                                bringIntoViewRequester.bringIntoView(
                                    Rect(
                                        left = 0f,
                                        top = 0f,
                                        right = size.width.toFloat(),
                                        bottom = size.height.toFloat() + extraBottom
                                    )
                                )
                            }
                        }
                    }
                }
            }
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isFocused) Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                else Modifier
            )
            .focusable(enabled = enabled)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Shortcuts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "You can add shortcuts to the homescreen from supported apps by using their 'Add to home screen' feature.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
