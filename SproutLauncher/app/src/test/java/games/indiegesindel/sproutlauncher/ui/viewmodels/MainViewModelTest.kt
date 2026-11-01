package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.data.InstalledAppsRepository
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp
import games.indiegesindel.sproutlauncher.model.QuickApp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class MainViewModelTest {
    private lateinit var tiles: TileRepository
    private lateinit var settings: SettingsRepository
    private val installedApps = MutableStateFlow<List<InstalledApp>?>(emptyList())

    private val a = AppTile(packageName = "pkg.a", activityName = "A", label = "a")
    private val b = AppTile(packageName = "pkg.b", activityName = "B", label = "b")
    private val c = AppTile(packageName = "pkg.c", activityName = "C", label = "c")

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        tiles = TileRepository(context.getSharedPreferences("main_vm_tiles", Context.MODE_PRIVATE).also { it.edit().clear().commit() })
        settings = SettingsRepository(context.getSharedPreferences("main_vm_settings", Context.MODE_PRIVATE).also { it.edit().clear().commit() }, isPhone = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): MainViewModel {
        val installedAppsRepository = mockk<InstalledAppsRepository> { every { apps } returns installedApps }
        return MainViewModel(tiles, settings, installedAppsRepository)
    }

    @Test
    fun initialFocus_targetsFirstTile() {
        tiles.add(a)
        tiles.add(b)
        assertThat(viewModel().pendingFocusTileId.value).isEqualTo(a.id)
    }

    @Test
    fun initialFocus_waitsForFirstTile() {
        val vm = viewModel()
        assertThat(vm.pendingFocusTileId.value).isNull()

        tiles.add(b)

        assertThat(vm.pendingFocusTileId.value).isEqualTo(b.id)
    }

    @Test
    fun quickApps_requireInstallAndRespectToggles() {
        installedApps.value = listOf(
            InstalledApp(QuickApp.YOUTUBE.packageName, "Main", "YouTube"),
            InstalledApp(QuickApp.DISCORD.packageName, "Main", "Discord"),
            InstalledApp(QuickApp.PHOTOS.packageName, "Main", "Photos")
        )
        val vm = viewModel()
        assertThat(vm.quickApps.value).containsExactly(QuickApp.YOUTUBE, QuickApp.DISCORD, QuickApp.PHOTOS).inOrder()

        settings.showDiscord.set(false)

        assertThat(vm.quickApps.value).containsExactly(QuickApp.YOUTUBE, QuickApp.PHOTOS).inOrder()
    }

    @Test
    fun wallpaper_combinesUriAndDim() {
        val vm = viewModel()
        assertThat(vm.wallpaper.value).isNull()

        settings.wallpaperUri.set("file://wall")
        settings.wallpaperDim.set(0.5f)

        assertThat(vm.wallpaper.value).isEqualTo(Wallpaper("file://wall", 0.5f))
    }

    @Test
    fun groupSelectedTiles_createsGroupAndFocusesIt() {
        tiles.update { listOf(a, b, c) }
        val vm = viewModel()

        vm.enterMultiSelectMode(a.id)
        vm.toggleTileSelection(c.id)
        vm.groupSelectedTiles("Group")

        val group = tiles.tiles.value.last()
        assertThat(group.isGroup).isTrue()
        assertThat(group.label).isEqualTo("Group")
        assertThat(group.groupTiles).containsExactly(a, c).inOrder()
        assertThat(vm.isInMultiSelectMode.value).isFalse()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(group.id)
    }

    @Test
    fun toggleSelection_leavesMultiSelectWhenEmpty() {
        tiles.add(a)
        val vm = viewModel()

        vm.enterMultiSelectMode(a.id)
        vm.toggleTileSelection(a.id)

        assertThat(vm.isInMultiSelectMode.value).isFalse()
    }

    @Test
    fun enterMultiSelectMode_ignoresGroups() {
        val group = AppTile.group("G", listOf(a))
        tiles.add(group)
        val vm = viewModel()

        vm.enterMultiSelectMode(group.id)

        assertThat(vm.isInMultiSelectMode.value).isFalse()
    }

    @Test
    fun openAndCloseGroup_moveFocus() {
        val group = AppTile.group("G", listOf(a, b))
        tiles.add(group)
        val vm = viewModel()

        vm.openGroup(group)
        assertThat(vm.openedGroup.value).isEqualTo(group)
        assertThat(vm.pendingFocusTileId.value).isEqualTo(a.id)

        vm.closeGroup()
        assertThat(vm.openedGroup.value).isNull()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(group.id)
    }

    @Test
    fun openedGroup_reflectsRepositoryChanges() {
        val group = AppTile.group("G", listOf(a))
        tiles.add(group)
        val vm = viewModel()
        vm.openGroup(group)

        tiles.replace(a.copy(label = "renamed"))

        assertThat(vm.openedGroup.value?.groupTiles?.single()?.label).isEqualTo("renamed")
    }

    @Test
    fun ungroup_focusesFirstChild() {
        val group = AppTile.group("G", listOf(b, c))
        tiles.update { listOf(a, group) }
        val vm = viewModel()

        vm.ungroup(group.id)

        assertThat(tiles.tiles.value).containsExactly(a, b, c).inOrder()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(b.id)
    }

    @Test
    fun removeTileFromGroup_closesDissolvedGroupAndFocusesRemovedTile() {
        val group = AppTile.group("G", listOf(a, b))
        tiles.add(group)
        val vm = viewModel()
        vm.openGroup(group)

        vm.removeTileFromGroup(group.id, a.id)

        assertThat(vm.openedGroup.value).isNull()
        assertThat(tiles.tiles.value).containsExactly(b, a).inOrder()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(a.id)
    }

    @Test
    fun removeTileFromGroup_keepsGroupOpenWhenTilesRemain() {
        val group = AppTile.group("G", listOf(a, b, c))
        tiles.add(group)
        val vm = viewModel()
        vm.openGroup(group)

        vm.removeTileFromGroup(group.id, a.id)

        assertThat(vm.openedGroup.value?.groupTiles).containsExactly(b, c).inOrder()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(b.id)
    }

    @Test
    fun moveTileToGroup_movesAndFocusesGroup() {
        val group = AppTile.group("G", listOf(b))
        tiles.update { listOf(a, group) }
        val vm = viewModel()
        vm.requestMoveToGroup(a)

        vm.moveTileToGroup(a.id, group.id)

        assertThat(tiles.tiles.value.single().groupTiles).containsExactly(b, a).inOrder()
        assertThat(vm.tileToMoveToGroup.value).isNull()
        assertThat(vm.pendingFocusTileId.value).isEqualTo(group.id)
    }

    @Test
    fun confirmRemoveTile_removesFromRepository() {
        tiles.update { listOf(a, b) }
        val vm = viewModel()

        vm.requestRemoveTile(a)
        vm.confirmRemoveTile()

        assertThat(tiles.tiles.value).containsExactly(b)
        assertThat(vm.tileToRemove.value).isNull()
    }

    @Test
    fun reorderTiles_persists() {
        tiles.update { listOf(a, b, c) }
        viewModel().reorderTiles(0, 2)
        assertThat(tiles.tiles.value).containsExactly(b, c, a).inOrder()
    }

    @Test
    fun resetState_closesEverything() {
        val group = AppTile.group("G", listOf(a))
        tiles.update { listOf(group, b) }
        val vm = viewModel()
        vm.openGroup(group)
        vm.enterMultiSelectMode(b.id)
        vm.requestMoveToGroup(b)
        vm.openAllApps()

        vm.resetState()

        assertThat(vm.openedGroup.value).isNull()
        assertThat(vm.isInMultiSelectMode.value).isFalse()
        assertThat(vm.selectedTileIds.value).isEmpty()
        assertThat(vm.tileToMoveToGroup.value).isNull()
        assertThat(vm.showAllApps.value).isFalse()
    }
}
