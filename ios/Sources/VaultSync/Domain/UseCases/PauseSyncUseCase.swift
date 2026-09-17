import Foundation

public final class PauseSyncUseCase: Sendable {
    private let syncController: any SyncController

    public init(syncController: any SyncController) {
        self.syncController = syncController
    }

    public func execute() async {
        await syncController.pauseSync()
    }
}

public final class ResumeSyncUseCase: Sendable {
    private let syncController: any SyncController

    public init(syncController: any SyncController) {
        self.syncController = syncController
    }

    public func execute() async throws {
        try await syncController.resumeSync()
    }
}
