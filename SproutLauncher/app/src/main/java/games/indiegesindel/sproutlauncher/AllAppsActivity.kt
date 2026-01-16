package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

class AllAppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SproutLauncherTheme {
                val context = LocalContext.current
                val appManager = remember { AppManager(context) }
                var selectedTiles by remember { mutableStateOf(appManager.getAppTiles()) }
                
                val installedApps = remember {
                    val intent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    packageManager.queryIntentActivities(intent, 0)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(installedApps) { app ->
                            AppListItem(
                                app = app,
                                isSelected = selectedTiles.any { it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name },
                                onToggle = { isChecked ->
                                    if (isChecked) {
                                        val newTile = AppTile(
                                            packageName = app.activityInfo.packageName,
                                            activityName = app.activityInfo.name,
                                            label = app.loadLabel(packageManager).toString()
                                        )
                                        appManager.addAppTile(newTile)
                                    } else {
                                        val tileToRemove = selectedTiles.find { 
                                            it.packageName == app.activityInfo.packageName && it.activityName == app.activityInfo.name 
                                        }
                                        tileToRemove?.let { appManager.removeAppTile(it.id) }
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

@Composable
fun AppListItem(app: ResolveInfo, isSelected: Boolean, onToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember { app.loadLabel(pm).toString() }
    val icon = remember { app.loadIcon(pm).toBitmap().asImageBitmap() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggle
        )
    }
}
