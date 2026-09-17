package com.vaultsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.domain.repository.SyncRepository
import com.vaultsync.domain.usecase.ResolveConflictUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConflictsViewModel(
    syncRepository: SyncRepository,
    private val resolveConflictUseCase: ResolveConflictUseCase
) : ViewModel() {

    val conflicts: StateFlow<List<Conflict>> = syncRepository.observeConflicts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun resolve(conflictId: String, resolution: ConflictResolution) {
        viewModelScope.launch {
            resolveConflictUseCase(conflictId, resolution)
        }
    }
}
