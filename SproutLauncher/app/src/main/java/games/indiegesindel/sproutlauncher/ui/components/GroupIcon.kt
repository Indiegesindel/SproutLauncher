package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.IconUtils

@Composable
fun GroupIcon(
    groupTiles: List<AppTile>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.weight(1f).padding(3.dp)) {
                    groupTiles.getOrNull(0)?.let { SmallIcon(it) }
                }
                Box(modifier = Modifier.weight(1f).padding(3.dp)) {
                    groupTiles.getOrNull(1)?.let { SmallIcon(it) }
                }
            }
            Row(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.weight(1f).padding(3.dp)) {
                    groupTiles.getOrNull(2)?.let { SmallIcon(it) }
                }
                Box(modifier = Modifier.weight(1f).padding(3.dp)) {
                    groupTiles.getOrNull(3)?.let { SmallIcon(it) }
                }
            }
        }
    }
}

@Composable
private fun SmallIcon(tile: AppTile) {
    val context = LocalContext.current
    val shortcutIcon = remember(tile.packageName, tile.shortcutId) {
        tile.shortcutId?.let { IconUtils.getShortcutIcon(context, tile.packageName, it) }
    }
    
    val appIcon = remember(tile.packageName) {
        IconUtils.getFullSquareIcon(context, tile.packageName, 128)
    }

    Image(
        painter = rememberAsyncImagePainter(
            model = remember(tile.packageName, tile.activityName, tile.iconUri, shortcutIcon, appIcon) {
                ImageRequest.Builder(context)
                    .data(tile.iconUri ?: shortcutIcon ?: appIcon)
                    .size(Size(128, 128))
                    .crossfade(true)
                    .build()
            }
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
    )
}
