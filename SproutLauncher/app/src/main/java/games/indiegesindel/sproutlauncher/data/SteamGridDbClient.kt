package games.indiegesindel.sproutlauncher.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import games.indiegesindel.sproutlauncher.model.SteamGridDbGame
import games.indiegesindel.sproutlauncher.model.SteamGridDbGrid
import games.indiegesindel.sproutlauncher.model.SteamGridDbResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

class SteamGridDbException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason { MISSING_API_KEY, UNAUTHORIZED, NETWORK, SERVER }
}

class SteamGridDbClient(
    private val httpClient: OkHttpClient,
    private val apiKey: () -> String?,
    private val ioDispatcher: CoroutineDispatcher,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL.toHttpUrl()
) {
    private val gson = Gson()

    suspend fun search(query: String): Result<List<SteamGridDbGame>> = get(
        url = baseUrl.newBuilder().addPathSegments("search/autocomplete").addPathSegment(query).build(),
        type = object : TypeToken<SteamGridDbResponse<SteamGridDbGame>>() {}.type
    )

    suspend fun gridsForGame(gameId: Int): Result<List<SteamGridDbGrid>> = get(
        url = baseUrl.newBuilder()
            .addPathSegments("grids/game")
            .addPathSegment(gameId.toString())
            .addQueryParameter("dimensions", "512x512,1024x1024")
            .build(),
        type = object : TypeToken<SteamGridDbResponse<SteamGridDbGrid>>() {}.type
    )

    suspend fun download(url: String): Result<ByteArray> = withContext(ioDispatcher) {
        val request = runCatching { Request.Builder().url(url).build() }.getOrElse {
            return@withContext Result.failure(SteamGridDbException(SteamGridDbException.Reason.SERVER, it))
        }
        execute(request, notFoundIsEmpty = false) { it.body.bytes() }
    }

    private suspend fun <T> get(url: HttpUrl, type: Type): Result<List<T>> = withContext(ioDispatcher) {
        val key = apiKey()?.trim()
        if (key.isNullOrEmpty()) {
            return@withContext Result.failure(SteamGridDbException(SteamGridDbException.Reason.MISSING_API_KEY))
        }
        val request = Request.Builder().url(url).header("Authorization", "Bearer $key").build()
        execute(request, notFoundIsEmpty = true) { response ->
            if (response.code == 404) return@execute emptyList()
            gson.fromJson<SteamGridDbResponse<T>>(response.body.charStream(), type)?.data.orEmpty().filterNotNull()
        }
    }

    private inline fun <T> execute(request: Request, notFoundIsEmpty: Boolean, parse: (Response) -> T): Result<T> = try {
        httpClient.newCall(request).execute().use { response ->
            when {
                response.code == 401 || response.code == 403 ->
                    Result.failure(SteamGridDbException(SteamGridDbException.Reason.UNAUTHORIZED))
                response.isSuccessful || (notFoundIsEmpty && response.code == 404) ->
                    Result.success(parse(response))
                else -> Result.failure(SteamGridDbException(SteamGridDbException.Reason.SERVER))
            }
        }
    } catch (e: IOException) {
        Result.failure(SteamGridDbException(SteamGridDbException.Reason.NETWORK, e))
    } catch (e: RuntimeException) {
        Result.failure(SteamGridDbException(SteamGridDbException.Reason.SERVER, e))
    }

    private companion object {
        const val DEFAULT_BASE_URL = "https://www.steamgriddb.com/api/v2/"
    }
}
