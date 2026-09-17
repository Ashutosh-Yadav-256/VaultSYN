package com.vaultsync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vaultsync.data.database.dao.ConflictDao
import com.vaultsync.data.database.dao.DocumentDao
import com.vaultsync.data.database.dao.SyncOperationDao
import com.vaultsync.data.database.entity.ConflictEntity
import com.vaultsync.data.database.entity.DocumentEntity
import com.vaultsync.data.database.entity.SyncOperationEntity

@Database(
    entities = [
        DocumentEntity::class,
        SyncOperationEntity::class,
        ConflictEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun conflictDao(): ConflictDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vaultsync.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }

        fun createInMemory(context: Context): VaultDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}
