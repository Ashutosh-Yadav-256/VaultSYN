# Cross-Platform Architecture Contract

> **Scope**: Strict architectural rules and contract parity standards governing all native implementations (iOS Swift and Android Kotlin) in the VaultSync ecosystem.

---

## The 10 Golden Architecture Rules

### Rule 1: UI Must Never Access Persistence Directly
Views (SwiftUI Views or Jetpack Compose Screens) must only observe state from their respective ViewModel. Direct access to Room, SwiftData, Core Data, SQLite, `UserDefaults`, `SharedPreferences`, or File I/O from presentation components is strictly forbidden.

### Rule 2: Business Logic Belongs Exclusively in Use Cases
ViewModels must remain lean coordinators of state. Complex workflows, file hashing, conflict evaluation, and synchronization steps must be encapsulated in single-purpose, domain-level **Use Cases**.

### Rule 3: Repositories Abstract All Data Sources
The domain layer interacts with data strictly through abstract interfaces (Kotlin `interface` / Swift `protocol`). Repositories conceal whether data is retrieved from local cache, disk storage, hardware security modules, or background queues.

### Rule 4: Platform-Specific APIs Remain Behind Abstractions
Low-level operating system APIs (e.g. `java.security.KeyStore`, `CryptoKit`, `FileManager`, `WorkManager`, `BGTaskScheduler`) must never bleed into the domain layer. They must be wrapped behind domain abstractions like `CryptoService`, `FileStorage`, and `SyncScheduler`.

### Rule 5: Sensitive Data Must Use Platform Secure Storage
Plaintext cryptographic keys, credentials, and master secrets must never be serialized into SQLite, Room, SwiftData, or shared preferences. Keys must be generated and stored inside the **Android Keystore** or **iOS Keychain / Secure Enclave**.

### Rule 6: All Critical Business Logic Requires Deterministic Unit Tests
Use cases, hash calculations, conflict detectors, retry policies, and sync planners must have 100% deterministic unit test coverage, decoupled from OS runtimes via mock interfaces.

### Rule 7: Every Pull Request Must Pass CI Before Merge
Continuous integration pipelines must validate linting, compilation, and automated test execution across both platforms on every pull request. No platform build may be neglected.

### Rule 8: No Secrets or Private Keys in Source Control
No hardcoded passwords, seed vectors, or dummy encryption keys are permitted in repository commits. Dynamic key generation through the platform security provider is mandatory.

### Rule 9: Background Work Must Use Platform-Approved Scheduling APIs
Long-running sync operations or deferred tasks must adhere to OS battery and memory constraints using **Android WorkManager** and **iOS BGTaskScheduler**. Unmanaged background threads or indefinite loops are prohibited.

### Rule 10: Errors Must Be Explicitly Handled and Categorized
Exceptions or unhandled panics are disallowed. System and I/O failures must map to standardized domain errors (`AppError`, `StorageError`, `CryptoError`, `SyncError`) and degrade gracefully with user-actionable feedback.

---

## Platform Conceptual Mapping

| Architectural Concept | Android Native (Kotlin) | iOS Native (Swift) |
| :--- | :--- | :--- |
| **Reactive Stream** | `kotlinx.coroutines.flow.StateFlow<T>` | `Combine.CurrentValueSubject<T>` / `@Observable` |
| **Async Primitive** | `suspend fun` (Kotlin Coroutines) | `async / await` (Swift Concurrency) |
| **Interface Definition** | `interface DocumentRepository` | `protocol DocumentRepository` |
| **ORM / Local Store** | Room (`@Dao`, `@Entity`, `@Database`) | SwiftData (`@Model`, `ModelContainer`) |
| **File Abstraction** | `interface FileStorage` | `protocol FileStorage` |
| **Hardware Key Vault** | `AndroidKeyStore` (`KeyGenParameterSpec`) | Apple Keychain (`kSecClassKey`) |
| **Symmetric Cipher** | `Cipher.getInstance("AES/GCM/NoPadding")` | `CryptoKit.AES.GCM` |
| **Digest Engine** | `MessageDigest.getInstance("SHA-256")` | `CryptoKit.SHA256` |
| **Background Processing** | `androidx.work.WorkManager` (`CoroutineWorker`) | `BGTaskScheduler` (`BGProcessingTaskRequest`) |
| **UI State Representation** | Immutable data classes (`DocumentUiState`) | Immutable structs (`DocumentUIState`) |
| **UI Declaration** | `@Composable fun DocumentListScreen(...)` | `struct DocumentListView: View` |
| **Unit Test Framework** | JUnit 4 + MockK / Truth | XCTest |

---

## Interface Parity Specification

Both platforms must mirror each other's domain contract signatures:

### Document Repository Contract

#### Android (Kotlin)
```kotlin
interface DocumentRepository {
    fun observeDocuments(): Flow<List<Document>>
    suspend fun getDocuments(): List<Document>
    suspend fun getDocument(id: String): Document?
    suspend fun saveDocument(document: Document)
    suspend fun deleteDocument(id: String)
    suspend fun getDocumentsByStatus(status: SyncStatus): List<Document>
}
```

#### iOS (Swift)
```swift
protocol DocumentRepository {
    func observeDocuments() -> AsyncStream<[Document]>
    func getDocuments() async throws -> [Document]
    func getDocument(id: String) async throws -> Document?
    func saveDocument(_ document: Document) async throws
    func deleteDocument(id: String) async throws
    func getDocumentsByStatus(_ status: SyncStatus) async throws -> [Document]
}
```

### Crypto Service Contract

#### Android (Kotlin)
```kotlin
interface CryptoService {
    suspend fun encrypt(data: ByteArray, keyAlias: String): EncryptedPayload
    suspend fun decrypt(payload: EncryptedPayload, keyAlias: String): ByteArray
    suspend fun calculateSha256(data: ByteArray): String
}
```

#### iOS (Swift)
```swift
protocol CryptoService {
    func encrypt(data: Data, keyAlias: String) async throws -> EncryptedPayload
    func decrypt(payload: EncryptedPayload, keyAlias: String) async throws -> Data
    func calculateSha256(data: Data) async -> String
}
```

### File Storage Contract

#### Android (Kotlin)
```kotlin
interface FileStorage {
    suspend fun readFile(path: String): ByteArray
    suspend fun writeFile(path: String, data: ByteArray): String
    suspend fun deleteFile(path: String): Boolean
    suspend fun fileExists(path: String): Boolean
    suspend fun getFileSize(path: String): Long
}
```

#### iOS (Swift)
```swift
protocol FileStorage {
    func readFile(at path: String) async throws -> Data
    func writeFile(at path: String, data: Data) async throws -> String
    func deleteFile(at path: String) async throws -> Bool
    func fileExists(at path: String) async -> Bool
    func getFileSize(at path: String) async throws -> Int64
}
```
