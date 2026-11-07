package games.indiegesindel.sproutlauncher.utils

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.compose.ui.geometry.Rect as ComposeRect
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp
import kotlin.math.roundToInt

class LaunchOrigin(val view: View, val bounds: ComposeRect)

object AppLauncher {
    private const val TAG = "AppLauncher"

    fun launch(context: Context, tile: AppTile, origin: LaunchOrigin? = null) = runOrToast(context, R.string.error_launch, tile.label) {
        val legacyUri = tile.legacyIntentUri
        val (sourceBounds, opts) = origin.animation()
        when {
            legacyUri != null -> context.startActivity(parseLegacyShortcut(legacyUri).also { it.sourceBounds = sourceBounds }, opts)
            tile.shortcutId != null -> context.launcherApps.startShortcut(
                tile.packageName, tile.shortcutId, sourceBounds, opts, Process.myUserHandle()
            )
            tile.activityName.isBlank() -> startPackage(context, tile.packageName, sourceBounds, opts)
            else -> startActivity(context, tile.packageName, tile.activityName, sourceBounds, opts)
        }
    }.also { if (it) context.appContainer.menuMusic.duck() }

    fun launch(context: Context, app: InstalledApp, origin: LaunchOrigin? = null) = runOrToast(context, R.string.error_launch, app.label) {
        val (sourceBounds, opts) = origin.animation()
        startActivity(context, app.packageName, app.activityName, sourceBounds, opts)
    }.also { if (it) context.appContainer.menuMusic.duck() }

    fun launchPackage(context: Context, packageName: String, origin: LaunchOrigin? = null) =
        runOrToast(context, R.string.error_launch, packageName) {
            val (sourceBounds, opts) = origin.animation()
            startPackage(context, packageName, sourceBounds, opts)
        }.also { if (it) context.appContainer.menuMusic.duck() }

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

    fun openUrl(context: Context, url: String) = runOrToast(context, R.string.error_open_browser) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun openSystemSettings(context: Context) = runOrToast(context, R.string.error_open_settings) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun startActivity(context: Context, packageName: String, activityName: String, sourceBounds: Rect?, opts: Bundle?) {
        context.launcherApps.startMainActivity(
            ComponentName(packageName, activityName), Process.myUserHandle(), sourceBounds, opts
        )
    }

    private fun startPackage(context: Context, packageName: String, sourceBounds: Rect? = null, opts: Bundle? = null) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: error("$packageName has no launch intent")
        intent.sourceBounds = sourceBounds
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), opts)
    }

    private fun LaunchOrigin?.animation(): Pair<Rect?, Bundle?> {
        if (this == null || bounds.isEmpty) return null to null
        val x = bounds.left.roundToInt()
        val y = bounds.top.roundToInt()
        val w = bounds.width.roundToInt()
        val h = bounds.height.roundToInt()
        val location = IntArray(2).also(view::getLocationOnScreen)
        val onScreen = Rect(location[0] + x, location[1] + y, location[0] + x + w, location[1] + y + h)
        return onScreen to ActivityOptions.makeClipRevealAnimation(view, x, y, w, h).toBundle()
    }

    private fun parseLegacyShortcut(uri: String): Intent =
        Intent.parseUri(uri, Intent.URI_INTENT_SCHEME).apply {
            selector = null
            flags = (flags and URI_GRANT_FLAGS.inv()) or Intent.FLAG_ACTIVITY_NEW_TASK
        }

    private inline fun runOrToast(context: Context, @StringRes message: Int, vararg args: Any, block: () -> Unit): Boolean =
        try {
            block()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Launch failed", e)
            Toast.makeText(context, context.getString(message, *args), Toast.LENGTH_SHORT).show()
            false
        }

    private val Context.launcherApps: LauncherApps
        get() = getSystemService(LauncherApps::class.java)

    private const val URI_GRANT_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
}
