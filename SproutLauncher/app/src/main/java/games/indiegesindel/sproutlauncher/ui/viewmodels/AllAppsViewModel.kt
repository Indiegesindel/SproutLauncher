package games.indiegesindel.sproutlauncher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.data.InstalledAppsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.InstalledApp
import games.indiegesindel.sproutlauncher.model.leafTiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AllAppsViewModel(
    private val tileRepository: TileRepository,
    installedApps: InstalledAppsRepository
) : ViewModel() {

    enum class Tab { ALL, HOMESCREEN }

    data class AppEntry(val app: InstalledApp, val tile: AppTile?)

    data class UiState(
        val tab: Tab = Tab.ALL,
        val isLoading: Boolean = true,
        val apps: List<AppEntry> = emptyList(),
        val shortcuts: List<AppTile> = emptyList()
    )

    private val tab = MutableStateFlow(Tab.ALL)

    val uiState: StateFlow<UiState> = combine(tab, installedApps.apps, tileRepository.tiles) { tab, apps, tiles ->
        val leaves = tiles.leafTiles().toList()
        val tilesByActivity = leaves.filterNot { it.isShortcut }.associateBy { it.packageName to it.activityName }
        val entries = apps.orEmpty().map { AppEntry(it, tilesByActivity[it.packageName to it.activityName]) }
        when (tab) {
            Tab.ALL -> UiState(tab, apps == null, entries)
            Tab.HOMESCREEN -> UiState(
                tab = tab,
                isLoading = apps == null,
                apps = entries.filter { it.tile != null },
                shortcuts = leaves.filter { it.isShortcut }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    private val _tileToRemove = MutableStateFlow<AppTile?>(null)
    val tileToRemove: StateFlow<AppTile?> = _tileToRemove.asStateFlow()

    fun setTab(tab: Tab) {
        this.tab.value = tab
    }

    fun addToHomeScreen(app: InstalledApp) {
        tileRepository.addIfAbsent(
            AppTile(packageName = app.packageName, activityName = app.activityName, label = app.label)
        ) { it.matchesActivity(app.packageName, app.activityName) }
    }

    fun requestRemoveTile(tile: AppTile) {
        _tileToRemove.value = tile
    }

    fun dismissRemoveConfirmation() {
        _tileToRemove.value = null
    }

    fun confirmRemoveTile() {
        _tileToRemove.value?.let { tileRepository.remove(it.id) }
        _tileToRemove.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                AllAppsViewModel(container.tileRepository, container.installedAppsRepository)
            }
        }
    }
}
