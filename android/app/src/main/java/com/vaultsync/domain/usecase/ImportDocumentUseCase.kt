package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.OperationType
import com.vaultsync.domain.model.SyncOperation
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncRepository
import java.io.ByteArrayOutputStream
import java.util.UUID

class ImportDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val syncRepository: SyncRepository,
    private val fileStorage: FileStorage,
    private val cryptoService: CryptoService
) {
    suspend operator fun invoke(
        name: String,
        mimeType: String,
        bytes: ByteArray
    ): Document {
        if (bytes.isEmpty()) {
            throw AppError.StorageError("Cannot import empty file: $name")
        }

        val sha256 = cryptoService.calculateSha256(bytes)

        val existing = documentRepository.findBySha256(sha256)
        if (existing != null) {
            throw AppError.DuplicateError("A document with identical content already exists: ${existing.name}")
        }

        val encryptedPayload = cryptoService.encrypt(bytes)

        val combinedOutput = ByteArrayOutputStream()
        combinedOutput.write(encryptedPayload.iv.size)
        combinedOutput.write(encryptedPayload.iv)
        combinedOutput.write(encryptedPayload.ciphertext)
        val encryptedBytes = combinedOutput.toByteArray()

        val docId = UUID.randomUUID().toString()
        val storagePath = "vault_$docId.enc"

        fileStorage.writeFile(storagePath, encryptedBytes)

        val now = System.currentTimeMillis()
        val document = Document(
            id = docId,
            name = name,
            size = bytes.size.toLong(),
            mimeType = mimeType,
            localPath = storagePath,
            sha256 = sha256,
            createdAt = now,
            modifiedAt = now,
            syncStatus = SyncStatus.PENDING
        )

        documentRepository.saveDocument(document)

        val syncOp = SyncOperation(
            id = UUID.randomUUID().toString(),
            documentId = docId,
            operationType = OperationType.UPLOAD,
            status = com.vaultsync.domain.model.OperationStatus.PENDING,
            startedAt = now
        )
        syncRepository.recordOperation(syncOp)

        return document
    }
}
