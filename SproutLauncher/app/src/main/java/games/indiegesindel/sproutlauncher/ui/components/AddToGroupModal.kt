package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.utils.LocalSoundManager
import games.indiegesindel.sproutlauncher.utils.UiSound

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddToGroupModal(
    groups: List<AppTile>,
    onGroupSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    roundness: Int = 16
) {
    val soundManager = LocalSoundManager.current
    val playAndDismiss = {
        soundManager?.play(UiSound.BACK)
        onDismiss()
    }
    val closeFocusRequester = remember { FocusRequester() }
    val firstItemFocusRequester = remember { FocusRequester() }
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(groups) {
        if (groups.isNotEmpty()) {
            firstItemFocusRequester.requestFocus()
        } else {
            closeFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = playAndDismiss
            )
            .focusable(false)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = innerPadding.calculateBottomPadding() + 56.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .defaultMinSize(minWidth = 200.dp)
                .wrapContentHeight()
                .clickable(enabled = false) { }
                .focusable(false)
                .focusProperties { exit = { FocusRequester.Cancel } },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Group",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    var isCloseFocused by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = playAndDismiss,
                        modifier = Modifier
                            .focusRequester(closeFocusRequester)
                            .onFocusChanged {
                                isCloseFocused = it.isFocused
                                if (it.isFocused) soundManager?.play(UiSound.MOVE)
                            }
                            .then(
                                if (isCloseFocused) Modifier.background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ) else Modifier
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isCloseFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No groups found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        itemsIndexed(groups) { index, group ->
                            var isItemFocused by remember { mutableStateOf(false) }
                            ListItem(
                                headlineContent = {
                                    Text(
                                        group.label,
                                        color = if (isItemFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isItemFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .then(if (index == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier)
                                    .onFocusChanged {
                                        isItemFocused = it.isFocused
                                        if (it.isFocused) soundManager?.play(UiSound.MOVE)
                                    }
                                    .clickable {
                                        soundManager?.play(UiSound.CONFIRM)
                                        onGroupSelected(group.id)
                                    },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (isItemFocused) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
