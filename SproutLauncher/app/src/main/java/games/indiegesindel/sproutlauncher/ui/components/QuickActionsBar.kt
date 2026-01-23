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
import androidx.compose.ui.graphics.painter.Painter
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

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

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
    onFocusChanged: (Boolean) -> Unit = {},
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 8.dp)
            .horizontalScroll(scrollState)
            .onFocusChanged { state ->
                // Check if any child is focused
                onFocusChanged(state.hasFocus)
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            id = "action:all_apps",
            label = "All Apps",
            onClick = onAllAppsClick,
            isTargetFocused = focusedItemId == "action:all_apps",
            onFocused = { onFocusItemIdChanged(it) },
            icon = Icons.Filled.Apps
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
            id = "action:browser",
            label = "Browser",
            onClick = onBrowserClick,
            isTargetFocused = focusedItemId == "action:browser",
            onFocused = { onFocusItemIdChanged(it) },
            packageName = "com.android.chrome", // Defaulting to Chrome for themed icon if available
            icon = Icons.Filled.Language
        )
        
        if (onYouTubeClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:youtube",
                label = "YouTube",
                onClick = onYouTubeClick,
                isTargetFocused = focusedItemId == "action:youtube",
                onFocused = { onFocusItemIdChanged(it) },
                packageName = "com.google.android.youtube",
                icon = Icons.Filled.PlayArrow
            )
        }

        if (onPlayStoreClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:play_store",
                label = "Play Store",
                onClick = onPlayStoreClick,
                isTargetFocused = focusedItemId == "action:play_store",
                onFocused = { onFocusItemIdChanged(it) },
                packageName = "com.android.vending",
                icon = Icons.Filled.Shop
            )
        }

        if (onDiscordClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:discord",
                label = "Discord",
                onClick = onDiscordClick,
                isTargetFocused = focusedItemId == "action:discord",
                onFocused = { onFocusItemIdChanged(it) },
                packageName = "com.discord",
                icon = Icons.AutoMirrored.Filled.Chat
            )
        }

        if (onSpotifyClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:spotify",
                label = "Spotify",
                onClick = onSpotifyClick,
                isTargetFocused = focusedItemId == "action:spotify",
                onFocused = { onFocusItemIdChanged(it) },
                packageName = "com.spotify.music",
                icon = Icons.Filled.MusicNote
            )
        }

        if (onPhotosClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:photos",
                label = "Photos",
                onClick = onPhotosClick,
                isTargetFocused = focusedItemId == "action:photos",
                onFocused = { onFocusItemIdChanged(it) },
                packageName = "com.google.android.apps.photos",
                icon = Icons.Filled.Image
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            id = "action:settings",
            label = "Settings",
            onClick = onSettingsClick,
            isTargetFocused = focusedItemId == "action:settings",
            onFocused = { onFocusItemIdChanged(it) },
            icon = Icons.Filled.Settings
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickActionButton(
    id: String,
    label: String,
    onClick: () -> Unit,
    isTargetFocused: Boolean,
    onFocused: (String) -> Unit,
    icon: ImageVector? = null,
    packageName: String? = null
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

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

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged { 
                    isFocused = it.isFocused
                    if (it.isFocused) onFocused(id)
                }
                .then(
                    if (isFocused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                )
                .clip(CircleShape)
                .clickable(onClick = onClick),
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
