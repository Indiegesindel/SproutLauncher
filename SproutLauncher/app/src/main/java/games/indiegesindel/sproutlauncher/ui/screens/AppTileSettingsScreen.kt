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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import games.indiegesindel.sproutlauncher.utils.IconUtils
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog

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
        RemoveTileConfirmationDialog(
            onConfirm = {
                viewModel.removeTile()
                onDone()
            },
            onDismiss = { viewModel.setShowDeleteConfirm(false) }
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
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
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val appIcon = remember(tile?.packageName) {
                    tile?.let { IconUtils.getFullSquareIcon(context, it.packageName, 512) }
                }

                AsyncImage(
                    model = remember(iconUri, appIcon) {
                        ImageRequest.Builder(context)
                            .data(iconUri ?: appIcon)
                            .size(coil.size.Size(512, 512))
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = "Tile Icon",
                    modifier = Modifier
                        .size(80.dp)
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
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { viewModel.onLabelChanged(it) },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "App Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ListItem(
                    headlineContent = { Text("Package") },
                    supportingContent = { Text(tile?.packageName ?: "") }
                )
                ListItem(
                    headlineContent = { Text("Activity") },
                    supportingContent = { Text(tile?.activityName ?: "") }
                )

                Spacer(modifier = Modifier.height(32.dp))

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
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
