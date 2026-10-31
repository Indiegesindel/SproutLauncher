package games.indiegesindel.sproutlauncher

import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import games.indiegesindel.sproutlauncher.model.AppTile

class ShortcutHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePinRequest()
        finish()
    }

    private fun handlePinRequest() {
        if (intent?.action != LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) return
        val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST, LauncherApps.PinItemRequest::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
        }
        if (request == null || request.requestType != LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) return
        val shortcut = request.shortcutInfo ?: return

        if (!request.isValid || !request.accept()) return

        val label = shortcut.shortLabel?.toString() ?: shortcut.longLabel?.toString() ?: getString(R.string.shortcut_default_label)
        val tile = AppTile(packageName = shortcut.`package`, activityName = "", label = label, shortcutId = shortcut.id)
        appContainer.tileRepository.addIfAbsent(tile) {
            it.packageName == shortcut.`package` && it.shortcutId == shortcut.id
        }
        Toast.makeText(this, getString(R.string.shortcut_added, label), Toast.LENGTH_SHORT).show()
    }
}
