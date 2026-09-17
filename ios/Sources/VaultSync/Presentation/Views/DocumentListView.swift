import SwiftUI

public struct DocumentListView: View {
    @ObservedObject public var viewModel: DocumentListViewModel
    public let onSelectDocument: (String) -> Void

    public init(viewModel: DocumentListViewModel, onSelectDocument: @escaping (String) -> Void) {
        self.viewModel = viewModel
        self.onSelectDocument = onSelectDocument
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header & Count
            VStack(alignment: .leading, spacing: 4) {
                Text("Documents")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Text("\(viewModel.documents.count) secure documents in vault")
                    .font(.system(size: 14))
                    .foregroundColor(VaultTheme.slate400)
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)

            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(VaultTheme.slate400)
                TextField("Search files by name...", text: $viewModel.searchQuery)
                    .foregroundColor(.white)
                    .onChange(of: viewModel.searchQuery) { _, newValue in
                        viewModel.filterChanged(viewModel.selectedFilter)
                    }
                if !viewModel.searchQuery.isEmpty {
                    Button(action: { viewModel.searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(VaultTheme.slate400)
                    }
                }
            }
            .padding(12)
            .background(VaultTheme.cardBackground)
            .cornerRadius(10)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(VaultTheme.borderSubtle, lineWidth: 1))
            .padding(.horizontal, 20)

            // Filter Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    FilterChipView(
                        title: "All",
                        isSelected: viewModel.selectedFilter == nil,
                        action: { viewModel.filterChanged(nil) }
                    )
                    ForEach(SyncStatus.allCases, id: \.self) { status in
                        FilterChipView(
                            title: status.rawValue.capitalized,
                            isSelected: viewModel.selectedFilter == status,
                            action: { viewModel.filterChanged(status) }
                        )
                    }
                }
                .padding(.horizontal, 20)
            }

            // Documents List
            if viewModel.documents.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Image(systemName: "lock.fill")
                        .font(.system(size: 48))
                        .foregroundColor(VaultTheme.slate800)
                    Text("No documents found")
                        .font(.system(size: 16))
                        .foregroundColor(VaultTheme.slate400)
                    Button(action: {
                        let sampleName = "confidential_spec_\(Int.random(in: 100...999)).pdf"
                        let sampleData = "VAULTSYNC CONFIDENTIAL SPECIFICATION".data(using: .utf8)!
                        viewModel.importFile(name: sampleName, mimeType: "application/pdf", bytes: sampleData)
                    }) {
                        Text("Import Sample Document")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(VaultTheme.accentCyan)
                    }
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else {
                List {
                    ForEach(viewModel.documents) { doc in
                        DocumentRowView(
                            document: doc,
                            onTap: { onSelectDocument(doc.id) },
                            onDelete: { viewModel.deleteDocument(id: doc.id) }
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

struct FilterChipView: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13, weight: .medium))
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(isSelected ? VaultTheme.accentCyan : VaultTheme.slate800)
                .foregroundColor(isSelected ? VaultTheme.slate950 : VaultTheme.slate400)
                .cornerRadius(20)
        }
    }
}

struct DocumentRowView: View {
    let document: Document
    let onTap: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(VaultTheme.slate800)
                    .frame(width: 40, height: 40)
                Image(systemName: "lock.fill")
                    .foregroundColor(VaultTheme.accentCyan)
                    .font(.system(size: 16))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(document.name)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.white)
                    .lineLimit(1)
                HStack(spacing: 6) {
                    Text(formatBytes(document.size))
                        .font(.system(size: 12))
                        .foregroundColor(VaultTheme.slate400)
                    Text("•")
                        .foregroundColor(VaultTheme.slate400)
                    StatusBadgeView(status: document.syncStatus)
                }
            }

            Spacer()

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .foregroundColor(VaultTheme.slate400)
                    .font(.system(size: 15))
            }
            .buttonStyle(BorderlessButtonStyle())
        }
        .padding(12)
        .background(VaultTheme.cardBackground)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(VaultTheme.borderSubtle, lineWidth: 1))
        .contentShape(Rectangle())
        .onTapGesture {
            onTap()
        }
    }
}

struct StatusBadgeView: View {
    let status: SyncStatus

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: iconName)
                .font(.system(size: 10))
                .foregroundColor(tintColor)
            Text(statusText)
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(tintColor)
        }
    }

    private var statusText: String {
        switch status {
        case .synced: return "Verified ✓"
        case .pending: return "Pending"
        case .syncing: return "Syncing..."
        case .conflict: return "Conflict"
        case .failed: return "Failed"
        }
    }

    private var tintColor: Color {
        switch status {
        case .synced: return VaultTheme.verifiedEmerald
        case .pending: return VaultTheme.warningAmber
        case .syncing: return VaultTheme.accentCyan
        case .conflict, .failed: return VaultTheme.errorRose
        }
    }

    private var iconName: String {
        switch status {
        case .synced: return "checkmark.circle.fill"
        case .pending: return "clock.fill"
        case .syncing: return "arrow.triangle.2.circlepath"
        case .conflict, .failed: return "exclamationmark.triangle.fill"
        }
    }
}
