package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.AppGrid
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

enum class FocusedElement {
    NONE, APP_TILE, QUICK_ACTION
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val appTheme by settingsManager.theme.collectAsState()

            SproutLauncherTheme(appTheme = appTheme) {
                val appManager = remember { AppManager(context) }
                var appTiles by remember { mutableStateOf(emptyList<AppTile>()) }
                var isLoading by remember { mutableStateOf(true) }
                var focusedElement by remember { mutableStateOf(FocusedElement.NONE) }
                var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    appTiles = appManager.getAppTiles()
                    isLoading = false
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        appTiles = appManager.getAppTiles()
                        isLoading = false
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AppGrid(
                                    appTiles = appTiles,
                                    isLoading = isLoading,
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
                                        }
                                    },
                                    onDragEnd = {
                                        appManager.saveAppTiles(appTiles)
                                    },
                                    onFocusChanged = { focused ->
                                        if (focused) {
                                            focusedElement = FocusedElement.APP_TILE
                                        } else if (focusedElement == FocusedElement.APP_TILE) {
                                            focusedElement = FocusedElement.NONE
                                        }
                                    },
                                    focusedItemId = focusedItemId,
                                    onFocusItemIdChanged = { focusedItemId = it },
                                    modifier = Modifier.padding(bottom = 0.dp)
                                )

                                Box(modifier = Modifier.zIndex(1f)) {
                                    QuickActionsBar(
                                        onAllAppsClick = {
                                            context.startActivity(Intent(context, ExtendedActivity::class.java))
                                        },
                                        onBrowserClick = {
                                            try {
                                                val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onSettingsClick = {
                                            try {
                                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open settings", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onFocusChanged = { focused ->
                                            if (focused) {
                                                focusedElement = FocusedElement.QUICK_ACTION
                                            } else if (focusedElement == FocusedElement.QUICK_ACTION) {
                                                focusedElement = FocusedElement.NONE
                                            }
                                        },
                                        focusedItemId = focusedItemId,
                                        onFocusItemIdChanged = { focusedItemId = it }
                                    )
                                }
                            }
                            
                            // Controller prompts
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, end = 32.dp, bottom = 12.dp, top = 0.dp)
                                    .height(32.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when (focusedElement) {
                                        FocusedElement.APP_TILE -> {
                                            ButtonPrompt(button = "+", label = "Settings")
                                            ButtonPrompt(button = "X", label = "Move (Hold)")
                                            ButtonPrompt(button = "A", label = "Launch")
                                        }
                                        FocusedElement.QUICK_ACTION -> {
                                            ButtonPrompt(button = "A", label = "Launch")
                                        }
                                        FocusedElement.NONE -> {
                                            // Show nothing or default
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonPrompt(
    button: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.inverseSurface, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = button,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

