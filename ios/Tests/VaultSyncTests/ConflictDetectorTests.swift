import XCTest
@testable import VaultSync

final class ConflictDetectorTests: XCTestCase {

    private var detector: ConflictDetector!

    override func setUp() {
        super.setUp()
        detector = ConflictDetector()
    }

    func testDetectConflictReturnsNilWhenHashesMatch() {
        let doc = Document(
            id: "doc-1",
            name: "report.pdf",
            size: 1024,
            mimeType: "application/pdf",
            localPath: "vault_doc-1.enc",
            sha256: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            createdAt: Date(timeIntervalSince1970: 1000),
            modifiedAt: Date(timeIntervalSince1970: 2000)
        )

        let conflict = detector.detectConflict(
            localDoc: doc,
            remoteHash: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            remoteModifiedAt: Date(timeIntervalSince1970: 2500)
        )

        XCTAssertNil(conflict, "Identical hashes must not trigger a conflict")
    }

    func testDetectConflictReturnsConflictWhenHashesDiverge() {
        let doc = Document(
            id: "doc-1",
            name: "report.pdf",
            size: 1024,
            mimeType: "application/pdf",
            localPath: "vault_doc-1.enc",
            sha256: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            createdAt: Date(timeIntervalSince1970: 1000),
            modifiedAt: Date(timeIntervalSince1970: 2000)
        )

        let remoteHash = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        let conflict = detector.detectConflict(
            localDoc: doc,
            remoteHash: remoteHash,
            remoteModifiedAt: Date(timeIntervalSince1970: 3000)
        )

        XCTAssertNotNil(conflict)
        XCTAssertEqual(conflict?.documentId, "doc-1")
        XCTAssertEqual(conflict?.documentName, "report.pdf")
        XCTAssertEqual(conflict?.localHash, doc.sha256)
        XCTAssertEqual(conflict?.remoteHash, remoteHash)
    }
}
