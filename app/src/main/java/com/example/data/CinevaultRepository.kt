package com.example.data

import com.example.model.MediaItem
import com.example.model.UserSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CinevaultRepository(private val dao: CinevaultDao) {

    val allMediaItems: Flow<List<MediaItem>> = dao.getAllMediaItemsFlow()
    val userSettings: Flow<UserSetting?> = dao.getSettingsFlow()

    suspend fun getMediaItemById(id: String): MediaItem? {
        return dao.getMediaItemById(id)
    }

    fun getMediaItemFlowById(id: String): Flow<MediaItem?> {
        return dao.getMediaItemFlowById(id)
    }

    suspend fun updateMediaItem(item: MediaItem) {
        dao.updateMediaItem(item)
    }

    suspend fun updateSettings(setting: UserSetting) {
        dao.insertSettings(setting)
    }

    suspend fun seedDatabaseIfEmpty() {
        val existingSettings = dao.getSettings()
        if (existingSettings == null) {
            dao.insertSettings(UserSetting())
        }

        // Delete all old media items that are not sync'd from Jellyfin
        dao.deleteNonJellyfinItems()
    }
}
