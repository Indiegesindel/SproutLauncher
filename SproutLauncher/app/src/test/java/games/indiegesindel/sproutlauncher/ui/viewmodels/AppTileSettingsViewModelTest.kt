package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppTileSettingsViewModelTest {
    private lateinit var tiles: TileRepository
    private lateinit var settings: SettingsRepository
    private val shortcut = AppTile(packageName = "pkg", activityName = "", label = "Label", iconUri = "file://old", shortcutId = "s1")

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        tiles = TileRepository(context.getSharedPreferences("tile_settings_tiles", Context.MODE_PRIVATE).also { it.edit().clear().commit() })
        settings = SettingsRepository(context.getSharedPreferences("tile_settings_settings", Context.MODE_PRIVATE).also { it.edit().clear().commit() }, shortScreen = false)
        tiles.add(shortcut)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(tileId: String = shortcut.id) = AppTileSettingsViewModel(tiles, mockk<ImageStore>(), settings, tileId)

    @Test
    fun init_exposesTileFields() {
        val vm = viewModel()
        assertThat(vm.tile).isEqualTo(shortcut)
        assertThat(vm.label.value).isEqualTo("Label")
        assertThat(vm.iconUri.value).isEqualTo("file://old")
    }

    @Test
    fun missingTile_isNull() {
        assertThat(viewModel("unknown").tile).isNull()
    }

    @Test
    fun saveChanges_persistsLabelAndIcon_preservingOtherFields() {
        val vm = viewModel()
        vm.onLabelChanged("  New  ")
        vm.onIconUriChanged(null)

        vm.saveChanges()

        assertThat(tiles.find(shortcut.id)).isEqualTo(shortcut.copy(label = "New", iconUri = null))
    }

    @Test
    fun saveChanges_keepsOldLabelWhenBlank() {
        val vm = viewModel()
        vm.onLabelChanged("   ")

        vm.saveChanges()

        assertThat(tiles.find(shortcut.id)?.label).isEqualTo("Label")
    }

    @Test
    fun saveChanges_doesNotRevertConcurrentMoveIntoGroup() {
        val vm = viewModel()
        tiles.update { listOf(AppTile.group("G", it)) }
        vm.onLabelChanged("Renamed")

        vm.saveChanges()

        val group = tiles.tiles.value.single()
        assertThat(group.groupTiles.single().label).isEqualTo("Renamed")
    }

    @Test
    fun removeTile_removesFromRepository() {
        viewModel().removeTile()
        assertThat(tiles.tiles.value).isEmpty()
    }

    @Test
    fun steamGridDbAvailability_followsApiKey() {
        val vm = viewModel()
        assertThat(vm.isSteamGridDbAvailable.value).isFalse()

        settings.steamGridDbApiKey.set("key")

        assertThat(vm.isSteamGridDbAvailable.value).isTrue()
    }
}
