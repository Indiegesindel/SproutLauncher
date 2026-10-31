package games.indiegesindel.sproutlauncher.ui.components

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.icons.iconModel
import games.indiegesindel.sproutlauncher.utils.AppLauncher
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TileItem(
    tile: AppTile,
    index: Int,
    roundness: Int,
    actions: TileGridActions,
    menuContext: TileMenuContext,
    reorderState: ReorderableGridState,
    isSelected: Boolean,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onFocused: () -> Unit,
    onMove: (MoveDirection) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var isFocused by remember { mutableStateOf(false) }
    var isYHeld by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(requestFocus) {
        if (!requestFocus) return@LaunchedEffect
        focusRequester.requestFocusWithRetry()
        onFocusHandled()
    }

    val isDragging = reorderState.draggedKey == tile.id
    val isLifted = isDragging || isYHeld

    val activate = {
        soundManager?.play(UiSound.CONFIRM)
        when {
            menuContext.isInMultiSelectMode -> if (!tile.isGroup) actions.onToggleSelection(tile)
            tile.isGroup -> actions.onOpenGroup(tile)
            else -> actions.onLaunch(tile)
        }
    }

    val focusScale by animateFloatAsState(
        targetValue = when {
            isLifted -> 1.15f
            isFocused -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "tileFocusScale"
    )
    val liftAlpha by animateFloatAsState(
        targetValue = if (isLifted) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tileLiftAlpha"
    )

    var hasEntered by remember { mutableStateOf(false) }
    val entranceAlpha by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "tileEntranceAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileEntranceScale"
    )
    LaunchedEffect(Unit) {
        delay((index * 30L).coerceAtMost(300L))
        hasEntered = true
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tilePressScale"
    )

    val borderWidth by animateFloatAsState(
        targetValue = when {
            isSelected -> 4f
            isFocused -> 3f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "tileBorderWidth"
    )
    val focusPadding by animateFloatAsState(
        targetValue = if (isFocused || isSelected) 6f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "tilePadding"
    )

    Box(
        modifier = Modifier
            .zIndex(if (isDragging) 1f else 0f)
            .focusRequester(focusRequester)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onSizeChanged { size = it }
            .onFocusChanged {
                isFocused = it.isFocused
                if (!it.isFocused) {
                    isYHeld = false
                    return@onFocusChanged
                }
                soundManager?.play(UiSound.MOVE)
                onFocused()
                coroutineScope.launch {
                    val horizontalPadding = with(density) { 24.dp.toPx() }
                    bringIntoViewRequester.bringIntoView(
                        Rect(-horizontalPadding, 0f, size.width + horizontalPadding, size.height.toFloat())
                    )
                }
            }
            .onKeyEvent { event ->
                if (event.key == Key.ButtonY) {
                    isYHeld = event.type == KeyEventType.KeyDown
                    return@onKeyEvent true
                }
                if (!isYHeld || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val direction = when (event.key) {
                    Key.DirectionUp, Key.W -> MoveDirection.UP
                    Key.DirectionDown, Key.S -> MoveDirection.DOWN
                    Key.DirectionLeft, Key.A -> MoveDirection.LEFT
                    Key.DirectionRight, Key.D -> MoveDirection.RIGHT
                    else -> return@onKeyEvent false
                }
                onMove(direction)
                true
            }
            .gamepadActions(
                onConfirm = activate,
                onMenu = { showMenu = true },
                onBack = {
                    soundManager?.play(UiSound.BACK)
                    actions.onBack()
                }
            )
            .focusable(enabled = enabled)
            .pointerInput(tile.id, enabled) {
                if (!enabled) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { reorderState.onDragStart(tile.id) },
                    onDragEnd = reorderState::onDragEnd,
                    onDragCancel = reorderState::onDragEnd,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        reorderState.onDrag(dragAmount)
                    }
                )
            }
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .offset { if (isDragging) reorderState.dragOffset.round() else IntOffset.Zero },
            contentAlignment = Alignment.Center
        ) {
            if (isFocused && !isLifted) {
                FocusTooltip(text = tile.label, showFolderIcon = tile.isGroup)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = liftAlpha * entranceAlpha
                        val scale = focusScale * entranceScale * pressScale
                        scaleX = scale
                        scaleY = scale
                    }
                    .then(
                        if (borderWidth > 0.5f) {
                            Modifier.border(borderWidth.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape((roundness + 4).dp))
                        } else {
                            Modifier
                        }
                    )
                    .padding(focusPadding.dp)
                    .clip(RoundedCornerShape(roundness.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = activate,
                        onDoubleClick = { showMenu = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tile.isGroup && tile.iconUri == null) {
                    GroupIcon(groupTiles = tile.groupTiles, modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = tile.iconModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }

        ContextMenu(
            expanded = showMenu,
            onDismiss = { showMenu = false },
            actions = tileMenuActions(context, tile, isSelected, menuContext, actions)
        )
    }
}

private fun tileMenuActions(
    context: Context,
    tile: AppTile,
    isSelected: Boolean,
    menu: TileMenuContext,
    actions: TileGridActions
): List<MenuAction> = buildList {
    when {
        menu.isInMultiSelectMode && isSelected -> {
            add(MenuAction(R.string.menu_group_selected, Icons.Default.GroupWork, actions::onGroupSelected))
            add(MenuAction(R.string.menu_remove_selected, Icons.Default.Delete, actions::onRemoveSelected))
            add(MenuAction(R.string.menu_clear_selection, Icons.Default.Clear, actions::onClearSelection))
        }
        tile.isGroup -> {
            add(MenuAction(R.string.menu_open, Icons.Default.FolderOpen) { actions.onOpenGroup(tile) })
            add(MenuAction(R.string.menu_edit, Icons.Default.Edit) { actions.onEdit(tile) })
            add(MenuAction(R.string.menu_ungroup, Icons.Default.LayersClear) { actions.onUngroup(tile) })
        }
        else -> {
            add(MenuAction(R.string.menu_launch, Icons.Default.PlayArrow) { actions.onLaunch(tile) })
            add(MenuAction(R.string.menu_edit, Icons.Default.Edit) { actions.onEdit(tile) })
            if (!menu.isInGroup) {
                add(MenuAction(R.string.menu_create_group, Icons.Default.CreateNewFolder) { actions.onCreateGroup(tile) })
                if (menu.hasGroups) {
                    add(MenuAction(R.string.menu_add_to_group, Icons.Default.Add) { actions.onAddToGroup(tile) })
                }
            }
            if (!tile.isShortcut) {
                add(MenuAction(R.string.menu_details, Icons.Default.Info) { AppLauncher.openAppDetails(context, tile.packageName) })
            }
            if (menu.isInGroup) {
                add(MenuAction(R.string.menu_remove_from_group, Icons.Default.FolderDelete) { actions.onRemoveFromGroup(tile) })
            }
            add(MenuAction(R.string.menu_remove_from_home, Icons.Default.Delete) { actions.onRemove(tile) })
            if (!tile.isShortcut) {
                add(MenuAction(R.string.menu_uninstall, Icons.Default.DeleteForever) { AppLauncher.requestUninstall(context, tile.packageName) })
            }
        }
    }
}
