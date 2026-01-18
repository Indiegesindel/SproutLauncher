package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE)

    private val isPhone = context.resources.configuration.smallestScreenWidthDp < 600
    private val defaultRows = if (isPhone) 1 else 2
    private val defaultSpacing = 8

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _homeScreenRows = MutableStateFlow(loadHomeScreenRows())
    val homeScreenRows: StateFlow<Int> = _homeScreenRows.asStateFlow()

    private val _horizontalSpacing = MutableStateFlow(loadHorizontalSpacing())
    val horizontalSpacing: StateFlow<Int> = _horizontalSpacing.asStateFlow()

    private val _verticalSpacing = MutableStateFlow(loadVerticalSpacing())
    val verticalSpacing: StateFlow<Int> = _verticalSpacing.asStateFlow()

    private val _wallpaperUri = MutableStateFlow(loadWallpaperUri())
    val wallpaperUri: StateFlow<String?> = _wallpaperUri.asStateFlow()

    private val _wallpaperDim = MutableStateFlow(loadWallpaperDim())
    val wallpaperDim: StateFlow<Float> = _wallpaperDim.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME -> _theme.value = loadTheme()
            KEY_HOME_SCREEN_ROWS -> _homeScreenRows.value = loadHomeScreenRows()
            KEY_HORIZONTAL_SPACING -> _horizontalSpacing.value = loadHorizontalSpacing()
            KEY_VERTICAL_SPACING -> _verticalSpacing.value = loadVerticalSpacing()
            KEY_WALLPAPER_URI -> _wallpaperUri.value = loadWallpaperUri()
            KEY_WALLPAPER_DIM -> _wallpaperDim.value = loadWallpaperDim()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_HOME_SCREEN_ROWS = "home_screen_rows"
        private const val KEY_HORIZONTAL_SPACING = "horizontal_spacing"
        private const val KEY_VERTICAL_SPACING = "vertical_spacing"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WALLPAPER_DIM = "wallpaper_dim"
    }

    private fun loadTheme(): AppTheme {
        val themeName = sharedPreferences.getString(KEY_THEME, AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName!!)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun setTheme(theme: AppTheme) {
        sharedPreferences.edit { putString(KEY_THEME, theme.name) }
        _theme.value = theme
    }

    private fun loadHomeScreenRows(): Int {
        return sharedPreferences.getInt(KEY_HOME_SCREEN_ROWS, defaultRows)
    }

    fun setHomeScreenRows(rows: Int) {
        sharedPreferences.edit { putInt(KEY_HOME_SCREEN_ROWS, rows) }
        _homeScreenRows.value = rows
    }

    private fun loadHorizontalSpacing(): Int {
        return sharedPreferences.getInt(KEY_HORIZONTAL_SPACING, defaultSpacing)
    }

    fun setHorizontalSpacing(spacing: Int) {
        sharedPreferences.edit { putInt(KEY_HORIZONTAL_SPACING, spacing) }
        _horizontalSpacing.value = spacing
    }

    private fun loadVerticalSpacing(): Int {
        return sharedPreferences.getInt(KEY_VERTICAL_SPACING, defaultSpacing)
    }

    fun setVerticalSpacing(spacing: Int) {
        sharedPreferences.edit { putInt(KEY_VERTICAL_SPACING, spacing) }
        _verticalSpacing.value = spacing
    }

    private fun loadWallpaperUri(): String? {
        return sharedPreferences.getString(KEY_WALLPAPER_URI, null)
    }

    fun setWallpaperUri(uri: String?) {
        sharedPreferences.edit { putString(KEY_WALLPAPER_URI, uri) }
        _wallpaperUri.value = uri
    }

    private fun loadWallpaperDim(): Float {
        return sharedPreferences.getFloat(KEY_WALLPAPER_DIM, 0f)
    }

    fun setWallpaperDim(dim: Float) {
        sharedPreferences.edit { putFloat(KEY_WALLPAPER_DIM, dim) }
        _wallpaperDim.value = dim
    }
}
