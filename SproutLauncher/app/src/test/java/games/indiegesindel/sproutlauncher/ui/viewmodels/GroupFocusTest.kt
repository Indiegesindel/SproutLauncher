package games.indiegesindel.sproutlauncher.ui.viewmodels

import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.model.AppTile
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GroupFocusTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var appManager: AppManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var vm: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appManager = mockk(relaxed = true)
        settingsManager = mockk(relaxed = true)
        vm = MainViewModel(appManager, settingsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun openGroup_sets_focus_to_first_tile_in_group() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val tile2 = AppTile(packageName = "p2", activityName = "a2", label = "l2")
        val groupTile = AppTile(
            label = "Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = listOf(tile1, tile2)
        )

        vm.openGroup(groupTile)

        assert(vm.openedGroup.value == groupTile)
        // Current behavior might not set focus yet, this test is expected to fail or demonstrate current state
        assert(vm.focusedItemId.value == "tile:${tile1.id}")
    }

    @Test
    fun closeGroup_returns_focus_to_group_tile() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val groupTile = AppTile(
            label = "Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = listOf(tile1)
        )

        vm.openGroup(groupTile)
        vm.onFocusedItemIdChanged("tile:${tile1.id}")
        
        vm.closeGroup()

        assert(vm.openedGroup.value == null)
        assert(vm.focusedItemId.value == "tile:${groupTile.id}")
    }

    @Test
    fun groupSelectedTiles_focuses_new_group_tile() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val tiles = listOf(tile1)
        
        // Use reflection to set internal tiles
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        (field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>).value = tiles

        vm.enterMultiSelectMode(tile1.id)
        vm.groupSelectedTiles()

        val appTiles = vm.appTiles.value
        assert(appTiles.size == 1)
        val newGroup = appTiles[0]
        assert(newGroup.isGroup)
        assert(vm.focusedItemId.value == "tile:${newGroup.id}")
    }

    @Test
    fun ungroup_focuses_first_ungrouped_tile() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val groupTile = AppTile(
            label = "Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = listOf(tile1)
        )
        
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        (field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>).value = listOf(groupTile)

        vm.ungroup(groupTile.id)

        assert(vm.appTiles.value.size == 1)
        assert(vm.appTiles.value[0].id == tile1.id)
        assert(vm.focusedItemId.value == "tile:${tile1.id}")
    }

    @Test
    fun removeLastTileFromGroup_ungroups_and_focuses_removed_tile() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val groupTile = AppTile(
            label = "Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = listOf(tile1)
        )
        
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        (field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>).value = listOf(groupTile)

        vm.openGroup(groupTile)
        vm.removeTileFromGroup(groupTile.id, tile1.id)

        assert(vm.openedGroup.value == null)
        assert(vm.appTiles.value.size == 1)
        assert(vm.appTiles.value[0].id == tile1.id)
        assert(vm.focusedItemId.value == "tile:${tile1.id}")
    }
    @Test
    fun removeTileFromGroup_ungroups_when_only_one_left() = runTest(testDispatcher) {
        val tile1 = AppTile(packageName = "p1", activityName = "a1", label = "l1")
        val tile2 = AppTile(packageName = "p2", activityName = "a2", label = "l2")
        val groupTile = AppTile(
            label = "Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = listOf(tile1, tile2)
        )
        
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        (field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>).value = listOf(groupTile)

        vm.openGroup(groupTile)
        // Focus on tile1
        vm.onFocusedItemIdChanged("tile:${tile1.id}")
        
        // Remove tile1, tile2 is left
        vm.removeTileFromGroup(groupTile.id, tile1.id)

        assert(vm.openedGroup.value == null)
        assert(vm.appTiles.value.size == 2)
        // They should be in some order. tile2 was moved out, then tile1 was added.
        assert(vm.appTiles.value.any { it.id == tile1.id })
        assert(vm.appTiles.value.any { it.id == tile2.id })
        assert(vm.focusedItemId.value == "tile:${tile1.id}")
    }
}
