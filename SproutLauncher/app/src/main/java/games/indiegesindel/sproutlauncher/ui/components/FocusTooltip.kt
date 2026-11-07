package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import games.indiegesindel.sproutlauncher.ui.theme.LocalSproutColors

@Composable
fun FocusTooltip(text: String, showFolderIcon: Boolean = false) {
    val density = LocalDensity.current
    val gap = with(density) { (Motion.RingGap + Motion.RingWidth).roundToPx() }
    val slidePx = with(density) { POP_SLIDE.toPx() }
    val accent = LocalSproutColors.current.accent
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val pop = remember { Animatable(POP_START_SCALE) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 1f, stiffness = 900f)) }

    Popup(
        popupPositionProvider = remember(gap) { AboveAnchor(gap) },
        properties = PopupProperties(focusable = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                val p = ((pop.value - POP_START_SCALE) / (1f - POP_START_SCALE)).coerceIn(0f, 1f)
                scaleX = pop.value
                scaleY = pop.value
                translationY = (1f - p) * slidePx
                alpha = p
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
        ) {
            Row(
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .background(accent, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showFolderIcon) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = onAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = text,
                    color = onAccent,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .size(10.dp)
                    .rotate(45f)
                    .background(accent, RoundedCornerShape(2.dp))
            )
        }
    }
}

private class AboveAnchor(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = anchorBounds.center.x - popupContentSize.width / 2
        val y = anchorBounds.top - gapPx - popupContentSize.height
        return IntOffset(
            x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y.coerceAtLeast(0)
        )
    }
}

private const val POP_START_SCALE = 0.92f
private val POP_SLIDE = 3.dp
