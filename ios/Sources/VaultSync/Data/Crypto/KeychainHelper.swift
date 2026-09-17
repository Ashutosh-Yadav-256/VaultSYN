import Foundation
import Security
import CryptoKit

public protocol KeyProvider: Sendable {
    func getOrCreateSymmetricKey(alias: String) throws -> Data
}

public final class KeychainHelper: KeyProvider, @unchecked Sendable {
    public init() {}

    public func getOrCreateSymmetricKey(alias: String) throws -> Data {
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

        let newKey = SymmetricKey(size: .bits256)
        let newKeyData = newKey.withUnsafeBytes { Data($0) }

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

        let key = SymmetricKey(size: .bits256)
        let data = key.withUnsafeBytes { Data($0) }
        keys[alias] = data
        return data
    }
}
