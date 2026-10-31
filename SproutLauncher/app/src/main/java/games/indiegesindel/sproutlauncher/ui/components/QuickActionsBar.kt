package games.indiegesindel.sproutlauncher.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.model.QuickApp
import games.indiegesindel.sproutlauncher.ui.icons.AppIcon
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

private const val BROWSER_ICON_PACKAGE = "com.android.chrome"
private const val QUICK_ACTION_ICON_PX = 192

@get:StringRes
private val QuickApp.label: Int
    get() = when (this) {
        QuickApp.YOUTUBE -> R.string.quick_action_youtube
        QuickApp.PLAY_STORE -> R.string.quick_action_play_store
        QuickApp.DISCORD -> R.string.quick_action_discord
        QuickApp.SPOTIFY -> R.string.quick_action_spotify
        QuickApp.PHOTOS -> R.string.quick_action_photos
    }

private val QuickApp.fallbackIcon: ImageVector
    get() = when (this) {
        QuickApp.YOUTUBE -> Icons.Filled.PlayArrow
        QuickApp.PLAY_STORE -> Icons.Filled.Shop
        QuickApp.DISCORD -> Icons.AutoMirrored.Filled.Chat
        QuickApp.SPOTIFY -> Icons.Filled.MusicNote
        QuickApp.PHOTOS -> Icons.Filled.Image
    }

@Composable
fun QuickActionsBar(
    quickApps: List<QuickApp>,
    onAllApps: () -> Unit,
    onBrowser: () -> Unit,
    onQuickApp: (QuickApp) -> Unit,
    onSystemSettings: () -> Unit,
    onFocused: () -> Unit,
    allAppsFocusRequester: FocusRequester,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            label = stringResource(R.string.quick_action_all_apps),
            icon = Icons.Filled.Apps,
            onClick = onAllApps,
            onFocused = onFocused,
            enabled = enabled,
            modifier = Modifier.focusRequester(allAppsFocusRequester)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        QuickActionButton(
            label = stringResource(R.string.quick_action_browser),
            icon = Icons.Filled.Language,
            iconPackage = BROWSER_ICON_PACKAGE,
            onClick = onBrowser,
            onFocused = onFocused,
            enabled = enabled
        )
        quickApps.forEach { app ->
            QuickActionButton(
                label = stringResource(app.label),
                icon = app.fallbackIcon,
                iconPackage = app.packageName,
                onClick = { onQuickApp(app) },
                onFocused = onFocused,
                enabled = enabled
            )
        }
        QuickActionButton(
            label = stringResource(R.string.quick_action_settings),
            icon = Icons.Filled.Settings,
            onClick = onSystemSettings,
            onFocused = onFocused,
            enabled = enabled
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onFocused: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    iconPackage: String? = null
) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }

    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "quickActionScale"
    )
    val borderWidth by animateFloatAsState(
        targetValue = if (isFocused) 2f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "quickActionBorder"
    )

    val tint = MaterialTheme.colorScheme.onSecondary
    val context = LocalContext.current
    val painter = iconPackage?.let { packageName ->
        rememberAsyncImagePainter(
            remember(packageName, tint) {
                ImageRequest.Builder(context).data(AppIcon.Themed(packageName, tint.toArgb())).size(QUICK_ACTION_ICON_PX).build()
            }
        )
    }
    val painterState = painter?.state?.collectAsState()?.value

    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        if (isFocused) FocusTooltip(text = label)

        Surface(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = focusScale
                    scaleY = focusScale
                }
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) {
                        soundManager?.play(UiSound.MOVE)
                        onFocused()
                    }
                }
                .border(borderWidth.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .clickable(enabled = enabled) {
                    soundManager?.play(UiSound.CONFIRM)
                    onClick()
                },
            color = MaterialTheme.colorScheme.secondary,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (painter != null && painterState is AsyncImagePainter.State.Success) {
                    Image(
                        painter = painter,
                        contentDescription = label,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = 1.15f, scaleY = 1.15f)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = tint
                    )
                }
            }
        }
    }
}
