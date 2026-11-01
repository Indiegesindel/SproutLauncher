package games.indiegesindel.sproutlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Default = Typography()

private fun TextStyle.role(size: Int, line: Int, weight: FontWeight) =
    copy(fontSize = size.sp, lineHeight = line.sp, fontWeight = weight, letterSpacing = 0.sp)

private val Title = Default.titleLarge.role(22, 28, FontWeight.Bold)
private val Heading = Default.headlineSmall.role(18, 24, FontWeight.Bold)
private val Item = Default.bodyLarge.role(16, 22, FontWeight.Normal)
private val Support = Default.bodyMedium.role(14, 20, FontWeight.Normal)
private val Label = Default.labelLarge.role(14, 20, FontWeight.Bold)
private val Caption = Default.labelSmall.role(12, 16, FontWeight.Medium)

val Typography = Typography(
    displayLarge = Title, displayMedium = Title, displaySmall = Title,
    headlineLarge = Heading, headlineMedium = Heading, headlineSmall = Heading,
    titleLarge = Title,
    titleMedium = Item.copy(fontWeight = FontWeight.Bold),
    titleSmall = Support.copy(fontWeight = FontWeight.Bold),
    bodyLarge = Item,
    bodyMedium = Support,
    bodySmall = Caption.copy(fontWeight = FontWeight.Normal),
    labelLarge = Label,
    labelMedium = Caption.copy(fontWeight = FontWeight.Bold),
    labelSmall = Caption
)
