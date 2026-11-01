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
        val tablet = SettingsRepository(prefs, isPhone = false)
        assertThat(tablet.baseTheme.value).isEqualTo(BaseTheme.SYSTEM)
        assertThat(tablet.isDarkMode.value).isFalse()
        assertThat(tablet.uiSoundsEnabled.value).isTrue()
        assertThat(tablet.homeGrid.currentLayout).isEqualTo(GridLayout(rows = 2, horizontalSpacing = 24, verticalSpacing = 24, roundness = 12))
        assertThat(tablet.groupGrid.rows.value).isEqualTo(2)
        assertThat(tablet.wallpaperUri.value).isNull()
        assertThat(tablet.steamGridDbApiKey.value).isNull()

        assertThat(SettingsRepository(prefs, isPhone = true).homeGrid.rows.value).isEqualTo(1)
    }

    @Test
    fun set_updatesFlowAndPersists() {
        val settings = SettingsRepository(prefs, isPhone = false)

        settings.baseTheme.set(BaseTheme.TEAL)
        settings.homeGrid.roundness.set(30)
        settings.wallpaperDim.set(0.4f)
        settings.steamGridDbApiKey.set("key")

        assertThat(settings.baseTheme.flow.value).isEqualTo(BaseTheme.TEAL)
        val reloaded = SettingsRepository(prefs, isPhone = false)
        assertThat(reloaded.baseTheme.value).isEqualTo(BaseTheme.TEAL)
        assertThat(reloaded.homeGrid.roundness.value).isEqualTo(30)
        assertThat(reloaded.wallpaperDim.value).isEqualTo(0.4f)
        assertThat(reloaded.steamGridDbApiKey.value).isEqualTo("key")
    }

    @Test
    fun externalWrites_areObserved() {
        val settings = SettingsRepository(prefs, isPhone = false)

        prefs.edit().putBoolean("is_dark_mode", true).putInt("group_rows", 4).commit()

        assertThat(settings.isDarkMode.value).isTrue()
        assertThat(settings.groupGrid.rows.value).isEqualTo(4)
    }

    @Test
    fun unreadableValues_fallBackToDefaults() {
        prefs.edit()
            .putString("base_theme", "REMOVED_THEME")
            .putString("home_screen_rows", "not an int")
            .commit()

        val settings = SettingsRepository(prefs, isPhone = false)

        assertThat(settings.baseTheme.value).isEqualTo(BaseTheme.SYSTEM)
        assertThat(settings.homeGrid.rows.value).isEqualTo(2)
    }
}
