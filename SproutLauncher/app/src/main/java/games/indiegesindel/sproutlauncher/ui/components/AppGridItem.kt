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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.imageLoader
import coil.size.Size
import games.indiegesindel.sproutlauncher.model.AppTile
import android.content.pm.ResolveInfo
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    app: ResolveInfo,
    isOnHomeScreen: Boolean,
    tile: AppTile?,
    onToggleHomeScreen: () -> Unit,
    onEdit: () -> Unit = {},
    id: String? = null,
    isTargetFocused: Boolean = false,
    onFocused: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(app.activityInfo.packageName, app.activityInfo.name) { app.loadLabel(pm).toString() }
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    val iconPainter: Painter = rememberAsyncImagePainter(
        model = remember(app.activityInfo.packageName, app.activityInfo.name, tile?.iconUri) {
            ImageRequest.Builder(context)
                .data(tile?.iconUri ?: app)
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
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused && id != null) onFocused(id)
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
                        val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                        true
                    } else {
                        false
                    }
                }
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isOnHomeScreen -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .then(
                    if (isOnHomeScreen && !isFocused) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                )
                .combinedClickable(
                    onClick = {
                        val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                    },
                    onDoubleClick = { showMenu = true },
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
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
                    isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Launch") },
                onClick = {
                    showMenu = false
                    val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
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
            }
            DropdownMenuItem(
                text = { Text("Details") },
                onClick = {
                    showMenu = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.activityInfo.packageName}")
                    }
                    context.startActivity(intent)
                }
            )
            DropdownMenuItem(
                text = { Text(if (isOnHomeScreen) "Remove from Homescreen" else "Add to Homescreen") },
                onClick = {
                    showMenu = false
                    onToggleHomeScreen()
                }
            )
            DropdownMenuItem(
                text = { Text("Uninstall") },
                onClick = {
                    showMenu = false
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.activityInfo.packageName}")
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
