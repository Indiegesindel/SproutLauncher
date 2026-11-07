package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.data.GridLayout
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimatedVisibilityScope.GroupModal(
    group: AppTile,
    origin: Rect?,
    layout: GridLayout,
    actions: TileGridActions,
    pendingFocusTileId: String?,
    onFocusConsumed: () -> Unit,
    onTileFocused: (AppTile) -> Unit,
    contentPadding: PaddingValues,
    enabled: Boolean
) {
    val soundManager = LocalSoundManager.current
    val dismiss = {
        soundManager?.play(UiSound.FOLDER_CLOSE)
        actions.onBack()
    }

    val reveal by transition.animateFloat(
        transitionSpec = { tween(Motion.FOLDER_ZOOM_MS, easing = Motion.Emphasized) },
        label = "folderReveal"
    ) { if (it == EnterExitState.Visible) 1f else 0f }
    var panelBounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    val panelRadiusPx = with(density) { layout.roundness.dp.toPx() }

    OverlayScrim(onDismiss = dismiss, enabled = enabled, contentPadding = contentPadding) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .onGloballyPositioned { panelBounds = it.boundsInRoot() }
                .graphicsLayer {
                    val panel = panelBounds ?: return@graphicsLayer
                    val full = Rect(Offset.Zero, size)
                    val start = origin?.translate(-panel.topLeft) ?: full.scaledAroundCenter(FALLBACK_START_SCALE)
                    clip = true
                    shape = RoundedRectShape(lerp(start, full, reveal), panelRadiusPx)
                    alpha = (reveal * 3f).coerceAtMost(1f)
                }
                .clip(RoundedCornerShape(layout.roundness.dp))
                .background(MaterialTheme.colorScheme.surface)
                .consumeClicks()
                .focusProperties { onExit = { cancelFocusChange() } }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = ((reveal - 0.35f) / 0.65f).coerceIn(0f, 1f) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    CloseButton(onClick = dismiss, enabled = enabled)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TileGrid(
                    tiles = group.groupTiles,
                    layout = layout,
                    actions = actions,
                    menuContext = TileMenuContext(isInGroup = true, hasGroups = true, isInMultiSelectMode = false),
                    modifier = Modifier.weight(1f),
                    pendingFocusTileId = pendingFocusTileId,
                    onFocusConsumed = onFocusConsumed,
                    onTileFocused = onTileFocused,
                    enabled = enabled,
                    entranceDelayMs = Motion.FOLDER_ITEM_DELAY_MS
                )
            }
        }
    }
}

private const val FALLBACK_START_SCALE = 0.8f

private fun Rect.scaledAroundCenter(factor: Float): Rect {
    val w = width * factor
    val h = height * factor
    return Rect(center.x - w / 2f, center.y - h / 2f, center.x + w / 2f, center.y + h / 2f)
}

private class RoundedRectShape(private val rect: Rect, private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(RoundRect(rect, CornerRadius(radius)))
}

@Composable
internal fun OverlayScrim(
    onDismiss: () -> Unit,
    contentPadding: PaddingValues,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(contentPadding)
            .padding(bottom = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

internal fun Modifier.consumeClicks(): Modifier = clickable(enabled = false) {}

@Composable
internal fun CloseButton(onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) soundManager?.play(UiSound.MOVE)
            }
            .then(
                if (isFocused) Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape) else Modifier
            )
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.action_close),
            tint = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}
