package games.indiegesindel.sproutlauncher.ui.screens

import games.indiegesindel.sproutlauncher.ui.components.RefocusOnKeyInput
import android.view.InputDevice
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import games.indiegesindel.sproutlauncher.ui.components.requestFocusWithRetry
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.data.SteamGridDbException
import games.indiegesindel.sproutlauncher.model.SteamGridDbGame
import games.indiegesindel.sproutlauncher.model.SteamGridDbGrid
import games.indiegesindel.sproutlauncher.ui.components.focusRing
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDbUiState
import games.indiegesindel.sproutlauncher.ui.viewmodels.SteamGridDbViewModel
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val CardShape = RoundedCornerShape(16.dp)
private val ThumbShape = RoundedCornerShape(14.dp)
private val PREVIEW_PANEL_WIDTH = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteamGridDbScreen(viewModel: SteamGridDbViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val soundManager = LocalSoundManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val backFocus = remember { FocusRequester() }
    val firstResultFocus = remember { FocusRequester() }
    val useIconFocus = remember { FocusRequester() }
    var searchFocused by remember { mutableStateOf(false) }
    val goBack = {
        soundManager?.play(UiSound.BACK)
        if (state.selectedGame != null) viewModel.goBackToSearch() else onBack()
    }
    val leaveSearch: (fromController: Boolean) -> Unit = { fromController ->
        keyboard?.hide()
        if (fromController) view.requestFocusFromTouch()
        val hasResults = if (state.selectedGame != null) state.grids.isNotEmpty() else state.games.isNotEmpty()
        val moved = (hasResults && runCatching { firstResultFocus.requestFocus() }.getOrDefault(false)) ||
            runCatching { backFocus.requestFocus() }.getOrDefault(false)
        if (!moved) focusManager.clearFocus()
    }
    val firstResultKey: Any? = if (state.selectedGame != null) state.grids.firstOrNull()?.id else state.games.firstOrNull()?.id
    LaunchedEffect(firstResultKey) {
        if (firstResultKey != null && !searchFocused) firstResultFocus.requestFocusWithRetry()
    }

    var screenHasFocus by remember { mutableStateOf(false) }
    RefocusOnKeyInput(screenHasFocus) { if (firstResultKey != null) firstResultFocus else backFocus }

    BackHandler(enabled = state.selectedGame != null) { goBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { SearchBar(state, viewModel, onFocusChanged = { searchFocused = it }, onLeave = leaveSearch) },
                navigationIcon = {
                    val backInteraction = remember { MutableInteractionSource() }
                    val backFocused by backInteraction.collectIsFocusedAsState()
                    IconButton(
                        onClick = goBack,
                        interactionSource = backInteraction,
                        modifier = Modifier
                            .focusRequester(backFocus)
                            .focusRing(focused = backFocused, cornerRadius = null)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
        modifier = Modifier
            .fillMaxSize()
            .onFocusChanged { screenHasFocus = it.hasFocus }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (state.selectedGame != null) {
                GridsContent(
                    state,
                    firstResultFocus = firstResultFocus,
                    useIconFocus = useIconFocus,
                    onGridFocused = viewModel::selectGrid,
                    onGridClick = { grid ->
                        viewModel.selectGrid(grid)
                        view.requestFocusFromTouch()
                        runCatching { useIconFocus.requestFocus() }
                    },
                    onUseIcon = viewModel::downloadSelectedGrid
                )
            } else {
                SearchContent(state, firstResultFocus, onGameClick = viewModel::selectGame)
            }
        }
    }
}

@Composable
private fun SearchBar(
    state: SteamGridDbUiState,
    viewModel: SteamGridDbViewModel,
    onFocusChanged: (Boolean) -> Unit,
    onLeave: (fromController: Boolean) -> Unit
) {
    val count = when {
        state.selectedGame != null && !state.isLoadingGrids -> state.grids.size
        state.selectedGame == null && state.hasSearched && !state.isSearching -> state.games.size
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = state.query,
            onValueChange = viewModel::onQueryChanged,
            placeholder = { Text(stringResource(R.string.steamgriddb_search_hint)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
            leadingIcon = {
                IconButton(onClick = viewModel::search) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .onPreviewKeyEvent { event ->
                    val fromController = event.nativeKeyEvent.isFromSource(InputDevice.SOURCE_GAMEPAD) ||
                        event.nativeKeyEvent.isFromSource(InputDevice.SOURCE_DPAD)
                    when (event.key) {
                        Key.Back, Key.ButtonB, Key.Escape -> {
                            if (event.type == KeyEventType.KeyUp) onLeave(fromController)
                            true
                        }
                        Key.DirectionDown -> {
                            if (event.type == KeyEventType.KeyDown) onLeave(true)
                            true
                        }
                        else -> false
                    }
                }
        )
        if (count != null) {
            Text(
                text = pluralStringResource(R.plurals.results_count, count, count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchContent(state: SteamGridDbUiState, firstResultFocus: FocusRequester, onGameClick: (SteamGridDbGame) -> Unit) {
    when {
        state.isSearching -> CenteredProgress()
        state.error != null -> CenteredMessage(errorMessage(state.error))
        !state.hasSearched -> CenteredMessage(stringResource(R.string.steamgriddb_search_prompt))
        state.games.isEmpty() -> CenteredMessage(stringResource(R.string.steamgriddb_no_games))
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(state.games, key = { _, it -> it.id }) { index, game ->
                GameCard(
                    game,
                    modifier = if (index == 0) Modifier.focusRequester(firstResultFocus) else Modifier,
                    onClick = { onGameClick(game) }
                )
            }
        }
    }
}

@Composable
private fun GameCard(game: SteamGridDbGame, modifier: Modifier, onClick: () -> Unit) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) soundManager?.playMove()
            }
            .focusRing(focused = isFocused, cornerRadius = 16.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable {
                soundManager?.play(UiSound.CONFIRM)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(game.name.orEmpty(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            releaseDateText(game.releaseDate),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GridsContent(
    state: SteamGridDbUiState,
    firstResultFocus: FocusRequester,
    useIconFocus: FocusRequester,
    onGridFocused: (SteamGridDbGrid) -> Unit,
    onGridClick: (SteamGridDbGrid) -> Unit,
    onUseIcon: () -> Unit
) {
    when {
        state.isLoadingGrids -> CenteredProgress()
        state.error != null -> CenteredMessage(errorMessage(state.error))
        state.grids.isEmpty() -> CenteredMessage(stringResource(R.string.steamgriddb_no_icons))
        else -> Row(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = THUMB_SIZE),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(state.grids, key = { _, it -> it.id }) { index, grid ->
                    GridThumbnail(
                        grid = grid,
                        modifier = if (index == 0) Modifier.focusRequester(firstResultFocus) else Modifier,
                        selected = grid.id == state.selectedGrid?.id,
                        onFocused = { onGridFocused(grid) },
                        onClick = { onGridClick(grid) }
                    )
                }
            }
            VerticalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            state.selectedGrid?.let { grid ->
                PreviewPanel(
                    grid = grid,
                    isDownloading = state.isDownloading,
                    downloadFailed = state.downloadFailed,
                    onUseIcon = onUseIcon,
                    useIconFocus = useIconFocus,
                    width = PREVIEW_PANEL_WIDTH
                )
            }
        }
    }
}

private val THUMB_SIZE = 96.dp
private val USE_BUTTON_HEIGHT = 48.dp

@Composable
private fun GridThumbnail(
    grid: SteamGridDbGrid,
    modifier: Modifier,
    selected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    soundManager?.playMove()
                    onFocused()
                }
            }
            .focusRing(focused = isFocused, cornerRadius = 14.dp)
            .clip(ThumbShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (selected && !isFocused) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, ThumbShape) else Modifier)
            .clickable {
                soundManager?.play(UiSound.CONFIRM)
                onClick()
            }
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
private fun PreviewPanel(
    grid: SteamGridDbGrid,
    isDownloading: Boolean,
    downloadFailed: Boolean,
    onUseIcon: () -> Unit,
    useIconFocus: FocusRequester,
    width: Dp
) {
    val soundManager = LocalSoundManager.current
    val scroll = rememberScrollState()
    val useInteraction = remember { MutableInteractionSource() }
    val useFocused by useInteraction.collectIsFocusedAsState()
    LaunchedEffect(useFocused) {
        if (useFocused) scroll.animateScrollTo(scroll.maxValue)
    }
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .verticalScroll(scroll)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = grid.url,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.steamgriddb_dimensions, grid.width, grid.height),
            style = MaterialTheme.typography.titleMedium
        )
        grid.author?.name?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = stringResource(R.string.sgdb_by_author, it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!grid.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = grid.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                soundManager?.play(UiSound.CONFIRM)
                onUseIcon()
            },
            enabled = !isDownloading,
            interactionSource = useInteraction,
            modifier = Modifier
                .fillMaxWidth()
                .height(USE_BUTTON_HEIGHT)
                .focusRequester(useIconFocus)
                .focusRing(focused = useFocused, cornerRadius = null)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.sgdb_use_icon))
            }
        }
        if (downloadFailed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.steamgriddb_error_download),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.steamgriddb_attribution),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
