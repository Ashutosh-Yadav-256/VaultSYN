import SwiftUI
import Combine

@MainActor
public final class DashboardViewModel: ObservableObject {
    @Published public var summary: SyncStatusSummary = SyncStatusSummary(totalDocuments: 0, syncedCount: 0, pendingCount: 0, conflictCount: 0, isSyncing: false)
    @Published public var lastReport: SyncReport?

    private let getSyncStatusUseCase: GetSyncStatusUseCase
    private let startSyncUseCase: StartSyncUseCase
    private let syncController: any SyncController

    public init(
        getSyncStatusUseCase: GetSyncStatusUseCase,
        startSyncUseCase: StartSyncUseCase,
        syncController: any SyncController
    ) {
        self.getSyncStatusUseCase = getSyncStatusUseCase
        self.startSyncUseCase = startSyncUseCase
        self.syncController = syncController
        Task {
            await refreshSummary()
            await observeSyncState()
        }
    }

    public func refreshSummary() async {
        if let s = try? await getSyncStatusUseCase.executeDirect() {
            summary = s
        }
    }

    private func observeSyncState() async {
        for await isSyncing in syncController.isSyncingStream() {
            summary = SyncStatusSummary(
                totalDocuments: summary.totalDocuments,
                syncedCount: summary.syncedCount,
                pendingCount: summary.pendingCount,
                conflictCount: summary.conflictCount,
                isSyncing: isSyncing
            )
        }
    }

    public func syncNow() {
        Task {
            if let report = try? await startSyncUseCase.execute() {
                lastReport = report
                await refreshSummary()
            }
        }
    }
}
