import Foundation

public final class ImportDocumentUseCase: Sendable {
    private let documentRepository: any DocumentRepository
    private let syncRepository: any SyncRepository
    private let fileStorage: any FileStorage
    private let cryptoService: any CryptoService

    public init(
        documentRepository: any DocumentRepository,
        syncRepository: any SyncRepository,
        fileStorage: any FileStorage,
        cryptoService: any CryptoService
    ) {
        self.documentRepository = documentRepository
        self.syncRepository = syncRepository
        self.fileStorage = fileStorage
        self.cryptoService = cryptoService
    }

    public func execute(name: String, mimeType: String, bytes: Data) async throws -> Document {
        guard !bytes.isEmpty else {
            throw AppError.storageError("Cannot import empty file: \(name)")
        }

        let sha256 = await cryptoService.calculateSha256(data: bytes)

        if let existing = try await documentRepository.findBySha256(sha256) {
            throw AppError.duplicateError("A document with identical content already exists: \(existing.name)")
        }

        let payload = try await cryptoService.encrypt(data: bytes, keyAlias: cryptoService.defaultKeyAlias)

        var combined = Data()
        var ivLength = UInt8(payload.iv.count)
        combined.append(&ivLength, count: 1)
        combined.append(payload.iv)
        combined.append(payload.ciphertext)

        let docId = UUID().uuidString
        let storagePath = "vault_\(docId).enc"

        _ = try await fileStorage.writeFile(at: storagePath, data: combined)

        let now = Date()
        let document = Document(
            id: docId,
            name: name,
            size: Int64(bytes.count),
            mimeType: mimeType,
            localPath: storagePath,
            sha256: sha256,
            createdAt: now,
            modifiedAt: now,
            syncStatus: .pending
        )

        try await documentRepository.saveDocument(document)

        let syncOp = SyncOperation(
            id: UUID().uuidString,
            documentId: docId,
            operationType: .upload,
            status: .pending,
            startedAt: now
        )
        try await syncRepository.recordOperation(syncOp)

        return document
    }
}
