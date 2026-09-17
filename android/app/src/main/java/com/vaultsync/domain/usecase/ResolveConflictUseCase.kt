package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncRepository
import java.util.UUID

class ResolveConflictUseCase(
    private val syncRepository: SyncRepository,
    private val documentRepository: DocumentRepository,
    private val fileStorage: FileStorage
) {
    suspend operator fun invoke(conflictId: String, resolution: ConflictResolution) {
        val conflicts = syncRepository.getConflicts()
        val conflict = conflicts.find { it.id == conflictId }
            ?: throw AppError.NotFoundError("Conflict $conflictId not found")

        val document = documentRepository.getDocument(conflict.documentId)
            ?: throw AppError.NotFoundError("Document ${conflict.documentId} not found")

        when (resolution) {
            ConflictResolution.KEEP_LOCAL -> {
                // Local version is retained; mark document synced
                documentRepository.updateSyncStatus(document.id, SyncStatus.SYNCED)
            }
            ConflictResolution.KEEP_REMOTE -> {
                // Remote version accepted; update document hash to remote
                val updatedDoc = document.copy(
                    sha256 = conflict.remoteHash,
                    modifiedAt = conflict.remoteModifiedAt,
                    syncStatus = SyncStatus.SYNCED
                )
                documentRepository.saveDocument(updatedDoc)
            }
            ConflictResolution.KEEP_BOTH -> {
                // Keep local version
                documentRepository.updateSyncStatus(document.id, SyncStatus.SYNCED)

                // Clone remote copy as a separate document
                val remoteDocId = UUID.randomUUID().toString()
                val remoteFilename = "${document.name.substringBeforeLast(".")}_remote_${conflict.remoteModifiedAt}.${document.name.substringAfterLast(".", "")}"
                val remoteStoragePath = "vault_$remoteDocId.enc"

                // Copy file storage if exists
                if (fileStorage.fileExists(document.localPath)) {
                    fileStorage.copyFile(document.localPath, remoteStoragePath)
                }

                val remoteDoc = Document(
                    id = remoteDocId,
                    name = remoteFilename,
                    size = document.size,
                    mimeType = document.mimeType,
                    localPath = remoteStoragePath,
                    sha256 = conflict.remoteHash,
                    createdAt = conflict.remoteModifiedAt,
                    modifiedAt = conflict.remoteModifiedAt,
                    syncStatus = SyncStatus.SYNCED
                )
                documentRepository.saveDocument(remoteDoc)
            }
        }

        syncRepository.resolveConflict(conflictId, resolution)
    }
}
