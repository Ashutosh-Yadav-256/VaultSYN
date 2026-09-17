import XCTest
@testable import VaultSync

final class CryptoServiceTests: XCTestCase {

    private var cryptoService: CryptoKitService!

    override func setUp() {
        super.setUp()
        cryptoService = CryptoKitService(keyProvider: InMemoryKeyProvider())
    }

    func testEncryptAndDecryptRoundtrip() async throws {
        let plain = "Confidential iOS Document".data(using: .utf8)!
        let payload = try await cryptoService.encrypt(data: plain, keyAlias: "test_key")

        XCTAssertNotEqual(plain, payload.ciphertext)

        let decrypted = try await cryptoService.decrypt(payload: payload, keyAlias: "test_key")
        XCTAssertEqual(plain, decrypted)
    }

    func testTamperedCiphertextThrowsError() async throws {
        let plain = "Tamper check payload".data(using: .utf8)!
        let payload = try await cryptoService.encrypt(data: plain, keyAlias: "test_key")

        var tamperedCiphertext = payload.ciphertext
        let lastByte = tamperedCiphertext[tamperedCiphertext.count - 1]
        tamperedCiphertext[tamperedCiphertext.count - 1] = lastByte ^ 0xFF

        let tamperedPayload = EncryptedPayload(ciphertext: tamperedCiphertext, iv: payload.iv)

        do {
            _ = try await cryptoService.decrypt(payload: tamperedPayload, keyAlias: "test_key")
            XCTFail("Decryption should fail when ciphertext is modified")
        } catch {
            XCTAssertTrue(error is AppError)
        }
    }
}
