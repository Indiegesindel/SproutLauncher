package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import games.indiegesindel.sproutlauncher.data.InstalledAppsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp
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
class AllAppsViewModelTest {
    private lateinit var tiles: TileRepository
    private val installedApps = MutableStateFlow<List<InstalledApp>?>(null)
    private lateinit var vm: AllAppsViewModel

    private val alpha = InstalledApp("pkg.alpha", "Main", "Alpha")
    private val beta = InstalledApp("pkg.beta", "Main", "Beta")

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("all_apps_vm", Context.MODE_PRIVATE).also { it.edit().clear().commit() }
        tiles = TileRepository(prefs)
        vm = AllAppsViewModel(tiles, mockk<InstalledAppsRepository> { every { apps } returns installedApps })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isLoading_untilAppsArrive() {
        assertThat(vm.uiState.value.isLoading).isTrue()
        installedApps.value = listOf(alpha)
        assertThat(vm.uiState.value.isLoading).isFalse()
    }

    @Test
    fun allTab_listsEveryApp_withHomeTileWhenPinnedAnywhere() {
        val pinned = AppTile(packageName = alpha.packageName, activityName = alpha.activityName, label = "Custom")
        tiles.add(AppTile.group("G", listOf(pinned)))
        installedApps.value = listOf(alpha, beta)

        val apps = vm.uiState.value.apps
        assertThat(apps.map { it.app }).containsExactly(alpha, beta).inOrder()
        assertThat(apps[0].tile).isEqualTo(pinned)
        assertThat(apps[1].tile).isNull()
        assertThat(vm.uiState.value.shortcuts).isEmpty()
    }

    @Test
    fun homeTab_showsOnlyPinnedAppsAndShortcuts() {
        val shortcut = AppTile(packageName = "pkg.web", activityName = "", label = "Site", shortcutId = "s1")
        tiles.update {
            listOf(AppTile(packageName = beta.packageName, activityName = beta.activityName, label = "Beta"), shortcut)
        }
        installedApps.value = listOf(alpha, beta)

        vm.setTab(AllAppsViewModel.Tab.HOMESCREEN)

        assertThat(vm.uiState.value.apps.map { it.app }).containsExactly(beta)
        assertThat(vm.uiState.value.shortcuts).containsExactly(shortcut)
    }

    @Test
    fun addToHomeScreen_isIdempotent() {
        installedApps.value = listOf(alpha)

        vm.addToHomeScreen(alpha)
        vm.addToHomeScreen(alpha)

        assertThat(tiles.tiles.value).hasSize(1)
        assertThat(vm.uiState.value.apps.single().tile?.label).isEqualTo("Alpha")
    }

    @Test
    fun confirmRemoveTile_removesPinnedApp() {
        vm.addToHomeScreen(alpha)
        vm.requestRemoveTile(tiles.tiles.value.single())

        vm.confirmRemoveTile()

        assertThat(tiles.tiles.value).isEmpty()
        assertThat(vm.tileToRemove.value).isNull()
    }
}
