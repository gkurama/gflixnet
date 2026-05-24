package com.example.data.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticateRequest(
    @Json(name = "Username") val username: String,
    @Json(name = "Pw") val pw: String
)

@JsonClass(generateAdapter = true)
data class JellyfinUser(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String
)

@JsonClass(generateAdapter = true)
data class AuthenticateResponse(
    @Json(name = "AccessToken") val accessToken: String,
    @Json(name = "User") val user: JellyfinUser
)

@JsonClass(generateAdapter = true)
data class JellyfinItemImageTags(
    @Json(name = "Primary") val primary: String? = null
)

@JsonClass(generateAdapter = true)
data class JellyfinItem(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Type") val type: String, // "Movie", "Series", "Audio"
    @Json(name = "ProductionYear") val productionYear: Int? = null,
    @Json(name = "RunTimeTicks") val runTimeTicks: Long? = null,
    @Json(name = "Overview") val overview: String? = null,
    @Json(name = "Genres") val genres: List<String>? = null,
    @Json(name = "OfficialRating") val officialRating: String? = null,
    @Json(name = "ImageTags") val imageTags: JellyfinItemImageTags? = null,
    @Json(name = "Album") val album: String? = null,
    @Json(name = "Artists") val artists: List<String>? = null,
    @Json(name = "IndexNumber") val indexNumber: Int? = null,
    @Json(name = "ParentIndexNumber") val parentIndexNumber: Int? = null
)

@JsonClass(generateAdapter = true)
data class JellyfinItemsResponse(
    @Json(name = "Items") val items: List<JellyfinItem>
)
