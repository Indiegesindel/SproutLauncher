package games.indiegesindel.sproutlauncher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.SteamGridDbClient
import games.indiegesindel.sproutlauncher.data.SteamGridDbException
import games.indiegesindel.sproutlauncher.model.SteamGridDbGame
import games.indiegesindel.sproutlauncher.model.SteamGridDbGrid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SteamGridDbUiState(
    val query: String = "",
    val hasSearched: Boolean = false,
    val isSearching: Boolean = false,
    val games: List<SteamGridDbGame> = emptyList(),
    val selectedGame: SteamGridDbGame? = null,
    val isLoadingGrids: Boolean = false,
    val grids: List<SteamGridDbGrid> = emptyList(),
    val selectedGrid: SteamGridDbGrid? = null,
    val isDownloading: Boolean = false,
    val downloadFailed: Boolean = false,
    val error: SteamGridDbException.Reason? = null,
    val pickedIconUri: String? = null
)

class SteamGridDbViewModel(
    private val client: SteamGridDbClient,
    private val imageStore: ImageStore,
    initialQuery: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(SteamGridDbUiState(query = initialQuery))
    val uiState: StateFlow<SteamGridDbUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var gridsJob: Job? = null

    init {
        if (initialQuery.isNotBlank()) search()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        _uiState.update { it.copy(isSearching = true, hasSearched = true, error = null, selectedGame = null, grids = emptyList()) }
        searchJob = viewModelScope.launch {
            val result = client.search(query)
            _uiState.update {
                it.copy(isSearching = false, games = result.getOrDefault(emptyList()), error = result.errorReason())
            }
        }
    }

    fun selectGame(game: SteamGridDbGame) {
        gridsJob?.cancel()
        _uiState.update { it.copy(selectedGame = game, grids = emptyList(), isLoadingGrids = true, error = null) }
        gridsJob = viewModelScope.launch {
            val result = client.gridsForGame(game.id)
            _uiState.update {
                it.copy(
                    isLoadingGrids = false,
                    grids = result.getOrDefault(emptyList()).filter { grid -> !grid.url.isNullOrBlank() },
                    error = result.errorReason()
                )
            }
        }
    }

    fun goBackToSearch() {
        gridsJob?.cancel()
        _uiState.update { it.copy(selectedGame = null, grids = emptyList(), selectedGrid = null, isLoadingGrids = false, error = null) }
    }

    fun selectGrid(grid: SteamGridDbGrid) {
        _uiState.update { it.copy(selectedGrid = grid, downloadFailed = false) }
    }

    fun dismissGridDetail() {
        _uiState.update { it.copy(selectedGrid = null) }
    }

    fun downloadSelectedGrid() {
        val grid = _uiState.value.selectedGrid ?: return
        val url = grid.url ?: return
        if (_uiState.value.isDownloading) return
        _uiState.update { it.copy(isDownloading = true, downloadFailed = false) }
        viewModelScope.launch {
            val result = client.download(url).mapCatching { bytes ->
                imageStore.saveIcon(bytes, "${ImageStore.STEAMGRIDDB_ICON_PREFIX}${grid.id}.${imageExtension(url)}")
            }
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    pickedIconUri = result.getOrNull()?.toString(),
                    downloadFailed = result.isFailure
                )
            }
        }
    }

    private fun Result<*>.errorReason(): SteamGridDbException.Reason? = exceptionOrNull()?.let {
        (it as? SteamGridDbException)?.reason ?: SteamGridDbException.Reason.SERVER
    }

    private fun imageExtension(url: String): String =
        url.substringAfterLast('/').substringAfterLast('.', "").lowercase()
            .takeIf { it in IMAGE_EXTENSIONS } ?: "png"

    companion object {
        private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif")

        fun factory(initialQuery: String) = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                SteamGridDbViewModel(container.steamGridDbClient, container.imageStore, initialQuery)
            }
        }
    }
}
