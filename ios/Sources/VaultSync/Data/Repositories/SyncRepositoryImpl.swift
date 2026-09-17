import Foundation

public actor LocalSyncStore {
    private var operations: [String: SyncOperation] = [:]
    private var conflicts: [String: Conflict] = [:]

    private var opContinuations: [UUID: AsyncStream<[SyncOperation]>.Continuation] = [:]
    private var conflictContinuations: [UUID: AsyncStream<[Conflict]>.Continuation] = [:]

    public init() {}

    public func observePendingOps() -> AsyncStream<[SyncOperation]> {
        let id = UUID()
        return AsyncStream { [weak self] continuation in
            continuation.onTermination = { _ in
                Task { [weak self] in
                    await self?.removeOpContinuation(id: id)
                }
            }
            Task { [weak self] in
                await self?.addOpContinuation(id: id, continuation: continuation)
            }
        }
    }

    private func addOpContinuation(id: UUID, continuation: AsyncStream<[SyncOperation]>.Continuation) {
        opContinuations[id] = continuation
        continuation.yield(getPending())
    }

    private func removeOpContinuation(id: UUID) {
        opContinuations.removeValue(forKey: id)
    }

    public func observeConflicts() -> AsyncStream<[Conflict]> {
        let id = UUID()
        return AsyncStream { [weak self] continuation in
            continuation.onTermination = { _ in
                Task { [weak self] in
                    await self?.removeConflictContinuation(id: id)
                }
            }
            Task { [weak self] in
                await self?.addConflictContinuation(id: id, continuation: continuation)
            }
        }
    }

    private func addConflictContinuation(id: UUID, continuation: AsyncStream<[Conflict]>.Continuation) {
        conflictContinuations[id] = continuation
        continuation.yield(getUnresolvedConflicts())
    }

    private func removeConflictContinuation(id: UUID) {
        conflictContinuations.removeValue(forKey: id)
    }

    private func notify() {
        let pending = getPending()
        for c in opContinuations.values {
            c.yield(pending)
        }
        let unresolved = getUnresolvedConflicts()
        for c in conflictContinuations.values {
            c.yield(unresolved)
        }
    }

    public func getPending() -> [SyncOperation] {
        Array(operations.values.filter { $0.status == .pending })
    }

    public func getRunning() -> [SyncOperation] {
        Array(operations.values.filter { $0.status == .running })
    }

    public func recordOp(_ op: SyncOperation) {
        operations[op.id] = op
        notify()
    }

    public func updateOpStatus(id: String, status: OperationStatus, errorMessage: String?, completedAt: Date?) {
        if var op = operations[id] {
            op.status = status
            op.errorMessage = errorMessage
            op.completedAt = completedAt
            operations[id] = op
            notify()
        }
    }

    public func incrementRetry(id: String) {
        if var op = operations[id] {
            op.retryCount += 1
            operations[id] = op
            notify()
        }
    }

    public func getUnresolvedConflicts() -> [Conflict] {
        Array(conflicts.values.filter { $0.resolution == nil })
    }

    public func recordConflict(_ conflict: Conflict) {
        conflicts[conflict.id] = conflict
        notify()
    }

    public func resolveConflict(id: String, resolution: ConflictResolution) {
        if var c = conflicts[id] {
            c.resolution = resolution
            conflicts[id] = c
            notify()
        }
    }
}

public final class SyncRepositoryImpl: SyncRepository, @unchecked Sendable {
    private let store: LocalSyncStore

    public init(store: LocalSyncStore = LocalSyncStore()) {
        self.store = store
    }

    public func observePendingOperations() -> AsyncStream<[SyncOperation]> {
        AsyncStream { continuation in
            Task {
                for await ops in await store.observePendingOps() {
                    continuation.yield(ops)
                }
            }
        }
    }

    public func getPendingOperations() async throws -> [SyncOperation] {
        await store.getPending()
    }

    public func getRunningOperations() async throws -> [SyncOperation] {
        await store.getRunning()
    }

    public func recordOperation(_ operation: SyncOperation) async throws {
        await store.recordOp(operation)
    }

    public func updateOperationStatus(
        id: String,
        status: OperationStatus,
        errorMessage: String?,
        completedAt: Date?
    ) async throws {
        await store.updateOpStatus(id: id, status: status, errorMessage: errorMessage, completedAt: completedAt)
    }

    public func incrementRetryCount(id: String) async throws {
        await store.incrementRetry(id: id)
    }

    public func getConflicts() async throws -> [Conflict] {
        await store.getUnresolvedConflicts()
    }

    public func observeConflicts() -> AsyncStream<[Conflict]> {
        AsyncStream { continuation in
            Task {
                for await conflicts in await store.observeConflicts() {
                    continuation.yield(conflicts)
                }
            }
        }
    }

    public func recordConflict(_ conflict: Conflict) async throws {
        await store.recordConflict(conflict)
    }

    public func resolveConflict(conflictId: String, resolution: ConflictResolution) async throws {
        await store.resolveConflict(id: conflictId, resolution: resolution)
    }
}
