package games.indiegesindel.sproutlauncher.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File

internal object DebugHttp {
    fun configure(builder: OkHttpClient.Builder, filesDir: File): OkHttpClient.Builder = builder.addInterceptor { chain ->
        val root = File(filesDir, "sgdb_fake")
        val url = chain.request().url
        if (!root.isDirectory || (url.host != "www.steamgriddb.com" && url.host != "sgdb.fake")) {
            return@addInterceptor chain.proceed(chain.request())
        }
        val path = url.encodedPath
        val file = when {
            url.host == "sgdb.fake" -> File(root, path.removePrefix("/"))
            "/search/autocomplete/" in path -> File(root, "search.json")
            "/grids/game/" in path -> File(root, "grids_${path.substringAfterLast('/')}.json")
            else -> null
        }
        Thread.sleep(250)
        val builder = Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
        if (file == null || !file.isFile) {
            builder.code(404).message("Not Found").body(ByteArray(0).toResponseBody()).build()
        } else {
            val type = if (file.extension == "json") "application/json" else "image/png"
            builder.code(200).message("OK").body(file.readBytes().toResponseBody(type.toMediaType())).build()
        }
    }
}
