import Foundation

public final class DeleteDocumentUseCase: Sendable {
    private let documentRepository: any DocumentRepository
    private let syncRepository: any SyncRepository
    private let fileStorage: any FileStorage

    public init(
        documentRepository: any DocumentRepository,
        syncRepository: any SyncRepository,
        fileStorage: any FileStorage
    ) {
        self.documentRepository = documentRepository
        self.syncRepository = syncRepository
        self.fileStorage = fileStorage
    }

    public func execute(documentId: String) async throws {
        guard let document = try await documentRepository.getDocument(id: documentId) else {
            throw AppError.notFoundError("Document \(documentId) not found.")
        }

        if await fileStorage.fileExists(at: document.localPath) {
            _ = try await fileStorage.deleteFile(at: document.localPath)
        }

        try await documentRepository.deleteDocument(id: documentId)

        let syncOp = SyncOperation(
            id: UUID().uuidString,
            documentId: documentId,
            operationType: .delete,
            status: .pending,
            startedAt: Date()
        )
        try await syncRepository.recordOperation(syncOp)
    }
}
