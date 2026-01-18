package games.indiegesindel.sproutlauncher.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import coil.imageLoader
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
    id: String? = null,
    isTargetFocused: Boolean = false,
    onFocused: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(app) { app.loadLabel(pm).toString() }
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTargetFocused) {
        if (isTargetFocused) {
            focusRequester.requestFocus()
        }
    }

    val iconPainter: Painter = if (tile?.iconUri != null) {
        rememberAsyncImagePainter(tile.iconUri)
    } else {
        rememberAsyncImagePainter(
            model = app,
            imageLoader = context.imageLoader
        )
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused && id != null) onFocused(id)
                }
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_X &&
                        event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        showMenu = true
                        true
                    } else {
                        false
                    }
                }
                .focusable()
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isOnHomeScreen -> MaterialTheme.colorScheme.secondaryContainer
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .combinedClickable(
                    onClick = {
                        val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                    },
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                if (isOnHomeScreen) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(
                                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                    )
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
                    isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                    isOnHomeScreen -> MaterialTheme.colorScheme.onSecondaryContainer
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
            DropdownMenuItem(
                text = { Text(if (isOnHomeScreen) "Remove from Homescreen" else "Add to Homescreen") },
                onClick = {
                    showMenu = false
                    onToggleHomeScreen()
                }
            )
        }
    }
}
