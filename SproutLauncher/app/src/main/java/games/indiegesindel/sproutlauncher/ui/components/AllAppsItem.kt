package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.launch

private val ItemShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppsItem(
    label: String,
    iconModel: Any,
    isOnHomeScreen: Boolean,
    onLaunch: () -> Unit,
    menuActions: List<MenuAction>,
    onFocused: () -> Unit,
    onBack: () -> Unit,
    enabled: Boolean
) {
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val launch = {
        soundManager?.play(UiSound.CONFIRM)
        onLaunch()
    }

    Box(contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .onSizeChanged { size = it }
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (!it.isFocused) return@onFocusChanged
                    soundManager?.play(UiSound.MOVE)
                    onFocused()
                    coroutineScope.launch {
                        val verticalPadding = with(density) { 16.dp.toPx() }
                        bringIntoViewRequester.bringIntoView(
                            Rect(0f, -verticalPadding, size.width.toFloat(), size.height + verticalPadding)
                        )
                    }
                }
                .gamepadActions(
                    enabled = enabled,
                    onConfirm = launch,
                    onMenu = { showMenu = true },
                    onBack = {
                        soundManager?.play(UiSound.BACK)
                        onBack()
                    }
                )
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isOnHomeScreen -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    },
                    shape = ItemShape
                )
                .then(
                    if (isOnHomeScreen && !isFocused) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, ItemShape)
                    } else {
                        Modifier
                    }
                )
                .focusable(enabled = enabled)
                .combinedClickable(
                    enabled = enabled,
                    onClick = launch,
                    onDoubleClick = { showMenu = true },
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = iconModel,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }

        ContextMenu(expanded = showMenu, onDismiss = { showMenu = false }, actions = menuActions)
    }
}
