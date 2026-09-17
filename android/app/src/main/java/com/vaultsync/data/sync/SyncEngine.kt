package com.vaultsync.data.sync

import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.SyncOperation
import com.vaultsync.domain.model.SyncReport
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncController
import com.vaultsync.domain.repository.SyncRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class SyncEngine(
    private val documentRepository: DocumentRepository,
    private val syncRepository: SyncRepository,
    private val fileStorage: FileStorage,
    private val cryptoService: CryptoService,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val conflictDetector: ConflictDetector = ConflictDetector(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncController {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    @Volatile
    private var isPaused: Boolean = false

    private val concurrencyLimiter = Semaphore(MAX_CONCURRENT_OPERATIONS)

    override suspend fun startSync(): SyncReport = withContext(dispatcher) {
        if (_isSyncing.value) {
            return@withContext SyncReport(0, 0, 0, 0, 0L)
        }

        _isSyncing.value = true
        isPaused = false
        val startTime = System.currentTimeMillis()

        var totalProcessed = 0
        var successful = 0
        var failed = 0
        var conflictsDetected = 0

        try {

            val pendingOps = syncRepository.getPendingOperations()
            totalProcessed = pendingOps.size

            if (pendingOps.isEmpty()) {

                val pendingDocs = documentRepository.getDocumentsByStatus(SyncStatus.PENDING)
                for (doc in pendingDocs) {
                    documentRepository.updateSyncStatus(doc.id, SyncStatus.SYNCED)
                    successful++
                }
                return@withContext SyncReport(
                    totalProcessed = pendingDocs.size,
                    successful = successful,
                    failed = 0,
                    conflictsDetected = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            coroutineScope {
                val jobs = pendingOps.map { op ->
                    async {
                        if (isPaused) return@async false
                        concurrencyLimiter.withPermit {
                            processOperationWithRetry(op)
                        }
                    }
                }

                val results = jobs.awaitAll()
                for (res in results) {
                    if (res) successful++ else failed++
                }
            }
        } finally {
            _isSyncing.value = false
        }

        val duration = System.currentTimeMillis() - startTime
        SyncReport(
            totalProcessed = totalProcessed,
            successful = successful,
            failed = failed,
            conflictsDetected = conflictsDetected,
            durationMs = duration
        )
    }

    private suspend fun processOperationWithRetry(op: SyncOperation): Boolean {
        var currentAttempt = op.retryCount
        var lastError: String? = null

        while (currentAttempt <= retryPolicy.maxRetries) {
            if (isPaused) return false

            try {

                syncRepository.updateOperationStatus(
                    id = op.id,
                    status = OperationStatus.RUNNING,
                    errorMessage = null
                )

                val doc = documentRepository.getDocument(op.documentId)
                if (doc == null) {
                    syncRepository.updateOperationStatus(
                        id = op.id,
                        status = OperationStatus.COMPLETED,
                        completedAt = System.currentTimeMillis()
                    )
                    return true
                }

                documentRepository.updateSyncStatus(doc.id, SyncStatus.SYNCING)

                if (fileStorage.fileExists(doc.localPath)) {
                    val bytes = fileStorage.readFile(doc.localPath)
                    if (bytes.isNotEmpty()) {

                        val verifiedHash = cryptoService.calculateSha256(bytes)

                        syncRepository.updateOperationStatus(
                            id = op.id,
                            status = OperationStatus.COMPLETED,
                            completedAt = System.currentTimeMillis()
                        )
                        documentRepository.updateSyncStatus(doc.id, SyncStatus.SYNCED)
                        return true
                    }
                }

                throw IllegalStateException("Local file missing or empty for document ${doc.id}")

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown sync error"
                currentAttempt++
                syncRepository.incrementRetryCount(op.id)

                if (retryPolicy.shouldRetry(currentAttempt)) {
                    val backoffDelay = retryPolicy.calculateDelayMs(currentAttempt)
                    delay(backoffDelay)
                } else {
                    break
                }
            }
        }

        syncRepository.updateOperationStatus(
            id = op.id,
            status = OperationStatus.FAILED,
            errorMessage = lastError,
            completedAt = System.currentTimeMillis()
        )
        val doc = documentRepository.getDocument(op.documentId)
        if (doc != null) {
            documentRepository.updateSyncStatus(doc.id, SyncStatus.FAILED)
        }
        return false
    }

    override suspend fun pauseSync() {
        isPaused = true
    }

    override suspend fun resumeSync() {
        if (isPaused) {
            isPaused = false
            startSync()
        }
    }

    companion object {
        const val MAX_CONCURRENT_OPERATIONS = 4
    }
}
