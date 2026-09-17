import Foundation

public struct ConflictDetector: Sendable {
    public init() {}

    public func detectConflict(
        localDoc: Document,
        remoteHash: String,
        remoteModifiedAt: Date
    ) -> Conflict? {
        if localDoc.sha256.caseInsensitiveCompare(remoteHash) != .orderedSame {
            return Conflict(
                id: UUID().uuidString,
                documentId: localDoc.id,
                documentName: localDoc.name,
                localHash: localDoc.sha256,
                remoteHash: remoteHash,
                localModifiedAt: localDoc.modifiedAt,
                remoteModifiedAt: remoteModifiedAt
            )
        }
        return nil
    }
}
