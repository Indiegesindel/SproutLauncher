package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import games.indiegesindel.sproutlauncher.model.AppTile
import android.view.KeyEvent

@Composable
fun AppGrid(
    appTiles: List<AppTile>,
    onAppClick: (AppTile) -> Unit,
    onRemove: (AppTile) -> Unit,
    onSettings: (AppTile) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val rows = if (isPhone) 1 else 2

    LazyHorizontalGrid(
        rows = GridCells.Fixed(rows),
        modifier = modifier
            .height(if (isPhone) 240.dp else 440.dp)
            .wrapContentWidth(),
        contentPadding = PaddingValues(start = 32.dp, top = 48.dp, end = 32.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(appTiles, key = { it.id }) { tile ->
            AppTileItem(
                tile = tile,
                onClick = { onAppClick(tile) },
                onRemove = { onRemove(tile) },
                onSettings = { onSettings(tile) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTileItem(
    tile: AppTile,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BUTTON_START,
                        KeyEvent.KEYCODE_MENU -> {
                            showMenu = true
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tooltip
        if (isFocused) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-45).dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tile.label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp, 7.dp)
                            .background(Color.Black, shape = TriangleShape)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .then(
                    if (isFocused) Modifier.border(2.dp, Color.Black, RoundedCornerShape(22.dp))
                    else Modifier
                )
                .padding(if (isFocused) 6.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            val appIcon = remember(tile.packageName) {
                try {
                    context.packageManager.getApplicationIcon(tile.packageName)
                } catch (e: Exception) {
                    null
                }
            }
            
            AsyncImage(
                model = tile.iconUri ?: appIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    showMenu = false
                    onSettings()
                }
            )
            DropdownMenuItem(
                text = { Text("Remove") },
                onClick = {
                    showMenu = false
                    onRemove()
                }
            )
        }
    }
}
