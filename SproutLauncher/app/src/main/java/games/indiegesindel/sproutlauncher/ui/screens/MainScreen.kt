package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import games.indiegesindel.sproutlauncher.AppTileSettingsActivity
import games.indiegesindel.sproutlauncher.ExtendedActivity
import games.indiegesindel.sproutlauncher.FocusedElement
import games.indiegesindel.sproutlauncher.ui.components.AppGrid
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.ui.components.QuickActionsBar
import games.indiegesindel.sproutlauncher.ui.viewmodels.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val appTiles by viewModel.appTiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusedItemId by viewModel.focusedItemId.collectAsState()
    val focusedElement by viewModel.focusedElement.collectAsState()
    val homeScreenRows by viewModel.homeScreenRows.collectAsState()

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
                        rows = homeScreenRows,
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
                            viewModel.removeAppTile(tile.id)
                        },
                        onSettings = { tile ->
                            val intent = Intent(context, AppTileSettingsActivity::class.java).apply {
                                putExtra("TILE_ID", tile.id)
                            }
                            context.startActivity(intent)
                        },
                        onReorder = { from, to ->
                            viewModel.reorderAppTiles(from, to)
                        },
                        onDragEnd = {
                            viewModel.saveAppTiles()
                        },
                        onFocusChanged = { focused ->
                            viewModel.onFocusChanged(focused)
                        },
                        focusedItemId = focusedItemId,
                        onFocusItemIdChanged = { viewModel.onFocusedItemIdChanged(it) },
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
                                    // Handle QuickAction focus in ViewModel if needed
                                }
                            },
                            focusedItemId = focusedItemId,
                            onFocusItemIdChanged = { viewModel.onFocusedItemIdChanged(it) }
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
                            else -> {
                                // Show nothing or default
                            }
                        }
                    }
                }
            }
        }
    }
}

