package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.MediaItem
import com.example.model.UserSetting

@Database(entities = [MediaItem::class, UserSetting::class], version = 3, exportSchema = false)
abstract class CinevaultDatabase : RoomDatabase() {
    abstract fun dao(): CinevaultDao

    companion object {
        @Volatile
        private var INSTANCE: CinevaultDatabase? = null

        fun getDatabase(context: Context): CinevaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CinevaultDatabase::class.java,
                    "cinevault_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
