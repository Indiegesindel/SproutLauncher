package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.data.TileRepository
import games.indiegesindel.sproutlauncher.model.AppTile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppTileSettingsViewModel(
    private val tileRepository: TileRepository,
    private val imageStore: ImageStore,
    settings: SettingsRepository,
    private val tileId: String
) : ViewModel() {

    val tile: AppTile? = tileRepository.find(tileId)

    private val _label = MutableStateFlow(tile?.label.orEmpty())
    val label: StateFlow<String> = _label.asStateFlow()

    private val _iconUri = MutableStateFlow(tile?.iconUri)
    val iconUri: StateFlow<String?> = _iconUri.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    val isSteamGridDbAvailable: StateFlow<Boolean> = settings.steamGridDbApiKey.flow
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, !settings.steamGridDbApiKey.value.isNullOrBlank())

    private val _iconImportFailed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val iconImportFailed: SharedFlow<Unit> = _iconImportFailed.asSharedFlow()

    fun onLabelChanged(label: String) {
        _label.value = label
    }

    fun onIconUriChanged(uri: String?) {
        _iconUri.value = uri
    }

    fun importIcon(source: Uri) {
        viewModelScope.launch {
            val imported = imageStore.importIcon(source)
            if (imported != null) _iconUri.value = imported.toString() else _iconImportFailed.tryEmit(Unit)
        }
    }

    fun setShowDeleteConfirm(show: Boolean) {
        _showDeleteConfirm.value = show
    }

    fun saveChanges() {
        val current = tileRepository.find(tileId) ?: return
        val label = _label.value.trim().ifEmpty { current.label }
        tileRepository.replace(current.copy(label = label, iconUri = _iconUri.value))
    }

    fun removeTile() {
        tileRepository.remove(tileId)
    }

    companion object {
        fun factory(tileId: String) = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                AppTileSettingsViewModel(
                    container.tileRepository,
                    container.imageStore,
                    container.settingsRepository,
                    tileId
                )
            }
        }
    }
}
