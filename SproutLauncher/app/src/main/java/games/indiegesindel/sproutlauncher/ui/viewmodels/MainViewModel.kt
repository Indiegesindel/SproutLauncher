package games.indiegesindel.sproutlauncher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.data.GridLayout
import games.indiegesindel.sproutlauncher.data.InstalledAppsRepository
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.QuickApp
import games.indiegesindel.sproutlauncher.model.findTile
import games.indiegesindel.sproutlauncher.model.groupTiles
import games.indiegesindel.sproutlauncher.model.moveIntoGroup
import games.indiegesindel.sproutlauncher.model.moveTile
import games.indiegesindel.sproutlauncher.model.moveTileInGroup
import games.indiegesindel.sproutlauncher.model.removeFromGroup
import games.indiegesindel.sproutlauncher.model.ungroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Wallpaper(val uri: String, val dim: Float)

class MainViewModel(
    private val tileRepository: TileRepository,
    settings: SettingsRepository,
    installedApps: InstalledAppsRepository
) : ViewModel() {

    val appTiles: StateFlow<List<AppTile>> = tileRepository.tiles

    val homeLayout: StateFlow<GridLayout> = settings.homeGrid.layout
        .stateIn(viewModelScope, SharingStarted.Eagerly, settings.homeGrid.currentLayout)

    val groupLayout: StateFlow<GridLayout> = settings.groupGrid.layout
        .stateIn(viewModelScope, SharingStarted.Eagerly, settings.groupGrid.currentLayout)

    val wallpaper: StateFlow<Wallpaper?> = combine(settings.wallpaperUri.flow, settings.wallpaperDim.flow) { uri, dim ->
        uri?.let { Wallpaper(it, dim) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val quickApps: StateFlow<List<QuickApp>> = combine(
        installedApps.apps.map { apps -> apps.orEmpty().mapTo(HashSet()) { it.packageName } },
        settings.showYouTube.flow,
        settings.showDiscord.flow,
        settings.showSpotify.flow
    ) { installed, showYouTube, showDiscord, showSpotify ->
        QuickApp.entries.filter { app ->
            app.packageName in installed && when (app) {
                QuickApp.YOUTUBE -> showYouTube
                QuickApp.DISCORD -> showDiscord
                QuickApp.SPOTIFY -> showSpotify
                QuickApp.PLAY_STORE, QuickApp.PHOTOS -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingFocusTileId = MutableStateFlow<String?>(null)
    val pendingFocusTileId: StateFlow<String?> = _pendingFocusTileId.asStateFlow()

    private val _openedGroupId = MutableStateFlow<String?>(null)
    val openedGroup: StateFlow<AppTile?> = combine(appTiles, _openedGroupId) { tiles, id ->
        id?.let { tiles.firstOrNull { tile -> tile.id == it && tile.isGroup } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _tileToRemove = MutableStateFlow<AppTile?>(null)
    val tileToRemove: StateFlow<AppTile?> = _tileToRemove.asStateFlow()

    private val _tileToMoveToGroup = MutableStateFlow<AppTile?>(null)
    val tileToMoveToGroup: StateFlow<AppTile?> = _tileToMoveToGroup.asStateFlow()

    private val _isInMultiSelectMode = MutableStateFlow(false)
    val isInMultiSelectMode: StateFlow<Boolean> = _isInMultiSelectMode.asStateFlow()

    private val _selectedTileIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTileIds: StateFlow<Set<String>> = _selectedTileIds.asStateFlow()

    private val _showAllApps = MutableStateFlow(false)
    val showAllApps: StateFlow<Boolean> = _showAllApps.asStateFlow()

    init {
        viewModelScope.launch {
            _pendingFocusTileId.value = appTiles.first { it.isNotEmpty() }.first().id
        }
    }

    fun consumePendingFocus() {
        _pendingFocusTileId.value = null
    }

    fun openAllApps() {
        _showAllApps.value = true
    }

    fun closeAllApps() {
        _showAllApps.value = false
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

    fun reorderTiles(from: Int, to: Int) = tileRepository.update { it.moveTile(from, to) }

    fun reorderGroupTiles(groupId: String, from: Int, to: Int) =
        tileRepository.update { it.moveTileInGroup(groupId, from, to) }

    fun enterMultiSelectMode(tileId: String) {
        if (appTiles.value.findTile(tileId)?.isGroup == true) return
        _isInMultiSelectMode.value = true
        _selectedTileIds.value = setOf(tileId)
    }

    fun toggleTileSelection(tileId: String) {
        if (appTiles.value.findTile(tileId)?.isGroup == true) return
        val selection = _selectedTileIds.value.let { if (tileId in it) it - tileId else it + tileId }
        _selectedTileIds.value = selection
        if (selection.isEmpty()) _isInMultiSelectMode.value = false
    }

    fun clearSelection() {
        _selectedTileIds.value = emptySet()
        _isInMultiSelectMode.value = false
    }

    fun groupSelectedTiles(groupLabel: String) {
        val selection = _selectedTileIds.value
        if (selection.isEmpty()) return
        var groupId: String? = null
        tileRepository.update { tiles ->
            tiles.groupTiles(selection) { selected -> AppTile.group(groupLabel, selected).also { groupId = it.id } }
        }
        clearSelection()
        groupId?.let { _pendingFocusTileId.value = it }
    }

    fun removeSelectedTiles() {
        val selection = _selectedTileIds.value
        if (selection.isEmpty()) return
        tileRepository.update { tiles -> tiles.filterNot { it.id in selection } }
        clearSelection()
    }

    fun ungroup(groupId: String) {
        val firstChild = appTiles.value.findTile(groupId)?.groupTiles?.firstOrNull()
        tileRepository.update { it.ungroup(groupId) }
        firstChild?.let { _pendingFocusTileId.value = it.id }
    }

    fun removeTileFromGroup(groupId: String, tileId: String) {
        tileRepository.update { it.removeFromGroup(groupId, tileId) }
        if (_openedGroupId.value != groupId) return

        val group = appTiles.value.firstOrNull { it.id == groupId && it.isGroup }
        if (group == null) {
            _openedGroupId.value = null
            _pendingFocusTileId.value = tileId
        } else {
            group.groupTiles.firstOrNull()?.let { _pendingFocusTileId.value = it.id }
        }
    }

    fun openGroup(group: AppTile) {
        _openedGroupId.value = group.id
        group.groupTiles.firstOrNull()?.let { _pendingFocusTileId.value = it.id }
    }

    fun closeGroup() {
        val groupId = _openedGroupId.value ?: return
        _openedGroupId.value = null
        _pendingFocusTileId.value = groupId
    }

    fun requestMoveToGroup(tile: AppTile) {
        _tileToMoveToGroup.value = tile
    }

    fun dismissMoveToGroup() {
        _tileToMoveToGroup.value = null
    }

    fun moveTileToGroup(tileId: String, groupId: String) {
        tileRepository.update { it.moveIntoGroup(tileId, groupId) }
        _tileToMoveToGroup.value = null
        _pendingFocusTileId.value = groupId
    }

    fun resetState() {
        _openedGroupId.value = null
        _tileToMoveToGroup.value = null
        _showAllApps.value = false
        clearSelection()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                MainViewModel(container.tileRepository, container.settingsRepository, container.installedAppsRepository)
            }
        }
    }
}
