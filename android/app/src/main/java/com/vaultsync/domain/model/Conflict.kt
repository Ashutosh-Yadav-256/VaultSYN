package com.vaultsync.domain.model

enum class ConflictResolution {
    KEEP_LOCAL,
    KEEP_REMOTE,
    KEEP_BOTH
}

data class Conflict(
    val id: String,
    val documentId: String,
    val documentName: String,
    val localHash: String,
    val remoteHash: String,
    val localModifiedAt: Long,
    val remoteModifiedAt: Long,
    val detectedAt: Long = System.currentTimeMillis(),
    val resolution: ConflictResolution? = null
)
