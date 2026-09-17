package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDocumentsUseCase(
    private val repository: DocumentRepository
) {
    operator fun invoke(query: String = "", filterStatus: SyncStatus? = null): Flow<List<Document>> {
        return repository.observeDocuments().map { list ->
            list.filter { doc ->
                val matchesQuery = query.isBlank() || doc.name.contains(query, ignoreCase = true)
                val matchesStatus = filterStatus == null || doc.syncStatus == filterStatus
                matchesQuery && matchesStatus
            }
        }
    }

    suspend fun executeDirect(): List<Document> {
        return repository.getDocuments()
    }
}
