package com.vaultsync.domain.repository

import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<Document>>
    suspend fun getDocuments(): List<Document>
    suspend fun getDocument(id: String): Document?
    suspend fun saveDocument(document: Document)
    suspend fun deleteDocument(id: String)
    suspend fun getDocumentsByStatus(status: SyncStatus): List<Document>
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
    suspend fun findBySha256(sha256: String): Document?
}
