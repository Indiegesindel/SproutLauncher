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
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _appTiles = MutableStateFlow<List<AppTile>>(emptyList())
    val appTiles: StateFlow<List<AppTile>> = _appTiles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val homeScreenRows: StateFlow<Int> = settingsManager.homeScreenRows

    private val _focusedElement = MutableStateFlow(FocusedElement.NONE)
    val focusedElement: StateFlow<FocusedElement> = _focusedElement.asStateFlow()

    private val _focusedItemId = MutableStateFlow<String?>(null)
    val focusedItemId: StateFlow<String?> = _focusedItemId.asStateFlow()

    fun loadAppTiles() {
        viewModelScope.launch {
            _appTiles.value = appManager.getAppTiles()
            _isLoading.value = false
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

    fun onFocusedItemIdChanged(id: String?) {
        _focusedItemId.value = id
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
