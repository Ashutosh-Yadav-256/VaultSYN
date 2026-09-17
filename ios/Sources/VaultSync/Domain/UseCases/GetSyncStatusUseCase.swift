import Foundation

public final class GetSyncStatusUseCase: Sendable {
    private let documentRepository: any DocumentRepository
    private let syncRepository: any SyncRepository
    private let syncController: any SyncController

    public init(
        documentRepository: any DocumentRepository,
        syncRepository: any SyncRepository,
        syncController: any SyncController
    ) {
        self.documentRepository = documentRepository
        self.syncRepository = syncRepository
        self.syncController = syncController
    }

    public func executeDirect() async throws -> SyncStatusSummary {
        let docs = try await documentRepository.getDocuments()
        let conflicts = try await syncRepository.getConflicts()

        let synced = docs.filter { $0.syncStatus == .synced }.count
        let pending = docs.filter { $0.syncStatus == .pending || $0.syncStatus == .syncing }.count

        return SyncStatusSummary(
            totalDocuments: docs.count,
            syncedCount: synced,
            pendingCount: pending,
            conflictCount: conflicts.count,
            isSyncing: false
        )
    }
}
