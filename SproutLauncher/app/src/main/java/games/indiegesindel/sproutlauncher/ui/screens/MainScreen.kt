package games.indiegesindel.sproutlauncher.ui.screens

import games.indiegesindel.sproutlauncher.ui.theme.accentColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import games.indiegesindel.sproutlauncher.data.AmbientSource
import games.indiegesindel.sproutlauncher.data.BackgroundMode
import games.indiegesindel.sproutlauncher.ui.theme.LocalSproutColors
import games.indiegesindel.sproutlauncher.ui.theme.plainBaseColor
import games.indiegesindel.sproutlauncher.ui.components.AmbientBackground
import games.indiegesindel.sproutlauncher.ui.components.LocalFocusPulse
import games.indiegesindel.sproutlauncher.ui.components.trackNavInput
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.utils.LaunchOrigin
import games.indiegesindel.sproutlauncher.utils.LocalControllerRumble
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.SettingsActivity
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.FocusedElement
import games.indiegesindel.sproutlauncher.ui.components.AddToGroupModal
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.GroupModal
import games.indiegesindel.sproutlauncher.ui.components.PROMPT_SPACING
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooterHeight
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.components.TileGrid
import games.indiegesindel.sproutlauncher.ui.components.TileGridActions
import games.indiegesindel.sproutlauncher.ui.components.TileMenuContext
import games.indiegesindel.sproutlauncher.ui.components.requestFocusWithRetry
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.Wallpaper
import games.indiegesindel.sproutlauncher.utils.AppLauncher
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@Composable
fun MainScreen(viewModel: MainViewModel, allAppsViewModel: AllAppsViewModel) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current

    val tiles by viewModel.appTiles.collectAsStateWithLifecycle()
    val homeLayout by viewModel.homeLayout.collectAsStateWithLifecycle()
    val groupLayout by viewModel.groupLayout.collectAsStateWithLifecycle()
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val quickApps by viewModel.quickApps.collectAsStateWithLifecycle()
    val pendingFocusTileId by viewModel.pendingFocusTileId.collectAsStateWithLifecycle()
    val tileToRemove by viewModel.tileToRemove.collectAsStateWithLifecycle()
    val isInMultiSelectMode by viewModel.isInMultiSelectMode.collectAsStateWithLifecycle()
    val selectedTileIds by viewModel.selectedTileIds.collectAsStateWithLifecycle()
    val openedGroup by viewModel.openedGroup.collectAsStateWithLifecycle()
    val tileToMoveToGroup by viewModel.tileToMoveToGroup.collectAsStateWithLifecycle()
    val showAllApps by viewModel.showAllApps.collectAsStateWithLifecycle()
    val backgroundMode by viewModel.backgroundMode.collectAsStateWithLifecycle()
    val ambientSource by viewModel.ambientSource.collectAsStateWithLifecycle()
    val plainBase by viewModel.plainBase.collectAsStateWithLifecycle()
    val customPlain by viewModel.customPlain.collectAsStateWithLifecycle()
    val ambientColor by viewModel.ambientColor.collectAsStateWithLifecycle()
    val customAmbient by viewModel.customAmbient.collectAsStateWithLifecycle()
    val wakeCount by viewModel.wakeCount.collectAsStateWithLifecycle()
    val view = LocalView.current
    val rumble = LocalControllerRumble.current

    var footerElement by remember { mutableStateOf(FocusedElement.NONE) }
    var focusedTileIsGroup by remember { mutableStateOf(false) }
    var focusedTile by remember { mutableStateOf<AppTile?>(null) }
    val onTileFocused: (AppTile) -> Unit = { tile ->
        footerElement = FocusedElement.APP_TILE
        focusedTileIsGroup = tile.isGroup
        focusedTile = tile
    }
    var groupOrigin by remember { mutableStateOf<Rect?>(null) }

    val isHomeInteractive = openedGroup == null && tileToMoveToGroup == null && !showAllApps

    val allAppsFocusRequester = remember { FocusRequester() }
    var sheetWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(showAllApps) {
        if (showAllApps) {
            soundManager?.play(UiSound.SHEET_OPEN)
            sheetWasOpen = true
        } else if (sheetWasOpen) {
            sheetWasOpen = false
            allAppsFocusRequester.requestFocusWithRetry()
        }
    }

    val density = LocalDensity.current
    var hasAppeared by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "homeAlpha"
    )
    val contentOffsetPx by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else with(density) { 40.dp.toPx() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "homeOffset"
    )
    LaunchedEffect(Unit) { hasAppeared = true }

    val wakeFade = remember { Animatable(0f) }
    val wakeScale = remember { Animatable(1f) }
    LaunchedEffect(wakeCount) {
        if (wakeCount == 0) return@LaunchedEffect
        soundManager?.playWake()
        wakeFade.snapTo(1f)
        wakeScale.snapTo(WAKE_SCALE)
        launch { wakeScale.animateTo(1f, spring(dampingRatio = 0.9f, stiffness = 500f)) }
        wakeFade.animateTo(0f, tween(WAKE_FADE_MS, easing = LinearEasing))
    }

    val homeBlur by animateDpAsState(
        targetValue = if (openedGroup != null) Motion.FolderBlur else 0.dp,
        animationSpec = tween(Motion.FOLDER_ZOOM_MS),
        label = "homeBlur"
    )

    tileToRemove?.let {
        RemoveTileConfirmationDialog(
            onConfirm = viewModel::confirmRemoveTile,
            onDismiss = viewModel::dismissRemoveConfirmation
        )
    }

    BackHandler {
        when {
            tileToMoveToGroup != null -> {
                soundManager?.play(UiSound.BACK)
                viewModel.dismissMoveToGroup()
            }
            openedGroup != null -> {
                soundManager?.play(UiSound.FOLDER_CLOSE)
                viewModel.closeGroup()
            }
        }
    }

    val newGroupLabel = stringResource(R.string.default_group_name)
    val openTileSettings: (AppTile) -> Unit = { context.startActivity(AppTileSettingsActivity.newIntent(context, it.id)) }

    val homeActions = remember(viewModel, newGroupLabel) {
        object : TileGridActions {
            override fun onLaunch(tile: AppTile, origin: Rect?) =
                AppLauncher.launch(context, tile, origin?.let { LaunchOrigin(view, it) })
            override fun onOpenGroup(group: AppTile, origin: Rect?) {
                groupOrigin = origin
                viewModel.openGroup(group)
            }
            override fun onEdit(tile: AppTile) = openTileSettings(tile)
            override fun onRemove(tile: AppTile) = viewModel.requestRemoveTile(tile)
            override fun onReorder(from: Int, to: Int) = viewModel.reorderTiles(from, to)
            override fun onBack() = viewModel.closeGroup()
            override fun onToggleSelection(tile: AppTile) = viewModel.toggleTileSelection(tile.id)
            override fun onCreateGroup(tile: AppTile) = viewModel.enterMultiSelectMode(tile.id)
            override fun onGroupSelected() = viewModel.groupSelectedTiles(newGroupLabel)
            override fun onRemoveSelected() = viewModel.removeSelectedTiles()
            override fun onClearSelection() = viewModel.clearSelection()
            override fun onAddToGroup(tile: AppTile) = viewModel.requestMoveToGroup(tile)
            override fun onUngroup(group: AppTile) = viewModel.ungroup(group.id)
        }
    }
    val groupActions = remember(viewModel) {
        object : TileGridActions {
            private val groupId get() = viewModel.openedGroup.value?.id
            override fun onLaunch(tile: AppTile, origin: Rect?) =
                AppLauncher.launch(context, tile, origin?.let { LaunchOrigin(view, it) })
            override fun onOpenGroup(group: AppTile, origin: Rect?) = Unit
            override fun onEdit(tile: AppTile) = openTileSettings(tile)
            override fun onRemove(tile: AppTile) = viewModel.requestRemoveTile(tile)
            override fun onReorder(from: Int, to: Int) {
                groupId?.let { viewModel.reorderGroupTiles(it, from, to) }
            }
            override fun onBack() = viewModel.closeGroup()
            override fun onRemoveFromGroup(tile: AppTile) {
                groupId?.let { viewModel.removeTileFromGroup(it, tile.id) }
            }
        }
    }

    val shownWallpaper = wallpaper.takeIf { backgroundMode == BackgroundMode.WALLPAPER }
    CompositionLocalProvider(LocalFocusPulse provides wakeCount) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .trackNavInput(soundManager, rumble),
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
            containerColor = when {
                shownWallpaper != null -> Color.Transparent
                backgroundMode == BackgroundMode.PLAIN ->
                    plainBaseColor(plainBase, customPlain, LocalSproutColors.current.isDark, MaterialTheme.colorScheme.background)
                else -> MaterialTheme.colorScheme.background
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    shownWallpaper != null -> WallpaperBackground(shownWallpaper)
                    backgroundMode == BackgroundMode.AMBIENT ->
                        AmbientBackground(
                            focusedTile = focusedTile.takeIf { ambientSource == AmbientSource.SELECTED_APP },
                            color = accentColor(ambientColor, customAmbient).takeIf { ambientSource == AmbientSource.CUSTOM }
                        )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(top = HOME_TOP_PADDING, bottom = PromptsFooterHeight)
                        .focusProperties { if (!isHomeInteractive) canFocus = false }
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetPx
                            scaleX = wakeScale.value
                            scaleY = wakeScale.value
                        }
                        .blur(homeBlur, BlurredEdgeTreatment.Unbounded),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TileGrid(
                        tiles = tiles,
                        layout = homeLayout,
                        actions = homeActions,
                        modifier = Modifier.weight(1f, fill = false),
                        menuContext = TileMenuContext(
                            isInGroup = false,
                            hasGroups = tiles.any { it.isGroup },
                            isInMultiSelectMode = isInMultiSelectMode
                        ),
                        selectedTileIds = selectedTileIds,
                        pendingFocusTileId = pendingFocusTileId.takeIf { openedGroup == null },
                        onFocusConsumed = viewModel::consumePendingFocus,
                        onTileFocused = onTileFocused,
                        enabled = isHomeInteractive
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.zIndex(1f)) {
                        QuickActionsBar(
                            quickApps = quickApps,
                            onAllApps = viewModel::openAllApps,
                            onBrowser = { AppLauncher.openBrowser(context) },
                            onQuickApp = { AppLauncher.launchPackage(context, it.packageName) },
                            onSystemSettings = { AppLauncher.openSystemSettings(context) },
                            onFocused = { footerElement = FocusedElement.QUICK_ACTION },
                            allAppsFocusRequester = allAppsFocusRequester,
                            enabled = isHomeInteractive
                        )
                    }
                }

                val layoutDirection = LocalLayoutDirection.current
                val overlayPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding() + PromptsFooterHeight
                )
                val visibleGroup = rememberLastNonNull(openedGroup)
                AnimatedVisibility(visible = openedGroup != null, enter = fadeIn(), exit = fadeOut()) {
                    visibleGroup?.let { group ->
                        GroupModal(
                            group = group,
                            origin = groupOrigin,
                            layout = groupLayout,
                            actions = groupActions,
                            pendingFocusTileId = pendingFocusTileId,
                            onFocusConsumed = viewModel::consumePendingFocus,
                            onTileFocused = onTileFocused,
                            contentPadding = overlayPadding,
                            enabled = openedGroup != null && tileToMoveToGroup == null
                        )
                    }
                }

                val visibleTileToMove = rememberLastNonNull(tileToMoveToGroup)
                AnimatedVisibility(visible = tileToMoveToGroup != null, enter = fadeIn(), exit = fadeOut()) {
                    visibleTileToMove?.let { tile ->
                        AddToGroupModal(
                            groups = tiles.filter { it.isGroup },
                            onGroupSelected = { groupId -> viewModel.moveTileToGroup(tile.id, groupId) },
                            onDismiss = viewModel::dismissMoveToGroup,
                            contentPadding = overlayPadding
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showAllApps,
                    enter = slideInVertically(spring(dampingRatio = 0.75f, stiffness = 380f)) { it } + fadeIn(tween(200)),
                    exit = slideOutVertically(tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(200))
                ) {
                    AllAppsBottomSheet(
                        viewModel = allAppsViewModel,
                        onDismiss = {
                            soundManager?.play(UiSound.SHEET_CLOSE)
                            viewModel.closeAllApps()
                        },
                        onSettingsClick = { context.startActivity(SettingsActivity.newIntent(context)) }
                    )
                }

                if (wakeFade.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(3f)
                            .graphicsLayer { alpha = wakeFade.value }
                            .background(Color.Black)
                    )
                }

                if (!showAllApps) PromptsFooter(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(2f)
                ) {
                    Crossfade(
                        targetState = FooterState(tileToMoveToGroup != null, footerElement, openedGroup != null, focusedTileIsGroup),
                        animationSpec = tween(120),
                        label = "footerPrompts"
                    ) { state ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PROMPT_SPACING)
                        ) {
                            HomePrompts(state)
                        }
                    }
                }
            }
        }
    }
}

private val HOME_TOP_PADDING = 40.dp
private const val WAKE_FADE_MS = 180
private const val WAKE_SCALE = 1.03f

private data class FooterState(
    val isMovingToGroup: Boolean,
    val focusedElement: FocusedElement,
    val isInGroup: Boolean,
    val focusedTileIsGroup: Boolean
)

@Composable
private fun HomePrompts(state: FooterState) {
    when {
        state.isMovingToGroup -> {
            ButtonPrompt(button = "A", label = stringResource(R.string.prompt_select))
            ButtonPrompt(button = "B", label = stringResource(R.string.prompt_close))
        }
        state.focusedElement == FocusedElement.APP_TILE -> {
            ButtonPrompt(button = "Y", label = stringResource(R.string.prompt_move))
            ButtonPrompt(button = "X", label = stringResource(R.string.prompt_options))
            ButtonPrompt(
                button = "A",
                label = stringResource(if (state.focusedTileIsGroup) R.string.prompt_open else R.string.prompt_launch)
            )
            if (state.isInGroup) ButtonPrompt(button = "B", label = stringResource(R.string.prompt_close))
        }
        state.focusedElement == FocusedElement.QUICK_ACTION ->
            ButtonPrompt(button = "A", label = stringResource(R.string.prompt_launch))
    }
}

@Composable
private fun WallpaperBackground(wallpaper: Wallpaper) {
    AsyncImage(
        model = wallpaper.uri,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = wallpaper.dim))
    )
}

@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { mutableStateOf(value) }
    if (value != null) last.value = value
    return last.value
}
