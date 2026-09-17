package com.vaultsync.data.repository

import com.vaultsync.data.database.dao.DocumentDao
import com.vaultsync.data.database.entity.DocumentEntity
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun observeDocuments(): Flow<List<Document>> {
        return documentDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDocuments(): List<Document> {
        return documentDao.getAll().map { it.toDomain() }
    }

    override suspend fun getDocument(id: String): Document? {
        return documentDao.getById(id)?.toDomain()
    }

    override suspend fun saveDocument(document: Document) {
        documentDao.insertOrUpdate(DocumentEntity.fromDomain(document))
    }

    override suspend fun deleteDocument(id: String) {
        documentDao.deleteById(id)
    }

    override suspend fun getDocumentsByStatus(status: SyncStatus): List<Document> {
        return documentDao.getByStatus(status.name).map { it.toDomain() }
    }

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        documentDao.updateSyncStatus(id, status.name)
    }

    override suspend fun findBySha256(sha256: String): Document? {
        return documentDao.getBySha256(sha256)?.toDomain()
    }
}
