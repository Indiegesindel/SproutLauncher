package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileUtilsTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clean up any wallpaper_ files prior to tests
        context.filesDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun deleteInternalFile_deletes_when_exists_and_returns_true() {
        val file = File(context.filesDir, "test.txt")
        file.writeText("hello")
        assertThat(file.exists()).isTrue()

        val result = FileUtils.deleteInternalFile(context, "test.txt")
        assertThat(result).isTrue()
        assertThat(file.exists()).isFalse()
    }

    @Test
    fun deleteInternalFile_returns_true_when_file_missing() {
        val result = FileUtils.deleteInternalFile(context, "missing.txt")
        assertThat(result).isTrue()
    }

    @Test
    fun cleanupWallpaperFiles_removes_wallpaper_prefixed_files_only() {
        val keep = File(context.filesDir, "keep.txt").apply { writeText("k") }
        val wall1 = File(context.filesDir, "wallpaper_1.png").apply { writeText("w1") }
        val wall2 = File(context.filesDir, "wallpaper_2.jpg").apply { writeText("w2") }

        FileUtils.cleanupWallpaperFiles(context)

        assertThat(keep.exists()).isTrue()
        assertThat(wall1.exists()).isFalse()
        assertThat(wall2.exists()).isFalse()
    }
}
