package com.vaultsync.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vaultsync.VaultSyncApplication

class VaultSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as VaultSyncApplication
            val report = app.startSyncUseCase()
            if (report.failed > 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "vaultsync_periodic_sync"
    }
}
