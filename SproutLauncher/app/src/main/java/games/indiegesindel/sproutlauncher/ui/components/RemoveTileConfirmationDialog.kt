package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import games.indiegesindel.sproutlauncher.ui.components.SproutAlertDialog

@Composable
fun RemoveTileConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    SproutAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Tile") },
        text = { Text("Are you sure you want to remove this tile from your home screen?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
