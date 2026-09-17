package com.vaultsync.data.sync

import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncRepository

class CrashRecoveryHandler(
    private val syncRepository: SyncRepository,
    private val documentRepository: DocumentRepository,
    private val fileStorage: FileStorage,
    private val cryptoService: CryptoService
) {
    suspend fun recover(): Int {
        val orphanedOps = syncRepository.getRunningOperations()
        var recoveredCount = 0

        for (op in orphanedOps) {
            val doc = documentRepository.getDocument(op.documentId)
            if (doc != null && fileStorage.fileExists(doc.localPath)) {
                try {
                    val fileBytes = fileStorage.readFile(doc.localPath)
                    if (fileBytes.isNotEmpty()) {
                        // Mark document and op as PENDING to retry clean execution
                        syncRepository.updateOperationStatus(
                            id = op.id,
                            status = OperationStatus.PENDING,
                            errorMessage = "Recovered from unexpected shutdown"
                        )
                        documentRepository.updateSyncStatus(doc.id, SyncStatus.PENDING)
                        recoveredCount++
                    } else {
                        syncRepository.updateOperationStatus(
                            id = op.id,
                            status = OperationStatus.FAILED,
                            errorMessage = "Zero-length file after crash",
                            completedAt = System.currentTimeMillis()
                        )
                        documentRepository.updateSyncStatus(doc.id, SyncStatus.FAILED)
                        recoveredCount++
                    }
                } catch (e: Exception) {
                    syncRepository.updateOperationStatus(
                        id = op.id,
                        status = OperationStatus.FAILED,
                        errorMessage = "Recovery failed: ${e.message}",
                        completedAt = System.currentTimeMillis()
                    )
                    recoveredCount++
                }
            } else {
                // Document or file disappeared during crash
                syncRepository.updateOperationStatus(
                    id = op.id,
                    status = OperationStatus.FAILED,
                    errorMessage = "Target file missing after crash",
                    completedAt = System.currentTimeMillis()
                )
                recoveredCount++
            }
        }
        return recoveredCount
    }
}
