package games.indiegesindel.sproutlauncher.model

data class SteamGridDBGridsResponse(
    val success: Boolean = false,
    val page: Int = 0,
    val total: Int = 0,
    val limit: Int = 0,
    val data: List<SteamGridDBGrid> = emptyList()
)
