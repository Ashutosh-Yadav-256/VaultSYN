import Foundation

public protocol SyncController: Sendable {
    func isSyncingStream() -> AsyncStream<Bool>
    func startSync() async throws -> SyncReport
    func pauseSync() async
    func resumeSync() async throws
}
