package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import games.indiegesindel.sproutlauncher.model.SteamGridDBGrid
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsCardHigher
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import games.indiegesindel.sproutlauncher.ui.components.SproutAlertDialog
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDBViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteamGridDBScreen(
    viewModel: SteamGridDBViewModel,
    onBack: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val games by viewModel.games.collectAsState()
    val grids by viewModel.grids.collectAsState()
    val selectedGame by viewModel.selectedGame.collectAsState()
    val selectedGrid by viewModel.selectedGrid.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoadingGrids by viewModel.isLoadingGrids.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()

    selectedGrid?.let { grid ->
        GridDetailDialog(
            grid = grid,
            isDownloading = isDownloading,
            onDismiss = { viewModel.dismissGridDetail() },
            onSelect = {
                viewModel.downloadIcon(grid) { uri ->
                    if (uri != null) {
                        onIconSelected(uri)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                title = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search SteamGridDB...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                            leadingIcon = {
                                IconButton(onClick = { viewModel.search() }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedIndicatorColor = Transparent,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth(fraction = 0.5f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedGame != null) {
                            viewModel.goBackToSearch()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        if (selectedGame != null) {
            GridsContent(
                grids = grids,
                isLoading = isLoadingGrids,
                onGridClick = { viewModel.selectGrid(it) },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            SearchContent(
                games = games,
                isSearching = isSearching,
                hasSearched = hasSearched,
                onGameClick = { viewModel.selectGame(it) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun SearchContent(
    games: List<games.indiegesindel.sproutlauncher.model.SteamGridDBGame>,
    isSearching: Boolean,
    hasSearched: Boolean,
    onGameClick: (games.indiegesindel.sproutlauncher.model.SteamGridDBGame) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isSearching) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (hasSearched && games.isEmpty()) {
            Text(
                text = "No games found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (!hasSearched) {
            Text(
                text = "Search for a game to find icons",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column {
                SettingsSectionHeader(title = "${games.size} Result${if (games.size > 1) "s" else ""}")

                SettingsCard {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(games) { game ->
                            ListItem(
                                headlineContent = { Text(game.name) },
                                supportingContent = { Text(game.formattedReleaseDate()) },
                                modifier = Modifier.clickable { onGameClick(game) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridsContent(
    grids: List<SteamGridDBGrid>,
    isLoading: Boolean,
    onGridClick: (SteamGridDBGrid) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (grids.isEmpty()) {
            Text(
                text = "No icons found for this game",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column {
                SettingsSectionHeader(title = "${grids.size} Result${if (grids.size > 1) "s" else ""}")

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(grids) { grid ->
                        var isItemFocused by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .onFocusChanged { isItemFocused = it.isFocused }
                                .then(
                                    if (isItemFocused) Modifier.border(
                                        width = 4.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    ) else Modifier
                                )
                                .clickable { onGridClick(grid) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            AsyncImage(
                                model = grid.thumb,
                                contentDescription = "Grid icon by ${grid.author.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridDetailDialog(
    grid: SteamGridDBGrid,
    isDownloading: Boolean,
    onDismiss: () -> Unit,
    onSelect: () -> Unit
) {
    SproutAlertDialog(
        onDismissRequest = onDismiss,
        isSmall = true,
        confirmButton = {
            Button(
                onClick = onSelect,
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Select this icon")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Icon Details") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = grid.url,
                    contentDescription = "Full icon",
                    modifier = Modifier
                        .size(256.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    SettingsSectionHeader(title = "Metadata")

                    SettingsCardHigher {
                        if (!grid.notes.isNullOrBlank()) {
                            ListItem(
                                headlineContent = { Text("Notes") },
                                supportingContent = { Text(grid.notes) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

                        ListItem(
                            headlineContent = { Text("Size") },
                            supportingContent = { Text("${grid.width} × ${grid.height}") },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { Text("Author") },
                            supportingContent = { Text(grid.author.name) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    Text("Provided by SteamGridDB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    )
}
