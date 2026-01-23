package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExtendedViewModel(
    private val appManager: AppManager,
    private val packageManager: android.content.pm.PackageManager
) : ViewModel() {
    enum class Tab { ALL, HOMESCREEN, SETTINGS }

    private val _selectedTiles = MutableStateFlow<List<AppTile>>(emptyList())
    val selectedTiles: StateFlow<List<AppTile>> = _selectedTiles.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ResolveInfo>>(emptyList())
    val installedApps: StateFlow<List<ResolveInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _currentTab = MutableStateFlow(Tab.ALL)
    val currentTab: StateFlow<Tab> = _currentTab.asStateFlow()

    private val _focusedItemId = MutableStateFlow<String?>(null)
    val focusedItemId: StateFlow<String?> = _focusedItemId.asStateFlow()

    private val _focusedElement = MutableStateFlow(FocusedElement.NONE)
    val focusedElement: StateFlow<FocusedElement> = _focusedElement.asStateFlow()

    private val _tileToRemove = MutableStateFlow<AppTile?>(null)
    val tileToRemove: StateFlow<AppTile?> = _tileToRemove.asStateFlow()

    init {
        loadTiles()
        loadApps()
    }

    fun loadTiles() {
        viewModelScope.launch {
            _selectedTiles.value = appManager.getAppTiles()
            
            // Focus first item if on HOMESCREEN tab and nothing focused
            if (_currentTab.value == Tab.HOMESCREEN && _focusedItemId.value == null) {
                _selectedTiles.value.firstOrNull()?.let { tile ->
                    setFocusedItemId("${tile.packageName}_${tile.activityName}")
                    onFocusChanged(FocusedElement.APP_TILE)
                }
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val apps = packageManager.queryIntentActivities(mainIntent, 0)
                
                // Cache labels to avoid redundant loadLabel calls during sorting
                val appsWithLabels = apps.map {
                    it to it.loadLabel(packageManager).toString()
                }.sortedBy { it.second.lowercase() }

                _installedApps.value = appsWithLabels.map { it.first }
                _isLoadingApps.value = false
            }
            
            // Focus first item if on ALL tab and nothing focused
            if (_currentTab.value == Tab.ALL && _focusedItemId.value == null) {
                _installedApps.value.firstOrNull()?.let { app ->
                    setFocusedItemId("${app.activityInfo.packageName}_${app.activityInfo.name}")
                    onFocusChanged(FocusedElement.APP_TILE)
                }
            }
        }
    }

    fun setTab(tab: Tab) {
        _currentTab.value = tab
        _focusedItemId.value = null
        
        when (tab) {
            Tab.ALL -> {
                _installedApps.value.firstOrNull()?.let { app ->
                    setFocusedItemId("${app.activityInfo.packageName}_${app.activityInfo.name}")
                    onFocusChanged(FocusedElement.APP_TILE)
                }
            }
            Tab.HOMESCREEN -> {
                _selectedTiles.value.firstOrNull()?.let { tile ->
                    setFocusedItemId("${tile.packageName}_${tile.activityName}")
                    onFocusChanged(FocusedElement.APP_TILE)
                }
            }
            Tab.SETTINGS -> {
                onFocusChanged(FocusedElement.NONE)
            }
        }
    }

    fun setFocusedItemId(id: String?) {
        _focusedItemId.value = id
    }

    fun onFocusChanged(element: FocusedElement) {
        _focusedElement.value = element
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
            loadTiles()
            _tileToRemove.value = null
        }
    }
}

class ExtendedViewModelFactory(
    private val appManager: AppManager,
    private val packageManager: android.content.pm.PackageManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExtendedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExtendedViewModel(appManager, packageManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
