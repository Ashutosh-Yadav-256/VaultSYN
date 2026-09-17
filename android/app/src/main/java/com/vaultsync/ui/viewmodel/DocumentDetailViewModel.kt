package com.vaultsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.IntegrityResult
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.usecase.VerifyIntegrityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentDetailViewModel(
    private val documentRepository: DocumentRepository,
    private val verifyIntegrityUseCase: VerifyIntegrityUseCase
) : ViewModel() {

    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    private val _integrityResult = MutableStateFlow<IntegrityResult?>(null)
    val integrityResult: StateFlow<IntegrityResult?> = _integrityResult.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    fun loadDocument(id: String) {
        viewModelScope.launch {
            _document.value = documentRepository.getDocument(id)
            _integrityResult.value = null
        }
    }

    fun verifyIntegrity(id: String) {
        viewModelScope.launch {
            _isVerifying.value = true
            try {
                _integrityResult.value = verifyIntegrityUseCase(id)
            } finally {
                _isVerifying.value = false
            }
        }
    }
}
