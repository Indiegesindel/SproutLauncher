package games.indiegesindel.sproutlauncher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppTileSettingsViewModel(
    private val appManager: AppManager,
    private val tileId: String
) : ViewModel() {
    private val _tile = MutableStateFlow<AppTile?>(null)
    val tile: StateFlow<AppTile?> = _tile.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _iconUri = MutableStateFlow<String?>(null)
    val iconUri: StateFlow<String?> = _iconUri.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    init {
        val initialTile = appManager.getAppTileById(tileId)
        _tile.value = initialTile
        initialTile?.let {
            _label.value = it.label
            _iconUri.value = it.iconUri
        }
    }

    fun onLabelChanged(newLabel: String) {
        _label.value = newLabel
    }

    fun onIconUriChanged(newUri: String?) {
        _iconUri.value = newUri
    }

    fun setShowDeleteConfirm(show: Boolean) {
        _showDeleteConfirm.value = show
    }

    fun saveChanges() {
        val currentTile = _tile.value ?: return
        val updatedTile = currentTile.copy(label = _label.value, iconUri = _iconUri.value)
        appManager.updateAppTile(updatedTile)
    }

    fun removeTile() {
        appManager.removeAppTile(tileId)
    }
}

class AppTileSettingsViewModelFactory(
    private val appManager: AppManager,
    private val tileId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppTileSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppTileSettingsViewModel(appManager, tileId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
