package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.data.GridAlignment
import games.indiegesindel.sproutlauncher.data.GridLayout
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.motion.Motion

@Stable
interface TileGridActions {
    fun onLaunch(tile: AppTile, origin: Rect? = null): Boolean
    fun onOpenGroup(group: AppTile, origin: Rect? = null)
    fun onEdit(tile: AppTile)
    fun onRemove(tile: AppTile)
    fun onReorder(from: Int, to: Int)
    fun onBack()
    fun onToggleSelection(tile: AppTile) {}
    fun onCreateGroup(tile: AppTile) {}
    fun onGroupSelected() {}
    fun onRemoveSelected() {}
    fun onClearSelection() {}
    fun onAddToGroup(tile: AppTile) {}
    fun onUngroup(group: AppTile) {}
    fun onRemoveFromGroup(tile: AppTile) {}
}

enum class MoveDirection { UP, DOWN, LEFT, RIGHT }

data class TileMenuContext(
    val isInGroup: Boolean,
    val hasGroups: Boolean,
    val isInMultiSelectMode: Boolean
)

@Composable
fun TileGrid(
    tiles: List<AppTile>,
    layout: GridLayout,
    actions: TileGridActions,
    menuContext: TileMenuContext,
    modifier: Modifier = Modifier,
    selectedTileIds: Set<String> = emptySet(),
    pendingFocusTileId: String? = null,
    onFocusConsumed: () -> Unit = {},
    onTileFocused: (AppTile) -> Unit = {},
    enabled: Boolean = true,
    entranceDelayMs: Long = 0L
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val rows = layout.rows.coerceAtLeast(1)
        val cap = MAX_TILE * rows + layout.verticalSpacing.dp * (rows - 1) + GRID_VERTICAL_PADDING * 2
        val gridHeight = if (maxHeight == Dp.Infinity) cap else minOf(maxHeight, cap)
        TileGridContent(
            tiles, layout, actions, menuContext, gridHeight, selectedTileIds, pendingFocusTileId,
            onFocusConsumed, onTileFocused, enabled, entranceDelayMs
        )
    }
}

private val MAX_TILE = 200.dp
private val GRID_VERTICAL_PADDING = 8.dp

@Composable
private fun TileGridContent(
    tiles: List<AppTile>,
    layout: GridLayout,
    actions: TileGridActions,
    menuContext: TileMenuContext,
    gridHeight: Dp,
    selectedTileIds: Set<String>,
    pendingFocusTileId: String?,
    onFocusConsumed: () -> Unit,
    onTileFocused: (AppTile) -> Unit,
    enabled: Boolean,
    entranceDelayMs: Long
) {
    if (tiles.isEmpty()) {
        HomeDisclaimer(modifier = Modifier.height(gridHeight).fillMaxWidth())
        return
    }

    val gridState = rememberLazyGridState()
    val reorderState = rememberReorderableGridState(gridState, actions::onReorder)

    fun move(index: Int, direction: MoveDirection) {
        if (!enabled) return
        val rows = layout.rows
        val target = when (direction) {
            MoveDirection.UP -> if (index % rows > 0) index - 1 else null
            MoveDirection.DOWN -> if (index % rows < rows - 1 && index + 1 < tiles.size) index + 1 else null
            MoveDirection.LEFT -> if (index >= rows) index - rows else null
            MoveDirection.RIGHT -> if (index + rows < tiles.size) index + rows else null
        }
        target?.let { actions.onReorder(index, it) }
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(layout.rows.coerceAtLeast(1)),
        state = gridState,
        modifier = Modifier
            .height(gridHeight)
            .then(
                if (layout.alignment == GridAlignment.START) Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start)
                else Modifier.wrapContentWidth()
            ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = GRID_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(layout.horizontalSpacing.dp),
        verticalArrangement = Arrangement.spacedBy(layout.verticalSpacing.dp)
    ) {
        itemsIndexed(tiles, key = { _, tile -> tile.id }) { index, tile ->
            TileItem(
                tile = tile,
                index = index,
                roundness = layout.roundness,
                actions = actions,
                menuContext = menuContext,
                reorderState = reorderState,
                isSelected = tile.id in selectedTileIds,
                requestFocus = pendingFocusTileId == tile.id,
                onFocusHandled = onFocusConsumed,
                onFocused = { onTileFocused(tile) },
                onMove = { move(index, it) },
                enabled = enabled,
                rows = layout.rows.coerceAtLeast(1),
                entranceDelayMs = entranceDelayMs,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = if (reorderState.draggedKey == tile.id) null else Motion.focus()
                )
            )
        }
    }
}
