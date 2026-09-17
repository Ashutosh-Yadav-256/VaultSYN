import SwiftUI
import Combine

public struct SettingsState: Sendable {
    public var cipherProtocol: String = "AES-256-GCM"
    public var keyStorage: String = "Apple Keychain (Secure Enclave)"
    public var isWifiOnly: Bool = true
    public var isBackgroundSyncEnabled: Bool = true
    public var totalStorageBytes: Int64 = 0
    public var documentCount: Int = 0

    public init() {}
}

@MainActor
public final class SettingsViewModel: ObservableObject {
    @Published public var state = SettingsState()

    private let documentRepository: any DocumentRepository

    public init(documentRepository: any DocumentRepository) {
        self.documentRepository = documentRepository
        refreshStorage()
    }

    public func refreshStorage() {
        Task {
            if let docs = try? await documentRepository.getDocuments() {
                state.totalStorageBytes = docs.reduce(0) { $0 + $1.size }
                state.documentCount = docs.count
            }
        }
    }

    public func toggleWifiOnly(_ enabled: Bool) {
        state.isWifiOnly = enabled
    }

    public func toggleBackgroundSync(_ enabled: Bool) {
        state.isBackgroundSyncEnabled = enabled
    }
}
