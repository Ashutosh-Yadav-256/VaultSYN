import Foundation

public struct FileMetadata: Codable, Equatable, Sendable {
    public let size: Int64
    public let mimeType: String
    public let sha256: String
    public let createdAt: Date
    public let modifiedAt: Date

    public init(size: Int64, mimeType: String, sha256: String, createdAt: Date, modifiedAt: Date) {
        self.size = size
        self.mimeType = mimeType
        self.sha256 = sha256
        self.createdAt = createdAt
        self.modifiedAt = modifiedAt
    }
}

public struct Document: Identifiable, Codable, Equatable, Sendable {
    public let id: String
    public let name: String
    public let size: Int64
    public let mimeType: String
    public let localPath: String
    public let sha256: String
    public let createdAt: Date
    public let modifiedAt: Date
    public var syncStatus: SyncStatus

    public init(
        id: String,
        name: String,
        size: Int64,
        mimeType: String,
        localPath: String,
        sha256: String,
        createdAt: Date,
        modifiedAt: Date,
        syncStatus: SyncStatus = .pending
    ) {
        self.id = id
        self.name = name
        self.size = size
        self.mimeType = mimeType
        self.localPath = localPath
        self.sha256 = sha256
        self.createdAt = createdAt
        self.modifiedAt = modifiedAt
        self.syncStatus = syncStatus
    }
}
