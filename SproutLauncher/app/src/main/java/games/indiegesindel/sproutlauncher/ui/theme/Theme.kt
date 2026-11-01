package games.indiegesindel.sproutlauncher.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.data.Accent

internal fun sproutColorScheme(accent: Color, isDark: Boolean): ColorScheme {
    val t = (if (isDark) Ink else Paper).tinted(accent, SURFACE_TINT)
    val onAccent = contentColorFor(accent)
    val container = mix(t.surface, accent, 0.25f)
    val softContainer = mix(t.surface, accent, 0.16f)
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent, onPrimary = onAccent, primaryContainer = container, onPrimaryContainer = t.text,
        inversePrimary = accent,
        secondary = accent, onSecondary = onAccent, secondaryContainer = softContainer, onSecondaryContainer = t.text,
        tertiary = accent, onTertiary = onAccent, tertiaryContainer = softContainer, onTertiaryContainer = t.text,
        background = t.background, onBackground = t.text,
        surface = t.surface, onSurface = t.text,
        surfaceVariant = t.surfaceVariant, onSurfaceVariant = t.text2,
        surfaceTint = accent,
        inverseSurface = t.inverseSurface, inverseOnSurface = t.inverseOnSurface,
        outline = t.text2, outlineVariant = t.surfaceVariant,
        surfaceBright = mix(t.surface, Color.White, if (isDark) 0.06f else 0.5f),
        surfaceDim = t.background,
        surfaceContainerLowest = t.background,
        surfaceContainerLow = mix(t.background, t.surface, 0.5f),
        surfaceContainer = t.surface,
        surfaceContainerHigh = mix(t.surface, t.surfaceVariant, 0.5f),
        surfaceContainerHighest = t.surfaceVariant
    )
}

private const val SURFACE_TINT = 0.06f

private fun BaseTokens.tinted(accent: Color, amount: Float) = copy(
    background = mix(background, accent, amount),
    surface = mix(surface, accent, amount),
    surfaceVariant = mix(surfaceVariant, accent, amount)
)

@Composable
fun SproutLauncherTheme(
    accent: Accent = Accent.TURQUOISE,
    customAccent: Int = Accent.CUSTOM.color.toArgb(),
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val accentColor = accentColor(accent, customAccent)
    val colorScheme = remember(accentColor, isDarkMode) { sproutColorScheme(accentColor, isDarkMode) }

    DisposableEffect(isDarkMode, colorScheme) {
        val barStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDarkMode }
        (context as? ComponentActivity)?.apply {
            enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            window.decorView.setBackgroundColor(colorScheme.background.toArgb())
        }
        onDispose {}
    }

    val sproutColors = remember(accentColor, isDarkMode) {
        SproutColors(accent = accentColor, focus = accentColor, isDark = isDarkMode)
    }
    CompositionLocalProvider(LocalSproutColors provides sproutColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
