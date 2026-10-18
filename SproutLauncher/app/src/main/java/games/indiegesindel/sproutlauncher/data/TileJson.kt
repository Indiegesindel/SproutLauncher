package games.indiegesindel.sproutlauncher.data

import android.util.Log
import games.indiegesindel.sproutlauncher.model.AppTile
import org.json.JSONArray
import org.json.JSONObject

internal object TileJson {
    private const val TAG = "TileJson"

    private const val ID = "id"
    private const val PACKAGE_NAME = "packageName"
    private const val ACTIVITY_NAME = "activityName"
    private const val LABEL = "label"
    private const val ICON_URI = "iconUri"
    private const val SHORTCUT_ID = "shortcutId"
    private const val IS_GROUP = "isGroup"
    private const val GROUP_TILES = "groupTiles"

    fun encode(tiles: List<AppTile>): String = toJsonArray(tiles).toString()

    fun decode(json: String): List<AppTile> = fromJsonArray(JSONArray(json))

    private fun toJsonArray(tiles: List<AppTile>): JSONArray = JSONArray().apply {
        tiles.forEach { tile ->
            put(
                JSONObject()
                    .put(ID, tile.id)
                    .put(PACKAGE_NAME, tile.packageName)
                    .put(ACTIVITY_NAME, tile.activityName)
                    .put(LABEL, tile.label)
                    .put(ICON_URI, tile.iconUri ?: JSONObject.NULL)
                    .put(SHORTCUT_ID, tile.shortcutId ?: JSONObject.NULL)
                    .put(IS_GROUP, tile.isGroup)
                    .apply { if (tile.isGroup) put(GROUP_TILES, toJsonArray(tile.groupTiles)) }
            )
        }
    }

    private fun fromJsonArray(array: JSONArray): List<AppTile> = (0 until array.length()).mapNotNull { index ->
        val obj = array.optJSONObject(index)
        val id = obj?.optStringOrNull(ID)
        if (obj == null || id == null) {
            Log.w(TAG, "Skipping malformed tile at index $index")
            return@mapNotNull null
        }
        AppTile(
            id = id,
            packageName = obj.optStringOrNull(PACKAGE_NAME).orEmpty(),
            activityName = obj.optStringOrNull(ACTIVITY_NAME).orEmpty(),
            label = obj.optStringOrNull(LABEL).orEmpty(),
            iconUri = obj.optStringOrNull(ICON_URI),
            shortcutId = obj.optStringOrNull(SHORTCUT_ID),
            isGroup = obj.optBoolean(IS_GROUP, false),
            groupTiles = obj.optJSONArray(GROUP_TILES)?.let(::fromJsonArray).orEmpty()
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key)
}
