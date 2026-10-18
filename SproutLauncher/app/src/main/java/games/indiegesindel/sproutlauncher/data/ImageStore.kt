package games.indiegesindel.sproutlauncher.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ImageStore(
    private val directory: File,
    private val contentResolver: ContentResolver,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun importWallpaper(source: Uri): Uri? = withContext(ioDispatcher) {
        val file = copy(source, "$WALLPAPER_PREFIX${System.currentTimeMillis()}") ?: return@withContext null
        deleteMatching { it.name.startsWith(WALLPAPER_PREFIX) && it != file }
        Uri.fromFile(file)
    }

    suspend fun clearWallpapers() = withContext(ioDispatcher) {
        deleteMatching { it.name.startsWith(WALLPAPER_PREFIX) }
    }

    suspend fun importIcon(source: Uri): Uri? = withContext(ioDispatcher) {
        copy(source, "$CUSTOM_ICON_PREFIX${UUID.randomUUID()}")?.let(Uri::fromFile)
    }

    suspend fun saveIcon(bytes: ByteArray, name: String): Uri = withContext(ioDispatcher) {
        File(directory, name).also { it.writeBytes(bytes) }.let(Uri::fromFile)
    }

    fun saveShortcutIcon(bitmap: Bitmap): Uri {
        val file = File(directory, "$SHORTCUT_ICON_PREFIX${UUID.randomUUID()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Uri.fromFile(file)
    }

    suspend fun pruneIcons(referencedUris: Set<String>) = withContext(ioDispatcher) {
        val cutoff = System.currentTimeMillis() - PRUNE_GRACE_PERIOD_MS
        deleteMatching { file ->
            ICON_PREFIXES.any(file.name::startsWith) &&
                file.lastModified() < cutoff &&
                Uri.fromFile(file).toString() !in referencedUris
        }
    }

    private fun copy(source: Uri, name: String): File? {
        val target = File(directory, name)
        return try {
            val input = contentResolver.openInputStream(source) ?: return null
            input.use { stream -> target.outputStream().use { stream.copyTo(it) } }
            target
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy $source", e)
            target.delete()
            null
        }
    }

    private fun deleteMatching(predicate: (File) -> Boolean) {
        directory.listFiles()?.filter(predicate)?.forEach { file ->
            if (!file.delete()) Log.w(TAG, "Could not delete ${file.name}")
        }
    }

    companion object {
        const val STEAMGRIDDB_ICON_PREFIX = "steamgriddb_"
        private const val WALLPAPER_PREFIX = "wallpaper_"
        private const val CUSTOM_ICON_PREFIX = "custom_icon_"
        private const val SHORTCUT_ICON_PREFIX = "shortcut_icon_"
        private val ICON_PREFIXES = listOf(STEAMGRIDDB_ICON_PREFIX, CUSTOM_ICON_PREFIX, SHORTCUT_ICON_PREFIX)
        private const val PRUNE_GRACE_PERIOD_MS = 60 * 60 * 1000L
        private const val TAG = "ImageStore"
    }
}
