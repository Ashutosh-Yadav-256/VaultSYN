import Foundation

public enum AppError: Error, LocalizedError, Equatable, Sendable {
    case storageError(String)
    case cryptoError(String)
    case syncError(String)
    case integrityError(String)
    case notFoundError(String)
    case duplicateError(String)

    public var errorDescription: String? {
        switch self {
        case .storageError(let msg): return "Storage Error: \(msg)"
        case .cryptoError(let msg): return "Crypto Error: \(msg)"
        case .syncError(let msg): return "Sync Error: \(msg)"
        case .integrityError(let msg): return "Integrity Error: \(msg)"
        case .notFoundError(let msg): return "Not Found: \(msg)"
        case .duplicateError(let msg): return "Duplicate: \(msg)"
        }
    }
}

public struct EncryptedPayload: Equatable, Sendable {
    public let ciphertext: Data
    public let iv: Data

    public init(ciphertext: Data, iv: Data) {
        self.ciphertext = ciphertext
        self.iv = iv
    }
}

public struct SyncReport: Equatable, Sendable {
    public let totalProcessed: Int
    public let successful: Int
    public let failed: Int
    public let conflictsDetected: Int
    public let durationMs: Int64

    public init(totalProcessed: Int, successful: Int, failed: Int, conflictsDetected: Int, durationMs: Int64) {
        self.totalProcessed = totalProcessed
        self.successful = successful
        self.failed = failed
        self.conflictsDetected = conflictsDetected
        self.durationMs = durationMs
    }
}

public struct SyncStatusSummary: Equatable, Sendable {
    public let totalDocuments: Int
    public let syncedCount: Int
    public let pendingCount: Int
    public let conflictCount: Int
    public let isSyncing: Bool

    public init(totalDocuments: Int, syncedCount: Int, pendingCount: Int, conflictCount: Int, isSyncing: Bool = false) {
        self.totalDocuments = totalDocuments
        self.syncedCount = syncedCount
        self.pendingCount = pendingCount
        self.conflictCount = conflictCount
        self.isSyncing = isSyncing
    }
}
