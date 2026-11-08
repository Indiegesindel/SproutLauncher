package games.indiegesindel.sproutlauncher.data

import okhttp3.OkHttpClient
import java.io.File

internal object DebugHttp {
    fun configure(builder: OkHttpClient.Builder, @Suppress("UNUSED_PARAMETER") filesDir: File): OkHttpClient.Builder = builder
}
