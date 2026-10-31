package games.indiegesindel.sproutlauncher.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager

fun ComponentActivity.setSproutContent(content: @Composable () -> Unit) {
    enableEdgeToEdge()
    val container = appContainer
    setContent {
        val baseTheme by container.settingsRepository.baseTheme.flow.collectAsStateWithLifecycle()
        val isDarkMode by container.settingsRepository.isDarkMode.flow.collectAsStateWithLifecycle()
        SproutLauncherTheme(baseTheme = baseTheme, isDarkMode = isDarkMode) {
            CompositionLocalProvider(LocalSoundManager provides container.soundManager, content = content)
        }
    }
}
