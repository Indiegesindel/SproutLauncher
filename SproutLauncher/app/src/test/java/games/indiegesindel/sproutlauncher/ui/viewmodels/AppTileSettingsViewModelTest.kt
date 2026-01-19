package games.indiegesindel.sproutlauncher.ui.viewmodels

import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AppTileSettingsViewModelTest {

    @Test
    fun init_loads_tile_and_exposes_fields() {
        val tile = AppTile(packageName = "pkg", activityName = "Act", label = "Label", iconUri = "uri")
        val appManager = mockk<AppManager>()
        every { appManager.getAppTiles() } returns listOf(tile)

        val vm = AppTileSettingsViewModel(appManager, tile.id)

        assert(vm.tile.value == tile)
        assert(vm.label.value == "Label")
        assert(vm.iconUri.value == "uri")
    }

    @Test
    fun saveChanges_updates_tile_via_manager() {
        val tile = AppTile(packageName = "pkg", activityName = "Act", label = "Label", iconUri = null)
        val appManager = mockk<AppManager>(relaxed = true)
        every { appManager.getAppTiles() } returns listOf(tile)

        val vm = AppTileSettingsViewModel(appManager, tile.id)
        vm.onLabelChanged("New")
        vm.onIconUriChanged("icon://new.png")
        vm.saveChanges()

        verify { appManager.updateAppTile(tile.copy(label = "New", iconUri = "icon://new.png")) }
    }

    @Test
    fun removeTile_calls_manager() {
        val tile = AppTile(packageName = "pkg", activityName = "Act", label = "Label")
        val appManager = mockk<AppManager>(relaxed = true)
        every { appManager.getAppTiles() } returns listOf(tile)

        val vm = AppTileSettingsViewModel(appManager, tile.id)
        vm.removeTile()

        verify { appManager.removeAppTile(tile.id) }
    }
}
