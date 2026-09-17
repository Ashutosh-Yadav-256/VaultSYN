import SwiftUI
import Combine

@MainActor
public final class DocumentListViewModel: ObservableObject {
    @Published public var documents: [Document] = []
    @Published public var searchQuery: String = ""
    @Published public var selectedFilter: SyncStatus?
    @Published public var errorMessage: String?

    private let getDocumentsUseCase: GetDocumentsUseCase
    private let importDocumentUseCase: ImportDocumentUseCase
    private let deleteDocumentUseCase: DeleteDocumentUseCase

    public init(
        getDocumentsUseCase: GetDocumentsUseCase,
        importDocumentUseCase: ImportDocumentUseCase,
        deleteDocumentUseCase: DeleteDocumentUseCase
    ) {
        self.getDocumentsUseCase = getDocumentsUseCase
        self.importDocumentUseCase = importDocumentUseCase
        self.deleteDocumentUseCase = deleteDocumentUseCase

        Task {
            await observeDocuments()
        }
    }

    public func observeDocuments() async {
        for await docs in getDocumentsUseCase.execute(query: searchQuery, filterStatus: selectedFilter) {
            self.documents = docs
        }
    }

    public func filterChanged(_ status: SyncStatus?) {
        selectedFilter = status
        Task {
            if let direct = try? await getDocumentsUseCase.executeDirect() {
                self.documents = direct.filter { doc in
                    let matchesQuery = searchQuery.isEmpty || doc.name.localizedCaseInsensitiveContains(searchQuery)
                    let matchesStatus = selectedFilter == nil || doc.syncStatus == selectedFilter
                    return matchesQuery && matchesStatus
                }
            }
        }
    }

    public func importFile(name: String, mimeType: String, bytes: Data) {
        Task {
            do {
                _ = try await importDocumentUseCase.execute(name: name, mimeType: mimeType, bytes: bytes)
                errorMessage = nil
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    public func deleteDocument(id: String) {
        Task {
            do {
                try await deleteDocumentUseCase.execute(documentId: id)
                errorMessage = nil
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
