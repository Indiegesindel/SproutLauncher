package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.ExtendedActivity
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.ui.components.AppGrid
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.GroupModal
import games.indiegesindel.sproutlauncher.ui.components.AddToGroupModal
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel
import games.indiegesindel.sproutlauncher.utils.LauncherUtils

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val appTiles by viewModel.appTiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val focusedElement by viewModel.focusedElement.collectAsState()
    val homeScreenRows by viewModel.homeScreenRows.collectAsState()
    val horizontalSpacing by viewModel.horizontalSpacing.collectAsState()
    val verticalSpacing by viewModel.verticalSpacing.collectAsState()
    val appTileRoundness by viewModel.appTileRoundness.collectAsState()
    val groupRows by viewModel.groupRows.collectAsState()
    val groupHorizontalSpacing by viewModel.groupHorizontalSpacing.collectAsState()
    val groupVerticalSpacing by viewModel.groupVerticalSpacing.collectAsState()
    val groupAppTileRoundness by viewModel.groupAppTileRoundness.collectAsState()
    val installedQuickActions by viewModel.installedQuickActions.collectAsState()
    val wallpaperUri by viewModel.wallpaperUri.collectAsState()
    val wallpaperDim by viewModel.wallpaperDim.collectAsState()
    val showYouTube by viewModel.showYouTube.collectAsState()
    val showDiscord by viewModel.showDiscord.collectAsState()
    val showSpotify by viewModel.showSpotify.collectAsState()
    val tileToRemove by viewModel.tileToRemove.collectAsState()
    val isInMultiSelectMode by viewModel.isInMultiSelectMode.collectAsState()
    val selectedTileIds by viewModel.selectedTileIds.collectAsState()
    val openedGroup by viewModel.openedGroup.collectAsState()
    val tileToMoveToGroup by viewModel.tileToMoveToGroup.collectAsState()

    fun launchApp(packageName: String) {
        LauncherUtils.launchApp(context, packageName)
    }

    if (tileToRemove != null) {
        RemoveTileConfirmationDialog(
            onConfirm = { viewModel.confirmRemoveTile() },
            onDismiss = { viewModel.dismissRemoveConfirmation() }
        )
    }

    if (openedGroup != null) {
        BackHandler {
            viewModel.closeGroup()
        }
    }

    if (tileToMoveToGroup != null) {
        BackHandler {
            viewModel.dismissMoveToGroup()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        containerColor = if (wallpaperUri != null) Color.Transparent else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (wallpaperUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(wallpaperUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = wallpaperDim))
                )
            }
            // Content and Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 56.dp) // Space for footer
                        .focusProperties {
                            if (openedGroup != null || tileToMoveToGroup != null) canFocus = false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppGrid(
                        appTiles = appTiles,
                        isLoading = isLoading,
                        rows = homeScreenRows,
                        horizontalSpacing = horizontalSpacing,
                        verticalSpacing = verticalSpacing,
                        onAppClick = { tile ->
                            LauncherUtils.launchTile(context, tile)
                        },
                        onRemove = { tile ->
                            viewModel.requestRemoveTile(tile)
                        },
                        onSettings = { tile ->
                            val intent =
                                Intent(context, AppTileSettingsActivity::class.java).apply {
                                    putExtra("TILE_ID", tile.id)
                                }
                            context.startActivity(intent)
                        },
                        onReorder = { from, to ->
                            viewModel.reorderAppTiles(from, to)
                        },
                        onDragEnd = {
                            viewModel.saveAppTiles()
                        },
                        onFocusChanged = { focused ->
                            viewModel.onFocusChanged(focused)
                        },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = { viewModel.onFocusedItemIdChanged(it) },
                        roundness = appTileRoundness,
                        isInMultiSelectMode = isInMultiSelectMode,
                        selectedTileIds = selectedTileIds,
                        onToggleSelection = { viewModel.toggleTileSelection(it) },
                        onCreateGroup = { viewModel.enterMultiSelectMode(it) },
                        onGroupSelected = { viewModel.groupSelectedTiles() },
                        onRemoveSelected = { viewModel.removeSelectedTiles() },
                        onClearSelection = { viewModel.clearSelection() },
                        onOpenGroup = { viewModel.openGroup(it) },
                        onAddToGroup = { viewModel.requestMoveToGroup(it) },
                        onUngroup = { viewModel.ungroup(it) },
                        onDismiss = { viewModel.closeGroup() },
                        hasGroups = appTiles.any { it.isGroup },
                        enabled = openedGroup == null && tileToMoveToGroup == null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.zIndex(1f)) {
                        QuickActionsBar(
                            onAllAppsClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        ExtendedActivity::class.java
                                    )
                                )
                            },
                            onBrowserClick = {
                                try {
                                    val intent = Intent.makeMainSelectorActivity(
                                        Intent.ACTION_MAIN,
                                        Intent.CATEGORY_APP_BROWSER
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Could not open browser",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onYouTubeClick = if (showYouTube && installedQuickActions["com.google.android.youtube"] == true) {
                                { launchApp("com.google.android.youtube") }
                            } else null,
                            onPlayStoreClick = if (installedQuickActions["com.android.vending"] == true) {
                                { launchApp("com.android.vending") }
                            } else null,
                            onDiscordClick = if (showDiscord && installedQuickActions["com.discord"] == true) {
                                { launchApp("com.discord") }
                            } else null,
                            onSpotifyClick = if (showSpotify && installedQuickActions["com.spotify.music"] == true) {
                                { launchApp("com.spotify.music") }
                            } else null,
                            onPhotosClick = if (installedQuickActions["com.google.android.apps.photos"] == true) {
                                { launchApp("com.google.android.apps.photos") }
                            } else null,
                            onSettingsClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Could not open settings",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onFocusChanged = { viewModel.onQuickActionsFocusChanged(it) },
                            focusedItemId = focusedItemId,
                            onFocusItemIdChanged = { viewModel.onFocusedItemIdChanged(it) },
                            enabled = openedGroup == null && tileToMoveToGroup == null
                        )
                    }
                }
            }

            // Overlay (outside innerPadding to fill screen)
            AnimatedVisibility(
                visible = openedGroup != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                openedGroup?.let { group ->
                    GroupModal(
                        group = group,
                        onDismiss = { viewModel.closeGroup() },
                        onAppClick = { tile -> LauncherUtils.launchTile(context, tile) },
                        onRemoveFromGroup = { tileId ->
                            viewModel.removeTileFromGroup(
                                group.id,
                                tileId
                            )
                        },
                        onSettings = { tile ->
                            val intent =
                                Intent(context, AppTileSettingsActivity::class.java).apply {
                                    putExtra("TILE_ID", tile.id)
                                }
                            context.startActivity(intent)
                        },
                        onReorder = { from, to ->
                            viewModel.reorderGroupTiles(group.id, from, to)
                        },
                        onDragEnd = {
                            viewModel.saveAppTiles()
                        },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = { viewModel.onFocusedItemIdChanged(it) },
                        innerPadding = innerPadding,
                        rows = groupRows,
                        horizontalSpacing = groupHorizontalSpacing,
                        verticalSpacing = groupVerticalSpacing,
                        roundness = groupAppTileRoundness,
                        enabled = tileToMoveToGroup == null
                    )
                }
            }

            AnimatedVisibility(
                visible = tileToMoveToGroup != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                tileToMoveToGroup?.let { tile ->
                    AddToGroupModal(
                        groups = appTiles.filter { it.isGroup },
                        onGroupSelected = { groupId ->
                            viewModel.moveTileToGroup(tile.id, groupId)
                        },
                        onDismiss = { viewModel.dismissMoveToGroup() },
                        innerPadding = innerPadding,
                        roundness = appTileRoundness
                    )
                }
            }

            // Permanent black bar footer (on top of everything)
            PromptsFooter(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .focusProperties {
                        if (openedGroup != null || tileToMoveToGroup != null) canFocus = false
                    }
            ) {
                if (tileToMoveToGroup != null) {
                    ButtonPrompt(button = "A", label = "Select")
                    ButtonPrompt(button = "B", label = "Close")
                } else {
                    when (focusedElement) {
                        FocusedElement.APP_TILE -> {
                            val focusedTile = appTiles.find { "tile:${it.id}" == focusedItemId }
                                ?: openedGroup?.groupTiles?.find { "tile:${it.id}" == focusedItemId }

                            ButtonPrompt(button = "Y", label = "Move (Hold)")
                            ButtonPrompt(button = "X", label = "Options")
                            val label = if (focusedTile?.isGroup == true) "Open" else "Launch"
                            ButtonPrompt(button = "A", label = label)
                            if (openedGroup != null) {
                                ButtonPrompt(button = "B", label = "Close")
                            }
                        }

                        FocusedElement.QUICK_ACTION -> {
                            ButtonPrompt(button = "A", label = "Launch")
                        }

                        else -> {
                            // Show nothing or default
                        }
                    }
                }
            }
        }
    }
}

