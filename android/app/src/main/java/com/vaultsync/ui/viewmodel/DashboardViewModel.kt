package com.vaultsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultsync.domain.model.SyncReport
import com.vaultsync.domain.model.SyncStatusSummary
import com.vaultsync.domain.usecase.GetSyncStatusUseCase
import com.vaultsync.domain.usecase.StartSyncUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    getSyncStatusUseCase: GetSyncStatusUseCase,
    private val startSyncUseCase: StartSyncUseCase
) : ViewModel() {

    val statusSummary: StateFlow<SyncStatusSummary> = getSyncStatusUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatusSummary(0, 0, 0, 0, false)
        )

    private val _syncReports = MutableSharedFlow<SyncReport>()
    val syncReports: SharedFlow<SyncReport> = _syncReports.asSharedFlow()

    fun syncNow() {
        viewModelScope.launch {
            val report = startSyncUseCase()
            _syncReports.emit(report)
        }
    }
}
