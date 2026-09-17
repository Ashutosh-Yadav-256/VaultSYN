import Foundation

public final class DetectDuplicatesUseCase: Sendable {
    private let documentRepository: any DocumentRepository

    public init(documentRepository: any DocumentRepository) {
        self.documentRepository = documentRepository
    }

    public func execute(sha256: String) async throws -> Document? {
        try await documentRepository.findBySha256(sha256)
    }
}
