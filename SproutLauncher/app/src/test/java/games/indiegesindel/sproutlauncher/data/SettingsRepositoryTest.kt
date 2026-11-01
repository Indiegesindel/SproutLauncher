package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("settings_repository_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun defaults_dependOnFormFactor() {
        val tablet = SettingsRepository(prefs, shortScreen = false)
        assertThat(tablet.accent.value).isEqualTo(Accent.TURQUOISE)
        assertThat(tablet.backgroundMode.value).isEqualTo(BackgroundMode.PLAIN)
        assertThat(tablet.ambientSource.value).isEqualTo(AmbientSource.SELECTED_APP)
        assertThat(tablet.menuMusicEnabled.value).isTrue()
        assertThat(tablet.controllerRumbleEnabled.value).isTrue()
        assertThat(tablet.isDarkMode.value).isFalse()
        assertThat(tablet.uiSoundsEnabled.value).isTrue()
        assertThat(tablet.homeGrid.currentLayout).isEqualTo(GridLayout(rows = 2, horizontalSpacing = 24, verticalSpacing = 24, roundness = 12))
        assertThat(tablet.groupGrid.rows.value).isEqualTo(2)
        assertThat(tablet.wallpaperUri.value).isNull()
        assertThat(tablet.steamGridDbApiKey.value).isNull()

        assertThat(SettingsRepository(prefs, shortScreen = true).homeGrid.rows.value).isEqualTo(1)
    }

    @Test
    fun set_updatesFlowAndPersists() {
        val settings = SettingsRepository(prefs, shortScreen = false)

        settings.accent.set(Accent.GRAPE)
        settings.homeGrid.roundness.set(30)
        settings.wallpaperDim.set(0.4f)
        settings.steamGridDbApiKey.set("key")

        assertThat(settings.accent.flow.value).isEqualTo(Accent.GRAPE)
        val reloaded = SettingsRepository(prefs, shortScreen = false)
        assertThat(reloaded.accent.value).isEqualTo(Accent.GRAPE)
        assertThat(reloaded.homeGrid.roundness.value).isEqualTo(30)
        assertThat(reloaded.wallpaperDim.value).isEqualTo(0.4f)
        assertThat(reloaded.steamGridDbApiKey.value).isEqualTo("key")
    }

    @Test
    fun externalWrites_areObserved() {
        val settings = SettingsRepository(prefs, shortScreen = false)

        prefs.edit().putBoolean("is_dark_mode", true).putInt("group_rows", 4).commit()

        assertThat(settings.isDarkMode.value).isTrue()
        assertThat(settings.groupGrid.rows.value).isEqualTo(4)
    }

    @Test
    fun unreadableValues_fallBackToDefaults() {
        prefs.edit()
            .putString("accent", "REMOVED_ACCENT")
            .putString("home_screen_rows", "not an int")
            .commit()

        val settings = SettingsRepository(prefs, shortScreen = false)

        assertThat(settings.accent.value).isEqualTo(Accent.TURQUOISE)
        assertThat(settings.homeGrid.rows.value).isEqualTo(2)
    }

    @Test
    fun customAccent_persists() {
        val settings = SettingsRepository(prefs, shortScreen = false)

        settings.customAccent.set(0xFF336699.toInt())
        settings.accent.set(Accent.CUSTOM)

        val reloaded = SettingsRepository(prefs, shortScreen = false)
        assertThat(reloaded.accent.value).isEqualTo(Accent.CUSTOM)
        assertThat(reloaded.customAccent.value).isEqualTo(0xFF336699.toInt())
    }

    @Test
    fun customAmbient_persists() {
        val settings = SettingsRepository(prefs, shortScreen = false)

        assertThat(settings.ambientColor.value).isEqualTo(Accent.CUSTOM)
        settings.customAmbient.set(0xFF3D8BFF.toInt())
        settings.ambientSource.set(AmbientSource.CUSTOM)
        settings.ambientColor.set(Accent.GRAPE)

        val reloaded = SettingsRepository(prefs, shortScreen = false)
        assertThat(reloaded.ambientSource.value).isEqualTo(AmbientSource.CUSTOM)
        assertThat(reloaded.ambientColor.value).isEqualTo(Accent.GRAPE)
        assertThat(reloaded.customAmbient.value).isEqualTo(0xFF3D8BFF.toInt())
    }

    @Test
    fun legacyTheme_migratesToAccent() {
        prefs.edit().putString("base_theme", "MINT_GREEN").commit()

        assertThat(SettingsRepository(prefs, shortScreen = false).accent.value).isEqualTo(Accent.SPROUT_GREEN)
    }

    @Test
    fun existingWallpaper_defaultsBackgroundModeToWallpaper() {
        prefs.edit().putString("wallpaper_uri", "file:///wall.png").commit()

        assertThat(SettingsRepository(prefs, shortScreen = false).backgroundMode.value).isEqualTo(BackgroundMode.WALLPAPER)
    }
}
