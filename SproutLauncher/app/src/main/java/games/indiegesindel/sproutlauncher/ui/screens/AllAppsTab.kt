package games.indiegesindel.sproutlauncher.ui.screens

import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppListItem

@Composable
fun AllAppsTab(
    installedApps: List<ResolveInfo>,
    selectedTiles: List<AppTile>,
    appManager: AppManager,
    onTilesChanged: (List<AppTile>) -> Unit,
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(installedApps, key = { app -> "${app.activityInfo.packageName}_${app.activityInfo.name}" }) { app ->
            val itemId = "${app.activityInfo.packageName}_${app.activityInfo.name}"
            val tile = selectedTiles.find {
                it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name
            }
            val isSelected = tile != null

            AppListItem(
                app = app,
                isSelected = isSelected,
                tile = tile,
                onToggle = {
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
