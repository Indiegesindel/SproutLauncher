package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import games.indiegesindel.sproutlauncher.R

private const val ICON_MARKER = "\u0000"
private const val ICON_ID = "all_apps_icon"

@Composable
fun HomeDisclaimer(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val message = stringResource(R.string.home_empty_hint, ICON_MARKER)
    val inlineContent = mapOf(
        ICON_ID to InlineTextContent(
            Placeholder(width = 22.sp, height = 22.sp, placeholderVerticalAlign = PlaceholderVerticalAlign.Center)
        ) {
            Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = color)
        }
    )

    Box(modifier = modifier.padding(48.dp), contentAlignment = Alignment.Center) {
        Text(
            text = buildAnnotatedString {
                append(message.substringBefore(ICON_MARKER))
                appendInlineContent(ICON_ID, "[menu]")
                append(message.substringAfter(ICON_MARKER, ""))
            },
            inlineContent = inlineContent,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
