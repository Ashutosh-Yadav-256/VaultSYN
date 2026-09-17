import Foundation

public enum OperationType: String, Codable, Sendable {
    case upload = "UPLOAD"
    case download = "DOWNLOAD"
    case delete = "DELETE"
}

public enum OperationStatus: String, Codable, Sendable {
    case pending = "PENDING"
    case running = "RUNNING"
    case completed = "COMPLETED"
    case failed = "FAILED"
}

public struct SyncOperation: Identifiable, Codable, Equatable, Sendable {
    public let id: String
    public let documentId: String
    public let operationType: OperationType
    public var status: OperationStatus
    public var retryCount: Int
    public let startedAt: Date?
    public var completedAt: Date?
    public var errorMessage: String?

    public init(
        id: String,
        documentId: String,
        operationType: OperationType,
        status: OperationStatus = .pending,
        retryCount: Int = 0,
        startedAt: Date? = nil,
        completedAt: Date? = nil,
        errorMessage: String? = nil
    ) {
        self.id = id
        self.documentId = documentId
        self.operationType = operationType
        self.status = status
        self.retryCount = retryCount
        self.startedAt = startedAt
        self.completedAt = completedAt
        self.errorMessage = errorMessage
    }
}
