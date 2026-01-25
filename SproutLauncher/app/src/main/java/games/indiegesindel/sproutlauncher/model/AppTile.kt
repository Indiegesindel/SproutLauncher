package games.indiegesindel.sproutlauncher.model

import java.util.UUID

/**
 * Data structure representing a single app tile in the launcher.
 */
data class AppTile(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconUri: String? = null, // URI or path to a custom icon/image
    val shortcutId: String? = null,
    val isGroup: Boolean = false,
    val groupTiles: List<AppTile> = emptyList()
)
