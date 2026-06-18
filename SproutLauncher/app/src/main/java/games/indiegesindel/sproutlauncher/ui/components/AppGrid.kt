package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import androidx.compose.material3.CircularProgressIndicator
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.provider.Settings
import games.indiegesindel.sproutlauncher.model.AppTile
import android.view.KeyEvent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.PlayArrow
import games.indiegesindel.sproutlauncher.utils.IconUtils
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun rememberReorderableLazyGridState(
    gridState: LazyGridState,
    onReorder: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
): ReorderableLazyGridState {
    return remember(gridState, onReorder, onDragEnd) {
        ReorderableLazyGridState(gridState, onReorder, onDragEnd)
    }
}

class ReorderableLazyGridState(
    val gridState: LazyGridState,
    private val onReorder: (Int, Int) -> Unit,
    private val onDragEndCallback: () -> Unit
) {
    var draggedIndex by mutableStateOf<Int?>(null)
        private set
    var dragOffset by mutableStateOf(IntOffset.Zero)
        private set

    fun onDragStart(index: Int) {
        draggedIndex = index
    }

    fun onDrag(dragAmount: IntOffset) {
        draggedIndex?.let { index ->
            dragOffset += dragAmount
            
            val layoutInfo = gridState.layoutInfo
            val currentItem = layoutInfo.visibleItemsInfo.find { it.index == index } ?: return
            
            val centerX = currentItem.offset.x + currentItem.size.width / 2 + dragOffset.x
            val centerY = currentItem.offset.y + currentItem.size.height / 2 + dragOffset.y
            
            val targetItem = layoutInfo.visibleItemsInfo.find { target ->
                target.index != index &&
                centerX in target.offset.x..(target.offset.x + target.size.width) &&
                centerY in target.offset.y..(target.offset.y + target.size.height)
            }
            
            targetItem?.let { target ->
                if (index != target.index) {
                    val fromOffset = currentItem.offset
                    val toOffset = target.offset
                    
                    onReorder(index, target.index)
                    draggedIndex = target.index
                    
                    // Adjust dragOffset to maintain the item under the finger
                    // We need to subtract the change in the item's static position
                    dragOffset -= IntOffset(toOffset.x - fromOffset.x, toOffset.y - fromOffset.y)
                }
            }
        }
    }

    fun onDragEnd() {
        draggedIndex = null
        dragOffset = IntOffset.Zero
        onDragEndCallback()
    }

    fun onDragCancel() {
        draggedIndex = null
        dragOffset = IntOffset.Zero
    }
}

@Composable
fun AppGrid(
    appTiles: List<AppTile>,
    onAppClick: (AppTile) -> Unit,
    onRemove: (AppTile) -> Unit,
    onSettings: (AppTile) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    rows: Int = 2,
    horizontalSpacing: Int = 0,
    verticalSpacing: Int = 0,
    onFocusChanged: (Boolean) -> Unit = {},
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {},
    onDragEnd: () -> Unit = {},
    roundness: Int = 16,
    isInMultiSelectMode: Boolean = false,
    selectedTileIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onCreateGroup: (String) -> Unit = {},
    onGroupSelected: () -> Unit = {},
    onRemoveSelected: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onOpenGroup: (AppTile) -> Unit = {},
    onAddToGroup: (AppTile) -> Unit = {},
    onUngroup: (String) -> Unit = {},
    onRemoveFromGroup: ((String) -> Unit)? = null,
    onDismiss: () -> Unit = {},
    showGroupOptions: Boolean = true,
    hasGroups: Boolean = false,
    enabled: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState, onReorder, onDragEnd)

    fun moveItem(currentIndex: Int, direction: String) {
        if (!enabled) return
        val targetIndex = when (direction) {
            "UP" -> if (currentIndex % rows > 0) currentIndex - 1 else -1
            "DOWN" -> if (currentIndex % rows < rows - 1 && currentIndex + 1 < appTiles.size) currentIndex + 1 else -1
            "LEFT" -> if (currentIndex >= rows) currentIndex - rows else -1
            "RIGHT" -> if (currentIndex + rows < appTiles.size) currentIndex + rows else -1
            else -> -1
        }
        if (targetIndex != -1 && targetIndex in appTiles.indices) {
            onReorder(currentIndex, targetIndex)
            onDragEnd() // Trigger save for D-pad reordering
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .height(if (isPhone) 240.dp else 440.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (appTiles.isEmpty()) {
        HomeDisclaimer(
            modifier = modifier
                .height(if (isPhone) 240.dp else 440.dp)
                .fillMaxWidth()
        )
    } else {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(rows),
            state = gridState,
            modifier = modifier
                .height(if (isPhone) 240.dp else 440.dp)
                .wrapContentWidth()
                .onFocusChanged { state ->
                    onFocusChanged(state.hasFocus)
                },
            contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing.dp)
        ) {
            items(appTiles.size, key = { appTiles[it].id }) { index ->
                val tile = appTiles[index]
                val tileId = "tile:${tile.id}"
                AppTileItem(
                    tile = tile,
                    index = index,
                    onClick = { onAppClick(tile) },
                    onSettings = { onSettings(tile) },
                    onRemove = { onRemove(tile) },
                    onMove = { direction -> moveItem(index, direction) },
                    reorderableState = reorderableState,
                    isTargetFocused = focusedItemId == tileId,
                    onFocused = { onFocusItemIdChanged(tileId) },
                    roundness = roundness,
                    isInMultiSelectMode = isInMultiSelectMode,
                    isSelected = selectedTileIds.contains(tile.id),
                    onToggleSelection = { if (!tile.isGroup) onToggleSelection(tile.id) },
                    onCreateGroup = { onCreateGroup(tile.id) },
                    onGroupSelected = onGroupSelected,
                    onRemoveSelected = onRemoveSelected,
                    onClearSelection = onClearSelection,
                    onOpenGroup = { onOpenGroup(tile) },
                    onAddToGroup = { onAddToGroup(tile) },
                    onUngroup = { onUngroup(tile.id) },
                    onRemoveFromGroup = onRemoveFromGroup?.let { { it(tile.id) } },
                    onDismiss = onDismiss,
                    showGroupOptions = showGroupOptions,
                    hasGroups = hasGroups,
                    enabled = enabled
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTileItem(
    tile: AppTile,
    index: Int,
    onClick: () -> Unit,
    onSettings: () -> Unit,
    onRemove: () -> Unit,
    onMove: (String) -> Unit,
    reorderableState: ReorderableLazyGridState,
    isTargetFocused: Boolean,
    onFocused: () -> Unit,
    roundness: Int = 16,
    isInMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onCreateGroup: () -> Unit = {},
    onGroupSelected: () -> Unit = {},
    onRemoveSelected: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onOpenGroup: () -> Unit = {},
    onAddToGroup: () -> Unit = {},
    onUngroup: () -> Unit = {},
    onRemoveFromGroup: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    showGroupOptions: Boolean = true,
    hasGroups: Boolean = false,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var isYPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    val isDragging = reorderableState.draggedIndex == index
    val dragOffset = if (isDragging) reorderableState.dragOffset else IntOffset.Zero

    // Focus scale (springs up when focused, higher scale when dragging)
    val focusScale by animateFloatAsState(
        targetValue = when {
            isDragging || isYPressed -> 1.15f
            isFocused -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "tileFocusScale"
    )
    val focusAlpha by animateFloatAsState(
        targetValue = if (isDragging || isYPressed) 0.8f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tileFocusAlpha"
    )

    // Staggered entrance (fires once when tile first enters composition)
    var entranceAlpha by remember { mutableStateOf(0f) }
    var entranceScale by remember { mutableStateOf(0.88f) }
    val animatedEntranceAlpha by animateFloatAsState(
        targetValue = entranceAlpha,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "entranceAlpha"
    )
    val animatedEntranceScale by animateFloatAsState(
        targetValue = entranceScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "entranceScale"
    )
    LaunchedEffect(Unit) {
        delay((index * 30L).coerceAtMost(300L))
        entranceAlpha = 1f
        entranceScale = 1f
    }

    // Touch press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tilePress"
    )

    // Animated border width and padding
    val focusBorderWidth by animateFloatAsState(
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
            .onGloballyPositioned { size = it.size }
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    soundManager?.play(UiSound.MOVE)
                    onFocused()
                    coroutineScope.launch {
                        val horizontalPadding = with(density) { 24.dp.toPx() }
                        bringIntoViewRequester.bringIntoView(
                            Rect(
                                left = -horizontalPadding,
                                top = 0f,
                                right = size.width.toFloat() + horizontalPadding,
                                bottom = size.height.toFloat()
                            )
                        )
                    }
                }
            }
            .onKeyEvent { event ->
                val isDown = event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
                
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
                    isYPressed = isDown
                    return@onKeyEvent true
                }
                
                if (isYPressed && isDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> { onMove("UP"); true }
                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> { onMove("DOWN"); true }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> { onMove("LEFT"); true }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> { onMove("RIGHT"); true }
                        else -> false
                    }
                } else if (isDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_BUTTON_A -> {
                            soundManager?.play(UiSound.CONFIRM)
                            if (isInMultiSelectMode) {
                                if (!tile.isGroup) onToggleSelection()
                            } else if (tile.isGroup) {
                                onOpenGroup()
                            } else {
                                onClick()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_BUTTON_X,
                        KeyEvent.KEYCODE_BUTTON_START,
                        KeyEvent.KEYCODE_MENU -> {
                            showMenu = true
                            true
                        }
                        KeyEvent.KEYCODE_BUTTON_B -> {
                            soundManager?.play(UiSound.BACK)
                            onDismiss()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(enabled = enabled)
            .pointerInput(index, tile.id) {
                if (!enabled) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { reorderableState.onDragStart(index) },
                    onDragEnd = { reorderableState.onDragEnd() },
                    onDragCancel = { reorderableState.onDragCancel() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        reorderableState.onDrag(IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt()))
                    }
                )
            }
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Tooltip
        if (isFocused && !isDragging && !isYPressed) {
            val density = LocalDensity.current
            val yOffset = with(density) { (-30).dp.roundToPx() }
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffset),
                properties = PopupProperties(focusable = false)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.inverseSurface)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (tile.isGroup) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = tile.label,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp, 7.dp)
                            .background(MaterialTheme.colorScheme.inverseSurface, shape = TriangleShape)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .offset { dragOffset }
                .graphicsLayer {
                    alpha = focusAlpha * animatedEntranceAlpha
                    scaleX = focusScale * animatedEntranceScale * pressScale
                    scaleY = focusScale * animatedEntranceScale * pressScale
                }
                .border(focusBorderWidth.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape((roundness + 4).dp))
                .padding(focusPadding.dp)
                .clip(RoundedCornerShape(roundness.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        soundManager?.play(UiSound.CONFIRM)
                        if (isInMultiSelectMode) {
                            if (!tile.isGroup) onToggleSelection()
                        } else if (tile.isGroup) {
                            onOpenGroup()
                        } else {
                            onClick()
                        }
                    },
                    onDoubleClick = { showMenu = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (tile.isGroup && tile.iconUri == null) {
                GroupIcon(groupTiles = tile.groupTiles, modifier = Modifier.fillMaxSize())
            } else {
                val shortcutIcon = remember(tile.packageName, tile.shortcutId) {
                    tile.shortcutId?.let { IconUtils.getShortcutIcon(context, tile.packageName, it) }
                }

                val appIcon = remember(tile.packageName) {
                    IconUtils.getFullSquareIcon(context, tile.packageName, 512)
                }
                
                AsyncImage(
                    model = remember(tile.iconUri, appIcon, shortcutIcon) {
                        ImageRequest.Builder(context)
                            .data(tile.iconUri ?: shortcutIcon ?: appIcon)
                            .size(Size(512, 512))
                            .crossfade(true)
                            .build()
                    },
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

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (showGroupOptions && isInMultiSelectMode && isSelected) {
                DropdownMenuItem(
                    text = { Text("Group selected") },
                    onClick = {
                        showMenu = false
                        onGroupSelected()
                    },
                    leadingIcon = { Icon(Icons.Default.GroupWork, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Remove selected") },
                    onClick = {
                        showMenu = false
                        onRemoveSelected()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Clear selection") },
                    onClick = {
                        showMenu = false
                        onClearSelection()
                    },
                    leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null) }
                )
            } else if (tile.isGroup) {
                if (showGroupOptions) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            showMenu = false
                            onOpenGroup()
                        },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onSettings()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Ungroup") },
                        onClick = {
                            showMenu = false
                            onUngroup()
                        },
                        leadingIcon = { Icon(Icons.Default.LayersClear, contentDescription = null) }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("Launch") },
                    onClick = {
                        showMenu = false
                        onClick()
                    },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onSettings()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                if (showGroupOptions && onRemoveFromGroup == null) {
                    DropdownMenuItem(
                        text = { Text("Create group") },
                        onClick = {
                            showMenu = false
                            onCreateGroup()
                        },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                    )
                    if (hasGroups) {
                        DropdownMenuItem(
                            text = { Text("Add to Group") },
                            onClick = {
                                showMenu = false
                                onAddToGroup()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                }
                if (tile.shortcutId == null) {
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${tile.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
                if (showGroupOptions && onRemoveFromGroup != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from group") },
                        onClick = {
                            showMenu = false
                            onRemoveFromGroup()
                        },
                        leadingIcon = { Icon(Icons.Default.FolderDelete, contentDescription = null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Remove from Homescreen") },
                    onClick = {
                        showMenu = false
                        onRemove()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                )
                if (tile.shortcutId == null) {
                    DropdownMenuItem(
                        text = { Text("Uninstall") },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${tile.packageName}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Log or handle error
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null) }
                    )
                }
            }
        }
    }
}
