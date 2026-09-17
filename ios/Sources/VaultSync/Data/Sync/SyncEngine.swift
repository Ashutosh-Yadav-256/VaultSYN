import Foundation

public actor SyncEngineState {
    var isSyncing: Bool = false
    var isPaused: Bool = false
    private var continuations: [UUID: AsyncStream<Bool>.Continuation] = [:]

    public func setSyncing(_ syncing: Bool) {
        isSyncing = syncing
        notify()
    }

    public func setPaused(_ paused: Bool) {
        isPaused = paused
    }

    public func observeSyncing() -> AsyncStream<Bool> {
        let id = UUID()
        return AsyncStream { continuation in
            continuation.yield(isSyncing)
            continuations[id] = continuation
            continuation.onTermination = { [weak self] _ in
                Task { [weak self] in
                    await self?.removeContinuation(id: id)
                }
            }
        }
    }

    private func removeContinuation(id: UUID) {
        continuations.removeValue(forKey: id)
    }

    private func notify() {
        for c in continuations.values {
            c.yield(isSyncing)
        }
    }
}

public final class SyncEngine: SyncController, @unchecked Sendable {
    private let documentRepository: any DocumentRepository
    private let syncRepository: any SyncRepository
    private let fileStorage: any FileStorage
    private let cryptoService: any CryptoService
    private let retryPolicy: RetryPolicy
    private let conflictDetector: ConflictDetector
    private let state = SyncEngineState()

    public init(
        documentRepository: any DocumentRepository,
        syncRepository: any SyncRepository,
        fileStorage: any FileStorage,
        cryptoService: any CryptoService,
        retryPolicy: RetryPolicy = RetryPolicy(),
        conflictDetector: ConflictDetector = ConflictDetector()
    ) {
        self.documentRepository = documentRepository
        self.syncRepository = syncRepository
        self.fileStorage = fileStorage
        self.cryptoService = cryptoService
        self.retryPolicy = retryPolicy
        self.conflictDetector = conflictDetector
    }

    public func isSyncingStream() -> AsyncStream<Bool> {
        AsyncStream { continuation in
            Task {
                for await status in await state.observeSyncing() {
                    continuation.yield(status)
                }
            }
        }
    }

    public func startSync() async throws -> SyncReport {
        let alreadySyncing = await state.isSyncing
        guard !alreadySyncing else {
            return SyncReport(totalProcessed: 0, successful: 0, failed: 0, conflictsDetected: 0, durationMs: 0)
        }

        await state.setSyncing(true)
        await state.setPaused(false)
        let startTime = Date()

        defer {
            Task { [state] in
                await state.setSyncing(false)
            }
        }

        let pendingOps = try await syncRepository.getPendingOperations()
        let totalProcessed = pendingOps.count
        var successful = 0
        var failed = 0
        let conflictsDetected = 0

        if pendingOps.isEmpty {
            let pendingDocs = try await documentRepository.getDocumentsByStatus(.pending)
            for doc in pendingDocs {
                try await documentRepository.updateSyncStatus(id: doc.id, status: .synced)
                successful += 1
            }
            let duration = Int64(Date().timeIntervalSince(startTime) * 1000)
            return SyncReport(
                totalProcessed: pendingDocs.count,
                successful: successful,
                failed: 0,
                conflictsDetected: 0,
                durationMs: duration
            )
        }

        let maxConcurrent = 4
        var index = 0

        while index < pendingOps.count {
            if await state.isPaused { break }
            let batch = Array(pendingOps[index..<min(index + maxConcurrent, pendingOps.count)])
            index += maxConcurrent

            await withTaskGroup(of: Bool.self) { group in
                for op in batch {
                    group.addTask { [self] in
                        await processOperationWithRetry(op: op)
                    }
                }

                for await result in group {
                    if result {
                        successful += 1
                    } else {
                        failed += 1
                    }
                }
            }
        }

        let duration = Int64(Date().timeIntervalSince(startTime) * 1000)
        return SyncReport(
            totalProcessed: totalProcessed,
            successful: successful,
            failed: failed,
            conflictsDetected: conflictsDetected,
            durationMs: duration
        )
    }

    private func processOperationWithRetry(op: SyncOperation) async -> Bool {
        var currentAttempt = op.retryCount
        var lastError: String?

        while currentAttempt <= retryPolicy.maxRetries {
            if await state.isPaused { return false }

            do {
                try await syncRepository.updateOperationStatus(
                    id: op.id,
                    status: .running,
                    errorMessage: nil,
                    completedAt: nil
                )

                guard let doc = try await documentRepository.getDocument(id: op.documentId) else {
                    try await syncRepository.updateOperationStatus(
                        id: op.id,
                        status: .completed,
                        errorMessage: nil,
                        completedAt: Date()
                    )
                    return true
                }

                try await documentRepository.updateSyncStatus(id: doc.id, status: .syncing)

                if await fileStorage.fileExists(at: doc.localPath) {
                    let bytes = try await fileStorage.readFile(at: doc.localPath)
                    if !bytes.isEmpty {
                        _ = await cryptoService.calculateSha256(data: bytes)
                        try await syncRepository.updateOperationStatus(
                            id: op.id,
                            status: .completed,
                            errorMessage: nil,
                            completedAt: Date()
                        )
                        try await documentRepository.updateSyncStatus(id: doc.id, status: .synced)
                        return true
                    }
                }
                throw AppError.storageError("Local file missing or empty")
            } catch {
                lastError = error.localizedDescription
                currentAttempt += 1
                try? await syncRepository.incrementRetryCount(id: op.id)

                if retryPolicy.shouldRetry(attempt: currentAttempt) {
                    let delaySeconds = retryPolicy.calculateDelay(attempt: currentAttempt)
                    try? await Task.sleep(nanoseconds: UInt64(delaySeconds * 1_000_000_000))
                } else {
                    break
                }
            }
        }

        try? await syncRepository.updateOperationStatus(
            id: op.id,
            status: .failed,
            errorMessage: lastError,
            completedAt: Date()
        )
        try? await documentRepository.updateSyncStatus(id: op.documentId, status: .failed)
        return false
    }

    public func pauseSync() async {
        await state.setPaused(true)
    }

    public func resumeSync() async throws {
        if await state.isPaused {
            await state.setPaused(false)
            _ = try await startSync()
        }
    }
}
