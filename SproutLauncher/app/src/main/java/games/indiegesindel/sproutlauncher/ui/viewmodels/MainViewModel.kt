package games.indiegesindel.sproutlauncher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.model.AppTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val appManager: AppManager,
    settingsManager: SettingsManager
) : ViewModel() {
    private val _appTiles = MutableStateFlow<List<AppTile>>(emptyList())
    val appTiles: StateFlow<List<AppTile>> = _appTiles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val homeScreenRows: StateFlow<Int> = settingsManager.homeScreenRows
    val horizontalSpacing: StateFlow<Int> = settingsManager.horizontalSpacing
    val verticalSpacing: StateFlow<Int> = settingsManager.verticalSpacing
    val appTileRoundness: StateFlow<Int> = settingsManager.appTileRoundness
    val groupRows: StateFlow<Int> = settingsManager.groupRows
    val groupHorizontalSpacing: StateFlow<Int> = settingsManager.groupHorizontalSpacing
    val groupVerticalSpacing: StateFlow<Int> = settingsManager.groupVerticalSpacing
    val groupAppTileRoundness: StateFlow<Int> = settingsManager.groupAppTileRoundness
    val wallpaperUri: StateFlow<String?> = settingsManager.wallpaperUri
    val wallpaperDim: StateFlow<Float> = settingsManager.wallpaperDim
    val showYouTube: StateFlow<Boolean> = settingsManager.showYouTube
    val showDiscord: StateFlow<Boolean> = settingsManager.showDiscord
    val showSpotify: StateFlow<Boolean> = settingsManager.showSpotify

    private val _focusedElement = MutableStateFlow(FocusedElement.NONE)
    val focusedElement: StateFlow<FocusedElement> = _focusedElement.asStateFlow()

    private val _focusedItemId = MutableStateFlow<String?>(null)
    val focusedItemId: StateFlow<String?> = _focusedItemId.asStateFlow()

    private val _tileToRemove = MutableStateFlow<AppTile?>(null)
    val tileToRemove: StateFlow<AppTile?> = _tileToRemove.asStateFlow()

    private val _isInMultiSelectMode = MutableStateFlow(false)
    val isInMultiSelectMode: StateFlow<Boolean> = _isInMultiSelectMode.asStateFlow()

    private val _selectedTileIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTileIds: StateFlow<Set<String>> = _selectedTileIds.asStateFlow()

    private val _openedGroup = MutableStateFlow<AppTile?>(null)
    val openedGroup: StateFlow<AppTile?> = _openedGroup.asStateFlow()

    private val _installedQuickActions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val installedQuickActions: StateFlow<Map<String, Boolean>> = _installedQuickActions.asStateFlow()

    fun loadAppTiles() {
        viewModelScope.launch {
            _appTiles.value = appManager.getAppTiles()
            checkInstalledQuickActions()
            _isLoading.value = false

            // Set focus on arrival
            val firstTile = _appTiles.value.firstOrNull()
            if (firstTile != null) {
                onFocusedItemIdChanged("tile:${firstTile.id}")
            } else {
                onFocusedItemIdChanged("action:all_apps")
            }
        }
    }

    private fun checkInstalledQuickActions() {
        val apps = listOf(
            "com.google.android.youtube",
            "com.android.vending",
            "com.discord",
            "com.spotify.music",
            "com.google.android.apps.photos"
        )
        val status = apps.associateWith { appManager.isPackageInstalled(it) }
        _installedQuickActions.value = status
    }

    fun requestRemoveTile(tile: AppTile) {
        _tileToRemove.value = tile
    }

    fun dismissRemoveConfirmation() {
        _tileToRemove.value = null
    }

    fun confirmRemoveTile() {
        _tileToRemove.value?.let {
            appManager.removeAppTile(it.id)
            loadAppTiles()
            _tileToRemove.value = null
        }
    }

    fun removeAppTile(tileId: String) {
        appManager.removeAppTile(tileId)
        loadAppTiles()
    }

    fun reorderAppTiles(from: Int, to: Int) {
        val newList = _appTiles.value.toMutableList()
        if (from in newList.indices && to in newList.indices) {
            val tile = newList.removeAt(from)
            newList.add(to, tile)
            _appTiles.value = newList
        }
    }

    fun saveAppTiles() {
        appManager.saveAppTiles(_appTiles.value)
    }

    fun enterMultiSelectMode(initialTileId: String) {
        _isInMultiSelectMode.value = true
        _selectedTileIds.value = setOf(initialTileId)
    }

    fun toggleTileSelection(tileId: String) {
        val currentSelection = _selectedTileIds.value
        if (currentSelection.contains(tileId)) {
            _selectedTileIds.value = currentSelection - tileId
        } else {
            _selectedTileIds.value = currentSelection + tileId
        }
        if (_selectedTileIds.value.isEmpty()) {
            _isInMultiSelectMode.value = false
        }
    }

    fun clearSelection() {
        _selectedTileIds.value = emptySet()
        _isInMultiSelectMode.value = false
    }

    fun groupSelectedTiles() {
        val selectedIds = _selectedTileIds.value
        if (selectedIds.isEmpty()) return

        val currentTiles = _appTiles.value
        val tilesToGroup = currentTiles.filter { selectedIds.contains(it.id) }
        val remainingTiles = currentTiles.filter { !selectedIds.contains(it.id) }

        val newGroup = AppTile(
            label = "New Group",
            packageName = "",
            activityName = "",
            isGroup = true,
            groupTiles = tilesToGroup
        )

        _appTiles.value = remainingTiles + newGroup
        saveAppTiles()
        clearSelection()
        onFocusedItemIdChanged("tile:${newGroup.id}")
    }

    fun ungroup(groupTileId: String) {
        val currentTiles = _appTiles.value.toMutableList()
        val index = currentTiles.indexOfFirst { it.id == groupTileId }
        if (index != -1) {
            val group = currentTiles.removeAt(index)
            if (group.isGroup) {
                val firstUngroupedId = group.groupTiles.firstOrNull()?.id
                currentTiles.addAll(group.groupTiles)
                _appTiles.value = currentTiles
                saveAppTiles()
                firstUngroupedId?.let { onFocusedItemIdChanged("tile:$it") }
            }
        }
    }

    fun removeTileFromGroup(groupTileId: String, tileId: String) {
        val currentTiles = _appTiles.value.toMutableList()
        val groupIndex = currentTiles.indexOfFirst { it.id == groupTileId }
        if (groupIndex != -1) {
            val group = currentTiles[groupIndex]
            if (group.isGroup) {
                val tileToRemove = group.groupTiles.find { it.id == tileId }
                if (tileToRemove != null) {
                    val newGroupTiles = group.groupTiles.filter { it.id != tileId }
                    if (newGroupTiles.size <= 1) {
                        currentTiles.removeAt(groupIndex)
                        currentTiles.addAll(newGroupTiles)
                    } else {
                        currentTiles[groupIndex] = group.copy(groupTiles = newGroupTiles)
                    }
                    currentTiles.add(tileToRemove)
                    _appTiles.value = currentTiles
                    saveAppTiles()
                    
                    // Update opened group if it was the one modified
                    if (_openedGroup.value?.id == groupTileId) {
                        if (newGroupTiles.size <= 1) {
                            _openedGroup.value = null
                            onFocusedItemIdChanged("tile:${tileToRemove.id}")
                        } else {
                            _openedGroup.value = currentTiles[groupIndex]
                            newGroupTiles.firstOrNull()?.let {
                                onFocusedItemIdChanged("tile:${it.id}")
                            }
                        }
                    }
                }
            }
        }
    }

    fun openGroup(groupTile: AppTile) {
        _openedGroup.value = groupTile
        groupTile.groupTiles.firstOrNull()?.let {
            onFocusedItemIdChanged("tile:${it.id}")
        }
    }

    fun closeGroup() {
        val currentGroup = _openedGroup.value
        _openedGroup.value = null
        currentGroup?.let {
            onFocusedItemIdChanged("tile:${it.id}")
        }
    }

    fun onFocusChanged(focused: Boolean) {
        if (focused) {
            _focusedElement.value = FocusedElement.APP_TILE
        } else if (_focusedElement.value == FocusedElement.APP_TILE) {
            _focusedElement.value = FocusedElement.NONE
        }
    }

    fun onQuickActionsFocusChanged(focused: Boolean) {
        if (focused) {
            _focusedElement.value = FocusedElement.QUICK_ACTION
        } else if (_focusedElement.value == FocusedElement.QUICK_ACTION) {
            _focusedElement.value = FocusedElement.NONE
        }
    }

    fun onFocusedItemIdChanged(id: String?) {
        _focusedItemId.value = id
        if (id == null) {
            _focusedElement.value = FocusedElement.NONE
        } else if (id.startsWith("action:")) {
            _focusedElement.value = FocusedElement.QUICK_ACTION
        } else if (id.startsWith("tile:")) {
            _focusedElement.value = FocusedElement.APP_TILE
        }
    }
}

class MainViewModelFactory(
    private val appManager: AppManager,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appManager, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
