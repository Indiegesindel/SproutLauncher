package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import games.indiegesindel.sproutlauncher.R
import kotlinx.coroutines.delay
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.utils.ControllerRumble
import games.indiegesindel.sproutlauncher.utils.LocalControllerRumble
import games.indiegesindel.sproutlauncher.utils.UiSound
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(24.dp)
private val IconShape = RoundedCornerShape(18.dp)
private val ICON_SIZE = 64.dp
private val BADGE_SIZE = 20.dp
private const val POP_FROM = 0.6f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppsItem(
    label: String,
    iconModel: Any,
    isOnHomeScreen: Boolean,
    onLaunch: (Rect?) -> Boolean,
    menuActions: List<MenuAction>,
    onFocused: () -> Unit,
    onBack: () -> Unit,
    enabled: Boolean,
    popDelayMs: Long? = null
) {
    val soundManager = LocalSoundManager.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var isFocused by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var boundsInRoot by remember { mutableStateOf<Rect?>(null) }
    val rumble = LocalControllerRumble.current
    val bump = rememberEdgeBump()
    val onEdge = rememberEdgeFeedback(bump)
    val shake = remember { Animatable(0f) }

    val focusScale by animateFloatAsState(if (isFocused) Motion.FOCUS_SCALE else 1f, Motion.focus(), label = "allAppsScale")
    val pop = remember { Animatable(if (popDelayMs != null) POP_FROM else 1f) }
    LaunchedEffect(Unit) {
        if (popDelayMs == null) return@LaunchedEffect
        delay(popDelayMs)
        pop.animateTo(1f, Motion.pop())
    }

    val launch: () -> Unit = {
        soundManager?.play(UiSound.CONFIRM)
        rumble?.pulse(ControllerRumble.Pulse.CONFIRM)
        if (onLaunch(boundsInRoot)) {
            soundManager?.playDelayed(UiSound.LAUNCH, 110L)
        } else {
            soundManager?.play(UiSound.ERROR)
            coroutineScope.launch { shake.shakeOnce(with(density) { 6.dp.toPx() }) }
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onSizeChanged { size = it }
                .onGloballyPositioned { boundsInRoot = it.boundsInRoot() }
                .graphicsLayer {
                    scaleX = focusScale * pop.value
                    scaleY = focusScale * pop.value
                    alpha = ((pop.value - POP_FROM) / (1f - POP_FROM)).coerceIn(0f, 1f)
                    translationX = bump.offset.value.x + shake.value
                    translationY = bump.offset.value.y
                }
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (!it.isFocused) return@onFocusChanged
                    soundManager?.playMove()
                    onFocused()
                    coroutineScope.launch {
                        val verticalPadding = with(density) { 16.dp.toPx() }
                        bringIntoViewRequester.bringIntoView(
                            Rect(0f, -verticalPadding, size.width.toFloat(), size.height + verticalPadding)
                        )
                    }
                }
                .edgeAwareNavigation(enabled = enabled, onEdge = onEdge)
                .gamepadActions(
                    enabled = enabled,
                    onConfirm = launch,
                    onMenu = { showMenu = true },
                    onBack = {
                        soundManager?.play(UiSound.BACK)
                        onBack()
                    }
                )
                .focusRing(focused = isFocused, cornerRadius = 24.dp)
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .focusable(enabled = enabled)
                .combinedClickable(
                    enabled = enabled,
                    onClick = launch,
                    onDoubleClick = { showMenu = true },
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = iconModel,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ICON_SIZE)
                        .shadow(4.dp, IconShape)
                        .clip(IconShape)
                )
                if (isOnHomeScreen) HomeBadge(modifier = Modifier.align(Alignment.BottomEnd).offset(6.dp, 6.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        ContextMenu(expanded = showMenu, onDismiss = { showMenu = false }, actions = menuActions)
    }
}

@Composable
private fun HomeBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(BADGE_SIZE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(2.dp, MaterialTheme.colorScheme.surfaceContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = stringResource(R.string.all_apps_on_home),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(12.dp)
        )
    }
}
