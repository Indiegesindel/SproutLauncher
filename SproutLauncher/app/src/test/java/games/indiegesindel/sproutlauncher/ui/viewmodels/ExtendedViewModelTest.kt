package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import io.mockk.every
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
class ExtendedViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var appManager: AppManager
    private lateinit var packageManager: PackageManager
    private lateinit var vm: ExtendedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appManager = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        vm = ExtendedViewModel(appManager, packageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadApps_sets_initial_focus_on_all_apps_tab() = runTest {
        val app = io.mockk.spyk(ResolveInfo())
        app.activityInfo = ActivityInfo().apply {
            packageName = "pkg.name"
            name = "ActivityName"
        }
        every { app.loadLabel(any()) } returns "AppLabel"
        
        every { packageManager.queryIntentActivities(any<Intent>(), 0) } returns listOf(app)

        vm.setTab(ExtendedViewModel.Tab.ALL)
        vm.loadApps()
        
        // Wait for the async load and focus logic to complete
        var count = 0
        while (vm.focusedItemId.value == null && count < 50) {
            kotlinx.coroutines.delay(20)
            count++
        }

        assert(vm.focusedItemId.value == "pkg.name_ActivityName")
        assert(vm.focusedElement.value == FocusedElement.APP_TILE)
    }

    @Test
    fun setTab_ALL_sets_focus_to_first_app() = runTest {
        val app = io.mockk.spyk(ResolveInfo())
        app.activityInfo = ActivityInfo().apply {
            packageName = "pkg.name"
            name = "ActivityName"
        }
        every { app.loadLabel(any()) } returns "AppLabel"

        every { packageManager.queryIntentActivities(any<Intent>(), 0) } returns listOf(app)
        vm.loadApps() // populate apps
        
        // Wait for apps to load
        var count = 0
        while (vm.installedApps.value.isEmpty() && count < 50) {
            kotlinx.coroutines.delay(20)
            count++
        }
        
        vm.setFocusedItemId(null) // clear focus
        
        vm.setTab(ExtendedViewModel.Tab.ALL)
        
        assert(vm.focusedItemId.value == "pkg.name_ActivityName")
        assert(vm.focusedElement.value == FocusedElement.APP_TILE)
    }
}
