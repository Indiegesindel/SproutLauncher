package games.indiegesindel.sproutlauncher.model

import java.util.UUID

data class AppTile(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconUri: String? = null,
    val shortcutId: String? = null,
    val isGroup: Boolean = false,
    val groupTiles: List<AppTile> = emptyList()
) {
    val isShortcut: Boolean get() = shortcutId != null

    val isLegacyShortcut: Boolean get() = shortcutId?.startsWith(LEGACY_SHORTCUT_PREFIX) == true

    val legacyIntentUri: String? get() = shortcutId?.takeIf { isLegacyShortcut }?.removePrefix(LEGACY_SHORTCUT_PREFIX)

    fun matchesActivity(packageName: String, activityName: String): Boolean =
        !isGroup && !isShortcut && this.packageName == packageName && this.activityName == activityName

    companion object {
        const val LEGACY_SHORTCUT_PREFIX = "legacy:"

        fun group(label: String, tiles: List<AppTile>) = AppTile(
            packageName = "",
            activityName = "",
            label = label,
            isGroup = true,
            groupTiles = tiles
        )
    }
}
