package games.indiegesindel.sproutlauncher.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.data.BaseTheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val PurpleLightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val PurpleDarkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val MintGreenLightScheme = lightColorScheme(
    primary = mintGreenPrimaryLight,
    onPrimary = mintGreenOnPrimaryLight,
    primaryContainer = mintGreenPrimaryContainerLight,
    onPrimaryContainer = mintGreenOnPrimaryContainerLight,
    secondary = mintGreenSecondaryLight,
    onSecondary = mintGreenOnSecondaryLight,
    secondaryContainer = mintGreenSecondaryContainerLight,
    onSecondaryContainer = mintGreenOnSecondaryContainerLight,
    background = mintGreenBackgroundLight,
    onBackground = mintGreenOnBackgroundLight,
    surface = mintGreenSurfaceLight,
    onSurface = mintGreenOnSurfaceLight,
    surfaceVariant = mintGreenSurfaceVariantLight,
    onSurfaceVariant = mintGreenOnSurfaceVariantLight,
    outline = mintGreenOutlineLight,
    outlineVariant = mintGreenOutlineVariantLight,
    surfaceContainerLowest = mintGreenSurfaceContainerLowestLight,
    surfaceContainerLow = mintGreenSurfaceContainerLowLight,
    surfaceContainer = mintGreenSurfaceContainerLight,
    surfaceContainerHigh = mintGreenSurfaceContainerHighLight,
    surfaceContainerHighest = mintGreenSurfaceContainerHighestLight,
)

private val MintGreenDarkScheme = darkColorScheme(
    primary = mintGreenPrimaryDark,
    onPrimary = mintGreenOnPrimaryDark,
    primaryContainer = mintGreenPrimaryContainerDark,
    onPrimaryContainer = mintGreenOnPrimaryContainerDark,
    secondary = mintGreenSecondaryDark,
    onSecondary = mintGreenOnSecondaryDark,
    secondaryContainer = mintGreenSecondaryContainerDark,
    onSecondaryContainer = mintGreenOnSecondaryContainerDark,
    background = mintGreenBackgroundDark,
    onBackground = mintGreenOnBackgroundDark,
    surface = mintGreenSurfaceDark,
    onSurface = mintGreenOnSurfaceDark,
    surfaceVariant = mintGreenSurfaceVariantDark,
    onSurfaceVariant = mintGreenOnSurfaceVariantDark,
    outline = mintGreenOutlineDark,
    outlineVariant = mintGreenOutlineVariantDark,
    surfaceContainerLowest = mintGreenSurfaceContainerLowestDark,
    surfaceContainerLow = mintGreenSurfaceContainerLowDark,
    surfaceContainer = mintGreenSurfaceContainerDark,
    surfaceContainerHigh = mintGreenSurfaceContainerHighDark,
    surfaceContainerHighest = mintGreenSurfaceContainerHighestDark,
)

private val DeepBlueLightScheme = lightColorScheme(
    primary = deepBluePrimaryLight,
    onPrimary = deepBlueOnPrimaryLight,
    primaryContainer = deepBluePrimaryContainerLight,
    onPrimaryContainer = deepBlueOnPrimaryContainerLight,
    secondary = deepBlueSecondaryLight,
    onSecondary = deepBlueOnSecondaryLight,
    secondaryContainer = deepBlueSecondaryContainerLight,
    onSecondaryContainer = deepBlueOnSecondaryContainerLight,
    background = deepBlueBackgroundLight,
    onBackground = deepBlueOnBackgroundLight,
    surface = deepBlueSurfaceLight,
    onSurface = deepBlueOnSurfaceLight,
    surfaceVariant = deepBlueSurfaceVariantLight,
    onSurfaceVariant = deepBlueOnSurfaceVariantLight,
    outline = deepBlueOutlineLight,
    outlineVariant = deepBlueOutlineVariantLight,
    surfaceContainerLowest = deepBlueSurfaceContainerLowestLight,
    surfaceContainerLow = deepBlueSurfaceContainerLowLight,
    surfaceContainer = deepBlueSurfaceContainerLight,
    surfaceContainerHigh = deepBlueSurfaceContainerHighLight,
    surfaceContainerHighest = deepBlueSurfaceContainerHighestLight,
)

private val DeepBlueDarkScheme = darkColorScheme(
    primary = deepBluePrimaryDark,
    onPrimary = deepBlueOnPrimaryDark,
    primaryContainer = deepBluePrimaryContainerDark,
    onPrimaryContainer = deepBlueOnPrimaryContainerDark,
    secondary = deepBlueSecondaryDark,
    onSecondary = deepBlueOnSecondaryDark,
    secondaryContainer = deepBlueSecondaryContainerDark,
    onSecondaryContainer = deepBlueOnSecondaryContainerDark,
    background = deepBlueBackgroundDark,
    onBackground = deepBlueOnBackgroundDark,
    surface = deepBlueSurfaceDark,
    onSurface = deepBlueOnSurfaceDark,
    surfaceVariant = deepBlueSurfaceVariantDark,
    onSurfaceVariant = deepBlueOnSurfaceVariantDark,
    outline = deepBlueOutlineDark,
    outlineVariant = deepBlueOutlineVariantDark,
    surfaceContainerLowest = deepBlueSurfaceContainerLowestDark,
    surfaceContainerLow = deepBlueSurfaceContainerLowDark,
    surfaceContainer = deepBlueSurfaceContainerDark,
    surfaceContainerHigh = deepBlueSurfaceContainerHighDark,
    surfaceContainerHighest = deepBlueSurfaceContainerHighestDark,
)

private val FireRedLightScheme = lightColorScheme(
    primary = fireRedPrimaryLight,
    onPrimary = fireRedOnPrimaryLight,
    primaryContainer = fireRedPrimaryContainerLight,
    onPrimaryContainer = fireRedOnPrimaryContainerLight,
    secondary = fireRedSecondaryLight,
    onSecondary = fireRedOnSecondaryLight,
    secondaryContainer = fireRedSecondaryContainerLight,
    onSecondaryContainer = fireRedOnSecondaryContainerLight,
    background = fireRedBackgroundLight,
    onBackground = fireRedOnBackgroundLight,
    surface = fireRedSurfaceLight,
    onSurface = fireRedOnSurfaceLight,
    surfaceVariant = fireRedSurfaceVariantLight,
    onSurfaceVariant = fireRedOnSurfaceVariantLight,
    outline = fireRedOutlineLight,
    outlineVariant = fireRedOutlineVariantLight,
    surfaceContainerLowest = fireRedSurfaceContainerLowestLight,
    surfaceContainerLow = fireRedSurfaceContainerLowLight,
    surfaceContainer = fireRedSurfaceContainerLight,
    surfaceContainerHigh = fireRedSurfaceContainerHighLight,
    surfaceContainerHighest = fireRedSurfaceContainerHighestLight,
)

private val FireRedDarkScheme = darkColorScheme(
    primary = fireRedPrimaryDark,
    onPrimary = fireRedOnPrimaryDark,
    primaryContainer = fireRedPrimaryContainerDark,
    onPrimaryContainer = fireRedOnPrimaryContainerDark,
    secondary = fireRedSecondaryDark,
    onSecondary = fireRedOnSecondaryDark,
    secondaryContainer = fireRedSecondaryContainerDark,
    onSecondaryContainer = fireRedOnSecondaryContainerDark,
    background = fireRedBackgroundDark,
    onBackground = fireRedOnBackgroundDark,
    surface = fireRedSurfaceDark,
    onSurface = fireRedOnSurfaceDark,
    surfaceVariant = fireRedSurfaceVariantDark,
    onSurfaceVariant = fireRedOnSurfaceVariantDark,
    outline = fireRedOutlineDark,
    outlineVariant = fireRedOutlineVariantDark,
    surfaceContainerLowest = fireRedSurfaceContainerLowestDark,
    surfaceContainerLow = fireRedSurfaceContainerLowDark,
    surfaceContainer = fireRedSurfaceContainerDark,
    surfaceContainerHigh = fireRedSurfaceContainerHighDark,
    surfaceContainerHighest = fireRedSurfaceContainerHighestDark,
)

private val OrangeLightScheme = lightColorScheme(
    primary = orangePrimaryLight,
    onPrimary = orangeOnPrimaryLight,
    primaryContainer = orangePrimaryContainerLight,
    onPrimaryContainer = orangeOnPrimaryContainerLight,
    secondary = orangeSecondaryLight,
    onSecondary = orangeOnSecondaryLight,
    secondaryContainer = orangeSecondaryContainerLight,
    onSecondaryContainer = orangeOnSecondaryContainerLight,
    background = orangeBackgroundLight,
    onBackground = orangeOnBackgroundLight,
    surface = orangeSurfaceLight,
    onSurface = orangeOnSurfaceLight,
    surfaceVariant = orangeSurfaceVariantLight,
    onSurfaceVariant = orangeOnSurfaceVariantLight,
    outline = orangeOutlineLight,
    outlineVariant = orangeOutlineVariantLight,
    surfaceContainerLowest = orangeSurfaceContainerLowestLight,
    surfaceContainerLow = orangeSurfaceContainerLowLight,
    surfaceContainer = orangeSurfaceContainerLight,
    surfaceContainerHigh = orangeSurfaceContainerHighLight,
    surfaceContainerHighest = orangeSurfaceContainerHighestLight,
)

private val OrangeDarkScheme = darkColorScheme(
    primary = orangePrimaryDark,
    onPrimary = orangeOnPrimaryDark,
    primaryContainer = orangePrimaryContainerDark,
    onPrimaryContainer = orangeOnPrimaryContainerDark,
    secondary = orangeSecondaryDark,
    onSecondary = orangeOnSecondaryDark,
    secondaryContainer = orangeSecondaryContainerDark,
    onSecondaryContainer = orangeOnSecondaryContainerDark,
    background = orangeBackgroundDark,
    onBackground = orangeOnBackgroundDark,
    surface = orangeSurfaceDark,
    onSurface = orangeOnSurfaceDark,
    surfaceVariant = orangeSurfaceVariantDark,
    onSurfaceVariant = orangeOnSurfaceVariantDark,
    outline = orangeOutlineDark,
    outlineVariant = orangeOutlineVariantDark,
    surfaceContainerLowest = orangeSurfaceContainerLowestDark,
    surfaceContainerLow = orangeSurfaceContainerLowDark,
    surfaceContainer = orangeSurfaceContainerDark,
    surfaceContainerHigh = orangeSurfaceContainerHighDark,
    surfaceContainerHighest = orangeSurfaceContainerHighestDark,
)

private val MonochromeLightScheme = lightColorScheme(
    primary = monochromePrimaryLight,
    onPrimary = monochromeOnPrimaryLight,
    primaryContainer = monochromePrimaryContainerLight,
    onPrimaryContainer = monochromeOnPrimaryContainerLight,
    secondary = monochromeSecondaryLight,
    onSecondary = monochromeOnSecondaryLight,
    secondaryContainer = monochromeSecondaryContainerLight,
    onSecondaryContainer = monochromeOnSecondaryContainerLight,
    background = monochromeBackgroundLight,
    onBackground = monochromeOnBackgroundLight,
    surface = monochromeSurfaceLight,
    onSurface = monochromeOnSurfaceLight,
    surfaceVariant = monochromeSurfaceVariantLight,
    onSurfaceVariant = monochromeOnSurfaceVariantLight,
    outline = monochromeOutlineLight,
    outlineVariant = monochromeOutlineVariantLight,
    surfaceContainerLowest = monochromeSurfaceContainerLowestLight,
    surfaceContainerLow = monochromeSurfaceContainerLowLight,
    surfaceContainer = monochromeSurfaceContainerLight,
    surfaceContainerHigh = monochromeSurfaceContainerHighLight,
    surfaceContainerHighest = monochromeSurfaceContainerHighestLight,
)

private val MonochromeDarkScheme = darkColorScheme(
    primary = monochromePrimaryDark,
    onPrimary = monochromeOnPrimaryDark,
    primaryContainer = monochromePrimaryContainerDark,
    onPrimaryContainer = monochromeOnPrimaryContainerDark,
    secondary = monochromeSecondaryDark,
    onSecondary = monochromeOnSecondaryDark,
    secondaryContainer = monochromeSecondaryContainerDark,
    onSecondaryContainer = monochromeOnSecondaryContainerDark,
    background = monochromeBackgroundDark,
    onBackground = monochromeOnBackgroundDark,
    surface = monochromeSurfaceDark,
    onSurface = monochromeOnSurfaceDark,
    surfaceVariant = monochromeSurfaceVariantDark,
    onSurfaceVariant = monochromeOnSurfaceVariantDark,
    outline = monochromeOutlineDark,
    outlineVariant = monochromeOutlineVariantDark,
    surfaceContainerLowest = monochromeSurfaceContainerLowestDark,
    surfaceContainerLow = monochromeSurfaceContainerLowDark,
    surfaceContainer = monochromeSurfaceContainerDark,
    surfaceContainerHigh = monochromeSurfaceContainerHighDark,
    surfaceContainerHighest = monochromeSurfaceContainerHighestDark,
)

private val RosePinkLightScheme = lightColorScheme(
    primary = rosePinkPrimaryLight,
    onPrimary = rosePinkOnPrimaryLight,
    primaryContainer = rosePinkPrimaryContainerLight,
    onPrimaryContainer = rosePinkOnPrimaryContainerLight,
    secondary = rosePinkSecondaryLight,
    onSecondary = rosePinkOnSecondaryLight,
    secondaryContainer = rosePinkSecondaryContainerLight,
    onSecondaryContainer = rosePinkOnSecondaryContainerLight,
    background = rosePinkBackgroundLight,
    onBackground = rosePinkOnBackgroundLight,
    surface = rosePinkSurfaceLight,
    onSurface = rosePinkOnSurfaceLight,
    surfaceVariant = rosePinkSurfaceVariantLight,
    onSurfaceVariant = rosePinkOnSurfaceVariantLight,
    outline = rosePinkOutlineLight,
    outlineVariant = rosePinkOutlineVariantLight,
    surfaceContainerLowest = rosePinkSurfaceContainerLowestLight,
    surfaceContainerLow = rosePinkSurfaceContainerLowLight,
    surfaceContainer = rosePinkSurfaceContainerLight,
    surfaceContainerHigh = rosePinkSurfaceContainerHighLight,
    surfaceContainerHighest = rosePinkSurfaceContainerHighestLight,
)

private val RosePinkDarkScheme = darkColorScheme(
    primary = rosePinkPrimaryDark,
    onPrimary = rosePinkOnPrimaryDark,
    primaryContainer = rosePinkPrimaryContainerDark,
    onPrimaryContainer = rosePinkOnPrimaryContainerDark,
    secondary = rosePinkSecondaryDark,
    onSecondary = rosePinkOnSecondaryDark,
    secondaryContainer = rosePinkSecondaryContainerDark,
    onSecondaryContainer = rosePinkOnSecondaryContainerDark,
    background = rosePinkBackgroundDark,
    onBackground = rosePinkOnBackgroundDark,
    surface = rosePinkSurfaceDark,
    onSurface = rosePinkOnSurfaceDark,
    surfaceVariant = rosePinkSurfaceVariantDark,
    onSurfaceVariant = rosePinkOnSurfaceVariantDark,
    outline = rosePinkOutlineDark,
    outlineVariant = rosePinkOutlineVariantDark,
    surfaceContainerLowest = rosePinkSurfaceContainerLowestDark,
    surfaceContainerLow = rosePinkSurfaceContainerLowDark,
    surfaceContainer = rosePinkSurfaceContainerDark,
    surfaceContainerHigh = rosePinkSurfaceContainerHighDark,
    surfaceContainerHighest = rosePinkSurfaceContainerHighestDark,
)

private val TealLightScheme = lightColorScheme(
    primary = tealPrimaryLight,
    onPrimary = tealOnPrimaryLight,
    primaryContainer = tealPrimaryContainerLight,
    onPrimaryContainer = tealOnPrimaryContainerLight,
    secondary = tealSecondaryLight,
    onSecondary = tealOnSecondaryLight,
    secondaryContainer = tealSecondaryContainerLight,
    onSecondaryContainer = tealOnSecondaryContainerLight,
    background = tealBackgroundLight,
    onBackground = tealOnBackgroundLight,
    surface = tealSurfaceLight,
    onSurface = tealOnSurfaceLight,
    surfaceVariant = tealSurfaceVariantLight,
    onSurfaceVariant = tealOnSurfaceVariantLight,
    outline = tealOutlineLight,
    outlineVariant = tealOutlineVariantLight,
    surfaceContainerLowest = tealSurfaceContainerLowestLight,
    surfaceContainerLow = tealSurfaceContainerLowLight,
    surfaceContainer = tealSurfaceContainerLight,
    surfaceContainerHigh = tealSurfaceContainerHighLight,
    surfaceContainerHighest = tealSurfaceContainerHighestLight,
)

private val TealDarkScheme = darkColorScheme(
    primary = tealPrimaryDark,
    onPrimary = tealOnPrimaryDark,
    primaryContainer = tealPrimaryContainerDark,
    onPrimaryContainer = tealOnPrimaryContainerDark,
    secondary = tealSecondaryDark,
    onSecondary = tealOnSecondaryDark,
    secondaryContainer = tealSecondaryContainerDark,
    onSecondaryContainer = tealOnSecondaryContainerDark,
    background = tealBackgroundDark,
    onBackground = tealOnBackgroundDark,
    surface = tealSurfaceDark,
    onSurface = tealOnSurfaceDark,
    surfaceVariant = tealSurfaceVariantDark,
    onSurfaceVariant = tealOnSurfaceVariantDark,
    outline = tealOutlineDark,
    outlineVariant = tealOutlineVariantDark,
    surfaceContainerLowest = tealSurfaceContainerLowestDark,
    surfaceContainerLow = tealSurfaceContainerLowDark,
    surfaceContainer = tealSurfaceContainerDark,
    surfaceContainerHigh = tealSurfaceContainerHighDark,
    surfaceContainerHighest = tealSurfaceContainerHighestDark,
)

val BaseTheme.previewColor: Color?
    get() = when (this) {
        BaseTheme.SYSTEM -> null
        BaseTheme.PURPLE -> primaryLight
        BaseTheme.MINT_GREEN -> mintGreenPrimaryLight
        BaseTheme.DEEP_BLUE -> deepBluePrimaryLight
        BaseTheme.FIRE_RED -> fireRedPrimaryLight
        BaseTheme.REFRESHING_ORANGE -> orangePrimaryLight
        BaseTheme.MONOCHROME -> monochromePrimaryLight
        BaseTheme.ROSE_PINK -> rosePinkPrimaryLight
        BaseTheme.TEAL -> tealPrimaryLight
    }

@Composable
fun SproutLauncherTheme(
    baseTheme: BaseTheme = BaseTheme.SYSTEM,
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(isDarkMode) {
        val barStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDarkMode }
        (context as? ComponentActivity)?.enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        onDispose {}
    }

    val colorScheme = when (baseTheme) {
        BaseTheme.SYSTEM -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            isDarkMode -> DarkColorScheme
            else -> LightColorScheme
        }
        BaseTheme.PURPLE -> if (isDarkMode) PurpleDarkScheme else PurpleLightScheme
        BaseTheme.MINT_GREEN -> if (isDarkMode) MintGreenDarkScheme else MintGreenLightScheme
        BaseTheme.DEEP_BLUE -> if (isDarkMode) DeepBlueDarkScheme else DeepBlueLightScheme
        BaseTheme.FIRE_RED -> if (isDarkMode) FireRedDarkScheme else FireRedLightScheme
        BaseTheme.REFRESHING_ORANGE -> if (isDarkMode) OrangeDarkScheme else OrangeLightScheme
        BaseTheme.MONOCHROME -> if (isDarkMode) MonochromeDarkScheme else MonochromeLightScheme
        BaseTheme.ROSE_PINK -> if (isDarkMode) RosePinkDarkScheme else RosePinkLightScheme
        BaseTheme.TEAL -> if (isDarkMode) TealDarkScheme else TealLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
