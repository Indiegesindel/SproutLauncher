package games.indiegesindel.sproutlauncher.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TileTreeTest {
    private val a = app("a")
    private val b = app("b")
    private val c = app("c")
    private val d = app("d")

    @Test
    fun findTile_searchesInsideGroups() {
        val group = AppTile.group("G", listOf(b, c))
        assertThat(listOf(a, group).findTile(c.id)).isEqualTo(c)
        assertThat(listOf(a, group).findTile("missing")).isNull()
    }

    @Test
    fun leafTiles_flattensGroups() {
        val tiles = listOf(a, AppTile.group("G", listOf(b, c)), d)
        assertThat(tiles.leafTiles().toList()).containsExactly(a, b, c, d).inOrder()
    }

    @Test
    fun replaceTile_updatesNestedTile() {
        val group = AppTile.group("G", listOf(b))
        val renamed = b.copy(label = "renamed")
        val result = listOf(a, group).replaceTile(renamed)
        assertThat(result.findTile(b.id)?.label).isEqualTo("renamed")
    }

    @Test
    fun removeTile_dropsGroupLeftEmpty() {
        val group = AppTile.group("G", listOf(b))
        assertThat(listOf(a, group).removeTile(b.id)).containsExactly(a)
    }

    @Test
    fun removeTile_keepsGroupWithRemainingChildren() {
        val group = AppTile.group("G", listOf(b, c))
        val result = listOf(group).removeTile(b.id)
        assertThat(result.single().groupTiles).containsExactly(c)
    }

    @Test
    fun removePackage_removesAtAnyDepthButNotGroups() {
        val sameA = app("a")
        val group = AppTile.group("G", listOf(sameA, b))
        val result = listOf(a, group, c).removePackage("pkg.a")
        assertThat(result.map { it.id }).containsExactly(group.id, c.id).inOrder()
        assertThat(result.first().groupTiles).containsExactly(b)
    }

    @Test
    fun moveTile_movesWithinBounds_andIgnoresInvalidIndices() {
        val tiles = listOf(a, b, c)
        assertThat(tiles.moveTile(0, 2)).containsExactly(b, c, a).inOrder()
        assertThat(tiles.moveTile(0, 5)).isSameInstanceAs(tiles)
    }

    @Test
    fun moveTileInGroup_reordersChildren() {
        val group = AppTile.group("G", listOf(a, b, c))
        val result = listOf(group).moveTileInGroup(group.id, 2, 0)
        assertThat(result.single().groupTiles).containsExactly(c, a, b).inOrder()
    }

    @Test
    fun groupTiles_wrapsSelectionAtEnd_andSkipsGroups() {
        val existing = AppTile.group("Existing", listOf(d))
        val result = listOf(a, b, existing, c).groupTiles(setOf(a.id, c.id, existing.id)) {
            AppTile.group("New", it)
        }
        assertThat(result.map { it.label }).containsExactly("b", "Existing", "New").inOrder()
        assertThat(result.last().groupTiles).containsExactly(a, c).inOrder()
    }

    @Test
    fun ungroup_replacesGroupInPlace() {
        val group = AppTile.group("G", listOf(b, c))
        assertThat(listOf(a, group, d).ungroup(group.id)).containsExactly(a, b, c, d).inOrder()
    }

    @Test
    fun removeFromGroup_appendsTileAndKeepsGroup() {
        val group = AppTile.group("G", listOf(a, b, c))
        val result = listOf(group, d).removeFromGroup(group.id, a.id)
        assertThat(result.map { it.id }).containsExactly(group.id, d.id, a.id).inOrder()
        assertThat(result.first().groupTiles).containsExactly(b, c).inOrder()
    }

    @Test
    fun removeFromGroup_dissolvesGroupWithOneTileLeft() {
        val group = AppTile.group("G", listOf(a, b))
        val result = listOf(c, group, d).removeFromGroup(group.id, a.id)
        assertThat(result).containsExactly(c, b, d, a).inOrder()
    }

    @Test
    fun moveIntoGroup_appendsToGroup() {
        val group = AppTile.group("G", listOf(b))
        val result = listOf(a, group).moveIntoGroup(a.id, group.id)
        assertThat(result.single().groupTiles).containsExactly(b, a).inOrder()
    }

    @Test
    fun moveIntoGroup_ignoresMissingGroup() {
        val tiles = listOf(a, b)
        assertThat(tiles.moveIntoGroup(a.id, b.id)).isSameInstanceAs(tiles)
    }

    private fun app(name: String) = AppTile(packageName = "pkg.$name", activityName = "Main", label = name)
}
