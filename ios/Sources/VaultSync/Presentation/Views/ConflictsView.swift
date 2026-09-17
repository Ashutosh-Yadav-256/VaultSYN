import SwiftUI

public struct ConflictsView: View {
    @ObservedObject public var viewModel: ConflictsViewModel

    public init(viewModel: ConflictsViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Conflicts")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Text("Divergent file versions detected during sync")
                    .font(.system(size: 14))
                    .foregroundColor(VaultTheme.slate400)
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)

            if viewModel.conflicts.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 52))
                        .foregroundColor(VaultTheme.verifiedEmerald)
                    Text("Zero Conflicts Detected")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                    Text("All local and destination files match SHA-256 digests")
                        .font(.system(size: 13))
                        .foregroundColor(VaultTheme.slate400)
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else {
                List {
                    ForEach(viewModel.conflicts) { conflict in
                        ConflictCardView(
                            conflict: conflict,
                            onKeepLocal: { viewModel.resolve(conflictId: conflict.id, resolution: .keepLocal) },
                            onKeepRemote: { viewModel.resolve(conflictId: conflict.id, resolution: .keepRemote) },
                            onKeepBoth: { viewModel.resolve(conflictId: conflict.id, resolution: .keepBoth) }
                        )
                        .listRowBackground(Color.clear)
                        .listRowInsets(EdgeInsets(top: 6, leading: 20, bottom: 6, trailing: 20))
                        .listRowSeparator(.hidden)
                    }
                }
                .listStyle(.plain)
                .background(Color.clear)
            }
        }
        .background(VaultTheme.slate950.ignoresSafeArea())
    }
}

struct ConflictCardView: View {
    let conflict: Conflict
    let onKeepLocal: () -> Void
    let onKeepRemote: () -> Void
    let onKeepBoth: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundColor(VaultTheme.errorRose)
                Text(conflict.documentName)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
            }

            HStack(spacing: 10) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Local Version")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(VaultTheme.accentCyan)
                    Text("SHA: \(String(conflict.localHash.prefix(10)))...")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundColor(VaultTheme.slate400)
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(VaultTheme.slate800)
                .cornerRadius(8)

                VStack(alignment: .leading, spacing: 4) {
                    Text("Remote Version")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(VaultTheme.errorRose)
                    Text("SHA: \(String(conflict.remoteHash.prefix(10)))...")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundColor(VaultTheme.slate400)
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(VaultTheme.slate800)
                .cornerRadius(8)
            }

            HStack(spacing: 8) {
                Button(action: onKeepLocal) {
                    Text("Keep Local")
                        .font(.system(size: 12, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(VaultTheme.slate800)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                Button(action: onKeepRemote) {
                    Text("Keep Remote")
                        .font(.system(size: 12, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(VaultTheme.slate800)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                Button(action: onKeepBoth) {
                    Text("Keep Both")
                        .font(.system(size: 12, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(VaultTheme.accentCyan.opacity(0.2))
                        .foregroundColor(VaultTheme.accentCyan)
                        .cornerRadius(8)
                }
            }
        }
        .padding(16)
        .background(VaultTheme.cardBackground)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.errorRose.opacity(0.5), lineWidth: 1))
    }
}
