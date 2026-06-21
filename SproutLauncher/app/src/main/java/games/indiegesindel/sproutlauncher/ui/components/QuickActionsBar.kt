package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import games.indiegesindel.sproutlauncher.utils.IconUtils
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.TransformOrigin

@Composable
fun QuickActionsBar(
    onAllAppsClick: () -> Unit,
    onBrowserClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onYouTubeClick: (() -> Unit)? = null,
    onPlayStoreClick: (() -> Unit)? = null,
    onDiscordClick: (() -> Unit)? = null,
    onSpotifyClick: (() -> Unit)? = null,
    onPhotosClick: (() -> Unit)? = null,
    onQuickActionFocused: () -> Unit = {},
    allAppsFocusRequester: FocusRequester? = null,
    enabled: Boolean = true
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 8.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            label = "All Apps",
            onClick = onAllAppsClick,
            onFocused = onQuickActionFocused,
            icon = Icons.Filled.Apps,
            enabled = enabled,
            focusRequester = allAppsFocusRequester
        )

        Spacer(modifier = Modifier.width(16.dp))
        // Vertical Divider
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.width(16.dp))

        QuickActionButton(
            label = "Browser",
            onClick = onBrowserClick,
            onFocused = onQuickActionFocused,
            packageName = "com.android.chrome", // Defaulting to Chrome for themed icon if available
            icon = Icons.Filled.Language,
            enabled = enabled
        )

        if (onYouTubeClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                label = "YouTube",
                onClick = onYouTubeClick,
                onFocused = onQuickActionFocused,
                packageName = "com.google.android.youtube",
                icon = Icons.Filled.PlayArrow,
                enabled = enabled
            )
        }

        if (onPlayStoreClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                label = "Play Store",
                onClick = onPlayStoreClick,
                onFocused = onQuickActionFocused,
                packageName = "com.android.vending",
                icon = Icons.Filled.Shop,
                enabled = enabled
            )
        }

        if (onDiscordClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                label = "Discord",
                onClick = onDiscordClick,
                onFocused = onQuickActionFocused,
                packageName = "com.discord",
                icon = Icons.AutoMirrored.Filled.Chat,
                enabled = enabled
            )
        }

        if (onSpotifyClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                label = "Spotify",
                onClick = onSpotifyClick,
                onFocused = onQuickActionFocused,
                packageName = "com.spotify.music",
                icon = Icons.Filled.MusicNote,
                enabled = enabled
            )
        }

        if (onPhotosClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                label = "Photos",
                onClick = onPhotosClick,
                onFocused = onQuickActionFocused,
                packageName = "com.google.android.apps.photos",
                icon = Icons.Filled.Image,
                enabled = enabled
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            label = "Settings",
            onClick = onSettingsClick,
            onFocused = onQuickActionFocused,
            icon = Icons.Filled.Settings,
            enabled = enabled
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    onFocused: () -> Unit,
    icon: ImageVector? = null,
    packageName: String? = null,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }

    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "actionFocusScale"
    )
    val focusBorderWidth by animateFloatAsState(
        targetValue = if (isFocused) 2f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "actionBorder"
    )

    val tint = MaterialTheme.colorScheme.onSecondary
    val iconPainter = if (packageName != null) {
        rememberAsyncImagePainter(
            model = remember(packageName, tint) {
                ImageRequest.Builder(context)
                    .data(IconUtils.getThemedIcon(context, packageName, tint))
                    .crossfade(true)
                    .build()
            }
        )
    } else null

    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tooltip
        if (isFocused) {
            val density = LocalDensity.current
            val yOffset = with(density) { (-30).dp.roundToPx() }
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffset),
                properties = PopupProperties(focusable = false)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(120)) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(120),
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.inverseSurface)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp, 7.dp)
                                .background(MaterialTheme.colorScheme.inverseSurface, shape = TriangleShape)
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = focusScale; scaleY = focusScale }
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) {
                        soundManager?.play(UiSound.MOVE)
                        onFocused()
                    }
                }
                .border(focusBorderWidth.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = {
                    soundManager?.play(UiSound.CONFIRM)
                    onClick()
                }),
            color = MaterialTheme.colorScheme.secondary,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = label,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = 1.15f, scaleY = 1.15f)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize()
                            .padding(12.dp),
                        tint = tint
                    )
                }
            }
        }
    }
}
