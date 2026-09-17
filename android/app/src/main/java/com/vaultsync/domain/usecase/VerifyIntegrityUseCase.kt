package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.EncryptedPayload
import com.vaultsync.domain.model.IntegrityResult
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import java.io.ByteArrayInputStream

class VerifyIntegrityUseCase(
    private val documentRepository: DocumentRepository,
    private val fileStorage: FileStorage,
    private val cryptoService: CryptoService
) {
    suspend operator fun invoke(documentId: String): IntegrityResult {
        val document = documentRepository.getDocument(documentId)
            ?: throw AppError.NotFoundError("Document $documentId not found.")

        if (!fileStorage.fileExists(document.localPath)) {
            throw AppError.StorageError("Ciphertext file missing at ${document.localPath}")
        }

        val rawFileBytes = fileStorage.readFile(document.localPath)
        if (rawFileBytes.isEmpty()) {
            return IntegrityResult(
                documentId = documentId,
                isValid = false,
                expectedHash = document.sha256,
                actualHash = "EMPTY_FILE"
            )
        }

        // Unpack IV and Ciphertext
        val inputStream = ByteArrayInputStream(rawFileBytes)
        val ivLength = inputStream.read()
        if (ivLength <= 0 || ivLength > rawFileBytes.size) {
            return IntegrityResult(
                documentId = documentId,
                isValid = false,
                expectedHash = document.sha256,
                actualHash = "CORRUPTED_HEADER"
            )
        }
        val iv = ByteArray(ivLength)
        inputStream.read(iv)
        val ciphertext = inputStream.readBytes()

        // Decrypt and re-compute plaintext SHA-256
        val decryptedBytes = try {
            cryptoService.decrypt(EncryptedPayload(ciphertext = ciphertext, iv = iv))
        } catch (e: Exception) {
            return IntegrityResult(
                documentId = documentId,
                isValid = false,
                expectedHash = document.sha256,
                actualHash = "DECRYPTION_AUTH_TAG_FAILED"
            )
        }

        val computedHash = cryptoService.calculateSha256(decryptedBytes)
        val isValid = computedHash.equals(document.sha256, ignoreCase = true)

        return IntegrityResult(
            documentId = documentId,
            isValid = isValid,
            expectedHash = document.sha256,
            actualHash = computedHash
        )
    }
}
