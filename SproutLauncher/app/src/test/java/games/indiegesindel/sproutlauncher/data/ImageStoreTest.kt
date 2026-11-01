package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ImageStoreTest {
    private lateinit var context: Context
    private lateinit var directory: File
    private lateinit var store: ImageStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        directory = File(context.cacheDir, "image_store_test").apply {
            deleteRecursively()
            mkdirs()
        }
        store = ImageStore(directory, context.contentResolver, Dispatchers.Unconfined)
    }

    @Test
    fun importWallpaper_replacesPreviousWallpaperOnly() = runBlocking {
        val old = file("wallpaper_1")
        val unrelated = file("keep.png")

        val imported = store.importWallpaper(Uri.fromFile(file("source.png", "image")))

        assertThat(imported).isNotNull()
        assertThat(File(imported!!.path!!).readText()).isEqualTo("image")
        assertThat(old.exists()).isFalse()
        assertThat(unrelated.exists()).isTrue()
    }

    @Test
    fun importWallpaper_keepsPreviousWhenCopyFails() = runBlocking {
        val old = file("wallpaper_1")

        val imported = store.importWallpaper(Uri.fromFile(File(directory, "missing.png")))

        assertThat(imported).isNull()
        assertThat(old.exists()).isTrue()
    }

    @Test
    fun clearWallpapers_removesOnlyWallpapers() = runBlocking {
        val wallpaper = file("wallpaper_2")
        val icon = file("custom_icon_1")

        store.clearWallpapers()

        assertThat(wallpaper.exists()).isFalse()
        assertThat(icon.exists()).isTrue()
    }

    @Test
    fun pruneIcons_deletesOldUnreferencedIconsOnly() = runBlocking {
        val referenced = file("custom_icon_used", age = TWO_HOURS)
        val orphaned = file("steamgriddb_1.png", age = TWO_HOURS)
        val recentOrphan = file("shortcut_icon_new.png")
        val wallpaper = file("wallpaper_1", age = TWO_HOURS)

        store.pruneIcons(setOf(Uri.fromFile(referenced).toString()))

        assertThat(referenced.exists()).isTrue()
        assertThat(orphaned.exists()).isFalse()
        assertThat(recentOrphan.exists()).isTrue()
        assertThat(wallpaper.exists()).isTrue()
    }

    private fun file(name: String, content: String = "x", age: Long = 0): File =
        File(directory, name).apply {
            writeText(content)
            if (age > 0) setLastModified(System.currentTimeMillis() - age)
        }

    private companion object {
        const val TWO_HOURS = 2 * 60 * 60 * 1000L
    }
}
