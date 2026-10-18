package games.indiegesindel.sproutlauncher.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.model.findTile
import games.indiegesindel.sproutlauncher.model.leafTiles
import games.indiegesindel.sproutlauncher.model.removePackage
import games.indiegesindel.sproutlauncher.model.removeTile
import games.indiegesindel.sproutlauncher.model.replaceTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TileRepository(private val prefs: SharedPreferences) {

    private val lock = Any()
    private val _tiles = MutableStateFlow(load())
    val tiles: StateFlow<List<AppTile>> = _tiles.asStateFlow()

    fun find(id: String): AppTile? = _tiles.value.findTile(id)

    fun add(tile: AppTile) = update { it + tile }

    fun addIfAbsent(tile: AppTile, isDuplicate: (AppTile) -> Boolean): Boolean {
        var added = false
        update { tiles ->
            if (tiles.leafTiles().any(isDuplicate)) tiles else tiles.plus(tile).also { added = true }
        }
        return added
    }

    fun replace(tile: AppTile) = update { it.replaceTile(tile) }

    fun remove(id: String) = update { it.removeTile(id) }

    fun removePackage(packageName: String) = update { it.removePackage(packageName) }

    fun update(transform: (List<AppTile>) -> List<AppTile>) {
        synchronized(lock) {
            val current = _tiles.value
            val updated = transform(current)
            if (updated == current) return
            _tiles.value = updated
            prefs.edit { putString(KEY_APP_TILES, TileJson.encode(updated)) }
        }
    }

    private fun load(): List<AppTile> {
        val json = prefs.getString(KEY_APP_TILES, null) ?: return emptyList()
        return try {
            TileJson.decode(json)
        } catch (e: Exception) {
            Log.e(TAG, "Stored tiles are unreadable; starting empty", e)
            prefs.edit { putString(KEY_APP_TILES_CORRUPT_BACKUP, json) }
            emptyList()
        }
    }

    companion object {
        const val PREFS_NAME = "sprout_launcher_prefs"
        private const val KEY_APP_TILES = "app_tiles"
        private const val KEY_APP_TILES_CORRUPT_BACKUP = "app_tiles_corrupt_backup"
        private const val TAG = "TileRepository"
    }
}
