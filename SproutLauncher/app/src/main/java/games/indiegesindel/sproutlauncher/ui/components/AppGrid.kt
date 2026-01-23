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
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import games.indiegesindel.sproutlauncher.model.AppTile
import android.view.KeyEvent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import games.indiegesindel.sproutlauncher.utils.IconUtils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

@Composable
fun rememberReorderableLazyGridState(
    gridState: LazyGridState,
    onReorder: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
): ReorderableLazyGridState {
    return remember(gridState) {
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
    roundness: Int = 16
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState, onReorder, onDragEnd)

    fun moveItem(currentIndex: Int, direction: String) {
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
                    roundness = roundness
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
    roundness: Int = 16
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var isYPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    val isDragging = reorderableState.draggedIndex == index
    val dragOffset = if (isDragging) reorderableState.dragOffset else IntOffset.Zero

    Box(
        modifier = Modifier
            .zIndex(if (isDragging) 1f else 0f)
            .focusRequester(focusRequester)
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
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
                            onClick()
                            true
                        }
                        KeyEvent.KEYCODE_BUTTON_X,
                        KeyEvent.KEYCODE_BUTTON_START,
                        KeyEvent.KEYCODE_MENU -> {
                            showMenu = true
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable()
            .pointerInput(Unit) {
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
            .fillMaxHeight()
            .padding(8.dp),
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.inverseSurface)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
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
                    if (isDragging || isYPressed) {
                        alpha = 0.8f
                        scaleX = 1.15f
                        scaleY = 1.15f
                    }
                }
                .then(
                    if (isFocused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape((roundness + 6).dp))
                    else Modifier
                )
                .padding(if (isFocused) 6.dp else 0.dp)
                .clip(RoundedCornerShape(roundness.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = onClick,
                    onDoubleClick = { showMenu = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            val appIcon = remember(tile.packageName) {
                IconUtils.getFullSquareIcon(context, tile.packageName, 512)
            }
            
            AsyncImage(
                model = remember(tile.iconUri, appIcon) {
                    ImageRequest.Builder(context)
                        .data(tile.iconUri ?: appIcon)
                        .size(Size(512, 512))
                        .crossfade(true)
                        .build()
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Launch") },
                onClick = {
                    showMenu = false
                    onClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onSettings()
                }
            )
            DropdownMenuItem(
                text = { Text("Details") },
                onClick = {
                    showMenu = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${tile.packageName}")
                    }
                    context.startActivity(intent)
                }
            )
            DropdownMenuItem(
                text = { Text("Remove from Homescreen") },
                onClick = {
                    showMenu = false
                    onRemove()
                }
            )
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
                }
            )
        }
    }
}
