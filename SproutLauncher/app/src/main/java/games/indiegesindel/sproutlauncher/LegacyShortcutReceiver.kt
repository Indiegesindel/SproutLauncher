package games.indiegesindel.sproutlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.FileUtils
import java.util.UUID

class LegacyShortcutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.android.launcher.action.INSTALL_SHORTCUT") return

        val shortcutIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT)
        } ?: return

        val label = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: "Shortcut"
        
        // Handle Icon
        var iconUri: String? = null
        val iconBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        }

        if (iconBitmap != null) {
            val fileName = "shortcut_icon_${UUID.randomUUID()}.png"
            val file = context.getFileStreamPath(fileName)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            iconUri = Uri.fromFile(file).toString()
        }
        // ICON_RESOURCE isn't handled (it's a resource from another app).

        val appManager = AppManager(context)
        val intentUri = shortcutIntent.toUri(Intent.URI_INTENT_SCHEME)
        
        // Check for duplicates
        val exists = appManager.getAppTiles().any { it.shortcutId == "legacy:$intentUri" }
        if (!exists) {
            val packageName = shortcutIntent.`package` ?: shortcutIntent.component?.packageName ?: ""
            val newTile = AppTile(
                packageName = packageName,
                activityName = "",
                label = label,
                shortcutId = "legacy:$intentUri",
                iconUri = iconUri
            )
            appManager.addAppTile(newTile)
            Toast.makeText(context, "Added $label to home screen", Toast.LENGTH_SHORT).show()
        }
    }
}
