package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme
import games.indiegesindel.sproutlauncher.utils.IconUtils

class AllAppsActivity : ComponentActivity() {
    enum class Tab { Home, All }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            SproutLauncherTheme {
                val context = LocalContext.current
                val appManager = remember { AppManager(context) }
                var selectedTiles by remember { mutableStateOf(appManager.getAppTiles()) }
                var currentTab by remember { mutableStateOf(Tab.Home) }

                val installedApps = remember {
                    val intent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    packageManager.queryIntentActivities(intent, 0)
                        .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
                }

                val displayedApps = remember(currentTab, selectedTiles, installedApps) {
                    when (currentTab) {
                        Tab.Home -> {
                            // Only apps that are in selectedTiles
                            installedApps.filter { app ->
                                selectedTiles.any { it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name }
                            }
                        }
                        Tab.All -> installedApps
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .displayCutoutPadding(),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            NavigationRailItem(
                                selected = currentTab == Tab.Home,
                                onClick = { currentTab = Tab.Home },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationRailItem(
                                selected = currentTab == Tab.All,
                                onClick = { currentTab = Tab.All },
                                icon = { Icon(Icons.Default.Apps, contentDescription = "All") },
                                label = { Text("All") }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            items(displayedApps, key = { "${it.activityInfo.packageName}_${it.activityInfo.name}" }) { app ->
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
                                                label = app.loadLabel(packageManager).toString()
                                            )
                                            appManager.addAppTile(newTile)
                                        } else {
                                            tile?.let { appManager.removeAppTile(it.id) }
                                        }
                                        selectedTiles = appManager.getAppTiles()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: ResolveInfo,
    isSelected: Boolean,
    tile: AppTile?,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(app) { app.loadLabel(pm).toString() }
    var isFocused by remember { mutableStateOf(false) }

    val iconPainter: Painter = if (tile?.iconUri != null) {
        rememberAsyncImagePainter(tile.iconUri)
    } else {
        val icon = remember(app) { IconUtils.getUnmaskedDrawable(app.loadIcon(pm)).toBitmap().asImageBitmap() }
        BitmapPainter(icon)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_X && 
                    event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    onToggle()
                    true
                } else {
                    false
                }
            }
            .focusable()
            .then(
                if (isFocused) Modifier.border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                else Modifier
            )
            .combinedClickable(
                onClick = {
                    val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                },
                onLongClick = onToggle
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isSelected,
            onCheckedChange = null // Read-only as per requirements, toggled via long press
        )
    }
}
