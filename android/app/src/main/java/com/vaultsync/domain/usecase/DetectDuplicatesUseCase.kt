package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.Document
import com.vaultsync.domain.repository.DocumentRepository

class DetectDuplicatesUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(sha256: String): Document? {
        return documentRepository.findBySha256(sha256)
    }
}
