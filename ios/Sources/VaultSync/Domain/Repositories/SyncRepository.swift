import Foundation

public protocol SyncRepository: Sendable {
    func observePendingOperations() -> AsyncStream<[SyncOperation]>
    func getPendingOperations() async throws -> [SyncOperation]
    func getRunningOperations() async throws -> [SyncOperation]
    func recordOperation(_ operation: SyncOperation) async throws
    func updateOperationStatus(
        id: String,
        status: OperationStatus,
        errorMessage: String?,
        completedAt: Date?
    ) async throws
    func incrementRetryCount(id: String) async throws
    func getConflicts() async throws -> [Conflict]
    func observeConflicts() -> AsyncStream<[Conflict]>
    func recordConflict(_ conflict: Conflict) async throws
    func resolveConflict(conflictId: String, resolution: ConflictResolution) async throws
}
