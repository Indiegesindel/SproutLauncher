package games.indiegesindel.sproutlauncher.model

fun List<AppTile>.findTile(id: String): AppTile? {
    for (tile in this) {
        if (tile.id == id) return tile
        if (tile.isGroup) tile.groupTiles.findTile(id)?.let { return it }
    }
    return null
}

fun List<AppTile>.leafTiles(): Sequence<AppTile> = asSequence().flatMap { tile ->
    if (tile.isGroup) tile.groupTiles.leafTiles() else sequenceOf(tile)
}

fun List<AppTile>.replaceTile(updated: AppTile): List<AppTile> = map { tile ->
    when {
        tile.id == updated.id -> updated
        tile.isGroup -> tile.copy(groupTiles = tile.groupTiles.replaceTile(updated))
        else -> tile
    }
}

fun List<AppTile>.removeTile(id: String): List<AppTile> =
    removeWhere { it.id == id }

fun List<AppTile>.removePackage(packageName: String): List<AppTile> =
    removeWhere { !it.isGroup && it.packageName == packageName }

private fun List<AppTile>.removeWhere(predicate: (AppTile) -> Boolean): List<AppTile> =
    mapNotNull { tile ->
        when {
            predicate(tile) -> null
            tile.isGroup -> {
                val children = tile.groupTiles.removeWhere(predicate)
                if (children.isEmpty() && tile.groupTiles.isNotEmpty()) null
                else tile.copy(groupTiles = children)
            }
            else -> tile
        }
    }

fun List<AppTile>.moveTile(from: Int, to: Int): List<AppTile> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

fun List<AppTile>.moveTileInGroup(groupId: String, from: Int, to: Int): List<AppTile> = map { tile ->
    if (tile.id == groupId && tile.isGroup) tile.copy(groupTiles = tile.groupTiles.moveTile(from, to)) else tile
}

fun List<AppTile>.groupTiles(ids: Set<String>, group: (List<AppTile>) -> AppTile): List<AppTile> {
    val (selected, remaining) = partition { it.id in ids && !it.isGroup }
    if (selected.isEmpty()) return this
    return remaining + group(selected)
}

fun List<AppTile>.ungroup(groupId: String): List<AppTile> = flatMap { tile ->
    if (tile.id == groupId && tile.isGroup) tile.groupTiles else listOf(tile)
}

fun List<AppTile>.removeFromGroup(groupId: String, tileId: String): List<AppTile> {
    val group = firstOrNull { it.id == groupId && it.isGroup } ?: return this
    val removed = group.groupTiles.firstOrNull { it.id == tileId } ?: return this
    val remaining = group.groupTiles - removed
    return flatMap { tile ->
        when {
            tile.id != groupId -> listOf(tile)
            remaining.size <= 1 -> remaining
            else -> listOf(tile.copy(groupTiles = remaining))
        }
    } + removed
}

fun List<AppTile>.moveIntoGroup(tileId: String, groupId: String): List<AppTile> {
    val tile = firstOrNull { it.id == tileId && !it.isGroup } ?: return this
    if (none { it.id == groupId && it.isGroup }) return this
    return mapNotNull { current ->
        when (current.id) {
            tileId -> null
            groupId -> current.copy(groupTiles = current.groupTiles + tile)
            else -> current
        }
    }
}
