package games.indiegesindel.sproutlauncher.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsManagerTest {
    private lateinit var context: Context
    private lateinit var settings: SettingsManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE).edit().clear().commit()
        settings = SettingsManager(context)
    }

    @Test
    fun default_values_loaded_from_prefs_or_defaults() = runTest(UnconfinedTestDispatcher()) {
        val expectedRows = if (context.resources.configuration.smallestScreenWidthDp < 600) 1 else 2
        assertThat(settings.baseTheme.first()).isNotNull()
        assertThat(settings.isDarkMode.first()).isFalse()
        assertThat(settings.homeScreenRows.first()).isEqualTo(expectedRows)
        assertThat(settings.horizontalSpacing.first()).isEqualTo(24)
        assertThat(settings.verticalSpacing.first()).isEqualTo(24)
        assertThat(settings.appTileRoundness.first()).isEqualTo(12)
        assertThat(settings.wallpaperUri.first()).isNull()
        assertThat(settings.wallpaperDim.first()).isEqualTo(0f)
        assertThat(settings.showYouTube.first()).isTrue()
        assertThat(settings.showDiscord.first()).isTrue()
        assertThat(settings.showSpotify.first()).isTrue()
    }

    @Test
    fun setters_update_stateflows_and_prefs() = runTest(UnconfinedTestDispatcher()) {
        settings.setIsDarkMode(true)
        settings.setHomeScreenRows(3)
        settings.setHorizontalSpacing(12)
        settings.setVerticalSpacing(16)
        settings.setAppTileRoundness(24)
        settings.setWallpaperUri("file://wall.png")
        settings.setWallpaperDim(0.5f)
        settings.setShowYouTube(false)
        settings.setShowDiscord(false)
        settings.setShowSpotify(false)

        assertThat(settings.isDarkMode.first()).isTrue()
        assertThat(settings.homeScreenRows.first()).isEqualTo(3)
        assertThat(settings.horizontalSpacing.first()).isEqualTo(12)
        assertThat(settings.verticalSpacing.first()).isEqualTo(16)
        assertThat(settings.appTileRoundness.first()).isEqualTo(24)
        assertThat(settings.wallpaperUri.first()).isEqualTo("file://wall.png")
        assertThat(settings.wallpaperDim.first()).isEqualTo(0.5f)
        assertThat(settings.showYouTube.first()).isFalse()
        assertThat(settings.showDiscord.first()).isFalse()
        assertThat(settings.showSpotify.first()).isFalse()
    }

    @Test
    fun listener_reacts_to_external_pref_changes() = runTest(UnconfinedTestDispatcher()) {
        val prefs = context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_spotify", false).commit()
        prefs.edit().putInt("home_screen_rows", 4).commit()

        // Allow listener to propagate
        assertThat(settings.showSpotify.first()).isFalse()
        assertThat(settings.homeScreenRows.first()).isEqualTo(4)
    }
}
