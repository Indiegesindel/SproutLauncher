package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTileSettingsScreen(
    viewModel: AppTileSettingsViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val tile by viewModel.tile.collectAsState()
    val label by viewModel.label.collectAsState()
    val iconUri by viewModel.iconUri.collectAsState()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsState()

    if (tile == null) {
        onDone()
        return
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowDeleteConfirm(false) },
            title = { Text("Remove Tile") },
            text = { Text("Are you sure you want to remove this tile from your home screen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTile()
                        onDone()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDeleteConfirm(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.onIconUriChanged(it.toString())
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tile Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Discard")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveChanges()
                        onDone()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            val appIcon = remember {
                try {
                    context.packageManager.getApplicationIcon(tile!!.packageName)
                } catch (e: Exception) {
                    null
                }
            }

            AsyncImage(
                model = iconUri ?: appIcon,
                contentDescription = "Tile Icon",
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { photoPickerLauncher.launch(arrayOf("image/*")) }
                ) {
                    Text("Change Icon")
                }
                if (iconUri != null) {
                    OutlinedButton(
                        onClick = { viewModel.onIconUriChanged(null) }
                    ) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { viewModel.onLabelChanged(it) },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "App Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Package: ${tile?.packageName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Activity: ${tile?.activityName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.setShowDeleteConfirm(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove Tile")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
