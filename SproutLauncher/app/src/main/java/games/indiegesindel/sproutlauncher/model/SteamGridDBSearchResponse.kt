package games.indiegesindel.sproutlauncher.model

data class SteamGridDBSearchResponse(
    val success: Boolean = false,
    val data: List<SteamGridDBGame> = emptyList()
)
