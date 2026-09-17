import SwiftUI
import Combine

@MainActor
public final class ConflictsViewModel: ObservableObject {
    @Published public var conflicts: [Conflict] = []

    private let syncRepository: any SyncRepository
    private let resolveConflictUseCase: ResolveConflictUseCase

    public init(
        syncRepository: any SyncRepository,
        resolveConflictUseCase: ResolveConflictUseCase
    ) {
        self.syncRepository = syncRepository
        self.resolveConflictUseCase = resolveConflictUseCase

        Task {
            await observeConflicts()
        }
    }

    public func observeConflicts() async {
        for await list in syncRepository.observeConflicts() {
            self.conflicts = list
        }
    }

    public func resolve(conflictId: String, resolution: ConflictResolution) {
        Task {
            try? await resolveConflictUseCase.execute(conflictId: conflictId, resolution: resolution)
        }
    }
}
