package games.indiegesindel.sproutlauncher.ui.viewmodels

import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.model.AppTile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class MainViewModelTest {
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
    fun loadAppTiles_sets_initial_focus() = runTest(testDispatcher) {
        val tiles = listOf(
            AppTile(packageName = "pkg1", activityName = "A", label = "L1")
        )
        every { appManager.getAppTiles() } returns tiles

        vm.loadAppTiles()

        assert(vm.pendingFocus.value == "tile:${tiles[0].id}")
    }

    @Test
    fun loadAppTiles_requests_no_focus_when_no_tiles() = runTest(testDispatcher) {
        every { appManager.getAppTiles() } returns emptyList()

        vm.loadAppTiles()

        // No tiles -> nothing to focus; the quick-actions bar becomes the natural first
        // focus target via Compose's own focus search.
        assert(vm.pendingFocus.value == null)
    }

    @Test
    fun loadAppTiles_populates_list_and_stops_loading_and_checks_quick_actions() = runTest(testDispatcher) {
        val tiles = listOf(
            AppTile(packageName = "pkg1", activityName = "A", label = "L1"),
            AppTile(packageName = "pkg2", activityName = "B", label = "L2")
        )
        every { appManager.getAppTiles() } returns tiles
        every { appManager.isPackageInstalled(any()) } returns false
        every { appManager.isPackageInstalled("com.discord") } returns true

        vm.loadAppTiles()

        assert(vm.appTiles.value == tiles)
        assert(vm.isLoading.value == false)
        // Should contain five quick actions keys
        val quick = vm.installedQuickActions.value
        assert(quick.containsKey("com.discord"))
        assert(quick.size == 5)
    }

    @Test
    fun removeAppTile_delegates_and_reload() = runTest(testDispatcher) {
        every { appManager.getAppTiles() } returns emptyList()
        every { appManager.removeAppTile(any()) } returns Unit

        vm.removeAppTile("id")

        verify { appManager.removeAppTile("id") }
    }

    @Test
    fun reorderAppTiles_moves_items_when_indices_valid() {
        val tiles = listOf(
            AppTile(packageName = "pkg1", activityName = "A", label = "1"),
            AppTile(packageName = "pkg2", activityName = "B", label = "2"),
            AppTile(packageName = "pkg3", activityName = "C", label = "3")
        )
        // Seed current state
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        vm.reorderAppTiles(0, 2)

        val expected = listOf(tiles[1], tiles[2], tiles[0])
        assert(vm.appTiles.value == expected)
    }

    @Test
    fun reorderGroupTiles_moves_items_within_group() {
        val groupTiles = listOf(
            AppTile(packageName = "p1", activityName = "A1", label = "L1"),
            AppTile(packageName = "p2", activityName = "A2", label = "L2")
        )
        val group = AppTile(id = "group1", packageName = "", activityName = "", label = "G", isGroup = true, groupTiles = groupTiles)
        val tiles = listOf(group)

        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        vm.reorderGroupTiles("group1", 0, 1)

        val updatedGroup = vm.appTiles.value[0]
        assert(updatedGroup.groupTiles[0].packageName == "p2")
        assert(updatedGroup.groupTiles[1].packageName == "p1")
    }

    @Test
    fun saveAppTiles_calls_manager_with_current_list() {
        val tiles = listOf(AppTile(packageName = "pkg1", activityName = "A", label = "1"))
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        every { appManager.saveAppTiles(any()) } returns Unit
        vm.saveAppTiles()

        verify { appManager.saveAppTiles(tiles) }
    }

    @Test
    fun loadAppTiles_maintains_openedGroup_if_exists() = runTest(testDispatcher) {
        val group = AppTile(id = "g1", packageName = "", activityName = "", label = "Old Label", isGroup = true)
        val updatedGroup = group.copy(label = "New Label")
        
        val field = MainViewModel::class.java.getDeclaredField("_openedGroup")
        field.isAccessible = true
        val openedGroupState = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<AppTile?>
        openedGroupState.value = group
        
        every { appManager.getAppTiles() } returns listOf(updatedGroup)
        
        vm.loadAppTiles()
        
        assert(vm.openedGroup.value?.label == "New Label")
    }

    @Test
    fun loadAppTiles_places_initial_focus_only_once() = runTest(testDispatcher) {
        val tiles = listOf(AppTile(id = "t1", packageName = "p", activityName = "a", label = "l"))
        every { appManager.getAppTiles() } returns tiles

        vm.loadAppTiles()
        assert(vm.pendingFocus.value == "tile:t1")

        // Simulate the UI consuming the one-shot focus request, then reload (e.g. on resume
        // or a package change). Initial focus must not be re-placed.
        vm.consumePendingFocus()
        vm.loadAppTiles()
        assert(vm.pendingFocus.value == null)
    }

    @Test
    fun removeSelectedTiles_removes_tiles_and_clears_selection() = runTest(testDispatcher) {
        val tile1 = AppTile(id = "1", packageName = "p1", activityName = "a1", label = "l1")
        val tile2 = AppTile(id = "2", packageName = "p2", activityName = "a2", label = "l2")
        val tile3 = AppTile(id = "3", packageName = "p3", activityName = "a3", label = "l3")
        val tiles = listOf(tile1, tile2, tile3)

        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        vm.enterMultiSelectMode("1")
        vm.toggleTileSelection("2")

        vm.removeSelectedTiles()

        assert(vm.appTiles.value == listOf(tile3))
        assert(vm.selectedTileIds.value.isEmpty())
        assert(vm.isInMultiSelectMode.value == false)
        verify { appManager.saveAppTiles(listOf(tile3)) }
    }

    @Test
    fun multiSelectMode_prevents_selecting_groups() = runTest(testDispatcher) {
        val tile1 = AppTile(id = "1", packageName = "p1", activityName = "a1", label = "l1")
        val group = AppTile(id = "g1", packageName = "", activityName = "", label = "G", isGroup = true)
        val tiles = listOf(tile1, group)

        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        // Try entering with group
        vm.enterMultiSelectMode("g1")
        assert(vm.isInMultiSelectMode.value == false)

        // Enter with regular tile
        vm.enterMultiSelectMode("1")
        assert(vm.isInMultiSelectMode.value == true)
        assert(vm.selectedTileIds.value == setOf("1"))

        // Try toggling group
        vm.toggleTileSelection("g1")
        assert(vm.selectedTileIds.value == setOf("1"))
    }
}
