# VaultSync Architecture Blueprint

> **System Architecture**: Clean Architecture + Model-View-ViewModel (MVVM) applied across native iOS and Android platforms.

---

## High-Level Architectural Flow

```text
       ┌─────────────────────────────────────────────────────────┐
       │                   Presentation Layer                    │
       │  ┌───────────────────────┐   ┌───────────────────────┐  │
       │  │    SwiftUI Views      │   │  Jetpack Compose      │  │
       │  │ (State / Environment) │   │ (StateFlow / Recomp)  │  │
       │  └───────────┬───────────┘   └───────────┬───────────┘  │
       │              ▼                           ▼              │
       │  ┌───────────────────────┐   ┌───────────────────────┐  │
       │  │    iOS ViewModels     │   │   Android ViewModels  │  │
       │  │  (ObservableObject)   │   │  (androidx.lifecycle) │  │
       │  └───────────┬───────────┘   └───────────┬───────────┘  │
       └──────────────┼───────────────────────────┼──────────────┘
                      │                           │
                      ▼                           ▼
       ┌─────────────────────────────────────────────────────────┐
       │                      Domain Layer                       │
       │            (Pure Platform-Independent Core)             │
       │                                                         │
       │  ┌───────────────────────────────────────────────────┐  │
       │  │                     Use Cases                     │  │
       │  │  - ImportDocumentUseCase   - VerifyIntegrityUseCase│  │
       │  │  - GetDocumentsUseCase     - StartSyncUseCase      │  │
       │  │  - DeleteDocumentUseCase   - ResolveConflictUseCase│  │
       │  └─────────────────────────┬─────────────────────────┘  │
       │                            ▼                            │
       │  ┌───────────────────────────────────────────────────┐  │
       │  │                 Repository Protocols              │  │
       │  │  - DocumentRepository      - CryptoService        │  │
       │  │  - SyncRepository          - FileStorage          │  │
       │  └─────────────────────────▲─────────────────────────┘  │
       └────────────────────────────┼────────────────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       │                            │        Data Layer          │
       │                            │                            │
       │  ┌─────────────────────────┴─────────────────────────┐  │
       │  │              Repository Implementations           │  │
       │  │  - DocumentRepositoryImpl - SyncRepositoryImpl    │  │
       │  └──────┬──────────────┬──────────────┬──────────────┘  │
       │         │              │              │                 │
       │         ▼              ▼              ▼                 │
       │  ┌──────────────┐┌──────────────┐┌──────────────┐       │
       │  │  Persistence ││ File System  ││  Encryption  │       │
       │  │ SwiftData/Room││ FileManager/ ││CryptoKit/    │       │
       │  │              ││ ScopedStorage││  Keystore    │       │
       │  └──────────────┘└──────────────┘└──────────────┘       │
       └─────────────────────────────────────────────────────────┘
```

---

## Layer Descriptions & Boundaries

### 1. Presentation Layer (UI & ViewModels)
- **Role**: Present domain state to the user and accept touch/navigation inputs.
- **Constraints**:
  - Contains **zero business rules** and **zero data access code**.
  - Renders unidirectionally from an immutable `UiState` model emitted by the ViewModel.
  - ViewModels communicate with the domain layer solely by invoking **Use Cases**.

### 2. Domain Layer (The Pure Core)
- **Role**: Encapsulate all enterprise and application business logic.
- **Components**:
  - **Models**: `Document`, `SyncItem`, `SyncStatus`, `Conflict`, `IntegrityResult`, `SyncOperation`.
  - **Repository Contracts**: Abstract interfaces for persistence, cryptographic functions, and storage.
  - **Use Cases**: Single-responsibility executors orchestrating domain workflows.
- **Constraints**:
  - Depends on **nothing** outside itself.
  - Free from Android/iOS platform framework dependencies (e.g. no UIKit, SwiftUI, Android SDK imports).

### 3. Data Layer (Infrastructure & Persistence)
- **Role**: Concrete implementation of domain repository contracts.
- **Components**:
  - **Local Persistence**: Room (Android) and SwiftData / Core Data (iOS) schema mappings and DAOs.
  - **Secure Storage**: Hardware Keystore (Android) and Keychain / Secure Enclave (iOS).
  - **File Operations**: Scoped app sandbox I/O with atomic file replacement.
  - **Sync Engine**: Durable queue coordinator, conflict detector, retry executor.

---

## Concurrency & Thread Boundaries

| Boundary | Android Implementation | iOS Implementation |
| :--- | :--- | :--- |
| **UI Main Thread** | `Dispatchers.Main.immediate` | `@MainActor` |
| **Database Read/Write** | `Dispatchers.IO` (Room internal pool) | Background `ModelContext` / Actor |
| **Disk File I/O** | `Dispatchers.IO` (bounded to 4 threads) | Custom `DispatchQueue` / `TaskGroup` |
| **Cryptographic Hashing** | `Dispatchers.Default` (CPU bound) | Detached Swift `Task(priority: .userInitiated)` |
| **Background Sync** | `WorkManager` CoroutineWorker | `BGTaskScheduler` background task |
