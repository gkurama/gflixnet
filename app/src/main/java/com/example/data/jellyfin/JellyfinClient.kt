package com.example.data.jellyfin

import android.util.Log
import com.example.model.MediaItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object JellyfinClient {
    private const val TAG = "JellyfinClient"

    fun getApi(serverUrl: String): JellyfinApi {
        val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(JellyfinApi::class.java)
    }

    fun makeAuthHeader(token: String = ""): String {
        return "MediaBrowser Client=\"Gflixnet\", Device=\"Android\", DeviceId=\"gflixnet_android_dev\", Version=\"1.0.0\", Token=\"$token\""
    }

    suspend fun authenticate(serverUrl: String, username: String, password: String): AuthenticateResponse {
        val api = getApi(serverUrl)
        val authHeader = "MediaBrowser Client=\"Gflixnet\", Device=\"Android\", DeviceId=\"gflixnet_android_dev\", Version=\"1.0.0\", Token=\"\""
        return api.authenticate(authHeader, AuthenticateRequest(username, password))
    }

    suspend fun fetchAndMapItems(
        serverUrl: String,
        userId: String,
        token: String
    ): List<MediaItem> {
        val api = getApi(serverUrl)
        val authHeader = "MediaBrowser Client=\"Gflixnet\", Device=\"Android\", DeviceId=\"gflixnet_android_dev\", Version=\"1.0.0\", Token=\"$token\""
        
        val allMappedItems = mutableListOf<MediaItem>()
        
        try {
            // 1. Fetch user views / libraries
            val viewsResponse = api.getViews(authHeader, userId)
            Log.d(TAG, "Fetched views: ${viewsResponse.items.map { it.name }}")
            
            // 2. For each view, fetch items and map them with libraryName
            for (view in viewsResponse.items) {
                val libraryName = view.name // e.g. "Filmes", "Séries", "Filmes Bíblicos", "Séries Bíblicas", "Música"
                Log.d(TAG, "Fetching folder: $libraryName (Id: ${view.id})")
                val response = api.getItems(authHeader, userId, parentId = view.id)
                
                val mapped = response.items.map { item ->
                    val isSeries = item.type == "Series"
                    val isAudio = item.type == "Audio"

                    val runtimeString = item.runTimeTicks?.let { ticks ->
                        val seconds = ticks / 10000000L
                        val minutes = seconds / 60
                        val hrs = minutes / 60
                        val mins = minutes % 60
                        if (hrs > 0) "${hrs}h ${mins}min" else "${minutes}min"
                    } ?: "N/A"

                    val itemGenres = if (isAudio) {
                        val list = mutableListOf("Música")
                        item.genres?.let { list.addAll(it) }
                        list.joinToString(", ")
                    } else {
                        item.genres?.joinToString(", ") ?: "Geral"
                    }

                    val subtitle = when {
                        isAudio -> "Música"
                        isSeries -> "Série de TV"
                        else -> "Filme"
                    }

                    val artistString = item.artists?.joinToString(", ") ?: ""
                    val synopsisText = when {
                        isAudio -> "Álbum: ${item.album ?: "Desconhecido"}\nArtista: ${artistString}\n${item.overview ?: "Música sincronizada via Jellyfin."}"
                        else -> item.overview ?: ""
                    }

                    MediaItem(
                        id = "jellyfin_${item.id}", // Prefix to avoid any clash
                        title = item.name,
                        year = item.productionYear ?: 2024,
                        runtime = runtimeString,
                        isSeries = isSeries,
                        detailsSubtitle = subtitle,
                        rating = item.officialRating?.ifBlank { "Livre" } ?: "Livre",
                        genres = itemGenres,
                        tags = if (isAudio) "Jellyfin Music, Track" else "Jellyfin, 1080p, Streaming",
                        synopsis = synopsisText,
                        director = if (isAudio) artistString else "Diretor Jellyfin",
                        writers = "Jellyfin Server",
                        language = "Português",
                        awards = "Mídia de Gflixnet",
                        userProgressPercent = 0,
                        userRemainingMinutes = 0,
                        lastWatchedTimestamp = 0L,
                        inWatchlist = false,
                        isFavorite = false,
                        libraryName = libraryName
                    )
                }
                allMappedItems.addAll(mapped)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch views, falling back to all items recursive", e)
            // Fallback to fetch all items at once if views fetch fails
            val response = api.getItems(authHeader, userId)
            val mapped = response.items.map { item ->
                val isSeries = item.type == "Series"
                val isAudio = item.type == "Audio"

                val runtimeString = item.runTimeTicks?.let { ticks ->
                    val seconds = ticks / 10000000L
                    val minutes = seconds / 60
                    val hrs = minutes / 60
                    val mins = minutes % 60
                    if (hrs > 0) "${hrs}h ${mins}min" else "${minutes}min"
                } ?: "N/A"

                val itemGenres = if (isAudio) {
                    val list = mutableListOf("Música")
                    item.genres?.let { list.addAll(it) }
                    list.joinToString(", ")
                } else {
                    item.genres?.joinToString(", ") ?: "Geral"
                }

                val subtitle = when {
                    isAudio -> "Música"
                    isSeries -> "Série de TV"
                    else -> "Filme"
                }

                val artistString = item.artists?.joinToString(", ") ?: ""
                val synopsisText = when {
                    isAudio -> "Álbum: ${item.album ?: "Desconhecido"}\nArtista: ${artistString}\n${item.overview ?: "Música sincronizada via Jellyfin."}"
                    else -> item.overview ?: ""
                }

                MediaItem(
                    id = "jellyfin_${item.id}", // Prefix to avoid any clash
                    title = item.name,
                    year = item.productionYear ?: 2024,
                    runtime = runtimeString,
                    isSeries = isSeries,
                    detailsSubtitle = subtitle,
                    rating = item.officialRating?.ifBlank { "Livre" } ?: "Livre",
                    genres = itemGenres,
                    tags = if (isAudio) "Jellyfin Music, Track" else "Jellyfin, 1080p, Streaming",
                    synopsis = synopsisText,
                    director = if (isAudio) artistString else "Diretor Jellyfin",
                    writers = "Jellyfin Server",
                    language = "Português",
                    awards = "Mídia de Gflixnet",
                    userProgressPercent = 0,
                    userRemainingMinutes = 0,
                    lastWatchedTimestamp = 0L,
                    inWatchlist = false,
                    isFavorite = false,
                    libraryName = ""
                )
            }
            allMappedItems.addAll(mapped)
        }
        
        return allMappedItems
    }
}
