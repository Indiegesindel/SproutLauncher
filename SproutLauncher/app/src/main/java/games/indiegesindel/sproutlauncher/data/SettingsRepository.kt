package games.indiegesindel.sproutlauncher.data

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

enum class Accent { TURQUOISE, SPROUT_GREEN, TANGERINE, GRAPE, CORAL, SUN, CUSTOM }

enum class BackgroundMode { PLAIN, WALLPAPER, AMBIENT }

enum class PlainBase { NEUTRAL, COOL, WARM, VIOLET, GREEN, ROSE, CUSTOM }

enum class AmbientSource { SELECTED_APP, ACCENT, CUSTOM }

enum class GridAlignment { START, CENTER }

data class GridLayout(
    val rows: Int,
    val horizontalSpacing: Int,
    val verticalSpacing: Int,
    val roundness: Int,
    val alignment: GridAlignment = GridAlignment.CENTER
)

class GridSettings internal constructor(
    val rows: Setting<Int>,
    val horizontalSpacing: Setting<Int>,
    val verticalSpacing: Setting<Int>,
    val roundness: Setting<Int>,
    val alignment: Setting<GridAlignment>
) {
    val layout: Flow<GridLayout> =
        combine(rows.flow, horizontalSpacing.flow, verticalSpacing.flow, roundness.flow, alignment.flow, ::GridLayout)

    val currentLayout: GridLayout
        get() = GridLayout(rows.value, horizontalSpacing.value, verticalSpacing.value, roundness.value, alignment.value)
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

class SettingsRepository(private val prefs: SharedPreferences, shortScreen: Boolean) {
    private val settings = mutableMapOf<String, Setting<*>>()

    val accent = setting(
        "accent", Accent.TURQUOISE,
        read = { key, default -> enumOrNull<Accent>(getString(key, null)) ?: legacyAccent(getString(LEGACY_THEME_KEY, null)) ?: default },
        write = { key, value -> putString(key, value.name) }
    )
    val customAccent = int("custom_accent", DEFAULT_CUSTOM_ACCENT)
    val isDarkMode = boolean("is_dark_mode", false)
    val uiSoundsEnabled = boolean("ui_sounds_enabled", true)
    val menuMusicEnabled = boolean("menu_music_enabled", true)
    val customMusicUris = setting<List<String>>(
        "custom_music_uris", emptyList(),
        read = { key, default -> getString(key, null)?.lines()?.filter { it.isNotBlank() } ?: default },
        write = { key, value -> if (value.isEmpty()) remove(key) else putString(key, value.joinToString("\n")) }
    )
    val controllerRumbleEnabled = boolean("controller_rumble_enabled", true)

    val homeGrid = GridSettings(
        rows = int("home_screen_rows", if (shortScreen) 1 else 2),
        horizontalSpacing = int("horizontal_spacing", DEFAULT_SPACING),
        verticalSpacing = int("vertical_spacing", DEFAULT_SPACING),
        roundness = int("app_tile_roundness", DEFAULT_ROUNDNESS),
        alignment = enum("home_alignment", GridAlignment.CENTER)
    )
    val groupGrid = GridSettings(
        rows = int("group_rows", 2),
        horizontalSpacing = int("group_horizontal_spacing", DEFAULT_SPACING),
        verticalSpacing = int("group_vertical_spacing", DEFAULT_SPACING),
        roundness = int("group_app_tile_roundness", DEFAULT_ROUNDNESS),
        alignment = enum("group_alignment", GridAlignment.START)
    )

    val wallpaperUri = nullableString("wallpaper_uri")
    val backgroundMode = setting(
        "background_mode", BackgroundMode.PLAIN,
        read = { key, default ->
            enumOrNull<BackgroundMode>(getString(key, null))
                ?: if (getString("wallpaper_uri", null) != null) BackgroundMode.WALLPAPER else default
        },
        write = { key, value -> putString(key, value.name) }
    )
    val ambientSource = setting(
        "ambient_source", AmbientSource.SELECTED_APP,
        read = { key, default -> enumOrNull<AmbientSource>(getString(key, null)) ?: default },
        write = { key, value -> putString(key, value.name) }
    )
    val ambientColor = enum("ambient_color", Accent.CUSTOM)
    val customAmbient = int("custom_ambient", DEFAULT_CUSTOM_AMBIENT)
    val plainBase = enum("plain_base", PlainBase.NEUTRAL)
    val customPlain = int("custom_plain", DEFAULT_CUSTOM_PLAIN)
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

    private inline fun <reified E : Enum<E>> enum(key: String, default: E) =
        setting(key, default, { k, d -> enumOrNull<E>(getString(k, null)) ?: d }, { k, v -> putString(k, v.name) })

    private fun nullableString(key: String) =
        setting<String?>(key, null, { k, d -> getString(k, d) }, { k, v -> putString(k, v) })

    companion object {
        const val PREFS_NAME = "sprout_launcher_settings"
        private const val LEGACY_THEME_KEY = "base_theme"

        private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? =
            name?.let { n -> enumValues<E>().firstOrNull { it.name == n } }

        internal fun legacyAccent(theme: String?): Accent? = when (theme) {
            "MINT_GREEN" -> Accent.SPROUT_GREEN
            "REFRESHING_ORANGE" -> Accent.TANGERINE
            "PURPLE" -> Accent.GRAPE
            "FIRE_RED", "ROSE_PINK" -> Accent.CORAL
            "SYSTEM", "DEEP_BLUE", "TEAL", "MONOCHROME" -> Accent.TURQUOISE
            else -> null
        }
        private const val DEFAULT_SPACING = 24
        private const val DEFAULT_ROUNDNESS = 12
        private const val DEFAULT_CUSTOM_ACCENT = 0xFFF249BF.toInt()
        private const val DEFAULT_CUSTOM_PLAIN = 0xFF1B2226.toInt()
        private const val DEFAULT_CUSTOM_AMBIENT = 0xFF3D8BFF.toInt()
    }
}
