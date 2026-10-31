package games.indiegesindel.sproutlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.data.BaseTheme
import games.indiegesindel.sproutlauncher.data.GridSettings
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.Setting
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialogItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import games.indiegesindel.sproutlauncher.ui.components.SettingsSwitchItem
import games.indiegesindel.sproutlauncher.ui.components.SliderSetting
import games.indiegesindel.sproutlauncher.ui.theme.previewColor
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class ValueUnit { ROWS, DP }

private data class SliderSpec(
    @param:StringRes val itemLabel: Int,
    @param:StringRes val title: Int,
    @param:StringRes val label: Int,
    val setting: Setting<Int>,
    val range: IntRange,
    val unit: ValueUnit
)

private sealed interface SettingsDialogState {
    data object ApiKey : SettingsDialogState
    data object WallpaperDim : SettingsDialogState
    data class Slider(val spec: SliderSpec) : SettingsDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsRepository,
    imageStore: ImageStore,
    onBack: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    var dialog by remember { mutableStateOf<SettingsDialogState?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager?.play(UiSound.BACK)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AppInfoHeader()

            SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
            AppearanceSettings(settings, imageStore, onEditWallpaperDim = { dialog = SettingsDialogState.WallpaperDim })

            SettingsSectionHeader(stringResource(R.string.settings_section_steamgriddb))
            SettingsCard {
                val apiKey by settings.steamGridDbApiKey.flow.collectAsStateWithLifecycle()
                SettingsDialogItem(
                    label = stringResource(R.string.settings_api_key),
                    value = stringResource(if (apiKey.isNullOrBlank()) R.string.settings_api_key_not_set else R.string.settings_api_key_set),
                    onClick = { dialog = SettingsDialogState.ApiKey }
                )
            }

            SettingsSectionHeader(stringResource(R.string.settings_section_homescreen))
            GridSettingsCard(gridSliderSpecs(settings.homeGrid, isGroup = false)) { dialog = SettingsDialogState.Slider(it) }

            SettingsSectionHeader(stringResource(R.string.settings_section_groups))
            GridSettingsCard(gridSliderSpecs(settings.groupGrid, isGroup = true)) { dialog = SettingsDialogState.Slider(it) }

            SettingsSectionHeader(stringResource(R.string.settings_section_quick_actions))
            SettingsCard {
                SettingToggle(R.string.settings_show_youtube, settings.showYouTube)
                SettingToggle(R.string.settings_show_discord, settings.showDiscord)
                SettingToggle(R.string.settings_show_spotify, settings.showSpotify)
            }

            DemoFeatures()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    when (val current = dialog) {
        null -> Unit
        SettingsDialogState.ApiKey -> ApiKeyDialog(settings.steamGridDbApiKey) { dialog = null }
        SettingsDialogState.WallpaperDim -> {
            val dim by settings.wallpaperDim.flow.collectAsStateWithLifecycle()
            SettingsDialog(title = stringResource(R.string.settings_wallpaper_dim), onDismiss = { dialog = null }) {
                SliderSetting(
                    label = stringResource(R.string.settings_slider_amount),
                    value = (dim * 100f).roundToInt(),
                    range = 0..100,
                    onValueChange = { settings.wallpaperDim.set(it / 100f) },
                    valueDisplay = { stringResource(R.string.value_percent, it) }
                )
            }
        }
        is SettingsDialogState.Slider -> {
            val spec = current.spec
            val value by spec.setting.flow.collectAsStateWithLifecycle()
            SettingsDialog(title = stringResource(spec.title), onDismiss = { dialog = null }) {
                SliderSetting(
                    label = stringResource(spec.label),
                    value = value,
                    range = spec.range,
                    onValueChange = spec.setting::set,
                    valueDisplay = { formatValue(it, spec.unit) }
                )
            }
        }
    }
}

@Composable
private fun AppInfoHeader() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
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
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.settings_version, versionName ?: stringResource(R.string.settings_version_unknown)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppearanceSettings(settings: SettingsRepository, imageStore: ImageStore, onEditWallpaperDim: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentTheme by settings.baseTheme.flow.collectAsStateWithLifecycle()
    val wallpaperUri by settings.wallpaperUri.flow.collectAsStateWithLifecycle()
    val wallpaperDim by settings.wallpaperDim.flow.collectAsStateWithLifecycle()

    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val local = imageStore.importWallpaper(uri)
            if (local != null) {
                settings.wallpaperUri.set(local.toString())
            } else {
                persistReadPermission(context, uri)
                settings.wallpaperUri.set(uri.toString())
            }
        }
    }

    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaseTheme.entries.forEach { theme ->
                ThemePreviewButton(theme = theme, selected = currentTheme == theme) { settings.baseTheme.set(theme) }
            }
        }

        SettingToggle(R.string.settings_dark_mode, settings.isDarkMode)
        SettingToggle(R.string.settings_ui_sounds, settings.uiSoundsEnabled)

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
            supportingContent = {
                Text(stringResource(if (wallpaperUri != null) R.string.settings_wallpaper_custom else R.string.settings_wallpaper_default))
            },
            trailingContent = {
                if (wallpaperUri != null) {
                    TextButton(onClick = {
                        settings.wallpaperUri.set(null)
                        scope.launch { imageStore.clearWallpapers() }
                    }) {
                        Text(stringResource(R.string.action_reset))
                    }
                }
            },
            modifier = Modifier.clickable {
                wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        if (wallpaperUri != null) {
            SettingsDialogItem(
                label = stringResource(R.string.settings_wallpaper_dim),
                value = stringResource(R.string.value_percent, (wallpaperDim * 100f).roundToInt()),
                onClick = onEditWallpaperDim
            )
        }
    }
}

private fun persistReadPermission(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (e: SecurityException) {
        Log.w("SettingsScreen", "No persistable permission for $uri", e)
        Toast.makeText(context, R.string.error_wallpaper_not_persisted, Toast.LENGTH_LONG).show()
    }
}

private fun gridSliderSpecs(grid: GridSettings, isGroup: Boolean): List<SliderSpec> = listOf(
    SliderSpec(
        itemLabel = R.string.settings_rows,
        title = if (isGroup) R.string.settings_group_rows_title else R.string.settings_rows,
        label = R.string.settings_slider_rows,
        setting = grid.rows,
        range = 1..5,
        unit = ValueUnit.ROWS
    ),
    SliderSpec(
        itemLabel = R.string.settings_horizontal_spacing,
        title = if (isGroup) R.string.settings_group_horizontal_spacing_title else R.string.settings_horizontal_spacing,
        label = R.string.settings_slider_spacing,
        setting = grid.horizontalSpacing,
        range = 0..64,
        unit = ValueUnit.DP
    ),
    SliderSpec(
        itemLabel = R.string.settings_vertical_spacing,
        title = if (isGroup) R.string.settings_group_vertical_spacing_title else R.string.settings_vertical_spacing,
        label = R.string.settings_slider_spacing,
        setting = grid.verticalSpacing,
        range = 0..64,
        unit = ValueUnit.DP
    ),
    SliderSpec(
        itemLabel = R.string.settings_roundness,
        title = if (isGroup) R.string.settings_group_roundness_title else R.string.settings_roundness,
        label = R.string.settings_slider_roundness,
        setting = grid.roundness,
        range = 0..100,
        unit = ValueUnit.DP
    )
)

@Composable
private fun GridSettingsCard(specs: List<SliderSpec>, onEdit: (SliderSpec) -> Unit) {
    SettingsCard {
        specs.forEach { spec ->
            val value by spec.setting.flow.collectAsStateWithLifecycle()
            SettingsDialogItem(
                label = stringResource(spec.itemLabel),
                value = formatValue(value, spec.unit),
                onClick = { onEdit(spec) }
            )
        }
    }
}

@Composable
private fun SettingToggle(@StringRes label: Int, setting: Setting<Boolean>) {
    val checked by setting.flow.collectAsStateWithLifecycle()
    SettingsSwitchItem(label = stringResource(label), checked = checked, onCheckedChange = setting::set)
}

@Composable
private fun formatValue(value: Int, unit: ValueUnit): String = when (unit) {
    ValueUnit.ROWS -> pluralStringResource(R.plurals.rows_count, value, value)
    ValueUnit.DP -> stringResource(R.string.value_dp, value)
}

@Composable
private fun ApiKeyDialog(setting: Setting<String?>, onDismiss: () -> Unit) {
    val apiKey by setting.flow.collectAsStateWithLifecycle()
    SettingsDialog(title = stringResource(R.string.settings_api_key_dialog_title), onDismiss = onDismiss) {
        OutlinedTextField(
            value = apiKey.orEmpty(),
            onValueChange = { setting.set(it.ifEmpty { null }) },
            label = { Text(stringResource(R.string.settings_api_key_field)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun ThemePreviewButton(theme: BaseTheme, selected: Boolean, onClick: () -> Unit) {
    val color = theme.previewColor ?: MaterialTheme.colorScheme.outline
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color)
                .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(theme.label),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@get:StringRes
private val BaseTheme.label: Int
    get() = when (this) {
        BaseTheme.SYSTEM -> R.string.theme_system
        BaseTheme.PURPLE -> R.string.theme_purple
        BaseTheme.MINT_GREEN -> R.string.theme_mint
        BaseTheme.DEEP_BLUE -> R.string.theme_blue
        BaseTheme.FIRE_RED -> R.string.theme_red
        BaseTheme.REFRESHING_ORANGE -> R.string.theme_orange
        BaseTheme.MONOCHROME -> R.string.theme_gray
        BaseTheme.ROSE_PINK -> R.string.theme_pink
        BaseTheme.TEAL -> R.string.theme_teal
    }
