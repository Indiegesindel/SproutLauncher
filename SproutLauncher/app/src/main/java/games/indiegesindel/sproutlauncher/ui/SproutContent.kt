package games.indiegesindel.sproutlauncher.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.utils.LocalControllerRumble
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager

fun ComponentActivity.setSproutContent(content: @Composable () -> Unit) {
    enableEdgeToEdge()
    val container = appContainer
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> container.menuMusic.onForeground()
            Lifecycle.Event.ON_PAUSE -> container.menuMusic.onBackground()
            else -> Unit
        }
    })
    setContent {
        val accent by container.settingsRepository.accent.flow.collectAsStateWithLifecycle()
        val customAccent by container.settingsRepository.customAccent.flow.collectAsStateWithLifecycle()
        val isDarkMode by container.settingsRepository.isDarkMode.flow.collectAsStateWithLifecycle()
        SproutLauncherTheme(accent = accent, customAccent = customAccent, isDarkMode = isDarkMode) {
            CompositionLocalProvider(
                LocalSoundManager provides container.soundManager,
                LocalControllerRumble provides container.controllerRumble,
                content = content
            )
        }
    }
}
