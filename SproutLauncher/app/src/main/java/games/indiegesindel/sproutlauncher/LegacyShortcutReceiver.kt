package games.indiegesindel.sproutlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.leafTiles

@Suppress("DEPRECATION")
class LegacyShortcutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_SHORTCUT) return
        val shortcutIntent = intent.parcelable<Intent>(Intent.EXTRA_SHORTCUT_INTENT) ?: return
        val label = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            ?: context.getString(R.string.shortcut_default_label)
        val shortcutId = AppTile.LEGACY_SHORTCUT_PREFIX + shortcutIntent.toUri(Intent.URI_INTENT_SCHEME)

        val container = context.appContainer
        if (container.tileRepository.tiles.value.leafTiles().any { it.shortcutId == shortcutId }) return

        val iconUri = intent.parcelable<Bitmap>(Intent.EXTRA_SHORTCUT_ICON)?.let { bitmap ->
            runCatching { container.imageStore.saveShortcutIcon(bitmap).toString() }
                .onFailure { Log.w(TAG, "Could not store shortcut icon", it) }
                .getOrNull()
        }
        val tile = AppTile(
            packageName = shortcutIntent.`package` ?: shortcutIntent.component?.packageName.orEmpty(),
            activityName = "",
            label = label,
            shortcutId = shortcutId,
            iconUri = iconUri
        )
        if (container.tileRepository.addIfAbsent(tile) { it.shortcutId == shortcutId }) {
            Toast.makeText(context, context.getString(R.string.shortcut_added, label), Toast.LENGTH_SHORT).show()
        }
    }

    private inline fun <reified T> Intent.parcelable(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }

    companion object {
        const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
        private const val TAG = "LegacyShortcutReceiver"
    }
}
