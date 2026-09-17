# ADR 003: Local-First Offline Storage Model

## Status
Accepted

## Context
Mobile devices experience frequent network transitions, air-gapped environments, and intermittent connectivity. Relying on an always-online cloud backend makes file management brittle and unresponsive.

## Decision
Adopt an **offline-first local-first architecture**:
1. All read and write operations are executed against the local sandbox and local SQLite database (Room / SwiftData).
2. The UI never waits for network responses; it reacts immediately to local database updates.
3. Synchronization is decoupled into a background operation queue that synchronizes opportunistically when target storage or network is available.

## Consequences
- **Positive**: Zero latency on file creation, search, deletion, and local verification.
- **Positive**: App is 100% operational in flight mode or disconnected environments.
- **Negative**: Requires robust conflict detection and resolution logic when syncing across disconnected states.
