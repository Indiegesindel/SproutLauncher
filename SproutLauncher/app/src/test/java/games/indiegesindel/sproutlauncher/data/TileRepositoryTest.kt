package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.model.AppTile
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TileRepositoryTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("tile_repository_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun tiles_roundTripThroughPreferences() {
        val shortcut = AppTile(
            packageName = "com.example.b", activityName = "", label = "B",
            iconUri = "file://icon.png", shortcutId = "shortcut123"
        )
        val group = AppTile.group("Games", listOf(AppTile(packageName = "com.example.c", activityName = "C", label = "C")))
        TileRepository(prefs).apply {
            add(AppTile(packageName = "com.example.a", activityName = "AActivity", label = "A"))
            add(shortcut)
            add(group)
        }

        val reloaded = TileRepository(prefs).tiles.value
        assertThat(reloaded).hasSize(3)
        assertThat(reloaded[1]).isEqualTo(shortcut)
        assertThat(reloaded[2]).isEqualTo(group)
    }

    @Test
    fun corruptJson_startsEmpty_andKeepsBackup() {
        prefs.edit().putString("app_tiles", "{not json").commit()

        val repository = TileRepository(prefs)

        assertThat(repository.tiles.value).isEmpty()
        assertThat(prefs.getString("app_tiles_corrupt_backup", null)).isEqualTo("{not json")
    }

    @Test
    fun malformedEntry_isSkipped() {
        prefs.edit().putString(
            "app_tiles",
            """[{"packageName":"no.id"},{"id":"1","packageName":"ok","activityName":"A","label":"Ok"}]"""
        ).commit()

        val tiles = TileRepository(prefs).tiles.value

        assertThat(tiles.map { it.id }).containsExactly("1")
    }

    @Test
    fun addIfAbsent_detectsDuplicatesInsideGroups() {
        val nested = AppTile(packageName = "pkg", activityName = "A", label = "A")
        val repository = TileRepository(prefs).apply { add(AppTile.group("G", listOf(nested))) }

        val added = repository.addIfAbsent(nested.copy(id = "other")) { it.matchesActivity("pkg", "A") }

        assertThat(added).isFalse()
        assertThat(repository.tiles.value).hasSize(1)
    }

    @Test
    fun removePackage_persists() {
        val repository = TileRepository(prefs).apply {
            add(AppTile(packageName = "keep", activityName = "A", label = "Keep"))
            add(AppTile(packageName = "drop", activityName = "A", label = "Drop"))
        }

        repository.removePackage("drop")

        assertThat(TileRepository(prefs).tiles.value.map { it.packageName }).containsExactly("keep")
    }

    @Test
    fun replace_updatesNestedTile() {
        val nested = AppTile(packageName = "pkg", activityName = "A", label = "Old")
        val repository = TileRepository(prefs).apply { add(AppTile.group("G", listOf(nested))) }

        repository.replace(nested.copy(label = "New"))

        assertThat(repository.find(nested.id)?.label).isEqualTo("New")
    }
}
