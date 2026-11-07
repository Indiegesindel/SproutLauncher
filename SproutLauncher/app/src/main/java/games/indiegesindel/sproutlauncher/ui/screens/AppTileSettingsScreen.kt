package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.ui.components.snapScrollToEdges
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.SteamGridDBActivity
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.RemoveTileConfirmationDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialogItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsInfoItem
import games.indiegesindel.sproutlauncher.ui.components.focusRing
import games.indiegesindel.sproutlauncher.ui.icons.iconModel
import games.indiegesindel.sproutlauncher.ui.viewmodels.AppTileSettingsViewModel
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

private val IconShape = RoundedCornerShape(18.dp)
private val CardShape = RoundedCornerShape(16.dp)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tile_settings_title)) },
                navigationIcon = {
                    val backInteraction = remember { MutableInteractionSource() }
                    val backFocused by backInteraction.collectIsFocusedAsState()
                    IconButton(
                        interactionSource = backInteraction,
                        modifier = Modifier.focusRing(focused = backFocused, cornerRadius = null),
                        onClick = {
                            soundManager?.play(UiSound.BACK)
                            onClose()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_discard))
                    }
                },
                actions = {
                    val saveInteraction = remember { MutableInteractionSource() }
                    val saveFocused by saveInteraction.collectIsFocusedAsState()
                    TextButton(
                        interactionSource = saveInteraction,
                        modifier = Modifier.focusRing(focused = saveFocused, cornerRadius = 50.dp),
                        onClick = {
                            soundManager?.play(UiSound.CONFIRM)
                            viewModel.saveChanges()
                            onClose()
                        }
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            PromptsFooter {
                ButtonPrompt(button = "A", label = stringResource(R.string.prompt_select))
                ButtonPrompt(button = "B", label = stringResource(R.string.prompt_back))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .snapScrollToEdges(scroll)
                .verticalScroll(scroll)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AsyncImage(
                    model = iconUri ?: tile.copy(iconUri = null).iconModel,
                    contentDescription = stringResource(R.string.tile_settings_icon),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(IconShape),
                    contentScale = ContentScale.Fit
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconOptionCard(
                            icon = Icons.Filled.Image,
                            title = stringResource(R.string.tile_settings_icon_local),
                            hint = stringResource(R.string.tile_settings_option_local_hint),
                            enabled = true,
                            onClick = { imagePicker.launch(arrayOf("image/*")) }
                        )
                        IconOptionCard(
                            icon = Icons.Filled.ImageSearch,
                            title = stringResource(R.string.tile_settings_icon_steamgriddb),
                            hint = stringResource(
                                if (isSteamGridDbAvailable) R.string.tile_settings_option_sgdb_hint else R.string.tile_settings_option_sgdb_no_key
                            ),
                            enabled = isSteamGridDbAvailable,
                            onClick = { steamGridDbPicker.launch(label) }
                        )
                    }
                    if (iconUri != null) {
                        TextButton(onClick = {
                            soundManager?.play(UiSound.BACK)
                            viewModel.onIconUriChanged(null)
                        }) {
                            Text(stringResource(R.string.action_reset))
                        }
                    }
                }
            }

            SettingsCard {
                SettingsDialogItem(
                    label = stringResource(R.string.tile_settings_label),
                    value = label,
                    onClick = { showLabelDialog = true }
                )
            }

            if (!tile.isGroup) AppDetailsCard(tile)

            SettingsCard {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.tile_settings_remove), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { viewModel.setShowDeleteConfirm(true) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
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
private fun RowScope.IconOptionCard(icon: ImageVector, title: String, hint: String, enabled: Boolean, onClick: () -> Unit) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .weight(1f)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) soundManager?.playMove()
            }
            .focusRing(focused = isFocused, cornerRadius = 16.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = enabled) {
                soundManager?.play(UiSound.CONFIRM)
                onClick()
            }
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private const val DISABLED_ALPHA = 0.5f

@Composable
private fun AppDetailsCard(tile: AppTile) {
    SettingsCard {
        SettingsInfoItem(stringResource(R.string.tile_settings_package), tile.packageName)
        SettingsInfoItem(stringResource(R.string.tile_settings_activity), tile.activityName)
    }
}
