package games.indiegesindel.sproutlauncher.ui.icons

import androidx.annotation.ColorInt
import games.indiegesindel.sproutlauncher.model.AppTile

sealed interface AppIcon {
    data class Package(val packageName: String) : AppIcon
    data class Activity(val packageName: String, val activityName: String) : AppIcon
    data class Shortcut(val packageName: String, val shortcutId: String) : AppIcon
    data class Themed(val packageName: String, @param:ColorInt val tint: Int) : AppIcon
}

val AppTile.iconModel: Any
    get() = iconUri ?: when {
        shortcutId != null && !isLegacyShortcut -> AppIcon.Shortcut(packageName, shortcutId)
        else -> AppIcon.Package(packageName)
    }
