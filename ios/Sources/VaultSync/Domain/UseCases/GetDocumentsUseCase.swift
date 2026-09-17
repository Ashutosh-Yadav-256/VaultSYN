import Foundation

public final class GetDocumentsUseCase: Sendable {
    private let repository: any DocumentRepository

    public init(repository: any DocumentRepository) {
        self.repository = repository
    }

    public func execute(query: String = "", filterStatus: SyncStatus? = nil) -> AsyncStream<[Document]> {
        let baseStream = repository.observeDocuments()
        return AsyncStream { continuation in
            Task {
                for await documents in baseStream {
                    let filtered = documents.filter { doc in
                        let matchesQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
                            doc.name.localizedCaseInsensitiveContains(query)
                        let matchesStatus = filterStatus == nil || doc.syncStatus == filterStatus
                        return matchesQuery && matchesStatus
                    }
                    continuation.yield(filtered)
                }
                continuation.finish()
            }
        }
    }

    public func executeDirect() async throws -> [Document] {
        try await repository.getDocuments()
    }
}
