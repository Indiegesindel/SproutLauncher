package games.indiegesindel.sproutlauncher.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "Unknown release date"

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    return Instant.ofEpochSecond(timestamp) // or ofEpochMilli if needed!
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

data class SteamGridDBGame (
    val id: Int = 0,
    val name: String = "",
    val release_date: Long = 0,
) {
    fun formattedReleaseDate(): String {
        return formatTimestamp(release_date)
    }
}
