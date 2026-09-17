package com.vaultsync.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vaultsync.data.database.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY started_at ASC")
    fun observePending(): Flow<List<SyncOperationEntity>>

    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY started_at ASC")
    suspend fun getPending(): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE status = 'RUNNING'")
    suspend fun getRunning(): List<SyncOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: SyncOperationEntity)

    @Query("UPDATE sync_operations SET status = :status, error_message = :errorMessage, completed_at = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, errorMessage: String?, completedAt: Long?)

    @Query("UPDATE sync_operations SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun deleteById(id: String)
}
