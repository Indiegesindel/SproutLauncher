package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Clock()
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.BatteryFull, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "67%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun Clock() {
    var time by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            time = SimpleDateFormat("h:mma", Locale.getDefault()).format(cal.time).lowercase()
            val secondsUntilNextMinute = 60 - cal.get(Calendar.SECOND)
            delay(secondsUntilNextMinute * 1000L)
        }
    }
    Text(
        text = time,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}
