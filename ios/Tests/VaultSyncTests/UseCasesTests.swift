import XCTest
@testable import VaultSync

final class UseCasesTests: XCTestCase {

    private var docRepo: DocumentRepositoryImpl!
    private var syncRepo: SyncRepositoryImpl!
    private var fileStorage: FileManagerStorage!
    private var crypto: CryptoKitService!

    private var importUseCase: ImportDocumentUseCase!
    private var verifyUseCase: VerifyIntegrityUseCase!
    private var duplicatesUseCase: DetectDuplicatesUseCase!
    private var resolveUseCase: ResolveConflictUseCase!

    private var tempDirectory: URL!

    override func setUp() {
        super.setUp()
        tempDirectory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString, isDirectory: true)
        try? FileManager.default.createDirectory(at: tempDirectory, withIntermediateDirectories: true)

        docRepo = DocumentRepositoryImpl()
        syncRepo = SyncRepositoryImpl()
        fileStorage = FileManagerStorage(rootDirectory: tempDirectory)
        crypto = CryptoKitService(keyProvider: InMemoryKeyProvider())

        importUseCase = ImportDocumentUseCase(
            documentRepository: docRepo,
            syncRepository: syncRepo,
            fileStorage: fileStorage,
            cryptoService: crypto
        )
        verifyUseCase = VerifyIntegrityUseCase(
            documentRepository: docRepo,
            fileStorage: fileStorage,
            cryptoService: crypto
        )
        duplicatesUseCase = DetectDuplicatesUseCase(documentRepository: docRepo)
        resolveUseCase = ResolveConflictUseCase(
            syncRepository: syncRepo,
            documentRepository: docRepo,
            fileStorage: fileStorage
        )
    }

    override func tearDown() {
        try? FileManager.default.removeItem(at: tempDirectory)
        super.tearDown()
    }

    func testImportDocumentStoresEncryptedFileAndMetadata() async throws {
        let sample = "VaultSync confidential specification on iOS".data(using: .utf8)!
        let doc = try await importUseCase.execute(name: "spec.pdf", mimeType: "application/pdf", bytes: sample)

        XCTAssertFalse(doc.id.isEmpty)
        XCTAssertEqual(doc.name, "spec.pdf")
        let exists = await fileStorage.fileExists(at: doc.localPath)
        XCTAssertTrue(exists)

        let storedBytes = try await fileStorage.readFile(at: doc.localPath)
        XCTAssertNotEqual(storedBytes, sample)

        let ops = try await syncRepo.getPendingOperations()
        XCTAssertEqual(ops.count, 1)
        XCTAssertEqual(ops.first?.documentId, doc.id)
    }

    func testImportDuplicateThrowsDuplicateError() async throws {
        let sample = "Identical payload on iOS".data(using: .utf8)!
        _ = try await importUseCase.execute(name: "file1.txt", mimeType: "text/plain", bytes: sample)

        do {
            _ = try await importUseCase.execute(name: "file2.txt", mimeType: "text/plain", bytes: sample)
            XCTFail("Should throw duplicate error")
        } catch {
            XCTAssertTrue(error is AppError)
        }
    }

    func testVerifyIntegrityReturnsValidForCleanFile() async throws {
        let sample = "Intact file payload".data(using: .utf8)!
        let doc = try await importUseCase.execute(name: "intact.pdf", mimeType: "application/pdf", bytes: sample)

        let result = try await verifyUseCase.execute(documentId: doc.id)
        XCTAssertTrue(result.isValid)
        XCTAssertEqual(result.expectedHash, doc.sha256)
        XCTAssertEqual(result.actualHash, doc.sha256)
    }

    func testResolveConflictKeepLocal() async throws {
        let sample = "Local file".data(using: .utf8)!
        let doc = try await importUseCase.execute(name: "local.pdf", mimeType: "application/pdf", bytes: sample)

        let conflict = Conflict(
            id: "c-1",
            documentId: doc.id,
            documentName: doc.name,
            localHash: doc.sha256,
            remoteHash: "remote_divergent_hash",
            localModifiedAt: doc.modifiedAt,
            remoteModifiedAt: doc.modifiedAt.addingTimeInterval(10)
        )
        try await syncRepo.recordConflict(conflict)

        try await resolveUseCase.execute(conflictId: "c-1", resolution: .keepLocal)

        let resolved = try await docRepo.getDocument(id: doc.id)
        XCTAssertEqual(resolved?.syncStatus, .synced)
        let unresolved = try await syncRepo.getConflicts()
        XCTAssertEqual(unresolved.count, 0)
    }

    func testResolveConflictKeepBothCreatesClonedDocument() async throws {
        let sample = "Original design".data(using: .utf8)!
        let doc = try await importUseCase.execute(name: "design.pdf", mimeType: "application/pdf", bytes: sample)

        let conflict = Conflict(
            id: "c-2",
            documentId: doc.id,
            documentName: doc.name,
            localHash: doc.sha256,
            remoteHash: "remote_cloned_hash",
            localModifiedAt: doc.modifiedAt,
            remoteModifiedAt: Date(timeIntervalSince1970: 55555)
        )
        try await syncRepo.recordConflict(conflict)

        try await resolveUseCase.execute(conflictId: "c-2", resolution: .keepBoth)

        let allDocs = try await docRepo.getDocuments()
        XCTAssertEqual(allDocs.count, 2)
        XCTAssertTrue(allDocs.contains { $0.name.contains("_remote_55555") })
        let unresolved = try await syncRepo.getConflicts()
        XCTAssertEqual(unresolved.count, 0)
    }
}
