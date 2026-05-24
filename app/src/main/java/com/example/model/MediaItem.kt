package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String,
    val title: String,
    val year: Int,
    val runtime: String,
    val isSeries: Boolean,
    val detailsSubtitle: String,
    val rating: String,
    val genres: String,
    val tags: String, // Comma separated, e.g. "4K HDR, Dolby Atmos"
    val synopsis: String,
    val director: String,
    val writers: String,
    val language: String,
    val awards: String,
    
    // User progress state (for Continue Watching)
    val userProgressPercent: Int = 0, // 0 means not showing in Continue Watching
    val userRemainingMinutes: Int = 0,
    val lastWatchedTimestamp: Long = 0L,
    
    // User lists
    val inWatchlist: Boolean = false,
    val isFavorite: Boolean = false,
    
    val libraryName: String = ""
) : Serializable
