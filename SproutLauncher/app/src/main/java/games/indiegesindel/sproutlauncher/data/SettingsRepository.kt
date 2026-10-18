package games.indiegesindel.sproutlauncher.data

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

enum class BaseTheme {
    SYSTEM, PURPLE, MINT_GREEN, DEEP_BLUE, FIRE_RED, REFRESHING_ORANGE, MONOCHROME, ROSE_PINK, TEAL
}

data class GridLayout(
    val rows: Int,
    val horizontalSpacing: Int,
    val verticalSpacing: Int,
    val roundness: Int
)

class GridSettings internal constructor(
    val rows: Setting<Int>,
    val horizontalSpacing: Setting<Int>,
    val verticalSpacing: Setting<Int>,
    val roundness: Setting<Int>
) {
    val layout: Flow<GridLayout> = combine(rows.flow, horizontalSpacing.flow, verticalSpacing.flow, roundness.flow, ::GridLayout)

    val currentLayout: GridLayout
        get() = GridLayout(rows.value, horizontalSpacing.value, verticalSpacing.value, roundness.value)
}

class Setting<T> internal constructor(
    private val prefs: SharedPreferences,
    val key: String,
    private val default: T,
    private val read: SharedPreferences.(String, T) -> T,
    private val write: SharedPreferences.Editor.(String, T) -> Unit
) {
    private val state = MutableStateFlow(load())
    val flow: StateFlow<T> = state.asStateFlow()
    val value: T get() = state.value

    fun set(value: T) {
        prefs.edit { write(key, value) }
        state.value = value
    }

    internal fun reload() {
        state.value = load()
    }

    private fun load(): T = runCatching { prefs.read(key, default) }.getOrDefault(default)
}

class SettingsRepository(private val prefs: SharedPreferences, isPhone: Boolean) {

    private val settings = mutableMapOf<String, Setting<*>>()

    val baseTheme = setting(
        "base_theme", BaseTheme.SYSTEM,
        read = { key, default -> getString(key, null)?.let { name -> BaseTheme.entries.firstOrNull { it.name == name } } ?: default },
        write = { key, value -> putString(key, value.name) }
    )
    val isDarkMode = boolean("is_dark_mode", false)
    val uiSoundsEnabled = boolean("ui_sounds_enabled", true)

    val homeGrid = GridSettings(
        rows = int("home_screen_rows", if (isPhone) 1 else 2),
        horizontalSpacing = int("horizontal_spacing", DEFAULT_SPACING),
        verticalSpacing = int("vertical_spacing", DEFAULT_SPACING),
        roundness = int("app_tile_roundness", DEFAULT_ROUNDNESS)
    )
    val groupGrid = GridSettings(
        rows = int("group_rows", 2),
        horizontalSpacing = int("group_horizontal_spacing", DEFAULT_SPACING),
        verticalSpacing = int("group_vertical_spacing", DEFAULT_SPACING),
        roundness = int("group_app_tile_roundness", DEFAULT_ROUNDNESS)
    )

    val wallpaperUri = nullableString("wallpaper_uri")
    val wallpaperDim = setting(
        "wallpaper_dim", 0f,
        read = { key, default -> getFloat(key, default) },
        write = { key, value -> putFloat(key, value.coerceIn(0f, 1f)) }
    )

    val showYouTube = boolean("show_youtube", true)
    val showDiscord = boolean("show_discord", true)
    val showSpotify = boolean("show_spotify", true)

    val steamGridDbApiKey = nullableString("steamgriddb_apikey")

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        settings[key]?.reload()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun <T> setting(
        key: String,
        default: T,
        read: SharedPreferences.(String, T) -> T,
        write: SharedPreferences.Editor.(String, T) -> Unit
    ): Setting<T> = Setting(prefs, key, default, read, write).also { settings[key] = it }

    private fun boolean(key: String, default: Boolean) =
        setting(key, default, { k, d -> getBoolean(k, d) }, { k, v -> putBoolean(k, v) })

    private fun int(key: String, default: Int) =
        setting(key, default, { k, d -> getInt(k, d) }, { k, v -> putInt(k, v) })

    private fun nullableString(key: String) =
        setting<String?>(key, null, { k, d -> getString(k, d) }, { k, v -> putString(k, v) })

    companion object {
        const val PREFS_NAME = "sprout_launcher_settings"
        private const val DEFAULT_SPACING = 24
        private const val DEFAULT_ROUNDNESS = 12
    }
}
