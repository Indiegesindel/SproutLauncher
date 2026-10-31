package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.R
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddToGroupModal(
    groups: List<AppTile>,
    onGroupSelected: (groupId: String) -> Unit,
    onDismiss: () -> Unit,
    contentPadding: PaddingValues
) {
    val soundManager = LocalSoundManager.current
    val dismiss = {
        soundManager?.play(UiSound.BACK)
        onDismiss()
    }
    val closeFocusRequester = remember { FocusRequester() }
    val firstGroupFocusRequester = remember { FocusRequester() }

    LaunchedEffect(groups.isEmpty()) {
        (if (groups.isEmpty()) closeFocusRequester else firstGroupFocusRequester).requestFocusWithRetry()
    }

    OverlayScrim(onDismiss = dismiss, contentPadding = contentPadding) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .defaultMinSize(minWidth = 200.dp)
                .consumeClicks()
                .focusProperties { onExit = { cancelFocusChange() } },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.add_to_group_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    CloseButton(onClick = dismiss, modifier = Modifier.focusRequester(closeFocusRequester))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.add_to_group_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        itemsIndexed(groups, key = { _, group -> group.id }) { index, group ->
                            GroupListItem(
                                group = group,
                                onClick = {
                                    soundManager?.play(UiSound.CONFIRM)
                                    onGroupSelected(group.id)
                                },
                                modifier = if (index == 0) Modifier.focusRequester(firstGroupFocusRequester) else Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupListItem(group: AppTile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val soundManager = LocalSoundManager.current
    var isFocused by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = {
            Text(
                group.label,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        },
        leadingContent = {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isFocused) 1f else 0.6f)
            )
        },
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) soundManager?.play(UiSound.MOVE)
            }
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        )
    )
}
