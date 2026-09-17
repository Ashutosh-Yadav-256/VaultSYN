import Foundation
import Security

public protocol KeyProvider: Sendable {
    func getOrCreateSymmetricKey(alias: String) throws -> Data
}

public final class KeychainHelper: KeyProvider, @unchecked Sendable {
    public init() {}

    public func getOrCreateSymmetricKey(alias: String) throws -> Data {
        let tag = alias.data(using: .utf8)!

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: alias,
            kSecAttrService as String: "com.vaultsync.keys",
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        if status == errSecSuccess, let keyData = item as? Data {
            return keyData
        }

        var keyBytes = [UInt8](repeating: 0, count: 32)
        let randomStatus = SecRandomCopyBytes(kSecRandomDefault, keyBytes.count, &keyBytes)
        guard randomStatus == errSecSuccess else {
            throw AppError.cryptoError("Failed to generate secure random key bytes")
        }

        let newKeyData = Data(keyBytes)

        let addQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: alias,
            kSecAttrService as String: "com.vaultsync.keys",
            kSecValueData as String: newKeyData,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]

        let addStatus = SecItemAdd(addQuery as CFDictionary, nil)
        guard addStatus == errSecSuccess || addStatus == errSecDuplicateItem else {
            throw AppError.cryptoError("Failed to save key in Keychain: status \(addStatus)")
        }

        return newKeyData
    }
}

public final class InMemoryKeyProvider: KeyProvider, @unchecked Sendable {
    private var keys = [String: Data]()
    private let lock = NSLock()

    public init() {}

    public func getOrCreateSymmetricKey(alias: String) throws -> Data {
        lock.lock()
        defer { lock.unlock() }

        if let existing = keys[alias] {
            return existing
        }

        var keyBytes = [UInt8](repeating: 0, count: 32)
        let status = SecRandomCopyBytes(kSecRandomDefault, keyBytes.count, &keyBytes)
        guard status == errSecSuccess else {
            throw AppError.cryptoError("Random key generation failed")
        }
        let data = Data(keyBytes)
        keys[alias] = data
        return data
    }
}
