# VaultSync Synchronization Protocol Specification

> **Offline-First Synchronization Engine**: Protocol state machine, conflict resolution algorithms, crash recovery, and concurrency controls.

---

## 🔁 The 6-Phase Sync Pipeline

The sync engine executes a deterministic 6-phase lifecycle:

```text
  ┌─────────┐
  │  SCAN   │ ──► Traverse local and remote storage indices, extracting metadata.
  └────┬────┘
       ▼
  ┌─────────┐
  │ COMPARE │ ──► Compare SHA-256 digests and timestamps to detect changes/conflicts.
  └────┬────┘
       ▼
  ┌─────────┐
  │  PLAN   │ ──► Construct deterministic list of SyncOperations with dependency order.
  └────┬────┘
       ▼
  ┌─────────┐
  │ EXECUTE │ ──► Dispatch operations through bounded worker pool (max 4 concurrent).
  └────┬────┘
       ▼
  ┌─────────┐
  │ VERIFY  │ ──► Re-compute SHA-256 on target files; validate byte parity.
  └────┬────┘
       ▼
  ┌──────────┐
  │ FINALIZE │ ──► Atomic database update: mark COMPLETED or trigger conflict state.
  └──────────┘
```

---

## ⚡ Conflict Resolution Matrix

When the same file path exists in both local storage and sync destination with divergent SHA-256 hashes:

| Resolution Strategy | Action on Local File | Action on Destination File | Post-State Result |
| :--- | :--- | :--- | :--- |
| **`KEEP_LOCAL`** | Retained as active | Overwritten with local ciphertext | Local state propagated to destination |
| **`KEEP_REMOTE`** | Overwritten with destination version | Retained | Destination state accepted locally |
| **`KEEP_BOTH`** | Retained with original filename | Forked into `filename_remote_timestamp.ext` | Both versions preserved without loss |

---

## 📈 Retry Policy & Exponential Backoff

For transient failures (I/O timeouts, lock contention, network blips), operations undergo bounded exponential backoff:

$$\text{Delay}(n) = \min\left(\text{BaseDelay} \times 2^{n}, \text{MaxDelay}\right) \pm \text{Jitter}$$

* **Base Delay**: 1.0 second
* **Max Delay**: 8.0 seconds
* **Max Retries**: 3 attempts
* **Progression**:
  - Attempt 1: Fail $\rightarrow$ Wait 1.0s
  - Attempt 2: Fail $\rightarrow$ Wait 2.0s
  - Attempt 3: Fail $\rightarrow$ Wait 4.0s
  - Exceeded: Transition status to `FAILED` and notify user via UI.

---

## 💥 Crash Recovery State Machine

The sync operations table acts as a write-ahead durable ledger:

```text
       ┌──────────┐
       │ PENDING  │
       └────┬─────┘
            │ (Worker begins transfer)
            ▼
       ┌──────────┐
  ┌─── │ RUNNING  │ ───┐ (Crash or Power Loss)
  │    └────┬─────┘    │
  │         │          ▼
  │         │    [Cold Start: CrashRecoveryHandler]
  │         │          │
  │         │          ├── SHA-256 matches target? ──► [COMPLETED]
  │         │          │
  │         │          └── Incomplete/Corrupted?   ──► [Clean up & PENDING]
  │         │
  │ (Success) (Fatal error)
  ▼         ▼
┌───────────┐ ┌────────┐
│ COMPLETED │ │ FAILED │
└───────────┘ └────────┘
```

---

## ⚙ Bounded Concurrency Worker Pool

To avoid saturating flash storage I/O bandwidth, draining device battery, and triggering memory pressure warnings on mobile hardware:

* **Maximum Concurrent Workers**: `4`
* **Queue Discipline**: Priority-aware FIFO (user-initiated sync operations take precedence over scheduled background sweeps).
