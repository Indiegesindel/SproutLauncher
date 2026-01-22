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
        val currentTiles = getAppTiles().map {
            if (it.id == updatedTile.id) updatedTile else it
        }
        saveAppTiles(currentTiles)
    }

    /**
     * Retrieves all saved app tiles.
     */
    fun getAppTiles(): List<AppTile> {
        val jsonString = sharedPreferences.getString(KEY_APP_TILES, null) ?: return emptyList()
        val tiles = mutableListOf<AppTile>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            tiles.add(
                AppTile(
                    id = obj.getString("id"),
                    packageName = obj.getString("packageName"),
                    activityName = obj.getString("activityName"),
                    label = obj.getString("label"),
                    iconUri = if (obj.isNull("iconUri")) null else obj.getString("iconUri")
                )
            )
        }
        return tiles
    }

    /**
     * Removes an app tile by its ID.
     */
    fun removeAppTile(appTileId: String) {
        val currentTiles = getAppTiles().filter { it.id != appTileId }
        saveAppTiles(currentTiles)
    }

    /**
     * Removes all app tiles associated with a given package name.
     */
    fun removeTilesForPackage(packageName: String) {
        val currentTiles = getAppTiles().filter { it.packageName != packageName }
        saveAppTiles(currentTiles)
    }


    /**
     * Saves the entire list of app tiles.
     */
    fun saveAppTiles(tiles: List<AppTile>) {
        val jsonArray = JSONArray()
        tiles.forEach { tile ->
            val obj = JSONObject()
            obj.put("id", tile.id)
            obj.put("packageName", tile.packageName)
            obj.put("activityName", tile.activityName)
            obj.put("label", tile.label)
            obj.put("iconUri", tile.iconUri)
            jsonArray.put(obj)
        }
        sharedPreferences.edit { putString(KEY_APP_TILES, jsonArray.toString()) }
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
