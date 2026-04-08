package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PromptsFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun ButtonPrompt(
    button: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(MaterialTheme.colorScheme.inverseSurface, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = button,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
