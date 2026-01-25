package games.indiegesindel.sproutlauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.imageLoader
import coil.size.Size
import games.indiegesindel.sproutlauncher.model.AppTile
import android.content.pm.ResolveInfo
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import games.indiegesindel.sproutlauncher.utils.IconUtils
import games.indiegesindel.sproutlauncher.utils.LauncherUtils
import androidx.core.net.toUri

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    app: ResolveInfo?,
    isOnHomeScreen: Boolean,
    tile: AppTile?,
    onToggleHomeScreen: () -> Unit,
    onEdit: () -> Unit = {},
    id: String? = null,
    isTargetFocused: Boolean = false,
    onFocused: (String) -> Unit = {},
    isInMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onCreateGroup: () -> Unit = {},
    onGroupSelected: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onOpenGroup: () -> Unit = {},
    onUngroup: () -> Unit = {},
    onRemoveFromGroup: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(app?.activityInfo?.packageName, app?.activityInfo?.name, tile?.label) {
        tile?.label ?: app?.loadLabel(pm)?.toString() ?: ""
    }
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    val shortcutIcon = remember(tile?.packageName, tile?.shortcutId) {
        tile?.shortcutId?.let { IconUtils.getShortcutIcon(context, tile.packageName, it) }
    }

    val iconPainter: Painter = rememberAsyncImagePainter(
        model = remember(app?.activityInfo?.packageName, app?.activityInfo?.name, tile?.iconUri, shortcutIcon) {
            ImageRequest.Builder(context)
                .data(tile?.iconUri ?: shortcutIcon ?: app)
                .size(Size(512, 512))
                .crossfade(true)
                .build()
        },
        imageLoader = context.imageLoader
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onGloballyPositioned { size = it.size }
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) {
                        if (id != null) onFocused(id)
                        coroutineScope.launch {
                            val verticalPadding = with(density) { 16.dp.toPx() }
                            bringIntoViewRequester.bringIntoView(
                                Rect(
                                    left = 0f,
                                    top = -verticalPadding,
                                    right = size.width.toFloat(),
                                    bottom = size.height.toFloat() + verticalPadding
                                )
                            )
                        }
                    }
                }
                .onKeyEvent { event ->
                    val isDown = event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
                    if (isDown && (
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_X ||
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_START ||
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU
                    )) {
                        showMenu = true
                        true
                    } else if (isDown && (
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_A
                    )) {
                        if (isInMultiSelectMode) {
                            onToggleSelection()
                        } else if (tile?.isGroup == true) {
                            onOpenGroup()
                        } else if (tile != null) {
                            LauncherUtils.launchTile(context, tile)
                        } else if (app != null) {
                            LauncherUtils.launchApp(context, app.activityInfo.packageName)
                        }
                        true
                    } else if (isDown && event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                        if (onRemoveFromGroup != null) {
                            onDismiss()
                        }
                        true
                    } else {
                        false
                    }
                }
                .background(
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isOnHomeScreen -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .then(
                    if (isOnHomeScreen && !isFocused && !isSelected) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                )
                .combinedClickable(
                    onClick = {
                        if (isInMultiSelectMode) {
                            onToggleSelection()
                        } else if (tile?.isGroup == true) {
                            onOpenGroup()
                        } else if (tile != null) {
                            LauncherUtils.launchTile(context, tile)
                        } else if (app != null) {
                            LauncherUtils.launchApp(context, app.activityInfo.packageName)
                        }
                    },
                    onDoubleClick = { showMenu = true },
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                if (tile?.isGroup == true && tile.iconUri == null) {
                    GroupIcon(groupTiles = tile.groupTiles)
                } else {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(2.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (isInMultiSelectMode && isSelected) {
                DropdownMenuItem(
                    text = { Text("Group selected") },
                    onClick = {
                        showMenu = false
                        onGroupSelected()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Clear selection") },
                    onClick = {
                        showMenu = false
                        onClearSelection()
                    }
                )
            } else if (tile?.isGroup == true) {
                DropdownMenuItem(
                    text = { Text("Open") },
                    onClick = {
                        showMenu = false
                        onOpenGroup()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Ungroup") },
                    onClick = {
                        showMenu = false
                        onUngroup()
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Launch") },
                    onClick = {
                        showMenu = false
                        if (tile != null) {
                            LauncherUtils.launchTile(context, tile)
                        } else if (app != null) {
                            LauncherUtils.launchApp(context, app.activityInfo.packageName)
                        }
                    }
                )
                if (isOnHomeScreen) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    if (onRemoveFromGroup == null) {
                        DropdownMenuItem(
                            text = { Text("Create group") },
                            onClick = {
                                showMenu = false
                                onCreateGroup()
                            }
                        )
                    }
                }
                if (tile?.shortcutId == null && app != null) {
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${app.activityInfo.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                if (onRemoveFromGroup != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from group") },
                        onClick = {
                            showMenu = false
                            onRemoveFromGroup()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(if (isOnHomeScreen) "Remove from Homescreen" else "Add to Homescreen") },
                    onClick = {
                        showMenu = false
                        onToggleHomeScreen()
                    }
                )
                if (tile?.shortcutId == null && app != null) {
                    DropdownMenuItem(
                        text = { Text("Uninstall") },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = "package:${app.activityInfo.packageName}".toUri()
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Log or handle error
                            }
                        }
                    )
                }
            }
        }
    }
}
