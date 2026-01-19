package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
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
import org.robolectric.Shadows

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ExtendedViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var context: Context
    private lateinit var appManager: AppManager
    private lateinit var vm: ExtendedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        appManager = mockk(relaxed = true)
        val pm = context.packageManager
        vm = ExtendedViewModel(appManager, pm)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadApps_sorts_by_label_case_insensitive() = runTest(testDispatcher) {
        val pm = context.packageManager
        val shadowPm = Shadows.shadowOf(pm)
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

        fun ri(pkg: String, cls: String, label: String): ResolveInfo {
            val r = ResolveInfo()
            val ai = ActivityInfo()
            ai.packageName = pkg
            ai.name = cls
            r.activityInfo = ai
            r.nonLocalizedLabel = label
            return r
        }
        val riB = ri("com.b", "B", "beta")
        val riA = ri("com.a", "A", "Alpha")
        shadowPm.addResolveInfoForIntent(intent, riB)
        shadowPm.addResolveInfoForIntent(intent, riA)

        vm.loadApps()

        val labels = vm.installedApps.value.map { it.loadLabel(pm).toString() }
        // Expect Alpha comes before beta (case-insensitive)
        assert(labels == listOf("Alpha", "beta"))
        assert(vm.isLoadingApps.value == false)
    }

    @Test
    fun tiles_loading_and_remove_flow() = runTest(testDispatcher) {
        val tiles = listOf(AppTile(packageName = "pkg", activityName = "A", label = "L"))
        every { appManager.getAppTiles() } returns tiles

        vm.loadTiles()
        assert(vm.selectedTiles.value == tiles)

        val tile = tiles.first()
        vm.requestRemoveTile(tile)
        assert(vm.tileToRemove.value == tile)
        vm.dismissRemoveConfirmation()
        assert(vm.tileToRemove.value == null)

        vm.requestRemoveTile(tile)
        every { appManager.removeAppTile(tile.id) } returns Unit
        every { appManager.getAppTiles() } returns emptyList()
        vm.confirmRemoveTile()
        verify { appManager.removeAppTile(tile.id) }
        assert(vm.selectedTiles.value.isEmpty())
        assert(vm.tileToRemove.value == null)
    }

    @Test
    fun filter_and_focus_behaviors() {
        vm.setFilter(ExtendedViewModel.Filter.HOMESCREEN)
        assert(vm.currentFilter.value == ExtendedViewModel.Filter.HOMESCREEN)
        assert(vm.focusedItemId.value == null)

        vm.setFocusedItemId("item1")
        assert(vm.focusedItemId.value == "item1")

        vm.onFocusChanged(FocusedElement.FILTER)
        assert(vm.focusedElement.value == FocusedElement.FILTER)
    }
}
