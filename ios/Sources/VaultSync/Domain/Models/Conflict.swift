import Foundation

public enum ConflictResolution: String, Codable, Sendable {
    case keepLocal = "KEEP_LOCAL"
    case keepRemote = "KEEP_REMOTE"
    case keepBoth = "KEEP_BOTH"
}

public struct Conflict: Identifiable, Codable, Equatable, Sendable {
    public let id: String
    public let documentId: String
    public let documentName: String
    public let localHash: String
    public let remoteHash: String
    public let localModifiedAt: Date
    public let remoteModifiedAt: Date
    public let detectedAt: Date
    public var resolution: ConflictResolution?

    public init(
        id: String,
        documentId: String,
        documentName: String,
        localHash: String,
        remoteHash: String,
        localModifiedAt: Date,
        remoteModifiedAt: Date,
        detectedAt: Date = Date(),
        resolution: ConflictResolution? = nil
    ) {
        self.id = id
        self.documentId = documentId
        self.documentName = documentName
        self.localHash = localHash
        self.remoteHash = remoteHash
        self.localModifiedAt = localModifiedAt
        self.remoteModifiedAt = remoteModifiedAt
        self.detectedAt = detectedAt
        self.resolution = resolution
    }
}
