package com.vaultsync.domain.repository

import com.vaultsync.domain.model.SyncReport
import kotlinx.coroutines.flow.StateFlow

interface SyncController {
    val isSyncing: StateFlow<Boolean>
    suspend fun startSync(): SyncReport
    suspend fun pauseSync()
    suspend fun resumeSync()
}
