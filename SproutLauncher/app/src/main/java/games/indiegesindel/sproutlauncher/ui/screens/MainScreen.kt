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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.SettingsActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.ui.components.AppGrid
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.GroupModal
import games.indiegesindel.sproutlauncher.ui.components.AddToGroupModal
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel
import games.indiegesindel.sproutlauncher.utils.LauncherUtils
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    allAppsViewModel: AllAppsViewModel
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val appManager = remember { AppManager(context) }
    val appTiles by viewModel.appTiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingFocus by viewModel.pendingFocus.collectAsState()
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
    val showAllApps by viewModel.showAllApps.collectAsState()

    var footerElement by remember { mutableStateOf(FocusedElement.NONE) }
    var focusedTileIsGroup by remember { mutableStateOf(false) }
    val allAppsFocusRequester = remember { FocusRequester() }

    // Restore focus to the All Apps button when the sheet closes, so it isn't stranded behind the overlay.
    var sheetWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(showAllApps) {
        if (showAllApps) {
            sheetWasOpen = true
        } else if (sheetWasOpen) {
            sheetWasOpen = false
            // Retry a few frames until the quick-actions bar is focusable again.
            var attempt = 0
            while (attempt < 10 && runCatching { allAppsFocusRequester.requestFocus() }.isFailure) {
                withFrameNanos {}
                attempt++
            }
        }
    }

    fun openAllApps() {
        allAppsViewModel.resetFocus()
        viewModel.openAllApps()
    }

    fun launchApp(packageName: String) {
        LauncherUtils.launchApp(context, packageName)
    }

    val density = LocalDensity.current
    var screenAppeared by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "screenAlpha"
    )
    val screenOffsetPx by animateFloatAsState(
        targetValue = with(density) { if (screenAppeared) 0f else 40.dp.toPx() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "screenSlide"
    )
    LaunchedEffect(Unit) { screenAppeared = true }

    if (tileToRemove != null) {
        RemoveTileConfirmationDialog(
            onConfirm = { viewModel.confirmRemoveTile() },
            onDismiss = { viewModel.dismissRemoveConfirmation() }
        )
    }

    if (openedGroup != null) {
        BackHandler {
            soundManager?.play(UiSound.BACK)
            viewModel.closeGroup()
        }
    }

    if (tileToMoveToGroup != null) {
        BackHandler {
            soundManager?.play(UiSound.BACK)
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
                        .padding(bottom = 44.dp) // Space for footer
                        .focusProperties {
                            if (openedGroup != null || tileToMoveToGroup != null || showAllApps) canFocus = false
                        }
                        .graphicsLayer {
                            alpha = screenAlpha
                            translationY = screenOffsetPx
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
                        pendingFocus = pendingFocus,
                        onFocusConsumed = { viewModel.consumePendingFocus() },
                        onTileFocused = { isGroup ->
                            footerElement = FocusedElement.APP_TILE
                            focusedTileIsGroup = isGroup
                        },
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
                        // Non-focusable while an overlay is open so the D-pad can't reach the covered home screen.
                        enabled = openedGroup == null && tileToMoveToGroup == null && !showAllApps
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.zIndex(1f)) {
                        QuickActionsBar(
                            onAllAppsClick = { openAllApps() },
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
                            onQuickActionFocused = { footerElement = FocusedElement.QUICK_ACTION },
                            allAppsFocusRequester = allAppsFocusRequester,
                            enabled = openedGroup == null && tileToMoveToGroup == null && !showAllApps
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
                        onRemove = { tile -> viewModel.requestRemoveTile(tile) },
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
                        pendingFocus = pendingFocus,
                        onFocusConsumed = { viewModel.consumePendingFocus() },
                        onTileFocused = { isGroup ->
                            footerElement = FocusedElement.APP_TILE
                            focusedTileIsGroup = isGroup
                        },
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

            // All Apps bottom sheet overlay
            AnimatedVisibility(
                visible = showAllApps,
                enter = slideInVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(200))
            ) {
                AllAppsBottomSheet(
                    viewModel = allAppsViewModel,
                    onDismiss = {
                        soundManager?.play(UiSound.BACK)
                        viewModel.closeAllApps()
                    },
                    onSettingsClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    appManager = appManager,
                    onTilesChanged = {
                        viewModel.loadAppTiles()
                    }
                )
            }

            // Permanent black bar footer (on top of everything)
            PromptsFooter(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .focusProperties {
                        if (openedGroup != null || tileToMoveToGroup != null || showAllApps) canFocus = false
                    }
            ) {
                Crossfade(
                    targetState = Triple(tileToMoveToGroup != null, footerElement, openedGroup != null),
                    animationSpec = tween(120),
                    label = "footerCrossfade"
                ) { (inMoveMode, element, inGroup) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (inMoveMode) {
                            ButtonPrompt(button = "A", label = "Select")
                            ButtonPrompt(button = "B", label = "Close")
                        } else when (element) {
                            FocusedElement.APP_TILE -> {
                                ButtonPrompt(button = "Y", label = "Move (Hold)")
                                ButtonPrompt(button = "X", label = "Options")
                                ButtonPrompt(button = "A", label = if (focusedTileIsGroup) "Open" else "Launch")
                                if (inGroup) ButtonPrompt(button = "B", label = "Close")
                            }
                            FocusedElement.QUICK_ACTION -> ButtonPrompt(button = "A", label = "Launch")
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
