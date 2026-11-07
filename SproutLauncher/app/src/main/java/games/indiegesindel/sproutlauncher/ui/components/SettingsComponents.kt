package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import games.indiegesindel.sproutlauncher.R
import kotlin.math.roundToInt
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@Composable
fun SproutAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    isSmall: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier
            .fillMaxWidth(if (isSmall) 0.6f else 0.92f)
            .padding(vertical = 24.dp),
        dismissButton = dismissButton,
        title = title,
        text = text,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
fun SettingsDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    SproutAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) } },
        title = { Text(title) },
        text = content
    )
}

@Composable
fun SettingsDialogItem(label: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsSwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val soundManager = LocalSoundManager.current
    val toggle: (Boolean) -> Unit = { on ->
        onCheckedChange(on)
        soundManager?.play(if (on) UiSound.TOGGLE_ON else UiSound.TOGGLE_OFF)
    }
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = toggle) },
        modifier = Modifier.clickable { toggle(!checked) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsInfoItem(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(value) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        content = content
    )
}

@Composable
fun SliderSetting(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    valueDisplay: @Composable (Int) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = valueDisplay(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
