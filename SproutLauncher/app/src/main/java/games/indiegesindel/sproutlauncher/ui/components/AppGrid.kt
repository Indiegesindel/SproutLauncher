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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.CircularProgressIndicator
import games.indiegesindel.sproutlauncher.model.AppTile
import android.view.KeyEvent
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
fun AppGrid(
    appTiles: List<AppTile>,
    isLoading: Boolean = false,
    onAppClick: (AppTile) -> Unit,
    onRemove: (AppTile) -> Unit,
    onSettings: (AppTile) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val rows = if (isPhone) 1 else 2
    val gridState = rememberLazyGridState()

    fun moveItem(currentIndex: Int, direction: String) {
        val targetIndex = when (direction) {
            "UP" -> if (currentIndex % rows == 1) currentIndex - 1 else -1
            "DOWN" -> if (currentIndex % rows == 0 && rows > 1 && currentIndex + 1 < appTiles.size) currentIndex + 1 else -1
            "LEFT" -> if (currentIndex >= rows) currentIndex - rows else -1
            "RIGHT" -> if (currentIndex + rows < appTiles.size) currentIndex + rows else -1
            else -> -1
        }
        if (targetIndex != -1 && targetIndex in appTiles.indices) {
            onReorder(currentIndex, targetIndex)
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
            contentPadding = PaddingValues(start = 32.dp, top = 16.dp, end = 32.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appTiles.size, key = { appTiles[it].id }) { index ->
                val tile = appTiles[index]
                val tileId = "tile:${tile.id}"
                AppTileItem(
                    tile = tile,
                    index = index,
                    rows = rows,
                    isPhone = isPhone,
                    onClick = { onAppClick(tile) },
                    onRemove = { onRemove(tile) },
                    onSettings = { onSettings(tile) },
                    onMove = { direction -> moveItem(index, direction) },
                    onDragReorder = { from, to -> onReorder(from, to) },
                    gridState = gridState,
                    isTargetFocused = focusedItemId == tileId,
                    onFocused = { onFocusItemIdChanged(tileId) }
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
    rows: Int,
    isPhone: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onSettings: () -> Unit,
    onMove: (String) -> Unit,
    onDragReorder: (Int, Int) -> Unit,
    gridState: LazyGridState,
    isTargetFocused: Boolean,
    onFocused: () -> Unit
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var isXPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    var dragOffset by remember { mutableStateOf(IntOffset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

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
                
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                    isXPressed = isDown
                    return@onKeyEvent true
                }
                
                if (isXPressed && isDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> { onMove("UP"); true }
                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> { onMove("DOWN"); true }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> { onMove("LEFT"); true }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> { onMove("RIGHT"); true }
                        else -> false
                    }
                } else if (isDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BUTTON_START,
                        KeyEvent.KEYCODE_MENU -> {
                            onSettings()
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
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = IntOffset.Zero
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = IntOffset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
                        
                        // Check if we dragged over another item
                        val currentItemInfo = gridState.layoutInfo.visibleItemsInfo.find { it.index == index }
                        currentItemInfo?.let { info ->
                            val centerX = info.offset.x + info.size.width / 2 + dragOffset.x
                            val centerY = info.offset.y + info.size.height / 2 + dragOffset.y
                            
                            val targetItem = gridState.layoutInfo.visibleItemsInfo.find { target ->
                                target.index != index &&
                                centerX in target.offset.x..(target.offset.x + target.size.width) &&
                                centerY in target.offset.y..(target.offset.y + target.size.height)
                            }
                            
                            targetItem?.let {
                                val fromOffset = info.offset
                                val toOffset = it.offset
                                onDragReorder(index, it.index)
                                dragOffset -= IntOffset(toOffset.x - fromOffset.x, toOffset.y - fromOffset.y)
                            }
                        }
                    }
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tooltip
        if (isFocused && !isDragging && !isXPressed) {
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
                .height(if (isPhone) 140.dp else 160.dp)
                .aspectRatio(1f)
                .offset { if (isDragging) dragOffset else IntOffset.Zero }
                .graphicsLayer {
                    if (isDragging || isXPressed) {
                        alpha = 0.8f
                        scaleX = 1.1f
                        scaleY = 1.1f
                    }
                }
                .then(
                    if (isFocused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp))
                    else Modifier
                )
                .padding(if (isFocused) 6.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = onClick,
                    onDoubleClick = onSettings
                ),
            contentAlignment = Alignment.Center
        ) {
            val appIcon = remember(tile.packageName) {
                IconUtils.getFullSquareIcon(context, tile.packageName)
            }
            
            AsyncImage(
                model = tile.iconUri ?: appIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
