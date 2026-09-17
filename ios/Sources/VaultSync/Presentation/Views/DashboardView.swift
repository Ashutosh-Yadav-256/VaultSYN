import SwiftUI

public struct DashboardView: View {
    @ObservedObject public var viewModel: DashboardViewModel

    public init(viewModel: DashboardViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Header
                HStack(spacing: 12) {
                    Image(systemName: "lock.icloud.fill")
                        .font(.system(size: 30))
                        .foregroundColor(VaultTheme.accentCyan)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("VaultSync")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.white)
                        Text("Secure Local-First Document Manager")
                            .font(.system(size: 13))
                            .foregroundColor(VaultTheme.slate400)
                    }
                }
                .padding(.top, 10)

                // 2x2 Metric Cards Grid
                LazyVGrid(columns: [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)], spacing: 12) {
                    MetricCardView(
                        title: "Documents",
                        value: "\(viewModel.summary.totalDocuments)",
                        icon: "doc.text.fill",
                        tint: VaultTheme.accentCyan
                    )
                    MetricCardView(
                        title: "Synced",
                        value: "\(viewModel.summary.syncedCount)",
                        icon: "checkmark.circle.fill",
                        tint: VaultTheme.verifiedEmerald
                    )
                    MetricCardView(
                        title: "Pending",
                        value: "\(viewModel.summary.pendingCount)",
                        icon: "arrow.triangle.2.circlepath",
                        tint: VaultTheme.warningAmber
                    )
                    MetricCardView(
                        title: "Conflicts",
                        value: "\(viewModel.summary.conflictCount)",
                        icon: "exclamationmark.triangle.fill",
                        tint: viewModel.summary.conflictCount > 0 ? VaultTheme.errorRose : VaultTheme.slate400
                    )
                }

                // Sync Now Button
                Button(action: {
                    viewModel.syncNow()
                }) {
                    HStack {
                        if viewModel.summary.isSyncing {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: VaultTheme.slate950))
                            Text("Syncing Vault...")
                                .fontWeight(.bold)
                        } else {
                            Image(systemName: "arrow.triangle.2.circlepath")
                            Text("Sync Now")
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(VaultTheme.accentCyan)
                    .foregroundColor(VaultTheme.slate950)
                    .cornerRadius(12)
                }
                .disabled(viewModel.summary.isSyncing)

                // Hardware Security Architecture Card
                VStack(alignment: .leading, spacing: 8) {
                    Text("HARDWARE-BACKED ENCRYPTION")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(VaultTheme.verifiedEmerald)
                        .tracking(1)

                    Text("AES-256-GCM + SHA-256")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)

                    Text("Ciphertext is stored on disk with nonces and auth tags. Keys are secured in Apple Keychain and Secure Enclave.")
                        .font(.system(size: 13))
                        .foregroundColor(VaultTheme.slate400)
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(VaultTheme.cardBackground)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))
            }
            .padding(20)
        }
        .background(VaultTheme.slate950.ignoresSafeArea())
    }
}

struct MetricCardView: View {
    let title: String
    let value: String
    let icon: String
    let tint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.system(size: 13))
                    .foregroundColor(VaultTheme.slate400)
                Spacer()
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(tint)
            }
            Text(value)
                .font(.system(size: 26, weight: .bold))
                .foregroundColor(.white)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(VaultTheme.cardBackground)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))
    }
}
