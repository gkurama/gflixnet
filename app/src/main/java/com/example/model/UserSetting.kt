package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey val id: String = "current_user",
    val metadataSyncEnabled: Boolean = true,
    val remoteStreamingQuality: String = "4K HDR",
    val audioPassthroughEnabled: Boolean = false,
    val glassmorphismEnabled: Boolean = true,
    val interfaceTheme: String = "Cinematográfico (Escuro)",
    
    // Jellyfin Integration settings
    val jellyfinServerUrl: String = "",
    val jellyfinUsername: String = "",
    val jellyfinToken: String = "",
    val jellyfinUserId: String = "",
    val jellyfinIsConnected: Boolean = false,
    val jellyfinLastSyncTimestamp: Long = 0L
)
