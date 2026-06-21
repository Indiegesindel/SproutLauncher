package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import games.indiegesindel.sproutlauncher.model.SteamGridDBGame
import games.indiegesindel.sproutlauncher.model.SteamGridDBGrid
import games.indiegesindel.sproutlauncher.model.SteamGridDBGridsResponse
import games.indiegesindel.sproutlauncher.model.SteamGridDBSearchResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SteamGridDBManager(context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val settingsManager = SettingsManager(context)

    fun search(query: String): List<SteamGridDBGame> {
        val apiKey = settingsManager.steamGridDBApiKey.value ?: return emptyList()

        val term = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://www.steamgriddb.com/api/v2/search/autocomplete/$term"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Error fetching data: ${response.code} ${response.message}")
                    return emptyList()
                }

                val body = response.body.string()
                val parsed = gson.fromJson(body, SteamGridDBSearchResponse::class.java)
                parsed?.data ?: emptyList()
            }
        } catch (e: Exception) {
            println("Error fetching data: $e")
            Log.e("SteamGridDB", "Error fetching data: $e")
            emptyList()
        }
    }

    fun getGridsByGameId(gameId: Int): List<SteamGridDBGrid> {
        val apiKey = settingsManager.steamGridDBApiKey.value ?: return emptyList()

        val url = "https://www.steamgriddb.com/api/v2/grids/game/$gameId?dimensions=512x512,1024x1024"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Error fetching data: ${response.code} ${response.message}")
                    return emptyList()
                }

                val body = response.body.string()
                val parsed = gson.fromJson(body, SteamGridDBGridsResponse::class.java)
                parsed?.data ?: emptyList()
            }
        } catch (e: Exception) {
            println("Error fetching data: $e")
            Log.e("SteamGridDB", "Error fetching data: $e")
            emptyList()
        }
    }
}