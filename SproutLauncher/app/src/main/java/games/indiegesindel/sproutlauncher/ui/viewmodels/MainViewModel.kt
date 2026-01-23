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
