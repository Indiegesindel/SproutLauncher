package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile

class ShortcutHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) {
            val pinItemRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST, LauncherApps.PinItemRequest::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
            }

            if (pinItemRequest != null && pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                val shortcutInfo = pinItemRequest.shortcutInfo
                if (shortcutInfo != null) {
                    val label = shortcutInfo.shortLabel?.toString() 
                        ?: shortcutInfo.longLabel?.toString() 
                        ?: "Shortcut"
                    addShortcutToHome(shortcutInfo, label)
                    if (pinItemRequest.isValid) {
                        pinItemRequest.accept()
                        Toast.makeText(this, "Added $label to home screen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        finish()
    }

    private fun addShortcutToHome(shortcutInfo: ShortcutInfo, label: String) {
        val appManager = AppManager(this)
        
        // Check if it already exists to avoid duplicates
        val currentTiles = appManager.getAppTiles()
        val exists = currentTiles.any { 
            it.packageName == shortcutInfo.`package` && it.shortcutId == shortcutInfo.id 
        }
        
        if (!exists) {
            val newTile = AppTile(
                packageName = shortcutInfo.`package`,
                activityName = "", // Not used for shortcuts
                label = label,
                shortcutId = shortcutInfo.id
            )
            appManager.addAppTile(newTile)
        }
    }
}
