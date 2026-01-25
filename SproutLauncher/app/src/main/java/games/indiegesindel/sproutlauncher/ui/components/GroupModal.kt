package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.ExperimentalComposeUiApi
import games.indiegesindel.sproutlauncher.model.AppTile

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GroupModal(
    group: AppTile,
    onDismiss: () -> Unit,
    onAppClick: (AppTile) -> Unit,
    onRemoveFromGroup: (String) -> Unit,
    onSettings: (AppTile) -> Unit,
    onReorder: (Int, Int) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    focusedItemId: String?,
    onFocusItemIdChanged: (String?) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    rows: Int = 2,
    horizontalSpacing: Int = 0,
    verticalSpacing: Int = 0,
    roundness: Int = 16
) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Full screen dimmed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )

        // Content area avoiding system bars and footer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding() + 56.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxSize(0.9f) // Slightly more space if padded
                    .clip(RoundedCornerShape(roundness.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) { } // Consume clicks
                    .focusProperties { exit = { FocusRequester.Cancel } }
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
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AppGrid(
                    appTiles = group.groupTiles,
                    onAppClick = onAppClick,
                    onRemove = { /* Not used here */ },
                    onSettings = onSettings,
                    onReorder = onReorder,
                    onDragEnd = onDragEnd,
                    modifier = Modifier.weight(1f),
                    rows = rows,
                    horizontalSpacing = horizontalSpacing,
                    verticalSpacing = verticalSpacing,
                    roundness = roundness,
                    focusedItemId = focusedItemId,
                    onFocusItemIdChanged = onFocusItemIdChanged,
                    onRemoveFromGroup = onRemoveFromGroup,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
}
