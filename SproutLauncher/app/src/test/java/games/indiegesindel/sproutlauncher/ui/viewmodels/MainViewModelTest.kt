package games.indiegesindel.sproutlauncher.ui.viewmodels

import games.indiegesindel.sproutlauncher.FocusedElement
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
        vm.apply { (this as MainViewModel).let {  } }
        // Directly mutate internal state for test visibility
        val field = MainViewModel::class.java.getDeclaredField("_appTiles")
        field.isAccessible = true
        val state = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<AppTile>>
        state.value = tiles

        vm.reorderAppTiles(0, 2)

        val expected = listOf(tiles[1], tiles[2], tiles[0])
        assert(vm.appTiles.value == expected)
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
    fun focus_logic_updates_correctly() {
        vm.onFocusChanged(true)
        assert(vm.focusedElement.value == FocusedElement.APP_TILE)
        vm.onFocusChanged(false)
        assert(vm.focusedElement.value == FocusedElement.NONE)

        vm.onQuickActionsFocusChanged(true)
        assert(vm.focusedElement.value == FocusedElement.QUICK_ACTION)
        vm.onQuickActionsFocusChanged(false)
        assert(vm.focusedElement.value == FocusedElement.NONE)

        vm.onFocusedItemIdChanged(null)
        assert(vm.focusedElement.value == FocusedElement.NONE)
        vm.onFocusedItemIdChanged("action:xyz")
        assert(vm.focusedElement.value == FocusedElement.QUICK_ACTION)
        vm.onFocusedItemIdChanged("tile:abc")
        assert(vm.focusedElement.value == FocusedElement.APP_TILE)
    }
}
