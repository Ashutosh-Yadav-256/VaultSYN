import Foundation
import CryptoKit

public final class CryptoKitService: CryptoService, @unchecked Sendable {
    private let keyProvider: any KeyProvider

    public init(keyProvider: any KeyProvider = KeychainHelper()) {
        self.keyProvider = keyProvider
    }

    public func encrypt(data: Data, keyAlias: String) async throws -> EncryptedPayload {
        do {
            let rawKey = try keyProvider.getOrCreateSymmetricKey(alias: keyAlias)
            let symmetricKey = SymmetricKey(data: rawKey)

            let sealedBox = try AES.GCM.seal(data, using: symmetricKey)

            let nonceData = Data(sealedBox.nonce)

            let combinedCiphertext = sealedBox.ciphertext + sealedBox.tag

            return EncryptedPayload(ciphertext: combinedCiphertext, iv: nonceData)
        } catch {
            throw AppError.cryptoError("Encryption failed: \(error.localizedDescription)")
        }
    }

    public func decrypt(payload: EncryptedPayload, keyAlias: String) async throws -> Data {
        do {
            let rawKey = try keyProvider.getOrCreateSymmetricKey(alias: keyAlias)
            let symmetricKey = SymmetricKey(data: rawKey)

            guard payload.iv.count == 12, payload.ciphertext.count >= 16 else {
                throw AppError.cryptoError("Invalid payload dimensions for AES-GCM")
            }

            let combined = payload.iv + payload.ciphertext
            let sealedBox = try AES.GCM.SealedBox(combined: combined)
            let decryptedData = try AES.GCM.open(sealedBox, using: symmetricKey)

            return decryptedData
        } catch {
            throw AppError.cryptoError("Decryption failed (tampering or key mismatch): \(error.localizedDescription)")
        }
    }

    public func calculateSha256(data: Data) async -> String {
        let digest = SHA256.hash(data: data)
        return digest.compactMap { String(format: "%02x", $0) }.joined()
    }
}
