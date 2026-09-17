package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.model.SyncStatusSummary
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.SyncController
import com.vaultsync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSyncStatusUseCase(
    private val documentRepository: DocumentRepository,
    private val syncRepository: SyncRepository,
    private val syncController: SyncController
) {
    operator fun invoke(): Flow<SyncStatusSummary> {
        return combine(
            documentRepository.observeDocuments(),
            syncRepository.observeConflicts(),
            syncController.isSyncing
        ) { docs, conflicts, syncing ->
            val synced = docs.count { it.syncStatus == SyncStatus.SYNCED }
            val pending = docs.count { it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.SYNCING }
            SyncStatusSummary(
                totalDocuments = docs.size,
                syncedCount = synced,
                pendingCount = pending,
                conflictCount = conflicts.size,
                isSyncing = syncing
            )
        }
    }

    suspend fun executeDirect(): SyncStatusSummary {
        val docs = documentRepository.getDocuments()
        val conflicts = syncRepository.getConflicts()
        val synced = docs.count { it.syncStatus == SyncStatus.SYNCED }
        val pending = docs.count { it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.SYNCING }
        return SyncStatusSummary(
            totalDocuments = docs.size,
            syncedCount = synced,
            pendingCount = pending,
            conflictCount = conflicts.size,
            isSyncing = syncController.isSyncing.value
        )
    }
}
