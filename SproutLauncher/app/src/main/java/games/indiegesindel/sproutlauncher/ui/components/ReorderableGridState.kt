package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset

@Stable
class ReorderableGridState internal constructor(
    private val gridState: LazyGridState,
    private val onMove: () -> (Int, Int) -> Unit
) {
    var draggedKey by mutableStateOf<Any?>(null)
        private set
    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    private var layoutAtLastMove: LazyGridLayoutInfo? = null

    fun onDragStart(key: Any) {
        draggedKey = key
        dragOffset = Offset.Zero
        layoutAtLastMove = null
    }

    fun onDrag(delta: Offset) {
        val key = draggedKey ?: return
        dragOffset += delta

        val layout = gridState.layoutInfo
        if (layout === layoutAtLastMove) return
        val current = layout.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        val center = (current.offset.toOffset() + current.size.center.toOffset() + dragOffset).round()
        val target = layout.visibleItemsInfo.firstOrNull {
            it.key != key && IntRect(it.offset, it.size).contains(center)
        } ?: return

        onMove()(current.index, target.index)
        layoutAtLastMove = layout
        dragOffset -= (target.offset - current.offset).toOffset()
    }

    fun onDragEnd() {
        draggedKey = null
        dragOffset = Offset.Zero
        layoutAtLastMove = null
    }
}

@Composable
fun rememberReorderableGridState(gridState: LazyGridState, onMove: (from: Int, to: Int) -> Unit): ReorderableGridState {
    val currentOnMove by rememberUpdatedState(onMove)
    return remember(gridState) { ReorderableGridState(gridState) { currentOnMove } }
}
