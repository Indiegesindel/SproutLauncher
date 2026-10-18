package games.indiegesindel.sproutlauncher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp

object AppLauncher {
    private const val TAG = "AppLauncher"

    fun launch(context: Context, tile: AppTile) = runOrToast(context, R.string.error_launch, tile.label) {
        val legacyUri = tile.legacyIntentUri
        when {
            legacyUri != null -> context.startActivity(parseLegacyShortcut(legacyUri))
            tile.shortcutId != null -> context.launcherApps.startShortcut(
                tile.packageName, tile.shortcutId, null, null, Process.myUserHandle()
            )
            tile.activityName.isBlank() -> startPackage(context, tile.packageName)
            else -> startActivity(context, tile.packageName, tile.activityName)
        }
    }

    fun launch(context: Context, app: InstalledApp) = runOrToast(context, R.string.error_launch, app.label) {
        startActivity(context, app.packageName, app.activityName)
    }

    fun launchPackage(context: Context, packageName: String) =
        runOrToast(context, R.string.error_launch, packageName) { startPackage(context, packageName) }

    fun openAppDetails(context: Context, packageName: String) = runOrToast(context, R.string.error_open_app_details) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun requestUninstall(context: Context, packageName: String) = runOrToast(context, R.string.error_uninstall) {
        context.startActivity(
            Intent(Intent.ACTION_DELETE, "package:$packageName".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openBrowser(context: Context) = runOrToast(context, R.string.error_open_browser) {
        context.startActivity(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openSystemSettings(context: Context) = runOrToast(context, R.string.error_open_settings) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun startActivity(context: Context, packageName: String, activityName: String) {
        context.launcherApps.startMainActivity(
            ComponentName(packageName, activityName), Process.myUserHandle(), null, null
        )
    }

    private fun startPackage(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: error("$packageName has no launch intent")
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun parseLegacyShortcut(uri: String): Intent =
        Intent.parseUri(uri, Intent.URI_INTENT_SCHEME).apply {
            selector = null
            flags = (flags and URI_GRANT_FLAGS.inv()) or Intent.FLAG_ACTIVITY_NEW_TASK
        }

    private inline fun runOrToast(context: Context, @StringRes message: Int, vararg args: Any, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.w(TAG, "Launch failed", e)
            Toast.makeText(context, context.getString(message, *args), Toast.LENGTH_SHORT).show()
        }
    }

    private val Context.launcherApps: LauncherApps
        get() = getSystemService(LauncherApps::class.java)

    private const val URI_GRANT_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
}
