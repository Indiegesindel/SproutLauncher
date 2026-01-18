package games.indiegesindel.sproutlauncher.ui.screens

import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
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
            contentPadding = PaddingValues(16.dp)
        ) {
            items(installedApps, key = { app -> "${app.activityInfo.packageName}_${app.activityInfo.name}" }) { app ->
                val itemId = "${app.activityInfo.packageName}_${app.activityInfo.name}"
                val tile = selectedTiles.find {
                    it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name
                }
                val isSelected = tile != null

                AppGridItem(
                    app = app,
                    isOnHomeScreen = isSelected,
                    tile = tile,
                    onToggleHomeScreen = {
                        if (!isSelected) {
                            val newTile = AppTile(
                                packageName = app.activityInfo.packageName,
                                activityName = app.activityInfo.name,
                                label = app.loadLabel(pm).toString()
                            )
                            appManager.addAppTile(newTile)
                        } else {
                            tile?.let { appManager.removeAppTile(it.id) }
                        }
                        onTilesChanged(appManager.getAppTiles())
                    },
                    id = itemId,
                    isTargetFocused = focusedItemId == itemId,
                    onFocused = { onFocusItemIdChanged(it) }
                )
            }
        }
}
}
