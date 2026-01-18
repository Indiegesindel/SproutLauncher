package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
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
    enum class Filter { ALL, HOMESCREEN }

    private val _selectedTiles = MutableStateFlow<List<AppTile>>(emptyList())
    val selectedTiles: StateFlow<List<AppTile>> = _selectedTiles.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ResolveInfo>>(emptyList())
    val installedApps: StateFlow<List<ResolveInfo>> = _installedApps.asStateFlow()

    private val _isLoadingTiles = MutableStateFlow(true)
    val isLoadingTiles: StateFlow<Boolean> = _isLoadingTiles.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _currentFilter = MutableStateFlow(Filter.ALL)
    val currentFilter: StateFlow<Filter> = _currentFilter.asStateFlow()

    private val _focusedItemId = MutableStateFlow<String?>(null)
    val focusedItemId: StateFlow<String?> = _focusedItemId.asStateFlow()

    private val _focusedElement = MutableStateFlow(FocusedElement.NONE)
    val focusedElement: StateFlow<FocusedElement> = _focusedElement.asStateFlow()

    init {
        loadTiles()
        loadApps()
    }

    fun loadTiles() {
        viewModelScope.launch {
            _selectedTiles.value = appManager.getAppTiles()
            _isLoadingTiles.value = false
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val apps = packageManager.queryIntentActivities(mainIntent, 0)
                    .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
                _installedApps.value = apps
                _isLoadingApps.value = false
            }
        }
    }

    fun setFilter(filter: Filter) {
        _currentFilter.value = filter
        _focusedItemId.value = null
    }

    fun setFocusedItemId(id: String?) {
        _focusedItemId.value = id
    }

    fun onFocusChanged(element: FocusedElement) {
        _focusedElement.value = element
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
