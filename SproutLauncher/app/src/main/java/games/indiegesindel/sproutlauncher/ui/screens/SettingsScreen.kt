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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.data.AppTheme
import games.indiegesindel.sproutlauncher.data.SettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme by settingsManager.theme.collectAsState()
    
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Appearance Section
            SettingsSectionHeader(title = "Appearance")
            
            var expanded by remember { mutableStateOf(false) }
            
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { 
                    Text(
                        when (currentTheme) {
                            AppTheme.SYSTEM -> "System default"
                            AppTheme.LIGHT -> "Light"
                            AppTheme.DARK -> "Dark"
                            AppTheme.PURPLE_LIGHT -> "Purple Light"
                            AppTheme.PURPLE_DARK -> "Purple Dark"
                        }
                    )
                },
                modifier = Modifier.clickable { expanded = true }
            )

            if (expanded) {
                ThemeSelectionDialog(
                    currentTheme = currentTheme,
                    onThemeSelected = {
                        settingsManager.setTheme(it)
                        expanded = false
                    },
                    onDismiss = { expanded = false }
                )
            }

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
                }
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

            val currentRows by settingsManager.homeScreenRows.collectAsState()
            val horizontalSpacing by settingsManager.horizontalSpacing.collectAsState()
            val verticalSpacing by settingsManager.verticalSpacing.collectAsState()

            SliderSetting(
                label = "Number of Rows",
                value = currentRows,
                valueRange = 1f..5f,
                steps = 3,
                onValueChange = { settingsManager.setHomeScreenRows(it) },
                valueDisplay = { if (it == 1) "1 Row" else "$it Rows" }
            )

            SettingsSectionHeader(title = "Grid Spacing")

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

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Developer Section
            SettingsSectionHeader(title = "About")
            
            ListItem(
                headlineContent = { Text("Developer") },
                supportingContent = { Text("Indiegesindel") }
            )
            
            ListItem(
                headlineContent = { Text("Made with passion") },
                supportingContent = { Text("Handcrafted for you") }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                ThemeOption(
                    label = "System default",
                    selected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onThemeSelected(AppTheme.SYSTEM) }
                )
                ThemeOption(
                    label = "Light",
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { onThemeSelected(AppTheme.LIGHT) }
                )
                ThemeOption(
                    label = "Dark",
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { onThemeSelected(AppTheme.DARK) }
                )
                ThemeOption(
                    label = "Purple Light",
                    selected = currentTheme == AppTheme.PURPLE_LIGHT,
                    onClick = { onThemeSelected(AppTheme.PURPLE_LIGHT) }
                )
                ThemeOption(
                    label = "Purple Dark",
                    selected = currentTheme == AppTheme.PURPLE_DARK,
                    onClick = { onThemeSelected(AppTheme.PURPLE_DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueDisplay(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange,
            steps = steps
        )
    }
}
