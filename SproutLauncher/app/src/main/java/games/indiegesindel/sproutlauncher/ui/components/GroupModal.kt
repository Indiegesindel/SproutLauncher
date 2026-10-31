package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.data.GridLayout
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimatedVisibilityScope.GroupModal(
    group: AppTile,
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
        soundManager?.play(UiSound.BACK)
        actions.onBack()
    }

    OverlayScrim(onDismiss = dismiss, enabled = enabled, contentPadding = contentPadding) {
        Box(
            modifier = Modifier
                .animateEnterExit(
                    enter = fadeIn(tween(150)) + scaleIn(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        initialScale = 0.82f
                    ),
                    exit = fadeOut(tween(100)) + scaleOut(animationSpec = tween(100), targetScale = 0.92f)
                )
                .fillMaxSize(0.9f)
                .clip(RoundedCornerShape(layout.roundness.dp))
                .background(MaterialTheme.colorScheme.surface)
                .consumeClicks()
                .focusProperties { onExit = { cancelFocusChange() } }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                    enabled = enabled
                )
            }
        }
    }
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
