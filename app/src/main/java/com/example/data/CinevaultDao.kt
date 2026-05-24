package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.MediaItem
import com.example.model.UserSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface CinevaultDao {
    @Query("SELECT * FROM user_settings WHERE id = :id_")
    fun getSettingsFlow(id_: String = "current_user"): Flow<UserSetting?>

    @Query("SELECT * FROM user_settings WHERE id = :id_")
    suspend fun getSettings(id_: String = "current_user"): UserSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(setting: UserSetting)

    @Query("SELECT * FROM media_items")
    fun getAllMediaItemsFlow(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: String): MediaItem?

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaItemFlowById(id: String): Flow<MediaItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItem>)

    @Update
    suspend fun updateMediaItem(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id NOT LIKE 'jellyfin_%'")
    suspend fun deleteNonJellyfinItems()
}
