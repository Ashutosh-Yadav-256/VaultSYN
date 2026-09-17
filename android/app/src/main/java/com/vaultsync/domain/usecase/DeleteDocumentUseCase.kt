package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.OperationType
import com.vaultsync.domain.model.SyncOperation
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncRepository
import java.util.UUID

class DeleteDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val syncRepository: SyncRepository,
    private val fileStorage: FileStorage
) {
    suspend operator fun invoke(documentId: String) {
        val document = documentRepository.getDocument(documentId)
            ?: throw AppError.NotFoundError("Document with ID $documentId not found.")

        if (fileStorage.fileExists(document.localPath)) {
            fileStorage.deleteFile(document.localPath)
        }

        documentRepository.deleteDocument(documentId)

        val syncOp = SyncOperation(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            operationType = OperationType.DELETE,
            status = com.vaultsync.domain.model.OperationStatus.PENDING,
            startedAt = System.currentTimeMillis()
        )
        syncRepository.recordOperation(syncOp)
    }
}
