package games.indiegesindel.sproutlauncher.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.SteamGridDBActivity
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialogItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsInfoItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import games.indiegesindel.sproutlauncher.ui.icons.iconModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTileSettingsScreen(
    viewModel: AppTileSettingsViewModel,
    onClose: () -> Unit
) {
    val tile = viewModel.tile
    if (tile == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val label by viewModel.label.collectAsStateWithLifecycle()
    val iconUri by viewModel.iconUri.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()
    val isSteamGridDbAvailable by viewModel.isSteamGridDbAvailable.collectAsStateWithLifecycle()

    var showLabelDialog by remember { mutableStateOf(false) }
    var showIconMenu by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.iconImportFailed.collect {
            Toast.makeText(context, R.string.error_icon_import, Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importIcon)
    }
    val steamGridDbPicker = rememberLauncherForActivityResult(SteamGridDBActivity.PickIcon()) { uri ->
        uri?.let(viewModel::onIconUriChanged)
    }

    if (showDeleteConfirm) {
        RemoveTileConfirmationDialog(
            onConfirm = {
                viewModel.removeTile()
                onClose()
            },
            onDismiss = { viewModel.setShowDeleteConfirm(false) }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.tile_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager?.play(UiSound.BACK)
                        onClose()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_discard))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        soundManager?.play(UiSound.CONFIRM)
                        viewModel.saveChanges()
                        onClose()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
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
                AsyncImage(
                    model = iconUri ?: tile.copy(iconUri = null).iconModel,
                    contentDescription = stringResource(R.string.tile_settings_icon),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        Button(onClick = { showIconMenu = true }) {
                            Text(stringResource(R.string.tile_settings_change_icon))
                        }
                        DropdownMenu(expanded = showIconMenu, onDismissRequest = { showIconMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.tile_settings_icon_local)) },
                                onClick = {
                                    showIconMenu = false
                                    imagePicker.launch(arrayOf("image/*"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.tile_settings_icon_steamgriddb)) },
                                enabled = isSteamGridDbAvailable,
                                onClick = {
                                    showIconMenu = false
                                    steamGridDbPicker.launch(label)
                                }
                            )
                        }
                    }
                    if (iconUri != null) {
                        OutlinedButton(onClick = { viewModel.onIconUriChanged(null) }) {
                            Text(stringResource(R.string.action_reset))
                        }
                    }
                }
            }

            SettingsSectionHeader(stringResource(R.string.tile_settings_section_general))
            SettingsCard {
                SettingsDialogItem(
                    label = stringResource(R.string.tile_settings_label),
                    value = label,
                    onClick = { showLabelDialog = true }
                )
            }

            if (!tile.isGroup) AppDetailsSection(tile)

            SettingsSectionHeader(stringResource(R.string.tile_settings_section_actions))
            SettingsCard {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.tile_settings_remove), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { viewModel.setShowDeleteConfirm(true) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLabelDialog) {
        SettingsDialog(title = stringResource(R.string.tile_settings_edit_label), onDismiss = { showLabelDialog = false }) {
            OutlinedTextField(
                value = label,
                onValueChange = viewModel::onLabelChanged,
                label = { Text(stringResource(R.string.tile_settings_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun AppDetailsSection(tile: AppTile) {
    SettingsSectionHeader(stringResource(R.string.tile_settings_section_details))
    SettingsCard {
        SettingsInfoItem(stringResource(R.string.tile_settings_package), tile.packageName)
        SettingsInfoItem(stringResource(R.string.tile_settings_activity), tile.activityName)
    }
}
