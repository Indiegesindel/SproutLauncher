package games.indiegesindel.sproutlauncher.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import games.indiegesindel.sproutlauncher.data.SteamGridDBManager
import games.indiegesindel.sproutlauncher.model.SteamGridDBGame
import games.indiegesindel.sproutlauncher.model.SteamGridDBGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class SteamGridDBViewModel(
    private val steamGridDBManager: SteamGridDBManager,
    private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _games = MutableStateFlow<List<SteamGridDBGame>>(emptyList())
    val games: StateFlow<List<SteamGridDBGame>> = _games.asStateFlow()

    private val _grids = MutableStateFlow<List<SteamGridDBGrid>>(emptyList())
    val grids: StateFlow<List<SteamGridDBGrid>> = _grids.asStateFlow()

    private val _selectedGame = MutableStateFlow<SteamGridDBGame?>(null)
    val selectedGame: StateFlow<SteamGridDBGame?> = _selectedGame.asStateFlow()

    private val _selectedGrid = MutableStateFlow<SteamGridDBGrid?>(null)
    val selectedGrid: StateFlow<SteamGridDBGrid?> = _selectedGrid.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isLoadingGrids = MutableStateFlow(false)
    val isLoadingGrids: StateFlow<Boolean> = _isLoadingGrids.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun search() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSearching.value = true
            _hasSearched.value = true
            val results = steamGridDBManager.search(query)
            _games.value = results
            _isSearching.value = false
        }
    }

    fun selectGame(game: SteamGridDBGame) {
        _selectedGame.value = game
        _grids.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingGrids.value = true
            val results = steamGridDBManager.getGridsByGameId(game.id)
            _grids.value = results
            _isLoadingGrids.value = false
        }
    }

    fun goBackToSearch() {
        _selectedGame.value = null
        _grids.value = emptyList()
        _selectedGrid.value = null
    }

    fun selectGrid(grid: SteamGridDBGrid) {
        _selectedGrid.value = grid
    }

    fun dismissGridDetail() {
        _selectedGrid.value = null
    }

    fun downloadIcon(grid: SteamGridDBGrid, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isDownloading.value = true
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(grid.url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body.bytes()
                    val extension = grid.url.substringAfterLast('.', "png").take(4)
                    val file = File(context.filesDir, "steamgriddb_${grid.id}.$extension")
                    file.writeBytes(bytes)
                    val uri = Uri.fromFile(file).toString()
                    _isDownloading.value = false
                    onComplete(uri)
                } else {
                    _isDownloading.value = false
                    onComplete(null)
                }
            } catch (e: Exception) {
                _isDownloading.value = false
                onComplete(null)
            }
        }
    }
}

class SteamGridDBViewModelFactory(
    private val steamGridDBManager: SteamGridDBManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SteamGridDBViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SteamGridDBViewModel(steamGridDBManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
