package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppGrid
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

class MainActivity : ComponentActivity() {
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
                var appTiles by remember { mutableStateOf(emptyList<AppTile>()) }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        appTiles = appManager.getAppTiles()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
                    containerColor = Color.White
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AppGrid(
                                    appTiles = appTiles,
                                    onAppClick = { tile ->
                                        try {
                                            val intent = Intent().apply {
                                                setClassName(tile.packageName, tile.activityName)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not launch ${tile.label}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onRemove = { tile ->
                                        appManager.removeAppTile(tile.id)
                                        appTiles = appManager.getAppTiles()
                                    },
                                    onSettings = { tile ->
                                        val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                                            putExtra("TILE_ID", tile.id)
                                        }
                                        context.startActivity(intent)
                                    },
                                    onReorder = { from, to ->
                                        val newList = appTiles.toMutableList()
                                        if (from in newList.indices && to in newList.indices) {
                                            val tile = newList.removeAt(from)
                                            newList.add(to, tile)
                                            appTiles = newList
                                            appManager.saveAppTiles(newList)
                                        }
                                    }
                                )
                            }
                            
                            QuickActionsBar(
                                onAllAppsClick = {
                                    context.startActivity(Intent(context, AllAppsActivity::class.java))
                                },
                                onSettingsClick = {
                                    Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
                                }
                            )
                            
                            // Controller prompts placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 16.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Text(
                                    text = "TODO",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

