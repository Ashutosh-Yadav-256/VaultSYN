import Foundation

public final class CrashRecoveryHandler: Sendable {
    private let syncRepository: any SyncRepository
    private let documentRepository: any DocumentRepository
    private let fileStorage: any FileStorage
    private let cryptoService: any CryptoService

    public init(
        syncRepository: any SyncRepository,
        documentRepository: any DocumentRepository,
        fileStorage: any FileStorage,
        cryptoService: any CryptoService
    ) {
        self.syncRepository = syncRepository
        self.documentRepository = documentRepository
        self.fileStorage = fileStorage
        self.cryptoService = cryptoService
    }

    public func recover() async throws -> Int {
        let runningOps = try await syncRepository.getRunningOperations()
        var recovered = 0

        for op in runningOps {
            if let doc = try await documentRepository.getDocument(id: op.documentId),
               await fileStorage.fileExists(at: doc.localPath) {
                let bytes = try await fileStorage.readFile(at: doc.localPath)
                if !bytes.isEmpty {
                    try await syncRepository.updateOperationStatus(
                        id: op.id,
                        status: .pending,
                        errorMessage: "Recovered from unexpected shutdown",
                        completedAt: nil
                    )
                    try await documentRepository.updateSyncStatus(id: doc.id, status: .pending)
                    recovered += 1
                } else {
                    try await syncRepository.updateOperationStatus(
                        id: op.id,
                        status: .failed,
                        errorMessage: "Zero-length file after crash",
                        completedAt: Date()
                    )
                    try await documentRepository.updateSyncStatus(id: doc.id, status: .failed)
                    recovered += 1
                }
            } else {
                try await syncRepository.updateOperationStatus(
                    id: op.id,
                    status: .failed,
                    errorMessage: "Target file missing after crash",
                    completedAt: Date()
                )
                recovered += 1
            }
        }
        return recovered
    }
}
