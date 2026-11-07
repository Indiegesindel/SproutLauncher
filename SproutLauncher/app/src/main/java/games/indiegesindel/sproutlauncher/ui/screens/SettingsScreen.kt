package games.indiegesindel.sproutlauncher.ui.screens

import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.text.style.TextAlign
import games.indiegesindel.sproutlauncher.ui.components.RefocusOnKeyInput
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import games.indiegesindel.sproutlauncher.data.PlainBase
import games.indiegesindel.sproutlauncher.ui.theme.LocalSproutColors
import games.indiegesindel.sproutlauncher.ui.theme.plainBaseColor
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import games.indiegesindel.sproutlauncher.ui.components.SproutAlertDialog
import games.indiegesindel.sproutlauncher.ui.theme.accentColor
import games.indiegesindel.sproutlauncher.ui.theme.contentColorFor
import games.indiegesindel.sproutlauncher.ui.theme.contrastRatio
import games.indiegesindel.sproutlauncher.utils.AppLauncher
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import games.indiegesindel.sproutlauncher.data.GridAlignment
import games.indiegesindel.sproutlauncher.ui.components.focusRing
import games.indiegesindel.sproutlauncher.ui.components.requestFocusWithRetry
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.ui.components.snapScrollToEdges
import games.indiegesindel.sproutlauncher.ui.components.PromptsFooter
import games.indiegesindel.sproutlauncher.ui.components.ButtonPrompt
import games.indiegesindel.sproutlauncher.data.Accent
import games.indiegesindel.sproutlauncher.data.AmbientSource
import games.indiegesindel.sproutlauncher.data.BackgroundMode
import games.indiegesindel.sproutlauncher.data.GridSettings
import games.indiegesindel.sproutlauncher.data.ImageStore
import games.indiegesindel.sproutlauncher.data.Setting
import games.indiegesindel.sproutlauncher.data.SettingsRepository
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialog
import games.indiegesindel.sproutlauncher.ui.components.SettingsDialogItem
import games.indiegesindel.sproutlauncher.ui.components.SettingsSwitchItem
import games.indiegesindel.sproutlauncher.ui.components.SliderSetting
import games.indiegesindel.sproutlauncher.ui.theme.color
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
    data class CustomColor(
        @param:StringRes val title: Int,
        val color: Setting<Int>,
        val warnLowContrast: Boolean,
        val onPicked: () -> Unit
    ) : SettingsDialogState
    data class Slider(val spec: SliderSpec) : SettingsDialogState
}

private enum class SettingsSection(@param:StringRes val title: Int, @param:StringRes val summary: Int, val icon: ImageVector) {
    APPEARANCE(R.string.settings_section_appearance, R.string.settings_summary_appearance, Icons.Filled.Palette),
    SOUND(R.string.settings_section_sound, R.string.settings_summary_sound, Icons.Filled.MusicNote),
    HOMESCREEN(R.string.settings_section_homescreen, R.string.settings_summary_homescreen, Icons.Filled.GridView),
    GROUPS(R.string.settings_section_groups, R.string.settings_summary_groups, Icons.Filled.Folder),
    QUICK_ACTIONS(R.string.settings_section_quick_actions, R.string.settings_summary_quick_actions, Icons.Filled.Bolt),
    STEAMGRIDDB(R.string.settings_section_steamgriddb, R.string.settings_summary_steamgriddb, Icons.Filled.Image),
    DEMO(R.string.settings_section_demo, R.string.settings_summary_demo, Icons.Filled.BugReport)
}

private val sections = SettingsSection.entries.filter { it != SettingsSection.DEMO || HAS_DEMO_FEATURES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsRepository,
    imageStore: ImageStore,
    onBack: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val twoPane = screenWidth >= TWO_PANE_MIN_WIDTH_DP
    val listPaneWidth = (screenWidth * LIST_PANE_FRACTION).dp.coerceIn(LIST_PANE_MIN_WIDTH, LIST_PANE_MAX_WIDTH)
    var dialog by remember { mutableStateOf<SettingsDialogState?>(null) }
    var openSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    var selected by rememberSaveable { mutableStateOf(sections.first()) }
    var lastOpened by remember { mutableStateOf<SettingsSection?>(null) }
    var paneHasFocus by remember { mutableStateOf(false) }
    val rowFocus = remember { SettingsSection.entries.associateWith { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    val closeSection = {
        soundManager?.play(UiSound.BACK)
        lastOpened = openSection
        openSection = null
    }
    BackHandler(enabled = !twoPane && openSection != null, onBack = closeSection)
    BackHandler(enabled = twoPane && paneHasFocus) {
        soundManager?.play(UiSound.BACK)
        rowFocus.getValue(selected).requestFocus()
    }
    LaunchedEffect(openSection) {
        if (openSection == null) lastOpened?.let { rowFocus.getValue(it).requestFocusWithRetry() }
    }

    var screenHasFocus by remember { mutableStateOf(false) }
    RefocusOnKeyInput(screenHasFocus) {
        if (twoPane) rowFocus.getValue(selected) else if (openSection == null) rowFocus.getValue(lastOpened ?: sections.first()) else null
    }

    val page: @Composable (SettingsSection) -> Unit = { section ->
        SectionPage(section, settings, imageStore, onDialog = { dialog = it })
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .onFocusChanged { screenHasFocus = it.hasFocus },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(openSection?.title?.takeIf { !twoPane } ?: R.string.settings_title)) },
                navigationIcon = {
                    val backInteraction = remember { MutableInteractionSource() }
                    val backFocused by backInteraction.collectIsFocusedAsState()
                    IconButton(
                        interactionSource = backInteraction,
                        modifier = Modifier.focusRing(focused = backFocused, cornerRadius = null),
                        onClick = {
                            if (!twoPane && openSection != null) closeSection() else {
                                soundManager?.play(UiSound.BACK)
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            PromptsFooter {
                ButtonPrompt(button = "A", label = stringResource(R.string.prompt_select))
                ButtonPrompt(button = "B", label = stringResource(R.string.prompt_back))
            }
        }
    ) { innerPadding ->
        if (twoPane) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Row(modifier = Modifier.weight(1f)) {
                    val listScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .width(listPaneWidth)
                            .fillMaxHeight()
                            .snapScrollToEdges(listScroll)
                            .verticalScroll(listScroll)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        AppInfoHeader(compact = true)
                        sections.forEach { section ->
                            SectionRow(
                                section = section,
                                selected = section == selected,
                                showChevron = false,
                                focusRequester = rowFocus.getValue(section),
                                onFocused = { selected = section },
                                onClick = {
                                    selected = section
                                    focusManager.moveFocus(FocusDirection.Right)
                                }
                            )
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AnimatedContent(
                        targetState = selected,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onFocusChanged { paneHasFocus = it.hasFocus },
                        transitionSpec = {
                            (fadeIn(tween(PANE_FADE_MS)) + slideInHorizontally(tween(PANE_SLIDE_MS)) { it / 40 }) togetherWith
                                fadeOut(tween(PANE_FADE_MS / 2))
                        },
                        label = "settingsPane"
                    ) { section ->
                        val paneScroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .snapScrollToEdges(paneScroll)
                                .verticalScroll(paneScroll)
                                .padding(top = 8.dp, bottom = 32.dp)
                        ) {
                            page(section)
                        }
                    }
                }
            }
        } else {
            AnimatedContent(
                targetState = openSection,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                transitionSpec = {
                    val forward = targetState != null
                    val dir = if (forward) 1 else -1
                    (fadeIn(tween(PAGE_MS, easing = Motion.Emphasized)) +
                        slideInHorizontally(tween(PAGE_MS, easing = Motion.Emphasized)) { dir * it / 10 }) togetherWith
                        (fadeOut(tween(PAGE_MS / 2)) + slideOutHorizontally(tween(PAGE_MS)) { -dir * it / 20 })
                },
                label = "settingsPage"
            ) { section ->
                val pageScroll = rememberScrollState()
                Column(modifier = Modifier.fillMaxSize().snapScrollToEdges(pageScroll).verticalScroll(pageScroll)) {
                    if (section == null) {
                        AppInfoHeader(compact = false)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(28.dp))
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            sections.forEach { s ->
                                SectionRow(
                                    section = s,
                                    selected = false,
                                    showChevron = true,
                                    focusRequester = rowFocus.getValue(s),
                                    onFocused = {},
                                    onClick = { openSection = s }
                                )
                            }
                        }
                    } else {
                        page(section)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    when (val current = dialog) {
        null -> Unit
        SettingsDialogState.ApiKey -> ApiKeyDialog(settings.steamGridDbApiKey) { dialog = null }
        is SettingsDialogState.CustomColor -> ColorPickerDialog(
            title = stringResource(current.title),
            initial = current.color.value,
            warnLowContrast = current.warnLowContrast,
            onSave = {
                current.color.set(it)
                current.onPicked()
            },
            onDismiss = { dialog = null }
        )
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

private const val TWO_PANE_MIN_WIDTH_DP = 600
private const val LIST_PANE_FRACTION = 0.4f
private val LIST_PANE_MIN_WIDTH = 300.dp
private val LIST_PANE_MAX_WIDTH = 400.dp
private const val PAGE_MS = 220
private const val PANE_FADE_MS = 120
private const val PANE_SLIDE_MS = 160

@Composable
private fun SectionPage(
    section: SettingsSection,
    settings: SettingsRepository,
    imageStore: ImageStore,
    onDialog: (SettingsDialogState) -> Unit
) {
    when (section) {
        SettingsSection.APPEARANCE -> AppearanceSettings(
            settings,
            imageStore,
            onEditWallpaperDim = { onDialog(SettingsDialogState.WallpaperDim) },
            onEditCustomAccent = {
                onDialog(SettingsDialogState.CustomColor(R.string.custom_accent_title, settings.customAccent, warnLowContrast = true) {
                    settings.accent.set(Accent.CUSTOM)
                })
            },
            onEditCustomPlain = {
                onDialog(SettingsDialogState.CustomColor(R.string.custom_plain_title, settings.customPlain, warnLowContrast = false) {
                    settings.plainBase.set(PlainBase.CUSTOM)
                })
            },
            onEditCustomAmbient = {
                onDialog(SettingsDialogState.CustomColor(R.string.custom_ambient_title, settings.customAmbient, warnLowContrast = false) {
                    settings.ambientColor.set(Accent.CUSTOM)
                })
            }
        )
        SettingsSection.SOUND -> SettingsCard {
            SettingToggle(R.string.settings_ui_sounds, settings.uiSoundsEnabled)
            SettingToggle(R.string.settings_menu_music, settings.menuMusicEnabled)
            SettingToggle(R.string.settings_controller_rumble, settings.controllerRumbleEnabled)
        }
        SettingsSection.HOMESCREEN -> GridSettingsCard(settings.homeGrid, isGroup = false) { onDialog(SettingsDialogState.Slider(it)) }
        SettingsSection.GROUPS -> GridSettingsCard(settings.groupGrid, isGroup = true) { onDialog(SettingsDialogState.Slider(it)) }
        SettingsSection.QUICK_ACTIONS -> SettingsCard {
            SettingToggle(R.string.settings_show_youtube, settings.showYouTube)
            SettingToggle(R.string.settings_show_discord, settings.showDiscord)
            SettingToggle(R.string.settings_show_spotify, settings.showSpotify)
        }
        SettingsSection.STEAMGRIDDB -> SteamGridDbSettings(settings, onEditKey = { onDialog(SettingsDialogState.ApiKey) })
        SettingsSection.DEMO -> DemoFeatures()
    }
}

@Composable
private fun SteamGridDbSettings(settings: SettingsRepository, onEditKey: () -> Unit) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val apiKey by settings.steamGridDbApiKey.flow.collectAsStateWithLifecycle()
    SettingsCard(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f).compositeOver(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ArtworkCollage()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.sgdb_explainer_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.sgdb_explainer_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    SettingsCard {
        ListItem(
            headlineContent = { Text(stringResource(R.string.sgdb_get_key)) },
            supportingContent = { Text(stringResource(R.string.sgdb_get_key_hint)) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
            modifier = Modifier.clickable {
                soundManager?.play(UiSound.CONFIRM)
                AppLauncher.openUrl(context, SGDB_KEY_URL)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
        SettingsDialogItem(
                label = stringResource(R.string.settings_api_key),
                value = stringResource(if (apiKey.isNullOrBlank()) R.string.settings_api_key_not_set else R.string.settings_api_key_set),
                onClick = onEditKey
            )
    }
}

private const val SGDB_KEY_URL = "https://www.steamgriddb.com/profile/preferences"

@Composable
private fun ArtworkCollage() {
    val cards = listOf(
        listOf(Color(0xFFFF9A3D), Color(0xFFD63C6B)),
        listOf(Color(0xFF3DD6FF), Color(0xFF3C62D6)),
        listOf(Color(0xFF8EE06B), Color(0xFF2B9C63)),
        listOf(Color(0xFFC08CFF), Color(0xFF7A3CD6))
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CollageCard(cards[0], width = 34.dp, height = 46.dp)
            CollageCard(cards[1], width = 34.dp, height = 46.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CollageCard(cards[2], width = 34.dp, height = 22.dp)
            CollageCard(cards[3], width = 34.dp, height = 22.dp)
        }
    }
}

@Composable
private fun CollageCard(colors: List<Color>, width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(colors))
    )
}

@Composable
private fun SectionRow(
    section: SettingsSection,
    selected: Boolean,
    showChevron: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(ROW_RADIUS)
    val bringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val ringRoom = with(LocalDensity.current) { (Motion.RingGap + Motion.RingWidth + 8.dp).toPx() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .onSizeChanged { size = it }
            .bringIntoViewRequester(bringIntoView)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    soundManager?.playMove()
                    onFocused()
                    scope.launch {
                        bringIntoView.bringIntoView(Rect(0f, -ringRoom, size.width.toFloat(), size.height + ringRoom))
                    }
                }
            }
            .focusRing(focused = isFocused, cornerRadius = ROW_RADIUS)
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.18f) else Color.Transparent)
            .clickable {
                soundManager?.play(UiSound.CONFIRM)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(SECTION_ICON_SIZE)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(section.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(section.title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(section.summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showChevron) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val ROW_RADIUS = 20.dp
private val SECTION_ICON_SIZE = 44.dp

@Composable
private fun AppInfoHeader(compact: Boolean) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
    val name: @Composable () -> Unit = {
        Text(
            text = stringResource(R.string.app_name),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
            fontWeight = if (compact) null else FontWeight.Bold
        )
    }
    val version: @Composable () -> Unit = {
        Text(
            text = stringResource(R.string.settings_version, versionName ?: stringResource(R.string.settings_version_unknown)),
            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (compact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = R.mipmap.ic_launcher,
                contentDescription = null,
                modifier = Modifier
                    .size(SECTION_ICON_SIZE)
                    .clip(CircleShape)
            )
            Column {
                name()
                version()
            }
        }
        return
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
        name()
        version()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceSettings(
    settings: SettingsRepository,
    imageStore: ImageStore,
    onEditWallpaperDim: () -> Unit,
    onEditCustomAccent: () -> Unit,
    onEditCustomPlain: () -> Unit,
    onEditCustomAmbient: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val scope = rememberCoroutineScope()
    val currentAccent by settings.accent.flow.collectAsStateWithLifecycle()
    val customAccent by settings.customAccent.flow.collectAsStateWithLifecycle()
    val plainBase by settings.plainBase.flow.collectAsStateWithLifecycle()
    val customPlain by settings.customPlain.flow.collectAsStateWithLifecycle()
    val isDark = LocalSproutColors.current.isDark
    val themedBackground = MaterialTheme.colorScheme.background
    val backgroundMode by settings.backgroundMode.flow.collectAsStateWithLifecycle()
    val ambientSource by settings.ambientSource.flow.collectAsStateWithLifecycle()
    val ambientColor by settings.ambientColor.flow.collectAsStateWithLifecycle()
    val customAmbient by settings.customAmbient.flow.collectAsStateWithLifecycle()
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
            settings.backgroundMode.set(BackgroundMode.WALLPAPER)
        }
    }

    SettingsCard {
        AccentSwatches(
            selected = currentAccent,
            custom = customAccent,
            onSelect = settings.accent::set,
            onEditCustom = onEditCustomAccent
        )

        SettingToggle(R.string.settings_dark_mode, settings.isDarkMode)

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_background)) },
            supportingContent = { Text(stringResource(backgroundMode.description)) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            BackgroundMode.entries.forEachIndexed { i, mode ->
                SegmentedButton(
                    selected = backgroundMode == mode,
                    onClick = {
                        soundManager?.play(UiSound.CONFIRM)
                        settings.backgroundMode.set(mode)
                        if (mode == BackgroundMode.WALLPAPER && wallpaperUri == null) {
                            wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(i, BackgroundMode.entries.size)
                ) {
                    Text(stringResource(mode.label))
                }
            }
        }

        if (backgroundMode == BackgroundMode.PLAIN) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_plain_base)) },
                supportingContent = { Text(stringResource(plainBase.label)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlainBase.entries.forEach { base ->
                    ColorSwatch(
                        color = plainBaseColor(base, customPlain, isDark, themedBackground),
                        selected = plainBase == base,
                        isCustom = base == PlainBase.CUSTOM,
                        size = PLAIN_SWATCH_SIZE,
                        contentDescription = stringResource(base.label)
                    ) {
                        if (base == PlainBase.CUSTOM) onEditCustomPlain() else settings.plainBase.set(base)
                    }
                }
            }
        }

        if (backgroundMode == BackgroundMode.AMBIENT) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_ambient_source)) },
                supportingContent = {
                    Text(stringResource(ambientSource.description))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                AmbientSource.entries.forEachIndexed { i, source ->
                    SegmentedButton(
                        selected = ambientSource == source,
                        onClick = {
                            soundManager?.play(UiSound.CONFIRM)
                            settings.ambientSource.set(source)
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, AmbientSource.entries.size)
                    ) {
                        Text(stringResource(source.label))
                    }
                }
            }
            if (ambientSource == AmbientSource.CUSTOM) {
                AccentSwatches(
                    selected = ambientColor,
                    custom = customAmbient,
                    onSelect = settings.ambientColor::set,
                    onEditCustom = onEditCustomAmbient
                )
            }
        }

        if (backgroundMode == BackgroundMode.WALLPAPER) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
                supportingContent = {
                    Text(stringResource(if (wallpaperUri != null) R.string.settings_wallpaper_custom else R.string.settings_wallpaper_choose))
                },
                trailingContent = {
                    if (wallpaperUri != null) {
                        TextButton(onClick = {
                            settings.wallpaperUri.set(null)
                            settings.backgroundMode.set(BackgroundMode.PLAIN)
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
        }

        if (backgroundMode == BackgroundMode.WALLPAPER && wallpaperUri != null) {
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
private fun GridSettingsCard(grid: GridSettings, isGroup: Boolean, onEdit: (SliderSpec) -> Unit) {
    val soundManager = LocalSoundManager.current
    val alignment by grid.alignment.flow.collectAsStateWithLifecycle()
    SettingsCard {
        gridSliderSpecs(grid, isGroup).forEach { spec ->
            val value by spec.setting.flow.collectAsStateWithLifecycle()
            SettingsDialogItem(
                label = stringResource(spec.itemLabel),
                value = formatValue(value, spec.unit),
                onClick = { onEdit(spec) }
            )
        }
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_alignment)) },
            trailingContent = {
                SingleChoiceSegmentedButtonRow {
                    GridAlignment.entries.forEachIndexed { i, option ->
                        SegmentedButton(
                            selected = alignment == option,
                            onClick = {
                                soundManager?.play(UiSound.CONFIRM)
                                grid.alignment.set(option)
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, GridAlignment.entries.size)
                        ) {
                            Text(stringResource(if (option == GridAlignment.START) R.string.alignment_left else R.string.alignment_center))
                        }
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentSwatches(selected: Accent, custom: Int, onSelect: (Accent) -> Unit, onEditCustom: () -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Accent.entries.forEach { accent ->
            AccentButton(accent = accent, color = accentColor(accent, custom), selected = selected == accent) {
                if (accent == Accent.CUSTOM) onEditCustom() else onSelect(accent)
            }
        }
    }
}

@Composable
private fun AccentButton(accent: Accent, color: Color, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ColorSwatch(
            color = color,
            selected = selected,
            isCustom = accent == Accent.CUSTOM,
            size = ACCENT_SWATCH_SIZE,
            contentDescription = null,
            onClick = onClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(accent.label),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    isCustom: Boolean,
    size: Dp,
    contentDescription: String?,
    onClick: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier = Modifier
            .size(size)
            .focusRing(focused = focused, cornerRadius = null)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = LocalIndication.current) {
                soundManager?.play(UiSound.CONFIRM)
                onClick()
            }
            .background(if (isCustom) Brush.sweepGradient(RAINBOW) else SolidColor(color))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        if (isCustom) {
            Box(
                modifier = Modifier
                    .size(size * 0.55f)
                    .clip(CircleShape)
                    .background(if (selected) color else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (!selected) Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(size * 0.33f))
            }
        }
    }
}

private val ACCENT_SWATCH_SIZE = 48.dp
private val PLAIN_SWATCH_SIZE = 40.dp

@get:StringRes
private val PlainBase.label: Int
    get() = when (this) {
        PlainBase.NEUTRAL -> R.string.plain_neutral
        PlainBase.COOL -> R.string.plain_cool
        PlainBase.WARM -> R.string.plain_warm
        PlainBase.VIOLET -> R.string.plain_violet
        PlainBase.GREEN -> R.string.plain_green
        PlainBase.ROSE -> R.string.plain_rose
        PlainBase.CUSTOM -> R.string.accent_custom
    }

@get:StringRes
private val Accent.label: Int
    get() = when (this) {
        Accent.TURQUOISE -> R.string.accent_turquoise
        Accent.SPROUT_GREEN -> R.string.accent_sprout_green
        Accent.TANGERINE -> R.string.accent_tangerine
        Accent.GRAPE -> R.string.accent_grape
        Accent.CORAL -> R.string.accent_coral
        Accent.SUN -> R.string.accent_sun
        Accent.CUSTOM -> R.string.accent_custom
    }

private val RAINBOW = listOf(
    Color(0xFFFF5A6A), Color(0xFFFFBF1F), Color(0xFF34B35A), Color(0xFF12B0D4), Color(0xFF8A4FFF), Color(0xFFFF5A6A)
)

@Composable
private fun ColorPickerDialog(
    title: String,
    initial: Int,
    warnLowContrast: Boolean,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val start = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial, it) } }
    var hue by remember { mutableFloatStateOf(start[0]) }
    var saturation by remember { mutableFloatStateOf(start[1]) }
    var brightness by remember { mutableFloatStateOf(start[2]) }
    val picked = Color.hsv(hue, saturation, brightness)
    val background = MaterialTheme.colorScheme.background
    val lowContrast = warnLowContrast && contrastRatio(picked, background) < MIN_ACCENT_CONTRAST

    SproutAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            val scroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .snapScrollToEdges(scroll)
                    .verticalScroll(scroll)
                    .padding(PICKER_RING_ROOM),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(picked))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(R.string.prompt_launch),
                                color = contentColorFor(picked),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(picked)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(3.dp, picked, RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        Text(
                            text = "#%06X".format(picked.toArgb() and 0xFFFFFF),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (lowContrast) {
                            Text(
                                text = stringResource(R.string.custom_accent_low_contrast),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(PICKER_PREVIEW_WIDTH)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GradientSlider(
                        label = stringResource(R.string.custom_accent_hue),
                        value = hue / 360f,
                        display = "${hue.roundToInt()}°",
                        track = (0..6).map { Color.hsv(it * 60f % 360f, saturation, brightness) },
                        thumb = picked,
                        onChange = { hue = it * 360f }
                    )
                    GradientSlider(
                        label = stringResource(R.string.custom_accent_saturation),
                        value = saturation,
                        display = "${(saturation * 100).roundToInt()}%",
                        track = listOf(Color.hsv(hue, 0f, brightness), Color.hsv(hue, 1f, brightness)),
                        thumb = picked,
                        onChange = { saturation = it }
                    )
                    GradientSlider(
                        label = stringResource(R.string.custom_accent_brightness),
                        value = brightness,
                        display = "${(brightness * 100).roundToInt()}%",
                        track = listOf(Color.Black, Color.hsv(hue, saturation, 1f)),
                        thumb = picked,
                        onChange = { brightness = it }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                soundManager?.play(UiSound.BACK)
                onDismiss()
            }) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            TextButton(onClick = {
                soundManager?.play(UiSound.CONFIRM)
                onSave(picked.toArgb())
                onDismiss()
            }) { Text(stringResource(R.string.action_save)) }
        }
    )
}

private const val MIN_ACCENT_CONTRAST = 2f
private val PICKER_PREVIEW_WIDTH = 160.dp
private val PICKER_RING_ROOM = Motion.RingGap + Motion.RingWidth + 2.dp
private val SLIDER_HEIGHT = 36.dp

@Composable
private fun GradientSlider(label: String, value: Float, display: String, track: List<Color>, thumb: Color, onChange: (Float) -> Unit) {
    val soundManager = LocalSoundManager.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(focused) { if (focused) soundManager?.playMove() }
    Column(
        modifier = Modifier
            .focusRing(focused = focused, cornerRadius = 16.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(display, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(track))
            )
            Slider(
                value = value,
                onValueChange = onChange,
                interactionSource = interaction,
                modifier = Modifier.height(SLIDER_HEIGHT).onPreviewKeyEvent { event ->
                    val direction = when (event.key) {
                        Key.DirectionUp -> FocusDirection.Up
                        Key.DirectionDown -> FocusDirection.Down
                        else -> return@onPreviewKeyEvent false
                    }
                    if (event.type == KeyEventType.KeyDown) focusManager.moveFocus(direction)
                    true
                },
                colors = SliderDefaults.colors(
                    thumbColor = thumb,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )
        }
    }
}

@get:StringRes
private val BackgroundMode.label: Int
    get() = when (this) {
        BackgroundMode.PLAIN -> R.string.background_plain
        BackgroundMode.WALLPAPER -> R.string.background_wallpaper
        BackgroundMode.AMBIENT -> R.string.background_ambient
    }

@get:StringRes
private val BackgroundMode.description: Int
    get() = when (this) {
        BackgroundMode.PLAIN -> R.string.background_plain_description
        BackgroundMode.WALLPAPER -> R.string.background_wallpaper_description
        BackgroundMode.AMBIENT -> R.string.background_ambient_description
    }

private val AmbientSource.label: Int
    get() = when (this) {
        AmbientSource.SELECTED_APP -> R.string.ambient_selected_app
        AmbientSource.ACCENT -> R.string.ambient_accent
        AmbientSource.CUSTOM -> R.string.ambient_custom
    }

private val AmbientSource.description: Int
    get() = when (this) {
        AmbientSource.SELECTED_APP -> R.string.ambient_selected_app_description
        AmbientSource.ACCENT -> R.string.ambient_accent_description
        AmbientSource.CUSTOM -> R.string.ambient_custom_description
    }
