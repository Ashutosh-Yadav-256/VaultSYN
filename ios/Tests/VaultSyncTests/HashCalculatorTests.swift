import XCTest
@testable import VaultSync

final class HashCalculatorTests: XCTestCase {

    private var cryptoService: CryptoKitService!

    override func setUp() {
        super.setUp()
        cryptoService = CryptoKitService(keyProvider: InMemoryKeyProvider())
    }

    func testCalculateSha256ForEmptyData() async {
        let empty = Data()
        let expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        let actual = await cryptoService.calculateSha256(data: empty)
        XCTAssertEqual(expected, actual.lowercased())
    }

    func testCalculateSha256ForHelloWorld() async {
        let data = "hello world".data(using: .utf8)!
        let expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        let actual = await cryptoService.calculateSha256(data: data)
        XCTAssertEqual(expected, actual.lowercased())
    }

    func testCalculateSha256Determinism() async {
        let payload = "vaultsync_deterministic_data_block".data(using: .utf8)!
        let hash1 = await cryptoService.calculateSha256(data: payload)
        let hash2 = await cryptoService.calculateSha256(data: payload)
        XCTAssertEqual(hash1, hash2)
    }
}
