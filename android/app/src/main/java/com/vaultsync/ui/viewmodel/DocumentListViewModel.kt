package com.vaultsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import com.vaultsync.domain.usecase.DeleteDocumentUseCase
import com.vaultsync.domain.usecase.GetDocumentsUseCase
import com.vaultsync.domain.usecase.ImportDocumentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentListViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val importDocumentUseCase: ImportDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<SyncStatus?>(null)
    val statusFilter: StateFlow<SyncStatus?> = _statusFilter.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val documents: StateFlow<List<Document>> = combine(_searchQuery, _statusFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        getDocumentsUseCase(query = query, filterStatus = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(status: SyncStatus?) {
        _statusFilter.value = status
    }

    fun importFile(name: String, mimeType: String, bytes: ByteArray) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                importDocumentUseCase(name, mimeType, bytes)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to import file"
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                deleteDocumentUseCase(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete document"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
