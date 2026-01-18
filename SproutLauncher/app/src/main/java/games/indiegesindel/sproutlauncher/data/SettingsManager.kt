package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == KEY_THEME) {
            _theme.value = loadTheme()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val KEY_THEME = "app_theme"
    }

    private fun loadTheme(): AppTheme {
        val themeName = sharedPreferences.getString(KEY_THEME, AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun setTheme(theme: AppTheme) {
        sharedPreferences.edit().putString(KEY_THEME, theme.name).apply()
        _theme.value = theme
    }
}
