package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

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
    onPowerClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    focusedItemId: String? = null,
    onFocusItemIdChanged: (String?) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 8.dp)
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
            icon = Icons.Filled.Apps,
            label = "All Apps",
            onClick = onAllAppsClick,
            isTargetFocused = focusedItemId == "action:all_apps",
            onFocused = { onFocusItemIdChanged(it) }
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
            icon = Icons.Filled.Language,
            label = "Browser",
            onClick = onBrowserClick,
            isTargetFocused = focusedItemId == "action:browser",
            onFocused = { onFocusItemIdChanged(it) }
        )
        
        if (onYouTubeClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:youtube",
                icon = Icons.Filled.PlayArrow,
                label = "YouTube",
                onClick = onYouTubeClick,
                isTargetFocused = focusedItemId == "action:youtube",
                onFocused = { onFocusItemIdChanged(it) }
            )
        }

        if (onPlayStoreClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:play_store",
                icon = Icons.Filled.Shop,
                label = "Play Store",
                onClick = onPlayStoreClick,
                isTargetFocused = focusedItemId == "action:play_store",
                onFocused = { onFocusItemIdChanged(it) }
            )
        }

        if (onDiscordClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:discord",
                icon = Icons.Filled.Chat,
                label = "Discord",
                onClick = onDiscordClick,
                isTargetFocused = focusedItemId == "action:discord",
                onFocused = { onFocusItemIdChanged(it) }
            )
        }

        if (onSpotifyClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:spotify",
                icon = Icons.Filled.MusicNote,
                label = "Spotify",
                onClick = onSpotifyClick,
                isTargetFocused = focusedItemId == "action:spotify",
                onFocused = { onFocusItemIdChanged(it) }
            )
        }

        if (onPhotosClick != null) {
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionButton(
                id = "action:photos",
                icon = Icons.Filled.Image,
                label = "Photos",
                onClick = onPhotosClick,
                isTargetFocused = focusedItemId == "action:photos",
                onFocused = { onFocusItemIdChanged(it) }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            id = "action:settings",
            icon = Icons.Filled.Settings,
            label = "Settings",
            onClick = onSettingsClick,
            isTargetFocused = focusedItemId == "action:settings",
            onFocused = { onFocusItemIdChanged(it) }
        )

        Spacer(modifier = Modifier.width(16.dp))
        QuickActionButton(
            id = "action:power",
            icon = Icons.Filled.PowerSettingsNew,
            label = "Power",
            onClick = onPowerClick,
            isTargetFocused = focusedItemId == "action:power",
            onFocused = { onFocusItemIdChanged(it) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickActionButton(
    id: String,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isTargetFocused: Boolean,
    onFocused: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

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
                .padding(if (isFocused) 4.dp else 0.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.secondary,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = label, 
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}
