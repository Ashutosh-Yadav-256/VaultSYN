# ADR 005: Three-Way Conflict Resolution Policy

## Status
Accepted

## Context
When synchronizing documents between local storage and target destinations in an offline-first architecture, concurrent modifications produce divergent file states. Silent overwrites can lead to catastrophic data loss.

## Decision
1. **Detection**: A conflict is flagged when destination metadata reveals divergent SHA-256 digests for the same relative document identifier.
2. **Quarantine**: The document's state transitions to `CONFLICT`, halting automatic synchronization of that document.
3. **User Resolution**: The user is presented with three deterministic choices:
   - `KEEP_LOCAL`: Local version overwrites remote destination.
   - `KEEP_REMOTE`: Remote version overwrites local version.
   - `KEEP_BOTH`: Remote version is cloned with a timestamped suffix (`document_remote_<timestamp>.ext`), preserving both without loss.

## Consequences
- **Positive**: Zero accidental data loss.
- **Positive**: Simple, transparent mental model for end users.
- **Negative**: Requires explicit user intervention to clear conflict states.
