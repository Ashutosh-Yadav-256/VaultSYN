import Foundation

public protocol CryptoService: Sendable {
    func encrypt(data: Data, keyAlias: String) async throws -> EncryptedPayload
    func decrypt(payload: EncryptedPayload, keyAlias: String) async throws -> Data
    func calculateSha256(data: Data) async -> String
}

public extension CryptoService {
    var defaultKeyAlias: String { "vaultsync_master_key" }
}
