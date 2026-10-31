package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import games.indiegesindel.sproutlauncher.R

@Composable
fun RemoveTileConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    SproutAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.remove_tile_title)) },
        text = { Text(stringResource(R.string.remove_tile_message)) },
        isSmall = true,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.action_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
