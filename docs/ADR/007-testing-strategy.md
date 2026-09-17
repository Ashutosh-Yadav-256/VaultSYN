# ADR 007: Four-Tier Testing Strategy

## Status
Accepted

## Context
High code quality and robust verification across two independent native codebases require a structured testing strategy. Without clear boundaries, developers either under-test or rely excessively on slow, brittle UI tests.

## Decision
Adopt a 4-tier testing hierarchy:
1. **Unit Tests (80% of test count)**: Focus on pure business logic (Domain models, Use Cases, HashCalculator, ConflictDetector, SyncPlanner, RetryPolicy). Must run in milliseconds with zero platform simulator overhead.
2. **Integration Tests (15% of test count)**: Validate Room / SwiftData schemas, hardware cryptographic round-trips, and file I/O sandbox behavior.
3. **UI Tests (5% of test count)**: Validate UI states (Dashboard, Document List, Detail, Conflicts, Settings) in Compose and SwiftUI.
4. **End-to-End Workflows**: Validate full flow from file import through encryption, hashing, and conflict resolution.

## Consequences
- **Positive**: Extremely fast CI pipeline runs (< 2 minutes).
- **Positive**: Immediate pinpointing of regressions in core business logic.
- **Negative**: Requires maintaining test doubles (mocks/stubs) for data source interfaces.
