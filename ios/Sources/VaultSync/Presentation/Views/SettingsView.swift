import SwiftUI

public struct SettingsView: View {
    @ObservedObject public var viewModel: SettingsViewModel

    public init(viewModel: SettingsViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Settings")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                    Text("Security parameters and synchronization policy")
                        .font(.system(size: 14))
                        .foregroundColor(VaultTheme.slate400)
                }
                .padding(.top, 10)

                VStack(alignment: .leading, spacing: 14) {
                    Text("SECURITY & KEY MANAGEMENT")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(VaultTheme.slate400)
                        .tracking(1)

                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Cipher Protocol")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            Text(viewModel.state.cipherProtocol)
                                .font(.system(size: 12))
                                .foregroundColor(VaultTheme.slate400)
                        }
                        Spacer()
                        HStack(spacing: 4) {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(VaultTheme.verifiedEmerald)
                            Text("Active")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(VaultTheme.verifiedEmerald)
                        }
                    }

                    Divider().background(VaultTheme.borderSubtle)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Hardware Key Storage")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white)
                        Text(viewModel.state.keyStorage)
                            .font(.system(size: 12))
                            .foregroundColor(VaultTheme.slate400)
                    }
                }
                .padding(16)
                .background(VaultTheme.cardBackground)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))

                VStack(alignment: .leading, spacing: 14) {
                    Text("SYNCHRONIZATION POLICY")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(VaultTheme.slate400)
                        .tracking(1)

                    Toggle(isOn: Binding(
                        get: { viewModel.state.isWifiOnly },
                        set: { viewModel.toggleWifiOnly($0) }
                    )) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Wi-Fi Only Sync")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            Text("Prevent cellular data usage")
                                .font(.system(size: 12))
                                .foregroundColor(VaultTheme.slate400)
                        }
                    }
                    .tint(VaultTheme.accentCyan)

                    Divider().background(VaultTheme.borderSubtle)

                    Toggle(isOn: Binding(
                        get: { viewModel.state.isBackgroundSyncEnabled },
                        set: { viewModel.toggleBackgroundSync($0) }
                    )) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Background Sync")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            Text("Scheduled via BGTaskScheduler")
                                .font(.system(size: 12))
                                .foregroundColor(VaultTheme.slate400)
                        }
                    }
                    .tint(VaultTheme.accentCyan)
                }
                .padding(16)
                .background(VaultTheme.cardBackground)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))

                VStack(alignment: .leading, spacing: 14) {
                    Text("STORAGE USAGE")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(VaultTheme.slate400)
                        .tracking(1)

                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Encrypted Vault Storage")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            Text("\(viewModel.state.documentCount) items stored")
                                .font(.system(size: 12))
                                .foregroundColor(VaultTheme.slate400)
                        }
                        Spacer()
                        Text(formatBytes(viewModel.state.totalStorageBytes))
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(VaultTheme.accentCyan)
                    }
                }
                .padding(16)
                .background(VaultTheme.cardBackground)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))
            }
            .padding(20)
        }
        .background(VaultTheme.slate950.ignoresSafeArea())
    }
}
