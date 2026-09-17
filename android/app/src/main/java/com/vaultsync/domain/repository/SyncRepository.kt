package com.vaultsync.domain.repository

import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.SyncOperation
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observePendingOperations(): Flow<List<SyncOperation>>
    suspend fun getPendingOperations(): List<SyncOperation>
    suspend fun getRunningOperations(): List<SyncOperation>
    suspend fun recordOperation(operation: SyncOperation)
    suspend fun updateOperationStatus(
        id: String,
        status: OperationStatus,
        errorMessage: String? = null,
        completedAt: Long? = null
    )
    suspend fun incrementRetryCount(id: String)
    suspend fun getConflicts(): List<Conflict>
    fun observeConflicts(): Flow<List<Conflict>>
    suspend fun recordConflict(conflict: Conflict)
    suspend fun resolveConflict(conflictId: String, resolution: ConflictResolution)
}
