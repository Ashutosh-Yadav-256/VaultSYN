import SwiftUI

public struct DocumentDetailView: View {
    let documentId: String
    @ObservedObject public var viewModel: DocumentDetailViewModel
    public let onBack: () -> Void

    public init(documentId: String, viewModel: DocumentDetailViewModel, onBack: @escaping () -> Void) {
        self.documentId = documentId
        self.viewModel = viewModel
        self.onBack = onBack
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Back Button & Header
                HStack(spacing: 12) {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    Text("Document Details")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                }

                if let doc = viewModel.document {
                    // Document Meta Card
                    VStack(alignment: .leading, spacing: 12) {
                        Text(doc.name)
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)

                        Divider().background(VaultTheme.borderSubtle)

                        MetaRowView(title: "Size", value: formatBytes(doc.size))
                        MetaRowView(title: "MIME Type", value: doc.mimeType)
                        MetaRowView(title: "Created", value: doc.createdAt.formatted(date: .abbreviated, time: .shortened))
                        MetaRowView(title: "Modified", value: doc.modifiedAt.formatted(date: .abbreviated, time: .shortened))
                        MetaRowView(title: "Local Ciphertext", value: doc.localPath)
                    }
                    .padding(16)
                    .background(VaultTheme.cardBackground)
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))

                    // SHA-256 Digest Card
                    VStack(alignment: .leading, spacing: 8) {
                        Text("SHA-256 CHECKSUM")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(VaultTheme.accentCyan)
                            .tracking(1)

                        Text(doc.sha256)
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundColor(VaultTheme.slate400)
                            .textSelection(.enabled)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(VaultTheme.cardBackground)
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))

                    // Integrity Verification Card
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            Text("INTEGRITY VERIFICATION")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(VaultTheme.slate400)
                                .tracking(1)
                            Spacer()
                            Image(systemName: "checkmark.shield.fill")
                                .foregroundColor(VaultTheme.accentCyan)
                        }

                        if let result = viewModel.integrityResult {
                            HStack(spacing: 8) {
                                Image(systemName: result.isValid ? "checkmark.circle.fill" : "xmark.circle.fill")
                                    .foregroundColor(result.isValid ? VaultTheme.verifiedEmerald : VaultTheme.errorRose)
                                Text(result.isValid ? "Integrity Verified (0 Bit-Rot)" : "Integrity Failure (File Mismatch)")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(result.isValid ? VaultTheme.verifiedEmerald : VaultTheme.errorRose)
                            }
                        }

                        Button(action: {
                            viewModel.verifyIntegrity(id: doc.id)
                        }) {
                            HStack {
                                if viewModel.isVerifying {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    Text("Verifying SHA-256...")
                                } else {
                                    Text("Verify Integrity Now")
                                        .fontWeight(.semibold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 44)
                            .background(VaultTheme.slate800)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                        .disabled(viewModel.isVerifying)
                    }
                    .padding(16)
                    .background(VaultTheme.cardBackground)
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))
                } else {
                    ProgressView().padding(.top, 40)
                }
            }
            .padding(20)
        }
        .background(VaultTheme.slate950.ignoresSafeArea())
        .onAppear {
            viewModel.loadDocument(id: documentId)
        }
    }
}

struct MetaRowView: View {
    let title: String
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .font(.system(size: 13))
                .foregroundColor(VaultTheme.slate400)
            Spacer()
            Text(value)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.white)
        }
    }
}
