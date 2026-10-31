package games.indiegesindel.sproutlauncher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.data.SteamGridDbException
import games.indiegesindel.sproutlauncher.model.SteamGridDbGame
import games.indiegesindel.sproutlauncher.model.SteamGridDbGrid
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsInfoItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import games.indiegesindel.sproutlauncher.ui.components.SproutAlertDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDbUiState
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDbViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteamGridDbScreen(viewModel: SteamGridDbViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.selectedGame != null, onBack = viewModel::goBackToSearch)

    state.selectedGrid?.let { grid ->
        GridDetailDialog(
            grid = grid,
            isDownloading = state.isDownloading,
            downloadFailed = state.downloadFailed,
            onDismiss = viewModel::dismissGridDetail,
            onSelect = viewModel::downloadSelectedGrid
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder = { Text(stringResource(R.string.steamgriddb_search_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                        leadingIcon = {
                            IconButton(onClick = viewModel::search) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (state.selectedGame != null) viewModel.goBackToSearch() else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (state.selectedGame != null) {
                GridsContent(state, onGridClick = viewModel::selectGrid)
            } else {
                SearchContent(state, onGameClick = viewModel::selectGame)
            }
        }
    }
}

@Composable
private fun SearchContent(state: SteamGridDbUiState, onGameClick: (SteamGridDbGame) -> Unit) {
    when {
        state.isSearching -> CenteredProgress()
        state.error != null -> CenteredMessage(errorMessage(state.error))
        !state.hasSearched -> CenteredMessage(stringResource(R.string.steamgriddb_search_prompt))
        state.games.isEmpty() -> CenteredMessage(stringResource(R.string.steamgriddb_no_games))
        else -> Column {
            SettingsSectionHeader(pluralStringResource(R.plurals.results_count, state.games.size, state.games.size))
            SettingsCard {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.games, key = { it.id }) { game ->
                        ListItem(
                            headlineContent = { Text(game.name.orEmpty()) },
                            supportingContent = { Text(releaseDateText(game.releaseDate)) },
                            modifier = Modifier.clickable { onGameClick(game) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridsContent(state: SteamGridDbUiState, onGridClick: (SteamGridDbGrid) -> Unit) {
    when {
        state.isLoadingGrids -> CenteredProgress()
        state.error != null -> CenteredMessage(errorMessage(state.error))
        state.grids.isEmpty() -> CenteredMessage(stringResource(R.string.steamgriddb_no_icons))
        else -> Column {
            SettingsSectionHeader(pluralStringResource(R.plurals.results_count, state.grids.size, state.grids.size))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.grids, key = { it.id }) { grid ->
                    GridThumbnail(grid, onClick = { onGridClick(grid) })
                }
            }
        }
    }
}

@Composable
private fun GridThumbnail(grid: SteamGridDbGrid, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else Modifier
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        AsyncImage(
            model = grid.thumb ?: grid.url,
            contentDescription = stringResource(R.string.steamgriddb_icon_by, grid.author?.name.orEmpty()),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun GridDetailDialog(
    grid: SteamGridDbGrid,
    isDownloading: Boolean,
    downloadFailed: Boolean,
    onDismiss: () -> Unit,
    onSelect: () -> Unit
) {
    SproutAlertDialog(
        onDismissRequest = onDismiss,
        isSmall = true,
        confirmButton = {
            Button(onClick = onSelect, enabled = !isDownloading) {
                if (isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.steamgriddb_select_icon))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        title = { Text(stringResource(R.string.steamgriddb_icon_details)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = grid.url,
                    contentDescription = null,
                    modifier = Modifier
                        .size(256.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )
                Column(modifier = Modifier.weight(1f)) {
                    SettingsSectionHeader(stringResource(R.string.steamgriddb_metadata))
                    SettingsCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        if (!grid.notes.isNullOrBlank()) {
                            SettingsInfoItem(stringResource(R.string.steamgriddb_notes), grid.notes)
                        }
                        SettingsInfoItem(
                            stringResource(R.string.steamgriddb_size),
                            stringResource(R.string.steamgriddb_dimensions, grid.width, grid.height)
                        )
                        SettingsInfoItem(stringResource(R.string.steamgriddb_author), grid.author?.name.orEmpty())
                    }
                    if (downloadFailed) {
                        Text(
                            text = stringResource(R.string.steamgriddb_error_download),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.steamgriddb_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun CenteredProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun errorMessage(reason: SteamGridDbException.Reason): String = stringResource(
    when (reason) {
        SteamGridDbException.Reason.MISSING_API_KEY -> R.string.steamgriddb_error_missing_key
        SteamGridDbException.Reason.UNAUTHORIZED -> R.string.steamgriddb_error_unauthorized
        SteamGridDbException.Reason.NETWORK -> R.string.steamgriddb_error_network
        SteamGridDbException.Reason.SERVER -> R.string.steamgriddb_error_server
    }
)

@Composable
private fun releaseDateText(epochSeconds: Long?): String {
    if (epochSeconds == null || epochSeconds <= 0) return stringResource(R.string.steamgriddb_release_unknown)
    val date = Instant.ofEpochSecond(epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    return stringResource(R.string.steamgriddb_released_on, date)
}
