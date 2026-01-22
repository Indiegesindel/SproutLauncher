package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.painterResource
import games.indiegesindel.sproutlauncher.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import games.indiegesindel.sproutlauncher.data.BaseTheme
import games.indiegesindel.sproutlauncher.data.SettingsManager
import games.indiegesindel.sproutlauncher.ExtendedActivity
import games.indiegesindel.sproutlauncher.ui.theme.*
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialogItem
import games.indiegesindel.sproutlauncher.ui.components.SproutAlertDialog
import games.indiegesindel.sproutlauncher.utils.FileUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentBaseTheme by settingsManager.baseTheme.collectAsState()
    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val showYouTube by settingsManager.showYouTube.collectAsState()
    val showDiscord by settingsManager.showDiscord.collectAsState()
    val showSpotify by settingsManager.showSpotify.collectAsState()

    val currentRows by settingsManager.homeScreenRows.collectAsState()
    val horizontalSpacing by settingsManager.horizontalSpacing.collectAsState()
    val verticalSpacing by settingsManager.verticalSpacing.collectAsState()
    val wallpaperDim by settingsManager.wallpaperDim.collectAsState()

    var showWallpaperDimDialog by remember { mutableStateOf(false) }
    var showRowsDialog by remember { mutableStateOf(false) }
    var showHorizontalSpacingDialog by remember { mutableStateOf(false) }
    var showVerticalSpacingDialog by remember { mutableStateOf(false) }
    
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "Unknown"
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // App Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = R.mipmap.ic_launcher,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance Section
            SettingsSectionHeader(title = "Appearance")

            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaseTheme.entries.forEach { theme ->
                        ThemePreviewButton(
                            theme = theme,
                            selected = currentBaseTheme == theme,
                            onClick = { settingsManager.setBaseTheme(theme) }
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text("Dark Mode") },
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { settingsManager.setIsDarkMode(it) }
                        )
                    },
                    modifier = Modifier.clickable { settingsManager.setIsDarkMode(!isDarkMode) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                val wallpaperUri by settingsManager.wallpaperUri.collectAsState()
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            // Cleanup old wallpaper files and copy new one with unique name
                            // to ensure StateFlow/Coil see it as a change and ensure persistence
                            FileUtils.cleanupWallpaperFiles(context)
                            val fileName = "wallpaper_${System.currentTimeMillis()}"
                            val localUri = FileUtils.saveUriToInternalStorage(context, uri, fileName)
                            if (localUri != null) {
                                settingsManager.setWallpaperUri(localUri.toString())
                            } else {
                                // Fallback to original URI if copy fails
                                try {
                                    context.contentResolver.takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                } catch (e: SecurityException) {
                                    Log.e("SettingsScreen", "Failed to take persistable URI permission", e)
                                    Toast.makeText(
                                        context,
                                        "Could not persist wallpaper. It might reset after a reboot.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                settingsManager.setWallpaperUri(uri.toString())
                            }
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Wallpaper") },
                    supportingContent = {
                        Text(if (wallpaperUri != null) "Custom Image" else "Default color background")
                    },
                    trailingContent = {
                        if (wallpaperUri != null) {
                            TextButton(onClick = {
                                settingsManager.setWallpaperUri(null)
                                FileUtils.cleanupWallpaperFiles(context)
                            }) {
                                Text("Reset")
                            }
                        }
                    },
                    modifier = Modifier.clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                if (wallpaperUri != null) {
                    SettingsDialogItem(
                        label = "Wallpaper Dimming",
                        value = "${(wallpaperDim * 100f).roundToInt()}%",
                        onClick = { showWallpaperDimDialog = true }
                    )
                }
            }

            SettingsSectionHeader(title = "Grid Spacing")

            SettingsCard {
                SettingsDialogItem(
                    label = "Number of Rows",
                    value = if (currentRows == 1) "1 Row" else "$currentRows Rows",
                    onClick = { showRowsDialog = true }
                )

                SettingsDialogItem(
                    label = "Horizontal Spacing",
                    value = "${horizontalSpacing}dp",
                    onClick = { showHorizontalSpacingDialog = true }
                )

                SettingsDialogItem(
                    label = "Vertical Spacing",
                    value = "${verticalSpacing}dp",
                    onClick = { showVerticalSpacingDialog = true }
                )
            }

            SettingsSectionHeader(title = "Quick Actions")

            SettingsCard {
                ListItem(
                    headlineContent = { Text("Show YouTube") },
                    trailingContent = {
                        Switch(
                            checked = showYouTube,
                            onCheckedChange = { settingsManager.setShowYouTube(it) }
                        )
                    },
                    modifier = Modifier.clickable { settingsManager.setShowYouTube(!showYouTube) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    headlineContent = { Text("Show Discord") },
                    trailingContent = {
                        Switch(
                            checked = showDiscord,
                            onCheckedChange = { settingsManager.setShowDiscord(it) }
                        )
                    },
                    modifier = Modifier.clickable { settingsManager.setShowDiscord(!showDiscord) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    headlineContent = { Text("Show Spotify") },
                    trailingContent = {
                        Switch(
                            checked = showSpotify,
                            onCheckedChange = { settingsManager.setShowSpotify(it) }
                        )
                    },
                    modifier = Modifier.clickable { settingsManager.setShowSpotify(!showSpotify) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showWallpaperDimDialog) {
        SproutAlertDialog(
            onDismissRequest = { showWallpaperDimDialog = false },
            confirmButton = {
                TextButton(onClick = { showWallpaperDimDialog = false }) {
                    Text("Done")
                }
            },
            title = { Text("Wallpaper Dimming") },
            text = {
                SliderSetting(
                    label = "Amount",
                    value = (wallpaperDim * 100f).roundToInt(),
                    valueRange = 0f..100f,
                    steps = 99,
                    onValueChange = { settingsManager.setWallpaperDim(it / 100f) },
                    valueDisplay = { "$it%" }
                )
            }
        )
    }

    if (showRowsDialog) {
        SproutAlertDialog(
            onDismissRequest = { showRowsDialog = false },
            confirmButton = {
                TextButton(onClick = { showRowsDialog = false }) {
                    Text("Done")
                }
            },
            title = { Text("Number of Rows") },
            text = {
                SliderSetting(
                    label = "Rows",
                    value = currentRows,
                    valueRange = 1f..5f,
                    steps = 3,
                    onValueChange = { settingsManager.setHomeScreenRows(it) },
                    valueDisplay = { if (it == 1) "1 Row" else "$it Rows" }
                )
            }
        )
    }

    if (showHorizontalSpacingDialog) {
        SproutAlertDialog(
            onDismissRequest = { showHorizontalSpacingDialog = false },
            confirmButton = {
                TextButton(onClick = { showHorizontalSpacingDialog = false }) {
                    Text("Done")
                }
            },
            title = { Text("Horizontal Spacing") },
            text = {
                SliderSetting(
                    label = "Spacing",
                    value = horizontalSpacing,
                    valueRange = 0f..64f,
                    steps = 63,
                    onValueChange = { settingsManager.setHorizontalSpacing(it) },
                    valueDisplay = { "${it}dp" }
                )
            }
        )
    }

    if (showVerticalSpacingDialog) {
        SproutAlertDialog(
            onDismissRequest = { showVerticalSpacingDialog = false },
            confirmButton = {
                TextButton(onClick = { showVerticalSpacingDialog = false }) {
                    Text("Done")
                }
            },
            title = { Text("Vertical Spacing") },
            text = {
                SliderSetting(
                    label = "Spacing",
                    value = verticalSpacing,
                    valueRange = 0f..64f,
                    steps = 63,
                    onValueChange = { settingsManager.setVerticalSpacing(it) },
                    valueDisplay = { "${it}dp" }
                )
            }
        )
    }
}

@Composable
fun ThemePreviewButton(
    theme: BaseTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = when (theme) {
        BaseTheme.SYSTEM -> MaterialTheme.colorScheme.outline
        BaseTheme.PURPLE -> primaryLight
        BaseTheme.MINT_GREEN -> mintGreenPrimaryLight
        BaseTheme.DEEP_BLUE -> deepBluePrimaryLight
        BaseTheme.FIRE_RED -> fireRedPrimaryLight
        BaseTheme.REFRESHING_ORANGE -> orangePrimaryLight
    }

    val label = when (theme) {
        BaseTheme.SYSTEM -> "System"
        BaseTheme.PURPLE -> "Purple"
        BaseTheme.MINT_GREEN -> "Mint"
        BaseTheme.DEEP_BLUE -> "Blue"
        BaseTheme.FIRE_RED -> "Red"
        BaseTheme.REFRESHING_ORANGE -> "Orange"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Int) -> Unit,
    valueDisplay: (Int) -> String = { it.toString() }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valueDisplay(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
