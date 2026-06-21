package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.widget.Toast
import games.indiegesindel.sproutlauncher.model.AppTile

object LauncherUtils {
    fun launchTile(context: Context, tile: AppTile) {
        try {
            if (tile.shortcutId != null) {
                if (tile.shortcutId.startsWith("legacy:")) {
                    val intentUri = tile.shortcutId.removePrefix("legacy:")
                    val intent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    val launcherApps =
                        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                    launcherApps.startShortcut(
                        tile.packageName,
                        tile.shortcutId,
                        null,
                        null,
                        Process.myUserHandle()
                    )
                }
            } else {
                val intent = Intent().apply {
                    setClassName(tile.packageName, tile.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Could not launch ${tile.label}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Could not launch $packageName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch $packageName", Toast.LENGTH_SHORT).show()
        }
    }
}
