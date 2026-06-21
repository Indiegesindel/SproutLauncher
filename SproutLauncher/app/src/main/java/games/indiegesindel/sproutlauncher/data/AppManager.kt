package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import games.indiegesindel.sproutlauncher.model.AppTile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages the persistence of app tiles using SharedPreferences.
 */
class AppManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sprout_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_TILES = "app_tiles"
    }

    /**
     * Adds a new app tile to the persistent storage.
     */
    fun addAppTile(appTile: AppTile) {
        val currentTiles = getAppTiles().toMutableList()
        currentTiles.add(appTile)
        saveAppTiles(currentTiles)
    }

    /**
     * Updates an existing app tile.
     */
    fun updateAppTile(updatedTile: AppTile) {
        val currentTiles = getAppTiles()
        val updatedList = updateTileRecursive(currentTiles, updatedTile)
        saveAppTiles(updatedList)
    }

    private fun updateTileRecursive(tiles: List<AppTile>, updatedTile: AppTile): List<AppTile> {
        return tiles.map {
            if (it.id == updatedTile.id) {
                updatedTile
            } else if (it.isGroup) {
                it.copy(groupTiles = updateTileRecursive(it.groupTiles, updatedTile))
            } else {
                it
            }
        }
    }

    fun getAppTileById(id: String): AppTile? {
        return findTileRecursive(getAppTiles(), id)
    }

    private fun findTileRecursive(tiles: List<AppTile>, id: String): AppTile? {
        for (tile in tiles) {
            if (tile.id == id) return tile
            if (tile.isGroup) {
                val found = findTileRecursive(tile.groupTiles, id)
                if (found != null) return found
            }
        }
        return null
    }

    /**
     * Retrieves all saved app tiles.
     */
    fun getAppTiles(): List<AppTile> {
        val jsonString = sharedPreferences.getString(KEY_APP_TILES, null) ?: return emptyList()
        val jsonArray = JSONArray(jsonString)
        return parseTiles(jsonArray)
    }

    private fun parseTiles(jsonArray: JSONArray): List<AppTile> {
        val tiles = mutableListOf<AppTile>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            tiles.add(
                AppTile(
                    id = obj.getString("id"),
                    packageName = obj.getString("packageName"),
                    activityName = obj.getString("activityName"),
                    label = obj.getString("label"),
                    iconUri = if (obj.isNull("iconUri")) null else obj.getString("iconUri"),
                    shortcutId = if (obj.isNull("shortcutId")) null else obj.getString("shortcutId"),
                    isGroup = obj.optBoolean("isGroup", false),
                    groupTiles = if (obj.isNull("groupTiles")) emptyList() else parseTiles(obj.getJSONArray("groupTiles"))
                )
            )
        }
        return tiles
    }

    /**
     * Removes an app tile by its ID.
     */
    fun removeAppTile(appTileId: String) {
        val currentTiles = getAppTiles()
        val filteredTiles = removeTileRecursive(currentTiles, appTileId)
        saveAppTiles(filteredTiles)
    }

    private fun removeTileRecursive(tiles: List<AppTile>, id: String): List<AppTile> {
        return tiles.filter { it.id != id }.map {
            if (it.isGroup) {
                it.copy(groupTiles = removeTileRecursive(it.groupTiles, id))
            } else {
                it
            }
        }
    }

    /**
     * Removes all app tiles associated with a given package name.
     */
    fun removeTilesForPackage(packageName: String) {
        val currentTiles = getAppTiles()
        val filteredTiles = removePackageRecursive(currentTiles, packageName)
        saveAppTiles(filteredTiles)
    }

    private fun removePackageRecursive(tiles: List<AppTile>, packageName: String): List<AppTile> {
        return tiles.filter { it.packageName != packageName }.map {
            if (it.isGroup) {
                it.copy(groupTiles = removePackageRecursive(it.groupTiles, packageName))
            } else {
                it
            }
        }
    }


    /**
     * Saves the entire list of app tiles.
     */
    fun saveAppTiles(tiles: List<AppTile>) {
        val jsonArray = serializeTiles(tiles)
        sharedPreferences.edit { putString(KEY_APP_TILES, jsonArray.toString()) }
    }

    private fun serializeTiles(tiles: List<AppTile>): JSONArray {
        val jsonArray = JSONArray()
        tiles.forEach { tile ->
            val obj = JSONObject()
            obj.put("id", tile.id)
            obj.put("packageName", tile.packageName)
            obj.put("activityName", tile.activityName)
            obj.put("label", tile.label)
            obj.put("iconUri", tile.iconUri)
            obj.put("shortcutId", tile.shortcutId)
            obj.put("isGroup", tile.isGroup)
            if (tile.isGroup) {
                obj.put("groupTiles", serializeTiles(tile.groupTiles))
            }
            jsonArray.put(obj)
        }
        return jsonArray
    }

    /**
     * Checks if a package is installed on the device.
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
