package games.indiegesindel.sproutlauncher.model

data class SteamGridDBGrid(
    val id: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val url: String = "",
    val thumb: String = "",
    val notes: String? = null,
    val author: Author = Author(""),
)

data class Author(
    val name: String
)