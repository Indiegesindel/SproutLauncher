package games.indiegesindel.sproutlauncher.ui.icons

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconColorTest {

    @Test
    fun dominant_prefersSaturatedColorOverWhiteBackground() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            for (x in 0 until 4) for (y in 0 until 4) setPixel(x, y, Color.rgb(220, 40, 40))
        }

        assertThat(IconColor.dominant(bitmap)).isEqualTo(Color.rgb(220, 40, 40))
    }

    @Test
    fun dominant_isTransparentForGreyscaleIcons() {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GRAY) }

        assertThat(IconColor.dominant(bitmap)).isEqualTo(Color.TRANSPARENT)
    }
}
