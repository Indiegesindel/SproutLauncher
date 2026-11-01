package games.indiegesindel.sproutlauncher

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.model.AppTile
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class LegacyShortcutReceiverTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tiles get() = context.appContainer.tileRepository.tiles.value

    private fun installIntent(target: Intent, name: String? = "My ROM") =
        Intent(LegacyShortcutReceiver.ACTION_INSTALL_SHORTCUT).apply {
            name?.let { putExtra(Intent.EXTRA_SHORTCUT_NAME, it) }
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, target)
        }

    @Test
    fun validShortcut_addsTile() {
        val target = Intent(Intent.ACTION_VIEW, "rom://path/to/rom".toUri()).setPackage("com.example.app")

        LegacyShortcutReceiver().onReceive(context, installIntent(target))

        val tile = tiles.single()
        assertThat(tile.label).isEqualTo("My ROM")
        assertThat(tile.packageName).isEqualTo("com.example.app")
        assertThat(tile.isLegacyShortcut).isTrue()
        assertThat(tile.shortcutId).startsWith("${AppTile.LEGACY_SHORTCUT_PREFIX}intent:")
        assertThat(tile.shortcutId).contains("path/to/rom")
        assertThat(tile.shortcutId).contains("scheme=rom")
    }

    @Test
    fun duplicateShortcut_isIgnored() {
        val install = installIntent(Intent(Intent.ACTION_VIEW).setPackage("com.example.app"))

        LegacyShortcutReceiver().onReceive(context, install)
        LegacyShortcutReceiver().onReceive(context, install)

        assertThat(tiles).hasSize(1)
    }

    @Test
    fun missingName_usesDefaultLabel() {
        LegacyShortcutReceiver().onReceive(context, installIntent(Intent(Intent.ACTION_VIEW), name = null))

        assertThat(tiles.single().label).isEqualTo(context.getString(R.string.shortcut_default_label))
    }

    @Test
    fun otherActions_areIgnored() {
        LegacyShortcutReceiver().onReceive(context, Intent("some.other.ACTION"))

        assertThat(tiles).isEmpty()
    }
}
