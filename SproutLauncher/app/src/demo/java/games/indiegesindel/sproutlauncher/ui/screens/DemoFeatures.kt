package games.indiegesindel.sproutlauncher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import games.indiegesindel.sproutlauncher.appContainer
import games.indiegesindel.sproutlauncher.model.AppTile
import games.indiegesindel.sproutlauncher.ui.components.SettingsCard
import games.indiegesindel.sproutlauncher.ui.components.SettingsSectionHeader
import java.util.UUID

@Composable
fun DemoFeatures() {
    val context = LocalContext.current

    SettingsSectionHeader(title = "Demo Tools")

    SettingsCard {
        ListItem(
            headlineContent = { Text("Add Shortcut to Random URL") },
            supportingContent = { Text("Creates a tile pointing to a unique https URL") },
            modifier = Modifier.clickable {
                val randomUrl = "https://${UUID.randomUUID()}"
                context.appContainer.tileRepository.add(
                    AppTile(packageName = randomUrl, activityName = "", label = "Demo: ${randomUrl.takeLast(8)}")
                )
                Toast.makeText(context, "Added shortcut to $randomUrl", Toast.LENGTH_SHORT).show()
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
