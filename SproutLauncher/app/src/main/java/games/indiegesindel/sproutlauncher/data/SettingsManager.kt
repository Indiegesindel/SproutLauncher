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

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _homeScreenRows = MutableStateFlow(loadHomeScreenRows())
    val homeScreenRows: StateFlow<Int> = _homeScreenRows.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME -> _theme.value = loadTheme()
            KEY_HOME_SCREEN_ROWS -> _homeScreenRows.value = loadHomeScreenRows()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_HOME_SCREEN_ROWS = "home_screen_rows"
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
}
