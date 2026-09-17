# ADR 001: Adoption of Clean Architecture + MVVM

## Status
Accepted

## Context
VaultSync requires maintaining consistent engineering standards, long-term maintainability, and code quality across two distinct native mobile platforms (iOS and Android). Without an explicit separation of concerns, mobile codebases frequently suffer from "Massive View Controller" / "Fat View" anti-patterns where presentation, database queries, cryptographic calls, and background sync logic are tangled together.

## Decision
We enforce **Clean Architecture** with three decoupled layers:
1. **Presentation Layer**: Implemented using **MVVM** (SwiftUI / Jetpack Compose with ViewModels emitting immutable state models).
2. **Domain Layer**: Independent core containing business models, abstract repository contracts, and single-purpose Use Cases.
3. **Data Layer**: Concrete infrastructure implementations (Room/SwiftData, Keystore/Keychain, File I/O, Sync Engine).

The dependency direction is strictly:
`Presentation → Domain ← Data`

## Consequences
- **Positive**: Domain logic is 100% testable in pure unit test environments without device simulators or UI test runners.
- **Positive**: Presentation components are completely decoupled from persistence libraries.
- **Negative**: Adds a modest amount of initial boilerplate (interfaces, use cases, mappers).
