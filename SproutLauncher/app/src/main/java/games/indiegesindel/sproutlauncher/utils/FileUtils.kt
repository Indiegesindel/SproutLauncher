package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    private const val TAG = "FileUtils"

    fun saveUriToInternalStorage(context: Context, uri: Uri, fileName: String): Uri? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save URI to internal storage", e)
            null
        }
    }

    fun deleteInternalFile(context: Context, fileName: String): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete internal file: $fileName", e)
            false
        }
    }

    fun cleanupWallpaperFiles(context: Context) {
        try {
            context.filesDir.listFiles { file ->
                file.name.startsWith("wallpaper_")
            }?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup wallpaper files", e)
        }
    }
}
