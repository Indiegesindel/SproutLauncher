package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppGridItem

@Composable
fun AllAppsTab(
    installedApps: List<ResolveInfo>,
    selectedTiles: List<AppTile>,
    isLoading: Boolean = false,
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(installedApps, key = { app -> "${app.activityInfo.packageName}_${app.activityInfo.name}" }) { app ->
                val packageName = app.activityInfo.packageName
                val activityName = app.activityInfo.name
                val itemId = "${packageName}_${activityName}"
                val tile = remember(selectedTiles, packageName, activityName) {
                    selectedTiles.find {
                        it.packageName == packageName && it.activityName == activityName
                    }
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
                            tile.let { onRequestRemove(it) }
                        }
                    },
                    id = itemId,
                    isTargetFocused = focusedItemId == itemId,
                    onFocused = { onFocusItemIdChanged(it) }
                )
            }
        }
}
}
