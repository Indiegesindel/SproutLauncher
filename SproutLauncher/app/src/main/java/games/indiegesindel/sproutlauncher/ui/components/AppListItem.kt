package games.indiegesindel.sproutlauncher.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.IconUtils
import android.content.pm.ResolveInfo

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: ResolveInfo,
    isSelected: Boolean,
    tile: AppTile?,
    onToggle: () -> Unit,
    id: String? = null,
    isTargetFocused: Boolean = false,
    onFocused: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(app) { app.loadLabel(pm).toString() }
    var isFocused by remember { mutableStateOf(false) }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused && id != null) onFocused(id)
            }
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_X && 
                    event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    onToggle()
                    true
                } else {
                    false
                }
            }
            .focusable()
            .then(
                if (isFocused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else Modifier
            )
            .combinedClickable(
                onClick = {
                    val intent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                },
                onLongClick = onToggle
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isSelected,
            onCheckedChange = null // Read-only as per requirements, toggled via long press
        )
    }
}
