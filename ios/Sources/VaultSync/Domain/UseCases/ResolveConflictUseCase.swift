import Foundation

public final class ResolveConflictUseCase: Sendable {
    private let syncRepository: any SyncRepository
    private let documentRepository: any DocumentRepository
    private let fileStorage: any FileStorage

    public init(
        syncRepository: any SyncRepository,
        documentRepository: any DocumentRepository,
        fileStorage: any FileStorage
    ) {
        self.syncRepository = syncRepository
        self.documentRepository = documentRepository
        self.fileStorage = fileStorage
    }

    public func execute(conflictId: String, resolution: ConflictResolution) async throws {
        let conflicts = try await syncRepository.getConflicts()
        guard let conflict = conflicts.first(where: { $0.id == conflictId }) else {
            throw AppError.notFoundError("Conflict \(conflictId) not found")
        }

        guard let document = try await documentRepository.getDocument(id: conflict.documentId) else {
            throw AppError.notFoundError("Document \(conflict.documentId) not found")
        }

        switch resolution {
        case .keepLocal:
            try await documentRepository.updateSyncStatus(id: document.id, status: .synced)

        case .keepRemote:
            var updatedDoc = document
            let newDoc = Document(
                id: updatedDoc.id,
                name: updatedDoc.name,
                size: updatedDoc.size,
                mimeType: updatedDoc.mimeType,
                localPath: updatedDoc.localPath,
                sha256: conflict.remoteHash,
                createdAt: updatedDoc.createdAt,
                modifiedAt: conflict.remoteModifiedAt,
                syncStatus: .synced
            )
            try await documentRepository.saveDocument(newDoc)

        case .keepBoth:
            try await documentRepository.updateSyncStatus(id: document.id, status: .synced)

            let remoteDocId = UUID().uuidString
            let nameWithoutExt = (document.name as NSString).deletingPathExtension
            let ext = (document.name as NSString).pathExtension
            let remoteFilename = "\(nameWithoutExt)_remote_\(Int(conflict.remoteModifiedAt.timeIntervalSince1970)).\(ext)"
            let remoteStoragePath = "vault_\(remoteDocId).enc"

            if await fileStorage.fileExists(at: document.localPath) {
                _ = try await fileStorage.copyFile(from: document.localPath, to: remoteStoragePath)
            }

            let remoteDoc = Document(
                id = remoteDocId,
                name = remoteFilename,
                size = document.size,
                mimeType = document.mimeType,
                localPath = remoteStoragePath,
                sha256 = conflict.remoteHash,
                createdAt: conflict.remoteModifiedAt,
                modifiedAt: conflict.remoteModifiedAt,
                syncStatus: .synced
            )
            try await documentRepository.saveDocument(remoteDoc)
        }

        try await syncRepository.resolveConflict(conflictId: conflictId, resolution: resolution)
    }
}
