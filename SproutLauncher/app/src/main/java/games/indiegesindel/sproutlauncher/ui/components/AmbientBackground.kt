package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.icons.IconColor
import games.indiegesindel.sproutlauncher.ui.icons.iconModel
import games.indiegesindel.sproutlauncher.ui.motion.Motion
import games.indiegesindel.sproutlauncher.ui.theme.LocalSproutColors

@Composable
fun AmbientBackground(focusedTile: AppTile?, color: Color? = null) {
    val context = LocalContext.current
    val base = MaterialTheme.colorScheme.background
    val fallback = color ?: LocalSproutColors.current.accent
    val isDark = LocalSproutColors.current.isDark
    var echo by remember { mutableStateOf(fallback) }

    LaunchedEffect(focusedTile?.iconModel, fallback) {
        val model = focusedTile?.iconModel
        echo = model?.let { IconColor.of(context, it) }?.let(::Color) ?: fallback
    }

    val tint by animateColorAsState(echo, tween(Motion.ECHO_FADE_MS), label = "ambientEcho")
    val glowAlpha = if (isDark) 0.38f else 0.30f
    val washAlpha = if (isDark) 0.18f else 0.14f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(base)
        drawRect(Brush.verticalGradient(listOf(tint.copy(alpha = washAlpha), Color.Transparent)))
        drawRect(
            Brush.radialGradient(
                colors = listOf(tint.copy(alpha = glowAlpha), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.4f),
                radius = size.maxDimension * 0.6f
            )
        )
    }
}
