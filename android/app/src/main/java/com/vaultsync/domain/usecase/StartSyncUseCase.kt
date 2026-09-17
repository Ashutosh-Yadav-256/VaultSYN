package com.vaultsync.domain.usecase

import com.vaultsync.domain.model.SyncReport
import com.vaultsync.domain.repository.SyncController

class StartSyncUseCase(
    private val syncController: SyncController
) {
    suspend operator fun invoke(): SyncReport {
        return syncController.startSync()
    }
}
