package games.indiegesindel.sproutlauncher.model

import java.util.UUID

data class AppTile(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconUri: String? = null,
    val shortcutId: String? = null
)
