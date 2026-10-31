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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.data.GridLayout
import games.indiegesindel.sproutlauncher.model.AppTile

@Stable
interface TileGridActions {
    fun onLaunch(tile: AppTile)
    fun onOpenGroup(group: AppTile)
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
    enabled: Boolean = true
) {
    val isPhone = LocalConfiguration.current.smallestScreenWidthDp < 600
    val gridHeight = if (isPhone) 240.dp else 440.dp

    if (tiles.isEmpty()) {
        HomeDisclaimer(modifier = modifier.height(gridHeight).fillMaxWidth())
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
        modifier = modifier.height(gridHeight).wrapContentWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
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
                enabled = enabled
            )
        }
    }
}
