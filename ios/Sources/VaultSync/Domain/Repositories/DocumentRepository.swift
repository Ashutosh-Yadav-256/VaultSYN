import Foundation

public protocol DocumentRepository: Sendable {
    func observeDocuments() -> AsyncStream<[Document]>
    func getDocuments() async throws -> [Document]
    func getDocument(id: String) async throws -> Document?
    func saveDocument(_ document: Document) async throws
    func deleteDocument(id: String) async throws
    func getDocumentsByStatus(_ status: SyncStatus) async throws -> [Document]
    func updateSyncStatus(id: String, status: SyncStatus) async throws
    func findBySha256(_ sha256: String) async throws -> Document?
}
