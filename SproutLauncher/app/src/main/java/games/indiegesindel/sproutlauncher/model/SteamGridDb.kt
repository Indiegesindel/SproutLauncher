package games.indiegesindel.sproutlauncher.model

import com.google.gson.annotations.SerializedName

data class SteamGridDbGame(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("release_date") val releaseDate: Long? = null
)

data class SteamGridDbGrid(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("url") val url: String? = null,
    @SerializedName("thumb") val thumb: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("author") val author: SteamGridDbAuthor? = null
)

data class SteamGridDbAuthor(
    @SerializedName("name") val name: String? = null
)

data class SteamGridDbResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<T>? = null
)
