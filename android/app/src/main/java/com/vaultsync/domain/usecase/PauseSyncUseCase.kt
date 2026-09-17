package com.vaultsync.domain.usecase

import com.vaultsync.domain.repository.SyncController

class PauseSyncUseCase(
    private val syncController: SyncController
) {
    suspend operator fun invoke() {
        syncController.pauseSync()
    }
}
