# ADR 006: Platform-Compliant Background Processing

## Status
Accepted

## Context
Mobile operating systems (iOS and Android) aggressively terminate background processes to maximize battery life and system responsiveness. Running unmanaged background daemon threads leads to process kills, incomplete transfers, and store review rejections.

## Decision
Use platform-sanctioned background scheduling architectures:
- **Android**: `androidx.work.WorkManager` with `CoroutineWorker` and battery-not-low / network constraints.
- **iOS**: Apple's `BGTaskScheduler` framework registering `BGProcessingTaskRequest` and `BGAppRefreshTaskRequest`.

## Consequences
- **Positive**: Guaranteed battery efficiency and OS compliance.
- **Positive**: Tasks automatically persist and resume across device reboots.
- **Negative**: Background execution timing is subject to OS heuristic scheduling.
