import Foundation

public struct IntegrityResult: Codable, Equatable, Sendable {
    public let documentId: String
    public let isValid: BooleanLiteralType
    public let expectedHash: String
    public let actualHash: String
    public let checkedAt: Date

    public init(
        documentId: String,
        isValid: Bool,
        expectedHash: String,
        actualHash: String,
        checkedAt: Date = Date()
    ) {
        self.documentId = documentId
        self.isValid = isValid
        self.expectedHash = expectedHash
        self.actualHash = actualHash
        self.checkedAt = checkedAt
    }
}
