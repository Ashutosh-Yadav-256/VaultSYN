import Foundation

public final class VerifyIntegrityUseCase: Sendable {
    private let documentRepository: any DocumentRepository
    private let fileStorage: any FileStorage
    private let cryptoService: any CryptoService

    public init(
        documentRepository: any DocumentRepository,
        fileStorage: any FileStorage,
        cryptoService: any CryptoService
    ) {
        self.documentRepository = documentRepository
        self.fileStorage = fileStorage
        self.cryptoService = cryptoService
    }

    public func execute(documentId: String) async throws -> IntegrityResult {
        guard let document = try await documentRepository.getDocument(id: documentId) else {
            throw AppError.notFoundError("Document \(documentId) not found.")
        }

        guard await fileStorage.fileExists(at: document.localPath) else {
            throw AppError.storageError("File does not exist at: \(document.localPath)")
        }

        let rawBytes = try await fileStorage.readFile(at: document.localPath)
        guard rawBytes.count > 1 else {
            return IntegrityResult(
                documentId: documentId,
                isValid: false,
                expectedHash: document.sha256,
                actualHash: "FILE_CORRUPT_OR_EMPTY"
            )
        }

        let ivLength = Int(rawBytes[0])
        guard rawBytes.count > 1 + ivLength else {
            return IntegrityResult(
                documentId: documentId,
                isValid: false,
                expectedHash: document.sha256,
                actualHash: "HEADER_CORRUPTED"
            )
        }

        let iv = rawBytes.subdata(in: 1..<(1 + ivLength))
        let ciphertext = rawBytes.subdata(in: (1 + ivLength)..<rawBytes.count)

        do {
            let decrypted = try await cryptoService.decrypt(
                payload: EncryptedPayload(ciphertext: ciphertext, iv: iv),
                keyAlias: cryptoService.defaultKeyAlias
            )
            let computedHash = await cryptoService.calculateSha256(data: decrypted)
            let isValid = computedHash.caseInsensitiveCompare(document.sha256) == .orderedSame

            return IntegrityResult(
                documentId: documentId,
                isValid: isValid,
                expectedHash: document.sha256,
                actualHash: computedHash
            )
        } catch {
            return IntegrityResult(
                documentId: documentId,
                isValid: false,
                expectedHash: document.sha256,
                actualHash: "DECRYPTION_AUTH_TAG_FAILED"
            )
        }
    }
}
