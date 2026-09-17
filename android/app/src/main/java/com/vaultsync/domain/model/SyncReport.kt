package com.vaultsync.domain.model

data class SyncReport(
    val totalProcessed: Int,
    val successful: Int,
    val failed: Int,
    val conflictsDetected: Int,
    val durationMs: Long
)
