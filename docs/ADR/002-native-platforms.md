# ADR 002: Dual Native Platform Implementations (Swift + Kotlin)

## Status
Accepted

## Context
Cross-platform hybrid frameworks (Flutter, React Native, MAUI) provide code sharing for UI widgets, but introduce abstraction friction when interfacing with hardware-level security (Secure Enclave, StrongBox), high-throughput sandboxed file systems, and platform background scheduling APIs (`BGTaskScheduler`, `WorkManager`).

## Decision
Implement VaultSync as two distinct native projects:
- **iOS**: Swift 5.9+, SwiftUI, SwiftData / CoreData, CryptoKit, Keychain.
- **Android**: Kotlin 1.9+, Jetpack Compose, Room, Android Keystore, WorkManager.

Shared architectural rules are enforced through a common contract document (`CROSS_PLATFORM_ARCHITECTURE.md`) rather than cross-compiled binary dependencies.

## Consequences
- **Positive**: Direct access to hardware cryptographic accelerators and platform capabilities with zero bridge overhead.
- **Positive**: Idiomatic, modern declarative UI on both platforms that adheres to Apple Human Interface Guidelines and Google Material 3.
- **Negative**: Requires maintaining two parallel native codebases.
