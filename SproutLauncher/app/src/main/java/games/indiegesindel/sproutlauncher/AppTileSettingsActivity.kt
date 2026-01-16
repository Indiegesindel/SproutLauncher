package games.indiegesindel.sproutlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import games.indiegesindel.sproutlauncher.data.AppManager
import games.indiegesindel.sproutlauncher.ui.theme.SproutLauncherTheme

class AppTileSettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tileId = intent.getStringExtra("TILE_ID") ?: finish().run { return }
        
        enableEdgeToEdge()
        setContent {
            SproutLauncherTheme {
                val context = LocalContext.current
                val appManager = remember { AppManager(context) }
                val initialTile = remember { appManager.getAppTiles().find { it.id == tileId } } ?: run {
                    finish()
                    return@SproutLauncherTheme
                }

                var label by remember { mutableStateOf(initialTile.label) }
                var iconUri by remember { mutableStateOf(initialTile.iconUri) }

                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri: Uri? ->
                        uri?.let {
                            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            iconUri = it.toString()
                        }
                    }
                )

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Tile Settings") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Discard")
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    val updatedTile = initialTile.copy(label = label, iconUri = iconUri)
                                    appManager.updateAppTile(updatedTile)
                                    finish()
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Left side: Icon Area (1fr)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val appIcon = remember {
                                    try {
                                        context.packageManager.getApplicationIcon(initialTile.packageName)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                AsyncImage(
                                    model = iconUri ?: appIcon,
                                    contentDescription = "Tile Icon",
                                    modifier = Modifier.size(128.dp),
                                    contentScale = ContentScale.Fit
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Change", maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { iconUri = null },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reset", maxLines = 1)
                                    }
                                }
                            }

                            // Right side: Label input (2fr)
                            Column(
                                modifier = Modifier.weight(2f)
                            ) {
                                OutlinedTextField(
                                    value = label,
                                    onValueChange = { label = it },
                                    label = { Text("Label") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Package: ${initialTile.packageName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "Activity: ${initialTile.activityName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Bottom buttons for explicit Save/Discard as requested
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { finish() }) {
                                Text("Discard")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                val updatedTile = initialTile.copy(label = label, iconUri = iconUri)
                                appManager.updateAppTile(updatedTile)
                                finish()
                            }) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
