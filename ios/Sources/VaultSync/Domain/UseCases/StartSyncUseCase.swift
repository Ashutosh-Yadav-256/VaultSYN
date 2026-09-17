import Foundation

public final class StartSyncUseCase: Sendable {
    private let syncController: any SyncController

    public init(syncController: any SyncController) {
        self.syncController = syncController
    }

    public func execute() async throws -> SyncReport {
        try await syncController.startSync()
    }
}
