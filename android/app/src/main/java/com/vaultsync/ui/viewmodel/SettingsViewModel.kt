package com.vaultsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultsync.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val encryptionAlgorithm: String = "AES-256-GCM",
    val keyStoreProvider: String = "AndroidKeyStore (Hardware TEE)",
    val isWifiOnly: Boolean = true,
    val isBackgroundSyncEnabled: Boolean = true,
    val totalStorageUsedBytes: Long = 0L,
    val documentCount: Int = 0
)

class SettingsViewModel(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val docs = documentRepository.getDocuments()
            val totalBytes = docs.sumOf { it.size }
            _state.value = _state.value.copy(
                totalStorageUsedBytes = totalBytes,
                documentCount = docs.size
            )
        }
    }

    fun toggleWifiOnly(enabled: Boolean) {
        _state.value = _state.value.copy(isWifiOnly = enabled)
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        _state.value = _state.value.copy(isBackgroundSyncEnabled = enabled)
    }
}
