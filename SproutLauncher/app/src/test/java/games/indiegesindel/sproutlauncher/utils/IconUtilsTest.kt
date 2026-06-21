package games.indiegesindel.sproutlauncher.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.BitmapDrawable
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconUtilsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun getUnmaskedDrawable_returns_same_for_non_adaptive() {
        val color = ColorDrawable(Color.RED)
        val result = IconUtils.getUnmaskedDrawable(context, color, size = 64)
        // For non-adaptive, the same instance should be returned
        assertThat(result).isSameInstanceAs(color)
    }

    @Ignore("AdaptiveIconDrawable not fully supported under Robolectric; covered in instrumented tests if needed")
    @Test
    fun getUnmaskedDrawable_renders_bitmap_for_adaptive() {
        val bg = ColorDrawable(Color.BLUE)
        val fg = ColorDrawable(Color.GREEN)
        val adaptive = AdaptiveIconDrawable(bg, fg)

        val size = 128
        val result = IconUtils.getUnmaskedDrawable(context, adaptive, size)
        assertThat(result).isInstanceOf(BitmapDrawable::class.java)
        val bmp = (result as BitmapDrawable).bitmap
        assertThat(bmp.width).isEqualTo(size)
        assertThat(bmp.height).isEqualTo(size)
    }

    @Test
    fun getFullSquareIcon_handles_missing_package_gracefully() {
        val drawable = IconUtils.getFullSquareIcon(context, "com.missing.package")
        assertThat(drawable).isNull()
    }
}
