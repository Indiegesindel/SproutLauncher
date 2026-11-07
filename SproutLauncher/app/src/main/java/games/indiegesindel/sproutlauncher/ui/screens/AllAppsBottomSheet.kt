package games.indiegesindel.sproutlauncher.ui.screens

import games.indiegesindel.sproutlauncher.ui.components.RefocusOnKeyInput
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import games.indiegesindel.sproutlauncher.ui.components.focusRing
import kotlin.math.roundToInt
import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp
import games.indiegesindel.sproutlauncher.ui.FocusedElement
import games.indiegesindel.sproutlauncher.ui.components.AllAppsItem
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.MenuAction
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.components.requestFocusWithRetry
import games.indiegesindel.sproutlauncher.ui.icons.AppIcon
import games.indiegesindel.sproutlauncher.ui.icons.iconModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AllAppsViewModel.AppEntry
import games.indiegesindel.sproutlauncher.utils.AppLauncher
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.LaunchOrigin
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.launch

private const val COLLAPSED_FRACTION = 0.78f
private const val EXPANDED_FRACTION = 0.96f

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllAppsBottomSheet(
    viewModel: AllAppsViewModel,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tileToRemove by viewModel.tileToRemove.collectAsStateWithLifecycle()

    var focusedElement by remember { mutableStateOf(FocusedElement.NONE) }
    var isExpanded by remember { mutableStateOf(false) }
    val sheetFraction by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_FRACTION else COLLAPSED_FRACTION,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "sheetFraction"
    )

    val entryFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        entryFocusRequester.requestFocusWithRetry()
    }

    var sheetHasFocus by remember { mutableStateOf(false) }
    RefocusOnKeyInput(sheetHasFocus) { entryFocusRequester }

    BackHandler(onBack = onDismiss)

    tileToRemove?.let {
        RemoveTileConfirmationDialog(
            onConfirm = viewModel::confirmRemoveTile,
            onDismiss = viewModel::dismissRemoveConfirmation
        )
    }

    val onHeaderFocused = { focusedElement = FocusedElement.NONE }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.key != Key.Back && event.key != Key.ButtonB) return@onPreviewKeyEvent false
                if (event.type == KeyEventType.KeyDown) onDismiss()
                true
            }
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(sheetFraction)
                .onFocusChanged { sheetHasFocus = it.hasFocus }
                .focusProperties {
                    onExit = {
                        if (requestedFocusDirection == FocusDirection.Up) entryFocusRequester.requestFocus()
                        cancelFocusChange()
                    }
                },
            shape = RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                            .size(width = 56.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    SheetHeader(
                        tab = uiState.tab,
                        onTabSelected = viewModel::setTab,
                        onSettingsClick = onSettingsClick,
                        onFocused = onHeaderFocused,
                        entryFocusRequester = entryFocusRequester
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        AppsGrid(
                            uiState = uiState,
                            onAddToHomeScreen = viewModel::addToHomeScreen,
                            onRequestRemove = viewModel::requestRemoveTile,
                            onDismiss = onDismiss,
                            enabled = tileToRemove == null,
                            onExpandedChange = { isExpanded = it },
                            onItemFocused = { focusedElement = FocusedElement.APP_TILE },
                            onHintFocused = onHeaderFocused
                        )
                    }
                    PromptsFooter {
                        if (focusedElement == FocusedElement.APP_TILE) {
                            ButtonPrompt(button = "A", label = stringResource(R.string.prompt_launch))
                            ButtonPrompt(button = "X", label = stringResource(R.string.prompt_options))
                        } else {
                            ButtonPrompt(button = "A", label = stringResource(R.string.prompt_select))
                        }
                        ButtonPrompt(button = "B", label = stringResource(R.string.prompt_close))
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    tab: AllAppsViewModel.Tab,
    onTabSelected: (AllAppsViewModel.Tab) -> Unit,
    onSettingsClick: () -> Unit,
    onFocused: () -> Unit,
    entryFocusRequester: FocusRequester
) {
    val soundManager = LocalSoundManager.current
    val focusFeedback = Modifier.onFocusChanged {
        if (it.isFocused) {
            soundManager?.play(UiSound.MOVE)
            onFocused()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabTrack(tab, onTabSelected, focusFeedback, entryFocusRequester)
        Spacer(modifier = Modifier.weight(1f))
        val settingsInteraction = remember { MutableInteractionSource() }
        val settingsFocused by settingsInteraction.collectIsFocusedAsState()
        IconButton(
            onClick = {
                soundManager?.play(UiSound.CONFIRM)
                onSettingsClick()
            },
            interactionSource = settingsInteraction,
            modifier = focusFeedback.focusRing(focused = settingsFocused, cornerRadius = null)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppsGrid(
    uiState: AllAppsViewModel.UiState,
    onAddToHomeScreen: (InstalledApp) -> Unit,
    onRequestRemove: (AppTile) -> Unit,
    onDismiss: () -> Unit,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemFocused: () -> Unit,
    onHintFocused: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val isHomeTab = uiState.tab == AllAppsViewModel.Tab.HOMESCREEN
    val popped = remember(uiState.tab) { HashSet<String>() }
    fun popDelay(key: String, index: Int): Long? =
        if (index < POP_ITEMS && popped.add(key)) POP_BASE_DELAY_MS + index * POP_STEP_MS else null

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index > 0) onExpandedChange(true) else if (offset == 0) onExpandedChange(false)
            }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isHomeTab && uiState.apps.isNotEmpty()) {
            sectionHeader(R.string.all_apps_section_apps, topPadding = 8.dp)
        }

        itemsIndexed(uiState.apps, key = { _, it -> "${it.app.packageName}/${it.app.activityName}" }) { index, entry ->
            AllAppsItem(
                popDelayMs = popDelay("${entry.app.packageName}/${entry.app.activityName}", index),
                label = entry.tile?.label ?: entry.app.label,
                iconModel = entry.tile?.iconUri ?: AppIcon.Activity(entry.app.packageName, entry.app.activityName),
                isOnHomeScreen = entry.tile != null,
                onLaunch = { origin -> AppLauncher.launch(context, entry.app, origin?.let { LaunchOrigin(view, it) }) },
                menuActions = appMenuActions(context, entry, onAddToHomeScreen, onRequestRemove),
                onFocused = onItemFocused,
                onBack = onDismiss,
                enabled = enabled
            )
        }

        if (isHomeTab) {
            if (uiState.shortcuts.isEmpty()) {
                item(key = "shortcuts_hint", span = { GridItemSpan(maxLineSpan) }) {
                    ShortcutsHintBlock(enabled = enabled, onFocused = onHintFocused)
                }
            } else {
                sectionHeader(R.string.all_apps_section_shortcuts, topPadding = 16.dp)
                itemsIndexed(uiState.shortcuts, key = { _, it -> "shortcut_${it.id}" }) { index, tile ->
                    AllAppsItem(
                        popDelayMs = popDelay("shortcut_${tile.id}", uiState.apps.size + index),
                        label = tile.label,
                        iconModel = tile.iconModel,
                        isOnHomeScreen = true,
                        onLaunch = { origin -> AppLauncher.launch(context, tile, origin?.let { LaunchOrigin(view, it) }) },
                        menuActions = shortcutMenuActions(context, tile, onRequestRemove),
                        onFocused = onItemFocused,
                        onBack = onDismiss,
                        enabled = enabled
                    )
                }
            }
        }
    }
}

private fun LazyGridScope.sectionHeader(@StringRes title: Int, topPadding: Dp) {
    item(key = "header_$title", span = { GridItemSpan(maxLineSpan) }) {
        Row(
            modifier = Modifier.padding(top = topPadding, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TabTrack(
    tab: AllAppsViewModel.Tab,
    onTabSelected: (AllAppsViewModel.Tab) -> Unit,
    focusFeedback: Modifier,
    entryFocusRequester: FocusRequester
) {
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    val bounds = remember { mutableStateMapOf<AllAppsViewModel.Tab, Pair<Float, Float>>() }
    val target = bounds[tab]
    val blobX by animateFloatAsState(target?.first ?: 0f, spring(dampingRatio = 0.6f, stiffness = 600f), label = "tabBlobX")
    val blobWidth by animateFloatAsState(target?.second ?: 0f, spring(dampingRatio = 0.6f, stiffness = 600f), label = "tabBlobWidth")

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .padding(TRACK_PADDING)
    ) {
        if (target != null) {
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(blobX.roundToInt(), 0) }
                        .width(with(density) { blobWidth.toDp() })
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Row {
            AllAppsViewModel.Tab.entries.forEach { entry ->
                val selected = tab == entry
                var focused by remember { mutableStateOf(false) }
                val textColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    label = "tabText"
                )
                Text(
                    text = stringResource(if (entry == AllAppsViewModel.Tab.ALL) R.string.all_apps_tab_all else R.string.all_apps_tab_home),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    modifier = (if (entry == AllAppsViewModel.Tab.ALL) Modifier.focusRequester(entryFocusRequester) else Modifier)
                        .onGloballyPositioned { bounds[entry] = it.positionInParent().x to it.size.width.toFloat() }
                        .onFocusChanged { focused = it.isFocused }
                        .then(focusFeedback)
                        .focusRing(focused = focused, cornerRadius = 50.dp)
                        .clip(CircleShape)
                        .clickable {
                            soundManager?.play(UiSound.CONFIRM)
                            onTabSelected(entry)
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private val SHEET_RADIUS = 36.dp
private val TRACK_PADDING = 5.dp
private const val POP_ITEMS = 24
private const val POP_BASE_DELAY_MS = 60L
private const val POP_STEP_MS = 18L

private fun appMenuActions(
    context: Context,
    entry: AppEntry,
    onAddToHomeScreen: (InstalledApp) -> Unit,
    onRequestRemove: (AppTile) -> Unit
): List<MenuAction> = buildList {
    val tile = entry.tile
    val packageName = entry.app.packageName
    add(MenuAction(R.string.menu_launch, Icons.Default.PlayArrow) { AppLauncher.launch(context, entry.app) })
    if (tile != null) {
        add(MenuAction(R.string.menu_edit, Icons.Default.Edit) { context.startActivity(AppTileSettingsActivity.newIntent(context, tile.id)) })
    }
    add(MenuAction(R.string.menu_details, Icons.Default.Info) { AppLauncher.openAppDetails(context, packageName) })
    if (tile != null) {
        add(MenuAction(R.string.menu_remove_from_home, Icons.Default.Delete) { onRequestRemove(tile) })
    } else {
        add(MenuAction(R.string.menu_add_to_home, Icons.Default.Add) { onAddToHomeScreen(entry.app) })
    }
    add(MenuAction(R.string.menu_uninstall, Icons.Default.DeleteForever) { AppLauncher.requestUninstall(context, packageName) })
}

private fun shortcutMenuActions(context: Context, tile: AppTile, onRequestRemove: (AppTile) -> Unit) = listOf(
    MenuAction(R.string.menu_launch, Icons.Default.PlayArrow) { AppLauncher.launch(context, tile) },
    MenuAction(R.string.menu_edit, Icons.Default.Edit) { context.startActivity(AppTileSettingsActivity.newIntent(context, tile.id)) },
    MenuAction(R.string.menu_remove_from_home, Icons.Default.Delete) { onRequestRemove(tile) }
)

@Composable
private fun ShortcutsHintBlock(enabled: Boolean, onFocused: () -> Unit) {
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .onSizeChanged { size = it }
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (!it.isFocused) return@onFocusChanged
                soundManager?.play(UiSound.MOVE)
                onFocused()
                scope.launch {
                    val footerClearance = with(density) { 16.dp.toPx() }
                    runCatching {
                        bringIntoViewRequester.bringIntoView(
                            Rect(0f, 0f, size.width.toFloat(), size.height + footerClearance)
                        )
                    }
                }
            }
            .focusRing(focused = isFocused, cornerRadius = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .focusable(enabled = enabled)
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(R.string.all_apps_section_shortcuts),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.all_apps_shortcuts_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
