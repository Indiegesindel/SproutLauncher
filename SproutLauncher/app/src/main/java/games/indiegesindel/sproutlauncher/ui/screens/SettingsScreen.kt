package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
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

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Home Screen Section
            SettingsSectionHeader(title = "Home Screen")

            val currentRows by settingsManager.homeScreenRows.collectAsState()
            var showRowsDialog by remember { mutableStateOf(false) }

            ListItem(
                headlineContent = { Text("Number of Rows") },
                supportingContent = { Text(if (currentRows == 1) "1 row" else "$currentRows rows") },
                modifier = Modifier.clickable { showRowsDialog = true }
            )

            if (showRowsDialog) {
                RowsSelectionDialog(
                    currentRows = currentRows,
                    onRowsSelected = {
                        settingsManager.setHomeScreenRows(it)
                        showRowsDialog = false
                    },
                    onDismiss = { showRowsDialog = false }
                )
            }

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
fun RowsSelectionDialog(
    currentRows: Int,
    onRowsSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Number of Rows") },
        text = {
            Column {
                (1..5).forEach { rows ->
                    ThemeOption(
                        label = if (rows == 1) "1 Row" else "$rows Rows",
                        selected = currentRows == rows,
                        onClick = { onRowsSelected(rows) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
