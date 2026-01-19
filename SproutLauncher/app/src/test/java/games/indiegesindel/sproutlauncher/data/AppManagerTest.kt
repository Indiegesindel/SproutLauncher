package games.indiegesindel.sproutlauncher.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.model.AppTile
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class AppManagerTest {
    private lateinit var context: Context
    private lateinit var appManager: AppManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure clean SharedPreferences for each run
        context.getSharedPreferences("sprout_launcher_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        appManager = AppManager(context)
    }

    @Test
    fun add_and_get_AppTiles_persists() {
        val tile1 = AppTile(packageName = "com.example.a", activityName = "AActivity", label = "A")
        val tile2 = AppTile(packageName = "com.example.b", activityName = "BActivity", label = "B", iconUri = "file://icon.png")

        appManager.addAppTile(tile1)
        appManager.addAppTile(tile2)

        val result = appManager.getAppTiles()
        assertThat(result).hasSize(2)
        assertThat(result[0].packageName).isEqualTo("com.example.a")
        assertThat(result[1].iconUri).isEqualTo("file://icon.png")
    }

    @Test
    fun update_AppTile_replaces_matching_id_only() {
        val tile = AppTile(packageName = "com.example.app", activityName = "Main", label = "Old")
        appManager.addAppTile(tile)

        val updated = tile.copy(label = "New")
        appManager.updateAppTile(updated)

        val stored = appManager.getAppTiles()
        assertThat(stored).hasSize(1)
        assertThat(stored.first().label).isEqualTo("New")
    }

    @Test
    fun remove_AppTile_by_id() {
        val tile1 = AppTile(packageName = "pkg1", activityName = "A", label = "L1")
        val tile2 = AppTile(packageName = "pkg2", activityName = "B", label = "L2")
        appManager.addAppTile(tile1)
        appManager.addAppTile(tile2)

        appManager.removeAppTile(tile1.id)

        val result = appManager.getAppTiles()
        assertThat(result.map { it.id }).containsExactly(tile2.id)
    }

    @Test
    fun saveAppTiles_overwrites_existing_list() {
        val tile1 = AppTile(packageName = "pkg1", activityName = "A", label = "L1")
        appManager.addAppTile(tile1)

        val newList = listOf(
            AppTile(packageName = "pkg3", activityName = "C", label = "L3"),
            AppTile(packageName = "pkg4", activityName = "D", label = "L4")
        )
        appManager.saveAppTiles(newList)

        val result = appManager.getAppTiles()
        assertThat(result.map { it.packageName }).containsExactly("pkg3", "pkg4").inOrder()
    }

    @Test
    fun isPackageInstalled_usesPackageManager() {
        val pm = context.packageManager
        val shadowPm = Shadows.shadowOf(pm)
        shadowPm.addPackage("com.installed.app")

        assertThat(appManager.isPackageInstalled("com.installed.app")).isTrue()
        assertThat(appManager.isPackageInstalled("com.missing.app")).isFalse()
    }
}
