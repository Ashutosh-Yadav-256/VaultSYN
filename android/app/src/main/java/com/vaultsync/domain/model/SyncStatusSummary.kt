package com.vaultsync.domain.model

data class SyncStatusSummary(
    val totalDocuments: Int,
    val syncedCount: Int,
    val pendingCount: Int,
    val conflictCount: Int,
    val isSyncing: Boolean = false
)
