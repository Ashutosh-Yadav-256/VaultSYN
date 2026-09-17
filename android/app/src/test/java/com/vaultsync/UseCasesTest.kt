package com.vaultsync

import com.vaultsync.data.security.AesGcmCryptoService
import com.vaultsync.data.security.InMemoryKeyProvider
import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.SyncOperation
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncRepository
import com.vaultsync.domain.usecase.DetectDuplicatesUseCase
import com.vaultsync.domain.usecase.ImportDocumentUseCase
import com.vaultsync.domain.usecase.ResolveConflictUseCase
import com.vaultsync.domain.usecase.VerifyIntegrityUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDocumentRepository : DocumentRepository {
    val documents = mutableMapOf<String, Document>()
    val flow = MutableStateFlow<List<Document>>(emptyList())

    override fun observeDocuments(): Flow<List<Document>> = flow

    override suspend fun getDocuments(): List<Document> = documents.values.toList()

    override suspend fun getDocument(id: String): Document? = documents[id]

    override suspend fun saveDocument(document: Document) {
        documents[document.id] = document
        flow.value = documents.values.toList()
    }

    override suspend fun deleteDocument(id: String) {
        documents.remove(id)
        flow.value = documents.values.toList()
    }

    override suspend fun getDocumentsByStatus(status: SyncStatus): List<Document> {
        return documents.values.filter { it.syncStatus == status }
    }

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        documents[id]?.let {
            saveDocument(it.copy(syncStatus = status))
        }
    }

    override suspend fun findBySha256(sha256: String): Document? {
        return documents.values.find { it.sha256.equals(sha256, ignoreCase = true) }
    }
}

class FakeSyncRepository : SyncRepository {
    val operations = mutableListOf<SyncOperation>()
    val conflicts = mutableListOf<Conflict>()
    val conflictsFlow = MutableStateFlow<List<Conflict>>(emptyList())

    override fun observePendingOperations(): Flow<List<SyncOperation>> =
        MutableStateFlow(operations.filter { it.status == OperationStatus.PENDING })

    override suspend fun getPendingOperations(): List<SyncOperation> =
        operations.filter { it.status == OperationStatus.PENDING }

    override suspend fun getRunningOperations(): List<SyncOperation> =
        operations.filter { it.status == OperationStatus.RUNNING }

    override suspend fun recordOperation(operation: SyncOperation) {
        operations.add(operation)
    }

    override suspend fun updateOperationStatus(
        id: String,
        status: OperationStatus,
        errorMessage: String?,
        completedAt: Long?
    ) {
        val index = operations.indexOfFirst { it.id == id }
        if (index >= 0) {
            val op = operations[index]
            operations[index] = op.copy(
                status = status,
                errorMessage = errorMessage,
                completedAt = completedAt
            )
        }
    }

    override suspend fun incrementRetryCount(id: String) {
        val index = operations.indexOfFirst { it.id == id }
        if (index >= 0) {
            val op = operations[index]
            operations[index] = op.copy(retryCount = op.retryCount + 1)
        }
    }

    override suspend fun getConflicts(): List<Conflict> = conflicts.filter { it.resolution == null }

    override fun observeConflicts(): Flow<List<Conflict>> = conflictsFlow

    override suspend fun recordConflict(conflict: Conflict) {
        conflicts.add(conflict)
        conflictsFlow.value = conflicts.filter { it.resolution == null }
    }

    override suspend fun resolveConflict(conflictId: String, resolution: ConflictResolution) {
        val index = conflicts.indexOfFirst { it.id == conflictId }
        if (index >= 0) {
            conflicts[index] = conflicts[index].copy(resolution = resolution)
            conflictsFlow.value = conflicts.filter { it.resolution == null }
        }
    }
}

class FakeFileStorage : FileStorage {
    val files = mutableMapOf<String, ByteArray>()

    override suspend fun readFile(path: String): ByteArray =
        files[path] ?: throw AppError.StorageError("File not found: $path")

    override suspend fun writeFile(path: String, data: ByteArray): String {
        files[path] = data
        return path
    }

    override suspend fun deleteFile(path: String): Boolean = files.remove(path) != null

    override suspend fun fileExists(path: String): Boolean = files.containsKey(path)

    override suspend fun getFileSize(path: String): Long = files[path]?.size?.toLong() ?: 0L

    override suspend fun copyFile(sourcePath: String, destinationPath: String): String {
        val data = readFile(sourcePath)
        writeFile(destinationPath, data)
        return destinationPath
    }
}

class UseCasesTest {

    private lateinit var docRepo: FakeDocumentRepository
    private lateinit var syncRepo: FakeSyncRepository
    private lateinit var storage: FakeFileStorage
    private lateinit var crypto: CryptoService

    private lateinit var importDocUseCase: ImportDocumentUseCase
    private lateinit var verifyIntegrityUseCase: VerifyIntegrityUseCase
    private lateinit var detectDuplicatesUseCase: DetectDuplicatesUseCase
    private lateinit var resolveConflictUseCase: ResolveConflictUseCase

    @Before
    fun setup() {
        docRepo = FakeDocumentRepository()
        syncRepo = FakeSyncRepository()
        storage = FakeFileStorage()
        crypto = AesGcmCryptoService(InMemoryKeyProvider())

        importDocUseCase = ImportDocumentUseCase(docRepo, syncRepo, storage, crypto)
        verifyIntegrityUseCase = VerifyIntegrityUseCase(docRepo, storage, crypto)
        detectDuplicatesUseCase = DetectDuplicatesUseCase(docRepo)
        resolveConflictUseCase = ResolveConflictUseCase(syncRepo, docRepo, storage)
    }

    @Test
    fun `ImportDocumentUseCase stores encrypted bytes and metadata`() {
        runBlocking {
            val sample = "Confidential specification content".toByteArray()
            val doc = importDocUseCase("spec.pdf", "application/pdf", sample)

            assertNotNull(doc.id)
            assertEquals("spec.pdf", doc.name)
            assertTrue(storage.fileExists(doc.localPath))
            assertFalse(storage.readFile(doc.localPath).contentEquals(sample))
            assertEquals(1, syncRepo.operations.size)
            assertEquals(doc.id, syncRepo.operations[0].documentId)
        }
    }

    @Test
    fun `ImportDocumentUseCase prevents duplicates by SHA-256`() {
        runBlocking {
            val sample = "Identical payload".toByteArray()
            importDocUseCase("file1.txt", "text/plain", sample)

            assertThrows(AppError.DuplicateError::class.java) {
                runBlocking {
                    importDocUseCase("file2.txt", "text/plain", sample)
                }
            }
        }
    }

    @Test
    fun `VerifyIntegrityUseCase returns valid true for intact encrypted file`() {
        runBlocking {
            val sample = "Verified payload without corruption".toByteArray()
            val doc = importDocUseCase("intact.pdf", "application/pdf", sample)

            val result = verifyIntegrityUseCase(doc.id)
            assertTrue("Checksum must match when file is intact", result.isValid)
            assertEquals(doc.sha256, result.expectedHash)
            assertEquals(doc.sha256, result.actualHash)
        }
    }

    @Test
    fun `ResolveConflictUseCase applies KEEP_LOCAL correctly`() {
        runBlocking {
            val sample = "Local version content".toByteArray()
            val doc = importDocUseCase("local.pdf", "application/pdf", sample)

            val conflict = Conflict(
                id = "c-1",
                documentId = doc.id,
                documentName = doc.name,
                localHash = doc.sha256,
                remoteHash = "remote_divergent_hash_12345",
                localModifiedAt = doc.modifiedAt,
                remoteModifiedAt = doc.modifiedAt + 5000
            )
            syncRepo.recordConflict(conflict)

            resolveConflictUseCase("c-1", ConflictResolution.KEEP_LOCAL)

            val resolvedDoc = docRepo.getDocument(doc.id)
            assertEquals(SyncStatus.SYNCED, resolvedDoc?.syncStatus)
            assertEquals(0, syncRepo.getConflicts().size)
        }
    }

    @Test
    fun `ResolveConflictUseCase applies KEEP_BOTH creating cloned remote document`() {
        runBlocking {
            val sample = "Local version content".toByteArray()
            val doc = importDocUseCase("design.pdf", "application/pdf", sample)

            val remoteHash = "remote_hash_67890"
            val conflict = Conflict(
                id = "c-2",
                documentId = doc.id,
                documentName = doc.name,
                localHash = doc.sha256,
                remoteHash = remoteHash,
                localModifiedAt = doc.modifiedAt,
                remoteModifiedAt = 99999L
            )
            syncRepo.recordConflict(conflict)

            resolveConflictUseCase("c-2", ConflictResolution.KEEP_BOTH)

            val allDocs = docRepo.getDocuments()
            assertEquals(2, allDocs.size)
            assertTrue(allDocs.any { it.name.contains("_remote_99999") })
            assertEquals(0, syncRepo.getConflicts().size)
        }
    }
}
