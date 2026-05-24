package com.example.data.jellyfin

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JellyfinApi {

    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Header("X-Emby-Authorization") authorizationHeader: String,
        @Body request: AuthenticateRequest
    ): AuthenticateResponse

    @GET("Users/{userId}/Views")
    suspend fun getViews(
        @Header("X-Emby-Authorization") authorizationHeader: String,
        @Path("userId") userId: String
    ): JellyfinItemsResponse

    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Header("X-Emby-Authorization") authorizationHeader: String,
        @Path("userId") userId: String,
        @Query("IncludeItemTypes") includeItemTypes: String = "Movie,Series,Audio",
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,BasicSyncInfo,Overview,Genres,ProductionYear,RunTimeTicks,OfficialRating,Studios",
        @Query("ParentId") parentId: String? = null
    ): JellyfinItemsResponse

    @GET("Shows/{seriesId}/Episodes")
    suspend fun getEpisodes(
        @Header("X-Emby-Authorization") authorizationHeader: String,
        @Path("seriesId") seriesId: String,
        @Query("userId") userId: String
    ): JellyfinItemsResponse
}
