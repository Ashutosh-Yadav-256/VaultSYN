# VaultSync Testing Strategy & Quality Assurance

> **4-Tier Testing Architecture**: Unit Tests, Integration Tests, UI Automation, and End-to-End Workflows across native iOS and Android codebases.

---

## The Testing Pyramid

```text
               ▲
              / \
             /   \     UI Tests (Compose Test / XCUITest)
            /     \    - Screen rendering, navigation, error dialogs
           /───────\
          /         \   Integration Tests
         /           \  - Room DAO & SwiftData CRUD, File I/O + Crypto
        /─────────────\
       /               \  Unit Tests
      /                 \ - Use Cases, HashCalculator, ConflictDetector,
     /                   \  SyncPlanner, RetryPolicy, State Transitions
    /─────────────────────\
```

---

## Test Tier Definitions

### 1. Unit Tests (Execution Speed: < 100ms per test)
- **Target**: Pure Domain Layer and Use Cases.
- **Rules**:
  - Zero Android SDK or iOS runtime dependencies.
  - Dependencies (Repositories, CryptoService, FileStorage) replaced with in-memory test doubles.
  - Test suites:
    - `HashCalculatorTest`: Validates deterministic SHA-256 output across known test vectors.
    - `ConflictDetectorTest`: Verifies hash comparisons, timestamp ordering, and conflict categorization.
    - `SyncPlannerTest`: Confirms generation of atomic operations from dirty file states.
    - `RetryPolicyTest`: Validates exponential backoff formulas and max retry clamps.
    - `UseCasesTest`: Ensures use case workflows properly invoke repository contracts.

### 2. Integration Tests
- **Target**: Data layer components interacting with SQLite / Room / SwiftData and hardware crypto primitives.
- **Rules**:
  - In-memory SQLite database (`Room.inMemoryDatabaseBuilder`).
  - Temporary sandboxed file directories deleted after test completion.
  - Tests roundtrip encryption and decryption: `plaintext -> encrypt -> store -> read -> decrypt -> verify equals`.

### 3. UI Tests
- **Target**: Jetpack Compose and SwiftUI user interfaces.
- **Rules**:
  - Test UI state rendering from mock ViewModels (e.g. Empty State, Syncing Indicator, Conflict Alert Badge).

### 4. End-to-End Workflows
- **Target**: Full pipeline execution:
  `Select File -> Encrypt at Rest -> Compute SHA-256 -> Sync Execution -> Simulate Conflict -> Resolve Conflict -> Verify Integrity`.
