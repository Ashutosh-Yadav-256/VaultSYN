package com.vaultsync.data.repository

import com.vaultsync.data.database.dao.ConflictDao
import com.vaultsync.data.database.dao.SyncOperationDao
import com.vaultsync.data.database.entity.ConflictEntity
import com.vaultsync.data.database.entity.SyncOperationEntity
import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.SyncOperation
import com.vaultsync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SyncRepositoryImpl(
    private val syncOperationDao: SyncOperationDao,
    private val conflictDao: ConflictDao
) : SyncRepository {

    override fun observePendingOperations(): Flow<List<SyncOperation>> {
        return syncOperationDao.observePending().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPendingOperations(): List<SyncOperation> {
        return syncOperationDao.getPending().map { it.toDomain() }
    }

    override suspend fun getRunningOperations(): List<SyncOperation> {
        return syncOperationDao.getRunning().map { it.toDomain() }
    }

    override suspend fun recordOperation(operation: SyncOperation) {
        syncOperationDao.insert(SyncOperationEntity.fromDomain(operation))
    }

    override suspend fun updateOperationStatus(
        id: String,
        status: OperationStatus,
        errorMessage: String?,
        completedAt: Long?
    ) {
        syncOperationDao.updateStatus(
            id = id,
            status = status.name,
            errorMessage = errorMessage,
            completedAt = completedAt
        )
    }

    override suspend fun incrementRetryCount(id: String) {
        syncOperationDao.incrementRetry(id)
    }

    override suspend fun getConflicts(): List<Conflict> {
        return conflictDao.getUnresolved().map { it.toDomain() }
    }

    override fun observeConflicts(): Flow<List<Conflict>> {
        return conflictDao.observeUnresolved().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun recordConflict(conflict: Conflict) {
        conflictDao.insert(ConflictEntity.fromDomain(conflict))
    }

    override suspend fun resolveConflict(conflictId: String, resolution: ConflictResolution) {
        conflictDao.updateResolution(conflictId, resolution.name)
    }
}
