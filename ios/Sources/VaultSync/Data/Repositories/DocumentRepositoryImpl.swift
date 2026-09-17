import Foundation

public actor LocalDocumentStore {
    private var documents: [String: Document] = [:]
    private var continuations: [UUID: AsyncStream<[Document]>.Continuation] = [:]

    public init() {}

    public func observe() -> AsyncStream<[Document]> {
        let id = UUID()
        return AsyncStream { [weak self] continuation in
            continuation.onTermination = { _ in
                Task { [weak self] in
                    await self?.removeContinuation(id: id)
                }
            }
            Task { [weak self] in
                await self?.addContinuation(id: id, continuation: continuation)
            }
        }
    }

    private func addContinuation(id: UUID, continuation: AsyncStream<[Document]>.Continuation) {
        continuations[id] = continuation
        continuation.yield(Array(documents.values.sorted { $0.modifiedAt > $1.modifiedAt }))
    }

    private func removeContinuation(id: UUID) {
        continuations.removeValue(forKey: id)
    }

    private func notify() {
        let current = Array(documents.values.sorted { $0.modifiedAt > $1.modifiedAt })
        for continuation in continuations.values {
            continuation.yield(current)
        }
    }

    public func getAll() -> [Document] {
        Array(documents.values.sorted { $0.modifiedAt > $1.modifiedAt })
    }

    public func get(id: String) -> Document? {
        documents[id]
    }

    public func save(_ document: Document) {
        documents[document.id] = document
        notify()
    }

    public func delete(id: String) {
        documents.removeValue(forKey: id)
        notify()
    }

    public func getByStatus(_ status: SyncStatus) -> [Document] {
        documents.values.filter { $0.syncStatus == status }
    }

    public func updateStatus(id: String, status: SyncStatus) {
        if var doc = documents[id] {
            doc.syncStatus = status
            documents[id] = doc
            notify()
        }
    }

    public func findBySha256(_ sha256: String) -> Document? {
        documents.values.first { $0.sha256.caseInsensitiveCompare(sha256) == .orderedSame }
    }
}

public final class DocumentRepositoryImpl: DocumentRepository, @unchecked Sendable {
    private let store: LocalDocumentStore

    public init(store: LocalDocumentStore = LocalDocumentStore()) {
        self.store = store
    }

    public func observeDocuments() -> AsyncStream<[Document]> {
        AsyncStream { continuation in
            Task {
                for await docs in await store.observe() {
                    continuation.yield(docs)
                }
            }
        }
    }

    public func getDocuments() async throws -> [Document] {
        await store.getAll()
    }

    public func getDocument(id: String) async throws -> Document? {
        await store.get(id: id)
    }

    public func saveDocument(_ document: Document) async throws {
        await store.save(document)
    }

    public func deleteDocument(id: String) async throws {
        await store.delete(id: id)
    }

    public func getDocumentsByStatus(_ status: SyncStatus) async throws -> [Document] {
        await store.getByStatus(status)
    }

    public func updateSyncStatus(id: String, status: SyncStatus) async throws {
        await store.updateStatus(id: id, status: status)
    }

    public func findBySha256(_ sha256: String) async throws -> Document? {
        await store.findBySha256(sha256)
    }
}
