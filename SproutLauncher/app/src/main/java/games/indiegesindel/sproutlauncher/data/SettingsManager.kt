package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

enum class BaseTheme {
    SYSTEM, PURPLE, MINT_GREEN, DEEP_BLUE, FIRE_RED, REFRESHING_ORANGE, MONOCHROME
}

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE)

    private val isPhone = context.resources.configuration.smallestScreenWidthDp < 600
    private val defaultRows = if (isPhone) 1 else 2
    private val defaultSpacing = 24
    private val defaultRoundness = 12

    private val _baseTheme = MutableStateFlow(loadBaseTheme())
    val baseTheme: StateFlow<BaseTheme> = _baseTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(loadIsDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _homeScreenRows = MutableStateFlow(loadHomeScreenRows())
    val homeScreenRows: StateFlow<Int> = _homeScreenRows.asStateFlow()

    private val _horizontalSpacing = MutableStateFlow(loadHorizontalSpacing())
    val horizontalSpacing: StateFlow<Int> = _horizontalSpacing.asStateFlow()

    private val _verticalSpacing = MutableStateFlow(loadVerticalSpacing())
    val verticalSpacing: StateFlow<Int> = _verticalSpacing.asStateFlow()

    private val _appTileRoundness = MutableStateFlow(loadAppTileRoundness())
    val appTileRoundness: StateFlow<Int> = _appTileRoundness.asStateFlow()

    private val _groupRows = MutableStateFlow(loadGroupRows())
    val groupRows: StateFlow<Int> = _groupRows.asStateFlow()

    private val _groupHorizontalSpacing = MutableStateFlow(loadGroupHorizontalSpacing())
    val groupHorizontalSpacing: StateFlow<Int> = _groupHorizontalSpacing.asStateFlow()

    private val _groupVerticalSpacing = MutableStateFlow(loadGroupVerticalSpacing())
    val groupVerticalSpacing: StateFlow<Int> = _groupVerticalSpacing.asStateFlow()

    private val _groupAppTileRoundness = MutableStateFlow(loadGroupAppTileRoundness())
    val groupAppTileRoundness: StateFlow<Int> = _groupAppTileRoundness.asStateFlow()

    private val _wallpaperUri = MutableStateFlow(loadWallpaperUri())
    val wallpaperUri: StateFlow<String?> = _wallpaperUri.asStateFlow()

    private val _wallpaperDim = MutableStateFlow(loadWallpaperDim())
    val wallpaperDim: StateFlow<Float> = _wallpaperDim.asStateFlow()

    private val _showYouTube = MutableStateFlow(loadShowYouTube())
    val showYouTube: StateFlow<Boolean> = _showYouTube.asStateFlow()

    private val _showDiscord = MutableStateFlow(loadShowDiscord())
    val showDiscord: StateFlow<Boolean> = _showDiscord.asStateFlow()

    private val _showSpotify = MutableStateFlow(loadShowSpotify())
    val showSpotify: StateFlow<Boolean> = _showSpotify.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_BASE_THEME -> _baseTheme.value = loadBaseTheme()
            KEY_IS_DARK_MODE -> _isDarkMode.value = loadIsDarkMode()
            KEY_HOME_SCREEN_ROWS -> _homeScreenRows.value = loadHomeScreenRows()
            KEY_HORIZONTAL_SPACING -> _horizontalSpacing.value = loadHorizontalSpacing()
            KEY_VERTICAL_SPACING -> _verticalSpacing.value = loadVerticalSpacing()
            KEY_APP_TILE_ROUNDNESS -> _appTileRoundness.value = loadAppTileRoundness()
            KEY_GROUP_ROWS -> _groupRows.value = loadGroupRows()
            KEY_GROUP_HORIZONTAL_SPACING -> _groupHorizontalSpacing.value = loadGroupHorizontalSpacing()
            KEY_GROUP_VERTICAL_SPACING -> _groupVerticalSpacing.value = loadGroupVerticalSpacing()
            KEY_GROUP_APP_TILE_ROUNDNESS -> _groupAppTileRoundness.value = loadGroupAppTileRoundness()
            KEY_WALLPAPER_URI -> _wallpaperUri.value = loadWallpaperUri()
            KEY_WALLPAPER_DIM -> _wallpaperDim.value = loadWallpaperDim()
            KEY_SHOW_YOUTUBE -> _showYouTube.value = loadShowYouTube()
            KEY_SHOW_DISCORD -> _showDiscord.value = loadShowDiscord()
            KEY_SHOW_SPOTIFY -> _showSpotify.value = loadShowSpotify()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val KEY_BASE_THEME = "base_theme"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_HOME_SCREEN_ROWS = "home_screen_rows"
        private const val KEY_HORIZONTAL_SPACING = "horizontal_spacing"
        private const val KEY_VERTICAL_SPACING = "vertical_spacing"
        private const val KEY_APP_TILE_ROUNDNESS = "app_tile_roundness"
        private const val KEY_GROUP_ROWS = "group_rows"
        private const val KEY_GROUP_HORIZONTAL_SPACING = "group_horizontal_spacing"
        private const val KEY_GROUP_VERTICAL_SPACING = "group_vertical_spacing"
        private const val KEY_GROUP_APP_TILE_ROUNDNESS = "group_app_tile_roundness"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WALLPAPER_DIM = "wallpaper_dim"
        private const val KEY_SHOW_YOUTUBE = "show_youtube"
        private const val KEY_SHOW_DISCORD = "show_discord"
        private const val KEY_SHOW_SPOTIFY = "show_spotify"
    }

    private fun loadBaseTheme(): BaseTheme {
        val themeName = sharedPreferences.getString(KEY_BASE_THEME, BaseTheme.SYSTEM.name)
        return try {
            BaseTheme.valueOf(themeName!!)
        } catch (e: Exception) {
            BaseTheme.SYSTEM
        }
    }

    fun setBaseTheme(theme: BaseTheme) {
        sharedPreferences.edit { putString(KEY_BASE_THEME, theme.name) }
        _baseTheme.value = theme
    }

    private fun loadIsDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
    }

    fun setIsDarkMode(isDark: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, isDark) }
        _isDarkMode.value = isDark
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
    
    private fun loadAppTileRoundness(): Int {
        return sharedPreferences.getInt(KEY_APP_TILE_ROUNDNESS, defaultRoundness)
    }

    fun setAppTileRoundness(roundness: Int) {
        sharedPreferences.edit { putInt(KEY_APP_TILE_ROUNDNESS, roundness) }
        _appTileRoundness.value = roundness
    }

    private fun loadGroupRows(): Int {
        return sharedPreferences.getInt(KEY_GROUP_ROWS, 2)
    }

    fun setGroupRows(rows: Int) {
        sharedPreferences.edit { putInt(KEY_GROUP_ROWS, rows) }
        _groupRows.value = rows
    }

    private fun loadGroupHorizontalSpacing(): Int {
        return sharedPreferences.getInt(KEY_GROUP_HORIZONTAL_SPACING, defaultSpacing)
    }

    fun setGroupHorizontalSpacing(spacing: Int) {
        sharedPreferences.edit { putInt(KEY_GROUP_HORIZONTAL_SPACING, spacing) }
        _groupHorizontalSpacing.value = spacing
    }

    private fun loadGroupVerticalSpacing(): Int {
        return sharedPreferences.getInt(KEY_GROUP_VERTICAL_SPACING, defaultSpacing)
    }

    fun setGroupVerticalSpacing(spacing: Int) {
        sharedPreferences.edit { putInt(KEY_GROUP_VERTICAL_SPACING, spacing) }
        _groupVerticalSpacing.value = spacing
    }

    private fun loadGroupAppTileRoundness(): Int {
        return sharedPreferences.getInt(KEY_GROUP_APP_TILE_ROUNDNESS, defaultRoundness)
    }

    fun setGroupAppTileRoundness(roundness: Int) {
        sharedPreferences.edit { putInt(KEY_GROUP_APP_TILE_ROUNDNESS, roundness) }
        _groupAppTileRoundness.value = roundness
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

    private fun loadShowYouTube(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_YOUTUBE, true)
    }

    fun setShowYouTube(show: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_SHOW_YOUTUBE, show) }
        _showYouTube.value = show
    }

    private fun loadShowDiscord(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_DISCORD, true)
    }

    fun setShowDiscord(show: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_SHOW_DISCORD, show) }
        _showDiscord.value = show
    }

    private fun loadShowSpotify(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_SPOTIFY, true)
    }

    fun setShowSpotify(show: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_SHOW_SPOTIFY, show) }
        _showSpotify.value = show
    }
}
