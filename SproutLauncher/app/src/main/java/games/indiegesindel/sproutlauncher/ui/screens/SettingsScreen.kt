package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Intent
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
import games.indiegesindel.sproutlauncher.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentBaseTheme by settingsManager.baseTheme.collectAsState()
    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val showYouTube by settingsManager.showYouTube.collectAsState()
    val showDiscord by settingsManager.showDiscord.collectAsState()
    val showSpotify by settingsManager.showSpotify.collectAsState()
    
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "Unknown"
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
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
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            settingsManager.setWallpaperUri(uri.toString())
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
                            TextButton(onClick = { settingsManager.setWallpaperUri(null) }) {
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
                    val wallpaperDim by settingsManager.wallpaperDim.collectAsState()
                    SliderSetting(
                        label = "Wallpaper Dimming",
                        value = (wallpaperDim * 100f).roundToInt(),
                        valueRange = 0f..100f,
                        steps = 99,
                        onValueChange = { settingsManager.setWallpaperDim(it / 100f) },
                        valueDisplay = { "$it%" }
                    )
                }
            }

            val currentRows by settingsManager.homeScreenRows.collectAsState()
            val horizontalSpacing by settingsManager.horizontalSpacing.collectAsState()
            val verticalSpacing by settingsManager.verticalSpacing.collectAsState()

            SettingsSectionHeader(title = "Grid Spacing")

            SettingsCard {
                SliderSetting(
                    label = "Number of Rows",
                    value = currentRows,
                    valueRange = 1f..5f,
                    steps = 3,
                    onValueChange = { settingsManager.setHomeScreenRows(it) },
                    valueDisplay = { if (it == 1) "1 Row" else "$it Rows" }
                )

                SliderSetting(
                    label = "Horizontal Spacing",
                    value = horizontalSpacing,
                    valueRange = 0f..64f,
                    steps = 63,
                    onValueChange = { settingsManager.setHorizontalSpacing(it) },
                    valueDisplay = { "${it}dp" }
                )

                SliderSetting(
                    label = "Vertical Spacing",
                    value = verticalSpacing,
                    valueRange = 0f..64f,
                    steps = 63,
                    onValueChange = { settingsManager.setVerticalSpacing(it) },
                    valueDisplay = { "${it}dp" }
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
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        content = content
    )
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
