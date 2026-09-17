// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "VaultSync",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "VaultSync",
            targets: ["VaultSync"]
        ),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "VaultSync",
            dependencies: [],
            path: "Sources/VaultSync"
        ),
        .testTarget(
            name: "VaultSyncTests",
            dependencies: ["VaultSync"],
            path: "Tests/VaultSyncTests"
        ),
    ]
)
