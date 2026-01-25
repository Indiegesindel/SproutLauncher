package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.data.AppManager
import android.content.Intent
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppGridItem

@Composable
fun AllAppsTab(
    installedApps: List<ResolveInfo>,
    selectedTiles: List<AppTile>,
    shortcuts: List<AppTile> = emptyList(),
    isLoading: Boolean = false,
    isHomeScreen: Boolean = false,
    appManager: AppManager,
    onTilesChanged: (List<AppTile>) -> Unit,
    onRequestRemove: (AppTile) -> Unit = {},
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("allapps_loading")
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val showAppsHeader = isHomeScreen && installedApps.isNotEmpty() && (shortcuts.isNotEmpty() || isHomeScreen)
            
            if (showAppsHeader) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .focusable()
                    )
                }
            }

            items(installedApps, key = { app -> "${app.activityInfo.packageName}_${app.activityInfo.name}" }) { app ->
                val packageName = app.activityInfo.packageName
                val activityName = app.activityInfo.name
                val itemId = "${packageName}_${activityName}"
                val tile = remember(selectedTiles, packageName, activityName) {
                    fun findTile(tiles: List<AppTile>): AppTile? {
                        for (tile in tiles) {
                            if (tile.isGroup) {
                                findTile(tile.groupTiles)?.let { return it }
                            } else if (tile.packageName == packageName &&
                                tile.activityName == activityName &&
                                tile.shortcutId == null
                            ) {
                                return tile
                            }
                        }
                        return null
                    }
                    findTile(selectedTiles)
                }
                val isSelected = tile != null

                AppGridItem(
                    app = app,
                    isOnHomeScreen = isSelected,
                    tile = tile,
                    onToggleHomeScreen = {
                        if (!isSelected) {
                            val newTile = AppTile(
                                packageName = packageName,
                                activityName = activityName,
                                label = app.loadLabel(pm).toString()
                            )
                            appManager.addAppTile(newTile)
                            onTilesChanged(appManager.getAppTiles())
                        } else {
                            tile?.let { onRequestRemove(it) }
                        }
                    },
                    onEdit = {
                        tile?.let {
                            val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                                putExtra("TILE_ID", it.id)
                            }
                            context.startActivity(intent)
                        }
                    },
                    id = itemId,
                    isTargetFocused = focusedItemId == itemId,
                    onFocused = { onFocusItemIdChanged(it) },
                    isInMultiSelectMode = false,
                    isSelected = false
                )
            }

            if (isHomeScreen || shortcuts.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Shortcuts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .focusable()
                    )
                }

                if (isHomeScreen && shortcuts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "You can add shortcuts to the homescreen from supported apps by using their 'Add to home screen' feature.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .focusable()
                        )
                    }
                } else {
                    items(shortcuts, key = { tile -> "shortcut_${tile.id}" }) { tile ->
                        val itemId = "shortcut_${tile.id}"
                        AppGridItem(
                            app = null,
                            isOnHomeScreen = true,
                            tile = tile,
                            onToggleHomeScreen = {
                                onRequestRemove(tile)
                            },
                            onEdit = {
                                val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                                    putExtra("TILE_ID", tile.id)
                                }
                                context.startActivity(intent)
                            },
                            id = itemId,
                            isTargetFocused = focusedItemId == itemId,
                            onFocused = { onFocusItemIdChanged(it) },
                            isInMultiSelectMode = false,
                            isSelected = false
                        )
                    }
                }
            }
        }
    }
}
