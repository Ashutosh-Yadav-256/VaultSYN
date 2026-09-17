import Foundation

public enum SyncStatus: String, Codable, CaseIterable, Sendable {
    case synced = "SYNCED"
    case pending = "PENDING"
    case syncing = "SYNCING"
    case conflict = "CONFLICT"
    case failed = "FAILED"
}
