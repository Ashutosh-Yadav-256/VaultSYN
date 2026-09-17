package com.vaultsync.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vaultsync.data.database.entity.ConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictDao {
    @Query("SELECT * FROM conflicts WHERE resolution IS NULL ORDER BY detected_at DESC")
    fun observeUnresolved(): Flow<List<ConflictEntity>>

    @Query("SELECT * FROM conflicts WHERE resolution IS NULL ORDER BY detected_at DESC")
    suspend fun getUnresolved(): List<ConflictEntity>

    @Query("SELECT * FROM conflicts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ConflictEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conflict: ConflictEntity)

    @Query("UPDATE conflicts SET resolution = :resolution WHERE id = :id")
    suspend fun updateResolution(id: String, resolution: String)

    @Query("DELETE FROM conflicts WHERE id = :id")
    suspend fun deleteById(id: String)
}
