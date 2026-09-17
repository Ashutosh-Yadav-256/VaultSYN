import SwiftUI
import Combine

@MainActor
public final class DocumentDetailViewModel: ObservableObject {
    @Published public var document: Document?
    @Published public var integrityResult: IntegrityResult?
    @Published public var isVerifying: Bool = false

    private let documentRepository: any DocumentRepository
    private let verifyIntegrityUseCase: VerifyIntegrityUseCase

    public init(
        documentRepository: any DocumentRepository,
        verifyIntegrityUseCase: VerifyIntegrityUseCase
    ) {
        self.documentRepository = documentRepository
        self.verifyIntegrityUseCase = verifyIntegrityUseCase
    }

    public func loadDocument(id: String) {
        Task {
            document = try? await documentRepository.getDocument(id: id)
            integrityResult = nil
        }
    }

    public func verifyIntegrity(id: String) {
        isVerifying = true
        Task {
            defer { isVerifying = false }
            integrityResult = try? await verifyIntegrityUseCase.execute(documentId: id)
        }
    }
}
