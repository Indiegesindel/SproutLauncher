package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.data.AppManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LegacyShortcutReceiverTest {
    private lateinit var context: Context
    private lateinit var appManager: AppManager
    private lateinit var receiver: LegacyShortcutReceiver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("sprout_launcher_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        appManager = AppManager(context)
        receiver = LegacyShortcutReceiver()
    }

    @Test
    fun onReceive_validLegacyShortcut_addsTile() {
        val targetIntent = Intent(Intent.ACTION_VIEW).apply {
            `package` = "com.example.app"
            data = android.net.Uri.parse("rom://path/to/rom")
        }
        val installIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(Intent.EXTRA_SHORTCUT_NAME, "My ROM")
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent)
        }

        receiver.onReceive(context, installIntent)

        val tiles = appManager.getAppTiles()
        assertThat(tiles).hasSize(1)
        val tile = tiles[0]
        assertThat(tile.label).isEqualTo("My ROM")
        assertThat(tile.packageName).isEqualTo("com.example.app")
        assertThat(tile.shortcutId).startsWith("legacy:intent:")
        assertThat(tile.shortcutId).contains("path/to/rom")
        assertThat(tile.shortcutId).contains("scheme=rom")
    }

    @Test
    fun onReceive_duplicateLegacyShortcut_doesNotAddTile() {
        val targetIntent = Intent(Intent.ACTION_VIEW).apply {
            `package` = "com.example.app"
        }
        val installIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(Intent.EXTRA_SHORTCUT_NAME, "My ROM")
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent)
        }

        receiver.onReceive(context, installIntent)
        receiver.onReceive(context, installIntent)

        val tiles = appManager.getAppTiles()
        assertThat(tiles).hasSize(1)
    }
}
