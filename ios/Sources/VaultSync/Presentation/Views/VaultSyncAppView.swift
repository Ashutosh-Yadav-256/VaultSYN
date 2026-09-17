import SwiftUI

public struct VaultSyncAppView: View {
    @StateObject private var dashboardVM: DashboardViewModel
    @StateObject private var documentListVM: DocumentListViewModel
    @StateObject private var documentDetailVM: DocumentDetailViewModel
    @StateObject private var conflictsVM: ConflictsViewModel
    @StateObject private var settingsVM: SettingsViewModel

    @State private var selectedTab: Int = 0
    @State private var selectedDocId: String?

    public init(
        dashboardVM: DashboardViewModel,
        documentListVM: DocumentListViewModel,
        documentDetailVM: DocumentDetailViewModel,
        conflictsVM: ConflictsViewModel,
        settingsVM: SettingsViewModel
    ) {
        _dashboardVM = StateObject(wrappedValue: dashboardVM)
        _documentListVM = StateObject(wrappedValue: documentListVM)
        _documentDetailVM = StateObject(wrappedValue: documentDetailVM)
        _conflictsVM = StateObject(wrappedValue: conflictsVM)
        _settingsVM = StateObject(wrappedValue: settingsVM)
    }

    public var body: some View {
        ZStack {
            VaultTheme.slate950.ignoresSafeArea()

            if let docId = selectedDocId {
                DocumentDetailView(
                    documentId: docId,
                    viewModel: documentDetailVM,
                    onBack: { selectedDocId = nil }
                )
            } else {
                TabView(selection: $selectedTab) {
                    DashboardView(viewModel: dashboardVM)
                        .tabItem {
                            Label("Dashboard", systemImage: "lock.icloud")
                        }
                        .tag(0)

                    DocumentListView(viewModel: documentListVM, onSelectDocument: { id in
                        selectedDocId = id
                    })
                    .tabItem {
                        Label("Documents", systemImage: "doc.text")
                    }
                    .tag(1)

                    ConflictsView(viewModel: conflictsVM)
                        .tabItem {
                            Label("Conflicts", systemImage: "exclamationmark.triangle")
                        }
                        .tag(2)

                    SettingsView(viewModel: settingsVM)
                        .tabItem {
                            Label("Settings", systemImage: "gearshape")
                        }
                        .tag(3)
                }
                .tint(VaultTheme.accentCyan)
            }
        }
    }
}
