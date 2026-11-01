package games.indiegesindel.sproutlauncher.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import games.indiegesindel.sproutlauncher.data.Accent
import games.indiegesindel.sproutlauncher.data.PlainBase

val Accent.color: Color
    get() = when (this) {
        Accent.TURQUOISE -> Color(0xFF12B0D4)
        Accent.SPROUT_GREEN -> Color(0xFF34B35A)
        Accent.TANGERINE -> Color(0xFFFF7A1A)
        Accent.GRAPE -> Color(0xFF8A4FFF)
        Accent.CORAL -> Color(0xFFFF5A6A)
        Accent.SUN -> Color(0xFFFFBF1F)
        Accent.CUSTOM -> Color(0xFFF249BF)
    }

fun accentColor(accent: Accent, custom: Int): Color = if (accent == Accent.CUSTOM) Color(custom) else accent.color

fun plainBaseColor(base: PlainBase, custom: Int, isDark: Boolean, themed: Color): Color = when (base) {
    PlainBase.NEUTRAL -> themed
    PlainBase.CUSTOM -> Color(custom)
    else -> (if (isDark) PLAIN_DARK else PLAIN_LIGHT).getValue(base)
}

private val PLAIN_DARK = mapOf(
    PlainBase.COOL to Color(0xFF1B2226), PlainBase.WARM to Color(0xFF22201D), PlainBase.VIOLET to Color(0xFF201D24),
    PlainBase.GREEN to Color(0xFF1C2420), PlainBase.ROSE to Color(0xFF241C1F)
)
private val PLAIN_LIGHT = mapOf(
    PlainBase.COOL to Color(0xFFE9F0F2), PlainBase.WARM to Color(0xFFF4ECE4), PlainBase.VIOLET to Color(0xFFEDE9F3),
    PlainBase.GREEN to Color(0xFFE8F1EA), PlainBase.ROSE to Color(0xFFF5E8EA)
)

@Immutable
internal data class BaseTokens(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val text: Color,
    val text2: Color
)

internal val Paper = BaseTokens(
    background = Color(0xFFF2EFE9),
    surface = Color(0xFFFBFAF7),
    surfaceVariant = Color(0xFFE5E1D8),
    inverseSurface = Color(0xFF2B2A28),
    inverseOnSurface = Color(0xFFFBFAF7),
    text = Color(0xFF2B2A28),
    text2 = Color(0xFF6B675F)
)

internal val Ink = BaseTokens(
    background = Color(0xFF1D1E21),
    surface = Color(0xFF27282C),
    surfaceVariant = Color(0xFF34363B),
    inverseSurface = Color(0xFFF2F2EF),
    inverseOnSurface = Color(0xFF1D1E21),
    text = Color(0xFFF2F2EF),
    text2 = Color(0xFFA9ABB2)
)

private val DarkInk = Color(0xFF1B1B1F)

fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

fun contentColorFor(background: Color): Color {
    val l = background.luminance()
    val onWhite = 1.05f / (l + 0.05f)
    val onDark = (l + 0.05f) / (DarkInk.luminance() + 0.05f)
    return if (onWhite >= onDark) Color.White else DarkInk
}

internal fun mix(a: Color, b: Color, t: Float): Color = lerp(a, b, t)

@Immutable
data class SproutColors(val accent: Color, val focus: Color, val isDark: Boolean)

val LocalSproutColors = staticCompositionLocalOf {
    SproutColors(accent = Accent.TURQUOISE.color, focus = Accent.TURQUOISE.color, isDark = false)
}
